package com.strangeone101.customchatemoji.commands;

import com.strangeone101.customchatemoji.CharacterData;
import com.strangeone101.customchatemoji.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.List;

public class EditCommand extends BaseCommand {

    private int pageWidth = 8;

    public EditCommand() {
        super("edit", "List and edit all the custom emoji", "e");
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can edit characters!");
            return;
        }
        if (args.size() == 0) {
            sender.spigot().sendMessage(getPage(0));
        } else if (Util.isInteger(args.get(0))) {
            int page = Integer.parseInt(args.get(0)) - 1;
            if (page <= -1 || page * pageWidth * pageWidth > CharacterData.getCharacters().size()) {
                sender.sendMessage(ChatColor.RED + "Page out of range!");
                return;
            }
            sender.spigot().sendMessage(getPage(page));
        } else if (args.get(0).startsWith("\\u")) {
            try {
                Character character = Character.forDigit(Integer.parseInt(args.get(0).substring(2), 16), 10);
                if (CharacterData.getFromCharacter(character) == null) {
                    sender.sendMessage(ChatColor.RED + "No custom character loaded for character " + args.get(0).toUpperCase() + "! Edit the config to include it!");
                    return;
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Failed");
            }
        }

    }

    public TextComponent getPage(int page) {
        List<Character> chars = new ArrayList<>(CharacterData.getCharacters());
        int maxPage = CharacterData.getCharacters().size() / pageWidth / pageWidth;
        TextComponent left = new TextComponent("  ◄ ");
        left.setColor(page > 0 ? ChatColor.GOLD : ChatColor.GRAY);
        if (page > 0) {
            left.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "cce edit " + (page)));
            left.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "Visit page #" + (page))));
        }
        TextComponent right = new TextComponent(" ►  ");
        right.setColor(page < maxPage ? ChatColor.GOLD : ChatColor.GRAY);
        if (page < maxPage) {
            right.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "cce edit " + (page + 2)));
            right.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW + "Visit page #" + (page + 2))));
        }
        TextComponent mid = new TextComponent("Page " + (page + 1) + "of " + (maxPage + 1));
        mid.setColor(ChatColor.YELLOW);
        TextComponent base = new TextComponent("");
        base.addExtra(left);
        base.addExtra(mid);
        base.addExtra(right);
        base.addExtra("\n");
        for (int i = page * pageWidth; i < page * pageWidth + pageWidth && i < chars.size(); i++) {
            for (int j = 0; j < pageWidth && j + i < chars.size(); j++) {
                int index = i + j;
                Character chaR = chars.get(index);
                CharacterData data = CharacterData.getFromCharacter(chaR);
                TextComponent thisChar = new TextComponent(chaR.toString());
                String hover = ChatColor.GRAY + "Character " + Integer.toHexString(chaR.charValue()).toUpperCase() + "\n" +
                        ChatColor.YELLOW + "Can be colored: " + getBooleanString(data.isCanColor()) + "\n" +
                        ChatColor.YELLOW + "Case sensitive: " + getBooleanString(data.isCaseSensitive()) + "\n" +
                        ChatColor.YELLOW + "Aliases: " + (data.getAliases().length == 0 ? ChatColor.DARK_GRAY + "(None)" :
                                ChatColor.GRAY + String.join(ChatColor.DARK_GRAY + ", " + ChatColor.GRAY, data.getAliases())) + "\n" +
                        "\n" + ChatColor.YELLOW + "Click to edit!";

                thisChar.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
                thisChar.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "cce edit \\u" + Integer.toHexString(chaR.charValue())));
                base.addExtra(thisChar);
            }
            base.addExtra("\n");
        }
        for (BaseComponent bc : TextComponent.fromLegacyText(ChatColor.YELLOW + "Hover over the characters and click them to edit them!")) {
            base.addExtra(bc);
        }
        return base;
    }

    public String getBooleanString(boolean b) {
        return (b ? ChatColor.GREEN : ChatColor.RED) + "" + b;
    }
}
