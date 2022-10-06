/*
 * @(#)IndexedLineSetVerifier.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.grammar.Table7;
import com.trapezium.vorlon.ErrorSummary;
import com.trapezium.util.NestedHashtable;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  Verifies fields within an IndexedLineeSet.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 7 May, created
 *
 *  @since           1.12
 */
public class IndexedLineSetVerifier implements Verifier {

	/** Get the Node associated with a Node field.
	 *
	 *  @return  the node associated with a node field, or null if
	 *     the nodeFieldName is unknown, or the nodeFieldName indicates
	 *     a field not defined in the file, or the nodeFieldName has
	 *     a USE statement referring to a name with no corresponding DEF.
	 *
	 *     If the node is a PROTOInstance, and the getField returns null,
	 *     try to get it from the PROTObase.
	 */
	public Node getNode( Node nodeToBeVerified, String nodeFieldName ) {
		Field f = nodeToBeVerified.getField( nodeFieldName );
		Node result = null;
		if ( f == null ) {
		    if ( nodeToBeVerified instanceof PROTOInstance ) {
		        PROTOInstance pi = (PROTOInstance)nodeToBeVerified;
		        Node n = pi.getPROTONodeType();
		        if ( n != null ) {
		            f = n.getField( nodeFieldName );
		        }
		    }
		} 
		if ( f != null ) {
			FieldValue fv = f.getFieldValue();
			if ( fv != null ) {
			    VrmlElement ve = fv.getChildAt( 0 );
			    if ( ve instanceof Node ) {
			        result = (Node)ve;
    				if ( result instanceof DEFUSENode ) {
	    				DEFUSENode dun = (DEFUSENode)result;
    					result = dun.getNode();
    				}
    			}
			}
		}
		return( result );
	}


	/**  Verify an IndexedFaceSet or IndexedLineSet.
	 *
	 *  @param nodeToBeVerified the Node we are checking
	 *  @param s the Scene containing the Node
	 *  @param errorSummary summary info used in case where there are too many errors to
	 *     specify individually
	 *  @param checkList list of what has already checked to prevent unnecessary rechecking
	 */
	public void verify( Node nodeToBeVerified, Scene s, ErrorSummary errorSummary, Hashtable checkList ) {
		Hashtable usageTable = s.getUsageTable();
		Hashtable coordTable = s.getCoordTable();
		Hashtable colorTable = s.getColorTable();
		TokenEnumerator dataSource = s.getTokenEnumerator();
		
		Field coordIndex = nodeToBeVerified.getField( "coordIndex" );
		MFFieldValue coordIndexValue = null;
		if ( coordIndex != null ) {
		    coordIndexValue =(MFFieldValue)coordIndex.getFieldValue();
		}
		Node coord = getNode( nodeToBeVerified, "coord" );
		verifyValues( coord, coordIndex, 3, dataSource, errorSummary, checkList );

		IndexInfo coordInfo = new IndexInfo( true,
			coordIndexValue, coord, "point", 3, "coordIndex",
			coordIndexValue, coord, nodeToBeVerified, usageTable );

		Field colorIndex = nodeToBeVerified.getField( "colorIndex" );
		MFFieldValue colorIndexValue = null;
		if ( colorIndex != null ) {
		    colorIndexValue = (MFFieldValue)colorIndex.getFieldValue();
		}
		Node color = getNode( nodeToBeVerified, "color" );
		verifyValues( color, colorIndex, 3, dataSource, errorSummary, checkList );
		IndexInfo colorInfo = new IndexInfo( 
			nodeToBeVerified.getBoolValue( "colorPerVertex" ),
			colorIndexValue, color, "color", 3, "colorIndex",
			coordIndexValue, coord, nodeToBeVerified, usageTable );

		coordTable.put( nodeToBeVerified, coordInfo );
		colorTable.put( nodeToBeVerified, colorInfo );

		int numberLines = verifyCoords( nodeToBeVerified, usageTable,
			coordInfo, colorInfo, s.getTokenEnumerator(), s.getErrorSummary() );
		verifyColors( nodeToBeVerified, usageTable, numberLines, 
			coordInfo, colorInfo, s.getTokenEnumerator() );
	}

