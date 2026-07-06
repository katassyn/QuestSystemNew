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

            // Dungeon quests (40+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T1, 2, "Beginner's Challenge - Complete 2 T1 dungeons (Odyssey)"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 3, "Daily Dungeon Dive - Complete 3 dungeons of any tier"));
        } else if (levelMin == 50 && levelMax == 64) {
            // 50-64 level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 1000, "Kill 1000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 2500, "Kill 2500 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 25, "Kill 25 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 50, "Kill 50 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 1, "Kill 1 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 5, "Kill 5 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 1, "Kill 1 boss mob"));
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

            // Dungeon quests (50+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T1, 2, "Beginner's Challenge - Complete 2 T1 dungeons (Odyssey)"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T2, 2, "Seaside Adventure - Complete 2 T2 dungeons (Poseidon)"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 3, "Daily Dungeon Dive - Complete 3 dungeons of any tier"));
        } else if (levelMin == 65 && levelMax == 80) {
            // 65-80 level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 5000, "Kill 5000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 10000, "Kill 10000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 100, "Kill 100 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 250, "Kill 250 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 2, "Kill 2 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 10, "Kill 10 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 2, "Kill 2 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 3, "Drop 3 legendary items"));

            // Add Q dungeon quests
            for (int i = 1; i <= 10; i++) {
                quests.add(new QuestTemplate(QuestObjective.FINISH_Q_HELL, 1,
                    "Finish Q" + i + " Hell", "q" + i));
            }

            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 5, "Drop 5 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 50, "Kill 50 players"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 100, "Kill 100 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 10, "Drop 10 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 100, "Drop 100 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 10, "Craft 10 items"));

            // Blood Chest quests (55+)
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_COMPLETE, 5, "Daily Routine - Complete 5 Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 25, "Collector's Day - Open 25 chests in Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 2, "Quick Execution - Complete 2 Blood Chest arenas in <=60 seconds each", "60"));

            // Bees quests (75+)
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_HONEY, 50, "Daily Harvest - Collect 50 honey bottles from your hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_CREATE_BEES, 10, "Daily Breeder - Create 10 bees and place them in hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_LARVAE, 40, "Larva Collector - Collect 40 larvae from your hives"));

            // Dungeon quests (65+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T1, 2, "Beginner's Challenge - Complete 2 T1 (Odyssey) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T2, 2, "Seaside Adventure - Complete 2 T2 (Poseidon) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 3, "Daily Dungeon Dive - Complete 3 group dungeons of any tier"));

            // Mining quests (70+)
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_ORES, 200, "Daily Miner - Mine 200 ores in mining spheres"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERES, 3, "Sphere Explorer - Complete 3 mining spheres of any type"));
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_TIER_ORES, 20, "Rare Collector - Mine 20 Tier II or Tier III ores in spheres", "2|3"));
        } else {
            // 80+ level daily quests
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 20, "Kill 20 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 5, "Kill 5 boss mobs"));

            // Add Q dungeon quests
            for (int i = 1; i <= 10; i++) {
                quests.add(new QuestTemplate(QuestObjective.FINISH_Q_BLOOD, 1,
                    "Finish Q" + i + " Blood", "q" + i));
            }

            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 5, "Drop 5 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 3, "Drop 3 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 100, "Kill 100 players"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 200, "Kill 200 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 20, "Drop 20 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 250, "Drop 250 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 25, "Craft 25 items"));

            // Blood Chest quests (55+)
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_COMPLETE, 5, "Daily Routine - Complete 5 Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 25, "Collector's Day - Open 25 chests in Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 2, "Quick Execution - Complete 2 Blood Chest arenas in <=60 seconds each", "60"));

            // Bees quests (75+)
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_HONEY, 50, "Daily Harvest - Collect 50 honey bottles from your hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_CREATE_BEES, 10, "Daily Breeder - Create 10 bees and place them in hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_LARVAE, 40, "Larva Collector - Collect 40 larvae from your hives"));

            // Dungeon quests (40+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T1, 2, "Beginner's Challenge - Complete 2 T1 (Odyssey) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T2, 2, "Seaside Adventure - Complete 2 T2 (Poseidon) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 3, "Daily Dungeon Dive - Complete 3 group dungeons of any tier"));

            // Fishing quests (80+)
            quests.add(new QuestTemplate(QuestObjective.FISHING_CATCH_FISH, 125, "Daily Catch - Catch 125 fish of any quality using fishing rod"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_EARN_MONEY, 75000, "Sea Merchant - Earn $75,000 by selling fish to quick sell"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_FIND_MAPS, 1, "Treasure Hunter - Find 1 treasure map while fishing"));

            // Farming quests (80+)
            quests.add(new QuestTemplate(QuestObjective.FARMING_HARVEST, 15, "Daily Farmer - Harvest crops 15 times from your farms"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_COLLECT_MATERIALS, 25, "Material Hunter - Collect 25 farming materials of any tier"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_SELL_FRUITS, 100000, "Fruit Merchant - Sell farmed fruits for $100,000 total"));

            // Mining quests (70+)
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_ORES, 200, "Daily Miner - Mine 200 ores in mining spheres"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERES, 3, "Sphere Explorer - Complete 3 mining spheres of any type"));
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_TIER_ORES, 20, "Rare Collector - Mine 20 Tier II or Tier III ores in spheres", "2|3"));
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
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 3, "Complete 3 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.SPEND_HOURS_ONLINE, 6, "Spend 6 hours online"));

            // Dungeon weekly quests
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 5, "Dungeon Explorer - Complete 5 dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 6, "Boss Slayer - Defeat 6 final bosses in dungeons"));
        } else if (levelMin == 50 && levelMax == 64) {
            // 50-64 level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 5000, "Kill 5000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 10000, "Kill 10000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 100, "Kill 100 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 250, "Kill 250 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 5, "Kill 5 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 25, "Kill 25 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 4, "Kill 4 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 5, "Drop 5 legendary items"));
            
            // Random 3 Q dungeons
            quests.add(new QuestTemplate(QuestObjective.FINISH_Q_INF, 3, "Finish 3 random Q Inf dungeons", "random"));
            
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 10, "Drop 10 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 50, "Kill 50 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 15, "Drop 15 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 200, "Drop 200 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 25, "Craft 25 items"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 5, "Complete 5 daily quests"));

            // Dungeon weekly quests (50+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 5, "Dungeon Explorer - Complete 5 dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 6, "Boss Slayer - Defeat 6 final bosses in dungeons"));
        } else if (levelMin == 65 && levelMax == 80) {
            // 65-80 level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 20000, "Kill 20000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 50000, "Kill 50000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 400, "Kill 400 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 1000, "Kill 1000 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 10, "Kill 10 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 50, "Kill 50 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 10, "Kill 10 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 8, "Drop 8 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_HELL, 1, "Finish all Q1-Q10 Hell"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 15, "Drop 15 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 200, "Kill 200 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 35, "Drop 35 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 500, "Drop 500 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 40, "Craft 40 items"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 6, "Complete 6 daily quests"));

            // Blood Chest weekly quests (55+)
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_COMPLETE, 7, "Warrior of the Week - Complete 7 Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 37, "Chest Hunter - Open 37 chests in Blood Chest arenas total"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 3, "Speed Demon - Complete 3 Blood Chest arenas in <=30 seconds each", "30"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 5, "Time Master - Complete 5 Blood Chest arenas in <=60 seconds each", "60"));

            // Bees weekly quests (75+)
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_HONEY, 50, "Honey Week - Collect 50 honey bottles from your hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_CREATE_TIER_BEES, 3, "Elite Breeder - Create 3 Tier III bees and place them in hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_TIER_HONEY, 7, "Production Master - Collect 7 Tier III honey bottles from hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_LARVAE, 37, "Larva Collector - Collect 37 larvae from your hives"));

            // Dungeon weekly quests (65+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 5, "Dungeon Explorer - Complete 5 group dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T3, 2, "Olympus Conqueror - Complete 2 T3 (Zeus) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 6, "Boss Slayer - Defeat 6 final bosses in group dungeons"));

            // Mining weekly quests (70+)
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_ORES, 300, "Weekly Miner - Mine 300 ores in mining spheres"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERES, 5, "Sphere Master - Complete 5 mining spheres of any type"));
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_TIER_ORES, 30, "Legendary Hunter - Mine 30 Tier III ores in spheres", "3"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERE_TYPE, 2, "Treasure Seeker - Complete 2 TREASURE type mining spheres", "TREASURE"));
        } else {
            // 80+ level weekly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 100, "Kill 100 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 20, "Kill 20 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_BLOOD, 1, "Finish all Q1-Q10 Blood"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 15, "Drop 15 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 10, "Drop 10 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 400, "Kill 400 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 60, "Drop 60 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 1000, "Drop 1000 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 80, "Craft 80 items"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 7, "Complete 7 daily quests"));

            // Blood Chest weekly quests (55+)
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_COMPLETE, 7, "Warrior of the Week - Complete 7 Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 37, "Chest Hunter - Open 37 chests in Blood Chest arenas total"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 3, "Speed Demon - Complete 3 Blood Chest arenas in <=30 seconds each", "30"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 5, "Time Master - Complete 5 Blood Chest arenas in <=60 seconds each", "60"));

            // Bees weekly quests (75+)
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_HONEY, 50, "Honey Week - Collect 50 honey bottles from your hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_CREATE_TIER_BEES, 3, "Elite Breeder - Create 3 Tier III bees and place them in hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_TIER_HONEY, 7, "Production Master - Collect 7 Tier III honey bottles from hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_LARVAE, 37, "Larva Collector - Collect 37 larvae from your hives"));

            // Dungeon weekly quests (80+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 5, "Dungeon Explorer - Complete 5 group dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T4, 2, "Labyrinth Master - Complete 2 T4 (Daedalus) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T5, 1, "Underworld Warrior - Complete 1 T5 (Hades) group dungeon"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 6, "Boss Slayer - Defeat 6 final bosses in group dungeons"));

            // Fishing weekly quests (80+)
            quests.add(new QuestTemplate(QuestObjective.FISHING_CATCH_FISH, 500, "Weekly Master - Catch 500 fish of any quality"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_FIND_MAPS, 5, "Treasure Hunter - Find 5 treasure maps while fishing"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_EARN_MONEY, 500000, "Wealth from the Sea - Earn $500,000 by selling fish"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_COMPLETE_BOUNTIES, 5, "Bounty Hunter - Complete 5 treasure map bounty hunts"));

            // Farming weekly quests (80+)
            quests.add(new QuestTemplate(QuestObjective.FARMING_HARVEST, 30, "Weekly Farmer - Harvest crops 30 times from your farms"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_COLLECT_MATERIALS, 60, "Material Tycoon - Collect 60 farming materials of any tier"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_UPGRADE, 1, "Upgrade Master - Upgrade farm 1 time (Storage/Speed/Quality)"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_SELL_FRUITS, 300000, "Fruit Empire - Sell farmed fruits for $300,000 total"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_COLLECT_RARE_MATERIALS, 3, "Rare Materials - Collect 3 RARE tier farming materials"));

            // Mining weekly quests (70+)
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_ORES, 300, "Weekly Miner - Mine 300 ores in mining spheres"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERES, 5, "Sphere Master - Complete 5 mining spheres of any type"));
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_TIER_ORES, 30, "Legendary Hunter - Mine 30 Tier III ores in spheres", "3"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERE_TYPE, 2, "Treasure Seeker - Complete 2 TREASURE type mining spheres", "TREASURE"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERE_TYPE, 1, "Boss Hunter - Complete 1 BOSS type mining sphere", "BOSS"));
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
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 15, "Complete 15 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.SPEND_HOURS_ONLINE, 40, "Spend 40 hours online"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 1, "Complete weekly quest 1 time"));

            // Dungeon monthly quests
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 25, "Dungeon Champion - Complete 25 dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 37, "Boss Destroyer - Defeat 37 final bosses"));
        } else if (levelMin == 50 && levelMax == 64) {
            // 50-64 level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 25000, "Kill 25000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 60000, "Kill 60000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 500, "Kill 500 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 1500, "Kill 1500 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 25, "Kill 25 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 120, "Kill 120 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 20, "Kill 20 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 20, "Drop 20 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_INF, 3, "Finish all Q1-Q10 Inf (complete set 3 times)"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 35, "Drop 35 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 300, "Kill 300 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 60, "Drop 60 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 1000, "Drop 1000 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 100, "Craft 100 items"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 20, "Complete 20 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 2, "Complete weekly quest 2 times"));

            // Dungeon monthly quests (50+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 25, "Dungeon Champion - Complete 25 dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 37, "Boss Destroyer - Defeat 37 final bosses"));
        } else if (levelMin == 65 && levelMax == 80) {
            // 65-80 level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_NORMAL, 100000, "Kill 100000 current expo normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_NORMAL, 250000, "Kill 250000 normal mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_ELITE, 2000, "Kill 2000 current expo elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_ELITE, 5000, "Kill 5000 elite mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_CURRENT_EXPO_MINI_BOSS, 50, "Kill 50 current expo mini boss"));
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 250, "Kill 250 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 50, "Kill 50 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 30, "Drop 30 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_HELL, 5, "Finish all Q1-Q10 Hell (complete set 5 times)"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 50, "Drop 50 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 1000, "Kill 1000 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 150, "Drop 150 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 2500, "Drop 2500 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 200, "Craft 200 items"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 25, "Complete 25 daily quests"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 3, "Complete weekly quest 3 times"));

            // Blood Chest monthly quests (55+)
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_COMPLETE, 20, "Arena Legend - Complete 20 Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 10, "Time Master Pro - Complete 10 Blood Chest arenas in <=60 seconds each", "60"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 100, "Chest Emperor - Open 100 chests in Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 5, "Speed God - Complete 5 Blood Chest arenas in <=30 seconds each", "30"));

            // Bees monthly quests (75+)
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_HONEY, 100, "Bee Emperor - Collect 100 honey bottles from hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_CREATE_TIER_BEES, 10, "Master Breeder - Create 10 Tier III bees and place in hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_TIER_HONEY, 15, "Elite Production King - Collect 15 Tier III honey from hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_LARVAE, 50, "Legendary Larvae - Collect 50 larvae from hives"));

            // Dungeon monthly quests (65+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 15, "Dungeon Champion - Complete 15 group dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T3, 6, "Mythology Master - Complete 6 T3+ group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 20, "Boss Destroyer - Defeat 20 final bosses in group dungeons"));

            // Mining monthly quests (70+)
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_ORES, 1500, "Mining Legend - Mine 1,500 ores in mining spheres"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERES, 15, "Sphere Emperor - Complete 15 mining spheres of any type"));
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_TIER_ORES, 150, "Legendary Collector - Mine 150 Tier III ores in spheres", "3"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERE_TYPE, 5, "Boss Slayer - Complete 5 BOSS type mining spheres", "BOSS"));
        } else {
            // 80+ level monthly quests
            quests.add(new QuestTemplate(QuestObjective.KILL_MINI_BOSS, 500, "Kill 500 mini boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.KILL_BOSS, 100, "Kill 100 boss mobs"));
            quests.add(new QuestTemplate(QuestObjective.FINISH_ALL_Q_BLOOD, 8, "Finish all Q1-Q10 Blood (complete set 8 times)"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 50, "Drop 50 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.DROP_LEGENDARY, 40, "Drop 40 legendary items"));
            quests.add(new QuestTemplate(QuestObjective.KILL_PLAYERS, 2000, "Kill 2000 players"));
            quests.add(new QuestTemplate(QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, 300, "Drop 300 quest crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.DROP_CRAFTING_MATERIALS, 5000, "Drop 5000 crafting materials"));
            quests.add(new QuestTemplate(QuestObjective.CRAFT_ITEMS, 400, "Craft 400 items"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_DAILY_QUESTS, 20, "Complete all daily quests for 20 days"));
            quests.add(new QuestTemplate(QuestObjective.COMPLETE_WEEKLY_QUESTS, 4, "Complete weekly quest 4 times"));

            // Blood Chest monthly quests (55+)
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_COMPLETE, 20, "Arena Legend - Complete 20 Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 10, "Time Master Pro - Complete 10 Blood Chest arenas in <=60 seconds each", "60"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_OPEN_CHESTS, 100, "Chest Emperor - Open 100 chests in Blood Chest arenas"));
            quests.add(new QuestTemplate(QuestObjective.BLOOD_CHEST_SPEED_RUN, 5, "Speed God - Complete 5 Blood Chest arenas in <=30 seconds each", "30"));

            // Bees monthly quests (75+)
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_HONEY, 100, "Bee Emperor - Collect 100 honey bottles from hives"));
            quests.add(new QuestTemplate(QuestObjective.BEES_CREATE_TIER_BEES, 10, "Master Breeder - Create 10 Tier III bees and place in hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_TIER_HONEY, 15, "Elite Production King - Collect 15 Tier III honey from hives", "3"));
            quests.add(new QuestTemplate(QuestObjective.BEES_COLLECT_LARVAE, 50, "Legendary Larvae - Collect 50 larvae from hives"));

            // Dungeon monthly quests (80+)
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_ANY, 15, "Dungeon Champion - Complete 15 group dungeons of any tier"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T3, 6, "Mythology Master - Complete 6 T3+ group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_COMPLETE_T5, 3, "Underworld Legend - Complete 3 T5 (Hades) group dungeons"));
            quests.add(new QuestTemplate(QuestObjective.DUNGEONS_DEFEAT_BOSSES, 20, "Boss Destroyer - Defeat 20 final bosses in group dungeons"));

            // Fishing monthly quests (80+)
            quests.add(new QuestTemplate(QuestObjective.FISHING_CATCH_FISH, 2000, "Legend of the Oceans - Catch 2,000 fish of any quality"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_COMPLETE_BOUNTIES, 20, "Bounty Master - Complete 20 treasure map bounty hunts"));
            quests.add(new QuestTemplate(QuestObjective.FISHING_EARN_MONEY, 2000000, "Fishing Tycoon - Earn $2,000,000 by selling fish total"));

            // Farming monthly quests (80+)
            quests.add(new QuestTemplate(QuestObjective.FARMING_HARVEST, 100, "Farming Legend - Harvest crops 100 times from your farms"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_COLLECT_MATERIALS, 200, "Material Emperor - Collect 200 farming materials of any tier"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_COLLECT_LEGENDARY_MATERIALS, 5, "Legendary Collector - Collect 5 LEGENDARY tier materials"));
            quests.add(new QuestTemplate(QuestObjective.FARMING_SELL_FRUITS, 1000000, "Farming Tycoon - Sell farmed fruits for $1,000,000 total"));

            // Mining monthly quests (70+)
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_ORES, 1500, "Mining Legend - Mine 1,500 ores in mining spheres"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERES, 15, "Sphere Emperor - Complete 15 mining spheres of any type"));
            quests.add(new QuestTemplate(QuestObjective.MINING_MINE_TIER_ORES, 150, "Legendary Collector - Mine 150 Tier III ores in spheres", "3"));
            quests.add(new QuestTemplate(QuestObjective.MINING_COMPLETE_SPHERE_TYPE, 5, "Boss Slayer - Complete 5 BOSS type mining spheres", "BOSS"));
            quests.add(new QuestTemplate(QuestObjective.MINING_USE_PICKAXE_LEVEL, 500, "Diamond Miner - Mine 500 ores with Diamond or Netherite pickaxe", "5|6"));
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