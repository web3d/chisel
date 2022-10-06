/*
 * @(#)NameShortener.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.Scene;
import com.trapezium.util.NameGenerator;
import com.trapezium.chisel.*;

import java.util.Hashtable;


/**
 *  This chisels shortens DEF names and adjusts all ROUTEs accordingly
 *
 *  (later version should do the same for PROTO field names, but this
 *  gets a little tricky due to unknown EXTERNPROTO references).
 *
 *  (or could just shorten Script node field names, but this too gets
 *  tricky due to javascript parsing).
 */
public class NameShortener extends Optimizer {

    // key is Scene, value is remapping Remapper,
    Hashtable sceneToRemapper;

	public NameShortener() {
		super( "DEFUSENode", "Shorten DEF names..." );
		reset();
	}
	
	public void reset() {
		sceneToRemapper = new Hashtable();
	}

    /** This Chisel gets access to ROUTEs */
    public boolean isROUTElistener() {
        return( true );
    }

    /** This Chisel gets access to DEFUSENodes */
    public boolean isDEFUSElistener() {
        return( true );
    }

	/** This Chisel gets access to the interior of PROTOs */
	public boolean isPROTOlistener() {
	    return( true );
	}

    /** Set up name shortening parameters for a DEFUSENode.
     *
     *  @param n a Node, ignored unless it is a DEF or USE node
     */
 	public void attemptOptimization( Node n ) {
 	    if ( n instanceof DEFUSENode ) {
 	        DEFUSENode dun = (DEFUSENode)n;
 	        Scene s = (Scene)n.getScene();
 	        if ( s != null ) {
 	            Remapper remapper = getRemapper( s );
 	            String originalName = dun.getDEFName();
 	            int nameToken0 = n.getFirstTokenOffset();
 	            int nameToken1 = dataSource.getNextToken( nameToken0 );
 	            if (( nameToken1 != -1 ) && ( originalName != null )) {
 	                replaceRange( nameToken0, nameToken1, remapper.remap( originalName ));
 	            }
 	        }
		}
	}

    
    /** Set up name shortening parameters for a ROUTE.  This method assumes
     *  that the necessary remapper has already been created when the
     *  DEF name was encountered.  If not, the ROUTE is not changed.
     *
     *  @param route the ROUTE referring to DEF names to be shortened.
     */
	public void attemptOptimization( ROUTE route ) {
        String sourceObject = route.getSourceDEFname();
        String destObject = route.getDestDEFname();
        Scene s = (Scene)route.getScene();
        if (( s != null ) && ( sourceObject != null ) && ( destObject != null )) {
            Remapper remapper = getRemapperNoCreate( s );
            if ( remapper != null ) {
                if (( remapper.getMapping( sourceObject ) != null ) || ( remapper.getMapping( destObject ) != null )) {
                     replaceRange( route.getFirstTokenOffset(), route.getLastTokenOffset(), route );
                }
            }
        }
    }

	
	/** Get a name remapper for a scene, creating one if necessary */
	Remapper getRemapper( Scene s ) {
	    Remapper result = (Remapper)sceneToRemapper.get( s );
	    if ( result == null ) {
	        result = new Remapper( s );
	        sceneToRemapper.put( s, result );
	    }
	    return( result );
	}
	
	/** Get a name remapper for a scene, just returns null if none found.
	 */
	Remapper getRemapperNoCreate( Scene s ) {
	    return( (Remapper)sceneToRemapper.get( s ));
	}


	// Do nothing, entire node or ROUTE is removed
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    // String param means we are just replacing a name
	    if ( param instanceof String ) {
	        String s = (String)param;
	        tp.print( dataSource, startTokenOffset );
	        tp.print( s );
	    } else if ( param instanceof ROUTE ) {
	        ROUTE route = (ROUTE)param;
	        Scene s = (Scene)route.getScene();
	        Remapper remapper = getRemapperNoCreate( s );
	        if ( s != null ) {
	            tp.flush();
	            tp.print( "ROUTE" );
	            String source = remapper.getPossibleRemap( route.getSourceDEFname() ) + "." + route.getSourceFieldName();
	            tp.print( source );
	            tp.print( "TO" );
	            String dest = remapper.getPossibleRemap( route.getDestDEFname() ) + "." + route.getDestFieldName();
	            tp.print( dest );
	        }
	    }
	}
}


/**
 *  This class is used to remap old names to new names within the name space
 *  of a Scene.
 */
class Remapper {
    // key is old name, value is new name
    Hashtable nameMapping;

    // the scene associated with this remapper
    Scene theScene;

    // generates unique names for scene
    NameGenerator nameGenerator;

    public Remapper( Scene s ) {
        theScene = s;
        nameMapping = new Hashtable();
        nameGenerator = new NameGenerator();
    }

    public String remap( String originalName ) {
        String result = (String)nameMapping.get( originalName );
        if ( result != null ) {
            return( result );
        } else {
            result = generateName();
            nameMapping.put( originalName, result );
            return( result );
        }
    }

    /** Remap if possible, otherwise just return string unchanged */
    public String getPossibleRemap( String originalName ) {
        String result = getMapping( originalName );
        if ( result == null ) {
            return( originalName );
        } else {
            return( result );
        }
    }

    public String getMapping( String originalName ) {
        return( (String)nameMapping.get( originalName ));
    }

    String generateName() {
        while ( true ) {
            String test = nameGenerator.generateName();
            if ( theScene.getDEF( test ) == null ) {
                return( test );
            }
        }
    }
}

