package com.zombiesdots.command;

import com.zombiesdots.ZombiesDotsMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class CommandDotColor extends CommandBase {

    @Override
    public String getCommandName() { return "dotcolor"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/dotcolor <RRGGBB>"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            int argb = ZombiesDotsMod.profileManager.getCustomARGB();
            String hex = String.format("%02X%02X%02X",
                    (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[ZDots] Current custom color: #" + hex
                    + "  Usage: /dotcolor <RRGGBB>"));
            return;
        }

        String input = args[0].startsWith("#") ? args[0].substring(1) : args[0];
        if (input.length() != 6) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[ZDots] Invalid hex — use 6 hex digits, e.g. /dotcolor FF8800"));
            return;
        }

        try {
            int r = Integer.parseInt(input.substring(0, 2), 16);
            int g = Integer.parseInt(input.substring(2, 4), 16);
            int b = Integer.parseInt(input.substring(4, 6), 16);
            ZombiesDotsMod.profileManager.setCustomColor(r, g, b);
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GREEN + "[ZDots] Custom color set to #" + input.toUpperCase()));
        } catch (NumberFormatException e) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[ZDots] Invalid hex — use 6 hex digits, e.g. /dotcolor FF8800"));
        }
    }
}
