package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.ConfigManager;
import com.strangeone101.customchatemoji.Customchatemoji;
import com.strangeone101.customchatemoji.EmojiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class CCECommand implements CommandExecutor, TabCompleter {
    private static final boolean LOG_DEBUG = true;

    public CCECommand() {
        this.rootCommands = new ArrayList<>();
        this.rootCommands.add(new CommandPermission("show"));
        this.rootCommands.add(new CommandPermission("list"));
        this.rootCommands.add(new CommandPermission("reload", "customchatemoji.config"));
    }

    private static class CommandPermission {
        public CommandPermission(String command, String permission) {
            this.command = command;
            this.permission = permission;
        }
        public CommandPermission(String command) {
            this(command, null);
        }
        public String command;
        public String permission;
    }

    private final List<CommandPermission> rootCommands;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            for (CommandPermission param : this.rootCommands) {
                if (param.command.toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (param.permission == null || sender.hasPermission(param.permission)) {
                        results.add(param.command);
                    }
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("show")) {
                for (String param : EmojiUtil.allPermittedEmojiNames(sender)) {
                    if (param.toLowerCase().startsWith(args[1].toLowerCase())) {
                        results.add(param);
                    }
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                List<String> groups = new ArrayList<>();
                groups.add("all");
                for (String group : ConfigManager.getGroupEntries().keySet()) {
                    if (sender.hasPermission("customchatemoji.group." + group.toLowerCase())) {
                        groups.add(group.toLowerCase());
                    }
                }

                return groups;

            }
        }
        return results;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String show = "/cce show <emoji>";

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                if (sender.hasPermission("customchatemoji.config")) {
                    sender.sendMessage("/cce reload");
                    sender.sendMessage(show);
                    sender.sendMessage("/cce list [group]|all");
                } else {
                    sender.sendMessage(show);
                    sender.sendMessage("/cce list [group]|all");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            }  else if (args[0].equalsIgnoreCase("show")) {
                if (args.length < 2) {
                    sender.sendMessage(show);
                    return true;
                }
                show(sender, args[1]);
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
                    list(sender);
                } else {
                    String group = args[1].toLowerCase();
                    if (!ConfigManager.getGroupEntries().containsKey(group)) {
                        sender.sendMessage(ChatColor.RED + "Emoji group not found! Use /cce list all to see all emoji");
                        return true;
                    }
                    list(sender, group);
                }
                return true;
            }
        }

        return false;
    }

    private void show(CommandSender sender, String emojiName) {
        char emoji = EmojiUtil.fromEmojiName(emojiName);
        if (emoji == '\0') {
            String emojiTag = String.valueOf(ConfigManager.getEmojiTag());
            sender.sendMessage(emojiTag + emojiName + emojiTag + " does not exist");
        } else {
            sender.sendMessage(String.valueOf(emoji));
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("customchatemoji.config")) return;
        Customchatemoji.getInstance().reloadConfig();
        boolean success = ConfigManager.setup();
        if (!success) {
            sender.sendMessage("Failed to reload ConfigManager");
        }
    }

    private void list(CommandSender sender) {
        if (LOG_DEBUG) Bukkit.getLogger().info("Running /cce list all");

        for (String group : ConfigManager.getGroupEntries().keySet()) {
            if (sender.hasPermission("customchatemoji.group." + group)) {
                list(sender, group);
            } else {
                if (LOG_DEBUG) Bukkit.getLogger().info(sender.getName() + "does not have permission for " + group);
            }
        }
    }

    private void list(CommandSender sender, String group) {
        LinkedHashSet<ConfigManager.EmojiEntry> emojiEntries = ConfigManager.getGroupEntries().get(group);
        if (emojiEntries != null) {
            sender.sendMessage(group + " emojis:");
            StringBuilder stringBuilder = new StringBuilder();

            for (ConfigManager.EmojiEntry emojiEntry : emojiEntries) {
                char unicode = ConfigManager.getEmojiNames().get(emojiEntry.getName());
                stringBuilder.append(unicode);
            }

            sender.sendMessage(stringBuilder.toString());
        }
    }
}
