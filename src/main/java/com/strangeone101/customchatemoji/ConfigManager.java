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
    private static HashMap<String, LinkedHashSet<EmojiEntry>> groupEntries;

    private static boolean resourcePack = false;
    private static String resourcePackURL;
    private static boolean resourcePackDirect;

    private static String version;

    private static final boolean LOG_DEBUG = false;

    public static boolean setup() {
        Customchatemoji.getInstance().saveDefaultConfig();
        config = Customchatemoji.getInstance().getConfig();

        String emojiTagString = config.getString("EmojiTag");
        if (LOG_DEBUG) Bukkit.getLogger().info("EmojiTag = [" + (emojiTagString != null ? emojiTagString : "null") + "]");
        if (emojiTagString != null) {
            emojiTag = emojiTagString.charAt(0);
            Bukkit.getLogger().info("emojiTag = [" + emojiTag + "]");
        }

        resourcePack = config.getBoolean("ResourcePack.Enabled", false);
        resourcePackURL = config.getString("ResourcePack.URL", "");
        resourcePackDirect = config.getString("ResourcePack.Method", "direct").equalsIgnoreCase("direct");

        version = config.getString("Version", "??");

        emojiNames = new HashMap<>();
        emojiEntries = new HashMap<>();
        groupEntries = new HashMap<>();

        ConfigurationSection emojiSection = config.getConfigurationSection("Emoji");
        if (LOG_DEBUG) Bukkit.getLogger().info("Emoji = [" + (emojiSection != null ? emojiSection.getName() : "null") + "]");
        if (emojiSection == null) return false;

        Map<String, Object> emojiMappings = emojiSection.getValues(false);
        for (Map.Entry<String, Object> emojiMapping : emojiMappings.entrySet()) {
            String emojiHexString = emojiMapping.getKey();
            char emojiUnicode = EmojiUtil.fromHexString(emojiHexString);
            if (emojiUnicode == '\0') {
                Bukkit.getLogger().warning("Emoji [" + emojiHexString + "] is invalid");
                continue;
            }
            String emojiName = emojiMapping.getValue().toString();
            if (LOG_DEBUG) Bukkit.getLogger().info(Integer.toHexString(emojiUnicode) + " = [" + emojiName + "]");

            emojiNames.put(emojiName, emojiUnicode);
            emojiEntries.put(emojiUnicode, new EmojiEntry(emojiName));
        }

        ConfigurationSection groupSection = config.getConfigurationSection("Groups");
        if (LOG_DEBUG) Bukkit.getLogger().info("Group = [" + (groupSection != null ? groupSection.getName() : "null") + "]");
        if (groupSection == null) return false;

        for (String group : groupSection.getKeys(false)) {
            if (LOG_DEBUG) Bukkit.getLogger().info("group = [" + group + "]");
            if (groupSection.isList(group)) {
                for (String emojiHex : groupSection.getStringList(group)) {
                    addEmojiGroup(emojiHex, group);
                }
            } else {
                String emojiHex = groupSection.getString(group);
                addEmojiGroup(emojiHex, group);
            }


        }
        return true;
    }

    private static void addEmojiGroup(String emojiHex, String group) {
        if (LOG_DEBUG) Bukkit.getLogger().info(group + " = [" + emojiHex + "]");

        if (emojiHex.contains(":")) {
            EmojiRange emojiRange = EmojiRange.fromHexStringRange(emojiHex);
            if (emojiRange == null) {
                Bukkit.getLogger().warning("Invalid emoji(s): " + emojiHex);
                return;
            }

            for (char emoji = emojiRange.emojiLow; emoji <= emojiRange.emojiHigh; emoji++) {
                addEmojiGroup(emoji, group);
            }
        } else {
            char emoji = EmojiUtil.fromHexString(emojiHex);
            if (emoji == '\0') {
                Bukkit.getLogger().warning("Invalid emoji(s): " + emojiHex);
                return;
            }
            addEmojiGroup(emoji, group);
        }
    }

    private static void addEmojiGroup(char emoji, String group) {
        group = group.toLowerCase();
        if (LOG_DEBUG) Bukkit.getLogger().info("Adding 0x" + Integer.toHexString(emoji) + " to group " + group);

        EmojiEntry entry = emojiEntries.get(emoji);
        if (entry == null) {
            Bukkit.getLogger().warning("Emoji 0x" + Integer.toHexString(emoji) + " does not exist. Ignored.");
        } else {
            if (LOG_DEBUG) Bukkit.getLogger().info(Integer.toHexString(emoji) + " added for " + group);
            entry.getGroups().add(group);

            if (!groupEntries.containsKey(group)) {
                groupEntries.put(group, new LinkedHashSet<>());
            }

            groupEntries.get(group).add(entry);
        }
    }

    public static boolean isResourcePackEnabled() {
        return resourcePack;
    }

    public static String getResourcePackURL() {
        return resourcePackURL;
    }

    public static boolean isResourcePackDirect() {
        return resourcePackDirect;
    }

    public static String getVersion() {
        return version;
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

    public static HashMap<String, LinkedHashSet<EmojiEntry>> getGroupEntries() {
        return groupEntries;
    }
}
