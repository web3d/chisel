package com.trapezium.space; // was com.trapezium.attractor...

/** IndexedFaceSet constants */
public interface IFS {
    // field types
    static public final int Coord = 0;
    static public final int CoordIndex = 1;
    static public final int TexCoord = 2;
    static public final int TexCoordIndex = 3;
    static public final int Normal = 4;
    static public final int NormalIndex = 5;
    static public final int Color = 6;
    static public final int ColorIndex = 7;

    // correspondence types
    static public final int OnePerCoord = 100;
    static public final int OnePerCoordIndex = 101;
    static public final int OnePerFace = 102;
}
