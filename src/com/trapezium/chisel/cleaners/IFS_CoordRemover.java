/*
 * @(#)IFS_CoordRemover.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.UsageInfo;
import com.trapezium.vrml.node.IndexInfo;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.node.generated.IndexedFaceSet;
import com.trapezium.chisel.*;

import java.util.BitSet;
import java.util.Hashtable;
import java.io.PrintStream;

//
//  Base class for any optimizer that removes "coord" entries from an IndexedFaceSet
//  or IndexedLineSet.
//
//  Collects "coord", "coordIndex" ranges to optimize.  Looks up any dependencies with
//  normal/normalIndex, color/colorIndex, or texCoord/texCoordIndex.  Replace range
//  handles all these.
//
public class IFS_CoordRemover extends Optimizer {
    static public int Coord = 0;
    static public int CoordIndex = 1;
    static public int TexCoord = 2;
    static public int TexCoordIndex = 3;
    static public int Color = 4;
    static public int ColorIndex = 5;
    static public int Normal = 6;
    static public int NormalIndex = 7;

	protected int numberCoordsRemoved = 0;
	protected int numberTexCoordsRemoved = 0;
	protected int numberNormalsRemoved = 0;
	protected int numberColorsRemoved = 0;

	public IFS_CoordRemover() {
		super( "CoordinateOwner", "Removing unused values..." );
	}

    //
    // Just records optimization for coord/coordIndex, plus any dependent nodes based
    // on the IndexInfo associated with the node.
    //
	public void attemptOptimization( Node n ) {
		Field coord = n.getField( "coord" );
		Field coordIndex = n.getField( "coordIndex" );
		if (( coord != null ) && ( coordIndex != null )) {
		    MFFieldValue mfv2 = (MFFieldValue)coordIndex.getFieldValue();
		    int coordIndexCount = 0;
		    if ( mfv2 != null ) {
		        coordIndexCount = mfv2.getRawValueCount();
		        if ( coordIndexCount == 0 ) {
		            return;
		        }
		    } else {
		        return;
		    }

			FieldValue fv = coord.getFieldValue();
			Node coordNode = (Node)fv.getChildAt( 0 );
			if ( coordNode instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)coordNode;
				coordNode = dun.getNode();
			}
			if ( coordNode != null ) {
			    BitSet coordBits = null;
				Scene s = (Scene)n.getScene();
				if ( s != null ) {
				    Hashtable usageTable = s.getUsageTable();
            		UsageInfo ui = (UsageInfo)usageTable.get( coordNode );
            		if ( ui != null ) {
        		        coordBits = ui.getUsageBits();
        		    }
        		    if ( coordBits == null ) {
        		        return;
        		    }
        		    int numberFaces = 0;
        		    if ( n instanceof IndexedFaceSet ) {
        		        numberFaces = ((IndexedFaceSet)n).getNumberFaces();
        		    } 

            		Hashtable texCoordTable = s.getTexCoordTable();
            		IndexInfo texInfo = (IndexInfo)texCoordTable.get( n );
            		synchWithCoord( texInfo, TexCoord, coordBits, coordIndexCount, numberFaces );

            		Hashtable colorTable = s.getColorTable();
            		IndexInfo colorInfo = (IndexInfo)colorTable.get( n );
            		synchWithCoord( colorInfo, Color, coordBits, coordIndexCount, numberFaces );

            		Hashtable normalTable = s.getNormalTable();
            		IndexInfo normalInfo = (IndexInfo)normalTable.get( n );
            		synchWithCoord( normalInfo, Normal, coordBits, coordIndexCount, numberFaces );
            	}

				Field coords = coordNode.getField( "point" );
				FieldValue coordFieldValue = coords.getFieldValue();
				int valcount = 0;
				if ( coordFieldValue instanceof MFFieldValue ) {
				    MFFieldValue mfv = (MFFieldValue)coordFieldValue;
				    valcount = mfv.getRawValueCount();
				}
				 
				if ( valcount > 0 ) {
    				replaceRange( coords.getFirstTokenOffset(), 
    				    coords.getLastTokenOffset(), 
    				    new CoordRemoveData( Coord, null, coordBits, coordIndexCount, 0 ));
	    			replaceRange( coordIndex.getFirstTokenOffset(), 
	    			    coordIndex.getLastTokenOffset(), 
	    			    new CoordRemoveData( CoordIndex, null, coordBits, coordIndexCount, 0 ));
	    		}
			}
		}
	}

    /** Synchronize IndexInfo with coord info.
     *
     *  There are three types of synchronization:
     *  1.  values are synched with coord values.  This means the "perVertex"
     *      setting is true, and there is no "..Index" field.
     *  2.  values are synched with index.  This means there is an "..Index" field,
     *      with values in synch with the corresponding value node.  There are two
     *      mutually exclusive further restrictions on the "..Index" field:
     *      a) the "..Index" field must be in synch with with the coord index,
     *         which happens when the "perVertex" setting is true.
     *      b) the "..Index" field must be in synch with the coord faces, which
     *         happens when the "perVertex" setting is false.
     *
     *  @param ii  IndexInfo contains relationship between value and index fields
     *     generic for texture, color, normal.  The corresponding fields are:
     *     texCoord/texCoordIndex, color/colorIndex/colorPerVertex,
     *     normal/normalIndex/normalPerVertex.
     *  @param dataType the type of field being removed:  Coord, CoordIndex,
     *     TexCoord, TexCoordIndex, Color, ColorIndex, Normal, NormalIndex.  
     *     These constants follow the convention that the "Index" constant
     *     is one greater than the "Value" constant.
     *  @param coordBits BitSet indicating which coordinates are in use
     *  @param coordIndexCount count of the number of index values, needed for
     *     truncating index lists that are too long.  This occurs if texCoordIndex
     *     has more values, or if colorIndex/normalIndex have more values and
     *     the colorPerVertex/normalPerVertex is true.
     *  @param numberFaces number of faces, which must match colorIndex/normalIndex
     *     if colorPerVertex/normalPerVertex is false.
     */
    void synchWithCoord( IndexInfo ii, int dataType, BitSet coordBits, int coordIndexCount, int numberFaces ) {
        if ( ii == null ) {
            return;
        }
        if ( coordBits == null ) {
            return;
        }
	    if ( ii.isValueSynchedWithCoord() ) {
	        Node valueNode = ii.getValueNode();
	        replaceRange( valueNode.getFirstTokenOffset(), 
	            valueNode.getLastTokenOffset(), 
	            new CoordRemoveData( dataType, ii, coordBits, coordIndexCount, 0 ));
	    } else if ( ii.isValueSynchedWithIndex() ) {
	        Node valueNode = ii.getValueNode();
	        MFFieldValue index = ii.getIndexValue();
	        coordBits = ii.getUsageBits();
	        if ( coordBits != null ) {
	            replaceRange( valueNode.getFirstTokenOffset(), 
	                valueNode.getLastTokenOffset(), 
	                new CoordRemoveData( dataType, ii, coordBits, coordIndexCount, 0 ));
	            if ( ii.isPerVertex() ) {
	                numberFaces = 0;
	            }
	            replaceRange( index.getFirstTokenOffset(), index.getLastTokenOffset(), 
	                new CoordRemoveData( dataType+1, ii, coordBits, coordIndexCount, numberFaces ));
	        }
	    }
	}

	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		CoordRemoveData cmd = (CoordRemoveData)param;
		if ( cmd.isCoord() ) {
		    removeCoord( tp, cmd, startTokenOffset, endTokenOffset );
		} else {
		    removeIndex( tp, cmd, startTokenOffset, endTokenOffset );
		}
	}

	/** remove coord, texCoord, color, or normal entries based on the usage bitset */
	void removeCoord( TokenPrinter tp, CoordRemoveData cmd, int startTokenOffset, int endTokenOffset ) {
	    BitSet keep = cmd.getCoordsToKeep();
	    int unitCount = cmd.getUnitCount();
	    int scannerOffset = startTokenOffset;
		int idx = 0;
		boolean hitEnd = false;  // only useful in degenerate cases
		while ( scannerOffset != -1 ) {
			if ( dataSource.isNumber( scannerOffset )) {
				if ( keep.get( idx )) {
				    for ( int i = 0; i < unitCount; i++ ) {
						tp.print( dataSource, scannerOffset );
						scannerOffset = dataSource.getNextToken( scannerOffset );
						if ( scannerOffset == endTokenOffset ) hitEnd = true;
					}
				} else {
				    for ( int i = 0; i < unitCount; i++ ) {
				        scannerOffset = dataSource.getNextToken( scannerOffset );
				        if ( scannerOffset == endTokenOffset ) hitEnd = true;
				    }
				    numberCoordsRemoved++;
				}
				idx++;
			} else {
				tp.print( dataSource, scannerOffset );
				if ( scannerOffset == endTokenOffset ) {
					break;
				}
				scannerOffset = dataSource.getNextToken( scannerOffset );
			}
			if ( hitEnd ) {
			    tp.print( dataSource, scannerOffset );
			    break;
			}
		}
	}

    /** Remove index values based on CoordRemoveData parameter.
     *
     *  There are three ways index values get removed.  For coord/coordIndex, 
     *  texCoord/texCoordIndex, color/colorIndex, normal/normalIndex, any removed
     *  coord affects the index.  In this case we create an array to adjust all the
     *  offsets.
     *
     *  The second way index values may be affected is in the case of color/colorIndex
     *  and normal/normalIndex when perVertex is true.  In this case, there is a one
     *  to one correspondence between the index and the coord.  
     *
     *  The third way is when perVertex is false, and there are faces involved.  In this
     *  case, the number of index values must match the number of faces.  If there are
     *  more index values than faces, we remove the additional index values.
     */
	void removeIndex( TokenPrinter tp, CoordRemoveData cmd, int startTokenOffset, int endTokenOffset ) {
	    BitSet keep = cmd.getCoordsToKeep();
	    int keepSize = keep.size();
	    int[] offsets = new int[ keepSize ];
	    int idx = 0;
	    for ( int i = 0; i < keepSize; i++ ) {
	        offsets[i] = idx;
	        if ( keep.get( i )) {
	            idx++;
	        }
	    }
   	    adjustIndex( tp, offsets, startTokenOffset, endTokenOffset, keepSize, cmd.getIndexLimit() );
	}
	
	/** Rewrite the index field, performing value adjustment along the way.
	 *  Also limits the index field size to the maximum allowed by coordIndex
	 */
	void adjustIndex( TokenPrinter tp, int[] offsets, int scannerOffset, int endTokenOffset, int keepSize, int indexLimit ) {
        int indexCount = 0;
		while ( scannerOffset != -1 ) {
		    int type = dataSource.getType( scannerOffset );
			if ( type == TokenTypes.NumberToken ) {
			    // indexLimit of 0, temporary until face count limit implemented
			    if (( indexLimit == 0 ) || ( indexCount < indexLimit )) {
    				int ival = dataSource.getIntValue( scannerOffset );
    				if ( ival == -1 ) {
    					tp.print( dataSource, scannerOffset, TokenTypes.NumberToken );
    				} else {
    				    if ( ival >= keepSize ) {
    				        // out of range value, print it
    				        tp.print( dataSource, scannerOffset );
    				        System.out.println( "Index value at " + scannerOffset + " value is " + ival + " out of range, max value is " + keepSize );
    				    } else {
        					tp.print( offsets[ ival ] );
        				}
    				}
    				indexCount++;
    			}
			} else {
				tp.print( dataSource, scannerOffset, type );
			}
			if ( scannerOffset == endTokenOffset ) {
				break;
			}
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
	}

	public void summarize( PrintStream ps ) {
		if ( numberCoordsRemoved == 0 ) {
			ps.println( "Did not remove any coordinates." );
		} else if ( numberCoordsRemoved == 1 ) {
			ps.println( "Removed 1 coordinate." );
		} else {
			ps.println( "Removed " + numberCoordsRemoved + " coordinates." );
		}
	}
}


