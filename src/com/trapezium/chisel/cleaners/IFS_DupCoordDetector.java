/*
 * @(#)IFS_DupCoordDetector.java
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
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.chisel.*;

import java.util.Vector;
import java.io.PrintStream;

/**
 *  Detects and removes unnecessarily duplicated values in a coord, texCoord, color,
 *  or normal node.  These are not removed if the Coordinate/TextureCoordinate/Color/
 *  Normal node are DEFfed and USEd by a ROUTE.  This is based on the assumption that
 *  the ROUTE changes the values, therefore editting these affects the corresponding
 *  "index" field, which exists for ROUTEd values and not for the pre-existing values.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 16 Oct 1998 (really older, just added header)
 *
 *  @since           1.0
 */

public class IFS_DupCoordDetector extends Optimizer {
	int mergedPointCount = 0;
	int modifiedFaceCount = 0;

	class CoordMergeData {
		int[] offsets;
		Vector[] vlistKey;
		Vector[] vlistValue;

		public CoordMergeData( int[] offsets ) {
			this.offsets = offsets;
			vlistKey = new Vector[ 512 ];
			vlistValue = new Vector[ 512 ];
		}

		public int[] getOffsets() {
			return( offsets );
		}
		
		public void makeUnique( int offset ) {
		    offsets[ offset ] = offset;
		}
		
		public void wipeout() {
		    vlistKey = null;
		    vlistValue = null;
		}

	    boolean arraysEqual( byte[] s1, byte[] s2 ) {
    	    int len = s1.length;
    	    for ( int i = 0; i < len; i++ ) {
    	        if ( s1[i] != s2[i] ) {
    	            return( false );
    	        }
    	    }
    	    return( true );
    	}
		
		public Integer get( byte[] key ) {
		    int offset = key[0];
		    if ( key.length > 3 ) {
		        offset += (key[1] + key[2] + key[3]);
		    }
		    offset += key[ key.length - 1 ];
	        offset = offset & 0x1ff;
	        Vector keyVector = vlistKey[ offset ];
	        if ( keyVector != null ) {
	            int keyVecSize = keyVector.size();
	            for ( int i = 0; i < keyVecSize; i++ ) {
	                byte[] entry = (byte[])keyVector.elementAt( i );
	                if ( entry.length == key.length ) {
	                    if ( arraysEqual( entry, key )) {
	                        Vector valueVector = vlistValue[ offset ];
	                        return( (Integer)valueVector.elementAt( i ));
	                    }
	                }
	            }
	        }
		    return( null );
		}
		 
		public void put( byte[] key, Integer keyValue ) {
		    int offset = key[0];
		    if ( key.length > 3 ) {
		        offset += (key[1] + key[2] + key[3]);
		    }
		    offset += key[ key.length - 1 ];
	        offset = offset & 0x1ff;
		    if ( vlistKey[ offset ] == null ) {
		        vlistKey[ offset ] = new Vector();
		        vlistValue[ offset ] = new Vector();
		    }
	        vlistKey[ offset ].addElement( key );
	        vlistValue[ offset ].addElement( keyValue );
	    }
		    
	}

    /** Class constructor */
	public IFS_DupCoordDetector() {
		super( "CoordinateOwner", "Removing duplicate values..." );
	}
	
	class CharArrayContainer {
	    char[] charArray;
	    CharArrayContainer( char[] a ) {
	        charArray = a;
	    }
	    public char[] getCharArray() {
	        return( charArray );
	    }
	}

int totalMadeUnique = 0;
	public void attemptOptimization( Node n ) {
	    CoordMergeData coord_cmd = attemptOptimization( n, "coord", "coordIndex", "point" );
	    CoordMergeData tex_cmd = attemptOptimization( n, "texCoord", "texCoordIndex", "point" );
	    CoordMergeData clr_cmd = attemptOptimization( n, "color", "colorIndex", "color" );
	    CoordMergeData normal_cmd = attemptOptimization( n, "normal", "normalIndex", "vector" );
	    
	    // adjust coord_cmd based on texCoord (see comment below)
	    if (( coord_cmd != null ) && ( tex_cmd != null ) && ( n.getField( "texCoordIndex" ) == null )) {
	        // this means there is no texCoordIndex, and texCoord maps one-to-one with coords.
	        // So if there is a coord merge, and the texCoords are not the same, then we
	        // have to undo the coord merge
	        int[] coordOffsets = coord_cmd.getOffsets();
	        int[] texCoordOffsets = tex_cmd.getOffsets();
	        int numberCoords = coordOffsets.length;
	        int numberTexCoords = texCoordOffsets.length;
	        int count = numberCoords;
	        if ( numberTexCoords < count ) {
	            count = numberTexCoords;
	        }
	        int discrepancyCount = 0;
	        int mergeCount = 0;
	        for ( int i = 0; i < count; i++ ) {
	            if ( coordOffsets[i] != i ) {
	                mergeCount++;
    	            if ( texCoordOffsets[i] != texCoordOffsets[coordOffsets[i]] ) {
    	                discrepancyCount++;
    	                coord_cmd.makeUnique( i );
    	            }
	            }
	        }
	        totalMadeUnique += discrepancyCount;
	   //     System.out.println( "mergeCount " + mergeCount + ", discrepancyCount " + discrepancyCount + ", totalMadeUnique " + totalMadeUnique );
	    }
	}
	
