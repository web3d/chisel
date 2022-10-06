/*
 * @(#)ShapeConnectivitySplitter.java
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
import com.trapezium.chisel.reducers.IFS_SpaceStructureLoader;
import com.trapezium.parse.TokenTypes;
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
import java.util.BitSet;

/**
 *  Splits IndexedFaceSets into connectivity sections, each in its
 *  own IndexedFaceSet.
 */
public class ShapeConnectivitySplitter extends IFS_SpaceStructureLoader {
    SpaceStructure ss;
    String[] skipFields;

	public ShapeConnectivitySplitter() {
		super( "Splitting IndexedFaceSet by connectivity..." );
		skipFields = new String[ 5 ];
		skipFields[0] = "coordIndex";
		skipFields[1] = "color";
		skipFields[2] = "colorIndex";
		skipFields[3] = "normal";
		skipFields[4] = "normalIndex";
	}

	class ConnectivityInfo {
	    Node shape;
	    Node ifs;
	    SpaceStructure ss;

	    public ConnectivityInfo( Node shape, SpaceStructure ss, Node ifs ) {
	        this.shape = shape;
	        this.ss = ss;
	        this.ifs = ifs;
	    }

	    public Node getShape() {
	        return( shape );
	    }

	    public Node getIFS() {
	        return( ifs );
	    }

	    public SpaceStructure getSpaceStructure() {
	        return( ss );
	    }
	}

    public void setSpaceStructure( SpaceStructure ss ) {
        this.ss = ss;
    }

