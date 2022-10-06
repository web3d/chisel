/*
 * @(#)InterpolatorResolution.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.fields.Field;
import com.trapezium.chisel.*;

/**
 *  This adjusts number of digits beyond the decimal point for any interpolator's
 *  "key" and "keyValue" fields.
 */
public class InterpolatorResolution extends ResolutionAdjuster {
    int colorKeyResolution;
    int colorKeyValueResolution;
    int coordinateKeyResolution;
    int coordinateKeyValueResolution;
    int normalKeyResolution;
    int normalKeyValueResolution;
    int orientationKeyResolution;
    int orientationKeyValueResolution;
    int positionKeyResolution;
    int positionKeyValueResolution;
    int scalarKeyResolution;
    int scalarKeyValueResolution;
    
    static final int COLOR_KEY_RESOLUTION = 0;
    static final int COLOR_KEYVALUE_RESOLUTION = 1;
    static final int COORDINATE_KEY_RESOLUTION = 2;
    static final int COORDINATE_KEYVALUE_RESOLUTION = 3;
    static final int NORMAL_KEY_RESOLUTION = 4;
    static final int NORMAL_KEYVALUE_RESOLUTION = 5;
    static final int ORIENTATION_KEY_RESOLUTION = 6;
    static final int ORIENTATION_KEYVALUE_RESOLUTION = 7;
    static final int POSITION_KEY_RESOLUTION = 8;
    static final int POSITION_KEYVALUE_RESOLUTION = 9;
    static final int SCALAR_KEY_RESOLUTION = 10;
    static final int SCALAR_KEYVALUE_RESOLUTION = 11;

	public InterpolatorResolution() {
		super( "Interpolator", "Adjusting interpolator numeric resolution..." );
		colorKeyResolution = 3;
		colorKeyValueResolution = 3;
		coordinateKeyResolution = 3;
		coordinateKeyValueResolution = 3;
		normalKeyResolution = 3;
		normalKeyValueResolution = 3;
		orientationKeyResolution = 3;
		orientationKeyValueResolution = 3;
		positionKeyResolution = 3;
		positionKeyValueResolution = 3;
		scalarKeyResolution = 3;
		scalarKeyValueResolution = 3;
	}

    /** Attempt optimization for an Interpolator Node.  If this node is part of a
     *  PROTOInstance, nothing is done, since it is assumed the optimization occurs
     *  within the PROTO declaration.
     */
 	public void attemptOptimization( Node n ) {
 	    if ( n.getParent() instanceof PROTOInstance ) {
 	        return;
 	    }
 	    int keyResolution = 3;
 	    int keyValueResolution = 3;
 	    String nodeName = n.getBaseName();
 	    if ( nodeName.compareTo( "ColorInterpolator" ) == 0 ) {
 	        keyResolution = colorKeyResolution;
 	        keyValueResolution = colorKeyValueResolution;
 	    } else if ( nodeName.compareTo( "CoordinateInterpolator" ) == 0 ) {
 	        keyResolution = coordinateKeyResolution;
 	        keyValueResolution = coordinateKeyValueResolution;
 	    } else if ( nodeName.compareTo( "NormalInterpolator" ) == 0 ) {
 	        keyResolution = normalKeyResolution;
 	        keyValueResolution = normalKeyValueResolution;
 	    } else if ( nodeName.compareTo( "OrientationInterpolator" ) == 0 ) {
 	        keyResolution = orientationKeyResolution;
 	        keyValueResolution = orientationKeyValueResolution;
 	    } else if ( nodeName.compareTo( "PositionInterpolator" ) == 0 ) {
 	        keyResolution = positionKeyResolution;
 	        keyValueResolution = positionKeyValueResolution;
 	    } else if ( nodeName.compareTo( "ScalarInterpolator" ) == 0 ) {
 	        keyResolution = scalarKeyResolution;
 	        keyValueResolution = scalarKeyValueResolution;
 	    }
 	    Field key = n.getField( "key" );
 	    if ( key != null ) {
     	    replaceRange( key.getFirstTokenOffset(), key.getLastTokenOffset(), new Integer( keyResolution ));
     	}
     	Field keyValue = n.getField( "keyValue" );
     	if ( keyValue != null ) {
     	    replaceRange( keyValue.getFirstTokenOffset(), keyValue.getLastTokenOffset(), new Integer( keyValueResolution ));
     	}
 	}


