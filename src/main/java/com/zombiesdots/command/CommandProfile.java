package com.zombiesdots.command;

import com.zombiesdots.ZombiesDotsMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class CommandProfile extends CommandBase {

    @Override
    public String getCommandName() { return "profile"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/profile <name|off>"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(ZombiesDotsMod.profileManager.getProfiles());
            options.add("off");
            return getListOfStringsMatchingLastWord(args, options.toArray(new String[0]));
        }
        return new ArrayList<>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            String current = ZombiesDotsMod.profileManager.getActiveProfile();
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[ZDots] Active profile: " +
                    EnumChatFormatting.WHITE + current));
            return;
        }

        String name = args[0];

        if ("off".equalsIgnoreCase(name)) {
            ZombiesDotsMod.profileManager.setActiveProfile("off");
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GRAY + "[ZDots] All markers hidden. Profile set to 'off'."));
            return;
        }

        List<String> profiles = ZombiesDotsMod.profileManager.getProfiles();
        boolean exists = false;
        for (String p : profiles) {
            if (p.equalsIgnoreCase(name)) {
                name = p; // preserve original case
                exists = true;
                break;
            }
        }
        if (!exists) {
            ZombiesDotsMod.profileManager.createProfile(name);
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GREEN + "[ZDots] Created new profile '" + name + "'."));
        }

        ZombiesDotsMod.profileManager.setActiveProfile(name);
        int count = ZombiesDotsMod.profileManager.getMarkerCount(name);
        sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GREEN + "[ZDots] Switched to profile '" + name +
                "' (" + count + " marker" + (count == 1 ? "" : "s") + ")."));
    }
}
