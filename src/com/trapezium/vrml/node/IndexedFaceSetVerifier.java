/*
 * @(#)IndexedFaceSetVerifier.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.clvorlon;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.fields.MFInt32Value;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.grammar.Table7;
import com.trapezium.vrml.node.generated.IndexedFaceSet;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Enumeration;

import com.trapezium.vorlon.ErrorSummary;

/**
 *  Verifies fields within an IndexedFaceSet.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 13 March 1998, update Translator interface, bad
 *                   faces warnings not errors
 *  @version         1.1, 13 Jan 1998
 *
 *  @since           1.0
 */
public class IndexedFaceSetVerifier extends IndexedLineSetVerifier {

	/**  Verify an IndexedFaceSet.
	 *
	 *  @param nodeToBeVerified the Node being verified for usage
	 *  @param s the Scene containing the node
	 *  @param errorSummary summary used for cases where there are to many errors to
	 *     specifiy individually
	 *  @param checkList hashtable of what is already checked to prevent unnecessary rechecking
     */
	public void verify( Node nodeToBeVerified, Scene s, ErrorSummary errorSummary, Hashtable checkList ) {
		TokenEnumerator dataSource = s.getTokenEnumerator();
	    if ( isSingleColor( nodeToBeVerified, dataSource )) {
	        nodeToBeVerified.setError( "Warning, single color can be placed in diffuseColor" );
	    }
		Hashtable usageTable = s.getUsageTable();
		Hashtable coordTable = s.getCoordTable();
		Hashtable texCoordTable = s.getTexCoordTable();
		Hashtable colorTable = s.getColorTable();
		Hashtable normalTable = s.getNormalTable();
		
		Field coordIndex = nodeToBeVerified.getField( "coordIndex" );
		MFFieldValue coordIndexValue = null;
		if ( coordIndex != null ) {
		    coordIndexValue =(MFFieldValue)coordIndex.getFieldValue();
		    if ( clvorlon.ifsInfo && coordIndexValue != null ) {
		        System.out.println( "IFS has " + coordIndexValue.getRawValueCount() + " index values" );
		    }
		}
		Node coord = getNode( nodeToBeVerified, "coord" );

		Field texCoordIndex = nodeToBeVerified.getField( "texCoordIndex" );
		MFFieldValue texIndexValue = null;
		if ( texCoordIndex != null ) {
    		texIndexValue = (MFFieldValue)texCoordIndex.getFieldValue();
    	}
		Node texCoord = getNode( nodeToBeVerified, "texCoord" );
		verifyValues( coord, coordIndex, 3, dataSource, texCoord, texCoordIndex, errorSummary, checkList );

		IndexInfo coordInfo = new IndexInfo( true,
			coordIndexValue, coord, "point", 3, "coordIndex",
			coordIndexValue, coord, nodeToBeVerified, usageTable );

		verifyValues( texCoord, texCoordIndex, 2, dataSource, errorSummary, checkList );
		IndexInfo texCoordInfo = new IndexInfo( true,
			texIndexValue, texCoord, "point", 2, "texCoordIndex",
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

		Field normalIndex = nodeToBeVerified.getField( "normalIndex" );
		MFFieldValue normalIndexValue = null;
		if ( normalIndex != null ) {
	    	normalIndexValue = (MFFieldValue)normalIndex.getFieldValue();
	    }
		Node normal = getNode( nodeToBeVerified, "normal" );
		verifyValues( normal, normalIndex, 3, dataSource, errorSummary, checkList );
		IndexInfo normalInfo = new IndexInfo( 
			nodeToBeVerified.getBoolValue( "normalPerVertex" ),
			normalIndexValue, normal, "vector", 3, "normalIndex",
			coordIndexValue, coord, nodeToBeVerified, usageTable );

		coordTable.put( nodeToBeVerified, coordInfo );
		texCoordTable.put( nodeToBeVerified, texCoordInfo );
		colorTable.put( nodeToBeVerified, colorInfo );
		normalTable.put( nodeToBeVerified, normalInfo );

		int numberFaces = verifyCoords( nodeToBeVerified, usageTable,
			coordInfo, texCoordInfo, colorInfo, normalInfo, 
			s.getTokenEnumerator(), s.getErrorSummary() );
		if ( numberFaces > Table7.IFSMaxFaces ) {
		    nodeToBeVerified.setError( "Nonconformance, " + 
				numberFaces + " faces exceeds base profile limit of " + 
				Table7.IFSMaxFaces );
		}
		if ( nodeToBeVerified instanceof IndexedFaceSet ) {
		    ((IndexedFaceSet)nodeToBeVerified).setNumberFaces( numberFaces );
		}
		verifyTexCoords( nodeToBeVerified, usageTable, coordInfo, 
			texCoordInfo, s.getTokenEnumerator() );
		verifyNormals( nodeToBeVerified, usageTable, numberFaces, 
			coordInfo, normalInfo, s.getTokenEnumerator() );
		verifyColors( nodeToBeVerified, usageTable, numberFaces, 
			coordInfo, colorInfo, s.getTokenEnumerator() );
	}

	/** Verify the coord fields of an IndexedFaceSet or IndexedLineSet.
	 *  Verifies that coordIndex values are valid, marks coords that are in
	 *  use, generates a warning if there are no faces.
	 *
	 *  If the texCoordIndex field is empty and the texCoord field has values,
	 *  this means there should be a one-to-one correspondence between
	 *  coordIndex values and texCoord values.  This is already stored
	 *  within the IndexInfo "texCoordInfo" by the "isUsingCoordIndex" method.
	 *  In this case, the usageBits for the texCoord must be marked as well.
	 *
	 *  If the coords are really defined by an IS in a PROTO, this 
	 *  verification has to be done on the PROTO instance.
	 */
	public int verifyCoords( Node nodeToBeVerified, Hashtable usageTable, 
	    IndexInfo coordInfo, IndexInfo texCoordInfo, IndexInfo colorInfo, 
	    IndexInfo normalInfo, TokenEnumerator v, ErrorSummary errorSummary ) {
		int numberFaces = 0;
		//
		//  If the texCoord is accessed via the coordIndex, we have to set its 
		//  usage while verifying the coordIndex, set up that information here.
		//
		boolean markTexUsageBits = false;
		int texUsageMax = 0;
		BitSet texUsageBits = null;

		if ( texCoordInfo.isUsingCoordIndex() ) {
			Node texNode = texCoordInfo.getValueNode();
			if ( texNode != null ) {
				UsageInfo ui = (UsageInfo)usageTable.get( texNode );
				if ( ui != null ) {
					texUsageBits = ui.getUsageBits();
					texUsageMax = ui.getCount();
					markTexUsageBits = true;
				}
			}
		} else {
			//
			//  If the texCoord exists and is accessed via texCoordIndex, 
			//  then there must  be one texCoordIndex for each coordIndex.  
			//  A further check (not done yet) is that the -1 values appear 
			//  in exactly the same place in each.
			//
			//  At the moment, we only check that the number of entries in 
			//  texCoordIndex is identical to the number in coordIndex.
			//
			MFFieldValue texIndex = texCoordInfo.getIndexValue();
			if ( texIndex != null ) {
				int numberTexCoordIndexValues = texCoordInfo.getNumberIndexValues();
				if ( numberTexCoordIndexValues > 0 ) {
    				int numberCoordIndexValues = coordInfo.getNumberIndexValues();
    				if ( numberTexCoordIndexValues < numberCoordIndexValues ) {
    					texIndex.setError( "must have at least " + numberCoordIndexValues + " coordinates, got " + numberTexCoordIndexValues );
    				} else if ( numberTexCoordIndexValues > numberCoordIndexValues ) {
    				    texIndex.setError( "Warning, found " + numberTexCoordIndexValues + " values, only need " + numberCoordIndexValues );
    				}
    			}
			}
		}

		boolean markColorUsageBitsByVertex = false;
		boolean markColorUsageBitsByFace = false;
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
						markColorUsageBitsByFace = true;
						colorTranslator = new Translator( v, colorInfo.getIndexValue(), colorInfo.getNumberIndexValues() );
					} else {
						markColorUsageBitsByVertex = true;
					}
				}
			}
		}

		boolean markNormalUsageBitsByVertex = false;
		boolean markNormalUsageBitsByFace = false;
		int normalUsageMax = 0;
		BitSet normalUsageBits = null;
		Translator normalTranslator = null;
		if ( normalInfo.isUsingCoordIndex() || normalInfo.isUsingCoordFaces() ) {
			Node normalNode = normalInfo.getValueNode();
			if ( normalNode != null ) {
				UsageInfo ui = (UsageInfo)usageTable.get( normalNode );
				if ( ui != null ) {
					normalUsageBits = ui.getUsageBits();
					normalUsageMax = ui.getCount();
					if ( normalInfo.isUsingCoordFaces() ) {
						markNormalUsageBitsByFace = true;
						normalTranslator = new Translator( v, normalInfo.getIndexValue(), normalInfo.getNumberIndexValues() );
					} else {
						markNormalUsageBitsByVertex = true;
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
   				nodeToBeVerified.setError( "Warning, no faces" );
   			}
            return( 0 );
        } else {
			Field coords = coordNode.getField( "point" );
			if ( coords == null ) {
			    if (( indexValues != null ) && ( numberCoordIndexValues > 0 )) {
		            nodeToBeVerified.setError( "no points" );
			    } else {
    				nodeToBeVerified.setError( "Warning, no faces" );
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
					nodeToBeVerified.setError( "Warning, no faces" );
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
			int[] face = new int[200];
			int faceIdx = 0;
			int maxFaceIdx = 0;
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
							for ( int i = 0; i < faceIdx; i++ ) {
								if ( face[ i ] == val ) {
								    if (( errorSummary == null ) || errorSummary.countWarning( "repeated index in" )) {
								        nodeToBeVerified.addWarning( scannerOffset, "Warning, repeated index in face" );
    								}
									error = true;
									break;
								}
							}
							if ( !error && ( faceIdx < 200 )) {
								face[ faceIdx ] = val;
								faceIdx++;
								if ( faceIdx > maxFaceIdx ) {
								    maxFaceIdx = faceIdx;
								}
								if ( faceIdx == ( Table7.IFSMaxVerticesPerFace + 1 ))   {
								    Value value = new Value( scannerOffset );
								    value.setError( "Nonconformance, base profile limit of " + Table7.IFSMaxVerticesPerFace + " edges per face exceeded here" );
								    nodeToBeVerified.addChild( value );
								    error = true;
								}
							}
						} else {
							Value value = new Value( scannerOffset );
							value.setError( "index out of range, max value is " + (maxCoordIndex-1) );
							nodeToBeVerified.addChild( value );
						}
						if ( markTexUsageBits ) {
							if ( val < texUsageMax ) {
								texUsageBits.set( val );
							} else {
								Value value = new Value( scannerOffset );
								value.setError( "index out of range for texCoord, max value is " + (texUsageMax-1) );
								nodeToBeVerified.addChild( value );
							}
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
						if ( markNormalUsageBitsByVertex ) {
							if ( val < normalUsageMax ) {
								normalUsageBits.set( val );
							} else {
								Value value = new Value( scannerOffset );
								value.setError( "index out of range for normal, max value is " + (normalUsageMax-1) );
								nodeToBeVerified.addChild( value );
							}
						}
					} else if ( val == -1 ) {
						faceIdx = 0;
						if ( edgeCount < 3 ) {
						    if (( errorSummary == null ) || errorSummary.countWarning( "at least 3 edges" )) {
						        nodeToBeVerified.addWarning( scannerOffset, "Warning, face must have at least 3 edges." );
    						}
						}
						if ( markColorUsageBitsByFace ) {
							int offset = colorTranslator.getOffset( numberFaces, 
							    colorUsageMax, nodeToBeVerified, scannerOffset, v, "colorIndex" );
							if ( offset == -1 ) {
							} else if ( offset < colorUsageMax ) {
								colorUsageBits.set( offset );
							} else {
								Value value = new Value( scannerOffset );
								value.setError( "face " + numberFaces + " has no color, max color is " + (colorUsageMax - 1) );
								nodeToBeVerified.addChild( value );
							}
						}
						if ( markNormalUsageBitsByFace ) {
							int offset = normalTranslator.getOffset( numberFaces, 
							    normalUsageMax, nodeToBeVerified, scannerOffset, v, "normalIndex" );
							if ( offset == -1 ) {
							} else if ( offset < normalUsageMax ) {
								normalUsageBits.set( offset );
							} else {
								Value value = new Value( scannerOffset );
								value.setError( "face " + numberFaces + " has no normal, max normal is " + (normalUsageMax - 1) );
								nodeToBeVerified.addChild( value );
							}
						}
						numberFaces++;
						edgeCount = 0;
					} else if ( val < -1 ) {
						Value value = new Value( scannerOffset );
						value.setError( "invalid index, must be -1, or 0 through " + (maxCoordIndex-1) );
						nodeToBeVerified.addChild( value );
					}
					coordCount++;
					if ( coordCount == numberCoordIndexValues ) {
						if ( val != -1 ) numberFaces++;
						break;
					}
				}
				scannerOffset = v.getNextToken();
			}
			if ( maxFaceIdx <= 3 ) {
    			Field convex = nodeToBeVerified.getField( "convex" );
        		if ( convex != null ) {
        		    if ( !nodeToBeVerified.getBoolValue( "convex" )) {
        		        if ( maxFaceIdx == 3 ) {
            		        convex.setError( "Warning, unnecessary setting for triangles" );
            		    } else {
            		        convex.setError( "Warning, unnecessary setting" );
            		    }
        		    }
        		}
        	}
		}
		return( numberFaces );
	}

    /** Check the usage in a set of values.
     *
     *  @param coordBits BitSet representing value list
     *  @param maxCoordIndex number of index values
     *  @param nodeToBeVerified the IndexedFaceSet being verified
     *  @param factor the number of numeric values in each value list entry
     *  @param indexString the name of the "index" field
     *  @param valueName the name of the "value" list field
     *  @param v the TokenEnumerator text source
     *  @param errorSummary for limiting creation of Value warning nodes
     */
	static public void checkValueUsage( BitSet coordBits, int maxCoordIndex, 
	    VrmlElement coordValues, Node nodeToBeVerified, int factor, 
	    String indexString, String valueName, 
	    TokenEnumerator v, ErrorSummary errorSummary ) {
	    if ( VrmlElement.nowarning ) {
	        return;
	    }
	    boolean indexExists = ( nodeToBeVerified.getField( indexString ) != null );
	    int saveScanOffset = -1;
	    int saveOffset = -1;
		for ( int i = 0; i < maxCoordIndex; i++ ) {
			if ( !coordBits.get( i )) {
				int scannerOffset = coordValues.getFirstTokenOffset();
				v.setState( scannerOffset );
				int offset = i*factor;
				int coordCounter = 0;
				if ( saveScanOffset != -1 ) {
				    coordCounter = saveOffset;
				    scannerOffset = saveScanOffset;
				    v.setState( scannerOffset );
				}
				while ( scannerOffset != -1 ) {
					if ( v.isNumber( scannerOffset )) {
    					if ( coordCounter == offset ) {
    					    saveScanOffset = scannerOffset;
    					    saveOffset = offset;
    					    if ( indexExists ) {
    					        if (( errorSummary == null ) || errorSummary.countWarning( " not in " )) {
               						Value value = new Value( scannerOffset );
               						value.setError( "Warning, " + valueName + " " + i + " not in " + indexString );
               						nodeToBeVerified.addChild( value );
               					}
           					} else {
           					    if (( errorSummary == null ) || errorSummary.countWarning( " not referenced" )) {
           					        Value value = new Value( scannerOffset );
               					    value.setError( "Warning, " + valueName + " " + i + " not referenced" );
               					    nodeToBeVerified.addChild( value );
               					}
           					}
    						break;
    					}
						coordCounter++;
					}
					scannerOffset = v.getNextToken();
				}
			}
		}
	}

    /** Verify usage in a set of values */
	static public void verifyUsage( Hashtable usageTable, TokenEnumerator v, ErrorSummary errorSummary ) {
		Enumeration nodes = usageTable.keys();
		while ( nodes.hasMoreElements() ) {
			Node n = (Node)nodes.nextElement();
			UsageInfo ui = (UsageInfo)usageTable.get( n );
			BitSet coordBits = ui.getUsageBits();
			Node node = ui.getOwner();
			int count = ui.getCount();
			checkValueUsage( coordBits, count, n, node, ui.getFactor(), ui.getIndexString(), ui.getValueName(), v, errorSummary );
		}
	}
	
	//
	//  When verifying normal, normalIndex
	//
	//  If using coordIndex, the usage marking took place when the coords were verified.
	//
	//  Otherwise, here we mark usage.
	//
	void verifyNormals( Node nodeToBeVerified, Hashtable usageTable, int numberFaces, 
	    IndexInfo coordInfo, IndexInfo normalInfo, TokenEnumerator v ) {
		boolean normalPerVertex = normalInfo.isPerVertex();
		int normalIndexCount = normalInfo.getNumberIndexValues();
		verifyCoordIndex( nodeToBeVerified, usageTable, coordInfo, normalInfo, (normalIndexCount > 0) && normalPerVertex, v );
		int normalValueCount = normalInfo.getNumberValues();
		int numberCoordValues = normalInfo.getNumberCoordValues();
		int numberCoordIndexValues = normalInfo.getNumberCoordIndexValues();
		verifyCounts( nodeToBeVerified, numberFaces,
			normalValueCount, normalIndexCount,
			numberCoordValues, numberCoordIndexValues,
			normalPerVertex, "normal", "normalIndex", normalInfo.getIndexValue() );
	}

	//
	//  When verifying texCoord, texCoordIndex:
	//
	//  If using coordIndex, the usage marking took place when the coords were verified.
	//
	//  Otherwise, here we mark usage.
	//
	void verifyTexCoords( Node nodeToBeVerified, Hashtable usageTable, IndexInfo coordInfo, IndexInfo texCoordInfo, TokenEnumerator v ) {
		verifyCoordIndex( nodeToBeVerified, usageTable, coordInfo, texCoordInfo, true, v );
	}
	
	/** Check if the IFS contains a single color */
	static public boolean isSingleColor( Node ifs, TokenEnumerator dataSource ) {
	    if ( ifs != null ) {
            if ( !ifs.getBoolean( "colorPerVertex" )) {
                 Field color = ifs.getField( "color" );
                 Field colorIndex = ifs.getField( "colorIndex" );
                 return( singleColor( color, colorIndex, dataSource ));
            }
        }
        return( false );
    }

	/** Check if the color and colorIndex fields indicate a single color. */
	static boolean singleColor( Field color, Field colorIndex, TokenEnumerator dataSource ) {
	    // still might be single color, bug if color node is DEFfed and USEd, checking
	    // is more complicated
	    if ( color != null ) {
	        Node node = color.getNode();
            if ( node instanceof DEFUSENode ) {
                DEFUSENode dun = (DEFUSENode)node;
                if ( dun.isUSE() ) {
                    return( false );
                } else if ( dun.isUsed() ) {
                    return( false );
                }
            }
	    }
	        
	    if ( colorIndex == null ) {
	        return( singleColor( color, dataSource ));
	    } else if ( color != null ) {
	        return( singleColorIndex( colorIndex, dataSource ));
	    } else {
	        return( false );
	    }
	}
	
	/** Check if the color node indicates a single color. */
	static boolean singleColor( Field fcolor, TokenEnumerator dataSource ) {
	    if ( fcolor == null ) {
	        return( false );
	    }
	    Node color = fcolor.getNodeValue();
	    if ( color != null ) {
	        Field ffcolor = color.getField( "color" );
	        if ( ffcolor != null ) {
	            return( checkValues( ffcolor.getFirstTokenOffset(), ffcolor.getLastTokenOffset(), dataSource ));
	        }
	    }
	    return( false );
	}
	
	/** Check if the numeric values in the token list are all identical */
	static boolean checkValues( int firstTokenOffset, int lastTokenOffset, TokenEnumerator dataSource ) {
	    int scanner = firstTokenOffset;
	    dataSource.setState( scanner );
	    int c1 = dataSource.skipToNumber( 0 );
	    scanner = dataSource.getNextToken();
        int c2 = dataSource.skipToNumber( 0 );
	    scanner = dataSource.getNextToken();
	    int c3 = dataSource.skipToNumber( 0 );
	    scanner = dataSource.getNextToken();
	    while (( scanner != -1 ) && ( scanner < lastTokenOffset )) {
	        int tc1 = dataSource.skipToNumber( 0 );
	        if ( tc1 == -1 ) {
	            break;
	        }
	        scanner = dataSource.getNextToken();
	        int tc2 = dataSource.skipToNumber( 0 );
	        if ( tc2 == -1 ) {
	            break;
	        }
	        scanner = dataSource.getNextToken();
	        int tc3 = dataSource.skipToNumber( 0 );
	        if ( tc3 == -1 ) {
	            break;
	        }
	        scanner = dataSource.getNextToken();
	        if ( !dataSource.sameAs( c1, tc1 )) {
	            return( false );
	        }
	        if ( !dataSource.sameAs( c2, tc2 )) {
	            return( false );
	        }
	        if ( !dataSource.sameAs( c3, tc3 )) {
	            return( false );
	        }
	    }
	    return( true );
	}
	
	static boolean singleColorIndex( Field colorIndex, TokenEnumerator dataSource ) {
	    if ( colorIndex != null ) {
	        int first = colorIndex.getFirstTokenOffset();
	        int last = colorIndex.getLastTokenOffset();
	        int scanner = first;
	        dataSource.setState( scanner );
	        int doffset = dataSource.skipToNumber( 0 );
	        int dval = dataSource.getIntValue( doffset );
	        while (( scanner != -1 ) && ( scanner < last )) {
	            scanner = dataSource.getNextToken();
	            if ( scanner == -1 ) {
	                return( true );
	            }
	            int offset = dataSource.skipToNumber( 0 );
	            if (( offset == -1 ) || ( offset > last )) {
	                return( true );
	            }
	            if ( dataSource.getIntValue( offset ) != dval ) {
	                return( false );
	            }
	        }
	    }
	    return( false );
	}
	
}

