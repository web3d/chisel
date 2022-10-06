/*
 * @(#)IFS_Simplifier.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reducers;

import com.trapezium.chisel.*;

import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.space.SpaceStructure;
import com.trapezium.vrml.node.space.SpacePrimitive;
import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.node.space.PerVertexData;
import com.trapezium.vrml.node.space.BoundingBox;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import java.util.Vector;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  This chisel removes polygons with an edge removal algorithm implemented in SpaceStructure
 */
public class IFS_Simplifier extends IFS_CoplanarTriToQuad {

    Hashtable boundingBoxes;  // key Node, value BoundingBox
    Hashtable boundingBoxOverlap;  // key BoundingBox, value Vector of overlap BoundingBox
    boolean firstOptimizeFlag;
    int percentMarkedEdgesToRemove;
    int minEdgeCount;
    int percentBBincrease;
    boolean preserveColorBoundaries;
    int xFactor;
    int yFactor;
    int zFactor;
    boolean useBoundingBox;
    int ifsNumber;
    
    static final int PERCENT_OPTION = 0;
    static final int EDGECOUNT_OPTION = 1;
    static final int USE_BOUNDING_BOX = 2;
    static final int BOUNDING_INCREASE = 3;
    static final int PRESERVE_COLOR_BOUNDARIES = 4;
    static final int SCALE_X = 5;
    static final int SCALE_Y = 6;
    static final int SCALE_Z = 7;
    
	public IFS_Simplifier() {
		super( "Removing small edges..." );
		boundingBoxes = new Hashtable();
		boundingBoxOverlap = new Hashtable();
		firstOptimizeFlag = true;
		percentMarkedEdgesToRemove = 20;
		minEdgeCount = 100;
		percentBBincrease = 0;
		preserveColorBoundaries = false;
		xFactor = 1;
		yFactor = 1;
		zFactor = 1;
		ifsNumber = 0;
	}

    public void reset() {
        firstOptimizeFlag = true;
        ifsNumber = 0;
    }
    
