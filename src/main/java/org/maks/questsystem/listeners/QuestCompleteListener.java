package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.events.QuestCompleteEvent;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestCompleteListener implements Listener {
    
    private final QuestSystem plugin;
    
    public QuestCompleteListener(QuestSystem plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onQuestComplete(QuestCompleteEvent event) {
        Player player = event.getPlayer();
        Quest quest = event.getQuest();
        
        // Check if this completion affects other quests
        switch (quest.getType()) {
            case DAILY:
                // Completing a daily quest counts towards monthly quest progress
                plugin.getQuestManager().updateProgress(player, 
                    QuestObjective.COMPLETE_DAILY_QUESTS, 1, null);
                break;
            case WEEKLY:
                // Completing a weekly quest counts towards monthly quest progress
                plugin.getQuestManager().updateProgress(player, 
                    QuestObjective.COMPLETE_WEEKLY_QUESTS, 1, null);
                break;
        }
    }
}