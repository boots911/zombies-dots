package com.zombiesdots.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class CommandDotHelp extends CommandBase {

    @Override
    public String getCommandName() { return "dothelp"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/dothelp"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String Y = EnumChatFormatting.YELLOW.toString();
        String G = EnumChatFormatting.GREEN.toString();
        String W = EnumChatFormatting.WHITE.toString();
        String D = EnumChatFormatting.DARK_GRAY.toString();

        sender.addChatMessage(new ChatComponentText(Y + "--- ZombiesDots Commands ---"));
        cmd(sender, G, W, D, "/adddot",             "Aim at a block and place a dot marker");
        cmd(sender, G, W, D, "/removedot",           "Remove the dot closest to your crosshair on the aimed block");
        cmd(sender, G, W, D, "/dotgui",              "Open the profile manager");
        cmd(sender, G, W, D, "/profile <name|off>",  "Switch active profile, or use 'off' to hide all dots");
        cmd(sender, G, W, D, "/profiles",            "List all profiles with marker counts");
        cmd(sender, G, W, D, "/dothelp",             "Show this help message");
    }

    private static void cmd(ICommandSender s, String cG, String cW, String cD,
                            String name, String desc) {
        s.addChatMessage(new ChatComponentText(
                cG + name + cD + " - " + cW + desc));
    }
}
