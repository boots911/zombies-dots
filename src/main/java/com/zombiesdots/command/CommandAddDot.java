package com.zombiesdots.command;

import com.zombiesdots.MarkerData;
import com.zombiesdots.TargetingHelper;
import com.zombiesdots.ZombiesDotsMod;
import com.zombiesdots.gui.GuiAddDot;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;

import java.util.Collections;
import java.util.List;

public class CommandAddDot extends CommandBase {

    @Override
    public String getCommandName() { return "adddot"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return "/adddot"; }

    @Override
    public int getRequiredPermissionLevel() { return 0; }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (ZombiesDotsMod.profileManager.isOff()) {
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.RED + "[ZDots] Active profile is 'off'. Run: /profile <name>"));
                return;
            }

            MovingObjectPosition mop = TargetingHelper.traceFromPlayer();

            if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                sender.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.RED + "[ZDots] No block in crosshair within 200 blocks."));
                return;
            }

            BlockPos bp = mop.getBlockPos();
            String face = mop.sideHit != null ? mop.sideHit.getName().toUpperCase() : "NORTH";

            String blockName;
            try {
                blockName = mop.getBlockPos() != null
                        ? net.minecraft.client.Minecraft.getMinecraft().theWorld
                              .getBlockState(bp).getBlock().getRegistryName()
                        : "unknown";
            } catch (Exception e) {
                blockName = "unknown";
            }
            if (blockName == null) blockName = "unknown";

            MarkerData pending = new MarkerData(
                    bp.getX(), bp.getY(), bp.getZ(),
                    face,
                    mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord,
                    "RED", 3,
                    blockName
            );

            ZombiesDotsMod.pendingGui = new GuiAddDot(pending);

        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "[ZDots] Error: " + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? ": " + e.getMessage() : "")));
        }
    }
}
