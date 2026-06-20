package com.zombiesdots.command;

import com.zombiesdots.TargetingHelper;
import com.zombiesdots.ZombiesDotsMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;

import java.util.Collections;
import java.util.List;

public class CommandRemoveDot extends CommandBase {

    @Override
    public String getCommandName() { return "removedot"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/removedot"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (ZombiesDotsMod.profileManager.isOff()) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[ZDots] Active profile is 'off'."));
            return;
        }

        MovingObjectPosition mop = TargetingHelper.traceFromPlayer();

        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[ZDots] No block in crosshair within 200 blocks."));
            return;
        }

        BlockPos bp = mop.getBlockPos();
        boolean removed = ZombiesDotsMod.profileManager.removeClosestMarkerAt(
                bp.getX(), bp.getY(), bp.getZ(),
                mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);

        if (removed) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GREEN + "[ZDots] Marker removed at " +
                    bp.getX() + ", " + bp.getY() + ", " + bp.getZ() + "."));
        } else {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.YELLOW + "[ZDots] No marker at " +
                    bp.getX() + ", " + bp.getY() + ", " + bp.getZ() + "."));
        }
    }
}
