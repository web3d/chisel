/*
 * @(#)ResolutionMaker.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.chisel.*;

/**
 *  This adjusts coordinate resolution to a specific number of places
 *  beyond the decimal point.
 */
public class ResolutionMaker extends ResolutionAdjuster {
    int coordResolution;
    int colorResolution;
    int texCoordResolution;
    int normalResolution;
    int elevationGridResolution;
    int extrusionResolution;
    static final int COORD_RESOLUTION = 0;
    static final int COLOR_RESOLUTION = 1;
    static final int TEXCOORD_RESOLUTION = 2;
    static final int NORMAL_RESOLUTION = 3;
    static final int ELEVATION_GRID_RESOLUTION = 4;
    static final int EXTRUSION_RESOLUTION = 5;

	public ResolutionMaker() {
		super( "IndexedFaceSet", "Adjusting numeric resolution..." );
		coordResolution = 3;
		colorResolution = 3;
		texCoordResolution = 3;
		normalResolution = 3;
		elevationGridResolution = 3;
		extrusionResolution = 3;
		addAdditionalNode( "IndexedLineSet" );
		addAdditionalNode( "PointSet" );
		addAdditionalNode( "ElevationGrid" );
		addAdditionalNode( "Extrusion" );
	}

 	public void attemptOptimization( Node n ) {
 	    // for IndexedFaceSet
 	    Field coord = n.getField( "coord" );
 	    if (( coord != null ) && ( coordResolution != 0 )) {
     	    replaceRange( coord.getFirstTokenOffset(), coord.getLastTokenOffset(), new Integer( coordResolution ));
     	}
 	    Field color = n.getField( "color" );
 	    if (( color != null ) && ( colorResolution != 0 )) {
 	        replaceRange( color.getFirstTokenOffset(), color.getLastTokenOffset(), new Integer( colorResolution ));
 	    }
 	    Field normal = n.getField( "normal" );
 	    if (( normal != null ) && ( normalResolution != 0 )) {
 	        replaceRange( normal.getFirstTokenOffset(), normal.getLastTokenOffset(), new Integer( normalResolution ));
 	    }
 	    Field texCoord = n.getField( "texCoord" );
 	    if (( texCoord != null ) && ( texCoordResolution != 0 )) {
 	        replaceRange( texCoord.getFirstTokenOffset(), texCoord.getLastTokenOffset(), new Integer( texCoordResolution ));
 	    }
 	    // for ElevationGrid
        Field height = n.getField( "height" );
        if (( height != null ) && ( elevationGridResolution != 0 )) {
            replaceRange( height.getFirstTokenOffset(), height.getLastTokenOffset(), new Integer( elevationGridResolution ));
        }
        // for Extrusion
        Field crossSection = n.getField( "crossSection" );
        if (( crossSection != null ) && ( extrusionResolution != 0 )) {
            replaceRange( crossSection.getFirstTokenOffset(), crossSection.getLastTokenOffset(), new Integer( extrusionResolution ));
        }
        Field spine = n.getField( "spine" );
        if (( spine != null ) && ( extrusionResolution != 0 )) {
            replaceRange( spine.getFirstTokenOffset(), spine.getLastTokenOffset(), new Integer( extrusionResolution ));
        }
 	}

    /** control over 6 resolution levels */
    public int getNumberOptions() {
        return( 6 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        return( Integer.TYPE );
    }

    public String getOptionLabel( int offset ) {
        switch (offset) {
            case COORD_RESOLUTION:
                return( "coordinate resolution" );
            case COLOR_RESOLUTION:
                return( "color resolution" );
            case NORMAL_RESOLUTION:
                return( "normal resolution" );
            case TEXCOORD_RESOLUTION:
                return( "texture coordinate resolution" );
            case ELEVATION_GRID_RESOLUTION:
                return( "elevation grid resolution" );
            case EXTRUSION_RESOLUTION:
                return( "extrusion resolution" );
            default:
                return( null );
        }
    }

    public Object getOptionValue( int offset ) {
        switch (offset) {
            case COORD_RESOLUTION:
                return( intToOptionValue( coordResolution ));
            case COLOR_RESOLUTION:
                return( intToOptionValue( colorResolution ));
            case NORMAL_RESOLUTION:
                return( intToOptionValue( normalResolution ));
            case TEXCOORD_RESOLUTION:
                return( intToOptionValue( texCoordResolution ));
            case ELEVATION_GRID_RESOLUTION:
                return( intToOptionValue( elevationGridResolution ));
            case EXTRUSION_RESOLUTION:
                return( intToOptionValue( extrusionResolution ));
        }
        return "";
    }

    public void setOptionValue( int offset, Object value ) {
        switch (offset) {
            case COORD_RESOLUTION:
                coordResolution = optionValueToInt( value );
                break;
            case COLOR_RESOLUTION:
                colorResolution = optionValueToInt( value );
                break;
            case NORMAL_RESOLUTION:
                normalResolution = optionValueToInt( value );
                break;
            case TEXCOORD_RESOLUTION:
                texCoordResolution = optionValueToInt( value );
                break;
            case ELEVATION_GRID_RESOLUTION:
                elevationGridResolution = optionValueToInt( value );
                break;
            case EXTRUSION_RESOLUTION:
                extrusionResolution = optionValueToInt( value );
                break;
        }
    }

    public Object getOptionConstraints( int offset ) {
        return( new IntegerConstraints(1, 10, 1 ));
    }
}
