package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.ChatTokenizer;
import com.strangeone101.customchatemoji.ConfigManager;
import com.strangeone101.customchatemoji.Customchatemoji;
import com.strangeone101.customchatemoji.EmojiUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CCECommand implements CommandExecutor, TabCompleter {
    private static final boolean LOG_DEBUG = true;

    public CCECommand() {
        this.rootCommands = new ArrayList<>();
        this.rootCommands.add(new CommandPermission("show"));
        this.rootCommands.add(new CommandPermission("list"));
        this.rootCommands.add(new CommandPermission("reload", "customchatemoji.config"));
        this.rootCommands.add(new CommandPermission("chat"));
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
                results.add("all");
                for (String group : ConfigManager.getGroupEntries().keySet()) {
                    if (sender.hasPermission("customchatemoji.group." + group.toLowerCase())) {
                        results.add(group.toLowerCase());
                    }
                }
            }
        }

        // /cce chat autocomplete on any argument
        if (args.length >= 2 && args[0].equalsIgnoreCase("chat")) {
            for (String param : EmojiUtil.allPermittedEmojiFullNames(sender)) {
                if (param.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    results.add(param);
                }
            }
        }
        return results;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String reloadHelp = "/cce reload";
        String chatHelp = "/cce chat {messages}";
        String listHelp = "/cce list [group]|all";
        String showHelp = "/cce show <emoji>";

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                if (sender.hasPermission("customchatemoji.config")) {
                    sender.sendMessage(reloadHelp);
                }
                sender.sendMessage(chatHelp);
                sender.sendMessage(listHelp);
                sender.sendMessage(showHelp);
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            }  else if (args[0].equalsIgnoreCase("show")) {
                if (args.length < 2) {
                    sender.sendMessage(showHelp);
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
            } else if (args[0].equalsIgnoreCase("chat")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Command must be used in-game");
                    return true;
                }

                if (args.length == 1) {
                    sender.sendMessage(chatHelp);
                    return true;
                }
                chat((Player)sender, args);
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
            sender.spigot().sendMessage(EmojiUtil.toTextComponent(emojiName, emoji));
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
            BaseComponent[] messages = new BaseComponent[1 + emojiEntries.size()];

            int cursor = 0;
            messages[cursor++] = new TextComponent(group + " emojis:\n");

            for (ConfigManager.EmojiEntry emojiEntry : emojiEntries) {
                String emojiName = emojiEntry.getName();
                messages[cursor++] = EmojiUtil.toTextComponent(emojiName);
            }

            sender.spigot().sendMessage(messages);
        }
    }

    private void chat(Player sender, String[] args) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int index = 1; index < args.length; index++) { //skip first argument, which is 'chat'
            String arg = args[index];

            ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(arg, sender);
            if (parseResults.needsTransform) {
                ChatTokenizer.transform(arg, parseResults.chatTokens, stringBuilder);
            } else {
                stringBuilder.append(arg);
            }

            if (index < args.length - 1) {
                stringBuilder.append(' ');
            }
        }

        sender.chat(stringBuilder.toString());
    }
}
