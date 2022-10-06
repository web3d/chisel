/*
 * @(#)IFS_CoplanarTriToQuad.java
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
import com.trapezium.util.CompositeObject;
import java.util.Vector;
import java.util.BitSet;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 *  This chisel turns coplanar triangles into quads, 
 *  preserving texture and normal information.
 */
public class IFS_CoplanarTriToQuad extends Optimizer {
	int polygonsSmashed = 0;
	int ifsCount = 0;
	int processedIFS = 0;

	static final int ReplaceCoordIndex = 1;
	static final int ReplaceTexCoordIndex = 2;
	static final int ReplaceNormalCoordIndex = 3;
	static final int ReplaceColorCoordIndex = 4;
	static final int ReplaceCoordNodeValue = 5;
	static final int ReplaceTexCoordNodeValue = 6;
	static final int ReplaceNormalNodeValue = 7;
	static final int ReplaceColorNodeValue = 8;
	
	protected SpaceStructure ss;
	
	/** The FaceTracker is used to manage per-face information
	 *  independent of how it is specified in the file.
	 */
	class FaceTracker {
	    Field indexField;
	    Field valueNode;
	    boolean perFace;
	    boolean oneValuePerIndex;
	    int scanner;
	    Vector colorVec;
	    
		/** FaceTracker constructor.
		 *
		 *  @param indexField the "colorIndex" or "normalIndex" field
		 *  @param valueNode the "color" or "normal" field
		 *  @param perFace true if the values are specified one per face
		 *  @param oneValuePerIndex true if the values are indicated by the
		 *     indexField field
		 */
	    FaceTracker( Field indexField, Field valueNode, boolean perFace, 
			boolean oneValuePerIndex ) {
	        this.indexField = indexField;
	        this.valueNode = valueNode;
	        this.perFace = perFace;
	        this.oneValuePerIndex = oneValuePerIndex;
	        scanner = -1;
	        if ( oneValuePerIndex ) {
	            scanner = indexField.getFirstTokenOffset();
	        } else if ( valueNode != null ) {
	            scanner = valueNode.getFirstTokenOffset();
	            colorVec = new Vector();
	        }
	        
	        // position scanner at first number
	        if ( scanner != -1 ) {
    	        int saveState = dataSource.getState();
    	        dataSource.setState( scanner );
    	        scanner = dataSource.skipNonNumbers();
    	        dataSource.setState( saveState );
    	    }
	    }
	    
		/** Get the next color index value, uses either the indexField
		 *  directly, or simulates the existence of the "colorIndex" or 
		 *  "normalIndex" by storing color values and returning an offset 
		 *  into the non-duplicated list of values.
		 */
	    int getNextIndexValue() {
	        int saveState = dataSource.getState();
	        int value = -1;
	        dataSource.setState( scanner );
	        if ( oneValuePerIndex ) {
	            value = dataSource.getIntValue( scanner );
	            dataSource.setState( scanner );
	            scanner = dataSource.getNextToken();
	            scanner = dataSource.skipNonNumbers();
	        } else {
	            float r = dataSource.getFloat( scanner );
	            scanner = dataSource.getNextToken();
	            float g = dataSource.getFloat( scanner );
	            scanner = dataSource.getNextToken();
	            float b = dataSource.getFloat( scanner );
	            scanner = dataSource.getNextToken();
	            scanner = dataSource.skipNonNumbers();
	            String val = r + "_" + g + "_" + b;
	            int colorVecSize = colorVec.size();
	            for ( int i = 0; i < colorVecSize; i++ ) {
	                String s = (String)colorVec.elementAt( i );
	                if ( s.compareTo( val ) == 0 ) {
	                    dataSource.setState( saveState );
	                    return( i );
	                }
	            }
	            colorVec.addElement( val );
	            dataSource.setState( saveState );
	            return( colorVecSize );
	        }
	        dataSource.setState( saveState );
	        return( value );
	    }
	}
	
	/** The VertexTracker manages per vertex information */
	/** The OptimizeParam is passed to each "optimize" call */
	class OptimizeParam {
		public int whichToReplace;
		public SpaceStructure ss;
		boolean perVertex;
		Node node;
		int firstFaceOffset;
		int lastFaceOffset;

