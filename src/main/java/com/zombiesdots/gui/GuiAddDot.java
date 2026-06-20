package com.zombiesdots.gui;

import com.zombiesdots.DotColor;
import com.zombiesdots.MarkerData;
import com.zombiesdots.ZombiesDotsMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAddDot extends GuiScreen {

    private final MarkerData pending;
    // null when CUSTOM is selected
    private DotColor selectedColor;
    private int sizeIndex;

    private static final double[] SIZES = {0.125, 0.25, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0};

    private static final int PANEL_W   = 210;
    private static final int PANEL_H   = 232;
    private static final int SWATCH    = 18;
    private static final int SWATCH_GAP = 3;
    // 7 standard colors + 1 custom slot = 8 swatches
    private static final int NUM_SWATCHES = 8;

    private static final int BTN_DEC    = 10;
    private static final int BTN_INC    = 11;
    private static final int BTN_SAVE   = 12;
    private static final int BTN_CANCEL = 13;

    private int panelX, panelY;

    public GuiAddDot(MarkerData pending) {
        this.pending = pending;
        if ("CUSTOM".equalsIgnoreCase(pending.color)) {
            this.selectedColor = null;
        } else {
            this.selectedColor = DotColor.fromName(pending.color);
        }
        this.sizeIndex = findSizeIndex(pending.size > 0 ? pending.size : 3.0);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        panelX = width  / 2 - PANEL_W / 2;
        panelY = height / 2 - PANEL_H / 2;

        buttonList.add(new GuiButton(BTN_DEC,    panelX + 14,           panelY + 100, 22, 18, "-"));
        buttonList.add(new GuiButton(BTN_INC,    panelX + PANEL_W - 36, panelY + 100, 22, 18, "+"));
        buttonList.add(new GuiButton(BTN_SAVE,   panelX + 18,           panelY + 200, 80, 20, "Save"));
        buttonList.add(new GuiButton(BTN_CANCEL, panelX + PANEL_W - 98, panelY + 200, 80, 20, "Cancel"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawRect(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        drawRect(panelX,     panelY,     panelX + PANEL_W,     panelY + PANEL_H,     0xFF2B2B2B);

        int cx   = panelX + PANEL_W / 2;
        int infoX = panelX + 8;

        drawCenteredString(fontRendererObj,
                EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Add Dot Marker",
                cx, panelY + 6, 0xFFFFFF);

        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Pos: "     + EnumChatFormatting.WHITE + pending.positionString(), infoX, panelY + 20, 0xFFFFFF);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Face: "    + EnumChatFormatting.WHITE + pending.face,             infoX, panelY + 31, 0xFFFFFF);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Block: "   + EnumChatFormatting.WHITE + shortName(pending.blockName), infoX, panelY + 42, 0xFFFFFF);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Profile: " + EnumChatFormatting.AQUA  + ZombiesDotsMod.profileManager.getActiveProfile(), infoX, panelY + 53, 0xFFFFFF);

        // Color section
        drawRect(panelX + 6, panelY + 66, panelX + PANEL_W - 7, panelY + 67, 0xFF666666);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Color  " + EnumChatFormatting.DARK_GRAY + "(custom: /dotcolor <hex>)", infoX, panelY + 70, 0xAAAAAA);

        DotColor[] colors = DotColor.allColors();  // 7 standard colors
        int totalW     = NUM_SWATCHES * SWATCH + (NUM_SWATCHES - 1) * SWATCH_GAP;
        int swatchStartX = cx - totalW / 2;
        int swatchY    = panelY + 83;

        // Standard color swatches
        for (int i = 0; i < colors.length; i++) {
            int sx = swatchStartX + i * (SWATCH + SWATCH_GAP);
            boolean sel = (selectedColor != null) && (colors[i] == selectedColor);
            if (sel) {
                drawRect(sx - 2, swatchY - 2, sx + SWATCH + 2, swatchY + SWATCH + 2, 0xFFFFFFFF);
                drawRect(sx - 1, swatchY - 1, sx + SWATCH + 1, swatchY + SWATCH + 1, 0xFF000000);
            }
            drawRect(sx, swatchY, sx + SWATCH, swatchY + SWATCH, colors[i].argb);
        }

        // Custom swatch (slot 7)
        int csx = swatchStartX + 7 * (SWATCH + SWATCH_GAP);
        boolean customSel = "CUSTOM".equalsIgnoreCase(pending.color);
        if (customSel) {
            drawRect(csx - 2, swatchY - 2, csx + SWATCH + 2, swatchY + SWATCH + 2, 0xFFFFFFFF);
            drawRect(csx - 1, swatchY - 1, csx + SWATCH + 1, swatchY + SWATCH + 1, 0xFF000000);
        }
        drawRect(csx, swatchY, csx + SWATCH, swatchY + SWATCH, ZombiesDotsMod.profileManager.getCustomARGB());
        // "C" label so the user can tell it's the custom slot
        drawCenteredString(fontRendererObj, "C", csx + SWATCH / 2, swatchY + SWATCH / 2 - 4, isLightColor(ZombiesDotsMod.profileManager.getCustomARGB()) ? 0xFF000000 : 0xFFFFFFFF);

        // Size section
        drawRect(panelX + 6, panelY + 108, panelX + PANEL_W - 7, panelY + 109, 0xFF666666);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Size", infoX, panelY + 90, 0xAAAAAA);
        drawCenteredString(fontRendererObj, "" + EnumChatFormatting.WHITE + formatSize(SIZES[sizeIndex]), cx, panelY + 105, 0xFFFFFF);

        // Preview section
        drawRect(panelX + 6, panelY + 126, panelX + PANEL_W - 7, panelY + 127, 0xFF666666);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Preview", infoX, panelY + 130, 0xAAAAAA);

        int previewSize = 64;
        int prevX = cx - previewSize / 2;
        int prevY = panelY + 143;

        drawRect(prevX, prevY, prevX + previewSize, prevY + previewSize, 0xFF888888);
        for (int gi = 1; gi < 4; gi++) {
            int gx = prevX + gi * previewSize / 4;
            int gy = prevY + gi * previewSize / 4;
            drawRect(gx,    prevY, gx + 1,               prevY + previewSize, 0x22000000);
            drawRect(prevX, gy,   prevX + previewSize, gy + 1,               0x22000000);
        }

        double fu   = clamp01(pending.getFaceU());
        double fv   = clamp01(pending.getFaceV());
        int dotCX   = prevX + (int)(fu * previewSize);
        int dotCY   = prevY + (int)(fv * previewSize);
        int dotR    = Math.max(1, (int)Math.round(SIZES[sizeIndex] * previewSize / 16.0 / 2.0));
        int dotARGB = dotARGB();

        drawRect(dotCX - dotR,     dotCY - dotR,     dotCX + dotR,     dotCY + dotR,     dotARGB);
        drawRect(dotCX - dotR - 1, dotCY - dotR - 1, dotCX + dotR + 1, dotCY - dotR,     0xFF000000);
        drawRect(dotCX - dotR - 1, dotCY + dotR,     dotCX + dotR + 1, dotCY + dotR + 1, 0xFF000000);
        drawRect(dotCX - dotR - 1, dotCY - dotR - 1, dotCX - dotR,     dotCY + dotR + 1, 0xFF000000);
        drawRect(dotCX + dotR,     dotCY - dotR - 1, dotCX + dotR + 1, dotCY + dotR + 1, 0xFF000000);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        DotColor[] colors = DotColor.allColors();
        int cx           = panelX + PANEL_W / 2;
        int totalW       = NUM_SWATCHES * SWATCH + (NUM_SWATCHES - 1) * SWATCH_GAP;
        int swatchStartX = cx - totalW / 2;
        int swatchY      = panelY + 83;

        for (int i = 0; i < colors.length; i++) {
            int sx = swatchStartX + i * (SWATCH + SWATCH_GAP);
            if (mouseX >= sx && mouseX < sx + SWATCH && mouseY >= swatchY && mouseY < swatchY + SWATCH) {
                selectedColor = colors[i];
                pending.color = selectedColor.name();
            }
        }
        // Custom swatch
        int csx = swatchStartX + 7 * (SWATCH + SWATCH_GAP);
        if (mouseX >= csx && mouseX < csx + SWATCH && mouseY >= swatchY && mouseY < swatchY + SWATCH) {
            selectedColor = null;
            pending.color = "CUSTOM";
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_DEC:
                if (sizeIndex > 0) { sizeIndex--; pending.size = SIZES[sizeIndex]; }
                break;
            case BTN_INC:
                if (sizeIndex < SIZES.length - 1) { sizeIndex++; pending.size = SIZES[sizeIndex]; }
                break;
            case BTN_SAVE:
                pending.size = SIZES[sizeIndex];
                ZombiesDotsMod.profileManager.addMarker(pending);
                String colorName = "CUSTOM".equalsIgnoreCase(pending.color) ? "custom"
                        : (selectedColor != null ? selectedColor.displayName : "unknown");
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GREEN + "[ZDots] Saved at "
                        + pending.positionString() + " face=" + pending.face
                        + " color=" + colorName + " size=" + formatSize(SIZES[sizeIndex])));
                mc.displayGuiScreen(null);
                break;
            case BTN_CANCEL:
                mc.displayGuiScreen(null);
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private int dotARGB() {
        if ("CUSTOM".equalsIgnoreCase(pending.color)) return ZombiesDotsMod.profileManager.getCustomARGB();
        return selectedColor != null ? selectedColor.argb : 0xFFFF0000;
    }

    private static int findSizeIndex(double size) {
        int best = 0;
        double bestDiff = Double.MAX_VALUE;
        for (int i = 0; i < SIZES.length; i++) {
            double d = Math.abs(SIZES[i] - size);
            if (d < bestDiff) { bestDiff = d; best = i; }
        }
        return best;
    }

    private static String formatSize(double s) {
        if (s >= 1.0 && s == Math.floor(s)) return (int)s + " px";
        return s + " px";
    }

    private static boolean isLightColor(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8)  & 0xFF;
        int b =  argb        & 0xFF;
        return (r * 299 + g * 587 + b * 114) > 128000;
    }

    private String shortName(String name) {
        if (name == null) return "unknown";
        int c = name.indexOf(':');
        return c >= 0 ? name.substring(c + 1) : name;
    }

    private double clamp01(double v) {
        return v < 0 ? 0 : v > 1 ? 1 : v;
    }
}
