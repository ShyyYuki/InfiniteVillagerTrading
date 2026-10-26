
package de.shyyyuki.infiniteVillagerTrading.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.shyyyuki.infiniteVillagerTrading.InfiniteVillagerTrading;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks Modrinth for newer releases of InfiniteVillagerTrading.
 *
 * <p>Update checks are performed asynchronously to avoid blocking the
 * Minecraft server thread. Operators are notified when an update is available
 * and can open the Modrinth or GitHub release page directly from chat.</p>
 *
 * <p>This class uses Bukkit APIs and is intended for Spigot, Paper, and
 * Purpur servers.</p>
 */
public final class UpdateChecker implements Listener {

    /** Modrinth API endpoint containing the project's published versions. */
    private static final String MODRINTH_API =
            "https://api.modrinth.com/v2/project/Olv4K3nb/version";;

    /** Web page listing all published Modrinth versions. */
    private static final String MODRINTH_URL =
            "https://modrinth.com/plugin/infinite-villager-trading/versions";

    /** GitHub page listing the project's releases. */
    private static final String GITHUB_URL =
            "https://github.com/ShyyYuki/InfiniteVillagerTrading/releases";

    /** Plugin instance used for logging and Bukkit scheduler access. */
    private final InfiniteVillagerTrading plugin;

    /** Whether a newer release than the installed version has been found. */
    private volatile boolean updateAvailable = false;

    /** Version number of the latest detected release. */
    private volatile String latestVersion = "";

    /**
     * Creates a new update checker.
     *
     * @param plugin the plugin instance
     */
    public UpdateChecker(InfiniteVillagerTrading plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers this class as a Bukkit event listener and starts the update
     * check asynchronously.
     */
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Perform network requests asynchronously to keep the server responsive.
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::checkForUpdates);
    }

    /**
     * Requests the published version list from Modrinth and compares the
     * newest stable release with the installed plugin version.
     *
     * <p>Network operations run asynchronously. Bukkit player notifications
     * are scheduled on the main server thread.</p>
     */
    private void checkForUpdates() {
        HttpURLConnection connection = null;

        try {
            URL url = URI.create(MODRINTH_API).toURL();
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty(
                    "User-Agent",
                    "InfiniteVillagerTrading/1.0 (Minecraft plugin)"
            );

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                plugin.getLogger().warning(
                        "Could not check for updates. HTTP status: " + responseCode
                );
                return;
            }

            StringBuilder response = new StringBuilder();

            try (InputStream input = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(input, StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String latest = findLatestRelease(response.toString());

            if (latest == null || latest.isBlank()) {
                plugin.getLogger().warning(
                        "Could not determine the latest release version."
                );
                return;
            }

            String current = plugin.getDescription().getVersion();

            if (compareVersions(latest, current) > 0) {
                latestVersion = latest;
                updateAvailable = true;

                // Bukkit player operations must run on the main server thread.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info(
                            "A new version is available: " + latestVersion
                    );

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.isOp()) {
                            sendUpdateMessage(player);
                        }
                    }
                });
            } else {
                plugin.getLogger().info(
                        "The plugin is up to date. Current version: " + current
                );
            }

        } catch (Exception exception) {
            plugin.getLogger().warning(
                    "Update check failed: " + exception.getMessage()
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Finds the newest stable release in the Modrinth API response.
     *
     * @param json the JSON response containing published versions
     * @return the latest release version, or null if none was found
     */
    private String findLatestRelease(String json) {
        JsonArray versions = JsonParser.parseString(json).getAsJsonArray();

        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();

            String versionType = version.has("version_type")
                    ? version.get("version_type").getAsString()
                    : "";

            if ("release".equals(versionType)
                    && version.has("version_number")) {
                return version.get("version_number").getAsString();
            }
        }

        return null;
    }

    /**
     * Extracts a string property from a JSON object using a regular expression.
     *
     * <p>This helper is intended for the simple string fields used by the
     * Modrinth response, not as a general-purpose JSON parser.</p>
     *
     * @param json the JSON text to search
     * @param key the property name
     * @return the property's string value, or {@code null} if it is not found
     */
    private String extractJsonString(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key)
                        + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Compares two numeric version strings.
     *
     * <p>A leading {@code v} is ignored, and suffixes separated by a dot,
     * plus sign, or hyphen are not treated as numeric version components.</p>
     *
     * @param first the candidate version
     * @param second the installed version
     * @return a positive number if {@code first} is newer, zero if equivalent,
     *         or a negative number if {@code first} is older
     */
    private int compareVersions(String first, String second) {
        String[] firstParts = first.replaceFirst("^[vV]", "").split("[.+-]");
        String[] secondParts = second.replaceFirst("^[vV]", "").split("[.+-]");

        int length = Math.max(firstParts.length, secondParts.length);

        for (int i = 0; i < length; i++) {
            int firstNumber = parseVersionPart(
                    i < firstParts.length ? firstParts[i] : "0"
            );
            int secondNumber = parseVersionPart(
                    i < secondParts.length ? secondParts[i] : "0"
            );

            if (firstNumber != secondNumber) {
                return Integer.compare(firstNumber, secondNumber);
            }
        }

        return 0;
    }

    /**
     * Parses the leading integer from a version component.
     *
     * @param part the version component to parse
     * @return the parsed integer, or zero if it cannot be parsed
     */
    private int parseVersionPart(String part) {
        Matcher matcher = Pattern.compile("^\\d+").matcher(part);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        return 0;
    }

    /**
     * Notifies operators who join after an update has been detected.
     *
     * @param event the player join event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp() && updateAvailable) {
            // Delay the message slightly so it does not appear during login.
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && player.isOp()) {
                    sendUpdateMessage(player);
                }
            }, 40L);
        }
    }

    /**
     * Sends the update notification and clickable download-page links to
     * an operator.
     *
     * @param player the operator receiving the notification
     */
    private void sendUpdateMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(
                ChatColor.GOLD.toString() + ChatColor.BOLD
                        + "[InfiniteVillagerTrading] Update available!"
        );
        player.sendMessage(
                ChatColor.GRAY + "Installed version: "
                        + ChatColor.WHITE + plugin.getDescription().getVersion()
        );
        player.sendMessage(
                ChatColor.GRAY + "Latest version: "
                        + ChatColor.GREEN + latestVersion
        );

        TextComponent modrinth = createLink(
                " [Modrinth] ",
                ChatColor.GREEN,
                MODRINTH_URL
        );

        TextComponent github = createLink(
                " [GitHub Releases] ",
                ChatColor.AQUA,
                GITHUB_URL
        );

        player.spigot().sendMessage(modrinth, github);
        player.sendMessage("");
    }

    /**
     * Creates a colored chat component that opens a URL when clicked and
     * displays a hover tooltip.
     *
     * @param text the text displayed in chat
     * @param color the text color
     * @param url the URL opened when the component is clicked
     * @return the configured clickable chat component
     */
    private TextComponent createLink(String text, ChatColor color, String url) {
        TextComponent component = new TextComponent(text);
        component.setColor(color);
        component.setUnderlined(true);
        component.setClickEvent(
                new ClickEvent(ClickEvent.Action.OPEN_URL, url)
        );
        component.setHoverEvent(
                new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Text("Click to open " + url)
                )
        );

        return component;
    }
}