package com.strangeone101.customchatemoji;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
}