	/** Verify values, finds and marks duplicate values.
	 *
	 *  @param valueField Field containing values
	 *  @param indexField Field containing index references to values
	 *  @param factor number of floats per value (2 for texture coordinates,
	 *    3 for colors, spatial coordinates, and normals)
	 *  @param dataSource contains text of the Fields
	 *  @param errorSummary optional parameter for limiting number of warning objects
	 *     inserted into scene graph
	 *  @param checkList list of nodes already checked
	 */
	public void verifyValues( Field valueField, Field indexField, 
		int factor, TokenEnumerator dataSource, ErrorSummary errorSummary, Hashtable checkList ) {
		verifyValues( valueField, indexField, factor, dataSource, null, null, errorSummary, checkList );
	}
	
	/** Verify values, finds and marks duplicate values.
	 *
	 *  @param valueField Field containing values
	 *  @param indexField Field containing index references to values
	 *  @param factor number of floats per value (2 for texture coordinates,
	 *    3 for colors, spatial coordinates, and normals)
	 *  @param dataSource contains text of the Fields
	 *  
	 *  These last two parameters handle a subtle case in duplicate value marking.
	 *  There is one case where a duplicate value is required, which is if it has
	 *  two separate texture coordinates.  So two adjacent faces might map the
	 *  texture starting from two separate places, which means that if the texture
	 *  coordinates are listed without an index field, the texCoord values have to
	 *  be exactly the same before the coord value can be legitimately marked as 
	 *  repeated.
	 * 
	 *  @param texValueField "texCoord" field
	 *  @param texIndexField "texCoordIndex" field
	 */
	public void verifyValues( Field valueField, Field indexField, 
		int factor, TokenEnumerator dataSource, 
		Field texValueField, Field texIndexField, ErrorSummary errorSummary, Hashtable checkList ) {
		// if there is an index field, this special case does not apply
		if ( texIndexField != null ) {
		    texValueField = null;
		}
		if ( texValueField != null ) {
		    FieldValue tvf = texValueField.getFieldValue();
		    if ( tvf == null ) {
		        texValueField = null;
		    }
		}

		// If there is an index field, then repeated values are not necessary,
		// since the index field only needs to refer to one of those values.
		// However, there is an exception to this rule.  If the values are
		// coordinates, and there is a parallel set of texture values, and those
		// texture values are different, the repeated value is necessary.
		// This exception case is not handled here.
        if (( valueField != null ) && ( indexField != null )) {
            // see if this valueField has already passed through this code, via
            // DEF/USE, if so, ignore it, it has already been checked
            if ( checkList.get( valueField ) != null ) {
                return;
            }
            // keep track of valueField, to prevent unnecessary recheck
            checkList.put( valueField, valueField );
            
            int scanner = valueField.getFirstTokenOffset();
            int end = valueField.getLastTokenOffset();
            int valueOffset = 0;
            if (( scanner != -1 ) && ( end != -1 )) {
                NestedHashtable nestedHashtable = new NestedHashtable();
                float[] values = new float[ factor ];
                int state = dataSource.getState();
                dataSource.setState( scanner );
                while ( scanner <= end ) {
                    int firstGuy = scanner;
                    for ( int i = 0; i < factor; i++ ) {
                        scanner = dataSource.skipNonNumbers();
                        if ( scanner == -1 ) {
                            break;
                        }
                        if ( i == 0 ) {
                            firstGuy = scanner;
                        }
                        values[i] = dataSource.getFloat( scanner );
                        scanner = dataSource.getNextToken();
                    }
                    if (( scanner == -1 ) || ( scanner >= end )) {
                        break;
                    }
                    if ( nestedHashtable.get( values ) != null ) {
                        // if there is a texValueField, its only repeated if the
                        // texValue at valueOffset is the same as the texValue at
                        // the previous coord
                        boolean doRepeat = false;
                        if ( texValueField == null ) {
                            doRepeat = true;
                        } else {
                            Integer prevValue = (Integer)nestedHashtable.get( values );
                            int originalValueOffset = prevValue.intValue();
                            try {
                                if (( texValueField.getFloat( dataSource, originalValueOffset*2 ) == texValueField.getFloat( dataSource, valueOffset*2 )) &&
                                    ( texValueField.getFloat( dataSource, originalValueOffset*2+1 ) == texValueField.getFloat( dataSource, valueOffset*2+1 ))) {
                                    doRepeat = true;
                                }
                            } catch ( Exception e ) {
                                doRepeat = true;
                            }
                        }
                        if ( doRepeat ) {
                            if (( errorSummary == null ) || errorSummary.countWarning( "repeated value" )) {
                                valueField.addWarning( firstGuy, "Warning, repeated value" );
                            }
                        }
                    } else {
                        nestedHashtable.put( values, new Integer( valueOffset ));
                    }
                    valueOffset++;
                }
                dataSource.setState( state );
            }
        }
    }

                        
	/** Verify the color fields in an IndexedFaceSet or IndexedLineSet
	 *
	 *  @param nodeToBeVerified the IndexedFaceSet or IndexedLineSet
	 *  @param usageTable used to record what color values are actually used
	 *  @param numberFaces number of faces in the IndexedFaceSet or lines in
	 *    the IndexedLineSet
	 *  @param coordInfo coord/coordIndex information
	 *  @param colorInfo color/colorIndex/colorPerVertex information
	 *  @param v text of the node
	 */
	public void verifyColors( Node nodeToBeVerified, Hashtable usageTable, 
		int numberFaces, IndexInfo coordInfo, IndexInfo colorInfo, 
		TokenEnumerator v ) {
		boolean colorPerVertex = colorInfo.isPerVertex();
		int colorIndexCount = colorInfo.getNumberIndexValues();
		verifyCoordIndex( nodeToBeVerified, usageTable, coordInfo, 
			colorInfo, ( colorIndexCount > 0 ) && colorPerVertex, v );
		int colorValueCount = colorInfo.getNumberValues();
		int numberCoordValues = colorInfo.getNumberCoordValues();
		int numberCoordIndexValues = colorInfo.getNumberCoordIndexValues();
		verifyCounts( nodeToBeVerified, numberFaces,
			colorValueCount, colorIndexCount,
			numberCoordValues, numberCoordIndexValues,
			colorPerVertex, "color", "colorIndex", colorInfo.getIndexValue() );
	}

