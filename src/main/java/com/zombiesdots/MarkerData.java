package com.zombiesdots;

public class MarkerData {
    // Block world position
    public int blockX, blockY, blockZ;
    // Which face was hit (EnumFacing name: NORTH/SOUTH/EAST/WEST/UP/DOWN)
    public String face;
    // Exact 3D hit point from raytrace (sub-block precision)
    public double hitX, hitY, hitZ;
    // Dot appearance
    public String color;  // DotColor.name() or "CUSTOM"
    public double size;   // pixel radius at 1/16-block scale; valid values: 0.125, 0.25, 0.5, 1-7
    // For display only
    public String blockName;

    public MarkerData() {}

    public MarkerData(int bx, int by, int bz, String face,
                      double hitX, double hitY, double hitZ,
                      String color, double size, String blockName) {
        this.blockX = bx; this.blockY = by; this.blockZ = bz;
        this.face = face;
        this.hitX = hitX; this.hitY = hitY; this.hitZ = hitZ;
        this.color = color;
        this.size = size;
        this.blockName = blockName;
    }

    // UV coordinates on the face in [0,1] range, for preview rendering.
    // U increases left-to-right, V increases top-to-bottom (texture convention).
    public double getFaceU() {
        if (face == null) return 0.5;
        // U=0 is the LEFT edge of the face as seen by the player looking at it from outside.
        // NORTH face: player faces south (+Z), so their left = east (+X, high hitX) → U=0 at high X
        // SOUTH face: player faces north (-Z), so their left = west (-X, low hitX) → U=0 at low X
        // EAST  face: player faces west (-X),  so their left = south (+Z, high hitZ) → U=0 at high Z
        // WEST  face: player faces east (+X),  so their left = north (-Z, low hitZ)  → U=0 at low Z
        switch (face) {
            case "NORTH": return 1.0 - (hitX - blockX);
            case "SOUTH": return hitX - blockX;
            case "EAST":  return 1.0 - (hitZ - blockZ);
            case "WEST":  return hitZ - blockZ;
            case "UP":    return hitX - blockX;
            case "DOWN":  return hitX - blockX;
            default:      return 0.5;
        }
    }

    public double getFaceV() {
        if (face == null) return 0.5;
        switch (face) {
            case "NORTH": return 1.0 - (hitY - blockY);
            case "SOUTH": return 1.0 - (hitY - blockY);
            case "EAST":  return 1.0 - (hitY - blockY);
            case "WEST":  return 1.0 - (hitY - blockY);
            case "UP":    return hitZ - blockZ;
            case "DOWN":  return 1.0 - (hitZ - blockZ);
            default:      return 0.5;
        }
    }

    public DotColor getDotColor() {
        return DotColor.fromName(color);
    }

    // Returns true if this marker is on the same block face as another
    public boolean sameBlock(int bx, int by, int bz) {
        return blockX == bx && blockY == by && blockZ == bz;
    }

    public String positionString() {
        return blockX + ", " + blockY + ", " + blockZ;
    }
}
