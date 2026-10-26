
package de.shyyyuki.infiniteVillagerTrading;

import de.shyyyuki.infiniteVillagerTrading.commands.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class InfiniteVillagerTrading extends JavaPlugin
        implements Listener {

    private final Map<UUID, List<Integer>> originalPrices = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("infinitevillagertrading") != null) {
            ReloadCommand handler = new ReloadCommand(this);
            Objects.requireNonNull(
                    getCommand("infinitevillagertrading")
            ).setExecutor(handler);
            Objects.requireNonNull(
                    getCommand("infinitevillagertrading")
            ).setTabCompleter(handler);
        }

        getLogger().info("Infinite Villager Trading enabled.");
    }

    @Override
    public void onDisable() {
        originalPrices.clear();
        getLogger().info("Infinite Villager Trading disabled.");
    }

    /**
     * Removes normal trade usage limits when a merchant inventory opens.
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!getConfig().getBoolean("enabled", true)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof Merchant)) {
            return;
        }

        Merchant merchant = (Merchant) event.getInventory().getHolder();
        List<MerchantRecipe> recipes = new ArrayList<>();
        boolean changed = false;

        for (MerchantRecipe recipe : merchant.getRecipes()) {
            if (getConfig().getBoolean(
                    "prevent-price-increase-on-hit", true)
                    && merchant instanceof Villager) {
                Villager villager = (Villager) merchant;
                List<Integer> prices = originalPrices.get(villager.getUniqueId());

                if (prices != null && recipes.size() < prices.size()) {
                    int originalPrice = prices.get(recipes.size());
                    recipe.setSpecialPrice(0);
                    recipe.setDemand(0);

                    // Restore the original first ingredient amount.
                    List<org.bukkit.inventory.ItemStack> ingredients =
                            recipe.getIngredients();

                    if (!ingredients.isEmpty()) {
                        ingredients.get(0).setAmount(originalPrice);
                        recipe.setIngredients(ingredients);
                    }

                    changed = true;
                }
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
     * Saves villager trade prices when a player attacks a villager.
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onVillagerDamage(EntityDamageByEntityEvent event) {
        if (!getConfig().getBoolean(
                "prevent-price-increase-on-hit", true)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Villager)) {
            return;
        }

        Villager villager = (Villager) event.getEntity();

        List<Integer> prices = new ArrayList<>();
        for (MerchantRecipe recipe : villager.getRecipes()) {
            List<org.bukkit.inventory.ItemStack> ingredients =
                    recipe.getIngredients();

            prices.add(ingredients.isEmpty()
                    ? 1
                    : ingredients.get(0).getAmount());
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