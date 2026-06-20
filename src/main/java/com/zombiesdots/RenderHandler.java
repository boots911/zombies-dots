package com.zombiesdots;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderHandler {

    // Outward offset so the quad sits just in front of the block face, no z-fighting
    private static final double EPSILON = 0.002;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ZombiesDotsMod.pendingGui == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(ZombiesDotsMod.pendingGui);
            ZombiesDotsMod.pendingGui = null;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (player == null) return;

        ProfileManager pm = ZombiesDotsMod.profileManager;
        if (pm == null || pm.isOff()) return;

        List<MarkerData> markers = pm.getActiveMarkers();
        if (markers.isEmpty()) return;

        float pt = event.partialTicks;

        // Use foot position (not eye) — the world GL transform is keyed to foot coords.
        double camX = player.prevPosX + (player.posX - player.prevPosX) * pt;
        double camY = player.prevPosY + (player.posY - player.prevPosY) * pt;
        double camZ = player.prevPosZ + (player.posZ - player.prevPosZ) * pt;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);  // ensure quad visible from front regardless of winding
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-2.0f, -4.0f);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        for (MarkerData m : markers) {
            double dx = m.hitX - camX;
            double dy = m.hitY - camY;
            double dz = m.hitZ - camZ;
            if (dx*dx + dy*dy + dz*dz > 256.0 * 256.0) continue;

            renderDot(wr, tess, m, camX, camY, camZ);
        }

        GL11.glPolygonOffset(0f, 0f);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GlStateManager.depthMask(true);

        GL11.glPopAttrib();
    }

    private void renderDot(WorldRenderer wr, Tessellator tess,
                           MarkerData m, double camX, double camY, double camZ) {
        // Center of dot at the exact hit position, pushed outward by EPSILON along the face normal
        double cx, cy, cz;
        // Tangent vectors that define the quad plane: Right (R) and Up (U)
        // Correct CCW winding from outside: TL → TR → BR → BL  (normal = R × U points outward)
        double rx, ry, rz;
        double ux, uy, uz;

        switch (m.face) {
            case "NORTH": // outward normal = -Z; R=+X, U=+Y
                cx = m.hitX;           cy = m.hitY;           cz = m.hitZ - EPSILON;
                rx = 1; ry = 0; rz = 0;
                ux = 0; uy = 1; uz = 0;
                break;
            case "SOUTH": // outward normal = +Z; R=-X, U=+Y
                cx = m.hitX;           cy = m.hitY;           cz = m.hitZ + EPSILON;
                rx = -1; ry = 0; rz = 0;
                ux =  0; uy = 1; uz = 0;
                break;
            case "EAST":  // outward normal = +X; R=+Z, U=+Y
                cx = m.hitX + EPSILON; cy = m.hitY;           cz = m.hitZ;
                rx = 0; ry = 0; rz = 1;
                ux = 0; uy = 1; uz = 0;
                break;
            case "WEST":  // outward normal = -X; R=-Z, U=+Y
                cx = m.hitX - EPSILON; cy = m.hitY;           cz = m.hitZ;
                rx =  0; ry = 0; rz = -1;
                ux =  0; uy = 1; uz =  0;
                break;
            case "UP":    // outward normal = +Y; R=+X, U=+Z
                cx = m.hitX;           cy = m.hitY + EPSILON; cz = m.hitZ;
                rx = 1; ry = 0; rz = 0;
                ux = 0; uy = 0; uz = 1;
                break;
            case "DOWN":  // outward normal = -Y; R=+X, U=-Z
                cx = m.hitX;           cy = m.hitY - EPSILON; cz = m.hitZ;
                rx = 1; ry =  0; rz =  0;
                ux = 0; uy =  0; uz = -1;
                break;
            default:
                return;
        }

        // half-size: size pixels at 1/16-block-per-pixel scale
        double hs = (m.size / 2.0) / 16.0;
        if (hs < 0.5 / 16.0) hs = 0.5 / 16.0;

        DotColor col = m.getDotColor();

        // Camera-relative center
        double ox = cx - camX;
        double oy = cy - camY;
        double oz = cz - camZ;

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // CCW from the outside of the face: TL → TR → BR → BL
        // TL = center - R*hs + U*hs
        wr.pos(ox - rx*hs + ux*hs, oy - ry*hs + uy*hs, oz - rz*hs + uz*hs).color(col.r, col.g, col.b, 255).endVertex();
        // TR = center + R*hs + U*hs
        wr.pos(ox + rx*hs + ux*hs, oy + ry*hs + uy*hs, oz + rz*hs + uz*hs).color(col.r, col.g, col.b, 255).endVertex();
        // BR = center + R*hs - U*hs
        wr.pos(ox + rx*hs - ux*hs, oy + ry*hs - uy*hs, oz + rz*hs - uz*hs).color(col.r, col.g, col.b, 255).endVertex();
        // BL = center - R*hs - U*hs
        wr.pos(ox - rx*hs - ux*hs, oy - ry*hs - uy*hs, oz - rz*hs - uz*hs).color(col.r, col.g, col.b, 255).endVertex();
        tess.draw();
    }
}
