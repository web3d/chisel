/*
 * @(#)DEFResolver.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.visitor.DEFVisitor;
import com.trapezium.parse.TokenEnumerator;
import java.util.Hashtable;

/** The DEFResolver resolves DEF name conflicts when one Scene is merged into another. */
public class DEFResolver {
    // the Scene that is growing
    Scene destinationScene;
    // the Scene that is getting placed in an existing Scene (getting placed in
    // destinationScene)
    Scene originalScene;

    /** Class constructor.
     *
     *  @param destinationScene the scene that is growing
     *  @param originalScene the Scene that is merging into destinationScene
     */
    public DEFResolver( Scene destinationScene, Scene originalScene ) {
        this.destinationScene = destinationScene;
        this.originalScene = originalScene;
    }

    /** Resolve DEF conflicts based on remapping object contained in Scene.
     *
     *  @param newNode the node that has DEF/USE conflicts that need to be resolved
     */
    public void resolve( Node newNode ) {
        Hashtable mapper = destinationScene.getNameMapper( originalScene );
        TokenEnumerator te = destinationScene.getTokenEnumerator();
        if (( mapper != null ) && ( te != null )) {
            int firstTokenOffset = newNode.getFirstTokenOffset();
            int lastTokenOffset = newNode.getLastTokenOffset();
  
            // go through new node token by token, change DEF/USE/ROUTE names
            for ( int i = firstTokenOffset; i < lastTokenOffset; i++ ) {
                if ( te.sameAs( i, "DEF" )) {
                    i++;
                    String newDEFname = (String)mapper.get( te.toString( i ));
                    if ( newDEFname != null ) {
                        te.replace( i, newDEFname );
                    }
                }
            }
        }
        DEFVisitor defVisitor = new DEFVisitor();
        newNode.traverse( defVisitor );
        if ( defVisitor.getNumberDEFs() > 0 ) {
            int defCount = defVisitor.getNumberDEFs();
            for ( int i = 0; i < defCount; i++ ) {
                // if there is a DEF that is unknown, register it
                if ( destinationScene.getDEF( defVisitor.getDEF( i )) == null ) {
                    destinationScene.registerDEF( defVisitor.getDEFnode( i ));
                } else {
                    // def name may have been remapped, if so, change it
                    // in the newNode
                    if ( mapper != null ) {
                        String remappedName = (String)mapper.get( defVisitor.getDEF( i ));
                        if ( remappedName != null ) {
                            DEFUSENode modifiedNode = defVisitor.getDEFnode( i );
                            modifiedNode.setDEFName( remappedName );
                            destinationScene.registerDEF( modifiedNode );
                        }
                    }
                }
            }
        }
    }
}
