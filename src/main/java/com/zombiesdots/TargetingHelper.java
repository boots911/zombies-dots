package com.zombiesdots;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class TargetingHelper {

    private static final int MAX_DISTANCE = 200;
    private static final int MAX_SKIP = 16;

    public static MovingObjectPosition traceFromPlayer() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null || player.worldObj == null) return null;

        Vec3 look  = player.getLookVec();
        double eyeX = player.posX;
        double eyeY = player.posY + player.getEyeHeight();
        double eyeZ = player.posZ;

        Vec3 start = new Vec3(eyeX, eyeY, eyeZ);
        Vec3 end   = new Vec3(
                eyeX + look.xCoord * MAX_DISTANCE,
                eyeY + look.yCoord * MAX_DISTANCE,
                eyeZ + look.zCoord * MAX_DISTANCE
        );

        for (int attempt = 0; attempt < MAX_SKIP; attempt++) {
            MovingObjectPosition mop = player.worldObj.rayTraceBlocks(start, end, false, false, false);
            if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return mop;

            Block block = player.worldObj.getBlockState(mop.getBlockPos()).getBlock();
            if (!shouldSkip(block)) return mop;

            // Advance to just past the far face of this block so the next raytrace
            // starts cleanly outside it. A fixed offset from the entry face would land
            // us inside the block and cause the next raytrace to skip the immediately
            // adjacent block behind it.
            start = exitFace(mop.hitVec, mop.getBlockPos(), look);
        }
        return null;
    }

    // Returns a point 0.001 blocks past the face where the ray exits the given block.
    private static Vec3 exitFace(Vec3 hitVec, BlockPos bp, Vec3 look) {
        double tx = Double.MAX_VALUE, ty = Double.MAX_VALUE, tz = Double.MAX_VALUE;
        if (look.xCoord > 0) tx = (bp.getX() + 1 - hitVec.xCoord) / look.xCoord;
        else if (look.xCoord < 0) tx = (bp.getX()     - hitVec.xCoord) / look.xCoord;
        if (look.yCoord > 0) ty = (bp.getY() + 1 - hitVec.yCoord) / look.yCoord;
        else if (look.yCoord < 0) ty = (bp.getY()     - hitVec.yCoord) / look.yCoord;
        if (look.zCoord > 0) tz = (bp.getZ() + 1 - hitVec.zCoord) / look.zCoord;
        else if (look.zCoord < 0) tz = (bp.getZ()     - hitVec.zCoord) / look.zCoord;

        double t = Math.min(Math.min(tx, ty), tz) + 0.001;
        return new Vec3(
                hitVec.xCoord + look.xCoord * t,
                hitVec.yCoord + look.yCoord * t,
                hitVec.zCoord + look.zCoord * t
        );
    }

    private static boolean shouldSkip(Block block) {
        return block == Blocks.barrier
            || block == Blocks.water
            || block == Blocks.flowing_water
            || block == Blocks.fire
            || block instanceof BlockButton
            || block instanceof BlockLever;
    }
}
