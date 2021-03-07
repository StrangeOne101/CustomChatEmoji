package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.ConfigManager;
import com.strangeone101.customchatemoji.Customchatemoji;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CCECommand implements CommandExecutor {
    public CCECommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                String targetPlayerName = sender.getName();
                boolean elevated = sender.hasPermission("customchatemoji.config");
                if (args.length >= 2 && elevated) {
                    targetPlayerName = args[1];
                }
                list(sender, targetPlayerName, elevated);
                return true;
            }
        }

        return false;
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("customchatemoji.config")) {
            sender.sendMessage("You are not allowed to do that.");
            return;
        }
        Customchatemoji.getInstance().reloadConfig();
        boolean success = ConfigManager.setup();
        if (!success) {
            sender.sendMessage("Failed to reload ConfigManager");
        }
    }

    private void list(CommandSender sender, String targetPlayerName, boolean elevated) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            sender.sendMessage("Player " + targetPlayerName + " not found");
        } else {
            String header;
            if (elevated && !sender.getName().equals(targetPlayerName)) {
                header = "Allowed emojis for " + targetPlayerName;
            } else {
                header = "Allowed emojis";
            }
            sender.sendMessage(header);
            for (Map.Entry<Character, ConfigManager.EmojiEntry> emojiEntries : ConfigManager.getEmojiEntries().entrySet()) {
                ConfigManager.EmojiEntry emojiEntry = emojiEntries.getValue();
                String info = emojiEntries.getKey() + " " + ConfigManager.getEmojiTag() + emojiEntry.getName() + ConfigManager.getEmojiTag();
                boolean hasPermission = false;

                for (String permission : emojiEntry.getPermissions()) {
                    if (targetPlayer.hasPermission(permission)) {
                        hasPermission = true;
                        break;
                    }
                }

                if (hasPermission) {
                    info = "\u2713 " + info;
                } else {
                    info = "\u2717 " + info;
                }
                sender.sendMessage(info);
            }
        }
    }

}
