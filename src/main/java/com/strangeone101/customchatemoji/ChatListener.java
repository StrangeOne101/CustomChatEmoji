package com.strangeone101.customchatemoji;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatListener implements Listener {

    private static Map<Player, Set<OnChatRunnable>> chatRunnables = new HashMap<>();

    /**
     * This class is a class that lets us run code after they next say something in chat.
     * Usage is `new OnChatRunnable(player) { ... }.listen();`
     */
    public abstract static class OnChatRunnable implements Runnable {

        private String chatMessage;
        private String format;
        private Player player;
        private boolean cancelled;

        public OnChatRunnable(Player player) {
            this.player = player;
        }

        public abstract void run();

        /**
         * Make this runnable start listening for the next chat event
         */
        public final void listen() {
            if (!chatRunnables.containsKey(player)) {
                chatRunnables.put(player, new HashSet<OnChatRunnable>());
            }
            chatRunnables.get(player).add(this);
        }

        public String getChatMessage() {
            return chatMessage;
        }

        private void setChatMessage(String chatMessage) {
            this.chatMessage = chatMessage;
        }

        public String getFormat() {
            return format;
        }

        private void setFormat(String format) {
            this.format = format;
        }

        public Player getPlayer() {
            return player;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        //If there is a chat runnable for this player, try parse what they say
        //in it and possibly cancel the event
        if (chatRunnables.containsKey(event.getPlayer())) {
            List<OnChatRunnable> done = new ArrayList<>();
            for (OnChatRunnable runnable : chatRunnables.get(event.getPlayer())) {
                done.add(runnable);
                runnable.setChatMessage(event.getMessage());
                runnable.setFormat(event.getFormat());
                runnable.run();
                if (runnable.isCancelled()) {
                    event.setCancelled(true);
                    break;
                }
            }
            chatRunnables.get(event.getPlayer()).removeAll(done);
            if (chatRunnables.get(event.getPlayer()).size() == 0) {
                chatRunnables.remove(event.getPlayer());
            }
        }

        if (!event.isCancelled() && event.getPlayer().hasPermission("customchatemoji.use")) {
            //Record a hash so we can check later if it has been changed
            int hash = event.getMessage().hashCode();
            String msg = event.getMessage();
            //TODO: Change this to do it via groups they have perm for instead of every single alias
            for (String alias : CharacterLoader.getINSTANCE().getAliases().keySet()) {
                CharacterData data = CharacterLoader.getINSTANCE().getAliases().get(alias);
                String replacement = data.isCanColor() ? data.getCharacter().toString() : ChatColor.WHITE + data.getCharacter().toString() + ChatColor.RESET;
                msg = msg.replaceAll(alias, replacement);
            }
            if (msg.hashCode() != hash) event.setMessage(msg);
        }
    }
}