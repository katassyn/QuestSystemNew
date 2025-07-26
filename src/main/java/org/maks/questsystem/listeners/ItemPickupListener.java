package org.maks.questsystem.listeners;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.maks.questsystem.objects.PlayerQuestData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemPickupListener implements Listener {

    private final QuestSystem plugin;

    public ItemPickupListener(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        // Get player quest data
        PlayerQuestData playerData = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Check if player has any item-related quests before proceeding
        boolean hasItemQuests = false;
        boolean hasMagicQuest = false;
        boolean hasExtraordinaryQuest = false;
        boolean hasLegendaryQuest = false;
        boolean hasUniqueQuest = false;
        boolean hasMythicQuest = false;
        boolean hasQuestCraftingMaterialsQuest = false;
        boolean hasCraftingMaterialsQuest = false;

        // Check daily quest
        Quest dailyQuest = playerData.getDailyQuest();
        if (dailyQuest != null && !dailyQuest.isCompleted()) {
            QuestObjective objective = dailyQuest.getObjective();
            if (objective == QuestObjective.DROP_MAGIC) {
                hasMagicQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_EXTRAORDINARY) {
                hasExtraordinaryQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_LEGENDARY) {
                hasLegendaryQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_UNIQUE) {
                hasUniqueQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_MYTHIC) {
                hasMythicQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_QUEST_CRAFTING_MATERIALS) {
                hasQuestCraftingMaterialsQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_CRAFTING_MATERIALS) {
                hasCraftingMaterialsQuest = true;
                hasItemQuests = true;
            }
        }

        // Check weekly quest
        Quest weeklyQuest = playerData.getWeeklyQuest();
        if (weeklyQuest != null && !weeklyQuest.isCompleted()) {
            QuestObjective objective = weeklyQuest.getObjective();
            if (objective == QuestObjective.DROP_MAGIC) {
                hasMagicQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_EXTRAORDINARY) {
                hasExtraordinaryQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_LEGENDARY) {
                hasLegendaryQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_UNIQUE) {
                hasUniqueQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_MYTHIC) {
                hasMythicQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_QUEST_CRAFTING_MATERIALS) {
                hasQuestCraftingMaterialsQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_CRAFTING_MATERIALS) {
                hasCraftingMaterialsQuest = true;
                hasItemQuests = true;
            }
        }

        // Check monthly quest
        Quest monthlyQuest = playerData.getMonthlyQuest();
        if (monthlyQuest != null && !monthlyQuest.isCompleted()) {
            QuestObjective objective = monthlyQuest.getObjective();
            if (objective == QuestObjective.DROP_MAGIC) {
                hasMagicQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_EXTRAORDINARY) {
                hasExtraordinaryQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_LEGENDARY) {
                hasLegendaryQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_UNIQUE) {
                hasUniqueQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_MYTHIC) {
                hasMythicQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_QUEST_CRAFTING_MATERIALS) {
                hasQuestCraftingMaterialsQuest = true;
                hasItemQuests = true;
            } else if (objective == QuestObjective.DROP_CRAFTING_MATERIALS) {
                hasCraftingMaterialsQuest = true;
                hasItemQuests = true;
            }
        }

        // If player doesn't have any item quests, skip all the checks
        if (!hasItemQuests) {
            return;
        }

        // Check item rarity only if player has relevant quests
        String rarity = getItemRarity(item);
        if (rarity != null) {
            switch (rarity.toLowerCase()) {
                case "magic":
                    if (hasMagicQuest) {
                        plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_MAGIC, item.getAmount(), null);
                    }
                    break;
                case "extraordinary":
                    if (hasExtraordinaryQuest) {
                        plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_EXTRAORDINARY, item.getAmount(), null);
                    }
                    break;
                case "legendary":
                    if (hasLegendaryQuest) {
                        plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_LEGENDARY, item.getAmount(), null);
                    }
                    break;
                case "unique":
                    if (hasUniqueQuest) {
                        plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_UNIQUE, item.getAmount(), null);
                    }
                    break;
                case "mythic":
                    if (hasMythicQuest) {
                        plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_MYTHIC, item.getAmount(), null);
                    }
                    break;
            }
        }

        // Check if it's a crafting material only if player has relevant quests
        if (hasQuestCraftingMaterialsQuest && isQuestCraftingMaterial(item)) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_QUEST_CRAFTING_MATERIALS, item.getAmount(), null);
        }

        if (hasCraftingMaterialsQuest && isCraftingMaterial(item)) {
            plugin.getQuestManager().updateProgress(player, QuestObjective.DROP_CRAFTING_MATERIALS, item.getAmount(), null);
        }
    }

    private String getItemRarity(ItemStack item) {
        if (!item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return null;
        }

        List<String> lore = meta.getLore();
        for (String line : lore) {
            // Remove color codes and check for rarity
            String cleanLine = ChatColor.stripColor(line).toLowerCase();

            for (String rarityKeyword : plugin.getConfig().getConfigurationSection("rarity_keywords").getKeys(false)) {
                String keyword = plugin.getConfig().getString("rarity_keywords." + rarityKeyword);
                if (cleanLine.contains(keyword.toLowerCase())) {
                    return rarityKeyword;
                }
            }
        }

        return null;
    }

    private boolean isQuestCraftingMaterial(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        List<String> materials = plugin.getConfig().getStringList("quest_crafting_materials");

        for (String material : materials) {
            if (itemName.toLowerCase().contains(material.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private boolean isCraftingMaterial(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            // Check vanilla materials
            String materialName = item.getType().name();
            List<String> materials = plugin.getConfig().getStringList("crafting_materials");

            for (String material : materials) {
                if (materialName.toLowerCase().contains(material.toLowerCase().replace(" ", "_"))) {
                    return true;
                }
            }

            return false;
        }

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        List<String> materials = plugin.getConfig().getStringList("crafting_materials");

        for (String material : materials) {
            if (itemName.toLowerCase().contains(material.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
