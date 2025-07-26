package org.maks.questsystem.commands;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.objects.PlayerQuestData;
import org.maks.questsystem.objects.Quest;
import org.maks.questsystem.objects.Quest.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestAdminCommand implements CommandExecutor, TabCompleter {

    private final QuestSystem plugin;

    public QuestAdminCommand(QuestSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("questsystem.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }

        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "reset":
                if (args.length >= 3) {
                    handleReset(sender, args[1], args[2]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /questadmin reset <player> <daily|weekly|monthly|all>");
                }
                break;

            case "complete":
                if (args.length >= 3) {
                    handleComplete(sender, args[1], args[2]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /questadmin complete <player> <daily|weekly|monthly>");
                }
                break;

            case "info":
                if (args.length >= 2) {
                    handleInfo(sender, args[1]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /questadmin info <player>");
                }
                break;

            case "resetall":
                if (args.length >= 2) {
                    handleResetAll(sender, args[1]);
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /questadmin resetall <daily|weekly|monthly>");
                }
                break;

            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Quest Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin reload" + ChatColor.GRAY + " - Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin reset <player> <type>" + ChatColor.GRAY + " - Reset player's quest");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin complete <player> <type>" + ChatColor.GRAY + " - Complete player's quest");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin info <player>" + ChatColor.GRAY + " - Show player's quest info");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin resetall <type>" + ChatColor.GRAY + " - Reset all quests of type");
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
    }

    private void handleReset(CommandSender sender, String playerName, String questType) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        PlayerQuestData data = plugin.getQuestManager().getPlayerData(target.getUniqueId());

        if (questType.equalsIgnoreCase("all")) {
            data.updateDailyReset();
            data.updateWeeklyReset();
            data.updateMonthlyReset();
            sender.sendMessage(ChatColor.GREEN + "Reset all quests for " + playerName);
        } else {
            try {
                QuestType type = QuestType.valueOf(questType.toUpperCase());
                Quest quest = data.getQuest(type);
                if (quest != null) {
                    quest.reset();
                    plugin.getQuestDatabase().saveQuest(target.getUniqueId(), quest);
                    sender.sendMessage(ChatColor.GREEN + "Reset " + type.name() + " quest for " + playerName);
                } else {
                    sender.sendMessage(ChatColor.RED + "Player has no active " + type.name() + " quest!");
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid quest type! Use: daily, weekly, monthly, or all");
            }
        }
    }

    private void handleComplete(CommandSender sender, String playerName, String questType) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        try {
            QuestType type = QuestType.valueOf(questType.toUpperCase());
            PlayerQuestData data = plugin.getQuestManager().getPlayerData(target.getUniqueId());
            Quest quest = data.getQuest(type);

            if (quest != null) {
                quest.setProgress(quest.getRequiredAmount());
                plugin.getQuestDatabase().saveQuest(target.getUniqueId(), quest);
                sender.sendMessage(ChatColor.GREEN + "Completed " + type.name() + " quest for " + playerName);

                // Notify player with center screen notification
                target.sendTitle(
                    ChatColor.GREEN + "Quest Completed!",
                    ChatColor.GOLD + quest.getDescription(),
                    10, 70, 20
                );
            } else {
                sender.sendMessage(ChatColor.RED + "Player has no active " + type.name() + " quest!");
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid quest type! Use: daily, weekly, or monthly");
        }
    }

    private void handleInfo(CommandSender sender, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        PlayerQuestData data = plugin.getQuestManager().getPlayerData(target.getUniqueId());

        sender.sendMessage(ChatColor.GOLD + "=== Quest Info for " + playerName + " ===");

        // Daily quest
        Quest daily = data.getDailyQuest();
        if (daily != null) {
            sender.sendMessage(ChatColor.YELLOW + "Daily: " + ChatColor.WHITE + daily.getDescription());
            sender.sendMessage(ChatColor.GRAY + "  Progress: " + daily.getCurrentProgress() + "/" + 
                             daily.getRequiredAmount() + " (" + 
                             String.format("%.1f%%", daily.getProgressPercentage()) + ")");
            sender.sendMessage(ChatColor.GRAY + "  Status: " + 
                             (daily.isRewardClaimed() ? "Claimed" : 
                              daily.isCompleted() ? "Complete" : "In Progress"));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Daily: " + ChatColor.GRAY + "No active quest");
        }

        // Weekly quest
        Quest weekly = data.getWeeklyQuest();
        if (weekly != null) {
            sender.sendMessage(ChatColor.GREEN + "Weekly: " + ChatColor.WHITE + weekly.getDescription());
            sender.sendMessage(ChatColor.GRAY + "  Progress: " + weekly.getCurrentProgress() + "/" + 
                             weekly.getRequiredAmount() + " (" + 
                             String.format("%.1f%%", weekly.getProgressPercentage()) + ")");
            sender.sendMessage(ChatColor.GRAY + "  Status: " + 
                             (weekly.isRewardClaimed() ? "Claimed" : 
                              weekly.isCompleted() ? "Complete" : "In Progress"));
        } else {
            sender.sendMessage(ChatColor.GREEN + "Weekly: " + ChatColor.GRAY + "No active quest");
        }

        // Monthly quest
        Quest monthly = data.getMonthlyQuest();
        if (monthly != null) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Monthly: " + ChatColor.WHITE + monthly.getDescription());
            sender.sendMessage(ChatColor.GRAY + "  Progress: " + monthly.getCurrentProgress() + "/" + 
                             monthly.getRequiredAmount() + " (" + 
                             String.format("%.1f%%", monthly.getProgressPercentage()) + ")");
            sender.sendMessage(ChatColor.GRAY + "  Status: " + 
                             (monthly.isRewardClaimed() ? "Claimed" : 
                              monthly.isCompleted() ? "Complete" : "In Progress"));
        } else {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Monthly: " + ChatColor.GRAY + "No active quest");
        }

        // Stats
        sender.sendMessage(ChatColor.AQUA + "Stats:");
        sender.sendMessage(ChatColor.GRAY + "  Completed daily quests: " + data.getCompletedDailyQuests());
        sender.sendMessage(ChatColor.GRAY + "  Completed weekly quests: " + data.getCompletedWeeklyQuests());
        sender.sendMessage(ChatColor.GRAY + "  Online time: " + data.getOnlineTimeHours() + " hours");
    }

    private void handleResetAll(CommandSender sender, String questType) {
        try {
            QuestType type = QuestType.valueOf(questType.toUpperCase());

            switch (type) {
                case DAILY:
                    plugin.getQuestManager().resetDailyQuests();
                    break;
                case WEEKLY:
                    plugin.getQuestManager().resetWeeklyQuests();
                    break;
                case MONTHLY:
                    plugin.getQuestManager().resetMonthlyQuests();
                    break;
            }

            sender.sendMessage(ChatColor.GREEN + "Reset all " + type.name() + " quests!");
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid quest type! Use: daily, weekly, or monthly");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("questsystem.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("reload", "reset", "complete", "info", "resetall")
                .stream()
                .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "reset":
                case "complete":
                case "info":
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());

                case "resetall":
                    return Arrays.asList("daily", "weekly", "monthly")
                        .stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("complete"))) {
            List<String> types = new ArrayList<>(Arrays.asList("daily", "weekly", "monthly"));
            if (args[0].equalsIgnoreCase("reset")) {
                types.add("all");
            }
            return types.stream()
                .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