    /** Verify the count of coord/coordIndex relative to texCoord/texCoordIndex,
     *  color/colorIndex, normal/normalIndex.
     *  If there are values, but no corresponding index, this means 
	 *  the coordIndex is to be used as the index, and there must be the 
	 *  same number of  coord values as texCoord/color/normal values.  
	 *  If colorPerVertex or normalPerVertex is false in this case, there 
	 *  must be the same number of faces as there are color/normal values.
     */
	public void verifyCounts( Node nodeToBeVerified, int numberFaces,
		int valueCount, int indexCount,
		int numberCoordValues, int numberCoordIndexValues,
		boolean perVertex, String valueStr, String indexStr,
		MFFieldValue indexValue ) {
		// If there are no values, make sure there are also no index values
		if ( valueCount == 0 ) {
		    if ( indexCount > 0 ) {
		        if ( indexValue != null ) {
		            VrmlElement p = indexValue.getParent();
		            if ( p != null ) {
        		        p.setError( "Warning, no values associated with index" );
        		    }
    		    }
		    }
			return;
		}
		
		// At this point, we have values...
		
		// If there are no indexes, there are two possibilities.  If this is perVertex,
		// it means there has to be a one-to-one correspondence between values and
		// coordinates.  If it is not perVertex, there has to be a one-to-one correspondence
		// between values and faces.
		if ( indexCount == 0 ) {
			if ( perVertex ) {
				if ( valueCount > numberCoordValues ) {
					nodeToBeVerified.setError( "Warning, coord count " + numberCoordValues + " does not match " + valueStr + " count " + valueCount );
				} else if ( valueCount < numberCoordValues ) {
					nodeToBeVerified.setError( "coord count " + numberCoordValues + " does not match " + valueStr + " count " + valueCount );
				}
			} else {
				if ( valueCount > numberFaces ) {
					nodeToBeVerified.setError( "Warning, coordIndex face count " + numberFaces + " does not match " + valueStr + " count " + valueCount );
				} else if ( valueCount < numberFaces ) {
					nodeToBeVerified.setError( "coordIndex face count " + numberFaces + " does not match " + valueStr + " count " + valueCount );
				}
			}
		// If there are indexes, and normalPerVertex or colorPerVertex are true, there
		// must be a one-to-one correspondence between the indexes and the coord indexes.
		// Texture coordinates do not have a "perVertex" field, because implicitly such
		// a field is always true, which means if texCoordIndex values always have a
		// one-to-one correspondence with the coord indexes.
		//
		// If there are indexes, and the "perVertex" flag is false, there is one index
		// value for each face.
		} else {
		    if ( perVertex ) {
				if ( indexCount > numberCoordIndexValues ) {
					nodeToBeVerified.setError( "Warning, " + indexStr + " count " + indexCount + " does not match coordIndex count " + numberCoordIndexValues );
				} else if ( indexCount < numberCoordIndexValues ) {
					nodeToBeVerified.setError( indexStr + " count " + indexCount + " does not match coordIndex count " + numberCoordIndexValues );
				}
			} else {
				if ( indexCount > numberFaces ) {
					indexValue.setError( "Warning, " + (valueCount-numberFaces) + " additional unusable index values (expected " + numberFaces + ", got " + valueCount + ")");
					//nodeToBeVerified.setError( "Warning, face count " + numberFaces + " does not match " + indexStr + " count " + indexCount );
				} else if ( indexCount < numberFaces ) {
					nodeToBeVerified.setError( "face count " + numberFaces + " does not match " + indexStr + " count " + indexCount );
				}
			}
		}
	}

