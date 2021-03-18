package com.strangeone101.customchatemoji;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (ConfigManager.getEmojiTag() == '\0') return;

        String message = event.getMessage();
        Player player = event.getPlayer();

        ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(message, player);
        if (!parseResults.needsTransform) return;

        message = ChatTokenizer.transform(message, parseResults.chatTokens);
        event.setMessage(message);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        if (ConfigManager.getEmojiTag() == '\0') return;

        String message = event.getMessage();
        Player player = event.getPlayer();

        ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(message, player);
        if (!parseResults.needsTransform) return;

        message = ChatTokenizer.transform(message, parseResults.chatTokens);
        event.setMessage(message);
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
                    String name = meta.getDisplayName();
                    ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(name, event.getWhoClicked());
                    if (!parseResults.needsTransform) return;

                    name = ChatTokenizer.transform(name, parseResults.chatTokens);
                    meta.setDisplayName(name);
                    stack.setItemMeta(meta);
                    event.setCurrentItem(stack);
                }

            }
        }
    }
}
