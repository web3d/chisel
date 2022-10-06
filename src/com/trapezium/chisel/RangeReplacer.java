/*
 * @(#)RangeReplacer.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.edit.TokenEditor;
import com.trapezium.vrml.NodeSelection;
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.util.ProgressIndicator;

import java.util.Vector;
import java.util.Hashtable;
import java.util.BitSet;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *  The RangeReplacer handles replacement of sequences of tokens.  Each
 *  sequence to be replaced is registered for replacement, along with the
 *  Optimizer object handling the replacement and a parameter that gets
 *  passed to the Optimizer object.
 *
 *  NOTE: this does not handle embedded replacements.  Only the first
 *  replacement range found is handled.
 */

public class RangeReplacer {
    // the TokenEnumerator required by the offsets
    TokenEditor tokenEditor;
    
    // the start and end offset of each range that is to be replaced
    int[] startOffsets;
    int[] endOffsets;
    int startEndBoundary;
    int startEndIdx;
   
	// Vector of Optimizer objects
	Vector chisels;

	// Vector of parameters to Optimizer "optimize" methods
	Vector params;

	// the start and end ranges to be replaced at the end of the file
	int[] eofStartTokens;
	int[] eofEndTokens;
	int eofBoundary;
	int eofIdx;

	public RangeReplacer() {
	    tokenEditor = null;
		startOffsets = new int[100];
		endOffsets = new int[100];
		startEndBoundary = 100;
		startEndIdx = 0;
		chisels = new Vector();
		params = new Vector();
		eofStartTokens = null;
		eofEndTokens = null;
	}

	/** Reset all internal info for this object, and reset all optimizers it used,
	 *  since these may keep around very large objects that need to be garbage
	 *  collected.
	 */
	public void wipeout() {
	    tokenEditor = null;
	    int numberChisels = chisels.size();
	    for ( int i = 0; i < numberChisels; i++ ) {
	        Optimizer o = (Optimizer)chisels.elementAt( i );
	        o.reset();
	    }
	}

    /** Register a range of tokens for replacement using a particular optimizer.
     *
     *  @param replacer the Optimizer responsible for generating replacement tokens
     *  @param startTokenOffset the first token in the range to be replaced
     *  @param endTokenOffset the last token in the range to be replaced
     *  @param param the object, possibly null, passed to the optimizer when the
     *     range is actually being replaced.
     */
	public void replaceRange( Optimizer replacer, int startTokenOffset, int endTokenOffset, Object param ) {
		chisels.addElement( replacer );
		addStartEnd( startTokenOffset, endTokenOffset );
		params.addElement( param );
	}
	
	/** Check if any replacements were registered */
	public boolean replacementsRegistered() {
	    return( startEndIdx > 0 );
	}
	
	public void replaceStartEnd( int oldStartOffset, int oldEndOffset, int newStartOffset, int newEndOffset ) {
	    for ( int i = 0; i < startEndIdx; i++ ) {
	        if (( oldStartOffset == startOffsets[i] ) && ( oldEndOffset == endOffsets[i] )) {
	            startOffsets[i] = newStartOffset;
	            endOffsets[i] = newEndOffset;
	            return;
	        }
	    }
	}

    /** Save the start/end token range that is going to be replaced.
     *
     *  @param start first token in range being replaced
     *  @param end last token in range being replaced.
     */
    void addStartEnd( int start, int end ) {
        // this occurs if item being replaced is a copy from a PROTO
        if ( end == -1 ) {
            return;
        }
        if ( startEndIdx >= startEndBoundary ) {
            int[] temp = new int[ startEndBoundary * 2 ];
            System.arraycopy( startOffsets, 0, temp, 0, startEndBoundary );
            startOffsets = temp;
            temp = new int[ startEndBoundary * 2 ];
            System.arraycopy( endOffsets, 0, temp, 0, startEndBoundary );
            endOffsets = temp;
            startEndBoundary *= 2;
        }
        startOffsets[ startEndIdx ] = start;
        endOffsets[ startEndIdx ] = end;
        startEndIdx++;
    }
    
