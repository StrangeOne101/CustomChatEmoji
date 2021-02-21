package com.strangeone101.customchatemoji;

import javafx.util.Pair;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CharacterLoader {

    private static CharacterLoader INSTANCE;

    private boolean dataExists;
    private Map<String, CharacterData> aliases;
    private Set<Pair<Character, Character>> CHARACTER_RANGES = new HashSet<Pair<Character, Character>>();

    public CharacterLoader() {
        INSTANCE = this;

        File file = new File(Customchatemoji.getInstance().getDataFolder(), "data.dat");
        List<CharacterData> data = null;
        dataExists = file.exists() && (data = DataIO.loadData(file)) != null;

        aliases = new HashMap<String, CharacterData>();
        if (data != null) {
            for (CharacterData cd : data) {
                for (String alias : cd.getAliases()) {
                    aliases.put(cd.isCaseSensitive() ? "" : "(?i)" + Pattern.quote(alias), cd);
                }
            }
        }

    }



    protected void setCharacterRange(Collection<Pair<Character, Character>> collection) {
        CHARACTER_RANGES.clear();
        CHARACTER_RANGES.addAll(collection);
    }

    public boolean dataExists() {
        return dataExists;
    }

    public Map<String, CharacterData> getAliases() {
        return aliases;
    }


    public static CharacterLoader getINSTANCE() {
        return INSTANCE;
    }

}
