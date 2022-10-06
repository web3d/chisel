/*
 * @(#)IFS_Converter.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.node.DEFUSENode;


/**
 *  This base class chisel turns IndexedFaceSets into IndexedLineSets or PointSet
 */
abstract public class IFS_Converter extends Optimizer {
    /** single option, sets emissiveColor because otherwise ILS might
     *  be invisible.
     */
    boolean setEmissiveColor;
    
    /** Preserve original as first in a LOD */
    boolean preserveOriginalInLOD;
    
    /** range for level 0 */
    int level0range;

    /** The Node type we are converting to -- either IndexedLineSet or PointSEt */
    String convertTo;
    
    /** Constructor, only notify on IndexedFaceSets */
	public IFS_Converter( String convertTo ) {
		super( "IndexedFaceSet", "Converting IndexedFaceSet to " + convertTo );
		setEmissiveColor = true;
		this.convertTo = convertTo;
		preserveOriginalInLOD = false;
		level0range = 10;
	}

    /** Always replace the entire containing Shape node */
	public void attemptOptimization( Node n ) {
	    Node ifs = n;
	    n = n.getNodeParent();
        if ( n instanceof DEFUSENode ) {
            n = n.getNodeParent();
        }
	    if ( n != null ) {
    	    replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), n );
    	}
	}


    void preserveField( TokenPrinter tp, Node n, String fieldName ) {
   		Field f = n.getField( fieldName );
   		if ( f != null ) {
  		    tp.printRange( f.getFirstTokenOffset(), f.getLastTokenOffset(), false );
   		}
	}

	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		if ( param instanceof Node ) {
		    Node shape = (Node)param;
		    
		    // if preserving original Shape in LOD 0, we rewrite it here
		    if ( preserveOriginalInLOD ) {
    		    tp.print( "LOD { range [" );
    		    tp.print( level0range );
    		    tp.print( "]" );
    		    tp.print( "level [" );
    		    tp.flush();
    		    tp.printRange( shape.getFirstTokenOffset(), shape.getLastTokenOffset(), false );
    		}
    		
    		Field f = shape.getField( "geometry" );
    		Node ifs = f.getNodeValue();
    		Field fa = shape.getField( "appearance" );
    		Node appearanceNode = null;
    		String appearanceDEFName = null;
    		Node materialNode = null;
    		String materialDEFName = null;
    		boolean preserveMaterial = false;

    		// if we are preserving original in LOD, DEFs are preserved there
    		if (( fa != null ) && !preserveOriginalInLOD ) {
    		    appearanceNode = fa.getNodeValue();
    		    if ( appearanceNode != null ) {
        		    if ( appearanceNode.getParent() instanceof DEFUSENode ) {
        		        DEFUSENode appearanceDEF = (DEFUSENode)(appearanceNode.getParent());
        		        if ( appearanceDEF.isUsed() ) {
            		        appearanceDEFName = appearanceDEF.getDEFName();
            		        preserveMaterial = true;
            		    }
        		    }
        		    Field fm = appearanceNode.getField( "material" );
        		    if ( fm != null ) {
        		        materialNode = fm.getNodeValue();
        		        if ( materialNode != null ) {
        		            if ( materialNode.getParent() instanceof DEFUSENode ) {
        		                DEFUSENode materialDEF = (DEFUSENode)(materialNode.getParent());
        		                if ( materialDEF.isUsed() ) {
        		                    materialDEFName = materialDEF.getDEFName();
        		                    preserveMaterial = true;
        		                }
        		            }
        		        }
        		    }
        		}
        	}
    		        
    		tp.print( "Shape {" );
    		// if there is an appearance node DEFfed, preserve the DEF
    		tp.print( "appearance" );
    		if ( appearanceDEFName != null ) {
    		    tp.print( "DEF" );
    		    tp.print( appearanceDEFName );
    		}
       		tp.print( "Appearance {" );
       		if ( appearanceDEFName != null ) {
           		// if its DEFfed, preserve its texture & textureTransform fields
           		// since they may be used
           		preserveField( tp, appearanceNode, "texture" );
           		preserveField( tp, appearanceNode, "textureTransform" );
           	}
           	// now print the matierial field
           	tp.print( "material" );
           	if ( materialDEFName != null ) {
           	    tp.print( "DEF" );
           	    tp.print( materialDEFName );
           	}
           	tp.print( "Material {" );
           	if ( preserveMaterial ) {
           	    // if its DEFfed, preserve its fields
           	    preserveField( tp, materialNode, "ambientIntensity" );
           	    preserveField( tp, materialNode, "diffuseColor" );
           	    preserveField( tp, materialNode, "shininess" );
           	    preserveField( tp, materialNode, "specularColor" );
           	    preserveField( tp, materialNode, "transparency" );
          	}
          	tp.print( "emissiveColor" );

            boolean printedColor = false;
    		if ( setEmissiveColor ) {
                Field appearance = shape.getField( "appearance" );
                if ( appearance != null ) {
                    appearanceNode = appearance.getNodeValue();
                    if ( appearanceNode != null ) {
	                    Field material = appearanceNode.getField( "material" );
	                    if ( material != null ) {
	                        materialNode = material.getNodeValue();
	                        if ( materialNode != null ) {
	                            Field diffuseColor = materialNode.getField( "diffuseColor" );
	                            if ( diffuseColor != null ) {
	                                tp.printRange( diffuseColor.getFirstTokenOffset() + 1, diffuseColor.getLastTokenOffset(), false );
	                                printedColor = true;
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        if ( !printedColor ) {
	            tp.print( "1 1 1" );
	        }
	        tp.print( "} }" );
	        tp.flush();
	        tp.print( "geometry" );

		    Field coord = ifs.getField( "coord" );
		    int nextStartTokenOffset = ifs.getFirstTokenOffset(); // MLo (overwrote a dead parameter: startTokenOffset)
		    int coordStart = -1;
                    Node coordNode = null; // MLo
		    if ( coord != null ) {
		        coordStart = coord.getFirstTokenOffset();
                        coordNode = coord.getNodeValue(); // MLo (fixed potential NPE error)
		    }
		    
		    boolean coordNodeIsUSE = false;
		    String coordDEFname = null;
		    if ( coordNode != null ) {
    		    VrmlElement vn = coordNode.getParent();
    		    if ( vn instanceof DEFUSENode ) {
    		        DEFUSENode dun = (DEFUSENode)vn;
    		        if ( !dun.isDEF() ) {
        		        coordDEFname = dun.getDEFName();
        		        coordNodeIsUSE = true;
        		    }
    		    }
        	}
		    Field coordIndex = ifs.getField( "coordIndex" );
		    int coordIndexStart = -1;
		    if ( coordIndex != null ) {
		        coordIndexStart = coordIndex.getFirstTokenOffset();
		    }
		    Field color = ifs.getField( "color" );
		    int colorStart = -1;
		    if ( color != null ) {
		        colorStart = color.getFirstTokenOffset();
		    }
		    Field colorIndex = ifs.getField( "colorIndex" );
		    int colorIndexStart = -1;
		    if ( colorIndex != null ) {
		        colorIndexStart = colorIndex.getFirstTokenOffset();
		    }
		    Field colorPerVertex = ifs.getField( "colorPerVertex" );
		    int colorPerVertexStart = -1;
		    if ( colorPerVertex != null ) {
		        colorPerVertexStart = colorPerVertex.getFirstTokenOffset();
		    }
		    Field normal = ifs.getField( "normal" );
		    int normalStart = -1;
		    if ( normal != null ) {
		        normalStart = normal.getFirstTokenOffset();
		    }
		    Field normalIndex = ifs.getField( "normalIndex" );
		    int normalIndexStart = -1;
		    if ( normalIndex != null ) {
		        normalIndexStart = normalIndex.getFirstTokenOffset();
		    }
		    Field normalPerVertex = ifs.getField( "normalPerVertex" );
		    int normalPerVertexStart = -1;
		    if ( normalPerVertex != null ) {
		        normalPerVertexStart = normalPerVertex.getFirstTokenOffset();
		    }
		    int breakOffset = getBreakOffset( coordStart, coordIndexStart );
		    breakOffset = getBreakOffset( breakOffset, colorStart );
		    breakOffset = getBreakOffset( breakOffset, colorIndexStart );
		    breakOffset = getBreakOffset( breakOffset, normalStart );
		    breakOffset = getBreakOffset( breakOffset, normalIndexStart );
		    breakOffset = getBreakOffset( breakOffset, colorPerVertexStart );
		    breakOffset = getBreakOffset( breakOffset, normalPerVertexStart );
		    breakOffset--;
		    tp.print( convertTo );
		    printIFSrange( tp, nextStartTokenOffset + 1, breakOffset, ifs ); // MLo 
		    if ( coordStart != -1 ) {
		        if ( coordNodeIsUSE ) {
		            tp.print( "coord USE" );
		            tp.print( coordDEFname );
		        } else {
                    printIFSrange( tp, coordStart, coord.getLastTokenOffset(), ifs );
                }
		    }
		    if ( coordIndexStart != -1 ) {
		        printCoordIndex( tp, coordIndex );
		    }
		    convertColorInfo( ifs, tp, colorStart, color, colorPerVertexStart, colorPerVertex, colorIndexStart, colorIndex );
		    // end braces for PointSet/ILS and Shape
		    tp.print( "} }" );
		    if ( preserveOriginalInLOD ) {
		        tp.print( "] }" );
		    }
		}
	}
	
	/** Print a range of tokens from an IFS, but skip ccw, convex, creaseAngle and solid
	 *  fields.
	 */
	int[] skipStart;
	int[] skipEnd;
	void printIFSrange( TokenPrinter tp, int start, int end, Node ifs ) {
	    Field ccw = ifs.getField( "ccw" );
	    Field convex = ifs.getField( "convex" );
	    Field creaseAngle = ifs.getField( "creaseAngle" );
	    Field solid = ifs.getField( "solid" );
	    if ( skipStart == null ) {
    	    skipStart = new int[4];
	        skipEnd = new int[4];
	    }
	    for ( int i = 0; i < 4; i++ ) {
	        skipStart[i] = skipEnd[i] = -1;
	    }
	    if ( ccw != null ) {
	        skipStart[0] = ccw.getFirstTokenOffset();
	        skipEnd[0] = ccw.getLastTokenOffset();
	    }
	    if ( convex != null ) {
	        skipStart[1] = convex.getFirstTokenOffset();
	        skipEnd[1] = convex.getLastTokenOffset();
	    }
	    if ( creaseAngle != null ) {
	        skipStart[2] = creaseAngle.getFirstTokenOffset();
	        skipEnd[2] = creaseAngle.getLastTokenOffset();
	    }
	    if ( solid != null ) {
	        skipStart[3] = solid.getFirstTokenOffset();
	        skipEnd[3] = solid.getLastTokenOffset();
	    }
	    for ( int scanner = start; scanner <= end; scanner++ ) {
	        if ( okToPrint( scanner )) {
	            tp.print( dataSource, scanner );
	        }
	    }
	}
	
	/** Check if the token is within the skipping range */
	boolean okToPrint( int offset ) {
	    for ( int i = 0; i < 4; i++ ) {
	        if (( offset >= skipStart[i] ) && ( offset <= skipEnd[i] )) {
	            return( false );
	        }
	    }
	    return( true );
	}


    int getBreakOffset( int a, int b ) {
        if ( a == -1 ) {
            return( b );
        } else if ( b == -1 ) {
            return( a );
        } else if ( a < b ) {
            return( a );
        } else {
            return( b );
        }
    }

    abstract void printCoordIndex( TokenPrinter tp, Field coordIndex );
    abstract void convertColorInfo( Node n, TokenPrinter tp, 
        int colorStart, VrmlElement color,
        int colorPerVertexStart, VrmlElement colorPerVertex,
        int colorIndexStart, VrmlElement colorIndex );

    /** Option interface, allow set emissiveColor to diffuseColor option */
    public int getNumberOptions() {
        return( 3 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        if ( offset < 2 ) {
            return( Boolean.TYPE );
        } else {
            return( Integer.TYPE );
        }
    }

    public String getOptionLabel( int offset ) {
        if ( offset == 0 ) {
            return( "set emissiveColor to diffuseColor" );
        } else if ( offset == 1 ) {
            return( "preserve original IFS in LOD level 0" );
        } else if ( offset == 2 ) {
            return( "level 0 range" );
        } else {
            return( null );
        }
    }

    public Object getOptionValue( int offset ) {
        if ( offset == 0 ) {
            return( booleanToOptionValue( setEmissiveColor ));
        } else if ( offset == 1 ) {
            return( booleanToOptionValue( preserveOriginalInLOD ));
        } else {
            return( intToOptionValue( level0range ));
        }
    }

    public void setOptionValue( int offset, Object value ) {
        if ( offset == 0 ) {
            setEmissiveColor = optionValueToBoolean(value);
        } else if ( offset == 1 ) {
            preserveOriginalInLOD = optionValueToBoolean(value);
        } else {
            level0range = optionValueToInt(value);
        }
    }
}


