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

public class CommandProfiles extends CommandBase {

    @Override
    public String getCommandName() { return "profiles"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/profiles"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        List<String> profiles = ZombiesDotsMod.profileManager.getProfiles();
        String active = ZombiesDotsMod.profileManager.getActiveProfile();

        sender.addChatMessage(new ChatComponentText(
                EnumChatFormatting.YELLOW + "--- ZombiesDots Profiles ---"));

        for (String p : profiles) {
            int count = ZombiesDotsMod.profileManager.getMarkerCount(p);
            boolean isActive = p.equals(active);
            String prefix = isActive
                    ? EnumChatFormatting.GREEN + "> "
                    : EnumChatFormatting.GRAY + "  ";
            sender.addChatMessage(new ChatComponentText(
                    prefix + EnumChatFormatting.WHITE + p +
                    EnumChatFormatting.DARK_GRAY + " [" + count + " marker" + (count == 1 ? "" : "s") + "]"));
        }

        boolean offActive = "off".equalsIgnoreCase(active);
        String offPrefix = offActive ? EnumChatFormatting.GREEN + "> " : EnumChatFormatting.GRAY + "  ";
        sender.addChatMessage(new ChatComponentText(
                offPrefix + EnumChatFormatting.DARK_GRAY + "off" +
                EnumChatFormatting.DARK_GRAY + " [markers hidden]"));
    }
}
