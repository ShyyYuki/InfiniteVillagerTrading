
package de.shyyyuki.infiniteVillagerTrading.config;

import de.shyyyuki.infiniteVillagerTrading.InfiniteVillagerTrading;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages the plugin configuration and provides access to its settings.
 *
 * @author Shyyyuki
 * @since 1.0
 */
public final class ConfigManager {

    private final InfiniteVillagerTrading plugin;

    /**
     * Creates a new configuration manager.
     *
     * @param plugin the main plugin instance
     */
    public ConfigManager(InfiniteVillagerTrading plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the default configuration if config.yml does not exist.
     */
    public void initialize() {
        plugin.saveDefaultConfig();
    }

    /**
     * Reloads config.yml from disk.
     */
    public void reload() {
        plugin.reloadConfig();
    }

    /**
     * Returns the current plugin configuration.
     *
     * @return the current configuration
     */
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    /**
     * Checks whether the plugin's trading functionality is enabled.
     *
     * @return true if the plugin functionality is enabled
     */
    public boolean isEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    /**
     * Checks whether villager price increases after player attacks
     * should be prevented.
     *
     * @return true if price protection is enabled
     */
    public boolean isPriceProtectionEnabled() {
        return getConfig().getBoolean(
                "prevent-price-increase-on-hit",
                true
        );
    }
}