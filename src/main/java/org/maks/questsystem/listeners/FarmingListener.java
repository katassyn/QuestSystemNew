package org.maks.questsystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;

/**
 * Listener for Farming plugin integration
 * Tracks: harvests, material collection, fruit sales, farm upgrades
 */
public class FarmingListener implements Listener {

    private final QuestSystem plugin;

    public static class FarmHarvestEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int amount; // Number of fruits harvested

        public FarmHarvestEvent(Player player, int amount) {
            this.player = player;
            this.amount = amount;
        }

        public Player getPlayer() { return player; }
        public int getAmount() { return amount; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class MaterialCollectEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final String tier; // BASIC, RARE, LEGENDARY

        public MaterialCollectEvent(Player player, String tier) {
            this.player = player;
            this.tier = tier;
        }

        public Player getPlayer() { return player; }
        public String getTier() { return tier; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class FruitSellEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final long moneyEarned; // Total money earned from sale

        public FruitSellEvent(Player player, long moneyEarned) {
            this.player = player;
            this.moneyEarned = moneyEarned;
        }

        public Player getPlayer() { return player; }
        public long getMoneyEarned() { return moneyEarned; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class FarmUpgradeEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;

        public FarmUpgradeEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public FarmingListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFarmHarvest(FarmHarvestEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();

        // Update harvest count with actual amount of fruits harvested
        plugin.getQuestManager().updateProgress(player, QuestObjective.FARMING_HARVEST, amount, null);
    }

    @EventHandler
    public void onMaterialCollect(MaterialCollectEvent event) {
        Player player = event.getPlayer();
        String tier = event.getTier();

        // Update general material collection (each event = 1 material)
        plugin.getQuestManager().updateProgress(player, QuestObjective.FARMING_COLLECT_MATERIALS, 1, null);

        // Update tier-specific material collection
        if ("RARE".equals(tier)) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.FARMING_COLLECT_RARE_MATERIALS, 1, null);
        } else if ("LEGENDARY".equals(tier)) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.FARMING_COLLECT_LEGENDARY_MATERIALS, 1, null);
        }
    }

    @EventHandler
    public void onFruitSell(FruitSellEvent event) {
        Player player = event.getPlayer();
        long moneyEarned = event.getMoneyEarned();

        // Update sell progress with actual money earned (not just 1 per transaction)
        plugin.getQuestManager().updateProgress(player, QuestObjective.FARMING_SELL_FRUITS, (int) moneyEarned, null);
    }

    @EventHandler
    public void onFarmUpgrade(FarmUpgradeEvent event) {
        Player player = event.getPlayer();

        // Update farm upgrade count
        plugin.getQuestManager().updateProgress(player, QuestObjective.FARMING_UPGRADE, 1, null);
    }
}
