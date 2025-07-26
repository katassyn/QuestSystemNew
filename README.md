# QuestSystem Plugin

This is a new implementation of the QuestSystem plugin for Minecraft servers. It provides a comprehensive quest system with daily, weekly, and monthly quests.

## Features

- Daily, weekly, and monthly quests
- Integration with MythicMobs for mob-killing quests
- Integration with crafting systems
- Integration with chest-opening mechanics
- Admin commands for quest management
- Player GUI for quest tracking and rewards

## Installation

1. Clone this repository
2. Build the project using Maven:
   ```
   mvn clean package
   ```
3. Copy the generated JAR file from the `target` folder to your server's `plugins` folder
4. Restart your server

## Configuration

The plugin uses a `config.yml` file for configuration. You can customize:

- Database settings
- Quest reset times
- AFK detection
- GUI appearance
- MythicMobs integration
- Crafting materials
- Messages

## Commands

- `/dailyquests` (aliases: `/quests`, `/dq`) - Opens the quest menu
- `/questadmin` (aliases: `/qa`) - Admin commands for quest management
- `/questrewards` (aliases: `/qr`) - Opens the rewards configuration GUI

## Permissions

- `questsystem.use` - Allows players to use the quest system
- `questsystem.admin` - Allows access to admin commands
- `questsystem.reroll.free` - Allows one free reroll per day
- `questsystem.reroll.premium` - Allows 3 rerolls per day
- `questsystem.reroll.deluxe` - Allows 10 rerolls per day

## Dependencies

- Paper/Spigot 1.20.1+
- MySQL database
- Optional: MythicMobs, LockPickChestPlugin, MyCraftingPlugin2

## Notes

This project was created as a clean implementation of the QuestSystem plugin to resolve issues with the original implementation.