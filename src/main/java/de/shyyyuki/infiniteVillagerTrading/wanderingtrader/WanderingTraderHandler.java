package de.shyyyuki.infiniteVillagerTrading.wanderingtrader;

import de.shyyyuki.infiniteVillagerTrading.InfiniteVillagerTrading;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles infinite trading and price protection
 * for wandering traders.
 *
 * @author Shyyyuki
 * @since 1.0
 */
public final class WanderingTraderHandler implements Listener {

    private final InfiniteVillagerTrading plugin;

    private final Map<UUID, List<Integer>> originalPrices = new HashMap<>();

    public WanderingTraderHandler(InfiniteVillagerTrading plugin) {
        this.plugin = plugin;
    }

    /**
     * Removes usage limits and restores original prices.
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.getConfigManager().isEnabled()
                || !plugin.getConfigManager().isWanderingTraderEnabled()) {
            return;
        }

        if (!(event.getInventory() instanceof MerchantInventory inventory)
                || !(inventory.getMerchant() instanceof WanderingTrader trader)) {
            return;
        }

        List<Integer> prices = null;

        if (plugin.getConfigManager().isWanderingTraderPriceProtectionEnabled()) {
            prices = originalPrices.get(trader.getUniqueId());
        }

        List<MerchantRecipe> recipes = new ArrayList<>();
        boolean changed = false;

        for (MerchantRecipe recipe : trader.getRecipes()) {
            if (prices != null && recipes.size() < prices.size()) {
                recipe.setSpecialPrice(0);
                recipe.setDemand(0);

                List<ItemStack> ingredients = recipe.getIngredients();

                if (!ingredients.isEmpty()) {
                    ingredients.getFirst().setAmount(prices.get(recipes.size()));
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
            trader.setRecipes(recipes);
        }
    }

    /**
     * Stores original prices when a player attacks a wandering trader.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWanderingTraderDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isWanderingTraderPriceProtectionEnabled()) {
            return;
        }

        if (!(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof WanderingTrader trader)) {
            return;
        }

        List<Integer> prices = new ArrayList<>();

        for (MerchantRecipe recipe : trader.getRecipes()) {
            List<ItemStack> ingredients = recipe.getIngredients();

            prices.add(ingredients.isEmpty()
                    ? 1
                    : ingredients.getFirst().getAmount());
        }

        originalPrices.put(trader.getUniqueId(), prices);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!trader.isValid()) {
                return;
            }

            List<MerchantRecipe> recipes = new ArrayList<>();

            for (MerchantRecipe original : trader.getRecipes()) {
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

            trader.setRecipes(recipes);
        });
    }
}