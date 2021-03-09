package com.strangeone101.customchatemoji;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatTokenizer {
    private static final boolean LOG_DEBUG = true;

    public static ParseResults parse(String message, Player player) {
        List<ChatToken> chatTokens = new ArrayList<>();
        boolean hasEmoji = false;

        HashMap<Character, ConfigManager.EmojiEntry> emojiEntries = ConfigManager.getEmojiEntries();
        HashMap<String, Character> emojiNames = ConfigManager.getEmojiNames();
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

                    if (EmojiUtil.isPermitted(c, player)) {
                        //Add this as an emoji token
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Pushing 0x" + Integer.toHexString(c));
                        chatTokens.add(new ChatToken(index, index + 1, c));
                        hasEmoji = true;
                    } else {
                        //Escape emoji. This will be actually done in transform().
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Pushing escaped emoji 0x" + Integer.toHexString(c));
                        chatTokens.add(new ChatToken(ChatToken.ESCAPE_EMOJI, ChatToken.ESCAPE_EMOJI, c));
                    }
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
                    String emojiName = message.substring(emojiTagBegin + 1, index);
                    if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode] Checking [" + emojiName + "]");
                    Character emoji = emojiNames.get(emojiName);
                    if (emoji != null && EmojiUtil.isPermitted(emoji, player)) {
                        //End token as emoji
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing 0x" + Integer.toHexString(emoji) + " (from " + emojiName + ")");
                        chatTokens.add(new ChatToken(emojiTagBegin, index + 1, emoji));
                        hasEmoji = true;
                        emojiTagBegin = -1;
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode] => [String mode]");
                    } else {
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

        public static final int ESCAPE_EMOJI = -1;
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
            } else if (token.indexBegin == ChatToken.ESCAPE_EMOJI) {
                char emojiTag = ConfigManager.getEmojiTag();
                stringBuilder.append(emojiTag);
                stringBuilder.append(EmojiUtil.toEmojiName(token.emoji));
                stringBuilder.append(emojiTag);
            } else {
                stringBuilder.append(token.emoji);
            }
        }

        return stringBuilder.toString();
    }
}
