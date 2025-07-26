package org.maks.questsystem.commands;

import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.gui.QuestGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor {
    
    private final QuestSystem plugin;
    private final QuestGUI questGUI;
    
    public QuestCommand(QuestSystem plugin) {
        this.plugin = plugin;
        this.questGUI = new QuestGUI(plugin);
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
        
        // Open quest GUI
        questGUI.openMainMenu(player);
        
        return true;
    }
}