	public void eofTokens( int startTokenOffset, int endTokenOffset ) {
	    if ( eofStartTokens == null ) {
	        eofStartTokens = new int[ 100 ];
	        eofEndTokens = new int[ 100 ];
	        eofBoundary = 100;
	        eofIdx = 0;
	    }
	    if ( eofIdx >= eofBoundary ) {
	        int[] temp = new int[ eofBoundary * 2 ];
	        System.arraycopy( eofStartTokens, 0, temp, 0, eofBoundary );
	        eofStartTokens = temp;
	        temp = new int[ eofBoundary * 2 ];
	        System.arraycopy( eofEndTokens, 0, temp, 0, eofBoundary );
	        eofEndTokens = temp;
	        eofBoundary *= 2;
	    }
	    eofStartTokens[ eofIdx ] = startTokenOffset;
	    eofEndTokens[ eofIdx ] = endTokenOffset;
	    eofIdx++;
	}

    /** See if a tokenOffset is a key start value */
    boolean optimized = false;
    Hashtable optimizedLookup = null;
    BitSet optimizedSettings = null;
    void optimizeData( int largestOffset ) {
        if ( startEndIdx > 40 ) {
            optimizedSettings = new BitSet( largestOffset );
            optimizedLookup = new Hashtable();
            for ( int i = 0; i < startEndIdx; i++ ) {
                optimizedSettings.set( startOffsets[i] );
                optimizedLookup.put( new Integer( startOffsets[i] ), new Integer( i ));
            }
            optimized = true;
        }
    }
    
	int startTokenIdx( int tokenOffset ) {
	    if ( optimized ) {
	        Integer keyValue = (Integer)optimizedLookup.get( new Integer( tokenOffset ));
	        if ( keyValue != null ) {
	            return( keyValue.intValue() );
	        }
	    } else {
       	    for ( int i = 0; i < startEndIdx; i++ ) {
       	        if ( startOffsets[i] == tokenOffset ) {
       	            return( i );
       	        }
       	    }
       	}
	    return( -1 );
	}
	
	/** Get the token offset that is greater than or equal to this one,
	 *  but is less than the next largest token in the startOffsets list.
	 */
	int getNextLargest( int tokenOffset, int largestPossible ) {
	    if ( optimized ) {
	        for ( int i = tokenOffset + 1; i < largestPossible; i++ ) {
	            if ( optimizedSettings.get( i )) {
	                return( i - 1 );
	            }
	        }
	        return( largestPossible - 1 );
	    } else {
    	    int result = largestPossible;
    	    for ( int i = 0; i < startEndIdx; i++ ) {
    	        if (( startOffsets[i] < result ) && ( startOffsets[i] > tokenOffset )) {
    	            result = startOffsets[i];
    	        }
    	    }
    	    return( result - 1 );
    	}
	}
		
	public void writeFile( String fileName, TokenEnumerator t ) { 
	    TokenEnumerator tokenEnumerator = t;
		try {
			System.out.println( "Writing file " + fileName );
			FileOutputStream f = new FileOutputStream( fileName );
			PrintStream ps = new PrintStream( f );
			TokenPrinter tp = new TokenPrinter( ps, t );
			int tokenScanner = 0; // start at token 0
			NodeSelection nodeSelection = NodeSelection.getSingleton();
    		boolean useNodeSelection = ( nodeSelection.firstNodeToken != -1 );
    		optimizeData( tokenEnumerator.getNumberTokens() );
    		int loopCount = 0;
			while ( tokenScanner != -1 ) {
			    int index = startTokenIdx( tokenScanner );
			    if (( loopCount == 1 ) && ( index == -1 )) {
			        index = startTokenIdx( 1 );
			    }
				if ( index != -1 ) {
					Optimizer o = (Optimizer)chisels.elementAt( index );
					if ( o.optimizePossible( params.elementAt( index )) &&
					    (( !useNodeSelection ) ||
					     (( startOffsets[ index ] >= nodeSelection.firstNodeToken ) &&
					      ( endOffsets[ index ] <= nodeSelection.lastNodeToken )))) {
   						o.optimize( tp, params.elementAt( index ), 
    						startOffsets[ index ], endOffsets[ index ] );
		    			tokenScanner = endOffsets[ index ];
					} else {
						tp.print( tokenEnumerator, tokenScanner );
					}
				} else {
				    int nextTokenScanner = getNextLargest( tokenScanner, tokenEnumerator.getNumberTokens() );
				    tp.printRange( tokenScanner, nextTokenScanner, false );
				    tokenScanner = nextTokenScanner;
				}
				loopCount++;
				tokenScanner = tokenEnumerator.getNextToken( tokenScanner );
			}
			tp.flush();
			for ( int i = 0; i < eofIdx; i++ ) {
				tp.printRange( eofStartTokens[i], eofEndTokens[i], false );
				tp.flush();
			}
			printFinalChiselInfo( tp );
			ps.close();
			f.close();
		} catch ( Exception e ) {
			System.out.println( "Unable to create output file '" + fileName + "'" );
			e.printStackTrace();
		}
		System.out.println( "Chisel completed." );
	}

