package com.strangeone101.customchatemoji;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CharacterData {

    private static Map<Character, CharacterData> mappedCharacters = new HashMap<>();

    private Character character;
    private boolean canColor = false;
    private boolean caseSensitive = false;
    private String[] aliases = new String[0];
    private int uses = 0;

    public CharacterData(Character character, boolean canColor, boolean caseSensitive, int uses, String... aliases) {
        this.character = character;
        this.canColor = canColor;
        this.caseSensitive = caseSensitive;
        this.aliases = aliases;
        this.uses = uses;

        mappedCharacters.put(character, this);
    }


    public Character getCharacter() {
        return character;
    }

    public boolean isCanColor() {
        return canColor;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public String[] getAliases() {
        return aliases;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public static CharacterData getFromCharacter(Character character) {
        return mappedCharacters.get(character);
    }

    public static Set<Character> getCharacters() {
        return mappedCharacters.keySet();
    }
}
