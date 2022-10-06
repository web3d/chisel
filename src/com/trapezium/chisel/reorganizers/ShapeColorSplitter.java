/*
 * @(#)ShapeColorSplitter.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

import com.trapezium.chisel.*;

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
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.parse.TokenTypes;
import java.util.Vector;
import java.util.BitSet;
import java.io.PrintStream;

/**
 *  This chisel divides an IndexedFaceSet into several IndexedFaceSets
 *  based on the colorIndex, creating one IndexedFaceSet for each color.
 */
public class ShapeColorSplitter extends Optimizer {

    class ColorSplitterParam {
        Node shape;
        Node ifs;
        boolean addBrackets;
        ColorSplitterParam( Node shape, Node ifs ) {
            this.shape = shape;
            this.ifs = ifs;
            VrmlElement shapeParent = shape.getParent();
            addBrackets = false;
            if (( shapeParent.getFirstTokenOffset() == shape.getFirstTokenOffset() ) &&
                ( shapeParent.getLastTokenOffset() == shape.getLastTokenOffset() )) {
                addBrackets = true;
            }
        }
        
        Node getIFS() {
            return( ifs );
        }
        
        boolean doBracket() {
            return( addBrackets );
        }
    }

    
	public ShapeColorSplitter() {
		super( "Shape", "Splitting IndexedFaceSet by color..." );
	}
	
