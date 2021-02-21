package com.strangeone101.customchatemoji;

import javafx.util.Pair;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private static FileConfiguration config;

    public static boolean setup() {
        File file = new File(Customchatemoji.getInstance().getDataFolder(), "config.yml");
        if (!file.exists()) {
            if (!Util.saveResource("config.yml", file)) {
                Customchatemoji.getInstance().getLogger().severe("Failed to copy default config!");
                return false;
            }
        }

        config = new YamlConfiguration();
        try {
            config.load(file);

            loadConfig();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return false;

    }

    public static void loadConfig() {
        List<String> l = config.getStringList("CharacterRanges");
        Set<Pair<Character, Character>> set = new HashSet<>();

        for (String s : l) {
            if (s.split(":").length == 1) {
                Customchatemoji.getInstance().getLogger().warning("Character range \"" + s + "\" in config did not include a colon character split!");
                continue;
            }
            String[] parts = s.split(":");
            String part1 = parts[0].replace("\\u", "").replace("0x", "");
            String part2 = parts[0].replace("\\u", "").replace("0x", "");
            try {
                int char1 = Integer.parseInt(part1, 16);
                int char2 = Integer.parseInt(part2, 16);

                Pair<Character, Character> pair = new Pair<Character, Character>(Character.forDigit(char1, 10), Character.forDigit(char2, 10));
                set.add(pair);
            } catch (NumberFormatException e) {
                Customchatemoji.getInstance().getLogger().warning("Character range \"" + s + "\" cannot be parsed! Are you sure they are in hexadecimal in the right format?");
                continue;
            }
        }

        CharacterLoader.getINSTANCE().setCharacterRange(set);
    }
}
