package org.maks.questsystem.objects;

import java.util.UUID;

public class Quest {
    
    public enum QuestType {
        DAILY, WEEKLY, MONTHLY
    }
    
    public enum QuestObjective {
        // Kill quests
        KILL_CURRENT_EXPO_NORMAL,
        KILL_CURRENT_EXPO_ELITE,
        KILL_CURRENT_EXPO_MINI_BOSS,
        KILL_NORMAL,
        KILL_ELITE,
        KILL_MINI_BOSS,
        KILL_BOSS,
        KILL_PLAYERS,

        // Drop quests
        DROP_MAGIC,
        DROP_EXTRAORDINARY,
        DROP_LEGENDARY,
        DROP_UNIQUE,
        DROP_MYTHIC,
        DROP_QUEST_CRAFTING_MATERIALS,
        DROP_CRAFTING_MATERIALS,

        // Dungeon quests
        FINISH_Q_INF,
        FINISH_Q_HELL,
        FINISH_Q_BLOOD,
        FINISH_ALL_Q_INF,
        FINISH_ALL_Q_HELL,
        FINISH_ALL_Q_BLOOD,

        // Blood Chest quests
        BLOOD_CHEST_COMPLETE,
        BLOOD_CHEST_OPEN_CHESTS,
        BLOOD_CHEST_SPEED_RUN,

        // Bees quests
        BEES_COLLECT_HONEY,
        BEES_CREATE_BEES,
        BEES_COLLECT_LARVAE,
        BEES_COLLECT_TIER_HONEY,
        BEES_CREATE_TIER_BEES,

        // Group Dungeons quests
        DUNGEONS_COMPLETE_ANY,
        DUNGEONS_COMPLETE_T1,
        DUNGEONS_COMPLETE_T2,
        DUNGEONS_COMPLETE_T3,
        DUNGEONS_COMPLETE_T4,
        DUNGEONS_COMPLETE_T5,
        DUNGEONS_DEFEAT_BOSSES,

        // Fishing quests
        FISHING_CATCH_FISH,
        FISHING_CATCH_QUALITY,
        FISHING_EARN_MONEY,
        FISHING_FIND_MAPS,
        FISHING_COMPLETE_BOUNTIES,

        // Farming quests
        FARMING_HARVEST,
        FARMING_COLLECT_MATERIALS,
        FARMING_SELL_FRUITS,
        FARMING_UPGRADE,
        FARMING_COLLECT_RARE_MATERIALS,
        FARMING_COLLECT_LEGENDARY_MATERIALS,

        // Mining quests
        MINING_MINE_ORES,
        MINING_MINE_SPECIFIC_ORE,
        MINING_COMPLETE_SPHERES,
        MINING_COMPLETE_SPHERE_TYPE,
        MINING_USE_PICKAXE_LEVEL,
        MINING_MINE_TIER_ORES,

        // Other quests
        LEVEL_UP,
        CRAFT_ITEMS,
        OPEN_CHESTS,
        OPEN_LOOTBOXES,
        COMPLETE_DAILY_QUESTS,
        COMPLETE_WEEKLY_QUESTS,
        SPEND_HOURS_ONLINE
    }
    
    private final UUID questId;
    private final QuestType type;
    private final QuestObjective objective;
    private final int levelMin;
    private final int levelMax;
    private final int requiredAmount;
    private int currentProgress;
    private final String description;
    private final String specificTarget; // For specific mobs, dungeons, etc.
    private boolean completed;
    private boolean rewardClaimed;
    
    public Quest(QuestType type, QuestObjective objective, int levelMin, int levelMax, 
                 int requiredAmount, String description, String specificTarget) {
        this.questId = UUID.randomUUID();
        this.type = type;
        this.objective = objective;
        this.levelMin = levelMin;
        this.levelMax = levelMax;
        this.requiredAmount = requiredAmount;
        this.currentProgress = 0;
        this.description = description;
        this.specificTarget = specificTarget;
        this.completed = false;
        this.rewardClaimed = false;
    }
    
    // Constructor for loading from database
    public Quest(UUID questId, QuestType type, QuestObjective objective, int levelMin, 
                 int levelMax, int requiredAmount, int currentProgress, String description, 
                 String specificTarget, boolean completed, boolean rewardClaimed) {
        this.questId = questId;
        this.type = type;
        this.objective = objective;
        this.levelMin = levelMin;
        this.levelMax = levelMax;
        this.requiredAmount = requiredAmount;
        this.currentProgress = currentProgress;
        this.description = description;
        this.specificTarget = specificTarget;
        this.completed = completed;
        this.rewardClaimed = rewardClaimed;
    }
    
    public void addProgress(int amount) {
        if (!completed) {
            currentProgress = Math.min(currentProgress + amount, requiredAmount);
            if (currentProgress >= requiredAmount) {
                completed = true;
            }
        }
    }
    
    public void setProgress(int progress) {
        this.currentProgress = Math.min(progress, requiredAmount);
        if (currentProgress >= requiredAmount) {
            completed = true;
        }
    }
    
    public double getProgressPercentage() {
        return (double) currentProgress / requiredAmount * 100;
    }
    
    public boolean isApplicableForLevel(int playerLevel) {
        return playerLevel >= levelMin && playerLevel <= levelMax;
    }
    
    public void claimReward() {
        if (completed && !rewardClaimed) {
            rewardClaimed = true;
        }
    }
    
    public void reset() {
        currentProgress = 0;
        completed = false;
        rewardClaimed = false;
    }
    
    // Getters
    public UUID getQuestId() { return questId; }
    public QuestType getType() { return type; }
    public QuestObjective getObjective() { return objective; }
    public int getLevelMin() { return levelMin; }
    public int getLevelMax() { return levelMax; }
    public int getRequiredAmount() { return requiredAmount; }
    public int getCurrentProgress() { return currentProgress; }
    public String getDescription() { return description; }
    public String getSpecificTarget() { return specificTarget; }
    public boolean isCompleted() { return completed; }
    public boolean isRewardClaimed() { return rewardClaimed; }
}