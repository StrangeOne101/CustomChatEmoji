package com.strangeone101.customchatemoji;

import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmojiUtil {
    public static boolean isPermitted (char emoji, Permissible permissible) {
        ConfigManager.EmojiEntry emojiEntry = ConfigManager.getEmojiEntries().get(emoji);
        if (emojiEntry == null) return true;

        for (String permission : emojiEntry.getPermissions()) {
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
            for (String permission : entries.getPermissions()) {
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
            for (String permission : entries.getPermissions()) {
                if (permissible.hasPermission(permission)) {
                    emojiNames.add(entries.getName());
                }
            }
        }

        return emojiNames;
    }
}