		public OptimizeParam( Node node, SpaceStructure ss, int faceBase,
			int whichToReplace, boolean perVertex ) {
		    this.node = node;
			this.ss = ss;
			this.whichToReplace = whichToReplace;
			this.perVertex = perVertex;
			this.firstFaceOffset = faceBase;
			this.lastFaceOffset = ss.getNumberEntities( SpacePrimitive.Face ) - 1;
		}

        public Node getNode() {
            return( node );
        }
        
        public int getFirstFaceOffset() {
            return( firstFaceOffset );
        }
        
        public int getLastFaceOffset() {
            return( lastFaceOffset );
        }
        
		public int getWhichToReplace() {
			return( whichToReplace );
		}
		
		public boolean getPerVertex() {
		    return( perVertex );
		}

		public SpaceStructure getSpaceStructure() {
			return( ss );
		}
	}

	/** Constructor for coplanar triangle to quad merger */
	public IFS_CoplanarTriToQuad() {
		super( "IndexedFaceSet", "Merging coplanar triangles into quads..." );
	}
	
	/** edge reduction shares some code... a bit weird, but missed out on general base class construction*/
	public IFS_CoplanarTriToQuad( String message ) {
	    super( "IndexedFaceSet", message );
	}

	public void attemptOptimization( Node n ) {
	    attemptOptimization( n, null );
	}
	
	public void reset() {
	    coordToSpace = null;
	    spaceStructuresReduced = null;
	    ifsCount = 0;
	}
	
    /** Keep track of association between coordNode and SpaceStructure.
     *  Don't remake SpaceStructure unless necessary.
     */
    Hashtable coordToSpace;
    void associate( Node coordNode, SpaceStructure ss ) {
        if ( coordToSpace == null ) {
            coordToSpace = new Hashtable();
        }
        coordToSpace.put( coordNode, ss );
    }
    
    boolean hasSpaceStructure( Node coordNode ) {
        if ( coordToSpace == null ) {
            return( false );
        } else {
            return( coordToSpace.get( coordNode ) != null );
        }
    }
        
    SpaceStructure getSpaceStructure( Node coordNode ) {
        if ( !hasSpaceStructure( coordNode )) {
            return( new SpaceStructure() );
        } else {
            return( (SpaceStructure)coordToSpace.get( coordNode ));
        }
    }
	
