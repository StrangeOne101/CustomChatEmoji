package com.strangeone101.customchatemoji;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {
    private static char emojiTag;
    private static HashMap<Character, EmojiEntry> emojiEntries;

    private static final boolean LOG_DEBUG = true;

    public static boolean setup() {
        Customchatemoji.getInstance().saveDefaultConfig();
        FileConfiguration config = Customchatemoji.getInstance().getConfig();

        String emojiTagString = config.getString("EmojiTag");
        if (LOG_DEBUG) Bukkit.getLogger().info("EmojiTag = [" + (emojiTagString != null ? emojiTagString : "null") + "]");
        if (emojiTagString != null) {
            emojiTag = emojiTagString.charAt(0);
            Bukkit.getLogger().info("emojiTag = [" + emojiTag + "]");
        }

        emojiEntries = new HashMap<>();

        ConfigurationSection emojiSection = config.getConfigurationSection("Emoji");
        if (LOG_DEBUG) Bukkit.getLogger().info("Emoji = [" + (emojiSection != null ? emojiSection.getName() : "null") + "]");
        if (emojiSection == null) return false;

        Map<String, Object> emojiMappings = emojiSection.getValues(false);
        for (Map.Entry<String, Object> emojiMapping : emojiMappings.entrySet()) {
            char emojiUnicode = emojiMapping.getKey().charAt(0);
            String emojiName = emojiMapping.getValue().toString();
            Bukkit.getLogger().info(Integer.toHexString(emojiUnicode) + " = [" + emojiName + "]");

            emojiEntries.put(emojiUnicode, new EmojiEntry(emojiName));
        }

        ConfigurationSection permissionSection = config.getConfigurationSection("Permission");
        if (LOG_DEBUG) Bukkit.getLogger().info("Permission = [" + (permissionSection != null ? permissionSection.getName() : "null") + "]");
        if (permissionSection == null) return false;

        for (String permission : permissionSection.getKeys(true)) {
            if (LOG_DEBUG) Bukkit.getLogger().info("permission = [" + permission + "]");
            if (!permissionSection.isList(permission)) continue;
            for (String range : permissionSection.getStringList(permission)) {
                if (LOG_DEBUG) Bukkit.getLogger().info(permission + " = [" + range + "]");
                String[] parts = range.split(":");

                if (parts.length == 2) {
                    char minRange, maxRange;
                    if (parts[0].length() != 1 || parts[1].length() != 1) {
                        Bukkit.getLogger().warning("Invalid unicode range: " + range);
                        continue;
                    }

                    minRange = parts[0].charAt(0);
                    maxRange = parts[1].charAt(0);
                    if (minRange >= maxRange) {
                        Bukkit.getLogger().warning("Invalid unicode range limit: " + range);
                        continue;
                    }

                    for (char unicode = minRange; unicode <= maxRange; unicode++) {
                        EmojiEntry entry = emojiEntries.get(unicode);
                        if (entry != null) {
                            Bukkit.getLogger().info(Integer.toHexString(unicode) + " added for " + permission);
                            entry.getPermissions().add(permission);
                        }
                    }
                } else if (parts.length == 1) {
                    if (parts[0].length() != 1) {
                        Bukkit.getLogger().warning("Invalid unicode: " + range);
                        continue;
                    }

                    char unicode = parts[0].charAt(0);
                    EmojiEntry entry = emojiEntries.get(unicode);
                    if (entry != null) {
                        Bukkit.getLogger().info(Integer.toHexString(unicode) + " added for " + permission);
                        entry.getPermissions().add(permission);
                    }
                } else {
                    Bukkit.getLogger().warning("Invalid range format: " + range);
                }
            }
        }
        return true;
    }

    public static class EmojiEntry {
        public EmojiEntry(String name, List<String> permissions) {
            this.name = name;
            this.permissions = permissions;
        }
        public EmojiEntry(String name) {
            this(name, new ArrayList<>());
        }
        public String getName() { return this.name; }
        public List<String> getPermissions() { return this.permissions; }

        private final String name;
        private final List<String> permissions;
    }

    public static char getEmojiTag() {
        return emojiTag;
    }

    public static HashMap<Character, EmojiEntry> getEmojiEntries() {
        return emojiEntries;
    }
}