	/** Adjust resolution */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param instanceof Integer ) {
	        int resolution = ((Integer)param).intValue();
	        int resolutionFactor = 1;
	        for ( int i = 0; i < resolution; i++ ) {
	            resolutionFactor = resolutionFactor * 10;
	        }
	        int scanner = startTokenOffset;
	        dataSource.setState( scanner );
	        while ( true ) {
	            if ( dataSource.isNumber( scanner )) {
	                float fval = dataSource.getFloat( scanner );
	                fval = fval * resolutionFactor;
	                int ival = (int)fval;
	                fval = fval - (float)ival;
	                if ( fval <= -.5f ) {
	                    ival--;
                    } else if ( fval >= .5f ) {
                        ival++;
                    }
	                tp.printAtResolution( ival, resolution );
	            } else {
	                tp.print( dataSource, scanner );
	            }
	            if ( scanner >= endTokenOffset ) {
	                break;
	            }
	            scanner = dataSource.getNextToken();
	        }
        }
	}

    /** control over 6 interpolators, key & keyValue resolution levels */
    public int getNumberOptions() {
        return( 12 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        return( Integer.TYPE );
    }

    public String getOptionLabel( int offset ) {
        switch (offset) {
            case COLOR_KEY_RESOLUTION:
                return( "Color key" );
            case COLOR_KEYVALUE_RESOLUTION:
                return( "Color keyValue" );
            case COORDINATE_KEY_RESOLUTION:
                return( "Coordinate key" );
            case COORDINATE_KEYVALUE_RESOLUTION:
                return( "Coordinate keyValue" );
            case NORMAL_KEY_RESOLUTION:
                return( "Normal key" );
            case NORMAL_KEYVALUE_RESOLUTION:
                return( "Normal keyValue" );
            case ORIENTATION_KEY_RESOLUTION:
                return( "Orientation key" );
            case ORIENTATION_KEYVALUE_RESOLUTION:
                return( "Orientation keyValue" );
            case POSITION_KEY_RESOLUTION:
                return( "Position key" );
            case POSITION_KEYVALUE_RESOLUTION:
                return( "Position keyValue" );
            case SCALAR_KEY_RESOLUTION:
                return( "Scalar key" );
            case SCALAR_KEYVALUE_RESOLUTION:
                return( "Scalar keyValue" );
            default:
                return( null );
        }
    }

    public Object getOptionValue( int offset ) {
        switch (offset) {
            case COLOR_KEY_RESOLUTION:
                return( intToOptionValue( colorKeyResolution ));
            case COLOR_KEYVALUE_RESOLUTION:
                return( intToOptionValue( colorKeyValueResolution ));
            case COORDINATE_KEY_RESOLUTION:
                return( intToOptionValue( coordinateKeyResolution ));
            case COORDINATE_KEYVALUE_RESOLUTION:
                return( intToOptionValue( coordinateKeyValueResolution ));
            case NORMAL_KEY_RESOLUTION:
                return( intToOptionValue( normalKeyResolution ));
            case NORMAL_KEYVALUE_RESOLUTION:
                return( intToOptionValue( normalKeyValueResolution ));
            case ORIENTATION_KEY_RESOLUTION:
                return( intToOptionValue( orientationKeyResolution ));
            case ORIENTATION_KEYVALUE_RESOLUTION:
                return( intToOptionValue( orientationKeyValueResolution ));
            case POSITION_KEY_RESOLUTION:
                return( intToOptionValue( positionKeyResolution ));
            case POSITION_KEYVALUE_RESOLUTION:
                return( intToOptionValue( positionKeyValueResolution ));
            case SCALAR_KEY_RESOLUTION:
                return( intToOptionValue( scalarKeyResolution ));
            case SCALAR_KEYVALUE_RESOLUTION:
                return( intToOptionValue( scalarKeyValueResolution ));
        }
        return "";
    }

    public void setOptionValue( int offset, Object value ) {
        switch (offset) {
            case COLOR_KEY_RESOLUTION:
                colorKeyResolution  = optionValueToInt( value );
                break;
            case COLOR_KEYVALUE_RESOLUTION:
                colorKeyValueResolution = optionValueToInt( value );
                break;
            case COORDINATE_KEY_RESOLUTION:
                coordinateKeyResolution = optionValueToInt( value );
                break;
            case COORDINATE_KEYVALUE_RESOLUTION:
                coordinateKeyValueResolution = optionValueToInt( value );
                break;
            case NORMAL_KEY_RESOLUTION:
                normalKeyResolution = optionValueToInt( value );
                break;
            case NORMAL_KEYVALUE_RESOLUTION:
                normalKeyValueResolution = optionValueToInt( value );
                break;
            case ORIENTATION_KEY_RESOLUTION:
                orientationKeyResolution = optionValueToInt( value );
                break;
            case ORIENTATION_KEYVALUE_RESOLUTION:
                orientationKeyValueResolution = optionValueToInt( value );
                break;
            case POSITION_KEY_RESOLUTION:
                positionKeyResolution = optionValueToInt( value );
                break;
            case POSITION_KEYVALUE_RESOLUTION:
                positionKeyValueResolution = optionValueToInt( value );
                break;
            case SCALAR_KEY_RESOLUTION:
                scalarKeyResolution = optionValueToInt( value );
                break;
            case SCALAR_KEYVALUE_RESOLUTION:
                scalarKeyValueResolution = optionValueToInt( value );
                break;
        }
    }

    public Object getOptionConstraints( int offset ) {
        return( new IntegerConstraints(1, 10, 1 ));
    }
}
