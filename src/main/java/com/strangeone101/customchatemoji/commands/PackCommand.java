package com.strangeone101.customchatemoji.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public class PackCommand extends BaseCommand {

    public PackCommand(String name, String description, String... aliases) {
        super("pack", "Generate files needed for the resource pack", "packgen", "resourcepack", "texturepack");
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        //TODO
        //List usages
        /* /cce pack create - start a new pack
           /cce pack description [description] - Set the description
           /cce pack addrange [start] [end] - Adds a supported range of characters
           /cce pack adddefaults - Adds the default range texture files
           /cce pack hd [file] - Adds HD support for that texture
           /cee pack texture [file] [texture/generate] - Set the texture for a file or generate a new one


         */
    }
}
