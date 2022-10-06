/*
 * @(#)CreateSer.java
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

// test class to create ChiselSetDescriptor serialized objects for
// plugin architecture.

//
// The specific plugin "plugin_1.ser", ... , "plugin_<N>.ser" is then
// included in the ".jar" file for testing

//
// Release version should probably include the "plugin_1.ser" file in
// a "plugin_1.jar" file that is added to the classpath
//

public class CreateSer {
    public static void main( String[] args ) {
        ChiselSetDescriptor csd = new ChiselSetDescriptor( "", "", true, "clean", 
            false );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.DEFremover",
            "Remove unused DEFs", false, ChiselSet.UnusedDEFCountListener );
        csd.addChiselEntry(
            "com.trapezium.chisel.cleaners.DefaultFieldValueRemover",
            "Remove default fields", false,
            ChiselSet.DefaultFieldCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.IFS_DupCoordDetector",
            "Remove repeated value refs", false,
            ChiselSet.RepeatedValueCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.IFS_CoordRemover",
            "Remove unused values", false, ChiselSet.UnusedCoordCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.IFS_DupIndexRemover",
            "Remove repeated index values", false,
            ChiselSet.DupIndexCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.DuplicateFieldRemover",
            "Remove repeated fields", false,
            ChiselSet.DuplicateFieldCountListener );
        csd.addChiselEntry(
            "com.trapezium.chisel.cleaners.UnusedPROTOInterfaceRemover",
            "Remove unused PROTO interface fields", false,
            ChiselSet.UnusedPROTOinterfaceCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.IFS_BadFaceRemover",
            "Remove bad faces", false, ChiselSet.BadFaceCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.NodeRemover",
            "Remove useless interpolators", false,
            ChiselSet.UselessNodeCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.IFS_NoFaceRemover",
            "Remove empty IndexedFaceSets", false,
            ChiselSet.EmptyIndexedFaceSetCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.ROUTEMover",
            "Move ROUTEs to end of file", false, ChiselSet.BadRouteListener );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.KeyValueRemover",
            "Remove unnecessary interpolator values", false,
            ChiselSet.UnnecessaryKeyValueListener );
        serialize( csd, "builtin_1.ser" );

        csd = new ChiselSetDescriptor( "", "", true, "condense", true );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.ResolutionMaker",
            "Adjust numeric resolution", false,
            ChiselSet.ValueNodeCountListener );
        csd.addChiselEntry(
            "com.trapezium.chisel.condensers.InterpolatorResolution",
            "Adjust Interpolator resolution", false,
            ChiselSet.InterpolatorCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.DEFmaker",
            "Create DEF/USE", false, ChiselSet.DEFUSECountListener );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.PROTOMaker",
            "Create PROTOs for interpolators", false,
            ChiselSet.InterpolatorCountListener2 );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.IFS_IndexOptimizer",
            "Create index fields", false, ChiselSet.UnindexedValueListener );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.NormalRemover",
            "Remove normals", false, ChiselSet.NormalCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.NameShortener",
            "Shorten DEF names", false, ChiselSet.DEFUSECountListener2 );
        csd.addChiselEntry( "com.trapezium.chisel.condensers.NameFixer",
            "Simplify DEF names", false, ChiselSet.DEFUSECountListener3 );
        csd.addChiselEntry( "com.trapezium.chisel.cleaners.InterpolatorMinimizer",
            "Single value interpolator keys", false );
        serialize( csd, "builtin_2.ser" );

        csd = new ChiselSetDescriptor( "", "", true, "reduce", true );
        csd.addChiselEntry( "com.trapezium.chisel.reducers.IFS_CoplanarTriToQuad",
            "Coplanar triangle to quad", false );
        csd.addChiselEntry( "com.trapezium.chisel.reducers.IFS_Simplifier",
            "Remove smallest edges", false );
        csd.addChiselEntry( "com.trapezium.chisel.reducers.IFS_PolygonRemover",
            "Remove smallest triangles", false );
        csd.addChiselEntry( "com.trapezium.chisel.reducers.IFS_MeshRemover",
            "Merge parallel edges", false );
        serialize( csd, "builtin_3.ser" );

        csd = new ChiselSetDescriptor( "", "", false, "reorganize", true );
        csd.addChiselEntry( "com.trapezium.chisel.reorganizers.Uninline",
            "Uninline files", false, ChiselSet.InlineCountListener );
        csd.addChiselEntry( "com.trapezium.chisel.reorganizers.ShapeToInline",
            "Turn Shapes into Inlines", false );
        csd.addChiselEntry( "com.trapezium.chisel.reorganizers.ShapeColorSplitter",
            "Split IFS by color", false );
        csd.addChiselEntry(
            "com.trapezium.chisel.reorganizers.ShapeConnectivitySplitter",
            "Split IFS by connectivity", false );
        csd.addChiselEntry(
            "com.trapezium.chisel.reorganizers.ElevationGridSplitter",
            "Split ElevationGrid", false,
            ChiselSet.ElevationGridCountListener );
        serialize( csd, "builtin_4.ser" );

        csd = new ChiselSetDescriptor( "MUTATE", "Modify the geometry in the current world", false, "mutate", true );
        //csd.addChiselEntry( "com.trapezium.chisel.mutators.Cubist",
        //    "Cubist", false );
        //csd.addChiselEntry( "com.trapezium.chisel.mutators.Origami",
        //    "Origami", false );
        //csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_Triangulator",
        //    "Triangulate", false );
        //csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_Smasher",
        //    "Flatten", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_FaceToLineSet",
            "Wireframe", false );
        csd.addChiselEntry( "com.trapezium.chisel.mutators.IFS_FaceToPointSet",
            "Point cloud", false );
        serialize( csd, "plugin_1.ser" );
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
