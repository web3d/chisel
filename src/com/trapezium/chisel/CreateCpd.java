/*
 * @(#)CreateCpd.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

// class to create ChiselPluginDescriptor serialized objects for
// plugin architecture.

public class CreateCpd {
    public static void main( String[] args ) {

        ChiselSetDescriptor csd = new ChiselSetDescriptor( "MUTATE", "Modify the geometry in the current world", false, "mutate", true );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.Cubist",
            "Cubist", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.Origami",
            "Origami", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_Triangulator",
            "Triangulate", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_Smasher",
            "Flatten", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_FaceToLineSet",
            "Wireframe", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_FaceToPointSet",
            "Point cloud", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.EV_to_IFS",
            "ElevationGrid to IFS", false );
        serialize( csd, "mutators.cpd" );

        csd = new ChiselSetDescriptor( "ATOMIZE", "Turn shapes into multiple objects", false, "atomize", true );
        csd.addChiselEntry( "com.trapezium.chisel.atomizers.IFS_VertexToCubeSet",
            "Change vertices into cubes", false );
        serialize( csd, "atomizers.cpd" );

    }

    static public void serialize( ChiselSetDescriptor csd, String fileName ) {
        File f = new File( fileName );
        try {
            FileOutputStream fos = new FileOutputStream( f );
            ObjectOutputStream oos = new ObjectOutputStream( fos );
            oos.writeObject( csd );
        } catch ( Exception e ) {
            System.out.println( "** Exception " + e );
            e.printStackTrace();
        }
    }
}
