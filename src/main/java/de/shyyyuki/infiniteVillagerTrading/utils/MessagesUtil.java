package de.shyyyuki.infiniteVillagerTrading.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;

/**
 * Manages localized messages for the InfiniteVillagerTrading plugin.
 *
 * <p>Language files are stored in the plugin's lang directory.
 * The selected language is configured through the language setting
 * in config.yml.</p>
 *
 * <p>If a language file or message is missing, English is used
 * as the fallback language.</p>
 *
 * @author Shyyyuki
 * @since 1.0
 */
public final class MessagesUtil {

    private static JavaPlugin plugin;
    private static YamlConfiguration messages;
    private static YamlConfiguration fallbackMessages;

    private static final String PREFIX_KEY = "prefix";
    private static final String DEFAULT_LANGUAGE = "en_us";

    private MessagesUtil() {
        throw new UnsupportedOperationException(
                "This utility class cannot be instantiated."
        );
    }

    /**
     * Initializes the message manager and loads the selected language.
     *
     * @param javaPlugin the main plugin instance
     */
    public static void initialize(JavaPlugin javaPlugin) {
        plugin = javaPlugin;

        File langDirectory = new File(
                plugin.getDataFolder(), "lang"
        );

        if (!langDirectory.exists() && !langDirectory.mkdirs()) {
            plugin.getLogger().warning(
                    "Could not create the language directory."
            );
        }

        saveLanguageFile(DEFAULT_LANGUAGE);
        saveLanguageFile("de_de");

        reload();
    }

    /**
     * Loads a bundled language file if it does not already exist.
     *
     * @param language the language code
     */
    private static void saveLanguageFile(String language) {
        File file = new File(
                new File(plugin.getDataFolder(), "lang"),
                language + ".yml"
        );

        if (!file.exists()) {
            plugin.saveResource("lang/" + language + ".yml", false);
        }
    }

    /**
     * Reloads the selected language and English fallback messages.
     */
    public static void reload() {
        if (plugin == null) {
            throw new IllegalStateException(
                    "MessagesUtil has not been initialized."
            );
        }

        File langDirectory = new File(
                plugin.getDataFolder(), "lang"
        );

        fallbackMessages = YamlConfiguration.loadConfiguration(
                new File(langDirectory, DEFAULT_LANGUAGE + ".yml")
        );

        String language = plugin.getConfig()
                .getString("language", DEFAULT_LANGUAGE);

        language = language == null
                ? DEFAULT_LANGUAGE
                : language.toLowerCase(Locale.ROOT);

        File languageFile = new File(
                langDirectory, language + ".yml"
        );

        if (!languageFile.isFile()) {
            plugin.getLogger().warning(
                    "Language '" + language
                            + "' was not found. Falling back to en_us."
            );

            languageFile = new File(
                    langDirectory, DEFAULT_LANGUAGE + ".yml"
            );
        }

        messages = YamlConfiguration.loadConfiguration(languageFile);

        plugin.getLogger().info(
                "Loaded language: " + languageFile.getName()
        );
    }

    /**
     * Returns a localized message with translated color codes.
     *
     * @param key the message key
     * @return the formatted message
     */
    public static String get(String key) {
        if (messages == null || fallbackMessages == null) {
            return ChatColor.RED + "Message system is not initialized.";
        }

        String message = messages.getString(
                key, fallbackMessages.getString(key, key)
        );

        String prefix = messages.getString(
                PREFIX_KEY,
                fallbackMessages.getString(PREFIX_KEY, "")
        );

        message = message.replace("%prefix%", prefix);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Returns a localized message with a placeholder replaced.
     *
     * @param key the message key
     * @param placeholder the placeholder name without percent signs
     * @param value the replacement value
     * @return the formatted message
     */
    public static String get(
            String key, String placeholder, String value
    ) {
        return get(key).replace(
                "%" + placeholder + "%", value
        );
    }
}