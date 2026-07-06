package org.maks.questsystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;

/**
 * Listener for Fishing plugin integration
 * Tracks: fish catches, quality catches, money earned, treasure maps, bounties
 */
public class FishingListener implements Listener {

    private final QuestSystem plugin;

    public static class FishCatchEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final String quality; // S, A, B, C

        public FishCatchEvent(Player player, String quality) {
            this.player = player;
            this.quality = quality;
        }

        public Player getPlayer() { return player; }
        public String getQuality() { return quality; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class FishSellEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int moneyEarned;

        public FishSellEvent(Player player, int moneyEarned) {
            this.player = player;
            this.moneyEarned = moneyEarned;
        }

        public Player getPlayer() { return player; }
        public int getMoneyEarned() { return moneyEarned; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class TreasureMapFindEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;

        public TreasureMapFindEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class BountyCompleteEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;

        public BountyCompleteEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public FishingListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFishCatch(FishCatchEvent event) {
        Player player = event.getPlayer();
        String quality = event.getQuality();

        // Update general fish catch
        plugin.getQuestManager().updateProgress(player, QuestObjective.FISHING_CATCH_FISH, 1, null);

        // Update quality-specific catches
        if ("S".equals(quality)) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.FISHING_CATCH_QUALITY, 1, "S");
        } else if ("A".equals(quality)) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.FISHING_CATCH_QUALITY, 1, "A");
        }
    }

    @EventHandler
    public void onFishSell(FishSellEvent event) {
        Player player = event.getPlayer();
        int moneyEarned = event.getMoneyEarned();

        // Update money earned from fishing
        plugin.getQuestManager().updateProgress(player, QuestObjective.FISHING_EARN_MONEY, moneyEarned, null);
    }

    @EventHandler
    public void onTreasureMapFind(TreasureMapFindEvent event) {
        Player player = event.getPlayer();

        // Update treasure map finds
        plugin.getQuestManager().updateProgress(player, QuestObjective.FISHING_FIND_MAPS, 1, null);
    }

    @EventHandler
    public void onBountyComplete(BountyCompleteEvent event) {
        Player player = event.getPlayer();

        // Update bounty completions
        plugin.getQuestManager().updateProgress(player, QuestObjective.FISHING_COMPLETE_BOUNTIES, 1, null);
    }
}
