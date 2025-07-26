package org.maks.questsystem.gui;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.PlayerQuestData;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestType;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestGUI implements Listener {

    private final QuestSystem plugin;

    public QuestGUI(QuestSystem plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        int size = plugin.getConfig().getInt("quests.gui.size", 54);
        String title = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("quests.gui.title", "&6&lQuest System"));

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Fill with background glass
        fillBackground(inv);

        // Get player quest data
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Daily quest item
        ItemStack dailyItem = createQuestItem(data.getDailyQuest(), QuestType.DAILY);
        inv.setItem(11, dailyItem);

        // Weekly quest item
        ItemStack weeklyItem = createQuestItem(data.getWeeklyQuest(), QuestType.WEEKLY);
        inv.setItem(13, weeklyItem);

        // Monthly quest item
        ItemStack monthlyItem = createQuestItem(data.getMonthlyQuest(), QuestType.MONTHLY);
        inv.setItem(15, monthlyItem);

        // Info item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "Quest Information");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Daily quests reset at midnight");
        infoLore.add(ChatColor.GRAY + "Weekly quests reset on Sunday");
        infoLore.add(ChatColor.GRAY + "Monthly quests reset on the last day of the month");
        infoLore.add("");
        infoLore.add(ChatColor.YELLOW + "Completed daily quests: " + data.getCompletedDailyQuests());
        infoLore.add(ChatColor.YELLOW + "Completed weekly quests: " + data.getCompletedWeeklyQuests());
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(22, infoItem);

        player.openInventory(inv);
    }

    private ItemStack createQuestItem(Quest quest, QuestType type) {
        Material material;
        String name;
        ChatColor color;

        switch (type) {
            case DAILY:
                material = Material.CLOCK;
                name = "Daily Quest";
                color = ChatColor.YELLOW;
                break;
            case WEEKLY:
                material = Material.COMPASS;
                name = "Weekly Quest";
                color = ChatColor.GREEN;
                break;
            case MONTHLY:
                material = Material.NETHER_STAR;
                name = "Monthly Quest";
                color = ChatColor.LIGHT_PURPLE;
                break;
            default:
                material = Material.PAPER;
                name = "Unknown Quest";
                color = ChatColor.WHITE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + name);

        List<String> lore = new ArrayList<>();

        if (quest == null) {
            lore.add(ChatColor.RED + "No active quest");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to get a new quest");
        } else {
            lore.add(ChatColor.GRAY + quest.getDescription());
            lore.add("");
            
            // For SPEND_HOURS_ONLINE quests, display time in minutes
            if (quest.getObjective() == QuestObjective.SPEND_HOURS_ONLINE) {
                long currentMinutes = quest.getCurrentProgress() * 60;
                long requiredMinutes = quest.getRequiredAmount() * 60;
                lore.add(ChatColor.YELLOW + "Progress: " + currentMinutes + "/" + requiredMinutes + " min");
                lore.add(ChatColor.YELLOW + "Completion: " + String.format("%.1f%%", quest.getProgressPercentage()));
            } else {
                lore.add(ChatColor.YELLOW + "Progress: " + quest.getCurrentProgress() + "/" + quest.getRequiredAmount());
                lore.add(ChatColor.YELLOW + "Completion: " + String.format("%.1f%%", quest.getProgressPercentage()));
            }
            lore.add("");

            if (quest.isCompleted()) {
                if (quest.isRewardClaimed()) {
                    lore.add(ChatColor.GREEN + "✓ Completed and claimed");
                } else {
                    lore.add(ChatColor.GOLD + "✓ Completed! Click to claim reward");
                }
            } else {
                lore.add(ChatColor.RED + "✗ Not completed");
                lore.add("");
                lore.add(ChatColor.YELLOW + "Right-click to abandon and get a new quest");
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private void fillBackground(Inventory inv) {
        List<String> bgColors = plugin.getConfig().getStringList("quests.gui.background_colors");
        if (bgColors.isEmpty()) {
            bgColors = Arrays.asList("BLACK", "WHITE");
        }

        boolean alternate = false;
        for (int i = 0; i < inv.getSize(); i++) {
            if (i % 9 == 0) alternate = !alternate;

            String colorName = bgColors.get(alternate ? 0 : 1 % bgColors.size());
            try {
                Material glassMaterial = Material.valueOf(colorName + "_STAINED_GLASS_PANE");
                ItemStack glass = new ItemStack(glassMaterial);
                ItemMeta meta = glass.getItemMeta();
                meta.setDisplayName(" ");
                glass.setItemMeta(meta);
                inv.setItem(i, glass);
            } catch (IllegalArgumentException e) {
                // Fallback to regular glass pane
                ItemStack glass = new ItemStack(Material.GLASS_PANE);
                ItemMeta meta = glass.getItemMeta();
                meta.setDisplayName(" ");
                glass.setItemMeta(meta);
                inv.setItem(i, glass);
            }

            alternate = !alternate;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        String configTitle = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("quests.gui.title", "&6&lQuest System"));

        if (!title.equals(configTitle)) {
            return;
        }

        event.setCancelled(true);

        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        switch (event.getSlot()) {
            case 11: // Daily quest
                handleQuestClick(player, data, QuestType.DAILY, event.isRightClick());
                break;
            case 13: // Weekly quest
                handleQuestClick(player, data, QuestType.WEEKLY, event.isRightClick());
                break;
            case 15: // Monthly quest
                handleQuestClick(player, data, QuestType.MONTHLY, event.isRightClick());
                break;
            case 22: // Info item (no action needed)
                break;
        }
    }

    private void handleQuestClick(Player player, PlayerQuestData data, QuestType type, boolean isRightClick) {
        Quest quest = data.getQuest(type);

        if (quest == null) {
            // Assign new quest
            plugin.getQuestManager().assignQuest(player, type);
            openMainMenu(player);
            return;
        }

        if (isRightClick && !quest.isCompleted()) {
            // Reroll quest
            boolean isPremium = player.hasPermission("questsystem.reroll.premium");
            boolean isDeluxe = player.hasPermission("questsystem.reroll.deluxe");

            if (data.canReroll(type, isPremium, isDeluxe)) {
                Quest newQuest = plugin.getQuestManager().rerollQuest(player, type);
                if (newQuest != null) {
                    player.sendMessage(plugin.getConfigManager().getMessage("reroll_used"));
                    openMainMenu(player);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("reroll_limit"));
                }
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("reroll_limit"));
            }
            return;
        }

        if (quest.isCompleted() && !quest.isRewardClaimed()) {
            // Claim reward
            boolean claimed = plugin.getQuestManager().claimReward(player, type);
            if (claimed) {
                openMainMenu(player);
            }
        }
    }
}
