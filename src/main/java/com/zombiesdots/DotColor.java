package com.zombiesdots;

public enum DotColor {
    WHITE  ("White",   0xFF, 0xFF, 0xFF),
    BLACK  ("Black",   0x00, 0x00, 0x00),
    RED    ("Red",     0xFF, 0x00, 0x00),
    GREEN  ("Green",   0x00, 0xFF, 0x00),
    BLUE   ("Blue",    0x00, 0x00, 0xFF),
    YELLOW ("Yellow",  0xFF, 0xFF, 0x00),
    CYAN   ("Cyan",    0x00, 0xFF, 0xFF),
    MAGENTA("Magenta", 0xFF, 0x00, 0xFF);

    public final String displayName;
    public final int r, g, b;
    public final int argb;

    DotColor(String displayName, int r, int g, int b) {
        this.displayName = displayName;
        this.r = r;
        this.g = g;
        this.b = b;
        this.argb = 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static DotColor fromName(String name) {
        if (name == null) return RED;
        for (DotColor c : values()) {
            if (c.name().equalsIgnoreCase(name)) return c;
        }
        return RED;
    }

    public static DotColor[] allColors() {
        return values();
    }
}
