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
    private DotColor selectedColor;
    private int selectedSize;

    private static final int PANEL_W  = 210;
    private static final int PANEL_H  = 232;

    // Swatch grid: 8 colors, 18px each with 2px gap
    private static final int SWATCH = 18;
    private static final int SWATCH_GAP = 3;

    // Button IDs
    private static final int BTN_DEC    = 10;
    private static final int BTN_INC    = 11;
    private static final int BTN_SAVE   = 12;
    private static final int BTN_CANCEL = 13;

    // Computed positions (set in initGui)
    private int panelX, panelY;

    public GuiAddDot(MarkerData pending) {
        this.pending = pending;
        this.selectedColor = DotColor.fromName(pending.color);
        this.selectedSize  = Math.max(1, Math.min(8, pending.size));
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        panelX = width  / 2 - PANEL_W / 2;
        panelY = height / 2 - PANEL_H / 2;

        // Size -/+ buttons on row y=100
        int sizeRow = panelY + 100;
        buttonList.add(new GuiButton(BTN_DEC, panelX + 14,            sizeRow, 22, 18, "-"));
        buttonList.add(new GuiButton(BTN_INC, panelX + PANEL_W - 36,  sizeRow, 22, 18, "+"));

        // Save / Cancel at y=200
        int btnRow = panelY + 200;
        buttonList.add(new GuiButton(BTN_SAVE,   panelX + 18,              btnRow, 80, 20, "Save"));
        buttonList.add(new GuiButton(BTN_CANCEL, panelX + PANEL_W - 98,    btnRow, 80, 20, "Cancel"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // Panel background
        drawRect(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + PANEL_H + 1, 0xFF555555);
        drawRect(panelX,     panelY,     panelX + PANEL_W,     panelY + PANEL_H,     0xFF2B2B2B);

        int cx = panelX + PANEL_W / 2;

        // ── Title ──────────────────────────────────────────────
        drawCenteredString(fontRendererObj,
                EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Add Dot Marker",
                cx, panelY + 6, 0xFFFFFF);

        // ── Block info ─────────────────────────────────────────
        int infoX = panelX + 8;
        drawString(fontRendererObj,
                EnumChatFormatting.GRAY + "Pos: "     + EnumChatFormatting.WHITE + pending.positionString(),
                infoX, panelY + 20, 0xFFFFFF);
        drawString(fontRendererObj,
                EnumChatFormatting.GRAY + "Face: "    + EnumChatFormatting.WHITE + pending.face,
                infoX, panelY + 31, 0xFFFFFF);
        drawString(fontRendererObj,
                EnumChatFormatting.GRAY + "Block: "   + EnumChatFormatting.WHITE + shortName(pending.blockName),
                infoX, panelY + 42, 0xFFFFFF);
        drawString(fontRendererObj,
                EnumChatFormatting.GRAY + "Profile: " + EnumChatFormatting.AQUA
                        + ZombiesDotsMod.profileManager.getActiveProfile(),
                infoX, panelY + 53, 0xFFFFFF);

        // ── Color section ──────────────────────────────────────
        drawRect(panelX + 6, panelY + 66, panelX + PANEL_W - 7, panelY + 67, 0xFF666666);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Color",
                infoX, panelY + 70, 0xAAAAAA);

        DotColor[] colors = DotColor.allColors();
        // Center swatches: 8 × (18+3) - 3 = 165 wide
        int totalW = colors.length * SWATCH + (colors.length - 1) * SWATCH_GAP;
        int swatchStartX = cx - totalW / 2;
        int swatchY = panelY + 83;

        for (int i = 0; i < colors.length; i++) {
            int sx = swatchStartX + i * (SWATCH + SWATCH_GAP);
            boolean selected = colors[i] == selectedColor;

            if (selected) {
                // White highlight border
                drawRect(sx - 2, swatchY - 2, sx + SWATCH + 2, swatchY + SWATCH + 2, 0xFFFFFFFF);
                drawRect(sx - 1, swatchY - 1, sx + SWATCH + 1, swatchY + SWATCH + 1, 0xFF000000);
            }
            drawRect(sx, swatchY, sx + SWATCH, swatchY + SWATCH, colors[i].argb);
        }

        // ── Size section ───────────────────────────────────────
        drawRect(panelX + 6, panelY + 108, panelX + PANEL_W - 7, panelY + 109, 0xFF666666);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Size",
                infoX, panelY + 90, 0xAAAAAA);
        // Size display centered between the two buttons
        String sizeLabel = "" + EnumChatFormatting.WHITE + selectedSize + " px";
        drawCenteredString(fontRendererObj, sizeLabel, cx, panelY + 105, 0xFFFFFF);

        // ── Preview section ────────────────────────────────────
        drawRect(panelX + 6, panelY + 126, panelX + PANEL_W - 7, panelY + 127, 0xFF666666);
        drawString(fontRendererObj, EnumChatFormatting.GRAY + "Preview",
                infoX, panelY + 130, 0xAAAAAA);

        // 64×64 preview box, centered
        int previewSize = 64;
        int prevX = cx - previewSize / 2;
        int prevY = panelY + 143;

        // Grey background representing the block face
        drawRect(prevX, prevY, prevX + previewSize, prevY + previewSize, 0xFF888888);
        // Subtle 4×4 texture grid lines (representing 16x16 texture at 4px per texel)
        for (int gi = 1; gi < 4; gi++) {
            int gx = prevX + gi * previewSize / 4;
            int gy = prevY + gi * previewSize / 4;
            drawRect(gx, prevY, gx + 1, prevY + previewSize, 0x22000000);
            drawRect(prevX, gy, prevX + previewSize, gy + 1, 0x22000000);
        }

        // Dot position mapped to preview space
        double fu = clamp01(pending.getFaceU());
        double fv = clamp01(pending.getFaceV());
        int dotCX = prevX + (int)(fu * previewSize);
        int dotCY = prevY + (int)(fv * previewSize);
        // Dot radius in preview: scale size by previewSize/16 / 2
        int dotR = Math.max(1, (int)Math.round(selectedSize * previewSize / 16.0 / 2.0));
        drawRect(dotCX - dotR, dotCY - dotR, dotCX + dotR, dotCY + dotR, selectedColor.argb);
        // Thin black outline on preview dot
        drawRect(dotCX - dotR - 1, dotCY - dotR - 1, dotCX + dotR + 1, dotCY - dotR,     0xFF000000);
        drawRect(dotCX - dotR - 1, dotCY + dotR,     dotCX + dotR + 1, dotCY + dotR + 1, 0xFF000000);
        drawRect(dotCX - dotR - 1, dotCY - dotR - 1, dotCX - dotR,     dotCY + dotR + 1, 0xFF000000);
        drawRect(dotCX + dotR,     dotCY - dotR - 1, dotCX + dotR + 1, dotCY + dotR + 1, 0xFF000000);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Color swatch hit-test
        DotColor[] colors = DotColor.allColors();
        int cx = panelX + PANEL_W / 2;
        int totalW = colors.length * SWATCH + (colors.length - 1) * SWATCH_GAP;
        int swatchStartX = cx - totalW / 2;
        int swatchY = panelY + 83;

        for (int i = 0; i < colors.length; i++) {
            int sx = swatchStartX + i * (SWATCH + SWATCH_GAP);
            if (mouseX >= sx && mouseX < sx + SWATCH &&
                mouseY >= swatchY && mouseY < swatchY + SWATCH) {
                selectedColor = colors[i];
                pending.color = selectedColor.name();
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_DEC:
                if (selectedSize > 1) { selectedSize--; pending.size = selectedSize; }
                break;
            case BTN_INC:
                if (selectedSize < 8) { selectedSize++; pending.size = selectedSize; }
                break;
            case BTN_SAVE:
                pending.color = selectedColor.name();
                pending.size  = selectedSize;
                ZombiesDotsMod.profileManager.addMarker(pending);
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GREEN + "[ZDots] Saved at "
                        + pending.positionString() + " face=" + pending.face
                        + " color=" + selectedColor.displayName + " size=" + selectedSize));
                mc.displayGuiScreen(null);
                break;
            case BTN_CANCEL:
                mc.displayGuiScreen(null);
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private String shortName(String name) {
        if (name == null) return "unknown";
        int c = name.indexOf(':');
        return c >= 0 ? name.substring(c + 1) : name;
    }

    private double clamp01(double v) {
        return v < 0 ? 0 : v > 1 ? 1 : v;
    }
}
