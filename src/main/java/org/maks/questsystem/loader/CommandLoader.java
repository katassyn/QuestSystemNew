package org.maks.questsystem.loader;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.maks.questsystem.QuestSystem;
import org.maks.questsystem.commands.QuestAdminCommand;
import org.maks.questsystem.commands.QuestCommand;
import org.maks.questsystem.commands.QuestPreviewCommand;
import org.maks.questsystem.commands.QuestRewardsCommand;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Utility class to manually register commands with the server's command map.
 * This is a fallback mechanism in case the normal command registration via plugin.yml fails.
 */
public class CommandLoader {

    private final QuestSystem plugin;

    public CommandLoader(QuestSystem plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers all plugin commands manually using reflection to access the server's command map.
     * This bypasses the normal command registration process that relies on plugin.yml.
     */
    public void registerCommands() {
        try {
            // Get the server's command map via reflection
            final Server server = Bukkit.getServer();
            Field commandMapField = server.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(server);

            // Register each command
            registerCommand(commandMap, "dailyquests", "Opens the daily quests menu", "/<command>", 
                    new String[]{"quests", "dq"}, new QuestCommand(plugin));

            registerCommand(commandMap, "dailyquestpreview", "Shows quest progress preview", "/<command>", 
                    new String[]{"questpreview", "qp", "dqp"}, new QuestPreviewCommand(plugin));

            registerCommand(commandMap, "questadmin", "Admin commands for quest system", "/<command> [args]", 
                    new String[]{"qa"}, new QuestAdminCommand(plugin));

            registerCommand(commandMap, "questrewards", "Opens the quest rewards configuration GUI", "/<command>", 
                    new String[]{"qr"}, new QuestRewardsCommand(plugin));

            plugin.getLogger().info("Commands registered manually via CommandLoader");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register commands manually: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registers a single command with the server's command map.
     */
    private void registerCommand(CommandMap commandMap, String name, String description, String usage, 
                                String[] aliases, Object executor) throws Exception {
        // Create a new PluginCommand instance via reflection
        Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        PluginCommand command = constructor.newInstance(name, plugin);

        // Set command properties
        command.setDescription(description);
        command.setUsage(usage);
        command.setAliases(Arrays.asList(aliases));
        command.setExecutor((org.bukkit.command.CommandExecutor) executor);

        // Register the command with the command map
        commandMap.register(plugin.getName().toLowerCase(), command);
        plugin.getLogger().info("Registered command: " + name);
    }
}
