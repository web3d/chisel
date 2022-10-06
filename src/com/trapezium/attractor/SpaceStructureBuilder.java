
/*
 * @(#)SpaceStructureBuilder.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.vrml.node.Node;
import com.trapezium.chisel.*;
import com.trapezium.vrmlspace.SpaceStructureLoader;
import com.trapezium.space.SpaceStructure;


/** The SpaceStructureBuilder is a Chisel which loads information from
 *  a VRML file into a SpaceStructureLoader.  It is used like a function
 *  call to a ChiselEngine, telling the chisel engine to chisel a file
 *  using the SpaceStructureBuilder, the end result is a SpaceStructureLoader
 *  filled in the all the SpaceStructures of the file.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class SpaceStructureBuilder extends Optimizer {
    SpaceStructureLoader loader;

    /** Class constructor */
    public SpaceStructureBuilder() {
        super( "IndexedFaceSet", "Loading IndexedFaceSets" );
    }

    /** Set up the loader to hold IFS info */
    public void setSpaceStructureLoader( SpaceStructureLoader loader ) {
        this.loader = loader;
    }

    /** Load an IFS into the loader */
    public void attemptOptimization( Node n ) {
 //       if ( loader != null ) { MLo (removed useless if-test -- since next line was/is commented out)
 //           loader.load( n );
 //       }
    }

    /** Get space structure by offset in the original file */
    public SpaceStructure getSpaceStructure( int offset ) {
        if ( loader != null ) {
            return( loader.getSpaceStructure( offset ));
        } else {
            return( null );
        }
    }
    
    public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
    }
}
