/*
 * @(#)CoordRemoveData.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.vrml.node.IndexInfo;
import java.util.BitSet;

public class CoordRemoveData {
    int dataType;  // one of the public types defined above
    IndexInfo indexInfo;
    BitSet coordsToKeep;
    int coordIndexCount;
    int faceCount;

    public CoordRemoveData( int dataType, IndexInfo indexInfo, BitSet coordsToKeep, int coordIndexCount, int faceCount ) {
	    this.dataType = dataType;
	    this.indexInfo = indexInfo;
	    this.coordsToKeep = coordsToKeep;
	    this.coordIndexCount = coordIndexCount;
	    this.faceCount = faceCount;
    }

    /** get the maximum number of values to put out in an index field */
    public int getIndexLimit() {
        if ( indexInfo == null ) {
            return( coordIndexCount );
        } else if ( indexInfo.isPerVertex() ) {
            return( coordIndexCount );
        } else {
            return( faceCount );
        }
    }

    //
    //  how many number fields are associated with each unit
    //
    int getUnitCount() {
        if ( dataType == IFS_CoordRemover.TexCoord ) return( 2 );
        else return( 3 );
    }

    //
    //  If a color/colorIndex or normal/normalIndex has perVertex set to true, this
    //  means the index values have a one-to-one correspondence with the coordinates.
    //
    public boolean isIndexSynchedWithCoord() {
        if (( dataType == IFS_CoordRemover.ColorIndex ) || ( dataType == IFS_CoordRemover.NormalIndex )) {
            return( indexInfo.isPerVertex() );
        }
        return( false );
    }

    //
    //  Get the BitSet the indicates which coord values are in use
    //
    public BitSet getCoordsToKeep() {
        return( coordsToKeep );
    }


    public int getDataType() {
        return( dataType );
    }

    public boolean isCoord() {
        return(( dataType == IFS_CoordRemover.Coord ) || ( dataType == IFS_CoordRemover.TexCoord ) ||
        ( dataType == IFS_CoordRemover.Color ) || ( dataType == IFS_CoordRemover.Normal ));
    }
}
