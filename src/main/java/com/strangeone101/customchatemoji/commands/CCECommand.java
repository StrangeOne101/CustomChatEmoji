package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.ChatTokenizer;
import com.strangeone101.customchatemoji.ConfigManager;
import com.strangeone101.customchatemoji.Customchatemoji;
import com.strangeone101.customchatemoji.EmojiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CCECommand implements CommandExecutor, TabCompleter {
    public CCECommand() {
        this.rootCommands = new ArrayList<>();
        this.rootCommands.add(new CommandPermission("show"));
        this.rootCommands.add(new CommandPermission("list"));
        this.rootCommands.add(new CommandPermission("perm", "customchatemoji.config"));
        this.rootCommands.add(new CommandPermission("reload", "customchatemoji.config"));

        this.permCommands = new ArrayList<>();
        this.permCommands.add("list");
        this.permCommands.add("add");
        this.permCommands.add("del");
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
    private final List<String> permCommands;

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

            } else if (args[0].equalsIgnoreCase("perm") &&
                    sender.hasPermission("customchatemoji.config")) {
                for (String param : this.permCommands) {
                    if (param.toLowerCase().startsWith(args[1].toLowerCase())) {
                        results.add(param);
                    }
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("perm") &&
                    sender.hasPermission("customchatemoji.config") &&
                    (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("del"))) {
                for (String param : EmojiUtil.allEmojiNames()) {
                    if (param.toLowerCase().startsWith(args[2].toLowerCase())) {
                        results.add(param);
                    }
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("perm") &&
                    sender.hasPermission("customchatemoji.config") &&
                    (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("del"))) {
                for (String param : EmojiUtil.allPermissions()) {
                    if (param.toLowerCase().startsWith(args[3].toLowerCase())) {
                        results.add(param);
                    }
                }
            }
        }
        return results;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String show = "/cce show <emoji>";
        String permList = "/cce perm list";
        String permAdd = "/cce perm add <emoji> <permission>";
        String permDel = "/cce perm del <emoji> <permission>";

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                if (sender.hasPermission("customchatemoji.config")) {
                    sender.sendMessage("/cce reload");
                    sender.sendMessage(show);
                    sender.sendMessage("/cce list [player]");
                    sender.sendMessage(permList);
                    sender.sendMessage(permAdd);
                    sender.sendMessage(permDel);
                } else {
                    sender.sendMessage(show);
                    sender.sendMessage("/cce list");
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
                Set<ConfigManager.EmojiEntry> chars = new LinkedHashSet<>(); //Keep the emoji in the order they were inserted

                if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
                    for (String group : ConfigManager.getGroupEntries().keySet()) {
                        if (sender.hasPermission("customchatemoji.group." + group.toLowerCase())) {
                            chars.addAll(ConfigManager.getGroupEntries().get(group.toLowerCase()));
                        }
                    }
                } else {
                    if (!ConfigManager.getGroupEntries().containsKey(args[1].toLowerCase())) {
                        sender.sendMessage(ChatColor.RED + "Emoji group not found! Use /cce list all to see all emoji");
                        return true;
                    }
                    chars.addAll(ConfigManager.getGroupEntries().get(args[1].toLowerCase()));

                }
                list(sender, chars);
                return true;
            } else if (args[0].equalsIgnoreCase("perm")) {
                if (!sender.hasPermission("customchatemoji.config")) return true;
                if (args.length < 2) {
                    sender.sendMessage(permList);
                    sender.sendMessage(permAdd);
                    sender.sendMessage(permDel);
                    return true;
                }

                if (args[1].equalsIgnoreCase("list")) {
                    listPerm(sender);
                    return true;
                } else if (args[1].equalsIgnoreCase("add")) {
                    if (args.length < 4) {
                        sender.sendMessage(permAdd);
                        return true;
                    }
                    addPerm(sender, args[2], args[3]);
                    return true;
                } else if (args[1].equalsIgnoreCase("del")) {
                    if (args.length < 4) {
                        sender.sendMessage(permDel);
                        return true;
                    }
                    delPerm(sender, args[2], args[3]);
                    return true;
                }

                sender.sendMessage(permList);
                sender.sendMessage(permAdd);
                sender.sendMessage(permDel);
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

    private void list(CommandSender sender, Collection<ConfigManager.EmojiEntry> chars) {
        //TODO
    }

    @Deprecated
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

                for (String permission : emojiEntry.getGroups()) {
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

    private void listPerm(CommandSender sender) {
        if (!sender.hasPermission("customchatemoji.config")) return;
        sender.sendMessage("Permission list");
        for (Map.Entry<Character, ConfigManager.EmojiEntry> emojiEntries : ConfigManager.getEmojiEntries().entrySet()) {
            ConfigManager.EmojiEntry emojiEntry = emojiEntries.getValue();
            sender.sendMessage(emojiEntries.getKey() + " " + emojiEntry.getName());

            Set<String> permissions = emojiEntry.getGroups();
            if (permissions.isEmpty()) {
                sender.sendMessage("- (No permissions set)");
            } else {
                for (String permission : permissions) {
                    sender.sendMessage("- " + permission);
                }
            }
        }
    }

    private void addPerm(CommandSender sender, String emojiName, String permission) {
        if (!sender.hasPermission("customchatemoji.config")) return;

        char emoji = EmojiUtil.fromEmojiName(emojiName);
        if (emoji == '\0') {
            sender.sendMessage(emojiName + " does not exist");
            return;
        }

        ConfigManager.addPermission(emoji, permission);
        sender.sendMessage("Permission " + permission + " added for " + emoji);
    }


    private void delPerm(CommandSender sender, String emojiName, String permission) {
        if (!sender.hasPermission("customchatemoji.config")) return;

        char emoji = EmojiUtil.fromEmojiName(emojiName);
        if (emoji == '\0') {
            sender.sendMessage("Emoji " + emojiName + " does not exist");
            return;
        }

        boolean success = ConfigManager.delPermission(emoji, permission);
        if (success) {
            sender.sendMessage("Permission " + permission + " deleted for " + emoji);
            if (ConfigManager.getEmojiEntries().get(emoji).getGroups().isEmpty()) {
                sender.sendMessage("Warning: " + emoji + " is now unused");
            }
        } else {
            sender.sendMessage("Permission " + permission + " does not exist for " + emoji);
        }
    }
}
