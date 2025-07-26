package org.maks.questsystem.commands;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.PlayerQuestData;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestType;
import org.maks.questsystem.objects.Quest.QuestObjective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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

public class QuestPreviewCommand implements CommandExecutor, Listener {

    private final QuestSystem plugin;
    private final String GUI_TITLE = ChatColor.GOLD + "Quest Progress Preview";

    public QuestPreviewCommand(QuestSystem plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("questsystem.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }

        openPreviewGUI(player);
        return true;
    }

    private void openPreviewGUI(Player player) {
        int size = 27; // Same size as main quest GUI
        Inventory inv = Bukkit.createInventory(null, size, GUI_TITLE);

        // Fill with background glass
        fillBackground(inv);

        // Get player quest data
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player.getUniqueId());

        // Daily quest item
        ItemStack dailyItem = createQuestItem(data, data.getDailyQuest(), QuestType.DAILY);
        inv.setItem(11, dailyItem);

        // Weekly quest item
        ItemStack weeklyItem = createQuestItem(data, data.getWeeklyQuest(), QuestType.WEEKLY);
        inv.setItem(13, weeklyItem);

        // Monthly quest item
        ItemStack monthlyItem = createQuestItem(data, data.getMonthlyQuest(), QuestType.MONTHLY);
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

    private ItemStack createQuestItem(PlayerQuestData data, Quest quest, QuestType type) {
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
            lore.add(ChatColor.GRAY + "Visit innkeeper to receive quests!");
        } else {
            lore.add(ChatColor.GRAY + quest.getDescription());
            lore.add("");

            // Progress bar
            String progressBar = createProgressBar(quest.getCurrentProgress(), quest.getRequiredAmount());
            lore.add(ChatColor.YELLOW + "Progress: " + progressBar);
            
            // For SPEND_HOURS_ONLINE quests, display online time in minutes
            if (quest.getObjective() == QuestObjective.SPEND_HOURS_ONLINE) {
                long currentMinutes = data.getOnlineTime();
                long requiredMinutes = quest.getRequiredAmount() * 60;
                double completion = (double) currentMinutes / requiredMinutes * 100;
                lore.add(ChatColor.YELLOW + "" + currentMinutes + "/" + requiredMinutes + " min" +
                        " (" + String.format("%.1f%%", completion) + ")");
            } else {
                lore.add(ChatColor.YELLOW + "" + quest.getCurrentProgress() + "/" + quest.getRequiredAmount() +
                        " (" + String.format("%.1f%%", quest.getProgressPercentage()) + ")");
            }
            lore.add("");

            if (quest.isCompleted()) {
                if (quest.isRewardClaimed()) {
                    lore.add(ChatColor.GREEN + "✓ Completed and claimed");
                } else {
                    lore.add(ChatColor.GOLD + "✓ Completed!");
                }
            } else {
                lore.add(ChatColor.RED + "✗ Not completed");
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

        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true); // Cancel all clicks in the preview GUI
        }
    }

    private void displayQuestProgress(Player player, Quest quest, QuestType type, PlayerQuestData data) {
        ChatColor typeColor = getTypeColor(type);
        String typeName = getTypeName(type);

        if (quest == null) {
            player.sendMessage(typeColor + typeName + ": " + ChatColor.RED + "No active quest");
            player.sendMessage("");
            return;
        }

        player.sendMessage(typeColor + typeName + ": " + ChatColor.WHITE + quest.getDescription());

        // Progress bar
        String progressBar = createProgressBar(quest.getCurrentProgress(), quest.getRequiredAmount());
        
        // For SPEND_HOURS_ONLINE quests, display time in minutes
        if (quest.getObjective() == QuestObjective.SPEND_HOURS_ONLINE) {
            long currentMinutes = data.getOnlineTime();
            long requiredMinutes = quest.getRequiredAmount() * 60;
            double completion = (double) currentMinutes / requiredMinutes * 100;
            player.sendMessage(ChatColor.GRAY + "Progress: " + progressBar + " " +
                             ChatColor.WHITE + currentMinutes + "/" + requiredMinutes + " min" +
                             " (" + String.format("%.1f%%", completion) + ")");
        } else {
            player.sendMessage(ChatColor.GRAY + "Progress: " + progressBar + " " + 
                             ChatColor.WHITE + quest.getCurrentProgress() + "/" + quest.getRequiredAmount() + 
                             " (" + String.format("%.1f%%", quest.getProgressPercentage()) + ")");
        }

        // Status
        if (quest.isRewardClaimed()) {
            player.sendMessage(ChatColor.GREEN + "✓ Completed and claimed");
        } else if (quest.isCompleted()) {
            player.sendMessage(ChatColor.GOLD + "✓ Completed!");
        } else {
            player.sendMessage(ChatColor.RED + "✗ In progress");
        }

        player.sendMessage("");
    }

    private String createProgressBar(int current, int max) {
        int barLength = 20;
        int filled = (int) ((double) current / max * barLength);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GREEN);

        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append(ChatColor.GRAY).append("█");
            }
        }

        return bar.toString();
    }

    private ChatColor getTypeColor(QuestType type) {
        switch (type) {
            case DAILY:
                return ChatColor.YELLOW;
            case WEEKLY:
                return ChatColor.GREEN;
            case MONTHLY:
                return ChatColor.LIGHT_PURPLE;
            default:
                return ChatColor.WHITE;
        }
    }

    private String getTypeName(QuestType type) {
        switch (type) {
            case DAILY:
                return "Daily Quest";
            case WEEKLY:
                return "Weekly Quest";
            case MONTHLY:
                return "Monthly Quest";
            default:
                return "Unknown Quest";
        }
    }
}
