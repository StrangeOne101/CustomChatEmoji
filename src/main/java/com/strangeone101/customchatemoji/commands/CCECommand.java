package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.ChatTokenizer;
import com.strangeone101.customchatemoji.ConfigManager;
import com.strangeone101.customchatemoji.Customchatemoji;
import com.strangeone101.customchatemoji.EmojiUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class CCECommand implements CommandExecutor, TabCompleter {
    private static final boolean LOG_DEBUG = false;

    private final String reloadHelp = "/cce reload - Reload the config";
    private final String chatHelp = "/cce chat {messages} - Chat with tab completion for emoji";
    private final String listHelp = "/cce list [group]|all - Show emoji you can use";
    private final String showHelp = "/cce show <emoji> - Preview what one emoji looks like";
    private final String versionHelp = "/cce version - Show what version of emoji you and the server are using";
    private final String resourcePackHelp = "/cce resourcepack - Send me the emoji resource pack!";

    public CCECommand() {
        this.rootCommands = new ArrayList<>();
        this.rootCommands.add(new CommandPermission("show"));
        this.rootCommands.add(new CommandPermission("list"));
        this.rootCommands.add(new CommandPermission("reload", "customchatemoji.command.reload"));
        this.rootCommands.add(new CommandPermission("chat"));
        this.rootCommands.add(new CommandPermission("version", "customchatemoji.command.version"));
        this.rootCommands.add(new CommandPermission("resourcepack"));
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
                        if (!param.command.equalsIgnoreCase("resourcepack") || ConfigManager.isResourcePackEnabled())
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


        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("help")) {
                help(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            }  else if (args[0].equalsIgnoreCase("show")) {
                if (args.length < 2) {
                    sender.sendMessage(showHelp);
                    return true;
                }
                show(sender, args[1].toLowerCase());
                return true;
            } else if (args[0].equalsIgnoreCase("list")) {
                if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
                    list(sender);
                } else {
                    String group = args[1].toLowerCase();
                    if (!ConfigManager.getGroupEntries().containsKey(group)) {
                        sender.sendMessage(ChatColor.RED + "[CCE] Emoji group not found! Use /cce list all to see all emoji");
                        return true;
                    }
                    list(sender, group);
                }
                return true;
            } else if (args[0].equalsIgnoreCase("chat")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "[CCE] Command must be used in-game");
                    return true;
                }

                if (args.length == 1) {
                    sender.sendMessage(chatHelp);
                    return true;
                }
                chat((Player)sender, args);
                return true;
            } else if (args[0].equalsIgnoreCase("version")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "[CCE] Command must be used in-game");
                    return true;
                }
                sender.sendMessage(ChatColor.YELLOW + "[CCE] The server is on " + ConfigManager.getVersion() + " and you are on \uefff");
                sender.sendMessage(ChatColor.YELLOW + "[CCE] If you can't see what version you are on, be sure you have the resource pack installed and enabled!");
                return true;
            } else if (args[0].equalsIgnoreCase("resourcepack") || args[0].equalsIgnoreCase("resource")) {
                if (!ConfigManager.isResourcePackEnabled()) {
                    sender.sendMessage(ChatColor.RED + "[CCE] The resource pack command is disabled right now!");
                    return true;
                }

                if (ConfigManager.getResourcePackURL().equals("")) {
                    sender.sendMessage(ChatColor.RED + "[CCE] Resource pack URL not found! Contact the admin!");
                    return true;
                }

                if (!(sender instanceof Player) && ConfigManager.isResourcePackDirect()) {
                    sender.sendMessage(ChatColor.RED + "[CCE] Command must be used in-game");
                    return true;
                }

                if (ConfigManager.isResourcePackDirect()) {
                    sender.sendMessage(ChatColor.GREEN + "[CCE] Sending resource pack...");
                    ((Player)sender).setResourcePack(ConfigManager.getResourcePackURL());
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.YELLOW + "Resource pack URL: ");
                        sender.sendMessage(ConfigManager.getResourcePackURL());
                    } else {
                        TextComponent base = new TextComponent("");
                        base.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Click!")));
                        base.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ConfigManager.getResourcePackURL()));
                        base.setExtra(Arrays.asList(TextComponent.fromLegacyText(ChatColor.GREEN + "" + ChatColor.BOLD + "Click here to download the resource pack!")));
                        sender.spigot().sendMessage(base);
                    }
                }
                return true;
            }
        }

        help(sender);

        return true; //We use our own help
    }

    private void show(CommandSender sender, String emojiName) {
        char emoji = EmojiUtil.fromEmojiName(emojiName);
        if (emoji == '\0') {
            String emojiTag = String.valueOf(ConfigManager.getEmojiTag());
            sender.sendMessage(ChatColor.RED + "[CCE] " + emojiTag + emojiName + emojiTag + " does not exist");
        } else {
            sender.spigot().sendMessage(EmojiUtil.toTextComponent(emojiName, emoji));
        }
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("customchatemoji.config")) return;
        Customchatemoji.getInstance().reloadConfig();
        boolean success = ConfigManager.setup();
        if (!success) {
            sender.sendMessage(ChatColor.RED + "[CCE] Failed to reload ConfigManager!");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "[CCE] Reloaded successfully with "
                + ConfigManager.getGroupEntries().size() + " groups and "
                + ConfigManager.getEmojiNames().size() + " emoji");
    }

    private void list(CommandSender sender) {
        if (LOG_DEBUG) Bukkit.getLogger().info("Running /cce list all");

        boolean seenAny = false;
        for (String group : ConfigManager.getGroupEntries().keySet()) {
            if (sender.hasPermission("customchatemoji.group." + group)) {
                list(sender, group);
                seenAny = true;
            } else {
                if (LOG_DEBUG) Bukkit.getLogger().info(sender.getName() + "does not have permission for " + group);
            }
        }

        if (!seenAny) {
            sender.sendMessage(ChatColor.RED + "[CCE] You don't have permission to view any emoji!");
        }
    }

    private void list(CommandSender sender, String group) {
        LinkedHashSet<ConfigManager.EmojiEntry> emojiEntries = ConfigManager.getGroupEntries().get(group);
        if (emojiEntries != null) {
            BaseComponent[] messages = new BaseComponent[1 + emojiEntries.size()];

            int cursor = 0;
            messages[cursor++] = new TextComponent(ChatColor.GREEN + pretty(group) + " Emoji:\n");

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

    private void help(CommandSender sender) {
        if (sender.hasPermission("customchatemoji.command.reload")) {
            sender.sendMessage(ChatColor.YELLOW + reloadHelp);
        }
        sender.sendMessage(ChatColor.YELLOW + chatHelp);
        sender.sendMessage(ChatColor.YELLOW + listHelp);
        sender.sendMessage(ChatColor.YELLOW + showHelp);
        if (sender.hasPermission("customchatemoji.command.version")) {
            sender.sendMessage(ChatColor.YELLOW + versionHelp);
        }
        if (ConfigManager.isResourcePackEnabled()) {
            sender.sendMessage(ChatColor.YELLOW + resourcePackHelp);
        }
    }

    private String pretty(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
