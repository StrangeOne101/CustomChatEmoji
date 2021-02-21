package com.strangeone101.customchatemoji;

import com.strangeone101.customchatemoji.commands.CCECommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Customchatemoji extends JavaPlugin {

    private static Customchatemoji INSTANCE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        INSTANCE = this;

        new CharacterLoader();

        ConfigManager.setup();

        CCECommand cmd = new CCECommand();

        getCommand("cce").setExecutor(cmd);

        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static Customchatemoji getInstance() {
        return INSTANCE;
    }
}
