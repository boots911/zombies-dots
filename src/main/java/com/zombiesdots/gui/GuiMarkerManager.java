package com.zombiesdots.gui;

import com.zombiesdots.ZombiesDotsMod;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.List;

public class GuiMarkerManager extends GuiScreen {

    private static final int ROWS_VISIBLE = 6;
    private static final int ROW_H        = 20;
    private static final int PANEL_W      = 250;

    // Heights
    private static final int HEADER_H     = 34;  // title + active line + separator
    private static final int SCROLL_H     = 26;  // ^ / v row always reserved
    private static final int FOOTER_H     = 34;  // separator + New Profile button
    private static final int INPUT_H      = 50;  // extra for text-field area

    private int scrollOffset = 0;
    private boolean inputMode = false;
    private GuiTextField newProfileField;

    private int panelX, panelY, listTop, listBottom;

    // Button IDs
    private static final int BTN_CLOSE     = 0;
    private static final int BTN_SCROLL_UP = 1;
    private static final int BTN_SCROLL_DN = 2;
    private static final int BTN_NEW       = 3;
    private static final int BTN_CONFIRM   = 4;
    private static final int BTN_CANCEL_IN = 5;
    // USE = 100+i,  DELETE = 200+i

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        rebuildButtons();
    }

    private int panelH() {
        return HEADER_H + ROWS_VISIBLE * ROW_H + SCROLL_H + FOOTER_H + (inputMode ? INPUT_H : 0);
    }

    private void rebuildButtons() {
        buttonList.clear();

        panelX   = width  / 2 - PANEL_W / 2;
        panelY   = height / 2 - panelH() / 2;
        listTop  = panelY + HEADER_H;
        listBottom = listTop + ROWS_VISIBLE * ROW_H;

        // Close [X]
        buttonList.add(new GuiButton(BTN_CLOSE, panelX + PANEL_W - 18, panelY + 3, 16, 14, "X"));

        // Row buttons (Use / Delete) — full-width rows, no right-side scroll overlap
        List<String> profiles = ZombiesDotsMod.profileManager.getProfiles();
        int end = Math.min(scrollOffset + ROWS_VISIBLE, profiles.size());
        for (int i = scrollOffset; i < end; i++) {
            int row = i - scrollOffset;
            int ry  = listTop + row * ROW_H + 1;
            buttonList.add(new GuiButton(100 + i, panelX + PANEL_W - 96, ry, 40, ROW_H - 2, "Use"));
            buttonList.add(new GuiButton(200 + i, panelX + PANEL_W - 52, ry, 46, ROW_H - 2, "Delete"));
        }

        // Scroll ^ / v — sit below the list in SCROLL_H area
        int scrollY = listBottom + 4;
        int cx = panelX + PANEL_W / 2;
        GuiButton upBtn = new GuiButton(BTN_SCROLL_UP, cx - 66, scrollY, 60, 18, "^ Up");
        GuiButton dnBtn = new GuiButton(BTN_SCROLL_DN, cx + 6,  scrollY, 60, 18, "v Down");
        upBtn.enabled = scrollOffset > 0;
        dnBtn.enabled = scrollOffset + ROWS_VISIBLE < profiles.size();
        buttonList.add(upBtn);
        buttonList.add(dnBtn);

        // New Profile
        int newY = listBottom + SCROLL_H + 8;
        buttonList.add(new GuiButton(BTN_NEW, cx - 60, newY, 120, 18, "+ New Profile"));

        // Input mode
        if (inputMode) {
            int fieldY = newY + 26;
            if (newProfileField == null) {
                newProfileField = new GuiTextField(0, fontRendererObj,
                        panelX + 8, fieldY, PANEL_W - 16, 16);
                newProfileField.setFocused(true);
                newProfileField.setMaxStringLength(32);
            } else {
                newProfileField.yPosition = fieldY;
            }
            int confirmY = fieldY + 20;
            buttonList.add(new GuiButton(BTN_CONFIRM,   panelX + 8,   confirmY, 110, 18, "Create"));
            buttonList.add(new GuiButton(BTN_CANCEL_IN, panelX + 122, confirmY, 110, 18, "Cancel"));
        } else {
            newProfileField = null;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        List<String> profiles = ZombiesDotsMod.profileManager.getProfiles();
        String active = ZombiesDotsMod.profileManager.getActiveProfile();
        int h = panelH();
        int cx = panelX + PANEL_W / 2;

        // Panel bg
        drawRect(panelX - 1, panelY - 1, panelX + PANEL_W + 1, panelY + h + 1, 0xFF555555);
        drawRect(panelX,     panelY,     panelX + PANEL_W,     panelY + h,     0xFF2B2B2B);

        // Title
        drawCenteredString(fontRendererObj,
                EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "Marker Profiles",
                cx, panelY + 5, 0xFFFFFF);

        // Active indicator
        boolean isOff = "off".equalsIgnoreCase(active);
        String activeColor = isOff ? EnumChatFormatting.GRAY.toString() : EnumChatFormatting.AQUA.toString();
        drawString(fontRendererObj,
                EnumChatFormatting.GRAY + "Active: " + activeColor + active,
                panelX + 8, panelY + 18, 0xFFFFFF);

        // Header separator
        drawRect(panelX + 4, panelY + 29, panelX + PANEL_W - 4, panelY + 30, 0xFF555555);

        // Profile rows
        int end = Math.min(scrollOffset + ROWS_VISIBLE, profiles.size());
        for (int i = scrollOffset; i < end; i++) {
            int row  = i - scrollOffset;
            int ry   = listTop + row * ROW_H;
            String name  = profiles.get(i);
            int count    = ZombiesDotsMod.profileManager.getMarkerCount(name);
            boolean here = name.equals(active);

            if (here) {
                drawRect(panelX + 2, ry + 1, panelX + PANEL_W - 2, ry + ROW_H - 1, 0x33FFFFFF);
            }

            String prefix = here ? EnumChatFormatting.GREEN + "> " : "  ";
            drawString(fontRendererObj,
                    prefix + EnumChatFormatting.WHITE + name
                    + EnumChatFormatting.DARK_GRAY + " (" + count + ")",
                    panelX + 6, ry + 6, 0xFFFFFF);
        }

        if (profiles.isEmpty()) {
            drawCenteredString(fontRendererObj,
                    EnumChatFormatting.DARK_GRAY + "(no profiles)",
                    cx, listTop + ROWS_VISIBLE * ROW_H / 2 - 4, 0xFFFFFF);
        }

        // Separator below list
        drawRect(panelX + 4, listBottom + 1, panelX + PANEL_W - 4, listBottom + 2, 0xFF555555);

        // Input mode label + text field
        if (inputMode) {
            int newY = listBottom + SCROLL_H + 8;
            drawString(fontRendererObj,
                    EnumChatFormatting.GRAY + "New profile name:",
                    panelX + 8, newY + 26, 0xFFFFFF);
            if (newProfileField != null) newProfileField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        List<String> profiles = ZombiesDotsMod.profileManager.getProfiles();

        if (button.id == BTN_CLOSE)     { mc.displayGuiScreen(null); return; }
        if (button.id == BTN_SCROLL_UP) { if (scrollOffset > 0) scrollOffset--; rebuildButtons(); return; }
        if (button.id == BTN_SCROLL_DN) { if (scrollOffset + ROWS_VISIBLE < profiles.size()) scrollOffset++; rebuildButtons(); return; }

        if (button.id == BTN_NEW) {
            inputMode = true;
            newProfileField = null;
            rebuildButtons();
            return;
        }
        if (button.id == BTN_CONFIRM && inputMode) {
            if (newProfileField != null) {
                String name = newProfileField.getText().trim();
                if (!name.isEmpty()) ZombiesDotsMod.profileManager.createProfile(name);
            }
            inputMode = false;
            newProfileField = null;
            rebuildButtons();
            return;
        }
        if (button.id == BTN_CANCEL_IN) {
            inputMode = false;
            newProfileField = null;
            rebuildButtons();
            return;
        }

        if (button.id >= 100 && button.id < 200) {
            int i = button.id - 100;
            if (i >= 0 && i < profiles.size()) {
                ZombiesDotsMod.profileManager.setActiveProfile(profiles.get(i));
                rebuildButtons();
            }
            return;
        }
        if (button.id >= 200 && button.id < 300) {
            int i = button.id - 200;
            if (i >= 0 && i < profiles.size() && profiles.size() > 1) {
                ZombiesDotsMod.profileManager.deleteProfile(profiles.get(i));
                if (scrollOffset > 0 && scrollOffset >= profiles.size() - 1) scrollOffset--;
                rebuildButtons();
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (inputMode && newProfileField != null) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                String name = newProfileField.getText().trim();
                if (!name.isEmpty()) ZombiesDotsMod.profileManager.createProfile(name);
                inputMode = false; newProfileField = null; rebuildButtons();
                return;
            }
            if (keyCode == Keyboard.KEY_ESCAPE) {
                inputMode = false; newProfileField = null; rebuildButtons();
                return;
            }
            newProfileField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (inputMode && newProfileField != null) {
            newProfileField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
