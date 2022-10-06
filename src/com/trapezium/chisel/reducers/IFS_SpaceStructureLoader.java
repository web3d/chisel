/*
 * @(#)IFS_SpaceStructureLoader.java
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

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.space.SpaceStructure;
import com.trapezium.vrml.node.space.SpacePrimitive;
import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import java.util.Hashtable;


/**
 *  This is the abstract base class for any Optimizer which relies on the 
 *  <B>trapezium.vrml.node.space</B> package.
 *
 *  It creates a space structure, then registers the coord node and coordIndex
 *  field for replacement.
 */
abstract public class IFS_SpaceStructureLoader extends Optimizer {
	static final int SS_Coordinate = 1;
	static final int SS_CoordIndex = 2;
	static final int SS_TexCoordIndex = 3;
	static final int SS_ColorIndex = 4;
	static final int SS_Color = 5;
	class SpaceStructureParam {
		public int whichToReplace;
		public SpaceStructure ss;

		public SpaceStructureParam( SpaceStructure ss, int whichToReplace ) {
			this.ss = ss;
			this.whichToReplace = whichToReplace;
		}

		public int getWhichToReplace() {
			return( whichToReplace );
		}

		public SpaceStructure getSpaceStructure() {
			return( ss );
		}
	}

	public IFS_SpaceStructureLoader( String actionStr ) {
		super( "IndexedFaceSet", actionStr );
	}

    public void setSpaceStructure( SpaceStructure ss ) {
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
    
	public void attemptOptimization( Node n ) {
		Field coord = n.getField( "coord" );
		if ( coord != null ) {
			FieldValue fv = coord.getFieldValue();
			Node coordNode = (Node)fv.getChildAt( 0 );
			if ( coordNode instanceof DEFUSENode ) {
				DEFUSENode dun = (DEFUSENode)coordNode;
				coordNode = dun.getNode();
			}
			if ( coordNode != null ) {
			    boolean spaceStructureExists = hasSpaceStructure( coordNode );
    			SpaceStructure ss = getSpaceStructure( coordNode );
    			if ( !spaceStructureExists ) {
    				MFFieldValue coordValues = null;
    				Field coords = coordNode.getField( "point" );
    				coordValues = (MFFieldValue)coords.getFieldValue();
    				int numberCoordValues = coordValues.getRawValueCount()/3;
    				System.out.println( numberCoordValues + " coordinates in structure" );
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
    
    					// to create the space structure vertices, each x,y,z value is just added in
    					// the order encountered using "SpaceStructure.addVertex( float,float,float )"
    					ss.addVertex( x, y, z );
    				}
    				associate( coordNode, ss );
    			}
				//
				//  This handles only the case where texCoordIndex corresponds
				//  with coordIndex.  If there is no texCoordIndex, this does nothing.
				//
				//  This section also handles adding each face to the SpaceStructure.
				//
				Field coordIndex = n.getField( "coordIndex" );
				Field texCoordIndex = n.getField( "texCoordIndex" );
				Field colorIndex = n.getField( "colorIndex" );
				Field color = n.getField( "color" );
				boolean replaceTexCoordIndex = false;
				if ( coordIndex != null ) {
					MFFieldValue mfv = (MFFieldValue)coordIndex.getFieldValue();
					MFFieldValue tfv = null;
					if ( texCoordIndex != null ) {
						FieldValue testfv = texCoordIndex.getFieldValue();
						if ( testfv instanceof MFFieldValue ) {
							tfv = (MFFieldValue)texCoordIndex.getFieldValue();
							if ( tfv.getRawValueCount() > 0 ) {
    							replaceTexCoordIndex = true;
    						} else {
    						    tfv = null;
    						}
						}
					}
					int numberCoordIndexValues = mfv.getRawValueCount();
					int scanner = mfv.getFirstTokenOffset();
					int texCoordScanner = -1;
					if ( tfv != null ) {
						texCoordScanner = tfv.getFirstTokenOffset();
					}
					dataSource.setState( scanner );
					for ( int i = 0; i < numberCoordIndexValues; i++ ) {
    					scanner = dataSource.skipNonNumbers();
						FieldValue texOneCoord = null;
						if ( tfv != null ) {
						    dataSource.setState( texCoordScanner );
							texCoordScanner = dataSource.skipNonNumbers();
							if ( texCoordScanner != -1 ) {
    							ss.addTexCoord( dataSource.getIntValue( texCoordScanner ));
    							texCoordScanner = dataSource.getNextToken();
    						}
							dataSource.setState( scanner );
						}

						// to create the space structure faces, each coordinate is added in the
						// order encountered (including the -1 values) with
						// "SpaceStructure.addFaceCoord( int value )"
						if ( scanner != -1 ) {
    						ss.addFaceCoord( dataSource.getIntValue( scanner ));
	    					scanner = dataSource.getNextToken();
	    				}
					}
				}
				if ( ss.getNumberEntities( SpacePrimitive.Face ) > 0 ) {
				    replaceRange( coordNode.getFirstTokenOffset(), coordNode.getLastTokenOffset(), 
				        new SpaceStructureParam( ss, SS_Coordinate ));
				    if ( coordIndex != null ) {
    				    replaceRange( coordIndex.getFirstTokenOffset(), coordIndex.getLastTokenOffset(),
    				        new SpaceStructureParam( ss, SS_CoordIndex ));
    				}
					if ( replaceTexCoordIndex ) {
						replaceRange( texCoordIndex.getFirstTokenOffset(), texCoordIndex.getLastTokenOffset(), 
						    new SpaceStructureParam( ss, SS_TexCoordIndex ));
					}
					if ( colorIndex != null ) {
					    replaceRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(),
					        new SpaceStructureParam( ss, SS_ColorIndex ));
					} else if ( color != null ) {
					    replaceRange( color.getFirstTokenOffset(), color.getLastTokenOffset(),
					        new SpaceStructureParam( ss, SS_Color ));
					}
					setSpaceStructure( ss );
				}
			}
		}
	}


	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		if ( param instanceof SpaceStructureParam ) {
			SpaceStructureParam ssp = (SpaceStructureParam)param;
			int paramType = ssp.getWhichToReplace();
			SpaceStructure ss = ssp.getSpaceStructure();
			if ( paramType == SS_Coordinate ) {
			    replaceCoord( tp, ss, startTokenOffset, endTokenOffset );
			} else if ( paramType == SS_CoordIndex ) {
			    replaceCoordIndex( tp, ss, startTokenOffset, endTokenOffset );
			} else if ( paramType == SS_TexCoordIndex ) {
			    replaceTexCoordIndex( tp, ss, startTokenOffset, endTokenOffset );
			} else if ( paramType == SS_ColorIndex ) {
			    replaceColorIndex( tp, ss, startTokenOffset, endTokenOffset );
			} else if ( paramType == SS_Color ) {
			    replaceColor( tp, ss, startTokenOffset, endTokenOffset );
			}
		}
	}

    abstract public void replaceCoord( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset );
    public void replaceCoordIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        tp.printRange( startTokenOffset, endTokenOffset, false );
        tp.flush();
    }
    
    public void replaceTexCoordIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        tp.printRange( startTokenOffset, endTokenOffset, false );
        tp.flush();
    }
    public void replaceColorIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        tp.printRange( startTokenOffset, endTokenOffset, false );
        tp.flush();
    }
    public void replaceColor( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        tp.printRange( startTokenOffset, endTokenOffset, false );
        tp.flush();
    }
}


