package com.strangeone101.customchatemoji;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permissible;

import java.util.HashSet;
import java.util.Set;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        String message = ChatTokenizer.apply(event.getMessage(), event.getPlayer());
        if (message != null) {
            event.setMessage(message);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        String message = ChatTokenizer.apply(event.getMessage(), event.getPlayer());
        if (message != null) {
            event.setMessage(message);
        }
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        if (event.isCancelled()) return;
        String message = ChatTokenizer.apply(event.getCommand(), event.getSender());
        if (message != null) {
            event.setCommand(message);
        }
    }

    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (ConfigManager.getEmojiTag() == '\0') return;

        if (event.getClickedInventory() instanceof AnvilInventory) {
            if (event.getSlot() == 2) {
                ItemStack stack = event.getClickedInventory().getItem(2);
                if (stack.hasItemMeta()) {
                    ItemMeta meta = stack.getItemMeta();
                    String name = ChatTokenizer.apply(meta.getDisplayName(), event.getWhoClicked(), ChatColor.ITALIC);
                    if (name == null) return;

                    meta.setDisplayName(name);
                    stack.setItemMeta(meta);
                    event.setCurrentItem(stack);
                }

            }
        }
    }
}
