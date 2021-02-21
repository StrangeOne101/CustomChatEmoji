package com.strangeone101.customchatemoji.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand {

    public static Map<String, BaseCommand> commands = new HashMap<>();

    private String command;
    private String[] aliases;
    private String description;

    public BaseCommand(String name, String description, String... aliases) {
        this.command = name;
        this.description = description;
        this.aliases = aliases;

        commands.put(name.toLowerCase(), this);
        for (String s : aliases) {
            commands.put(s, this);
        }
    }

    public String getDescription() {
        return description;
    }

    public String getCommand() {
        return command;
    }

    public String[] getAliases() {
        return aliases;
    }

    public abstract void runCommand(CommandSender sender, List<String> args);

    public void suggest(CommandSender sender, List<String> args) {}
}
