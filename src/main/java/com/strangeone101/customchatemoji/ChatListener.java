package com.strangeone101.customchatemoji;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        if (ConfigManager.getEmojiTag() == '\0') return;

        String message = event.getMessage();

        ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(message);
        if (!parseResults.hasEmoji) return;

        message = ChatTokenizer.transform(message, parseResults.chatTokens, event.getPlayer());
        event.setMessage(message);
    }
}
