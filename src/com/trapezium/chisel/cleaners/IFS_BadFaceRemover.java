/*
 * @(#)IFS_BadFaceRemover.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.chisel.*;

import java.util.BitSet;

/**
 *  Remove any faces with less than 3 sides.
 */
 
public class IFS_BadFaceRemover extends Optimizer {

    class BadFaceParam {
        Field coordIndex;
        Field texCoordIndex;
        Field colorIndex;
        boolean replaceColorIndex;
        boolean colorPerVertex;
        Field normalIndex;
        boolean replaceNormalIndex;
        boolean normalPerVertex;
        Field color;
        boolean replaceColor;
        
        public BadFaceParam( Field coordIndex, Field texCoordIndex, 
            Field color, boolean replaceColor,
            Field colorIndex, boolean replaceColorIndex, boolean colorPerVertex,
            Field normalIndex, boolean replaceNormalIndex, boolean normalPerVertex ) {
            this.coordIndex = coordIndex;
            this.texCoordIndex = texCoordIndex;
            this.color = color;
            this.replaceColor = replaceColor;
            this.colorPerVertex = colorPerVertex;
            this.colorIndex = colorIndex;
            this.replaceColorIndex = replaceColorIndex;
            this.normalIndex = normalIndex;
            this.replaceNormalIndex = replaceNormalIndex;
            this.normalPerVertex = normalPerVertex;
        }
        
        public Field getTexCoordIndex() {
            return( texCoordIndex );
        }
        
        public Field getCoordIndex() {
            return( coordIndex );
        }

        /** Get the <B>color</B> field for replacement.
         *
         *  @return the "color" field of the node, or null if the color
         *    field is not affected by the bad face removal.
         */
        public Field getColor() {
            if ( replaceColor ) {
                return( color );
            } else {
                return( null );
            }
        }
        
        public Field getColorIndex() {
            if ( replaceColorIndex ) {
                return( colorIndex );
            } else {
                return( null );
            }
        }
        
        public boolean getColorPerVertex() {
            return( colorPerVertex );
        }
        
        public boolean getNormalPerVertex() {
            return( normalPerVertex );
        }
        
        public Field getNormalIndex() {
            if ( replaceNormalIndex ) {
                return( normalIndex );
            } else {
                return( null );
            }
        }
    }
    
	public IFS_BadFaceRemover() {
		super( "IndexedFaceSet", "Removing degenerate faces..." );
	}