	/** create the SpaceStructure for the IndexedFaceSet */
	public void attemptOptimization( Node n, BoundingBox b ) {
		ifsCount++;
		Field coord = n.getField( "coord" );
		Field coordIndex = n.getField( "coordIndex" );
		// no coordIndex on empty IndexedFaceSets
		if ( coordIndex == null ) {
		    return;
		}
		if ( coord != null ) {
			FieldValue fv = coord.getFieldValue();
			Node coordNode = (Node)fv.getChildAt( 0 );
			String name = null;
			if ( coordNode instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)coordNode;
				coordNode = dun.getNode();
				name = dun.getDEFName();
			}
			if ( coordNode != null ) {
			    boolean spaceStructureExists = hasSpaceStructure( coordNode );
			    if ( spaceStructureExists ) {
			        ss = getSpaceStructure( coordNode );
			    } else {
    				ss = new SpaceStructure();
    				ss.setName( name );
    				associate( coordNode, ss );
    				MFFieldValue coordValues = null;
    				Field coords = coordNode.getField( "point" );
    				coordValues = (MFFieldValue)coords.getFieldValue();
    				int numberCoordValues = coordValues.getRawValueCount()/3;
    				int scanner = coordValues.getFirstTokenOffset();
    				dataSource.setState( scanner );
    				for ( int i = 0; i < numberCoordValues; i++ ) {
    					scanner = dataSource.skipNonNumbers();
    					float x = dataSource.getFloat( scanner );
    					scanner = dataSource.getNextToken();
    					scanner = dataSource.skipNonNumbers();
    					float y = dataSource.getFloat( scanner );
    					scanner = dataSource.getNextToken();
    					scanner = dataSource.skipNonNumbers();
    					float z = dataSource.getFloat( scanner );
    					scanner = dataSource.getNextToken();
    					// add the coordinate to the SpaceStructure
    					ss.addVertex( x, y, z );
    
    					// if there is a bounding box, update it as well
    					if ( b != null ) {
    					    b.setXYZ( x, y, z );
	    				}
    				}
    			}

				//
				//  Since we may be merging faces, this potentially affects
				//  textures, colors, and normals.  Set flags indicating which
				//  of these are affected.
				//
				Field texCoordIndex = n.getField( "texCoordIndex" );
				Field normalIndex = n.getField( "normalIndex" );
				Field colorIndex = n.getField( "colorIndex" );
				Field colorNode = n.getField( "color" );
				Field normalNode = n.getField( "normal" );
				boolean replaceTexCoordIndex = false;
				boolean replaceNormalIndex = false;
				boolean replaceColorIndex = false;
				boolean replaceColorNodeValues = false;
				boolean replaceNormalNodeValues = false;
				
				// 
				//  Merging faces means that some faces disappear.  Colors
				//  are affected if there is one color per face (flag 
				//  is "colorPerFace").  If there is one color per face, this
				//  could be indicated by either a colorIndex field listing
				//  the offset into the color field for each face's color
				//  or just the color field listing each face's color directly.
				//
				boolean colorPerFace =  !n.getBoolValue( "colorPerVertex" );
				boolean normalPerFace =  !n.getBoolValue( "normalPerVertex" );
				boolean trackColorByIndex = false;
				boolean trackNormalByIndex = false;
				if ( colorNode == null ) {
				    colorPerFace = false;
				} else if ( colorIndex != null ) {
				    trackColorByIndex = true;
				}
				if ( normalNode == null ) {
				    normalPerFace = false;
				} else if ( normalIndex != null ) {
				    trackNormalByIndex = true;
				}
				FaceTracker faceColorTracker = new FaceTracker( colorIndex, 
					colorNode, colorPerFace, trackColorByIndex );
				FaceTracker faceNormalTracker = new FaceTracker( normalIndex, 
					normalNode, normalPerFace, trackNormalByIndex );
				//VertexTracker textureTracker = VertexTracker.factory(
				//	texCoordIndex, texCoord );
				int faceBase = ss.getNumberEntities( SpacePrimitive.Face );
				if ( coordIndex != null ) {
					MFFieldValue mfv = (MFFieldValue)coordIndex.getFieldValue();
					MFFieldValue texCoordFieldValues = null;
					MFFieldValue normalCoordFieldValues = null;
					MFFieldValue colorCoordFieldValues = null;
					
					// missing the object:
					//   texCoordScanner - first token of texture coordinates
					//   texCoordFieldValues is FieldValue of texture 
					//     coordinates
					//   replaceTexCoordIndex set to true if we have 
					//     both texCoord and texCoordIndex fields
					//     This means that only texCoordIndex is affected
					//
					if ( texCoordIndex != null ) {
						FieldValue testfv = texCoordIndex.getFieldValue();
						if ( testfv instanceof MFFieldValue ) {
							texCoordFieldValues = 
								(MFFieldValue)texCoordIndex.getFieldValue();
							if ( texCoordFieldValues.getRawValueCount() > 0 ) {
    							replaceTexCoordIndex = true;
    						} else {
    						    texCoordFieldValues = null;
    						}
						}
					}
					int texCoordScanner = -1;
					if ( texCoordFieldValues != null ) {
						texCoordScanner = 
							texCoordFieldValues.getFirstTokenOffset();
					}

					// missing the object:
					//   normalCoordScanner - first token of normal values
					//   normalCoordFieldValues is FieldValue of normal field
					//   replaceNormalCoordIndex set to true if the both the
					//     normal and normalIndex values are valid
					//
					if ( normalIndex != null ) {
						FieldValue testfv = normalIndex.getFieldValue();
						if ( testfv instanceof MFFieldValue ) {
							normalCoordFieldValues = 
								(MFFieldValue)normalIndex.getFieldValue();
							if ( normalCoordFieldValues.getRawValueCount() 
									> 0 ) {
    							replaceNormalIndex = true;
    						} else {
    						    normalCoordFieldValues = null;
    						}
						}
					} else if ( normalNode != null ) {
					    replaceNormalNodeValues = true;
					}
					if ( normalCoordFieldValues != null ) {
					    if ( !n.getBoolValue( "normalPerVertex" )) {
					        normalCoordFieldValues = null;
					    } else {
					        normalPerFace = true;
					    }
					}
					int normalCoordScanner = -1;
					if ( normalCoordFieldValues != null ) {
						normalCoordScanner = 
							normalCoordFieldValues.getFirstTokenOffset();
					}
					if ( colorIndex != null ) {
					    FieldValue testfv = colorIndex.getFieldValue();
					    if ( testfv instanceof MFFieldValue ) {
					        colorCoordFieldValues = 
								(MFFieldValue)colorIndex.getFieldValue();
					        if ( colorCoordFieldValues.getRawValueCount() 
									> 0 ) {
					            replaceColorIndex = true;
					        } else {
					            colorCoordFieldValues = null;
					        }
					    }
					} else if ( colorNode != null ) {
					    replaceColorNodeValues = true;
					}
					if ( colorCoordFieldValues != null ) {
					    if ( !n.getBoolValue( "colorPerVertex" )) {
					        colorCoordFieldValues = null;
					    }
					}
					int colorCoordScanner = -1;
					if ( colorCoordFieldValues != null ) {
					    colorCoordScanner =
							colorCoordFieldValues.getFirstTokenOffset();
					}
					
					int numberCoordIndexValues = mfv.getRawValueCount();
					int scanner = mfv.getFirstTokenOffset();
					dataSource.setState( scanner );
					
					for ( int i = 0; i < numberCoordIndexValues; i++ ) {
    					scanner = dataSource.skipNonNumbers();
    					// add texture coordinates to space structure
						//if ( textureTracker != null ) {
						//	ss.addTexCoord( textureTracker.getIndexValue() );
						//}
						if ( texCoordFieldValues != null ) {
						    dataSource.setState( texCoordScanner );
							texCoordScanner = dataSource.skipNonNumbers();
							if ( texCoordScanner != -1 ) {
    							ss.addTexCoord( 
									dataSource.getIntValue( texCoordScanner ));
    							texCoordScanner = dataSource.getNextToken();
    						}
							dataSource.setState( scanner );
						}
						
						// add normal coordinates to space structure
						if ( normalCoordFieldValues != null ) {
						    dataSource.setState( normalCoordScanner );
						    normalCoordScanner = dataSource.skipNonNumbers();
						    if ( normalCoordScanner != -1 ) {
						        ss.addNormalCoord( 
								dataSource.getIntValue( normalCoordScanner ));
						        normalCoordScanner = dataSource.getNextToken();
						    }
						    dataSource.setState( scanner );
						}

                        if ( colorCoordFieldValues != null ) {
                            dataSource.setState( colorCoordScanner );
                            colorCoordScanner = dataSource.skipNonNumbers();
                            if ( colorCoordScanner != -1 ) {
                                ss.addColorCoord( 
								dataSource.getIntValue( colorCoordScanner ));
                                colorCoordScanner = dataSource.getNextToken();
                            }
                            dataSource.setState( scanner );
                        }

						// set face index value, and set the face color
						// when the end of the face is encountered
						if ( scanner != -1 ) {
						    int value = dataSource.getIntValue( scanner );
    						ss.addFaceCoord( value );
	    					scanner = dataSource.getNextToken();
	    					if (( value == -1 ) && colorPerFace ) {
	    					    ss.setFaceColor( 
									faceColorTracker.getNextIndexValue() );
	    					}
	    				}
					}
				}
				if ( b != null ) {
				    replaceRange( coord.getFirstTokenOffset(), 
						coord.getLastTokenOffset(), 
						new OptimizeParam( n, ss, faceBase,
							ReplaceCoordNodeValue, true ));
					replaceRange( coordIndex.getFirstTokenOffset(), 
						coordIndex.getLastTokenOffset(), 
						new OptimizeParam( n, ss, faceBase, ReplaceCoordIndex, true ));
					if ( replaceTexCoordIndex ) {
						replaceRange( texCoordIndex.getFirstTokenOffset(), 
							texCoordIndex.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase,
								ReplaceTexCoordIndex, true ));
					}
					if ( replaceNormalIndex ) {
					    replaceRange( normalIndex.getFirstTokenOffset(), 
							normalIndex.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase, ReplaceNormalCoordIndex, 
								n.getBoolValue( "normalPerVertex" )));
					}
					if ( replaceColorIndex ) {
					    replaceRange( colorIndex.getFirstTokenOffset(), 
							colorIndex.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase, ReplaceColorCoordIndex, 
								n.getBoolValue( "colorPerVertex" )));
					} else if ( replaceColorNodeValues ) {
					    replaceRange( colorNode.getFirstTokenOffset(), 
							colorNode.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase, ReplaceColorNodeValue, 
								n.getBoolValue( "colorPerVertex" )));
					}
				} else if ( ss.getNumberEntities( SpacePrimitive.Face ) > 0 ) {
					replaceRange( coordIndex.getFirstTokenOffset(), 
						coordIndex.getLastTokenOffset(), 
						new OptimizeParam( n, ss, faceBase,
							ReplaceCoordIndex, true ));
					if ( replaceTexCoordIndex ) {
						replaceRange( texCoordIndex.getFirstTokenOffset(), 
							texCoordIndex.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase,
								ReplaceTexCoordIndex, true ));
					}
					if ( replaceNormalIndex ) {
					    replaceRange( 
							normalIndex.getFirstTokenOffset(), 
							normalIndex.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase,
								ReplaceNormalCoordIndex, 
								n.getBoolValue( "normalPerVertex" )));
					} else if ( replaceNormalNodeValues ) {
					    replaceRange( normalNode.getFirstTokenOffset(),
					        normalNode.getLastTokenOffset(),
					        new OptimizeParam( n, ss, faceBase,
					            ReplaceNormalNodeValue,
					            n.getBoolValue( "normalPerVertex" )));
					}
					if ( replaceColorIndex ) {
					    replaceRange( colorIndex.getFirstTokenOffset(), 									colorIndex.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase,
								ReplaceColorCoordIndex,
								n.getBoolValue( "colorPerVertex" )));
					} else if ( replaceColorNodeValues ) {
					    replaceRange( colorNode.getFirstTokenOffset(),
							colorNode.getLastTokenOffset(), 
							new OptimizeParam( n, ss, faceBase,
								ReplaceColorNodeValue, 
								n.getBoolValue( "colorPerVertex" )));
					}
				}
			}
		}
	}


	/** The SpaceStructure already contains the reduction information,
	 *  here we just write that information out to the file.
	 *
	 *  @param tp where to write the text
	 *  @param param optimization inforamtion
	 *  @param startTokenOffset first token to replace
	 *  @param endTokenOffset last token to replace
	 */
	Hashtable spaceStructuresReduced;
	protected boolean alreadyProcessed( SpaceStructure ss ) {
	    if ( spaceStructuresReduced == null ) {
	        spaceStructuresReduced = new Hashtable();
	        spaceStructuresReduced.put( ss, ss );
	        return( false );
	    } else if ( spaceStructuresReduced.get( ss ) == null ) {
	        spaceStructuresReduced.put( ss, ss );
	        return( false );
	    } else {
	        return( true );
	    }
	}
	
	public void optimize( TokenPrinter tp, Object param, 
		int startTokenOffset, int endTokenOffset ) {
		SpaceStructure ss = ((OptimizeParam)param).getSpaceStructure();
		if ( !alreadyProcessed( ss )) {
		    processedIFS++;
			int numberReduced = ss.coplanarTriToQuad( processedIFS );
			if ( numberReduced > 0 ) {
				polygonsSmashed += numberReduced;
			}
		}
		if ( param instanceof OptimizeParam ) {
			OptimizeParam op = (OptimizeParam)param;
			if ( op.getWhichToReplace() == ReplaceCoordIndex ) {
				replaceCoordIndex( tp, op.getSpaceStructure(), 
				    op.getFirstFaceOffset(), op.getLastFaceOffset(), 
					startTokenOffset, endTokenOffset );
			} else if ( op.getWhichToReplace() == ReplaceTexCoordIndex ) {
				replaceTexCoordIndex( tp, op.getSpaceStructure(), 
				    op.getFirstFaceOffset(), op.getLastFaceOffset(),
					startTokenOffset, endTokenOffset );
			} else if ( op.getWhichToReplace() == ReplaceNormalCoordIndex ) {
			    replaceNormalCoordIndex( tp, op.getSpaceStructure(), 
			        op.getFirstFaceOffset(), op.getLastFaceOffset(),
					startTokenOffset, endTokenOffset, op.getPerVertex() );
			} else if ( op.getWhichToReplace() == ReplaceColorCoordIndex ) {
			    replaceColorCoordIndex( tp, op.getSpaceStructure(), 
			        op.getFirstFaceOffset(), op.getLastFaceOffset(), 
					startTokenOffset, endTokenOffset, op.getPerVertex() );
			} else if ( op.getWhichToReplace() == ReplaceColorNodeValue ) {
			    replaceColorNodeValue( tp, op.getSpaceStructure(), 
			        op.getFirstFaceOffset(), op.getLastFaceOffset(), 
					startTokenOffset, endTokenOffset, op.getPerVertex() );
			}
		}
	}
	
	/** Replace color node values.
	 *
	 *  @param tp where to print to
	 *  @param ss the SpaceStructure that contains information about what
	 *     faces have been merged
	 *  @param startTokenOffset the first token of the color node
	 *  @param endTokenOffset the last token of the color node
	 *  @param perVertex true if color node values are per vertex, false if
	 *     they are per face
	 */
	void replaceColorNodeValue( TokenPrinter tp, SpaceStructure ss, int firstFaceOffset, int lastFaceOffset,
		int startTokenOffset, int endTokenOffset, boolean perVertex ) {
	    if ( !perVertex ) {
	        replaceNodeValues( tp, ss, firstFaceOffset, lastFaceOffset, startTokenOffset, endTokenOffset );
	    }
	}
	
	void replaceNodeValues( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset, int startTokenOffset, int endTokenOffset ) {
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
	        if ( !faceBits.get( bitNo )) {
	            scanner = printNumbers( tp, scanner, endTokenOffset, 3 );
	   	    } else {
	   	        scanner = skipNumbers( scanner, endTokenOffset, 3 );
    	    }
    	    bitNo++;
    	}
    }
    
    
    int skipNumbers( int scanner, int endTokenOffset, int n ) {
        for ( int i = 0; i < n; i++ ) {
            scanner = dataSource.getNextToken();
            if ( scanner >= endTokenOffset ) {
                break;
            }
            scanner = dataSource.skipNonNumbers();
            if ( scanner >= endTokenOffset ) {
                break;
            }
        }
        return( scanner );
    }

    /** translate a face offset in the old face set into the corresponding
     *  face offset in the new set.
     */
    int xlate( SpaceStructure ss, int faceOffset ) {
        BitSet faceBits = ss.getFaceBits();
        // faceBits null when nothing removed
        if ( faceBits == null ) {
            return( faceOffset );
        }
        int count = 0;
        for ( int i = 0; i < faceOffset; i++ ) {
            if ( !faceBits.get( i )) {
                count++;
            }
        }
        return( count );
    }

	public void replaceCoordIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset,
		int startTokenOffset, int endTokenOffset ) {
		// If the space structure performed an optimization, it affects the coordIndex
		// and texCoord fields.
		int scanner = startTokenOffset;
		dataSource.setState( scanner );
		
		// print everything up to the first number
		scanner = tp.printUntilToken( scanner, endTokenOffset, TokenTypes.NumberToken );
		tp.flush();
		
		// now get the string value of each face from the SpaceEntitySet, and print those
		SpaceEntitySet faces = ss.getEntitySet( SpacePrimitive.Face );
		int numberFaces = faces.getNumberEntities();
		StringBuffer sb = new StringBuffer();
//		System.out.println( "original: firstFaceOffset " + firstFaceOffset + ", lastFaceOffset " + lastFaceOffset );
		firstFaceOffset = xlate( ss, firstFaceOffset );
		lastFaceOffset = xlate( ss, lastFaceOffset );
//		System.out.println( "xlated: firstFaceOffset " + firstFaceOffset + ", lastFaceOffset " + lastFaceOffset + " at line " + dataSource.getLineNumber( scanner ));
		for ( int i = firstFaceOffset; i <= lastFaceOffset; i++ ) {
			SpacePrimitive face = faces.getEntity( i );
			if ( face == null ) {
//			    System.out.println( "Face " + i + " is null, firstFaceOffset " + firstFaceOffset + ", lastFaceOffset " + lastFaceOffset + ", numberFaces " + numberFaces );
			} else {
			    int fcount = faces.getCount( face, SpacePrimitive.Vertex );
//			    System.out.println( "face " + i + " has " + fcount + " vertices" );
    			for ( int j = 0; j < fcount; j++ ) {
    				sb.append( faces.getValue( face, SpacePrimitive.Vertex, j ));
    				sb.append( " " );
    			}
    			sb.append( "-1" );
    			tp.print( new String( sb ));
    			sb.setLength( 0 );
    		}
		}
		
		//  now scan up to the closing right bracket
		while (( scanner != -1 ) && !dataSource.isRightBracket( scanner )) {
		    if ( scanner == endTokenOffset ) {
		        break;
		    }
			scanner = dataSource.getNextToken();
		}

        // now print everything from the right bracket to the end of the
        // section being replaced
		while ( scanner != -1 ) {
			tp.print( dataSource, scanner );
			if ( scanner == endTokenOffset ) {
				break;
			}
			scanner = dataSource.getNextToken();
		}
	}

    /** Replace the texCoordIndex field.
     *
     *  @param tp TokenPrinter printing destination
     *  @param ss SpaceStructure, contains info about how structure has changed
     *  @param firstFaceOffset when there is a set of IFSes all using the same
     *     coordNode, these are all placed into the same SpaceStructure, they
     *     are distinguished by the range of faces defined by each IFS (since 
     *     each IFS adds its own faces to the same SpaceStructure).  This is
     *     ths offset of the first face for a particular IFS.
     *  @param lastFaceOffset offset of the last face for this IFS
     *  @param startTokenOffset first token in range being replaced
     *  @param lastTokenOffset last token in range being replaced.
     */
	public void replaceTexCoordIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset,
		int startTokenOffset, int endTokenOffset ) {
	    replaceIndex( tp, ss, firstFaceOffset, lastFaceOffset,
	        startTokenOffset, endTokenOffset, PerVertexData.Texture );
	}
	
	public void replaceNormalCoordIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset,
		int startTokenOffset, int endTokenOffset, boolean perVertex ) {
	    if ( perVertex ) {
    	    replaceIndex( tp, ss, firstFaceOffset, lastFaceOffset,
    	        startTokenOffset, endTokenOffset, 
				PerVertexData.Normal );
    	} else {
    	    replaceIndex( tp, ss, firstFaceOffset, lastFaceOffset, startTokenOffset, endTokenOffset );
    	}
	}
	
	public void replaceColorCoordIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset,
		int startTokenOffset, int endTokenOffset, boolean perVertex ) {
	    if ( perVertex ) {
    	    replaceIndex( tp, ss, firstFaceOffset, lastFaceOffset,
    	        startTokenOffset, endTokenOffset, PerVertexData.Color );
    	} else {
    	    replaceIndex( tp, ss, firstFaceOffset, lastFaceOffset, startTokenOffset, endTokenOffset );
    	}
	}

	/** Replace an index field where the index values are one-to-one with
	 *  coordIndex.  We keep track of which faces get preserved, which
	 *  don't, then decide what to print based on that.
	 *
	 *  @param tp TokenPrinter printing destination
	 *  @param ss SpaceStructure contains info about what faces have been removed
	 *  @param firstFaceOffset when a set of IFSes use the same coords, they
	 *     all use the same Structure, but are distinguished from each other
	 *     by each covering a different range of faces in the SpaceStructure.
	 *     This parameter indicates the offset of the first SpaceStructure
	 *     face for the IFS we are optimizing
	 *  @param lastFaceOFfset the offset of the last SpaceStructure face for
	 *     the IFS we are optimizing
	 *  @param startTokenOffset first token in range being replaced
	 *  @param lastTokenOffset last token in range being replaced
	 */
	public void replaceIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset, 
	    int startTokenOffset, int endTokenOffset ) {
		// If the space structure performed an optimization, it affects the coordIndex
		// and texCoord fields.
		int scanner = startTokenOffset;
		dataSource.setState( scanner );
		int bitNo = firstFaceOffset;
		BitSet bits = ss.getFaceBits();
        int faceCount = ss.getFaceBitCount();
        while ( scanner <= endTokenOffset ) {
		    scanner = tp.printNonNumbers( scanner, endTokenOffset );
		    if ( scanner >= endTokenOffset ) {
		        break;
		    }
		    // if the face is preserved, or we've gone beyone all faces,
		    // print the token
		    if ( bits == null ) {
		        tp.print( dataSource, scanner, TokenTypes.NumberToken );
		    } else if ( !bits.get( bitNo ) || ( bitNo >= faceCount )) {
		        tp.print( dataSource, scanner, TokenTypes.NumberToken );
		    }
	        scanner = dataSource.getNextToken();
		    bitNo++;
		}
	}
	
	
	public void replaceIndex( TokenPrinter tp, SpaceStructure ss, 
	    int firstFaceOffset, int lastFaceOffset,
	    int startTokenOffset, int endTokenOffset, int type ) {
		// If the space structure performed an optimization, it affects the coordIndex
		// and texCoord fields.
		int scanner = startTokenOffset;
		dataSource.setState( scanner );
		scanner = tp.printUntilToken( scanner, endTokenOffset, TokenTypes.NumberToken );
		tp.flush();
		SpaceEntitySet faces = ss.getEntitySet( SpacePrimitive.Face );
		int numberFaces = faces.getNumberEntities();
		firstFaceOffset = xlate( ss, firstFaceOffset );
		lastFaceOffset = xlate( ss, lastFaceOffset );
		StringBuffer sb = new StringBuffer();
    	for ( int i = firstFaceOffset; i <= lastFaceOffset; i++ ) {
			SpacePrimitive face = faces.getEntity( i );
			if ( face == null ) {
//			    System.out.println( "got a null, firstFaceOffset " + firstFaceOffset + ", lastFaceOffset " + lastFaceOffset );
			    break;
			}
			for ( int j = 0; 
				  j < faces.getCount( face, SpacePrimitive.Vertex ); j++ ) {
			    Object x = face.getAttachedObject();
			    PerVertexData t;
			    if ( x instanceof CompositeObject ) {
			        CompositeObject c = (CompositeObject)x;
			        t = (PerVertexData)c.getObject( type );
			    } else {
                    t = (PerVertexData)x;
                }
				sb.append( t.getValue( faces.getValue( face, SpacePrimitive.Vertex, j )));
				sb.append( " " );
			}
			sb.append( "-1" );
			tp.print( new String( sb ));
			sb.setLength( 0 );
		}
		while (( scanner != -1 ) && !dataSource.isRightBracket( scanner )) {
			scanner = dataSource.getNextToken();
		}

		while ( scanner != -1 ) {
			tp.print( dataSource, scanner );
			if ( scanner == endTokenOffset ) {
				break;
			}
			scanner = dataSource.getNextToken();
		}
	}

	public void summarize( PrintStream ps ) {
		if ( polygonsSmashed == 0 ) {
			ps.println( "IFS_CoplanarTriToQuad removed no polygons." );
		} else if ( polygonsSmashed == 1 ) {
			ps.println( "IFS_CoplanarTriToQuad removed 1 polygon." );
		} else {
			ps.println( "IFS_CoplanarTriToQuad removed " + polygonsSmashed + " polygons." );
		}
	}
}


