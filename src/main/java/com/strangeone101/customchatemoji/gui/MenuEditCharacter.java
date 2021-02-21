package com.strangeone101.customchatemoji.gui;

import com.strangeone101.customchatemoji.CharacterData;

public class MenuEditCharacter extends MenuBase {

    private CharacterData data;

    public MenuEditCharacter(CharacterData data) {
        super("Edit Character", 3);

        this.data = data;
    }


    public CharacterData getData() {
        return data;
    }
}
