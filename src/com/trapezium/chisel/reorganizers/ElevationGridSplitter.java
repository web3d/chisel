/*
 * @(#)ElevationGridSplitter.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

import com.trapezium.chisel.*;

import com.trapezium.parse.TokenTypes;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.fields.SFNodeValue;

public class ElevationGridSplitter extends Optimizer {
    /** defined index of Number of Columns option */
    public final static int OPTION_COLUMNS = 0;
    /** defined index of Number of Rows option */
    public final static int OPTION_ROWS = 1;
    /** labelling grids with comments */
    public final static int OPTION_LABEL = 2;
    /** column grouping */
    public final static int OPTION_XGROUP = 3;
    /** row grouping */
    public final static int OPTION_ZGROUP = 4;


    /** The main class for performing an ElevationGrid split operation */
    class ElevationInfo {
        int xDimension;
        int zDimension;
        float xSpacing;
        float zSpacing;
        Node elevationNode; // the ElevationGrid itself
        Node shapeNode;   // the Shape containing the ElevationGrid
        MFFieldValue heightValues;
        MFFieldValue colorValues;
        boolean colorPerVertex;
        MFFieldValue normalValues;
        boolean normalPerVertex;
        MFFieldValue textureValues;
        boolean elevationGridHasTexture;
        int scaleToInvert;

        public ElevationInfo( int xd, int zd, float xp, float zp, Node en, Node rn ) {
            xDimension = xd;
            zDimension = zd;
            xSpacing = xp;
            zSpacing = zp;
            elevationNode = en;
            shapeNode = rn;
            elevationGridHasTexture = false;
            scaleToInvert = -1;
            Field height = en.getField( "height" );
            if ( height != null ) {
                FieldValue fv = height.getFieldValue();
                if ( fv instanceof MFFieldValue ) {
                    heightValues = (MFFieldValue)fv;
                }
            }
            colorPerVertex = en.getBoolValue( "colorPerVertex" );
            colorValues = en.getMFfield( "color", "color" );
            normalPerVertex = en.getBoolValue( "normalPerVertex" );
            normalValues = en.getMFfield( "normal", "vector" );
            textureValues = en.getMFfield( "texCoord", "point" );
            Node appearance = shapeNode.getNodeValue( "appearance" );
            if ( appearance != null ) {
                Node texture = appearance.getNodeValue( "texture" );
                if ( texture != null ) {
                    elevationGridHasTexture = true;
                }
                Node textureTransform = appearance.getNodeValue( "textureTransform" );
                if ( textureTransform != null ) {
                    Field scale = textureTransform.getField( "scale" );
                    if ( scale != null ) {
                        FieldValue scaleValue = scale.getFieldValue();
                        if ( scaleValue != null ) {
                            scaleToInvert = scaleValue.getLastTokenOffset();
                        }
                    }
                }
            }
        }

        /** Actually perform the ElevationGrid split */
        public void split( TokenPrinter tp, int xDivisor, int zDivisor ) {
            if ( heightValues == null ) return;
            int xWidth = xDimension/xDivisor;
            int zWidth = zDimension/zDivisor;
            if (( xGroupSize == 1 ) && ( zGroupSize == 1 )) {
                for ( int j = 0; j < zDivisor; j++ ) {
                    for ( int i = 0; i < xDivisor; i++ ) {
                        genTransformHeader( i, xWidth, j, zWidth, tp );
                        genShapeHeader( tp );
                        genShapeData( i, xWidth, j, zWidth, tp, xDivisor, zDivisor );
                        genShapeTrailer( tp );
                        genTransformTrailer( tp );
                    }
                }
            } else {
                int numberXGroups = xDivisor/xGroupSize;
                if (( xDivisor % xGroupSize ) > 0 ) {
                    numberXGroups++;
                }
                int numberZGroups = zDivisor/zGroupSize;
                if (( zDivisor % zGroupSize ) > 0 ) {
                    numberZGroups++;
                }
                if (( numberXGroups > xGroupSize ) || ( numberZGroups > zGroupSize )) {
                    int numberXG1 = numberXGroups/xGroupSize;
                    if (( numberXGroups % xGroupSize ) > 0 ) {
                        numberXG1++;
                    }
                    int numberZG1 = numberZGroups/zGroupSize;
                    if (( numberZGroups % zGroupSize ) > 0 ) {
                        numberZG1++;
                    }
                    for ( int zg1 = 0; zg1 < numberZG1; zg1++ ) {
                        for ( int xg1 = 0; xg1 < numberXG1; xg1++ ) {
                            tp.flush();
                            tp.print( "Transform { children [" );
                            int xgend = xg1*xGroupSize + xGroupSize;
                            if ( xgend > numberXGroups ) {
                                xgend = numberXGroups;
                            }
                            int zgend = zg1*zGroupSize + zGroupSize;
                            if ( zgend > numberZGroups ) {
                                zgend = numberZGroups;
                            }
                            for ( int xg = xg1*xGroupSize; xg < xgend; xg++ ) {
                                for ( int zg = zg1*zGroupSize; zg < zgend; zg++ ) {
                                    tp.flush();
                                    tp.print( "Transform { children [" );
                                    int xend = xg*xGroupSize + xGroupSize;
                                    if ( xend > xDivisor ) {
                                        xend = xDivisor;
                                    }
                                    int zend = zg*zGroupSize + zGroupSize;
                                    if ( zend > zDivisor ) {
                                        zend = zDivisor;
                                    }
                                    for ( int i = xg*xGroupSize; i < xend; i++ ) {
                                        for ( int j = zg*zGroupSize; j < zend; j++ ) {
                                            genTransformHeader( i, xWidth, j, zWidth, tp );
                                            genShapeHeader( tp );
                                            genShapeData( i, xWidth, j, zWidth, tp, xDivisor, zDivisor );
                                            genShapeTrailer( tp );
                                            genTransformTrailer( tp );
                                        }
                                    }
                                    tp.print( "] }" );
                                    tp.flush();
                                }
                            }
                            tp.print( "] }" );
                            tp.flush();
                        }
                    }
                } else {
                    for ( int zg = 0; zg < numberZGroups; zg++ ) {
                        for ( int xg = 0; xg < numberXGroups; xg++ ) {
                            tp.flush();
                            tp.print( "Transform { children [" );
                            int xend = xg*xGroupSize + xGroupSize;
                            if ( xend > xDivisor ) {
                                xend = xDivisor;
                            }
                            int zend = zg*zGroupSize + zGroupSize;
                            if ( zend > zDivisor ) {
                                zend = zDivisor;
                            }
                            for ( int i = xg*xGroupSize; i < xend; i++ ) {
                                for ( int j = zg*zGroupSize; j < zend; j++ ) {
                                    genTransformHeader( i, xWidth, j, zWidth, tp );
                                    genShapeHeader( tp );
                                    genShapeData( i, xWidth, j, zWidth, tp, xDivisor, zDivisor );
                                    genShapeTrailer( tp );
                                    genTransformTrailer( tp );
                                }
                            }
                            tp.print( "] }" );
                            tp.flush();
                        }
                    }
                }
            }
        }

        /** generate the header for the Transform containing a unit of the split result
         *
         *  @param xOffset which grid in X dimension
         *  @param xWidth the width of each ElevationGrid section
         *  @param zOffset which grid in Z dimension
         *  @param zWidth the width of each ElevationGrid section
         *  @param tp the TokenPrinter print destination
         */
        void genTransformHeader( int xOffset, int xWidth, int zOffset, int zWidth, TokenPrinter tp ) {
            if ( labelGrids ) {
                tp.flush();
                tp.print( "# x: " + (xOffset+1) + " z: " + (zOffset+1) );
                tp.flush();
            }
            tp.print( "Transform { translation " );
            tp.print( xWidth * xOffset * xSpacing );
            tp.print( 0 );
            tp.print( zWidth * zOffset * zSpacing );
            tp.print( "children [" );
        }

        void genTransformTrailer( TokenPrinter tp ) {
            tp.print( "] }" );
        }

        /** Generate text for everything from start of Shape node to first token before
         *  the start of the ElevationGrid.
         *
         *  @param tp print destination
         */
        void genShapeHeader( TokenPrinter tp ) {
            int firstInRange = shapeNode.getFirstTokenOffset();
            int lastInRange = elevationNode.getFirstTokenOffset();
            if (( scaleToInvert == -1 ) || ( scaleToInvert < firstInRange ) || ( scaleToInvert > lastInRange )) {
                tp.printRange( firstInRange, lastInRange - 1, false );
            } else {
                tp.printRange( firstInRange, scaleToInvert - 1, false );
                TokenEnumerator te = shapeNode.getTokenEnumerator();
                float f = te.getFloat( scaleToInvert );
                f = f * -1;
                tp.print( f );
                tp.printRange( scaleToInvert + 1, lastInRange - 1, false );
            }
        }

        /** Generate text for everything after last token of ElevationGrid to the last token
         *  of the Shape node.
         *
         *  @param tp print destination
         */
        void genShapeTrailer( TokenPrinter tp ) {
            tp.printRange( elevationNode.getLastTokenOffset() + 1, shapeNode.getLastTokenOffset(), false );
        }


        /** generate Shape data for the ElevationGrid.
         *
         *  @param xOffset which grid in X dimension
         *  @param xWidth the number of units wide of previous girds generated
         *  @param zOffset which grid in Z dimension
         *  @param zWidth the number of units tall of previous grids generated
         *  @param tp print destination
         *  @param xDivisor number of generated grids in X dimension
         *  @param zDivisor number of generated grids in Z dimension
         */
        void genShapeData( int xOffset, int xWidth, int zOffset, int zWidth, TokenPrinter tp, int xDivisor, int zDivisor ) {
            tp.print( "ElevationGrid {" );
            int numXspots;
            if ( xOffset < ( xDivisor - 1 )) {
                numXspots = xWidth + 1;
            } else {
                numXspots = xDimension - xWidth*xOffset;
            }
            int startSkip = xWidth * xOffset;
            int endSkip = xDimension - startSkip - numXspots;
            int rowSkip = zWidth * zOffset;
            int numZspots;
            if ( zOffset < ( zDivisor - 1 )) {
                numZspots = zWidth + 1;
            } else {
                numZspots = zDimension - zWidth*zOffset;
            }
            tp.print( "xDimension " + numXspots );
            tp.print( "zDimension " + numZspots );
            tp.print( "xSpacing " + xSpacing );
            tp.print( "zSpacing " + zSpacing );
            tp.print( "height [" );

    		int scanner = heightValues.getFirstTokenOffset();
			dataSource.setState( scanner );
			for ( int i = 0; i < rowSkip; i++ ) {
			    for ( int j = 0; j < xDimension; j++ ) {
			        scanner = dataSource.skipNonNumbers();
			        scanner = dataSource.getNextToken();
			    }
			}
			for ( int i = 0; i < numZspots; i++ ) {
			    // skip to the right spot in the row
			    for ( int skip = 0; skip < startSkip; skip++ ) {
			        scanner = dataSource.skipNonNumbers();
			        scanner = dataSource.getNextToken();
			    }
			    for ( int j = 0; j < numXspots; j++ ) {
			        scanner = dataSource.skipNonNumbers();
			        tp.print( dataSource, scanner, TokenTypes.NumberToken );
			        scanner = dataSource.getNextToken();
			    }
			    // skip to the end of the row
			    for ( int skip = 0; skip < endSkip; skip++ ) {
			        scanner = dataSource.skipNonNumbers();
			        scanner = dataSource.getNextToken();
			    }
			}

            tp.print( "]" );
            additionalValues( tp, "color", "Color", "color",
                colorValues, colorPerVertex, 3, startSkip, endSkip,
                rowSkip, numXspots, numZspots );
            additionalValues( tp, "normal", "Normal", "vector",
                normalValues, normalPerVertex, 3, startSkip, endSkip,
                rowSkip, numXspots, numZspots );
            if ( textureValues != null ) {
                additionalValues( tp, "texCoord", "TextureCoordinate", "point",
                    textureValues, true, 2, startSkip, endSkip,
                    rowSkip, numXspots, numZspots );
            } else if ( elevationGridHasTexture ) {
                outputTexCoord( tp, xWidth, xOffset, numXspots, zWidth, zOffset, numZspots );
            }
            tp.print( "}" );
        }

        /** Output texCoords
         *
         *  @param tp print destination
         *  @param xOffset which grid in X dimension
         *  @param xWidth the number of units wide of previous girds generated
         *  @param numXspots number of units wide for this grid
         *  @param zOffset which grid in Z dimension
         *  @param zWidth the number of units tall of previous grids generated
         *  @param numZspots number of units tall for this grid
         */
        void outputTexCoord( TokenPrinter tp, int xWidth, int xOffset, int numXspots, int zWidth, int zOffset, int numZspots ) {
            tp.flush();
            tp.print( "texCoord TextureCoordinate { point [" );
            float totalWidth = (xDimension - 1) * xSpacing;
            float totalHeight = (zDimension - 1) * zSpacing;
            float adjustedWidthUnit = xSpacing/totalWidth;
            float adjustedHeightUnit = zSpacing/totalHeight;
            float startWidth = xOffset * xWidth * adjustedWidthUnit;
            float startHeight = 1.0f - zOffset * zWidth * adjustedHeightUnit;
            for ( int j = 0; j < numZspots; j++ ) {
                float zLoc = startHeight - j * adjustedHeightUnit;
                for ( int i = 0; i < numXspots; i++ ) {
                    float xLoc = startWidth + i * adjustedWidthUnit;
                    tp.print( xLoc );
                    tp.print( zLoc );
                }
            }
                    
            tp.print( "] }" );
        }
         
        /** Output additional ElevationGrid field values.
         *
         *  @param tp print destination
         *  @param nodeFieldName name of ElevationGrid field containing a node
         *  @param nodeName type of node contained by nodeFieldName
         *  @param valueFieldName name of the value field we are concerned with
         *  @param values field containing values
         *  @param perVertex indicates whether values are indicated one per vertex
         *     (vs. one per face)
         *  @param factor how many numeric values per "value"
         *  @param startSkip the number of "values" to skip at the start of each row
         *  @param endSkip the number of "values" to skip at the end of each row
         *  @param rowSkip the number of "rows" to skip
         *  @param numXspots the number of X "values" to output in a row
         *  @param numZspots the number of Z "values" to output in a column
         */
        void additionalValues( TokenPrinter tp,
            String nodeFieldName, String nodeName, String valueFieldName,
            MFFieldValue values, boolean perVertex, int factor,
            int startSkip, int endSkip, int rowSkip,
            int numXspots, int numZspots ) {
            if ( values != null ) {
                tp.print( nodeFieldName + " " + nodeName + " { " + valueFieldName + " [" );
                int scanner = values.getFirstTokenOffset();
//                System.out.println( "There are " + values.getRawValueCount() + " " + nodeName + " values" );
                dataSource.setState( scanner );
                int valueXdimension = xDimension;
                if ( !perVertex ) {
                    valueXdimension--;
                }
                for ( int i = 0; i < rowSkip; i++ ) {
                    for ( int j = 0; j < valueXdimension; j++ ) {
                        for ( int k = 0; k < factor; k++ ) {
                            scanner = dataSource.skipNonNumbers();
                            scanner = dataSource.getNextToken();
                        }
                    }
                }
                if ( !perVertex ) {
                    numXspots--;
                    numZspots--;
                }
    			for ( int i = 0; i < numZspots; i++ ) {
    			    // skip to the right spot in the row
    			    for ( int skip = 0; skip < startSkip; skip++ ) {
    			        for ( int k = 0; k < factor; k++ ) {
        			        scanner = dataSource.skipNonNumbers();
        			        scanner = dataSource.getNextToken();
        			    }
    			    }
    			    for ( int j = 0; j < numXspots; j++ ) {
    			        for ( int k = 0; k < factor; k++ ) {
        			        scanner = dataSource.skipNonNumbers();
        			        tp.print( dataSource, scanner, TokenTypes.NumberToken );
    	    		        scanner = dataSource.getNextToken();
    	    		    }
    			    }

    			    // skip to the end of the row
    			    for ( int skip = 0; skip < endSkip; skip++ ) {
    			        for ( int k = 0; k < factor; k++ ) {
        			        scanner = dataSource.skipNonNumbers();
        			        scanner = dataSource.getNextToken();
        			    }
    			    }
    			}
                tp.print( "] }" );
            }
        }

    }

	public ElevationGridSplitter() {
		super( "ElevationGrid", "Splitting ElevationGrids..." );
	}

    /** Elevation grid has two integer options, NumberColumns and NumberRows */
    public int getNumberOptions() {
        return( 5 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        switch (offset) {
            case OPTION_COLUMNS:
            case OPTION_ROWS:
            case OPTION_XGROUP:
            case OPTION_ZGROUP:
                try {
                    return( Integer.TYPE );
                } catch (Exception e) {
                    break;
                }
            case OPTION_LABEL:
                return( Boolean.TYPE );
        }
        return null;
    }

    public String getOptionLabel( int offset ) {
        switch (offset) {
            case OPTION_COLUMNS:
                return( "Number of columns" );
            case OPTION_ROWS:
                return( "Number of rows" );
            case OPTION_LABEL:
                return( "Label each grid" );
            case OPTION_XGROUP:
                return( "Column group size" );
            case OPTION_ZGROUP:
                return( "Row group size" );
        }
        return null;
    }

    int numColumns = 2;
    int numRows = 2;
    boolean labelGrids = false;
    int xGroupSize = 1;
    int zGroupSize = 1;
    public Object getOptionValue( int offset ) {
        switch (offset) {
            case OPTION_COLUMNS:
                return( intToOptionValue(numColumns) );
            case OPTION_ROWS:
                return( intToOptionValue(numRows) );
            case OPTION_XGROUP:
                return( intToOptionValue(xGroupSize) );
            case OPTION_ZGROUP:
                return( intToOptionValue(zGroupSize) );
            case OPTION_LABEL:
                return( booleanToOptionValue(labelGrids) );
        }
        return "";
    }

    public void setOptionValue( int offset, Object value ) {
        switch (offset) {
            case OPTION_COLUMNS:
                numColumns = optionValueToInt(value);
                break;
            case OPTION_ROWS:
                numRows = optionValueToInt(value);
                break;
            case OPTION_LABEL:
                labelGrids = optionValueToBoolean(value);
                break;
            case OPTION_XGROUP:
                xGroupSize = optionValueToInt(value);
                break;
            case OPTION_ZGROUP:
                zGroupSize = optionValueToInt(value);
                break;
        }
    }

    public Object getOptionConstraints( int offset ) {
        switch (offset) {
            case OPTION_COLUMNS:
                return( new IntegerConstraints(1, 16, 1 ));
            case OPTION_ROWS:
                return( new IntegerConstraints(1, 16, 1 ));
            case OPTION_XGROUP:
            case OPTION_ZGROUP:
                return( new IntegerConstraints(1, 8, 1 ));
        }
        return "";
    }

	public void attemptOptimization( Node n ) {
		int xDimension = n.getIntValue( "xDimension" );
		int zDimension = n.getIntValue( "zDimension" );
		float xSpacing = n.getFloatValue( dataSource, "xSpacing" );
		float zSpacing = n.getFloatValue( dataSource, "zSpacing" );
		// don't try to split small ElevationGrids... gets tricky
		if ((  xDimension > 10 ) && ( zDimension > 10 ) && ( xSpacing > 0 ) && ( zSpacing > 0 )) {
		    // According to spec, this can only be in a geometry
		    // field, so find the Shape node containing that geometry field
		    // We replace the Shape.
		    Node elevationNode = n;
		    VrmlElement p = n.getParent();
		    if ( p != null ) {
		        // if the parent is a DEF/USE node, we only do the split if it is a DEF
		        if ( p instanceof Node ) {
		            Node pnode = (Node)p;
    		        if ( pnode.isDEForUSE() ) {
    		            if ( pnode.isDEF() ) {
    		                p = pnode.getParent();
    		            } else {
    		                return;
    		            }
    		        }
		        }
    		    VrmlElement gp = p.getParent();

    		    if ( gp instanceof ExposedField ) {
    		        ExposedField gpf = (ExposedField)gp;
    		        if ( gpf.getFieldId().compareTo( "geometry" ) == 0 ) {
       		            VrmlElement gpp = gp.getParent();
    		            if ( gpp instanceof Node ) {
    		                n = (Node)gpp;
    		                System.out.println( "replaceRange for n " + n );
                		    replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), new ElevationInfo( xDimension, zDimension, xSpacing, zSpacing, elevationNode, n ));
    		            }
    		        }
    		    }
    		}
		}
	}


	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    // if the token before start is "children" ratner than the expected "[",
	    // then we have to add the brackets manually
	    String tokenBeforeStart = dataSource.toString( startTokenOffset - 1 );
	    boolean manualBrackets = false;
	    if ( tokenBeforeStart.compareTo( "children" ) == 0 ) {
	        manualBrackets = true;
	    }
	    if ( param instanceof ElevationInfo ) {
	        if ( manualBrackets ) {
	            tp.print( "[" );
	        }
	        ElevationInfo ei = (ElevationInfo)param;
	        ei.split(tp, numColumns, numRows);
	        if ( manualBrackets ) {
	            tp.print( "]" );
	        }
	    }
	}
}


