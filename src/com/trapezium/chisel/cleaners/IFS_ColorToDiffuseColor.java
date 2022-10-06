/*
 * @(#)IFS_ColorToDiffuseColor.java
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
import com.trapezium.vrml.node.IndexedFaceSetVerifier;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.chisel.*;

import java.util.BitSet;

/**
 *  This Chisel takes any single colored IFS within a Shape, and modifies it
 *  so that the color is indicated by the "diffuseColor" field of the material
 *  node of the appearance.
 *
 *  The single color is detected if the IFS colorPerVertex is false, and the color
 *  node contains only one color, or if the colorIndex contains only one index value.
 */

public class IFS_ColorToDiffuseColor extends Optimizer {

    static public final int GenAppearance = 0;
    static public final int GenMaterial = 1;
    static public final int GenDiffuseColor = 2;
    static public final int GenDiffuseColorReplacement = 3;
    
    class GenDiffuse {
        int type;
        int c1offset;
        int c2offset;
        int c3offset;
        
        GenDiffuse( Node ifs, int type ) {
            this.type = type;
            setColor( ifs );
        }
        
        void setColor( Node ifs ) {
            Field color = ifs.getField( "color" );
            Field colorIndex = ifs.getField( "colorIndex" );
            if ( colorIndex != null ) {
                int scanner = colorIndex.getFirstTokenOffset();
                dataSource.setState( scanner );
                scanner = dataSource.skipToNumber( 0 );
                if (( scanner != -1 ) && ( scanner <= colorIndex.getLastTokenOffset() )) {
                    int value = dataSource.getIntValue( scanner );
                    scanner = color.getFirstTokenOffset();
                    dataSource.setState( scanner );
                    c1offset = dataSource.skipToNumber( value*3 );
                    scanner = dataSource.getNextToken();
                    c2offset = dataSource.skipToNumber( 0 );
                    scanner = dataSource.getNextToken();
                    c3offset = dataSource.skipToNumber( 0 );
                }
            } else {
                int scanner = color.getFirstTokenOffset();
                dataSource.setState( scanner );
                c1offset = dataSource.skipToNumber( 0 );
                scanner = dataSource.getNextToken();
                c2offset = dataSource.skipToNumber( 0 );
                scanner = dataSource.getNextToken();
                c3offset = dataSource.skipToNumber( 0 );
            }
        }
        
        int getType() {
            return( type );
        }
        
        int getc1() {
            return( c1offset );
        }
        
        int getc2() {
            return( c2offset );
        }
        
        int getc3() {
            return( c3offset );
        }
    }
    
	public IFS_ColorToDiffuseColor() {
		super( "Shape", "Replace single color with diffuse color..." );
	}

