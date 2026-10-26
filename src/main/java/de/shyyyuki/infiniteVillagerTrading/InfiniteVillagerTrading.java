
package de.shyyyuki.infiniteVillagerTrading;

import de.shyyyuki.infiniteVillagerTrading.commands.ReloadCommand;
import de.shyyyuki.infiniteVillagerTrading.config.ConfigManager;
import de.shyyyuki.infiniteVillagerTrading.utils.MessagesUtil;
import de.shyyyuki.infiniteVillagerTrading.utils.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Main class of the InfiniteVillagerTrading plugin.
 *
 * <p>This plugin removes villager trade usage limits and provides
 * an optional workaround for price increases after villager attacks.</p>
 *
 * @author Shyyyuki
 * @since 1.0
 */
public final class InfiniteVillagerTrading extends JavaPlugin implements Listener {

    private final Map<UUID, List<Integer>> originalPrices = new HashMap<>();

    private ConfigManager configManager;

    /**
     * Initializes the plugin, configuration, language files,
     * event listeners and main command.
     */
    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.initialize();

        MessagesUtil.initialize(this);

        UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.initialize();

        getServer().getPluginManager().registerEvents(this, this);

        ReloadCommand handler = new ReloadCommand(this);

        if (getCommand("infinitevillagertrading") != null) {
            Objects.requireNonNull(
                    getCommand("infinitevillagertrading")
            ).setExecutor(handler);

            Objects.requireNonNull(
                    getCommand("infinitevillagertrading")
            ).setTabCompleter(handler);
        }

        getLogger().info("Infinite Villager Trading enabled.");
    }

    /**
     * Shuts down the plugin and clears stored trade price data.
     */
    @Override
    public void onDisable() {
        originalPrices.clear();
        getLogger().info("Infinite Villager Trading disabled.");
    }

    /**
     * Returns the configuration manager.
     *
     * @return the configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Removes usage limits from merchant trades when an inventory opens.
     *
     * <p>When price protection is enabled for villagers, this handler
     * restores stored ingredient amounts and resets price modifiers.</p>
     *
     * @param event the inventory open event
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!configManager.isEnabled()) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof Merchant merchant)) {
            return;
        }

        List<MerchantRecipe> recipes = new ArrayList<>();
        boolean changed = false;

        List<Integer> prices = null;

        if (configManager.isPriceProtectionEnabled()
                && merchant instanceof Villager villager) {
            prices = originalPrices.get(villager.getUniqueId());
        }

        for (MerchantRecipe recipe : merchant.getRecipes()) {
            if (prices != null && recipes.size() < prices.size()) {
                int originalPrice = prices.get(recipes.size());

                recipe.setSpecialPrice(0);
                recipe.setDemand(0);

                List<ItemStack> ingredients = recipe.getIngredients();

                if (!ingredients.isEmpty()) {
                    ingredients.getFirst().setAmount(originalPrice);
                    recipe.setIngredients(ingredients);
                }

                changed = true;
            }

            if (recipe.getMaxUses() != Integer.MAX_VALUE
                    || recipe.getUses() != 0) {
                recipe.setMaxUses(Integer.MAX_VALUE);
                recipe.setUses(0);
                changed = true;
            }

            recipes.add(recipe);
        }

        if (changed) {
            merchant.setRecipes(recipes);
        }
    }

    /**
     * Stores villager trade prices when a player attacks a villager.
     *
     * <p>If price protection is enabled, the handler records original
     * ingredient amounts and schedules a task to reset trade modifiers.</p>
     *
     * @param event the entity damage event
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onVillagerDamage(EntityDamageByEntityEvent event) {
        if (!configManager.isPriceProtectionEnabled()) {
            return;
        }

        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Villager villager)) {
            return;
        }

        List<Integer> prices = new ArrayList<>();

        for (MerchantRecipe recipe : villager.getRecipes()) {
            List<ItemStack> ingredients = recipe.getIngredients();

            prices.add(ingredients.isEmpty()
                    ? 1
                    : ingredients.getFirst().getAmount());
        }

        originalPrices.put(villager.getUniqueId(), prices);

        getServer().getScheduler().runTask(this, () -> {
            if (!villager.isValid()) {
                return;
            }

            List<MerchantRecipe> recipes = new ArrayList<>();

            for (MerchantRecipe original : villager.getRecipes()) {
                MerchantRecipe recipe = new MerchantRecipe(
                        original.getResult(),
                        0,
                        Integer.MAX_VALUE,
                        original.hasExperienceReward(),
                        original.getVillagerExperience(),
                        original.getPriceMultiplier(),
                        0,
                        0
                );

                recipe.setIngredients(original.getIngredients());
                recipe.setUses(0);

                recipes.add(recipe);
            }

            villager.setRecipes(recipes);
        });
    }
}