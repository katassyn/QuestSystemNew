package org.maks.questsystem;

import org.maks.questsystem.commands.*;
import org.maks.questsystem.database.DatabaseManager;
import org.maks.questsystem.database.QuestDatabase;
import org.maks.questsystem.listeners.*;
import org.maks.questsystem.managers.*;
import org.maks.questsystem.api.CraftingAPI;
import org.maks.questsystem.api.LockpickChestAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public class QuestSystem extends JavaPlugin {

    private static QuestSystem instance;
    private DatabaseManager databaseManager;
    private QuestDatabase questDatabase;
    private QuestManager questManager;
    private RewardManager rewardManager;
    private ConfigManager configManager;
    private CraftingAPI craftingAPI;
    private LockpickChestAPI lockpickChestAPI;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize config manager
        configManager = new ConfigManager(this);

        // Setup database
        if (!setupDatabase()) {
            getLogger().severe("Failed to setup database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // LuckPerms is integrated with Bukkit's permission system
        getLogger().info("Using Bukkit permission system for permission checks.");

        // Initialize APIs with a delay to ensure other plugins are loaded
        getLogger().info("Scheduling delayed API initialization to ensure other plugins are loaded...");
        Bukkit.getScheduler().runTaskLater(this, () -> {
            setupAPIs();

            // Initialize managers
            questManager = new QuestManager(this);
            rewardManager = new RewardManager(this);

            // Register listeners
            registerListeners();

            // Schedule quest resets after questManager is initialized
            scheduleQuestResets();

            getLogger().info("Delayed initialization complete!");
        }, 40L); // 2-second delay (40 ticks)

        // Register commands
        registerCommands();

        getLogger().info("QuestSystem has been enabled!");
    }

    @Override
    public void onDisable() {
        if (questManager != null) {
            questManager.saveAllPlayerData();
        }

        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        getLogger().info("QuestSystem has been disabled!");
    }

    private boolean setupDatabase() {
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            questDatabase = new QuestDatabase(databaseManager);
            questDatabase.createTables();

            return true;
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Database setup failed!", e);
            return false;
        }
    }

    /**
     * Helper method to check if a player has a permission
     * @param player The player to check
     * @param permission The permission to check for
     * @return true if the player has the permission, false otherwise
     */
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    /**
     * Helper method to check if a player has a specific rank
     * @param player The player to check
     * @param rank The rank to check for (e.g., "premium", "deluxe")
     * @return true if the player has the rank, false otherwise
     */
    public boolean hasRank(Player player, String rank) {
        return player.hasPermission("daily." + rank);
    }

    private void setupAPIs() {
        // Debug: List all loaded plugins
        getLogger().info("Listing all loaded plugins for debugging:");
        for (org.bukkit.plugin.Plugin plugin : getServer().getPluginManager().getPlugins()) {
            getLogger().info("Loaded plugin: " + plugin.getName() + " (version: " + plugin.getDescription().getVersion() + ")");
        }

        // Setup Crafting API
        if (getServer().getPluginManager().getPlugin("MyCraftingPlugin2") != null) {
            craftingAPI = new CraftingAPI(this);
            getLogger().info("MyCraftingPlugin2 found, crafting quests enabled!");
        } else {
            getLogger().warning("MyCraftingPlugin2 not found, crafting quests will be disabled!");
        }

        // Setup LockpickChest API
        if (getServer().getPluginManager().getPlugin("ChestTreasure") != null) {
            lockpickChestAPI = new LockpickChestAPI(this);
            getLogger().info("ChestTreasure found, chest opening quests enabled!");
        } else {
            getLogger().warning("ChestTreasure not found, chest opening quests will be disabled!");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MythicMobKillListenerNew(this), this);
        getServer().getPluginManager().registerEvents(new ItemPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerKillListener(this), this);
        getServer().getPluginManager().registerEvents(new LootboxOpenListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLevelListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTimeListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestCompleteListener(this), this);
        // RewardsGUI registers itself as a listener in its constructor
    }

    private void registerCommands() {
        boolean normalRegistrationFailed = false;

        // Try normal command registration first
        if (getCommand("dailyquests") != null) {
            getCommand("dailyquests").setExecutor(new QuestCommand(this));
        } else {
            getLogger().warning("Failed to register 'dailyquests' command via plugin.yml - command not found!");
            normalRegistrationFailed = true;
        }

        if (getCommand("questadmin") != null) {
            getCommand("questadmin").setExecutor(new QuestAdminCommand(this));
        } else {
            getLogger().warning("Failed to register 'questadmin' command via plugin.yml - command not found!");
            normalRegistrationFailed = true;
        }

        if (getCommand("questrewards") != null) {
            getCommand("questrewards").setExecutor(new QuestRewardsCommand(this));
        } else {
            getLogger().warning("Failed to register 'questrewards' command via plugin.yml - command not found!");
            normalRegistrationFailed = true;
        }

        if (getCommand("dailyquestpreview") != null) {
            getCommand("dailyquestpreview").setExecutor(new QuestPreviewCommand(this));
        } else {
            getLogger().warning("Failed to register 'dailyquestpreview' command via plugin.yml - command not found!");
            normalRegistrationFailed = true;
        }

        // If normal registration failed, try manual registration
        if (normalRegistrationFailed) {
            getLogger().info("Attempting to register commands manually...");
            try {
                // Use CommandLoader as a fallback
                org.maks.questsystem.loader.CommandLoader commandLoader = new org.maks.questsystem.loader.CommandLoader(this);
                commandLoader.registerCommands();
            } catch (Exception e) {
                getLogger().severe("Failed to register commands manually: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            getLogger().info("All commands registered successfully via plugin.yml");
        }
    }

    private void scheduleQuestResets() {
        // Safety check to ensure questManager is initialized
        if (questManager == null) {
            getLogger().severe("Cannot schedule quest resets - questManager is null!");
            return;
        }

        // Daily reset at midnight
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (questManager != null) {
                questManager.resetDailyQuests();
            } else {
                getLogger().warning("questManager is null during daily reset!");
            }
        }, 20L, 72000L); // Check every hour

        // Weekly reset on Sunday
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (questManager != null) {
                questManager.resetWeeklyQuests();
            } else {
                getLogger().warning("questManager is null during weekly reset!");
            }
        }, 20L, 1728000L); // Check every day

        // Monthly reset on last day
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (questManager != null) {
                questManager.resetMonthlyQuests();
            } else {
                getLogger().warning("questManager is null during monthly reset!");
            }
        }, 20L, 1728000L); // Check every day

        getLogger().info("Quest reset tasks scheduled successfully!");
    }

    // Getters
    public static QuestSystem getInstance() { return instance; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public QuestDatabase getQuestDatabase() { return questDatabase; }
    public QuestManager getQuestManager() { return questManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public CraftingAPI getCraftingAPI() { return craftingAPI; }
    public LockpickChestAPI getLockpickChestAPI() { return lockpickChestAPI; }
}