	//
	//  When verifying coord, coordIndex:
	//
	//  - verify coordIndex values are valid
	//  - mark coords that are in use
	//  - generate a warning if there are no faces
	//
	//  The coordIndex may also be used relative to texCoords if the texCoordIndex field
	//  is empty, but the texCoord field has values.  This has already been determined
	//  within the IndexInfo "texCoordInfo" by the "isUsingCoordIndex" method.  If this
	//  is the case, the usageBits for the texCoord must be marked as well.
	//
	//  If the coordIndex is also used by texCoords
	//  However, if the coords are really defined by an IS in a PROTO, we need to
	//  verify the PROTO instance.
	//
	public int verifyCoords( Node nodeToBeVerified, Hashtable usageTable, 
	    IndexInfo coordInfo, IndexInfo colorInfo, TokenEnumerator v, 
	    ErrorSummary errorSummary ) {
		int numberLines = 0;

		boolean markColorUsageBitsByVertex = false;
		boolean markColorUsageBitsByLine = false;
		int colorUsageMax = 0;
		BitSet colorUsageBits = null;
		Translator colorTranslator = null;
		if ( colorInfo.isUsingCoordIndex() || colorInfo.isUsingCoordFaces() ) {
			Node colorNode = colorInfo.getValueNode();
			if ( colorNode != null ) {
				UsageInfo ui = (UsageInfo)usageTable.get( colorNode );
				if ( ui != null ) {
					colorUsageBits = ui.getUsageBits();
					colorUsageMax = ui.getCount();
					if ( colorInfo.isUsingCoordFaces() ) {
						markColorUsageBitsByLine = true;
						colorTranslator = new Translator( v, colorInfo.getIndexValue(), colorInfo.getNumberIndexValues() );
					} else {
						markColorUsageBitsByVertex = true;
					}
				}
			}
		}

		int numberCoordIndexValues = coordInfo.getNumberIndexValues();
   		MFFieldValue indexValues = coordInfo.getIndexValue();
		Node coordNode = coordInfo.getValueNode();
        if ( coordNode == null ) {
		    if (( indexValues != null ) && ( numberCoordIndexValues > 0 )) {
	            nodeToBeVerified.setError( "no points" );
		    } else {
   				nodeToBeVerified.setError( "Warning, no lines" );
   			}
            return( 0 );
        } else {
			Field coords = coordNode.getField( "point" );
			if ( coords == null ) {
			    if (( indexValues != null ) && ( numberCoordIndexValues > 0 )) {
		            nodeToBeVerified.setError( "no points" );
			    } else {
    				nodeToBeVerified.setError( "Warning, no lines" );
    			}
				return( 0 );
			}
			FieldValue cfv = coords.getFieldValue();
			MFFieldValue coordValues = null;
			if ( cfv instanceof MFFieldValue ) {
				coordValues = (MFFieldValue)cfv;
			}
			if ( coordValues == null ) {
				boolean dowarning = true;
				coords = coordNode.getISfield( "point" );
				if ( coords != null ) {
					if ( coords instanceof ISField ) {
						ISField iscoords = (ISField)coords;
						Field fcfv = iscoords.getPROTOfield();
						if ( fcfv != null ) {
							cfv = fcfv.getFieldValue();
							if ( cfv instanceof MFFieldValue ) {
								dowarning = false;
							}
						}
					}
				}
				if ( dowarning ) {
					nodeToBeVerified.setError( "Warning, no lines" );
				}
			}
		}
//		if ( coordInfo.getValueNode() == null ) {
//			nodeToBeVerified.dump( "This guy has no coordInfo.getValueNode" );
//		}
		UsageInfo ui = (UsageInfo)usageTable.get( coordInfo.getValueNode() );
		BitSet coordBits = null;
		if ( ui != null ) {
		    coordBits = ui.getUsageBits();
		}
		int maxCoordIndex = 0;
		if ( ui != null ) {
		    maxCoordIndex = ui.getCount();
		}
		if (( indexValues != null ) && ( coordBits != null )) {
			// get each number token, get int value, set bit
			int scannerOffset = indexValues.getFirstTokenOffset();
			v.setState( scannerOffset );
			int coordCount = 0;
			int edgeCount = 0;
			int[] line = new int[1000];
			int lineIdx = 0;
			while ( scannerOffset != -1 ) {
				if ( v.isRightBracket( scannerOffset )) {
					break;
				} else if ( v.isNumber( scannerOffset )) {
					int val = v.getIntValue( scannerOffset );
					if ( val >= 0 ) {
						if ( val < maxCoordIndex ) {
							coordBits.set( val );
							edgeCount++;
							boolean error = false;
							if (( lineIdx > 0 ) && ( val == line[ lineIdx - 1 ] )) {
							    if (( errorSummary == null ) || errorSummary.countWarning( "repeated index in" )) {
    							    nodeToBeVerified.addWarning( scannerOffset, "Warning, repeated index in line" );
    							}
								error = true;
							}
							if ( !error && ( lineIdx < 1000 )) {
								line[ lineIdx ] = val;
								lineIdx++;
							}
						} else {
							Value value = new Value( scannerOffset );
							value.setError( "index out of range, max value is " + (maxCoordIndex-1) );
							nodeToBeVerified.addChild( value );
						}
						if ( markColorUsageBitsByVertex ) {
							if ( val < colorUsageMax ) {
								colorUsageBits.set( val );
							} else {
								Value value = new Value( scannerOffset );
								value.setError( "index out of range for color, max value is " + (colorUsageMax - 1) );
								nodeToBeVerified.addChild( value );
							}
						}
					} else if ( val == -1 ) {
						lineIdx = 0;
						if ( markColorUsageBitsByLine ) {
							int offset = colorTranslator.getOffset( numberLines, 
							    colorUsageMax, nodeToBeVerified, scannerOffset, v, "colorIndex" );
							if ( offset == -1 ) {
							} else if ( offset < colorUsageMax ) {
								colorUsageBits.set( offset );
							} else {
								Value value = new Value( scannerOffset );
								value.setError( "line " + numberLines + " has no color, max color is " + (colorUsageMax - 1) );
								nodeToBeVerified.addChild( value );
							}
						}
						numberLines++;
						edgeCount = 0;
					} else if ( val < -1 ) {
						Value value = new Value( scannerOffset );
						value.setError( "invalid index, must be -1, or 0 through " + (maxCoordIndex-1) );
						nodeToBeVerified.addChild( value );
					}
					coordCount++;
					if ( coordCount == numberCoordIndexValues ) {
						if ( val != -1 ) numberLines++;
						break;
					}
				}
				scannerOffset = v.getNextToken();
			}
		}
		return( numberLines );
	}