	public void attemptOptimization( Node n ) {
	    Node ifs = getSingleColorIFS( n );
	    if ( ifs != null ) {
	        // if either the appearance or material are DEF/USE, do nothing
	        Field appearanceF = n.getField( "appearance" );
	        if ( appearanceF != null ) {
	            Node appNode = appearanceF.getNode();
	            if ( appNode instanceof DEFUSENode ) {
	                DEFUSENode dun = (DEFUSENode)appNode;
	                if ( dun.isDEF() && dun.isUsed() ) {
    	                return;
    	            }
	            }
	            Field materialF = appNode.getField( "material" );
	            if ( materialF != null ) {
	                Node matNode = materialF.getNode();
	                if ( matNode instanceof DEFUSENode ) {
	                    DEFUSENode dun = (DEFUSENode)matNode;
	                    if ( dun.isDEF() && dun.isUsed() ) {
    	                    return;
    	                }
	                }
	            }
	        }
    	    Node appearance = getAppearance( n );
    	    Node material = getMaterial( appearance );
    	    Field color = ifs.getField( "color" );
    	    Field colorIndex = ifs.getField( "colorIndex" );
    	    if ( color != null ) {
        	    replaceRange( color.getFirstTokenOffset(), color.getLastTokenOffset(), null );
        	}
        	if ( colorIndex != null ) {
        	    replaceRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(), null );
        	}
        	Field colorPerVertex = ifs.getField( "colorPerVertex" );
        	if ( colorPerVertex != null ) {
        	    replaceRange( colorPerVertex.getFirstTokenOffset(), colorPerVertex.getLastTokenOffset(), null );
        	}
    	    if ( appearance == null ) {
    	        int addition = 1;
    	        if ( dataSource.sameAs( n.getFirstTokenOffset() + 1, "DEF" )) {
    	            addition = 3;
    	        }
    	        replaceRange( n.getFirstTokenOffset(), n.getFirstTokenOffset() + addition, 
    	            new GenDiffuse( ifs, GenAppearance ));
    	    } else if ( material == null ) {
    	        int addition = 1;
    	        if ( dataSource.sameAs( appearance.getFirstTokenOffset() + 1, "DEF" )) {
    	            addition = 3;
    	        }
    	        replaceRange( appearance.getFirstTokenOffset(), appearance.getFirstTokenOffset() + addition,
    	            new GenDiffuse( ifs, GenMaterial ));
    	    } else {
    	        Field diffuseColor = material.getField( "diffuseColor" );
    	        if ( diffuseColor == null ) {
    	            int addition = 1;
    	            if ( dataSource.sameAs( material.getFirstTokenOffset() + 1, "DEF" )) {
    	                addition = 3;
    	            }
    	            replaceRange( material.getFirstTokenOffset(), material.getFirstTokenOffset() + addition,
    	                new GenDiffuse( ifs, GenDiffuseColor ));
    	        } else {
    	            replaceRange( diffuseColor.getFirstTokenOffset(), diffuseColor.getLastTokenOffset(),
    	                new GenDiffuse( ifs, GenDiffuseColorReplacement ));
    	        }
    	    }
    	}
	}

	Node getAppearance( Node n ) {
	    Field appearance = n.getField( "appearance" );
	    if ( appearance != null ) {
	        return( appearance.getNodeValue() );
	    }
	    return( null );
	}
	
	Node getMaterial( Node appearance ) {
	    if ( appearance != null ) {
	        Field material = appearance.getField( "material" );
	        if ( material != null ) {
	            return( material.getNodeValue() );
	        }
	    }
	    return( null );
	}
	
	/** Return an IndexedFaceSet with a single color in the Shape node provided,
	 *  if not found, returns null.
	 *
	 *  Single color IFS that is acceptable is one in which color node is not
	 *  DEF/USE, or corresponding Appearance or Material node is not DEF/USE
	 */
	Node getSingleColorIFS( Node n ) {
	    Field geometry = n.getField( "geometry" );
	    if ( geometry != null ) {
	        Node geoNode = geometry.getNodeValue();
	        if ( geoNode != null ) {
	            if ( geoNode.getBaseName().compareTo( "IndexedFaceSet" ) == 0 ) {
	                if ( IndexedFaceSetVerifier.isSingleColor( geoNode, dataSource )) {
	                    return( geoNode );
	                }
	            }
	        }
	    }
	    return( null );
	}


	/** Replace the coordIndex, get rid of bad faces, regenerate other fields */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    // null param indicates a color or colorIndex being wiped out
	    //System.out.println( "optimize from " + startTokenOffset + " to " + endTokenOffset );
	    if ( param instanceof GenDiffuse ) {
	        GenDiffuse gd = (GenDiffuse)param;
	        int type = gd.getType();
	        if ( type != GenDiffuseColorReplacement ) {
                for ( int i = startTokenOffset; i <= endTokenOffset; i++ ) {
       	            tp.print( dataSource, i );
       	        }
       	    }
	        if ( type == GenAppearance ) {
	            tp.print( "appearance Appearance { material Material { diffuseColor" );
	            tp.print( dataSource, gd.getc1() );
	            tp.print( dataSource, gd.getc2() );
	            tp.print( dataSource, gd.getc3() );
	            tp.print( " } }" );
	        } else if ( type == GenMaterial ) {
	            tp.print( "material Material { diffuseColor" );
	            tp.print( dataSource, gd.getc1() );
	            tp.print( dataSource, gd.getc2() );
	            tp.print( dataSource, gd.getc3() );
	            tp.print( "}" );
	        } else if (( type == GenDiffuseColor ) || ( type == GenDiffuseColorReplacement )) {
	            tp.print( "diffuseColor" );
	            tp.print( dataSource, gd.getc1() );
	            tp.print( dataSource, gd.getc2() );
	            tp.print( dataSource, gd.getc3() );
	        }
        }
	}
}


