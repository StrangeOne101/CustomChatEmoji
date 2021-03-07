package com.strangeone101.customchatemoji;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatTokenizer {
    private static final boolean LOG_DEBUG = true;

    public static ParseResults parse(String message, Player player) {
        List<ChatToken> chatTokens = new ArrayList<>();
        boolean hasEmoji = false;

        HashMap<Character, ConfigManager.EmojiEntry> emojiEntries = ConfigManager.getEmojiEntries();
        char emojiTag = ConfigManager.getEmojiTag();

        int stringBegin = -1;
        int emojiTagBegin = -1;
        for (int index = 0; index < message.length(); index++) {
            char c = message.charAt(index);

            if (emojiTagBegin == -1) {
                // Not in emoji tag mode
                if (emojiEntries.containsKey(c)) {
                    //Emoji found directly as unicode (e.g. user pasted directly)
                    if (stringBegin >= 0) {
                        //End previous token
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Found direct unicode. Pushing previous [" + message.substring(stringBegin, index) + "]");
                        chatTokens.add(new ChatToken(stringBegin, index));
                        stringBegin = -1;
                    }
                    //Add this as an emoji token
                    if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Pushing 0x" + Integer.toHexString(c));
                    chatTokens.add(new ChatToken(index, index + 1, c));
                    hasEmoji = true;
                } else if (c == emojiTag) {
                    //In emoji tag mode
                    if (stringBegin >= 0) {
                        //End previous token
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Found " + emojiTag + ". Pushing previous [" + message.substring(stringBegin, index) + "]");
                        chatTokens.add(new ChatToken(stringBegin, index));
                        stringBegin = -1;
                    }
                    //Start possible emoji token
                    if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] => [Emoji mode]");
                    emojiTagBegin = index;
                } else {
                    //Regular character
                    if (stringBegin == -1) {
                        //Start token
                        stringBegin = index;
                    }
                }
            } else {
                //In emoji tag mode
                if (emojiEntries.containsKey(c)) {
                    //Emoji found directly as unicode (e.g. user pasted directly)
                    //Break emoji tag mode
                    //End token as string (not emoji)
                    if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Found direct unicode. Pushing previous [" + message.substring(emojiTagBegin, index) + "]");
                    chatTokens.add(new ChatToken(emojiTagBegin, index));
                    //Add this as an emoji token
                    if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing 0x" + Integer.toHexString(c));
                    chatTokens.add(new ChatToken(index, index + 1, c));
                    hasEmoji = true;
                    emojiTagBegin = -1;
                } else if (c == emojiTag) {
                    //Closing emoji tag found
                    boolean isEmoji = false;
                    for (Map.Entry<Character, ConfigManager.EmojiEntry> emojiEntry : ConfigManager.getEmojiEntries().entrySet()) {
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode] Checking [" + message.substring(emojiTagBegin + 1, index) + "] x [" + emojiEntry.getValue().getName() + "]");
                        Character emoji = emojiEntry.getKey();
                        String emojiName = emojiEntry.getValue().getName();

                        if (ConfigManager.emojiAllowed(player, emoji) && emojiName.regionMatches(0, message, emojiTagBegin + 1, emojiName.length())) {
                            isEmoji = true;
                            //End token as emoji
                            if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing 0x" + Integer.toHexString(emoji) + " (from " + emojiName + ")");
                            chatTokens.add(new ChatToken(emojiTagBegin, index + 1, emoji));
                            hasEmoji = true;
                            emojiTagBegin = -1;
                            if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode] => [String mode]");
                            break;
                        }
                    }
                    if (!isEmoji) {
                        //End token as string (not emoji)
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Not an emoji. Pushing [" + message.substring(emojiTagBegin, index) + "]");
                        chatTokens.add(new ChatToken(emojiTagBegin, index));
                        //Set this as possible new emoji beginning
                        emojiTagBegin = index;
                    }
                }
            }
        }

        //Left over
        if (stringBegin >= 0) {
            //End last token
            if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Pushing leftover [" + message.substring(stringBegin) + "]");
            chatTokens.add(new ChatToken(stringBegin, message.length()));
        } else if (emojiTagBegin >= 0) {
            //End last token as string (not emoji)
            if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing leftover [" + message.substring(emojiTagBegin) + "]");
            chatTokens.add(new ChatToken(emojiTagBegin, message.length()));
        }

        return new ParseResults(chatTokens, hasEmoji);
    }

    public static class ChatToken {
        public ChatToken(int indexBegin, int indexEnd, char emoji) {
            this.indexBegin = indexBegin;
            this.indexEnd = indexEnd;
            this.emoji = emoji;
        }
        public ChatToken(int indexBegin, int indexEnd) {
            this(indexBegin, indexEnd, '\0');
        }

        public int indexBegin;
        public int indexEnd;
        public char emoji;
    }

    public static class ParseResults {
        private ParseResults(List<ChatToken> chatTokens, boolean hasEmoji) {
            this.chatTokens = chatTokens;
            this.hasEmoji = true;
        }
        public final List<ChatToken> chatTokens;
        public final boolean hasEmoji;
    }

    public static String transform(String message, List<ChatToken> chatTokens) {
        StringBuilder stringBuilder = new StringBuilder();

        for (ChatTokenizer.ChatToken token : chatTokens) {
            if (token.emoji == '\0') {
                stringBuilder.append(message, token.indexBegin, token.indexEnd);
            } else {
                stringBuilder.append(token.emoji);
            }
        }

        return stringBuilder.toString();
    }
}