    /** Verify index values for texCoord/texCoordIndex, color/colorIndex,
	 *  normal/normalIndex.
	 *
	 *  @param nodeToBeVerified the IndexedFaceSet or IndexedLineSet being
	 *     verified
	 */
	void verifyCoordIndex( Node nodeToBeVerified, Hashtable usageTable, 
	    IndexInfo synchCoordInfo, IndexInfo genericCoordInfo, 
		boolean negativeOneAllowed, TokenEnumerator v ) {
	    boolean doSynch = !genericCoordInfo.isUsingCoordFaces();
		if ( !genericCoordInfo.isUsingCoordIndex() ) {
			Node valueNode = genericCoordInfo.getValueNode();
			if ( valueNode != null ) {
				UsageInfo ui = (UsageInfo)usageTable.get( valueNode );
				if ( ui != null ) {
					MFFieldValue indexValue = genericCoordInfo.getIndexValue();
					MFFieldValue synchIndexValue = null;
					if ( doSynch ) {
					    synchIndexValue = synchCoordInfo.getIndexValue();
					}
					int maxCoordIndex = ui.getCount();
					BitSet coordBits = ui.getUsageBits();
					if (( indexValue != null ) && ( coordBits != null )) {
						int scannerOffset = indexValue.getFirstTokenOffset();
						int synchScannerOffset = 0;
						if ( synchIndexValue != null ) {
						    synchScannerOffset = synchIndexValue.getFirstTokenOffset();
						}
						v.setState( scannerOffset );
						int coordCount = 0;
						int numberCoordIndexValues = genericCoordInfo.getNumberIndexValues();
						while ( scannerOffset != -1 ) {
							if ( v.isRightBracket( scannerOffset )) {
								break;
							} else if ( v.isNumber( scannerOffset )) {
								int val = v.getIntValue( scannerOffset );
								if ( val >= 0 ) {
									if ( val < maxCoordIndex ) {
										coordBits.set( val );
									} else {
										Value value = new Value( scannerOffset );
										value.setError( "index out of range, max value is " + (maxCoordIndex-1) );
										nodeToBeVerified.addChild( value );
									}
									if ( synchIndexValue != null ) {
									    v.setState( synchScannerOffset );
									    while ( synchScannerOffset != -1 ) {
									        if ( v.isRightBracket( synchScannerOffset )) {
									            nodeToBeVerified.addWarning( scannerOffset, "Warning, index out of synch with coordIndex at this point" );
									            synchIndexValue = null;
									            v.setState( scannerOffset );
									            break;
									        } else if ( v.isNumber( synchScannerOffset )) {
									            int synchVal = v.getIntValue( synchScannerOffset );
									            if ( synchVal < 0 ) {
									                nodeToBeVerified.addWarning( scannerOffset, "Warning, index out of synch with coordIndex at this point" );
									                synchScannerOffset = v.getNextToken();
									                v.setState( scannerOffset );
									                break;
									            } else {
									                synchScannerOffset = v.getNextToken();
									                v.setState( scannerOffset );
									                break;
									            }
									        }
									        synchScannerOffset = v.getNextToken();
									    }
									}
								} else if ( val == -1 ) {
								    if ( !negativeOneAllowed ) {
								        Value value = new Value( scannerOffset );
								        value.setError( "index value must be 0 through " + (maxCoordIndex-1) );
								        nodeToBeVerified.addChild( value );
								    }
									if ( synchIndexValue != null ) {
									    v.setState( synchScannerOffset );
									    while ( synchScannerOffset != -1 ) {
									        if ( v.isRightBracket( synchScannerOffset )) {
									            nodeToBeVerified.addWarning( scannerOffset, "Warning, index out of synch with coordIndex at this point" );
									            synchIndexValue = null;
									            v.setState( scannerOffset );
									            break;
									        } else if ( v.isNumber( synchScannerOffset )) {
									            int synchVal = v.getIntValue( synchScannerOffset );
									            if ( synchVal == -1 ) {
									                synchScannerOffset = v.getNextToken();
									                v.setState( scannerOffset );
									                break;
									            } else {
									                Value value = new Value( scannerOffset );
									                value.setError( "index out of synch with coordIndex at this point" );
									                nodeToBeVerified.addChild( value );
									                synchIndexValue = null;
									                v.setState( scannerOffset );
									                break;
									            }
									        }
									        synchScannerOffset = v.getNextToken();
									    }
									}
								} else if ( val < -1 ) {
									Value value = new Value( scannerOffset );
									value.setError( "invalid index, must be -1, or 0 through " + (maxCoordIndex-1) );
									nodeToBeVerified.addChild( value );
								}
								coordCount++;
								if ( coordCount == numberCoordIndexValues ) {
									break;
								}
							}
							scannerOffset = v.getNextToken();
						}
					}
				}
			}
		}
	}
}