    /** This chisel taks the approach of just replacing entire Shape node.
     *  This approach is a mistake, makes PROTO/IS handling nearly impossible.
     *  Have to rewrite this to use field-by-field replacement technique.
     */
    public void attemptOptimization( Node n ) {
        System.out.println( "ShapeConnectivitySplitter attemptOptimization" );
        ss = null;
        super.attemptOptimization( n );
        if ( ss != null ) {
            Node shape = n.getParent( "Shape" );
            if ( shape != null ) {
                replaceRange( shape.getFirstTokenOffset(), shape.getLastTokenOffset(), new ConnectivityInfo( shape, ss, n ));
            }
        }
    }

    
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    System.out.println( "ShapeConnectivitySplitter optimizing" );
        if ( param instanceof ConnectivityInfo ) {
            ConnectivityInfo info = (ConnectivityInfo)param;
            SpaceStructure ss = info.getSpaceStructure();
            Node ifs = info.getIFS();
            System.out.println( "marking connectivity" );
            ss.markConnectivity();
            int numberConnected = ss.numberConnectivity();
            System.out.println( numberConnected + " connected" );
            if ( numberConnected <= 1 ) {
                tp.printRange( startTokenOffset, endTokenOffset, false );
            } else {
                initOffsets( ifs );
                for ( int i = 0; i < numberConnected; i++ ) {
                    System.out.println( "Generating shape " + (i+1) + " of " + numberConnected );
                    printHeader( tp, startTokenOffset, endTokenOffset );
                    printSkippingFields( tp, ifs, skipFields );
                    printCoordIndex( tp, ss, i );
					printColorInfo( tp, ss, i, ifs );
					printNormalInfo( tp, ss, i, ifs );
                    printTrailer( tp, endTokenOffset );
                }
            }
        }
    }
    
    /** Print up to first field in IFS */
    int firstIFSfieldOffset;
    int lastIFSfieldOffset;
    String[] fieldNames;
    void setFieldOffset( Field f ) {
        if ( f != null ) {
            int firstOffset = f.getFirstTokenOffset();
            int lastOffset = f.getLastTokenOffset();
            if ( firstOffset != -1 ) {
                if ( firstIFSfieldOffset == -1 ) {
                    firstIFSfieldOffset = firstOffset;
                } else if ( firstOffset < firstIFSfieldOffset ) {
                    firstIFSfieldOffset = firstOffset;
                }
            }
            if ( lastOffset != -1 ) {
                if ( lastIFSfieldOffset == -1 ) {
                    lastIFSfieldOffset = lastOffset;
                } else if ( lastOffset > lastIFSfieldOffset ) {
                    lastIFSfieldOffset = lastOffset;
                }
            }
        }
    }
 
    void initOffsets( Node ifs ) {
        firstIFSfieldOffset = -1;
        lastIFSfieldOffset = -1;
        if ( fieldNames == null ) {
            fieldNames = ifs.getFieldNames();
        }
        for ( int i = 0; i < fieldNames.length; i++ ) {
            setFieldOffset( ifs.getField( fieldNames[i] ));
        }
    }
     
    void printHeader( TokenPrinter tp, int startTokenOffset, int endTokenOffset ) {
        printRange( tp, startTokenOffset, firstIFSfieldOffset, endTokenOffset );
    }
    
    void printTrailer( TokenPrinter tp, int endTokenOffset ) {
        printRange( tp, lastIFSfieldOffset + 1, endTokenOffset + 1, endTokenOffset );
    }
    
    void printRange( TokenPrinter tp, int firstInRange, int lastInRange, int tokenBoundary ) {
        int scanner = firstInRange;
        dataSource.setState( scanner );
        while ( scanner != -1 ) {
            if ( scanner > tokenBoundary ) {
                break;
            }
            tp.print( dataSource, scanner );
            scanner = dataSource.getNextToken();
            if ( scanner >= lastInRange ) {
                break;
            }
        }
    }
            
	/** Print color info for a specific connectivity section */
	void printColorInfo( TokenPrinter tp, SpaceStructure ss, int offset, Node ifs ) {
		Field color = ifs.getField( "color" );
		Field colorIndex = ifs.getField( "colorIndex" );
		boolean colorPerVertex = ifs.getBoolValue( "colorPerVertex" );
		if ( colorPerVertex ) {
		    if ( color != null ) {
		        tp.printRange( color.getFirstTokenOffset(), color.getLastTokenOffset(), false );
		    }
		    if ( colorIndex != null ) {
		        tp.printRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(), false );
		    }
		} else {
			if ( colorIndex == null ) {
				if ( color != null ) {
					printValues( tp, ss, offset, color.getFirstTokenOffset(),
						color.getLastTokenOffset(), 3 );
				}
			} else {
				printValues( tp, ss, offset, colorIndex.getFirstTokenOffset(),
					colorIndex.getLastTokenOffset(), 1 );
			}
		}
	}

	/** Print normal info for a specific connectivity section */
	void printNormalInfo( TokenPrinter tp, SpaceStructure ss, int offset, Node ifs ) {
		Field normal = ifs.getField( "normal" );
		Field normalIndex = ifs.getField( "normalIndex" );
		boolean normalPerVertex = ifs.getBoolValue( "normalPerVertex" );
		if ( !normalPerVertex ) {
			if ( normalIndex == null ) {
				if ( normal != null ) {
					printValues( tp, ss, offset, normal.getFirstTokenOffset(),
						normal.getLastTokenOffset(), 3 );
				}
			} else {
				printValues( tp, ss, offset, normalIndex.getFirstTokenOffset(),						normalIndex.getLastTokenOffset(), 1 );
			}
		}
	}

	void printValues( TokenPrinter tp, SpaceStructure ss, int offset, 
		int scanner, int endTokenOffset, int factor ) {
		dataSource.setState( scanner );
		while ( true ) {
		    int type = dataSource.getType( scanner );
			tp.print( dataSource, scanner, type );
			if ( type == TokenTypes.LeftBracket ) {
				scanner = dataSource.getNextToken();
				break;
			}
			scanner = dataSource.getNextToken();
		}
        SpaceEntitySet faces = ss.getEntitySet( SpacePrimitive.Face );
        int numberFaces = faces.getNumberEntities();
        for ( int i = 0; i < numberFaces; i++ ) {
            if ( ss.getConnectivity( i ) == offset ) {
				for ( int j = 0; j < factor; j++ ) {
					scanner = dataSource.skipNonNumbers();
					tp.print( dataSource, scanner, TokenTypes.NumberToken );
					scanner = dataSource.getNextToken();
				}
			} else {
				for ( int j = 0; j < factor; j++ ) {
					scanner = dataSource.skipNonNumbers();
					scanner = dataSource.getNextToken();
				}
			}
		}
		while ( true ) {
			tp.print( dataSource, scanner );
			if ( scanner == endTokenOffset ) {
				break;
			}
			scanner = dataSource.getNextToken();
		}
	}

    void printCoordIndex( TokenPrinter tp, SpaceStructure ss, int offset ) {
        tp.print( "coordIndex [" );
        SpaceEntitySet faces = ss.getEntitySet( SpacePrimitive.Face );
        int numberFaces = faces.getNumberEntities();
        for ( int i = 0; i < numberFaces; i++ ) {
            if ( ss.getConnectivity( i ) == offset ) {
                printFace( tp, ss, faces, faces.getEntity( i ));
            }
        }
        tp.print( "]" );
    }
    
    void printFace( TokenPrinter tp, SpaceStructure ss, SpaceEntitySet faces, SpacePrimitive face ) {
        int numberVertices = faces.getCount( face, SpacePrimitive.Vertex );
        for ( int i = 0; i < numberVertices; i++ ) {
            tp.print( faces.getValue( face, SpacePrimitive.Vertex, i ));
        }
        tp.print( -1 );
    }
    
    void printSkippingFields( TokenPrinter tp, Node ifs, String[] fieldList ) {
        int[] startFieldOffset = new int[fieldList.length];
        int[] endFieldOffset = new int[fieldList.length];
        int scanner = firstIFSfieldOffset;
        int end = lastIFSfieldOffset+1;
        dataSource.setState( scanner );
        if ( end > scanner ) {
            int range = end - scanner;
            BitSet unprintable = new BitSet( range );
            for ( int i = 0; i < fieldList.length; i++ ) {
                Field f = ifs.getField( fieldList[i] );
                if ( f != null ) {
                    int firstInRange = f.getFirstTokenOffset() - scanner;
                    int lastInRange = f.getLastTokenOffset() - scanner;
                    if (( firstInRange >= 0 ) && ( firstInRange < range ) &&
                        ( lastInRange >= 0 ) && ( lastInRange < range ) &&
                        ( lastInRange >= firstInRange )) {
                        for ( int j = firstInRange; j <= lastInRange; j++ ) {
                            unprintable.set( j );
                        }
                    }
                }
            }
            while ( scanner < end ) {
                if ( unprintable.get( scanner - firstIFSfieldOffset )) {
                    scanner = dataSource.getNextToken();
                    continue;
                }
                tp.print( dataSource, scanner );
                scanner = dataSource.getNextToken();
            }
        }
    }
            
        

    public void replaceCoord( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
    }

    public void replaceCoordIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
    }

    public void replaceTexCoordIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
    }
}


