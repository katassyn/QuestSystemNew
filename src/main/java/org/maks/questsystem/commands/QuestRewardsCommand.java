package org.maks.questsystem.commands;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.gui.RewardsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestRewardsCommand implements CommandExecutor {
    
    private final QuestSystem plugin;
    private final RewardsGUI rewardsGUI;
    
    public QuestRewardsCommand(QuestSystem plugin) {
        this.plugin = plugin;
        this.rewardsGUI = new RewardsGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("questsystem.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
            return true;
        }
        
        // Open rewards configuration GUI
        rewardsGUI.openMainMenu(player);
        
        return true;
    }
}