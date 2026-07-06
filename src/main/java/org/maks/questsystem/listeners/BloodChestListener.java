package org.maks.questsystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;

/**
 * Listener for Blood Chest plugin integration
 * Tracks: arena completions, chest openings, speed runs
 */
public class BloodChestListener implements Listener {

    private final QuestSystem plugin;

    public static class BloodChestCompleteEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int completionTime; // in seconds

        public BloodChestCompleteEvent(Player player, int completionTime) {
            this.player = player;
            this.completionTime = completionTime;
        }

        public Player getPlayer() { return player; }
        public int getCompletionTime() { return completionTime; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class BloodChestOpenEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;

        public BloodChestOpenEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() { return player; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public BloodChestListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBloodChestComplete(BloodChestCompleteEvent event) {
        Player player = event.getPlayer();
        int completionTime = event.getCompletionTime();

        // Update quest progress for arena completion
        plugin.getQuestManager().updateProgress(player, QuestObjective.BLOOD_CHEST_COMPLETE, 1, null);

        // Update speed run quests based on completion time
        if (completionTime <= 30) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.BLOOD_CHEST_SPEED_RUN, 1, "30");
        }
        if (completionTime <= 60) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.BLOOD_CHEST_SPEED_RUN, 1, "60");
        }
    }

    @EventHandler
    public void onBloodChestOpen(BloodChestOpenEvent event) {
        Player player = event.getPlayer();

        // Update quest progress for opening chests (each event = 1 chest opened)
        plugin.getQuestManager().updateProgress(player, QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 1, null);
    }
}
