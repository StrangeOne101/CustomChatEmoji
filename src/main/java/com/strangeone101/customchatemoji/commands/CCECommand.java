package com.strangeone101.customchatemoji.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class CCECommand implements CommandExecutor {

    public CCECommand() {
        new EditCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0 || !BaseCommand.commands.containsKey(args[0].toLowerCase())) {
            sendHelp(sender);
            return true;
        }

        BaseCommand cmd = BaseCommand.commands.get(args[0].toLowerCase());
        cmd.runCommand(sender, Arrays.asList(Arrays.copyOfRange(args, 1, args.length)));
        return true;
    }

    public void sendHelp(CommandSender sender) {
        for (BaseCommand cmd : BaseCommand.commands.values()) {
            sender.sendMessage(ChatColor.YELLOW + "/cce " + cmd.getCommand().toLowerCase() + " - " + cmd.getDescription());
        }
    }
}
