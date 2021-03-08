package com.strangeone101.customchatemoji;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ConfigManager {
    private static FileConfiguration config;
    private static char emojiTag;
    private static HashMap<Character, EmojiEntry> emojiEntries;

    private static final boolean LOG_DEBUG = true;

    public static boolean setup() {
        Customchatemoji.getInstance().saveDefaultConfig();
        config = Customchatemoji.getInstance().getConfig();

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
            for (String emoji : permissionSection.getStringList(permission)) {
                if (LOG_DEBUG) Bukkit.getLogger().info(permission + " = [" + emoji + "]");

                if (emoji.length() != 1) {
                    Bukkit.getLogger().warning("Invalid unicode: " + emoji);
                    continue;
                }

                char unicode = emoji.charAt(0);
                EmojiEntry entry = emojiEntries.get(unicode);
                if (entry != null) {
                    Bukkit.getLogger().info(Integer.toHexString(unicode) + " added for " + permission);
                    entry.getPermissions().add(permission);
                }
            }
        }
        return true;
    }

    public static class EmojiEntry {
        public EmojiEntry(String name, Set<String> permissions) {
            this.name = name;
            this.permissions = permissions;
        }
        public EmojiEntry(String name) {
            this(name, new HashSet<>());
        }
        public String getName() { return this.name; }
        public Set<String> getPermissions() { return this.permissions; }

        private final String name;
        private final Set<String> permissions;
    }

    public static char getEmojiTag() {
        return emojiTag;
    }

    public static HashMap<Character, EmojiEntry> getEmojiEntries() {
        return emojiEntries;
    }

    public static void addPermission(char emoji, String permission) {
        //Memory
        ConfigManager.getEmojiEntries().get(emoji).getPermissions().add(permission);

        //File
        String key = "Permission." + permission;

        List<String> emojis = config.getStringList(key);
        if (!emojis.contains(emoji)) {
            emojis.add(String.valueOf(emoji));
            config.set(key, emojis);
            Customchatemoji.getInstance().saveConfig();
        }
    }

    public static boolean delPermission(char emoji, String permission) {
        boolean success;
        //Memory
        success = ConfigManager.getEmojiEntries().get(emoji).getPermissions().remove(permission);

        //File
        if (success) {
            String key = "Permission." + permission;

            List<String> emojis = config.getStringList(key);

            emojis.remove(String.valueOf(emoji));
            if (emojis.isEmpty()) {
                config.set(key, null);
            } else {
                config.set(key, emojis);
            }
            Customchatemoji.getInstance().saveConfig();
        }

        return success;
    }
}
