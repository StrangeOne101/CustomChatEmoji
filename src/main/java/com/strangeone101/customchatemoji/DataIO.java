package com.strangeone101.customchatemoji;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataIO {

    private static final char[] ALIAS_SPLITTERS = {';', ',', ':', '|', '/', '\\', '_', '>'};

    public static List<CharacterData> loadData(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() == 0) return null;
            if (!Util.isInteger(lines.get(0))) return null;

            int version = Integer.parseInt(lines.get(0));

            List<CharacterData> characterDataList = new ArrayList<>();

            if (version == 0) {
                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);

                    String chAr = line.split("=", 2)[0];
                    String[] parts = line.split("=",2)[1].split(",", 3);

                    int charInt = Integer.parseInt(chAr, 16);
                    Character character = Character.forDigit(charInt, 10);

                    byte bools = Byte.parseByte(parts[0]);
                    boolean canColor = (bools & 1) == 1;
                    boolean caseSensitive = (bools >>> 1 & 1) == 1;

                    int uses = Integer.parseInt(parts[1]);
                    String[] aliases = new String[0];

                    String aliasesPart = parts[2];
                    if (aliasesPart.length() > 0) {
                        char splitter = aliasesPart.charAt(0);

                        aliases = aliasesPart.substring(1).split(splitter + "");


                    }
                    characterDataList.add(new CharacterData(character, canColor, caseSensitive, uses, aliases));
                }
            }

            return characterDataList;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveData(File file) {
        List<String> lines = new ArrayList<String>();
        lines.add("0");

        CharacterData[] data = {}; //TODO

        for (CharacterData d : data) {
            String l = "";

            l = Integer.toHexString(Character.getNumericValue(d.getCharacter())) + "=";

            int bools = 0;
            bools += d.isCanColor() ? 1 : 0;
            bools += d.isCaseSensitive() ? 2 : 0;

            l = l + bools + ",";
            l = l + d.getUses() + ",";

            char aliasSplit = '\u00a7';
            List<String> aliases = Arrays.asList(d.getAliases());

            outer: for (int i = 0; i < ALIAS_SPLITTERS.length; i++) {
                for (String s : aliases) {
                    if (s.contains(ALIAS_SPLITTERS[i] + "")) { //If the alias contains the splitter we were going to use
                        continue outer; //Go to the next alias splitter
                    }
                }
                aliasSplit = ALIAS_SPLITTERS[i];
                break; //If the loop hasn't broken by this point, it means we have found an unused splitter
            }

            String joinedAliases = String.join(aliasSplit + "", aliases);
            l = l + aliasSplit + joinedAliases;

            lines.add(l);
        }

        try {
            Files.write(file.toPath(), lines, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }
}
