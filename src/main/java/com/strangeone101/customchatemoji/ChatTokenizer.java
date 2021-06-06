package com.strangeone101.customchatemoji;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permissible;

import java.util.*;

public class ChatTokenizer {
    private static final boolean LOG_DEBUG = false;

    public static ParseResults parse(String message, Permissible permissible) {
        return parse(message, permissible, null, null);
    }
    public static ParseResults parse(String message, Permissible permissible, ChatColor... precedingColorOrFormats) {
        ChatColor precedingColor = null;
        Set<ChatColor> precedingFormats = new HashSet<>();
        for (ChatColor chatColorOrFormat : precedingColorOrFormats) {
            if (chatColorOrFormat.isColor()) {
                precedingColor = chatColorOrFormat;
                precedingFormats.clear(); //In Java Edition, chat colors reset preceding formats
            } else if (chatColorOrFormat.isFormat()) {
                precedingFormats.add(chatColorOrFormat);
            }
        }
        return parse(message, permissible, precedingColor, precedingFormats);
    }
    public static ParseResults parse(String message, Permissible permissible, ChatColor precedingColor, Set<ChatColor> precedingFormats) {
        List<ChatToken> chatTokens = new ArrayList<>();
        boolean needsTransform = false;

        HashMap<Character, ConfigManager.EmojiEntry> emojiEntries = ConfigManager.getEmojiEntries();
        HashMap<String, Character> emojiNames = ConfigManager.getEmojiNames();
        char emojiTag = ConfigManager.getEmojiTag();

        int stringBegin = -1;
        int emojiTagBegin = -1;
        if (precedingFormats == null) {
            precedingFormats = new HashSet<>();
        }
        for (int index = 0; index < message.length(); index++) {
            char c = message.charAt(index);

            if (LOG_DEBUG) Bukkit.getLogger().info("Char [" + Integer.toHexString(c) + "]");

            if (emojiTagBegin == -1) {
                // Not in emoji tag mode
                if (emojiEntries.containsKey(c)) {
                    //Emoji found directly as unicode (e.g. user pasted directly)
                    if (EmojiUtil.isPermitted(c, permissible)) {
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Found allowed unicode 0x" + Integer.toHexString(c));
                        if (precedingColor == null && precedingFormats.isEmpty()) {
                            //Handle as if it was any other character
                            if (stringBegin == -1) {
                                //Start token
                                stringBegin = index;
                            }
                        } else {
                            //End previous token
                            if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Pushing previous [" + message.substring(stringBegin, index) + "]");
                            chatTokens.add(new SubstringChatToken(stringBegin, index));
                            stringBegin = -1;

                            needsTransform = true;
                            //Add emoji with color/format codes
                            chatTokens.add(new EmojiChatToken(c, precedingColor, precedingFormats));
                        }
                    } else {
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Found disallowed unicode 0x" + Integer.toHexString(c));
                        if (stringBegin >= 0) {
                            //End previous token
                            if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Pushing previous [" + message.substring(stringBegin, index) + "]");
                            chatTokens.add(new SubstringChatToken(stringBegin, index));
                            stringBegin = -1;
                        }
                        needsTransform = true;
                        //Escape emoji. This will be actually done in transform().
                        chatTokens.add(new EscapedEmojiChatToken(c));
                    }
                } else if (c == emojiTag) {
                    //In emoji tag mode
                    if (stringBegin >= 0) {
                        //End previous token
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode]Found " + emojiTag + ". Pushing previous [" + message.substring(stringBegin, index) + "]");
                        chatTokens.add(new SubstringChatToken(stringBegin, index));
                        stringBegin = -1;
                    }
                    //Start possible emoji token
                    if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] => [Emoji mode]");
                    emojiTagBegin = index;
                } else {
                    if (c == ChatColor.COLOR_CHAR && index + 1 < message.length()) {
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] Found possible color/format code [" + message.charAt(index + 1) + "]");
                        //Color or format code. Do a look ahead and update color/format codes
                        precedingColor = updateColorAndFormats(message.charAt(index + 1), precedingColor, precedingFormats);
                        //Do not advance index in case of "§§" or other nonexistent color/format codes
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] Color is now " + (precedingColor == null ? "(none)" : precedingColor.name()));
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] There are now " + precedingFormats.size() + " formats");
                    }

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
                    chatTokens.add(new SubstringChatToken(emojiTagBegin, index));
                    //Add this as an emoji token
                    if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing 0x" + Integer.toHexString(c));
                    chatTokens.add(new EmojiChatToken(c, precedingColor, precedingFormats));
                    needsTransform = true;
                    emojiTagBegin = -1;
                } else if (c == ChatColor.COLOR_CHAR) {
                    //Color code found
                    //Break emoji tag mode (emoji names don't have § in them)
                    //End token as string (not emoji)
                    if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Found color/format code. Pushing previous [" + message.substring(emojiTagBegin, index) + "]");
                    chatTokens.add(new SubstringChatToken(emojiTagBegin, index));

                    if (index + 1 < message.length()) {
                        //Color or format code. Do a look ahead and update color/format codes
                        precedingColor = updateColorAndFormats(message.charAt(index + 1), precedingColor, precedingFormats);
                        //Do not advance index in case of "§§" or other nonexistent color/format codes
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] Color is now " + (precedingColor == null ? "(none)" : precedingColor.name()));
                        if (LOG_DEBUG) Bukkit.getLogger().info("[String mode] There are now " + precedingFormats.size() + " formats");
                    }

                    emojiTagBegin = -1;
                    //Start string token
                    stringBegin = index;
                } else if (c == emojiTag) {
                    //Closing emoji tag found
                    String emojiName = message.substring(emojiTagBegin + 1, index);
                    if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode] Checking [" + emojiName + "]");
                    Character emoji = emojiNames.get(emojiName.toLowerCase());
                    if (emoji != null && EmojiUtil.isPermitted(emoji, permissible)) {
                        //End token as emoji
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing 0x" + Integer.toHexString(emoji) + " (from " + emojiName + ")");
                        chatTokens.add(new EmojiChatToken(emoji, precedingColor, precedingFormats));
                        needsTransform = true;
                        emojiTagBegin = -1;
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode] => [String mode]");
                    } else {
                        //End token as string (not emoji)
                        if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Not an emoji. Pushing [" + message.substring(emojiTagBegin, index) + "]");
                        chatTokens.add(new SubstringChatToken(emojiTagBegin, index));
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
            chatTokens.add(new SubstringChatToken(stringBegin, message.length()));
        } else if (emojiTagBegin >= 0) {
            //End last token as string (not emoji)
            if (LOG_DEBUG) Bukkit.getLogger().info("[Emoji mode]Pushing leftover [" + message.substring(emojiTagBegin) + "]");
            chatTokens.add(new SubstringChatToken(emojiTagBegin, message.length()));
        }

        return new ParseResults(chatTokens, needsTransform);
    }

    private static ChatColor updateColorAndFormats(char code, ChatColor precedingColor, Set<ChatColor> precedingFormats) {
        // Design note: we don't create a new type of token because it's better to do a look-ahead,
        // as all color/format codes have a same exact length.
        // If lengths were variable, it would be better to create a special token.
        ChatColor chatColor = ChatColor.getByChar(code);
        if (chatColor != null) {
            if (LOG_DEBUG) Bukkit.getLogger().info("Found ChatColor " + chatColor.name());

            if (chatColor == ChatColor.RESET) {
                precedingColor = null;
                precedingFormats.clear();
            } else if (chatColor.isColor()) {
                precedingColor = chatColor;
                //In Java Edition, color codes reset formatting codes
                precedingFormats.clear();
            } else if (chatColor.isFormat()) {
                precedingFormats.add(chatColor);
            } else {
                Bukkit.getLogger().info("Unknown chat color/format found: " + code);
            }
        } else {
            if (LOG_DEBUG) Bukkit.getLogger().info("[" + code + "] is not a ChatColor code");
        }

        return precedingColor;
    }

    public interface ChatToken { }

    public static class SubstringChatToken implements ChatToken {
        public SubstringChatToken(int indexBegin, int indexEnd) {
            this.indexBegin = indexBegin;
            this.indexEnd = indexEnd;
        }
        public int indexBegin;
        public int indexEnd;
    }

    public static class EmojiChatToken implements ChatToken {
        public EmojiChatToken(char emoji, ChatColor precedingColor, Set<ChatColor> precedingFormats) {
            this.emoji = emoji;
            this.precedingColor = precedingColor;

            //need to serialize, because collections are references
            if (!precedingFormats.isEmpty()) {
                this.precedingFormats = new StringBuilder();
                for (ChatColor code : precedingFormats) {
                    this.precedingFormats.append(code.toString());
                }
            }

            if (LOG_DEBUG) {
                if (emoji != '\0') {
                    Bukkit.getLogger().info("New emoji ChatToken 0x" + Integer.toHexString(emoji) + " " +
                            (precedingColor == null ? "" : precedingColor.name()) + " color " +
                            (this.precedingFormats == null ? "" : this.precedingFormats));
                }
            }
        }

        public char emoji;
        public ChatColor precedingColor;
        public StringBuilder precedingFormats;
    }

    public static class EscapedEmojiChatToken implements ChatToken {
        public EscapedEmojiChatToken(char emoji) {
            this.emoji = emoji;
        }
        public char emoji;
    }

    public static class ParseResults {
        private ParseResults(List<ChatToken> chatTokens, boolean needsTransform) {
            this.chatTokens = chatTokens;
            this.needsTransform = needsTransform;
        }
        public final List<ChatToken> chatTokens;
        public final boolean needsTransform;
    }

    public static String transform(String message, List<ChatToken> chatTokens) {
        StringBuilder stringBuilder = new StringBuilder();

        transform(message, chatTokens, stringBuilder);

        return stringBuilder.toString();
    }

    public static void transform(String message, List<ChatToken> chatTokens, StringBuilder stringBuilder) {
        for (ChatTokenizer.ChatToken chatToken : chatTokens) {
            if (chatToken instanceof SubstringChatToken) {
                //Regular string - just copy it
                SubstringChatToken token = (SubstringChatToken)chatToken;
                stringBuilder.append(message, token.indexBegin, token.indexEnd);
            } else if (chatToken instanceof EscapedEmojiChatToken) {
                //Escape a direct emoji to its name (due to permissions). Ignore color and format codes.
                EscapedEmojiChatToken token = (EscapedEmojiChatToken)chatToken;
                char emojiTag = ConfigManager.getEmojiTag();
                stringBuilder.append(emojiTag);
                stringBuilder.append(EmojiUtil.toEmojiName(token.emoji));
                stringBuilder.append(emojiTag);
            } else if (chatToken instanceof EmojiChatToken) {
                //Reset before emoji if needed
                EmojiChatToken token = (EmojiChatToken)chatToken;

                if (LOG_DEBUG) Bukkit.getLogger().info("Emoji 0x" + Integer.toHexString(token.emoji) + " has preceding color " +
                        ((token.precedingColor == null) ? "(none)" : token.precedingColor.name()) + " " +
                        ((token.precedingFormats == null) ? "(no formats)" : token.precedingFormats.toString()));

                if (token.precedingColor != null || token.precedingFormats != null) {
                    if (LOG_DEBUG) Bukkit.getLogger().info("Resetting emoji with " + ChatColor.RESET.name());
                    stringBuilder.append(ChatColor.RESET.toString());
                }
                //Copy emoji
                stringBuilder.append(token.emoji);
                //Restore color
                if (token.precedingColor != null) {
                    if (LOG_DEBUG) Bukkit.getLogger().info("Restoring color with " + token.precedingColor.name());
                    stringBuilder.append(token.precedingColor.toString());
                }
                //Restore format
                if (token.precedingFormats != null) {
                    if (LOG_DEBUG) Bukkit.getLogger().info("Restoring format with " + token.precedingFormats.toString());
                    stringBuilder.append(token.precedingFormats);
                }
            }
        }
    }

    public static String apply (String message, Permissible permissible) {
        if (ConfigManager.getEmojiTag() == '\0') return null;

        ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(message, permissible);
        if (!parseResults.needsTransform) return null;

        return ChatTokenizer.transform(message, parseResults.chatTokens);
    }
    public static String apply (String message, Permissible permissible, ChatColor... precedingColorOrFormats) {
        if (ConfigManager.getEmojiTag() == '\0') return null;

        ChatTokenizer.ParseResults parseResults = ChatTokenizer.parse(message, permissible, precedingColorOrFormats);
        if (!parseResults.needsTransform) return null;

        return ChatTokenizer.transform(message, parseResults.chatTokens);
    }
}
