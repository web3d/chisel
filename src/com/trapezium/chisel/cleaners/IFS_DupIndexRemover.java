/*
 * @(#)IFS_DupIndexRemover.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.chisel.*;

import java.util.BitSet;
import java.io.PrintStream;

/** Removes duplicate values in the coordIndex */
public class IFS_DupIndexRemover extends Optimizer {
    int dupIndexCount = 0;

    class ReplaceInfo {
        Field coordIndex;
        Field texCoordIndex;
        Field colorIndex;
        Field normalIndex;
        
        public ReplaceInfo( Field coordIndex, Field texCoordIndex, Field colorIndex, Field normalIndex ) {
            this.coordIndex = coordIndex;
            this.texCoordIndex = texCoordIndex;
            this.colorIndex = colorIndex;
            this.normalIndex = normalIndex;
        }
        
        public Field getCoordIndex() {
            return( coordIndex );
        }
        
        public Field getTexCoordIndex() {
            return( texCoordIndex );
        }
        
        public Field getColorIndex() {
            return( colorIndex );
        }
        
        public Field getNormalIndex() {
            return( normalIndex );
        }
    }

	public IFS_DupIndexRemover() {
		super( "CoordinateOwner", "Removing duplicate index values..." );
	}

	public void attemptOptimization( Node n ) {
		Field normalIndex = n.getField( "normalIndex" );
		if ( !n.getBoolValue( "normalPerVertex" )) {
		    normalIndex = null;
		}
		Field colorIndex = n.getField( "colorIndex" );
		if ( !n.getBoolValue( "colorPerVertex" )) {
		    colorIndex = null;
		}
		Field texCoordIndex = n.getField( "texCoordIndex" );
		Field coordIndex = n.getField( "coordIndex" );
		if ( coordIndex != null ) {
			replaceRange( coordIndex.getFirstTokenOffset(), coordIndex.getLastTokenOffset(), new ReplaceInfo( coordIndex, texCoordIndex, colorIndex, normalIndex ));
		}
		if ( texCoordIndex != null ) {
		    replaceRange( texCoordIndex.getFirstTokenOffset(), texCoordIndex.getLastTokenOffset(), null );
		}
		if ( colorIndex != null ) {
		    replaceRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(), null );
		}
		if ( normalIndex != null ) {
		    replaceRange( normalIndex.getFirstTokenOffset(), normalIndex.getLastTokenOffset(), null );
		}
	}

    /** Skip a face.
     *
     *  @param scannerOffset offset of a token in a face
     *  @return offset of the last token in the face
     */
    int skipFace( int scannerOffset ) {
        int prevOffset = scannerOffset;
		while ( dataSource.isNumber( scannerOffset )) {
		    if ( dataSource.isNegativeOne( scannerOffset )) {
		        return( scannerOffset );
		    } else if ( dataSource.isRightBracket( scannerOffset )) {
		        return( prevOffset );
		    }
		    prevOffset = scannerOffset;
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
		return( prevOffset );
	}
	
	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param == null ) {
	        return;
	    }
	    ReplaceInfo ri = (ReplaceInfo)param;
	    Field coordIndex = ri.getCoordIndex();
	    FieldValue fv = coordIndex.getFieldValue();
	    MFFieldValue mfv = (MFFieldValue)fv;
	    int count = mfv.getRawValueCount();
	    BitSet numberUsageBits = new BitSet( count );
		int scannerOffset = startTokenOffset;
		int numberOffset = 0;
		while ( scannerOffset != -1 ) {
			if ( dataSource.isNumber( scannerOffset )) {
				numberOffset = printFace( tp, scannerOffset, numberUsageBits, numberOffset, endTokenOffset );
				scannerOffset = skipFace( scannerOffset );
			} else {
				tp.print( dataSource, scannerOffset );
			}
			if ( scannerOffset == endTokenOffset ) {
				break;
			}
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
		Field texCoordIndex = ri.getTexCoordIndex();
		if ( texCoordIndex != null ) {
		    printIndexValues( tp, texCoordIndex, numberUsageBits, numberOffset );
		}
		Field colorIndex = ri.getColorIndex();
		if ( colorIndex != null ) {
		    printIndexValues( tp, colorIndex, numberUsageBits, numberOffset );
		}
		Field normalIndex = ri.getNormalIndex();
		if ( normalIndex != null ) {
		    printIndexValues( tp, normalIndex, numberUsageBits, numberOffset );
		}
	}

    void printIndexValues( TokenPrinter tp, Field indexField, BitSet usageBits, int usageBitsSize ) {
        int numberOffset = 0;
        int scannerOffset = indexField.getFirstTokenOffset();
        int endTokenOffset = indexField.getLastTokenOffset();
        dataSource.setState( scannerOffset );
		while ( scannerOffset != -1 ) {
		    int type = dataSource.getType( scannerOffset );
			if ( type == TokenTypes.NumberToken ) {
			    if (( numberOffset < usageBitsSize ) && usageBits.get( numberOffset )) {
			        tp.print( dataSource, scannerOffset, type );
			    }
			    numberOffset++;
			} else {
				tp.print( dataSource, scannerOffset, type );
			}
			if ( scannerOffset == endTokenOffset ) {
				break;
			}
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
    }

    /** 
     *  Print a face, but don't print repeated coordIndex values
     *
     *  @param tp TokenPrinter to print to
     *  @param scannerOffset where the index values for the face begin
     *  @param numberUsageBits BitSet that keeps track of which index values
     *      are preserved
     *  @param numberOffset current offset into the numberUsageBits
     *  @param endTokenOffset last possible token in the coordIndex list
     *
     *  @return the new numberOffset value
     */
	public int printFace( TokenPrinter tp, int scannerOffset, BitSet numberUsageBits, int numberOffset, int endTokenOffset ) {
		int numberEntries = getNumberEntries( scannerOffset, endTokenOffset );
		if ( numberEntries > 0 ) {
    		int[] values = new int[ numberEntries ];
    		int startOffset = scannerOffset;
    		for ( int i = 0; i < numberEntries; i++ ) {
    			values[i] = dataSource.getIntValue( scannerOffset );
    			scannerOffset = dataSource.getNextToken( scannerOffset );
    		}
    		/** Only remove consecutive repeated index values */
    		for ( int i = 0; i < ( numberEntries - 1 ); i++ ) {
    			for ( int j = i + 1; j < numberEntries; j++ ) {
    				if ( values[i] == values[j] ) {
    					values[j] = -2;
    				} else {
    				    break;
    				}
    			}
    		}
    		/** Handle case where first and last are same (remove last) */
    		if ( values[0] == values[ numberEntries - 1 ]) {
    		    values[ numberEntries - 1 ] = -2;
    		}
    		for ( int i = 0; i < numberEntries; i++ ) {
    			if ( values[i] == -2 ) {
    			    dupIndexCount++;
    				continue;
    			}
    			tp.print( values[i] );
    			numberUsageBits.set( numberOffset + i );
    		}
    	}
   		tp.print( -1 );
   		numberUsageBits.set( numberOffset + numberEntries );
   		numberOffset += numberEntries;
   		numberOffset++;
		return( numberOffset );
	}

    /** Get the number of digits until a -1 is reached.
     *
     *  @param scannerOffset where to start counting digits
     *  @param endTokenOffset don't go past here counting
     *
     *  @return  the number of consecutive number entries, until a -1 is
     *     reached.
     */
	int getNumberEntries( int scannerOffset, int endTokenOffset ) {
		int count = 0;
		while ( dataSource.isNumber( scannerOffset )) {
			if ( dataSource.getIntValue( scannerOffset ) == -1 ) {
				break;
			}
			count++;
			scannerOffset = dataSource.getNextToken( scannerOffset );
			if (( scannerOffset >= endTokenOffset ) || ( scannerOffset == -1 )) {
			    break;
			}
		}
		return( count );
	}


	public void summarize( PrintStream ps ) {
	    if ( dupIndexCount == 0 ) {
	        ps.println( "Removed no index values." );
	    } else if ( dupIndexCount == 1 ) {
	        ps.println( "Removed one index value." );
	    } else {
	        ps.println( "Removed " + dupIndexCount + " index values." );
	    }
	}
}


