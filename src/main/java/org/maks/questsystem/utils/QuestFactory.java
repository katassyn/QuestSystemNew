package org.maks.questsystem.utils;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.Quest.QuestType;

import java.util.*;

public class QuestFactory {
    
    private final QuestSystem plugin;
    private final Random random = new Random();
    
    public QuestFactory(QuestSystem plugin) {
        this.plugin = plugin;
    }
    
    public Quest generateQuest(QuestType type, int playerLevel) {
        List<QuestTemplate> availableQuests = getQuestsForLevel(type, playerLevel);
        if (availableQuests.isEmpty()) {
            return null;
        }
        
        // Select random quest
        QuestTemplate template = availableQuests.get(random.nextInt(availableQuests.size()));
        
        // Create quest from template
        return new Quest(
            type,
            template.objective,
            template.levelMin,
            template.levelMax,
            template.amount,
            template.description,
            template.specificTarget
        );
    }
    
    private List<QuestTemplate> getQuestsForLevel(QuestType type, int playerLevel) {
        List<QuestTemplate> quests = new ArrayList<>();
        
        // Determine level range
        int levelMin, levelMax;
        if (playerLevel < 50) {
            levelMin = 1;
            levelMax = 49;
        } else if (playerLevel < 65) {
            levelMin = 50;
            levelMax = 64;
        } else if (playerLevel <= 80) {
            levelMin = 65;
            levelMax = 80;
        } else {
            levelMin = 80;
            levelMax = 100;
        }
        
        // Generate quests based on type and level
        switch (type) {
            case DAILY:
                quests.addAll(getDailyQuests(levelMin, levelMax));
                break;
            case WEEKLY:
                quests.addAll(getWeeklyQuests(levelMin, levelMax));
                break;
            case MONTHLY:
                quests.addAll(getMonthlyQuests(levelMin, levelMax));
                break;
        }
        
        return quests;
    }
    