	/**
	 *  This needs some work, when the CoordMergeData gets made, the
	 *  offsets need to be readjusted, depending on how the related sets
	 *  get merged.  For example, "texCoord" alone map one-to-one with
	 *  coordinates if there is no "texCoordIndex".  In this case, the
	 *  offsets of the two arrays must match exactly.  Any place they don't
	 *  match must be un-optimized.
	 *
	 *  @param n the Node to optimize
	 *  @param nodeFieldName the name of the IFS node field to attempt optimization for,
	 *     possible values are "coord", "normal", "color", "texCoord"
	 *  @param indexName the name of the index field that corresponds to the node field,
	 *     possible values are "coordIndex", "normalIndex", "colorIndex", "texCoordIndex"
	 *  @param valueName the name of the field within the value node containing values
	 *
	 *  @return a CoordMergeData object used during the actual optimization.  If the
	 *     Node is DEFfed and USEd by a ROUTE, it is not optimized, and null is returned.
	 */
	CoordMergeData attemptOptimization( Node n, String nodeFieldName, String indexName, String valueName ) {
	    if ( n.isFieldUsedByROUTE( nodeFieldName )) {
	        return( null );
	    }
		Field coord = n.getField( nodeFieldName );
		Field coordIndex = n.getField( indexName );
		int factor = 3;
		if ( nodeFieldName.compareTo( "texCoord" ) == 0 ) {
		    factor = 2;
		}
		if ( coord != null ) {
			FieldValue fv = coord.getFieldValue();
			Node coordNode = (Node)fv.getChildAt( 0 );
			if ( coordNode instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)coordNode;
				coordNode = dun.getNode();
			}
			if ( coordNode != null ) {
				MFFieldValue coordValues = null;
				Field coords = coordNode.getField( valueName );
				if ( coords == null ) {
				    return( null );
				}
				if ( coords.isISfield() ) {
				    return( null );
				}
				FieldValue cfv = coords.getFieldValue();
				if ( cfv instanceof MFFieldValue ) {
				    coordValues = (MFFieldValue)cfv;
//    				System.out.println( "raw value count " + coordValues.getRawValueCount() + ", factor " + factor );
    			}
    			if ( coordValues == null ) {
    			    return( null );
    			}
    			int numberCoords = coordValues.getRawValueCount()/factor;
    			if ( numberCoords < 2 ) {
    			    return( null );
    			}
				int[] offsets = new int[ numberCoords ];
				for ( int i = 0; i < numberCoords; i++ ) {
					offsets[i] = i;
				}
				int scannerOffset = coords.getFirstTokenOffset();
				int endOffset = coords.getLastTokenOffset();
				CoordMergeData cmd = new CoordMergeData( offsets );
				int coordNumber = 0;
				while ( true ) {
					if ( dataSource.isNumber( scannerOffset )) {
						int n1 = scannerOffset;
						int n2 = dataSource.getNextToken( n1 );
						if ( factor == 3 ) {
    						int n3 = dataSource.getNextToken( n2 );
    						byte[] result = dataSource.getCharArray( n1, n2, n3 );
    						Integer iresult = cmd.get( result );
    						if ( iresult == null ) {
    						    cmd.put( result, new Integer( coordNumber ));
    						} else {
    						    offsets[ coordNumber ] = iresult.intValue();
    						    mergedPointCount++;
    						}
    						coordNumber++;
    						scannerOffset = n3;
    					} else {
    					    byte[] result = dataSource.getCharArray( n1, n2 );
    					    Integer iresult = cmd.get( result );
    					    if ( iresult == null ) {
    					        cmd.put( result, new Integer( coordNumber ));
    					    } else {
    					        offsets[ coordNumber ] = iresult.intValue();
    					        mergedPointCount++;
    					    }
    					    coordNumber++;
    					    scannerOffset = n2;
    					}
					}
					if ( scannerOffset == endOffset ) {
						break;
					}
					if ( coordNumber >= numberCoords ) {
					    break;
					}
					scannerOffset = dataSource.getNextToken( scannerOffset );
					if ( scannerOffset == -1 ) {
					    return( null );
					}
				}
				cmd.wipeout();
				if ( coordIndex != null ) {
    				replaceRange( coordIndex.getFirstTokenOffset(), coordIndex.getLastTokenOffset(), cmd );
    			}
				return( cmd );
			}
		}
		return( null );
	}
	
	/** Replace index values so that they only refer to a coordinate with a given
	 *  value one way.  Duplicate coordinates then end up not being referenced, which
	 *  allows them to be clean with the IFS_CoordRemover chisel.
	 *
	 *  @param tp print destination
	 *  @param param coordinate merge info
	 *  @param startTokenOffset first offset in the range of tokens to replace
	 *  @param endTokenOffset last offset in the range of tokens to replace
	 */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		int scannerOffset = startTokenOffset;
		CoordMergeData cmd = (CoordMergeData)param;
		int[] offsets = cmd.getOffsets();
		int offsetSize = offsets.length;
		boolean modifiedFace = false;
		dataSource.setState( scannerOffset );
		while ( scannerOffset != -1 ) {
			if ( dataSource.isNumber( scannerOffset )) {
				int ival = dataSource.getIntValue( scannerOffset );
				if ( ival == -1 ) {
					tp.print( dataSource, scannerOffset );
					modifiedFace = false;
				} else if (( ival >= 0 ) && ( ival < offsetSize )) {
					tp.print( offsets[ ival ] );
					if ( offsets[ ival ] != ival ) {
						if ( !modifiedFace ) {
							modifiedFaceCount++;
							modifiedFace = true;
						}
					}
				}
			} else {
				tp.print( dataSource, scannerOffset );
			}
			if ( scannerOffset == endTokenOffset ) {
				break;
			}
			scannerOffset = dataSource.getNextToken( scannerOffset );
		}
	}

	public void summarize( PrintStream ps ) {
		ps.println( "mergedPointCount is " + mergedPointCount );
		ps.println( "modifiedFaceCount is " + modifiedFaceCount );
	}
}


