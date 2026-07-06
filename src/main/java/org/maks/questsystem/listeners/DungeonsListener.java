package org.maks.questsystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest.QuestObjective;

/**
 * Listener for Group Dungeon plugin integration
 * Tracks: dungeon completions by tier, boss defeats
 */
public class DungeonsListener implements Listener {

    private final QuestSystem plugin;

    public static class DungeonCompleteEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final int tier; // 1-5

        public DungeonCompleteEvent(Player player, int tier) {
            this.player = player;
            this.tier = tier;
        }

        public Player getPlayer() { return player; }
        public int getTier() { return tier; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public static class DungeonBossDefeatEvent extends org.bukkit.event.Event {
        private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();
        private final Player player;
        private final String bossType;

        public DungeonBossDefeatEvent(Player player, String bossType) {
            this.player = player;
            this.bossType = bossType;
        }

        public Player getPlayer() { return player; }
        public String getBossType() { return bossType; }

        @Override
        public org.bukkit.event.HandlerList getHandlers() { return HANDLERS; }
        public static org.bukkit.event.HandlerList getHandlerList() { return HANDLERS; }
    }

    public DungeonsListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDungeonComplete(DungeonCompleteEvent event) {
        Player player = event.getPlayer();
        int tier = event.getTier();

        // Update general dungeon completion
        plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_COMPLETE_ANY, 1, null);

        // Update tier-specific dungeon completion
        switch (tier) {
            case 1:
                plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_COMPLETE_T1, 1, null);
                break;
            case 2:
                plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_COMPLETE_T2, 1, null);
                break;
            case 3:
                plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_COMPLETE_T3, 1, null);
                break;
            case 4:
                plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_COMPLETE_T4, 1, null);
                break;
            case 5:
                plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_COMPLETE_T5, 1, null);
                break;
        }
    }

    @EventHandler
    public void onDungeonBossDefeat(DungeonBossDefeatEvent event) {
        Player player = event.getPlayer();
        String bossType = event.getBossType();

        // Update boss defeat count (bossType contains tier info like "T1_BOSS", "T2_BOSS", etc.)
        plugin.getQuestManager().updateProgress(player, QuestObjective.DUNGEONS_DEFEAT_BOSSES, 1, null);
    }
}