    private List<QuestTemplate> getDailyQuests(int levelMin, int levelMax) {
        List<QuestTemplate> quests = new ArrayList<>();
        
        if (levelMin == 1 && levelMax == 49) {
            // 1-49 level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 100, "Kill 100 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 10, "Kill 10 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 1, "Kill 1 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.DROP_MAGIC, 5, "Drop 5 magic items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_EXTRAORDINARY, 2, "Drop 2 extraordinary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 1, "Drop 1 legendary item"));
            quests.add(new QuestTemplate(QuestObjective.LEVEL_UP, 1, "Level up"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 500, "Kill 500 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 20, "Kill 20 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 3, "Drop 3 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 3, "Craft 3 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 1, "Open 1 chest"));
        } else if (levelMin == 50 && levelMax == 64) {
            // 50-64 level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 1000, "Kill 1000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 2500, "Kill 2500 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 25, "Kill 25 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 50, "Kill 50 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 1, "Kill 1 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 5, "Kill 5 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 2, "Kill 2 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 1, "Drop 1 legendary item"));
            
            // Add Q dungeon quests
            for (int i = 1; i <= 10; i++) {
                quests.add(new QuestTemplate(QuestObjective.FINISH_Q_INF, 1, 
                    "Finish Q" + i + " Inf", "q" + i));
            }
            
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 3, "Drop 3 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 10, "Kill 10 players"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 20, "Kill 20 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 5, "Drop 5 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 50, "Drop 50 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 5, "Craft 5 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 32, "Open 32 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 2, "Open 2 chests"));
        } else if (levelMin == 65 && levelMax == 80) {
            // 65-80 level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 5000, "Kill 5000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 10000, "Kill 10000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 100, "Kill 100 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 250, "Kill 250 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 2, "Kill 2 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 10, "Kill 10 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 5, "Kill 5 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 3, "Drop 3 legendary items"));
            
            // Add Q dungeon quests
            for (int i = 1; i <= 10; i++) {
                quests.add(new QuestTemplate(QuestObjective.FINISH_Q_HELL, 1, 
                    "Finish Q" + i + " Hell", "q" + i));
            }
            
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 5, "Drop 5 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_UNIQUE, 1, "Drop 1 unique item"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 50, "Kill 50 players"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 100, "Kill 100 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 10, "Drop 10 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 100, "Drop 100 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 10, "Craft 10 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 64, "Open 64 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 3, "Open 3 chests"));
        } else {
            // 80+ level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 20, "Kill 20 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 10, "Kill 10 boss mobs"));
            
            // Add Q dungeon quests
            for (int i = 1; i <= 10; i++) {
                quests.add(new QuestTemplate(QuestObjective.FINISH_Q_BLOOD, 1, 
                    "Finish Q" + i + " Blood", "q" + i));
            }
            
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 5, "Drop 5 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_UNIQUE, 2, "Drop 2 unique items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 3, "Drop 3 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 100, "Kill 100 players"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 200, "Kill 200 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 20, "Drop 20 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 250, "Drop 250 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 25, "Craft 25 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 128, "Open 128 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 5, "Open 5 chests"));
        }
        
        return quests;
    }
    
    private List<QuestTemplate> getWeeklyQuests(int levelMin, int levelMax) {
        List<QuestTemplate> quests = new ArrayList<>();
        
        if (levelMin == 1 && levelMax == 49) {
            // 1-49 level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 500, "Kill 500 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 50, "Kill 50 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 5, "Kill 5 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.DROP_MAGIC, 15, "Drop 15 magic items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_EXTRAORDINARY, 8, "Drop 8 extraordinary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 3, "Drop 3 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.LEVEL_UP, 3, "Level up 3 times"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 2000, "Kill 2000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 100, "Kill 100 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 10, "Drop 10 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 15, "Craft 15 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 5, "Open 5 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 3, "Complete 3 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.SPEND_HOURS_ONLINE, 6, "Spend 6 hours online"));
        } else if (levelMin == 50 && levelMax == 64) {
            // 50-64 level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 5000, "Kill 5000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 10000, "Kill 10000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 100, "Kill 100 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 250, "Kill 250 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 5, "Kill 5 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 25, "Kill 25 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 8, "Kill 8 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 5, "Drop 5 legendary items"));
            
            // Random 3 Q dungeons
            quests.add(new QuestTemplate(QuestObjective.FINISH_Q_INF, 3, "Finish 3 random Q Inf dungeons", "random"));
            
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 10, "Drop 10 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 50, "Kill 50 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 15, "Drop 15 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 200, "Drop 200 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 25, "Craft 25 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 150, "Open 150 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 8, "Open 8 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 5, "Complete 5 daily quests"));
        } else if (levelMin == 65 && levelMax == 80) {
            // 65-80 level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 20000, "Kill 20000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 50000, "Kill 50000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 400, "Kill 400 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 1000, "Kill 1000 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 10, "Kill 10 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 50, "Kill 50 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 20, "Kill 20 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 8, "Drop 8 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_HELL, 1, "Finish all Q1-Q10 Hell"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 15, "Drop 15 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_UNIQUE, 3, "Drop 3 unique items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 200, "Kill 200 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 35, "Drop 35 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 500, "Drop 500 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 40, "Craft 40 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 300, "Open 300 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 12, "Open 12 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 6, "Complete 6 daily quests"));
        } else {
            // 80+ level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 100, "Kill 100 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 40, "Kill 40 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_BLOOD, 1, "Finish all Q1-Q10 Blood"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 15, "Drop 15 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_UNIQUE, 5, "Drop 5 unique items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 10, "Drop 10 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 400, "Kill 400 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 60, "Drop 60 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 1000, "Drop 1000 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 80, "Craft 80 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 500, "Open 500 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 20, "Open 20 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 7, "Complete 7 daily quests"));
        }
        
        return quests;
    }
    
    private List<QuestTemplate> getMonthlyQuests(int levelMin, int levelMax) {
        List<QuestTemplate> quests = new ArrayList<>();
        
        if (levelMin == 1 && levelMax == 49) {
            // 1-49 level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 2000, "Kill 2000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 200, "Kill 200 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 20, "Kill 20 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.DROP_MAGIC, 50, "Drop 50 magic items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_EXTRAORDINARY, 25, "Drop 25 extraordinary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 10, "Drop 10 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.LEVEL_UP, 10, "Level up 10 times"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 8000, "Kill 8000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 400, "Kill 400 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 30, "Drop 30 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 50, "Craft 50 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 20, "Open 20 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 15, "Complete 15 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.SPEND_HOURS_ONLINE, 40, "Spend 40 hours online"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 1, "Complete weekly quest 1 time"));
        } else if (levelMin == 50 && levelMax == 64) {
            // 50-64 level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 25000, "Kill 25000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 60000, "Kill 60000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 500, "Kill 500 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 1500, "Kill 1500 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 25, "Kill 25 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 120, "Kill 120 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 40, "Kill 40 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 20, "Drop 20 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_INF, 3, "Finish all Q1-Q10 Inf (complete set 3 times)"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 35, "Drop 35 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 300, "Kill 300 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 60, "Drop 60 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 1000, "Drop 1000 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 100, "Craft 100 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 800, "Open 800 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 35, "Open 35 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 20, "Complete 20 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 2, "Complete weekly quest 2 times"));
        } else if (levelMin == 65 && levelMax == 80) {
            // 65-80 level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 100000, "Kill 100000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 250000, "Kill 250000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 2000, "Kill 2000 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 5000, "Kill 5000 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 50, "Kill 50 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 250, "Kill 250 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 100, "Kill 100 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 30, "Drop 30 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_HELL, 5, "Finish all Q1-Q10 Hell (complete set 5 times)"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 50, "Drop 50 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_UNIQUE, 15, "Drop 15 unique items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 1000, "Kill 1000 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 150, "Drop 150 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 2500, "Drop 2500 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 200, "Craft 200 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 1500, "Open 1500 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 50, "Open 50 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 25, "Complete 25 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 3, "Complete weekly quest 3 times"));
        } else {
            // 80+ level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 500, "Kill 500 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 200, "Kill 200 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_BLOOD, 8, "Finish all Q1-Q10 Blood (complete set 8 times)"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 50, "Drop 50 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_UNIQUE, 20, "Drop 20 unique items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 40, "Drop 40 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 2000, "Kill 2000 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 300, "Drop 300 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 5000, "Drop 5000 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 400, "Craft 400 items"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_LOOTBOXES, 3000, "Open 3000 lootboxes"));
            quests.add(new QuestTemplate(QuestObjective.OPEN_CHESTS, 100, "Open 100 chests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 20, "Complete all daily quests for 20 days"));
            quests.add(new QuestTemplate(QuestObjective.DROP_MYTHIC, 1, "Drop 1 mythic item"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 4, "Complete weekly quest 4 times"));
        }
        
        return quests;
    }
    
    private static class QuestTemplate {
        final QuestObjective objective;
        final int amount;
        final String description;
        final String specificTarget;
        final int levelMin;
        final int levelMax;
        
        QuestTemplate(QuestObjective objective, int amount, String description) {
            this(objective, amount, description, null);
        }
        
        QuestTemplate(QuestObjective objective, int amount, String description, String specificTarget) {
            this.objective = objective;
            this.amount = amount;
            this.description = description;
            this.specificTarget = specificTarget;
            this.levelMin = 0;
            this.levelMax = 100;
        }
    }
}