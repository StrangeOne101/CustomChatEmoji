package com.strangeone101.customchatemoji;

public class EmojiRange {
    public EmojiRange (char emojiLow, char emojiHigh) {
        this.emojiLow = emojiLow;
        this.emojiHigh = emojiHigh;
    }
    public char emojiLow;
    public char emojiHigh;

    public static EmojiRange fromHexStringRange (String hexStringRange) {
        try {
            String[] ranges = hexStringRange.split(":", 2);

            char emojiLow = EmojiUtil.fromHexString(ranges[0]);
            if (emojiLow == '\0') return null;
            char emojiHigh = EmojiUtil.fromHexString(ranges[1]);
            if (emojiHigh == '\0') return null;

            if (emojiLow > emojiHigh) {
                char aux = emojiLow;
                emojiLow = emojiHigh;
                emojiHigh = aux;
            }

            return new EmojiRange(emojiLow, emojiHigh);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