    /** Recreate a token stream, assume new stream nearly same size as old stream.
     *  If there was nothing to do, just returns original.
     *
     *  @param action String to be displayed during progress reporting
     *  @param originalTokenEditor original tokenStream
     *  @param firstTokenOffset first token in range being re-created
     *  @param lastTokenOffset last token in range being re-created
     *  @param frl progress reporter
     *
     *  @returns the TokenEditor containing the re-creation of the original token stream,
     *    If no replacements are registered, just returns original token stream without
     *    doing anything.
     *    Returns null if there is an error or the re-creation is aborted
     */
	public TokenEditor recreateTokenStream( String action, TokenEditor originalTokenEditor, int firstTokenOffset, int lastTokenOffset, ProgressIndicator frl ) {
		TokenEditor te = new TokenEditor( originalTokenEditor.getByteArraySize(), originalTokenEditor.getTokenArraySize(), originalTokenEditor.getLineArraySize() );
		te.setFileUrl( originalTokenEditor.getFileUrl() );
		tokenEditor = originalTokenEditor;
		tokenEditor.notifyLineNumbers( frl );
		if ( frl != null ) {
    		frl.setTitle( action );
    	}
		int tokenScanner = firstTokenOffset;
		tokenEditor.setState( tokenScanner );
		int lastLine = 0;
		NodeSelection nodeSelection = NodeSelection.getSingleton();
		boolean useNodeSelection = ( nodeSelection.firstNodeToken != -1 );
		try {
			TokenPrinter tp = new TokenPrinter( originalTokenEditor, te );
			optimizeData( originalTokenEditor.getNumberTokens() );
			int loopCount = 0;
			while ( tokenScanner != -1 ) {
			    if ( GlobalProgressIndicator.abortCurrentProcess ) {
			        return( null );
			    }
			    int index = startTokenIdx( tokenScanner );
			    if (( loopCount == 1 ) && ( index == -1 )) {
			        // handle case for PROTOs at start
			        index = startTokenIdx( 1 );
			    }
				if ( index != -1 ) {
					Optimizer o = (Optimizer)chisels.elementAt( index );
					if ( o.optimizePossible( params.elementAt( index )) &&
					    (( !useNodeSelection ) ||
					     (( startOffsets[ index ] >= nodeSelection.firstNodeToken ) &&
					      ( endOffsets[ index ] <= nodeSelection.lastNodeToken )))) {
   						o.optimize( tp, params.elementAt( index ), 
    						startOffsets[ index ], endOffsets[ index ] );
		    			tokenScanner = endOffsets[ index ];
 					} else {
						tp.print( tokenEditor, tokenScanner );
					}
				} else {
				    int nextTokenScanner = getNextLargest( tokenScanner, originalTokenEditor.getNumberTokens() );
					tp.printRange( tokenScanner, nextTokenScanner, false );
					tokenScanner = nextTokenScanner;
				}
				tokenScanner = tokenEditor.getNextToken( tokenScanner );
				loopCount++;
			}
			tp.flush();
			for ( int i = 0; i < eofIdx; i++ ) {
				tp.printRange( eofStartTokens[i], eofEndTokens[i], false );
				tp.flush();
			}
			printFinalChiselInfo( tp );
	//		te.retokenize();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		return( te );
	}
	
	/** Print final info for all chisels */
	void printFinalChiselInfo( TokenPrinter tp ) {
		int numberChisels = chisels.size();
		Hashtable singleuse = new Hashtable();
		for ( int i = 0; i < numberChisels; i++ ) {
		    Optimizer o = (Optimizer)chisels.elementAt( i );
		    if ( singleuse.get( o ) == null ) {
    		    if ( o.hasFinalCode() ) {
    		        o.printFinalCode( tp );
    		    }
    		    singleuse.put( o, o );
    		}
		}
		tp.flush();
    }	
}