	public void attemptOptimization( Node n ) {
		Field coordIndex = n.getField( "coordIndex" );
		Field texCoordIndex = n.getField( "texCoordIndex" );
		Field color = n.getField( "color" );
		Field colorIndex = n.getField( "colorIndex" );
		boolean replaceColor = (( colorIndex == null ) && ( color != null ) && !n.getBoolValue( "colorPerVertex" ));
		boolean replaceColorIndex = ( colorIndex != null );
       	Field normalIndex = n.getField( "normalIndex" );
    	boolean replaceNormalIndex = ( normalIndex != null );
    	
		if ( coordIndex != null ) {
			replaceRange( coordIndex.getFirstTokenOffset(), coordIndex.getLastTokenOffset(), 
    			new BadFaceParam( coordIndex, texCoordIndex, 
    			    color, replaceColor, colorIndex, replaceColorIndex, n.getBoolValue( "colorPerVertex" ),
    			    normalIndex, replaceNormalIndex, n.getBoolValue( "normalPerVertex" )));
		}
		if ( texCoordIndex != null ) {
		    replaceRange( texCoordIndex.getFirstTokenOffset(), texCoordIndex.getLastTokenOffset(), null );
		}
		if ( replaceColor ) {
		    replaceRange( color.getFirstTokenOffset(), color.getLastTokenOffset(), null );
		}
		if ( replaceColorIndex ) {
		    replaceRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(), null );
		}
		if ( replaceNormalIndex ) {
		    replaceRange( normalIndex.getFirstTokenOffset(), normalIndex.getLastTokenOffset(), null );
		}
	}

	/** Replace the coordIndex, get rid of bad faces, regenerate other fields */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    // null param indicates texCoordIndex, color, colorIndex, normalIndex,
	    // do nothing here causes no tokens to be generated for these.
	    // Tokens for these are generated as part of coordIndex
	    if ( param == null ) {
	        return;
	    }
	    BadFaceParam bfp = (BadFaceParam)param;
	    Field texCoordIndex = bfp.getTexCoordIndex();
		int scannerOffset = startTokenOffset;
		int faceIdx = 0;
		Field coordIndex = bfp.getCoordIndex();
		FieldValue coordIndexValues = coordIndex.getFieldValue();
		BitSet faceBits = null;
		int approxNumberFaces = 0;
		if ( coordIndexValues instanceof MFFieldValue ) {
		    approxNumberFaces = (((MFFieldValue)coordIndexValues).getRawValueCount() )/2;
		    faceBits = new BitSet( approxNumberFaces );
		}
		while ( true ) {
			if ( dataSource.isNumber( scannerOffset )) {
				if ( printFace( tp, scannerOffset )) {
				    if ( faceBits != null ) {
				        if ( faceIdx < approxNumberFaces ) {
        				    faceBits.set( faceIdx );
        				}
    				}
				}
				faceIdx++;
				while ( dataSource.isNumber( scannerOffset ) && dataSource.getIntValue( scannerOffset ) >= 0 ) {
					scannerOffset = dataSource.getNextToken( scannerOffset );
				}
			} else {
				tp.print( dataSource, scannerOffset );
			}
			if ( scannerOffset == endTokenOffset ) {
				break;
			}
			scannerOffset = dataSource.getNextToken( scannerOffset);
		}
		int validFaceCount = 0;
		for ( int i = 0; i < faceIdx; i++ ) {
		    if ( faceBits.get( i )) {
		        validFaceCount++;
		    }
		}
		// do texCoordIndex if there is one
		if ( texCoordIndex != null ) {
		    printVertexBasedIndex( tp, startTokenOffset, texCoordIndex );
	    }
	    Field color = bfp.getColor();
	    if ( color != null ) {
	        printFaceValues( tp, faceBits, color );
	    }
    	Field colorIndex = bfp.getColorIndex();
    	if ( colorIndex != null ) {
    	    if ( bfp.getColorPerVertex() ) {
    	        printVertexBasedIndex( tp, startTokenOffset, colorIndex );
    	    } else {
        	    printFaceBasedIndex( tp, faceBits, colorIndex );
        	}
    	}
    	Field normalIndex = bfp.getNormalIndex();
    	if ( normalIndex != null ) {
    	    if ( bfp.getNormalPerVertex() ) {
    	        printVertexBasedIndex( tp, startTokenOffset, normalIndex );
    	    } else {
        	    printFaceBasedIndex( tp, faceBits, normalIndex );
        	}
	    }
	}

    /** Print index fields that have one entry for vertex, corresponding to coordIndex.
     */
    void printVertexBasedIndex( TokenPrinter tp, int startTokenOffset, Field indexField ) {
	    int scannerOffset = startTokenOffset;
	    int indexScannerOffset = indexField.getFirstTokenOffset();
		int endTokenOffset = indexField.getLastTokenOffset();
	    while( true ) {
   			if ( dataSource.isNumber( indexScannerOffset )) {
   			    while ( !dataSource.isNumber( scannerOffset )) {
   			        if ( scannerOffset == -1 ) {
   			            break;
   			        }
   			        scannerOffset = dataSource.getNextToken( scannerOffset );
   			    }
   			    if ( getNumberEntries( scannerOffset ) >= 3 ) {
       				printFace( tp, indexScannerOffset );
       			}
       			while ( dataSource.isNumber( scannerOffset ) && dataSource.getIntValue( scannerOffset ) >= 0 ) {
       			    scannerOffset = dataSource.getNextToken( scannerOffset );
       			}
   				while ( dataSource.isNumber( indexScannerOffset ) && dataSource.getIntValue( indexScannerOffset ) >= 0 ) {
   					indexScannerOffset = dataSource.getNextToken( indexScannerOffset );
   				}
   			} else {
   				tp.print( dataSource, indexScannerOffset );
   			}
   			if ( indexScannerOffset == endTokenOffset ) {
   				break;
   			}
   			indexScannerOffset = dataSource.getNextToken( indexScannerOffset);
   			scannerOffset = dataSource.getNextToken( scannerOffset );
    	}
    }
    
    /** Print values for all faces that exist after bad face removal.
     */
	void printFaceValues( TokenPrinter tp, BitSet faceBits, Field values ) {
   	    int scannerOffset = values.getFirstTokenOffset();
   	    int endTokenOffset = values.getLastTokenOffset();
   	    dataSource.setState( scannerOffset );
   	    int faceNo = 0;
   	    while ( true ) {
   	        scannerOffset = tp.printNonNumbers( scannerOffset, endTokenOffset );
   	        if ( scannerOffset >= endTokenOffset ) {
   	            break;
   	        }
   	        if ( faceBits.get( faceNo )) {
   	            // print R,G,B
   	            tp.print( dataSource, scannerOffset, TokenTypes.NumberToken );
   	            scannerOffset = dataSource.getNextToken();
   	            scannerOffset = tp.printNonNumbers( scannerOffset, endTokenOffset );
   	            tp.print( dataSource, scannerOffset, TokenTypes.NumberToken );
   	            scannerOffset = dataSource.getNextToken();
   	            scannerOffset = tp.printNonNumbers( scannerOffset, endTokenOffset );
   	            tp.print( dataSource, scannerOffset, TokenTypes.NumberToken );
   	        } else {
   	            // skip R,G,B
   	            scannerOffset = dataSource.getNextToken();
   	            scannerOffset = dataSource.skipNonNumbers();
   	            scannerOffset = dataSource.getNextToken();
   	            scannerOffset = dataSource.skipNonNumbers();
   	        }
   	            
   	        faceNo++;
   	        scannerOffset = dataSource.getNextToken();
   	        if ( scannerOffset > endTokenOffset ) {
   	            break;
   	        }
   	    }
   	}

    /** Print index field that has one index entry per face.
     * 
     *  @param tp TokenPrinter to print to
     *  @param faceBits which faces to print
     *  @param index index field to use as data source
     */
	void printFaceBasedIndex( TokenPrinter tp, BitSet faceBits, Field index ) {
   	    int scannerOffset = index.getFirstTokenOffset();
   	    int endTokenOffset = index.getLastTokenOffset();
   	    dataSource.setState( scannerOffset );
   	    int faceNo = 0;
   	    int printedFaceCount = 0;
   	    while ( true ) {
   	        scannerOffset = tp.printNonNumbers( scannerOffset, endTokenOffset );
   	        if ( scannerOffset >= endTokenOffset ) {
   	            break;
   	        }
   	        if ( faceBits.get( faceNo )) {
   	            tp.print( dataSource, scannerOffset, TokenTypes.NumberToken );
   	            printedFaceCount++;
   	        } 
   	        faceNo++;
   	        scannerOffset = dataSource.getNextToken();
   	        if ( scannerOffset > endTokenOffset ) {
   	            break;
   	        }
   	    }
   	}
   	
    /** Print a face if it has at least 3 edges.
     *
     *  @param tp  TokenPrinter to print on
     *  @param scannerOffset  start token of the face
     *
     *  @return true if the face is printed, otherwise false
     */
	public boolean printFace( TokenPrinter tp, int scannerOffset ) {
		int numberEntries = getNumberEntries( scannerOffset );
		if ( numberEntries < 3 ) {
			return( false );
		}
		for ( int i = 0; i < numberEntries; i++ ) {
			tp.print( dataSource, scannerOffset );
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
		tp.print( -1 );
		return( true );
	}

	int getNumberEntries( int scannerOffset ) {
		int count = 0;
		while ( dataSource.isNumber( scannerOffset )) {
			if ( dataSource.isNegativeOne( scannerOffset )) {
				break;
			}
			count++;
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
		return( count );
	}
}


