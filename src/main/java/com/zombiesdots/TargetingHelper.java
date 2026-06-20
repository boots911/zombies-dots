package com.zombiesdots;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class TargetingHelper {

    private static final int MAX_DISTANCE = 200;
    // Max times to skip a transparent/non-solid block and retry
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

            // Advance the ray start 0.05 blocks past the hit point to skip this block
            start = new Vec3(
                    mop.hitVec.xCoord + look.xCoord * 0.05,
                    mop.hitVec.yCoord + look.yCoord * 0.05,
                    mop.hitVec.zCoord + look.zCoord * 0.05
            );
        }
        return null;
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
