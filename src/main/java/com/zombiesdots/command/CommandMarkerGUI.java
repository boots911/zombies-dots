package com.zombiesdots.command;

import com.zombiesdots.ZombiesDotsMod;
import com.zombiesdots.gui.GuiMarkerManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandMarkerGUI extends CommandBase {

    @Override
    public String getCommandName() { return "dotgui"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/dotgui"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        ZombiesDotsMod.pendingGui = new GuiMarkerManager();
    }
}
