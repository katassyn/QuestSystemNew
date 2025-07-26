package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelListener implements Listener {

    private final QuestSystem plugin;

    public PlayerLevelListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();

        if (newLevel > oldLevel) {
            // Get player quest data
            PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(player.getUniqueId());

            // Check if player has any level up quests before proceeding
            boolean hasLevelUpQuest = false;

            // Check daily quest
            Quest dailyQuest = playerData.getDailyQuest();
            if (dailyQuest != null && !dailyQuest.isCompleted() && 
                dailyQuest.getObjective() == QuestObjective.LEVEL_UP) {
                hasLevelUpQuest = true;
            }

            // Check weekly quest
            if (!hasLevelUpQuest) {
                Quest weeklyQuest = playerData.getWeeklyQuest();
                if (weeklyQuest != null && !weeklyQuest.isCompleted() && 
                    weeklyQuest.getObjective() == QuestObjective.LEVEL_UP) {
                    hasLevelUpQuest = true;
                }
            }

            // Check monthly quest
            if (!hasLevelUpQuest) {
                Quest monthlyQuest = playerData.getMonthlyQuest();
                if (monthlyQuest != null && !monthlyQuest.isCompleted() && 
                    monthlyQuest.getObjective() == QuestObjective.LEVEL_UP) {
                    hasLevelUpQuest = true;
                }
            }

            // If player has a level up quest, update progress
            if (hasLevelUpQuest) {
                // Player leveled up
                int levelsGained = newLevel - oldLevel;
                plugin.getQuestManager().updateProgress(player, QuestObjective.LEVEL_UP, levelsGained, null);
            }
        }
    }

    // This would need to be adapted to work with MyExperiencePlugin
    // You might need to create a custom event or hook into their leveling system
}