    /** only optimize Shapes that have geometry fields that are
     *  IndexedFaceSets with colorIndex fields and colorPerVertex FALSE
     */
	public void attemptOptimization( Node n ) {
	    Field geometry = n.getField( "geometry" );
	    if ( geometry != null ) {
	        FieldValue fv = geometry.getFieldValue();
	        if ( fv != null ) {
	            VrmlElement c0 = fv.getChildAt( 0 );
	            if ( c0 instanceof Node ) {
	                Node ifs = (Node)c0;
	                if ( ifs.getBaseName().compareTo( "IndexedFaceSet" ) == 0 ) {
	                    Field colorIndex = ifs.getField( "colorIndex" );
	                    Field coordIndex = ifs.getField( "coordIndex" );
	                    Field color = ifs.getField( "color" );
	                    if (( colorIndex != null ) && ( color != null ) && ( coordIndex != null ) && !ifs.getBoolValue( "colorPerVertex" )) {
	                        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), new ColorSplitterParam( n, ifs ));
	                    }
	                }
	            }
	        }
	    }
	}

	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param instanceof ColorSplitterParam ) {
	        ColorSplitterParam csp = (ColorSplitterParam)param;
	        Node ifs = csp.getIFS();
	        boolean addBracket = csp.doBracket();
	        Field colorIndex = ifs.getField( "colorIndex" );
	        Field color = ifs.getField( "color" );
	        FieldValue colorFV = color.getFieldValue();
	        VrmlElement c0 = colorFV.getChildAt( 0 );
	        int colorCount = 0;
	        if ( c0 instanceof Node ) {
	            c0 = c0.getChildAt( 0 );
	            if ( c0 instanceof ExposedField ) {
	                Field ef = (Field)c0;
	                c0 = ef.getFieldValue();
	                if ( c0 instanceof MFFieldValue ) {
	                    MFFieldValue mfv = (MFFieldValue)c0;
	                    colorCount = mfv.getRawValueCount()/3;
	                }
	            }
	        }
	        FieldValue fv = colorIndex.getFieldValue();
	        if ( fv instanceof MFFieldValue ) {
	            MFFieldValue mfv = (MFFieldValue)fv;
	            int count = mfv.getRawValueCount();
	            int[] faceColors = new int[ count];
	            dataSource.setState( mfv.getFirstTokenOffset() );
	            int faceColorIdx = 0;
	            int scanner = dataSource.getCurrentTokenOffset();
	            while ( true ) {
	                scanner = dataSource.skipNonNumbers();
	                faceColors[ faceColorIdx++ ] = dataSource.getIntValue( scanner );
	                scanner = dataSource.getNextToken();
	                if ( faceColorIdx >= count ) {
	                    break;
	                }
	            }
	                
	            if ( addBracket ) {
	                tp.print( "[" );
	            }
	            for ( int i = 0; i < colorCount; i++ ) {
	                System.out.println( "Creating shape " + (i+1) + " of " + colorCount );
	                redoShape( i, tp, startTokenOffset, endTokenOffset, ifs, faceColors );
	            }
	            if ( addBracket ) {
	                tp.print( "]" );
	            }

	        }
	    }
	}

	/** Regenerate the Shape node */
	void redoShape( int colorIdxOffset, TokenPrinter tp, int startTokenOffset, int endTokenOffset, Node ifs, int[] faceColors ) {
	    // print everything from the start of the Shape node to the start of the IFS
	    tp.printRange( startTokenOffset, ifs.getFirstTokenOffset(), false );
	    dataSource.setState( ifs.getFirstTokenOffset() );
	    int scanner = dataSource.getNextToken();
	    Field coordIndex = ifs.getField( "coordIndex" );
	    Field colorIndex = ifs.getField( "colorIndex" );
	    
	    // print everything from the start of the IFS to the start of the coordIndex or colorIndex,
	    // whichever comes first.
	    tp.printRange( scanner, Math.min( coordIndex.getFirstTokenOffset(), colorIndex.getFirstTokenOffset() ), false );
	    
	    // if the color index came first, skip it entirely
	    if ( colorIndex.getFirstTokenOffset() < coordIndex.getFirstTokenOffset() ) {
	        dataSource.setState( colorIndex.getLastTokenOffset() );
	        scanner = dataSource.getNextToken();
	        tp.printRange( scanner, coordIndex.getFirstTokenOffset(), false );
	    }
	    dataSource.setState( coordIndex.getFirstTokenOffset() );
	    scanner = dataSource.getNextToken();
	    int lastCoordIndex = coordIndex.getLastTokenOffset();
	    int faceIdx = 0;
	    int numberFacesPrinted = 0;
	    
	    // now print the coordIndex by face color, skipping faces that
	    // have a different color
	    while ( true ) {
	        scanner = tp.printNonNumbers( scanner, lastCoordIndex );
	        if ( scanner >= lastCoordIndex ) {
	            break;
	        }
	        if ( faceColors[ faceIdx ] == colorIdxOffset ) {
	            scanner = printFace( tp, scanner, lastCoordIndex );
	            numberFacesPrinted++;
	        } else {
	            scanner = skipFace( scanner, lastCoordIndex );
	        }
	        faceIdx++;
	    }
	    // If the old colorIndex hasn't been encountered yet, skip it
	    if ( colorIndex.getFirstTokenOffset() > coordIndex.getLastTokenOffset() ) {
	        scanner = coordIndex.getLastTokenOffset();
	        dataSource.setState( scanner );
	        scanner = dataSource.getNextToken();
   	        tp.printRange( scanner, colorIndex.getFirstTokenOffset() - 1, false );
	        scanner = colorIndex.getLastTokenOffset();
	        dataSource.setState( scanner );
	        scanner = dataSource.getNextToken();
	    }
	    // now print everything right up to the end of the IFS
	    tp.printRange( scanner, ifs.getLastTokenOffset() - 1, false );
	    
	    // now print the colorIndex
	    tp.print( "colorIndex [ " );
	    for ( int i = 0; i < numberFacesPrinted; i++ ) {
	        tp.print( colorIdxOffset );
	    }
	    tp.print( "]" );
	    System.out.println( "Shape " + (colorIdxOffset+1) + ", has " + numberFacesPrinted + " of " + faceIdx + " faces" );
	    
	    // now print everything up to the end
	    tp.printRange( ifs.getLastTokenOffset(), endTokenOffset, false );
	}
	
	int printFace( TokenPrinter tp, int scanner, int endTokenOffset ) {
	    while ( true ) {
	        int type = dataSource.getType( scanner );
	        if ( type == TokenTypes.NumberToken ) {
    	        boolean isNegativeOne = dataSource.isNegativeOne( scanner );
    	        tp.print( dataSource, scanner, TokenTypes.NumberToken );
    	        scanner = dataSource.getNextToken();
    	        if ( isNegativeOne ) {
    	            break;
    	        }
    	    } else {
    	        tp.print( dataSource, scanner, type );
    	        scanner = dataSource.getNextToken();
    	    }
    	    if ( scanner >= endTokenOffset ) {
    	        return( scanner );
    	    }
	    }
	    return( scanner );
	}
	
	int skipFace( int scanner, int endTokenOffset ) {
	    while ( true ) {
	        int type = dataSource.getType( scanner );
	        if ( type == TokenTypes.NumberToken ) {
	            boolean isNegativeOne = dataSource.isNegativeOne( scanner );
	            scanner = dataSource.getNextToken();
	            if ( isNegativeOne ) {
	                return( scanner );
	            }
	        } else {
	            scanner = dataSource.getNextToken();
	        }
	        if ( scanner >= endTokenOffset ) {
	            return( scanner );
	        }
	    }
	}
}


