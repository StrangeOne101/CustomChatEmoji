package com.strangeone101.customchatemoji;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {
    private static FileConfiguration config;
    private static char emojiTag;
    private static HashMap<String, Character> emojiNames;
    private static HashMap<Character, EmojiEntry> emojiEntries;
    private static HashMap<String, Set<EmojiEntry>> groupEntries;

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

        emojiNames = new HashMap<>();
        emojiEntries = new HashMap<>();
        groupEntries = new HashMap<>();

        ConfigurationSection emojiSection = config.getConfigurationSection("Emoji");
        if (LOG_DEBUG) Bukkit.getLogger().info("Emoji = [" + (emojiSection != null ? emojiSection.getName() : "null") + "]");
        if (emojiSection == null) return false;

        Map<String, Object> emojiMappings = emojiSection.getValues(false);
        for (Map.Entry<String, Object> emojiMapping : emojiMappings.entrySet()) {
            char emojiUnicode = emojiMapping.getKey().charAt(0);
            String emojiName = emojiMapping.getValue().toString();
            Bukkit.getLogger().info(Integer.toHexString(emojiUnicode) + " = [" + emojiName + "]");

            emojiNames.put(emojiName, emojiUnicode);
            emojiEntries.put(emojiUnicode, new EmojiEntry(emojiName));
        }

        ConfigurationSection groupSection = config.getConfigurationSection("Groups");
        if (LOG_DEBUG) Bukkit.getLogger().info("Group = [" + (groupSection != null ? groupSection.getName() : "null") + "]");
        if (groupSection == null) return false;

        for (String group : groupSection.getKeys(false)) {
            if (LOG_DEBUG) Bukkit.getLogger().info("group = [" + group + "]");
            if (groupSection.isList(group)) {
                for (String emoji : groupSection.getStringList(group)) {
                    addEmojiGroup(emoji, group);
                }
            } else {
                addEmojiGroup(groupSection.getString(group), group);
            }


        }
        return true;
    }

    private static void addEmojiGroup(String emoji, String group) {
        if (LOG_DEBUG) Bukkit.getLogger().info(group + " = [" + emoji + "]");

        if (emoji.length() != 1) {
            Bukkit.getLogger().warning("Invalid unicode: " + emoji);
            return;
        }

        Set<Character> chars = new HashSet<>();

        if (emoji.contains(":")) {
            String split1 = emoji.split(":", 2)[0].trim();
            String split2 = emoji.split(":", 2)[0].trim();

            char a = split1.charAt(0);
            char b = split2.charAt(0);

            if (a > b) {
                char temp = a;
                a = b;
                b = temp;
            }

            for (char c = a; a <= b; a++) {
                chars.add(c);
            }
        } else {
            chars.add(emoji.charAt(0));
        }

        for (Character charr : chars) {
            char unicode = charr.charValue();
            EmojiEntry entry = emojiEntries.get(unicode);
            if (entry != null) {
                Bukkit.getLogger().info(Integer.toHexString(unicode) + " added for " + group);
                entry.getGroups().add(group.toLowerCase());

                if (!groupEntries.containsKey(group.toLowerCase())) {
                    groupEntries.put(group.toLowerCase(), new HashSet<>());
                }

                groupEntries.get(group.toLowerCase()).add(entry);
            }
        }


    }

    public static class EmojiEntry {
        public EmojiEntry(String name, Set<String> groups) {
            this.name = name;
            this.groups = groups;
        }
        public EmojiEntry(String name) {
            this(name, new HashSet<>());
        }
        public String getName() { return this.name; }

        public Set<String> getGroups() { return this.groups; }

        private final String name;
        private final Set<String> groups;
    }

    public static char getEmojiTag() {
        return emojiTag;
    }

    public static HashMap<String, Character> getEmojiNames() {
        return emojiNames;
    }
    public static HashMap<Character, EmojiEntry> getEmojiEntries() {
        return emojiEntries;
    }

    public static HashMap<String, Set<EmojiEntry>> getGroupEntries() {
        return groupEntries;
    }

    @Deprecated
    public static void addPermission(char emoji, String permission) {
        //Memory
        ConfigManager.getEmojiEntries().get(emoji).getGroups().add(permission);

        //File
        String key = "Permission." + permission;

        List<String> emojis = config.getStringList(key);
        if (!emojis.contains(emoji)) {
            emojis.add(String.valueOf(emoji));
            config.set(key, emojis);
            Customchatemoji.getInstance().saveConfig();
        }
    }

    @Deprecated
    public static boolean delPermission(char emoji, String permission) {
        boolean success;
        //Memory
        success = ConfigManager.getEmojiEntries().get(emoji).getGroups().remove(permission);

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
