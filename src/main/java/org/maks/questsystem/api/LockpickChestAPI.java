package org.maks.questsystem.api;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

import java.lang.reflect.Method;

public class LockpickChestAPI implements Listener {

    private final QuestSystem plugin;
    private Plugin chestPlugin;
    private boolean enabled = false;
    private boolean useReflection = false;
    private Class<?> chestOpenEventClass;
    private Method getPlayerMethod;
    private Method isSuccessMethod;
    private Method isCancelledMethod;

    public LockpickChestAPI(QuestSystem plugin) {
        this.plugin = plugin;

        // Hook into ChestTreasure plugin
        chestPlugin = Bukkit.getPluginManager().getPlugin("ChestTreasure");

        if (chestPlugin != null) {
            enabled = true;
            setupReflection();

            // Register dynamic listener if reflection successful
            if (useReflection) {
                registerDynamicListener();
            }

            // Always register the fallback listener
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("ChestTreasure API enabled - monitoring chest opening events");
        } else {
            plugin.getLogger().warning("ChestTreasure plugin not found! Chest opening quests will not work.");
        }
    }

    private void setupReflection() {
        try {
            // Try to find the ChestOpenEvent class
            chestOpenEventClass = Class.forName("maks.com.lockpickChestPlugin.events.ChestOpenEvent");

            // Get the required methods
            getPlayerMethod = chestOpenEventClass.getMethod("getPlayer");
            isSuccessMethod = chestOpenEventClass.getMethod("isSuccess");
            isCancelledMethod = chestOpenEventClass.getMethod("isCancelled");

            useReflection = true;
            plugin.getLogger().info("Successfully found ChestOpenEvent class");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not find ChestOpenEvent class: " + e.getMessage());
            plugin.getLogger().warning("Will use fallback chest detection");
        }
    }

    private void registerDynamicListener() {
        try {
            Bukkit.getPluginManager().registerEvent(
                chestOpenEventClass.asSubclass(Event.class),
                new Listener() {},
                EventPriority.MONITOR,
                (listener, event) -> {
                    try {
                        // Check if event is cancelled
                        Boolean cancelled = (Boolean) isCancelledMethod.invoke(event);
                        if (cancelled) {
                            return;
                        }

                        // Get player
                        Player player = (Player) getPlayerMethod.invoke(event);
                        if (player == null) {
                            return;
                        }

                        // Check if successful
                        Boolean success = (Boolean) isSuccessMethod.invoke(event);
                        if (success) {
                            handleChestOpen(player);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error handling chest open event: " + e.getMessage());
                    }
                },
                plugin,
                false
            );
            plugin.getLogger().info("Successfully registered dynamic listener for chest open events");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register dynamic listener: " + e.getMessage());
            useReflection = false;
        }
    }

    // Fallback handler for normal chest interactions (in case the plugin allows normal opens)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!enabled || event.isCancelled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        // Check if it's a chest or similar container
        Material type = block.getType();
        if (type != Material.CHEST && 
            type != Material.TRAPPED_CHEST && 
            type != Material.ENDER_CHEST &&
            type != Material.BARREL &&
            !type.name().contains("SHULKER_BOX")) {
            return;
        }

        Player player = event.getPlayer();

        // Check if ChestTreasure is managing this chest
        try {
            // Get ChestManager from the plugin
            Object chestManager = chestPlugin.getClass().getMethod("getChestManager").invoke(chestPlugin);

            // Check if chest exists at this location
            Object chest = chestManager.getClass()
                .getMethod("getChestAt", org.bukkit.Location.class)
                .invoke(chestManager, block.getLocation());

            // If this is a treasure chest, let ChestOpenEvent handle it
            if (chest != null) {
                return;
            }
        } catch (Exception e) {
            // If reflection fails, continue with normal handling
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().warning("Failed to check if chest is managed by ChestTreasure: " + e.getMessage());
            }
        }

        // We should only count chests from LockpickChestAPI, not normal Minecraft chests
        // handleChestOpen(player);
    }

    private void handleChestOpen(Player player) {
        // Get player quest data
        PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Check all quest types for chest opening objectives
        boolean hasChestQuest = false;

        // Check daily quest
        Quest dailyQuest = playerData.getDailyQuest();
        if (dailyQuest != null && !dailyQuest.isCompleted() && 
            dailyQuest.getObjective() == QuestObjective.OPEN_CHESTS) {
            hasChestQuest = true;
        }

        // Check weekly quest
        if (!hasChestQuest) {
            Quest weeklyQuest = playerData.getWeeklyQuest();
            if (weeklyQuest != null && !weeklyQuest.isCompleted() && 
                weeklyQuest.getObjective() == QuestObjective.OPEN_CHESTS) {
                hasChestQuest = true;
            }
        }

        // Check monthly quest
        if (!hasChestQuest) {
            Quest monthlyQuest = playerData.getMonthlyQuest();
            if (monthlyQuest != null && !monthlyQuest.isCompleted() && 
                monthlyQuest.getObjective() == QuestObjective.OPEN_CHESTS) {
                hasChestQuest = true;
            }
        }

        // Update progress if player has a chest opening quest
        if (hasChestQuest) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.OPEN_CHESTS, 1, null);

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("Player " + player.getName() + " opened a chest - quest progress updated");
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
