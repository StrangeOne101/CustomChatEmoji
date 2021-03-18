package com.strangeone101.customchatemoji;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EmojiUtil {
    public static char fromHexString (String hexString) {
        char emoji = '\0';
        try {
            int emojiInt = Integer.parseUnsignedInt(hexString, 16);
            if (emojiInt < Character.MAX_VALUE) {
                emoji = (char)emojiInt;
            }
        } catch (NumberFormatException ignored) { }
        return emoji;
    }

    public static boolean isPermitted (char emoji, Permissible permissible) {
        ConfigManager.EmojiEntry emojiEntry = ConfigManager.getEmojiEntries().get(emoji);
        if (emojiEntry == null) return true;

        for (String permission : emojiEntry.getGroups()) {
            if (permissible.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public static String toEmojiName (char emoji) {
        ConfigManager.EmojiEntry emojiEntry = ConfigManager.getEmojiEntries().get(emoji);
        if (emojiEntry == null) return null;

        return emojiEntry.getName();
    }

    public static char fromEmojiName (String emojiName) {
        if (emojiName == null) return '\0';
        Character emoji = ConfigManager.getEmojiNames().get(emojiName);
        if (emoji == null) return '\0';
        return emoji;
    }

    public static Set<String> allEmojiNames () {
        return ConfigManager.getEmojiNames().keySet();
    }

    public static List<String> allPermissions () {
        List<String> permissions = new ArrayList<>();

        for (ConfigManager.EmojiEntry entries : ConfigManager.getEmojiEntries().values()) {
            for (String permission : entries.getGroups()) {
                if (!permissions.contains(permission)) {
                    permissions.add(permission);
                }
            }
        }

        return permissions;
    }

    public static List<String> allPermittedEmojiNames (Permissible permissible) {
        List<String> emojiNames = new ArrayList<>();

        for (ConfigManager.EmojiEntry entries : ConfigManager.getEmojiEntries().values()) {
            for (String permission : entries.getGroups()) {
                if (permissible.hasPermission(permission)) {
                    emojiNames.add(entries.getName());
                }
            }
        }

        return emojiNames;
    }

    public static List<String> allPermittedEmojiFullNames (Permissible permissible) {
        List<String> emojiNames = new ArrayList<>();
        char emojiTag = ConfigManager.getEmojiTag();

        for (ConfigManager.EmojiEntry entries : ConfigManager.getEmojiEntries().values()) {
            for (String permission : entries.getGroups()) {
                if (permissible.hasPermission(permission)) {
                    emojiNames.add("" + emojiTag + entries.getName() + emojiTag);
                }
            }
        }

        return emojiNames;
    }

    public static TextComponent toTextComponent(String emojiName) {
        return toTextComponent(emojiName, EmojiUtil.fromEmojiName(emojiName));
    }

    public static TextComponent toTextComponent(char emoji) {
        return toTextComponent(EmojiUtil.toEmojiName(emoji), emoji);
    }

    public static TextComponent toTextComponent(String emojiName, char emoji) {
        char emojiTag = ConfigManager.getEmojiTag();

        TextComponent message = new TextComponent(String.valueOf(emoji));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("" + emojiTag + emojiName + emojiTag)));

        return message;
    }
}
