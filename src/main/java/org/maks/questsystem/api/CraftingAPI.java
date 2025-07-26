package org.maks.questsystem.api;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.Material;

import java.lang.reflect.Method;

public class CraftingAPI implements Listener {

    private final QuestSystem plugin;
    private Plugin craftingPlugin;
    private boolean enabled = false;
    private boolean useReflection = false;

    // Reflection fields
    private Class<?> customCraftEventClass;
    private Method getPlayerMethod;
    private Method getResultMethod;
    private Method getAmountMethod;
    private Method isCancelledMethod;

    public CraftingAPI(QuestSystem plugin) {
        this.plugin = plugin;

        // Hook into MyCraftingPlugin2
        craftingPlugin = Bukkit.getPluginManager().getPlugin("MyCraftingPlugin2");

        if (craftingPlugin != null) {
            enabled = true;
            setupReflection();

            // Register dynamic listener if reflection successful
            if (useReflection) {
                registerDynamicListener();
            }

            // Always register for vanilla crafting
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("MyCraftingPlugin2 API enabled - monitoring crafting events");
        } else {
            // Still register for vanilla crafting
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().warning("MyCraftingPlugin2 not found, only vanilla crafting will be tracked!");
        }
    }

    private void setupReflection() {
        try {
            // Try to find the CustomCraftEvent class
            customCraftEventClass = Class.forName("com.maks.mycraftingplugin2.events.CustomCraftEvent");

            // Get the required methods
            getPlayerMethod = customCraftEventClass.getMethod("getPlayer");
            getResultMethod = customCraftEventClass.getMethod("getResult");
            getAmountMethod = customCraftEventClass.getMethod("getAmount");
            isCancelledMethod = customCraftEventClass.getMethod("isCancelled");

            useReflection = true;
            plugin.getLogger().info("Successfully found CustomCraftEvent class");
        } catch (Exception e) {
            plugin.getLogger().warning("Could not find CustomCraftEvent class: " + e.getMessage());
            plugin.getLogger().warning("Will only track vanilla crafting");
        }
    }

    private void registerDynamicListener() {
        try {
            Bukkit.getPluginManager().registerEvent(
                customCraftEventClass.asSubclass(Event.class),
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

                        // Get result item
                        ItemStack result = (ItemStack) getResultMethod.invoke(event);
                        if (result == null || result.getType() == Material.AIR) {
                            return;
                        }

                        // Get amount
                        int amount = 1;
                        Object amountObj = getAmountMethod.invoke(event);
                        if (amountObj instanceof Integer) {
                            amount = (Integer) amountObj;
                        }

                        handleCraftItem(player, result, amount);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error handling custom craft event: " + e.getMessage());
                    }
                },
                plugin,
                false
            );
            plugin.getLogger().info("Successfully registered dynamic listener for custom craft events");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register dynamic listener: " + e.getMessage());
            useReflection = false;
        }
    }

    // Handler for vanilla Minecraft crafting
    @EventHandler(priority = EventPriority.MONITOR)
    public void onVanillaCraft(CraftItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack result = event.getRecipe().getResult();

        if (result != null && result.getType() != Material.AIR) {
            // Calculate actual amount crafted
            int amount = result.getAmount();

            // Check for shift-click (craft all)
            if (event.isShiftClick()) {
                int lowestAmount = 64;
                for (ItemStack item : event.getInventory().getMatrix()) {
                    if (item != null && item.getType() != Material.AIR) {
                        lowestAmount = Math.min(lowestAmount, item.getAmount());
                    }
                }
                amount *= lowestAmount;
            }

            handleCraftItem(player, result, amount);
        }
    }

    // Additional listener for other crafting methods (furnace, etc.)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFurnaceExtract(org.bukkit.event.inventory.FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        int amount = event.getItemAmount();

        if (amount > 0) {
            // Create a dummy ItemStack for the extracted item
            ItemStack result = new ItemStack(event.getItemType(), amount);
            handleCraftItem(player, result, amount);
        }
    }

    private void handleCraftItem(Player player, ItemStack result, int amount) {
        // Get player quest data
        PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Check all quest types for crafting objectives
        boolean hasCraftingQuest = false;
        String targetItem = null;

        // Check daily quest
        Quest dailyQuest = playerData.getDailyQuest();
        if (dailyQuest != null && !dailyQuest.isCompleted() && 
            dailyQuest.getObjective() == QuestObjective.CRAFT_ITEMS) {
            hasCraftingQuest = true;
            targetItem = dailyQuest.getSpecificTarget();
        }

        // Check weekly quest
        if (!hasCraftingQuest) {
            Quest weeklyQuest = playerData.getWeeklyQuest();
            if (weeklyQuest != null && !weeklyQuest.isCompleted() && 
                weeklyQuest.getObjective() == QuestObjective.CRAFT_ITEMS) {
                hasCraftingQuest = true;
                targetItem = weeklyQuest.getSpecificTarget();
            }
        }

        // Check monthly quest
        if (!hasCraftingQuest) {
            Quest monthlyQuest = playerData.getMonthlyQuest();
            if (monthlyQuest != null && !monthlyQuest.isCompleted() && 
                monthlyQuest.getObjective() == QuestObjective.CRAFT_ITEMS) {
                hasCraftingQuest = true;
                targetItem = monthlyQuest.getSpecificTarget();
            }
        }

        // Update progress if player has a crafting quest
        if (hasCraftingQuest) {
            // If no specific target item, or if the crafted item matches the target
            if (targetItem == null || targetItem.isEmpty() || 
                result.getType().name().equalsIgnoreCase(targetItem)) {

                plugin.getQuestManager().updateProgress(player, 
                    QuestObjective.CRAFT_ITEMS, amount, targetItem);

                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("Player " + player.getName() + 
                        " crafted " + amount + "x " + result.getType().name());
                }
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
