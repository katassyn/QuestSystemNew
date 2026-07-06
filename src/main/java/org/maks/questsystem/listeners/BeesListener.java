package org.maks.questsystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;

/**
 * Listener for Bees plugin integration
 * Tracks: honey collection, bee creation, larvae collection, tier-specific actions
 */
public class BeesListener implements Listener {

    private final QuestSystem plugin;

    public static class HoneyCollectEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int amount;
        private final Object tier; // Tier enum from beesPlugin (or null for general)

        public HoneyCollectEvent(Player player, int amount, Object tier) {
            this.player = player;
            this.amount = amount;
            this.tier = tier;
        }

        public Player getPlayer() { return player; }
        public int getAmount() { return amount; }
        public Object getTier() { return tier; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class BeeCreateEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int amount;
        private final Object tier; // Tier enum from beesPlugin (or null for general)

        public BeeCreateEvent(Player player, int amount, Object tier) {
            this.player = player;
            this.amount = amount;
            this.tier = tier;
        }

        public Player getPlayer() { return player; }
        public int getAmount() { return amount; }
        public Object getTier() { return tier; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class LarvaeCollectEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int amount;

        public LarvaeCollectEvent(Player player, int amount) {
            this.player = player;
            this.amount = amount;
        }

        public Player getPlayer() { return player; }
        public int getAmount() { return amount; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public BeesListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHoneyCollect(HoneyCollectEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();
        Object tierObj = event.getTier();

        // Update general honey collection
        plugin.getQuestManager().updateProgress(player, QuestObjective.BEES_COLLECT_HONEY, amount, null);

        // Update tier-specific honey collection (Tier.III)
        if (tierObj != null && tierObj.toString().equals("III")) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.BEES_COLLECT_TIER_HONEY, amount, "3");
        }
    }

    @EventHandler
    public void onBeeCreate(BeeCreateEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();
        Object tierObj = event.getTier();

        // Update general bee creation
        plugin.getQuestManager().updateProgress(player, QuestObjective.BEES_CREATE_BEES, amount, null);

        // Update tier-specific bee creation (Tier.III)
        if (tierObj != null && tierObj.toString().equals("III")) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.BEES_CREATE_TIER_BEES, amount, "3");
        }
    }

    @EventHandler
    public void onLarvaeCollect(LarvaeCollectEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();

        // Update larvae collection
        plugin.getQuestManager().updateProgress(player, QuestObjective.BEES_COLLECT_LARVAE, amount, null);
    }
}