    /** changed from 7 to 4, last 3 I think are a failed experiment... */
    public int getNumberOptions() {
        return( 4 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        if (( offset == PRESERVE_COLOR_BOUNDARIES ) || ( offset == USE_BOUNDING_BOX )) {
            return( Boolean.TYPE );
        } else {
            return( Integer.TYPE );
        }
    }

    public String getOptionLabel( int offset ) {
        if ( offset == PERCENT_OPTION ) {
            return( "% marked edges to remove" );
        } else if ( offset == EDGECOUNT_OPTION ) {
            return( "min edge count" );
        } else if ( offset == USE_BOUNDING_BOX ) {
            return( "no polygon reduction within overlapping bounding boxes" );
        } else if ( offset == BOUNDING_INCREASE ) {
            return( "% bounding box increase" );
        } else if ( offset == PRESERVE_COLOR_BOUNDARIES ) {
            return( "preserve color boundaries" );
        } else if ( offset == SCALE_X ) {
            return( "X scale factor" );
        } else if ( offset == SCALE_Y ) {
            return( "Y scale factor" );
        } else {
            return( "Z scale factor" );
        }
    }

    public Object getOptionValue( int offset ) {
        if ( offset == PERCENT_OPTION ) {
            return( intToOptionValue(percentMarkedEdgesToRemove) );
        } else if ( offset == EDGECOUNT_OPTION ) {
            return( intToOptionValue(minEdgeCount) );
        } else if ( offset == BOUNDING_INCREASE ) {
            return( intToOptionValue(percentBBincrease) );
        } else if ( offset == PRESERVE_COLOR_BOUNDARIES ) {
            return( booleanToOptionValue(preserveColorBoundaries) );
        } else if ( offset == USE_BOUNDING_BOX ) {
            return( booleanToOptionValue(useBoundingBox) );
        } else if ( offset == SCALE_X ) {
            return( intToOptionValue( xFactor ));
        } else if ( offset == SCALE_Y ) {
            return( intToOptionValue( yFactor ));
        } else {
            return( intToOptionValue( zFactor ));
        }
    }

    public void setOptionValue( int offset, Object value ) {
        if ( offset == PERCENT_OPTION ) {
            percentMarkedEdgesToRemove = optionValueToInt(value);
        } else if ( offset == EDGECOUNT_OPTION ) {
            minEdgeCount = optionValueToInt( value );
        } else if ( offset == BOUNDING_INCREASE ) {
            percentBBincrease = optionValueToInt( value );
        } else if ( offset == PRESERVE_COLOR_BOUNDARIES ) {
            preserveColorBoundaries = optionValueToBoolean(value);
        } else if ( offset == USE_BOUNDING_BOX ) {
            useBoundingBox = optionValueToBoolean(value);
        } else if ( offset == SCALE_X ) {
            xFactor = optionValueToInt( value );
        } else if ( offset == SCALE_Y ) {
            yFactor = optionValueToInt( value );
        } else {
            zFactor = optionValueToInt( value );
        }
    }

    public Object getOptionConstraints( int offset ) {
        if ( offset == PERCENT_OPTION ) {
            return( new IntegerConstraints( 5, 100, 5 ));
        } else if ( offset == EDGECOUNT_OPTION ) {
            return( new IntegerConstraints( 0, 100, 100 ));
        } else if ( offset == BOUNDING_INCREASE ) {
            return( new IntegerConstraints( 0, 100, 5 ));
        } else if ( offset == PRESERVE_COLOR_BOUNDARIES ) {
            return( null );
        } else {
            return( new IntegerConstraints( 1, 20, 1 ));
        }
    }

	public void attemptOptimization( Node n ) {
	    BoundingBox b = new BoundingBox();
	    super.attemptOptimization( n, b );
	    boundingBoxes.put( n, b );
	    b.increase( percentBBincrease );
	}

	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    // create overlap bounding boxes if not done yet
	    if ( firstOptimizeFlag ) {
	        createOverlap();
	        firstOptimizeFlag = false;
	    }
	    
		if ( param instanceof OptimizeParam ) {
			OptimizeParam op = (OptimizeParam)param;
			
			// process the space structure if not done yet
			SpaceStructure ss = op.getSpaceStructure();
			if ( !alreadyProcessed( ss )) {
			    Node n = op.getNode();
			    BoundingBox b = (BoundingBox)boundingBoxes.get( n );
			    ifsNumber++;
			    ss.edgeRemovalPolygonReduction( preserveColorBoundaries, xFactor, yFactor, zFactor, percentMarkedEdgesToRemove, minEdgeCount, b, boundingBoxOverlap, useBoundingBox, ifsNumber );  
			}
	
			if ( op.getWhichToReplace() == ReplaceCoordIndex ) {
				replaceCoordIndex( tp, ss, op.getFirstFaceOffset(), op.getLastFaceOffset(), startTokenOffset, endTokenOffset );
			} else if ( op.getWhichToReplace() == ReplaceCoordNodeValue ) {
			    replaceCoordValue( tp, ss, startTokenOffset, endTokenOffset );
			} else {
			    super.optimize( tp, param, startTokenOffset, endTokenOffset );
			}
		}
	}

    
	public void replaceCoordIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset, 
	    int startTokenOffset, int endTokenOffset ) {
	    int scanner = startTokenOffset;
	    dataSource.setState( scanner );
	    BitSet faceBits = ss.getFaceBits();
	    int faceCount = ss.getFaceBitCount();
	    int bitNo = firstFaceOffset;
	    while (( scanner > 0 ) && ( scanner <= endTokenOffset )) {
	        scanner = tp.printNonNumbers( scanner, endTokenOffset );
	        if ( scanner >= endTokenOffset ) {
	            break;
	        }
	        if ( bitNo >= faceCount ) {
	            tp.print( dataSource, scanner, TokenTypes.NumberToken );
	            scanner = dataSource.getNextToken();
	            continue;
	        }
	        boolean printFace = true;
	        if ( faceBits.get( bitNo )) {
	            printFace = false;
	        }
            while ( true ) {
                if ( printFace ) {
       	            tp.print( dataSource, scanner, TokenTypes.NumberToken );
       	        }
   	            boolean isNegativeOne = dataSource.isNegativeOne( scanner );
   	            scanner = dataSource.getNextToken();
   	            if ( scanner >= endTokenOffset ) {
   	                break;
   	            } else if ( isNegativeOne ) {
   	                break;
   	            }
  	            scanner = tp.printNonNumbers( scanner, endTokenOffset );
   	            if ( scanner >= endTokenOffset ) {
   	                break;
   	            }
   	        }
    	    bitNo++;
    	}
	}
	
	public void replaceCoordValue( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
	    int scanner = startTokenOffset;
	    dataSource.setState( scanner );
	    int coordNo = 0;
	    SpaceEntitySet vertices = ss.getEntitySet( SpacePrimitive.Vertex );
	    while (( scanner > 0 ) && ( scanner <= endTokenOffset )) {
	        scanner = tp.printNonNumbers( scanner, endTokenOffset );
	        if ( scanner >= endTokenOffset ) {
	            break;
	        }
	        SpacePrimitive vertex = vertices.getEntity( coordNo );
	        tp.print( vertex.getX() );
	        tp.print( vertex.getY() );
	        tp.print( vertex.getZ() );
//	        System.out.println( coordNo + " : " + vertex.getX() + ", " + vertex.getY() + ", " + vertex.getZ() );
	        scanner = dataSource.getNextToken();
	        scanner = dataSource.getNextToken();
	        scanner = dataSource.getNextToken();
	        coordNo++;
	    }
	}
	
	void createOverlap() {
	    Enumeration bboxes = boundingBoxes.elements();
	    while ( bboxes.hasMoreElements() ) {
	        BoundingBox bb = (BoundingBox)bboxes.nextElement();
	        Vector v = new Vector();
	        boundingBoxOverlap.put( bb, v );
	        Enumeration bboxes2 = boundingBoxes.elements();
	        while ( bboxes2.hasMoreElements() ) {
	            BoundingBox bb2 = (BoundingBox)bboxes2.nextElement();
	            if ( bb != bb2 ) {
	                BoundingBox bo = bb.createOverlap( bb2 );
	                if ( bo != null ) {
	                    v.addElement( bo );
	                }
	            }
	        }
	    }
	}
}


