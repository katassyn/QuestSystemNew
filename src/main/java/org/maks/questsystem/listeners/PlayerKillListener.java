package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerKillListener implements Listener {

    private final QuestSystem plugin;

    public PlayerKillListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            // Get player quest data
            PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(killer.getUniqueId());

            // Check if player has any player kill quests before proceeding
            boolean hasPlayerKillQuest = false;

            // Check daily quest
            Quest dailyQuest = playerData.getDailyQuest();
            if (dailyQuest != null && !dailyQuest.isCompleted() && 
                dailyQuest.getObjective() == QuestObjective.KILL_PLAYERS) {
                hasPlayerKillQuest = true;
            }

            // Check weekly quest
            if (!hasPlayerKillQuest) {
                Quest weeklyQuest = playerData.getWeeklyQuest();
                if (weeklyQuest != null && !weeklyQuest.isCompleted() && 
                    weeklyQuest.getObjective() == QuestObjective.KILL_PLAYERS) {
                    hasPlayerKillQuest = true;
                }
            }

            // Check monthly quest
            if (!hasPlayerKillQuest) {
                Quest monthlyQuest = playerData.getMonthlyQuest();
                if (monthlyQuest != null && !monthlyQuest.isCompleted() && 
                    monthlyQuest.getObjective() == QuestObjective.KILL_PLAYERS) {
                    hasPlayerKillQuest = true;
                }
            }

            // If player has a player kill quest, update progress
            if (hasPlayerKillQuest) {
                plugin.getQuestManager().updateProgress(killer, QuestObjective.KILL_PLAYERS, 1, null);
            }
        }
    }
}
