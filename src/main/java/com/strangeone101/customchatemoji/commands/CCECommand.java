package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.ConfigManager;
import com.strangeone101.customchatemoji.Customchatemoji;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CCECommand implements CommandExecutor {
    public CCECommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                Customchatemoji.getInstance().reloadConfig();
                boolean success = ConfigManager.setup();
                if (!success) {
                    sender.sendMessage("Failed to reload ConfigManager");
                }
                return true;
            }
        }

        return false;
    }
}
