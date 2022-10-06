/*
 * @(#)NameFixer.java
 *
 * Copyright (c) 2005 Michael N. Louka.
 * (Based on NameShortener code Copyright (c) 1998 Trapezium Development LLC).
 * All rights reserved.
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
 *  This chisel fixes invalid DEF names and adjusts all ROUTEs accordingly.
 *
 */
public class NameFixer extends Optimizer {
    
    // key is Scene, value is remapping Remapper,
    Hashtable sceneToRemapper;
    
    public NameFixer() {
        super( "DEFUSENode", "Simplify DEF names..." );
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
                SimplifiedNamesRemapper remapper = getRemapper( s );
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
            SimplifiedNamesRemapper remapper = getRemapperNoCreate( s );
            if ( remapper != null ) {
                if (( remapper.getMapping( sourceObject ) != null ) || ( remapper.getMapping( destObject ) != null )) {
                    replaceRange( route.getFirstTokenOffset(), route.getLastTokenOffset(), route );
                }
            }
        }
    }
    
    
    /** Get a name remapper for a scene, creating one if necessary */
    SimplifiedNamesRemapper getRemapper( Scene s ) {
        SimplifiedNamesRemapper result = (SimplifiedNamesRemapper)sceneToRemapper.get( s );
        if ( result == null ) {
            result = new SimplifiedNamesRemapper( s );
            sceneToRemapper.put( s, result );
        }
        return( result );
    }
    
    /** Get a name remapper for a scene, just returns null if none found.
     */
    SimplifiedNamesRemapper getRemapperNoCreate( Scene s ) {
        return( (SimplifiedNamesRemapper)sceneToRemapper.get( s ));
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
            SimplifiedNamesRemapper remapper = getRemapperNoCreate( s );
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
class SimplifiedNamesRemapper {
    // key is old name, value is new name
    Hashtable nameMapping;
    
    // the scene associated with this remapper
    Scene theScene;
    
    public SimplifiedNamesRemapper( Scene s ) {
        theScene = s;
        nameMapping = new Hashtable();
    }
    
    public String remap( String originalName ) {
        String result = (String)nameMapping.get( originalName );
        if ( result != null ) {
            return( result );
        } else {
            result = simplifyName(originalName.trim());
            nameMapping.put( originalName, result );
            return( result );
        }
    }
    
    /** Remap if possible, otherwise just return string unchanged */
    public String getPossibleRemap( String originalName ) {
        String result = getMapping( originalName );
        if ( result == null ) {
            return originalName;
        } else {
            return result;
        }
    }
    
    public String getMapping( String originalName ) {
        return( (String)nameMapping.get( originalName ));
    }
    
    /**
     * Get a unique name
     *
     */
    protected String simplifyName(String originalName) {
        int count = 0;
        String name = getSimplifiedName(originalName);
        if (name.compareTo(originalName) != 0) {
            String test = name;
            while (true) {
                if (count > 0) {
                    name = test + count;
                }
                if (theScene.getDEF(name) == null) {
                    break;
                }
                count++;
            }
        }
        return name;
    }
    
    /**
     * If first character is a digit then replace with an underscore.
     *
     * Any other characters that are not either digits or letters are
     * replaced with underscores.
     *
     */
    private String getSimplifiedName(String originalName) {
        StringBuffer newName = new StringBuffer(originalName);
        if (!(Character.isLetter(newName.charAt(0)) || newName.charAt(0) == '_')) {
            newName.setCharAt(0, '_');
        }
        for(int i = 1; i < (originalName.length() - 1); i++){
            if (!(Character.isLetterOrDigit(newName.charAt(i)) || newName.charAt(i) == '_')) {
                newName.setCharAt(i, '_');
            }
        }
        return ((newName.toString()).trim());
    }
}

