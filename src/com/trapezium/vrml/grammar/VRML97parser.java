/*
 * @(#)VRML97parser.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.ErrorElement;
import com.trapezium.vrml.visitor.ParentSetter;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Main class for parsing a VRML 2.0 file.
 *
 *  A Scene is the root of a VRML element hierarchy.  This is a simple hierarchy where each
 *  VrmlElement may or may not have children.  Much of the additional functionality required
 *  is implemented by using the "Visitor Pattern" on this hierarchy.  This pattern encapsulates
 *  features in the visitor objects.
 *
 *  Within the SceneRule factory, the above parameters eventually turn into a Token enumerator,
 *  and the Scene is constructed as follows:
 *
 *            TokenEnumerator tokenEnumerator = ...
 *            Scene scene = new Scene( fileUrl, tokenEnumerator );
 *            VRML97parser parser = new VRML97parser();
 *            parser.Build( tokenEnumerator, scene );
 *
 *  The convention followed by this and all other grammar rules is that a static public
 *  "Build" method is used to add children to an object.
 *
 *  The SceneRule handles the following portion of the grammar from the VRML 2.0 spec:
 *
 *   vrmlScene ::=
 *        statements ;
 *   statements ::=
 *        statement |
 *        statement statements |
 *        empty ;
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 2 April 1998, allow inclusion of first token in parsing
 *  @version         1.1, 21 Jan 1998
 *
 *  @since           1.0
 */
public class VRML97parser {
    NodeRule nodeRule;
    StatementRule statementRule;
    
    // PROTOs use same parser as base
    static public VRML97parser singleton = null;

    /** Parser with no DEFNameFactory and no error reporting limits */
    public VRML97parser() {
        this( null );
    }
    
    /** Parser with an optional DEFNameFactory for generating DEF names.
     *
     *  If the "Build" method gets called with a Scene that also has
     *  a DEFNameFactory, that DEFNameFactory will replace the VRML97parser
     *  DEFNameFactory.
     */
    public VRML97parser( DEFNameFactory defNameFactory ) {
        nodeRule = new NodeRule( defNameFactory );
        statementRule = new StatementRule( nodeRule );
        singleton = this;
    }
    
	/**
	 *  Apply the SceneRule to update the Scene.
	 */
	public void Build( TokenEnumerator v, Scene scene ) {
	    String line = v.getLineAt( 0 );
	    if ( line.indexOf( "#VRML V2.0 utf8" ) != 0 ) {
	        scene.addChild( new ErrorElement( 0, "Bad header, expected 'VRML V2.0 utf8'" ));
	    } else {
    	    Build( v, scene, 0 );
    	}
	}
	
	/**
	 *  Build Scene, startToken needed for dynamic generation.  Normally
	 *  this is 0, which is the header, which is not ignored by the parsing.
	 *  For dynamically generated Nodes, with no header, this is set to
	 *  -1 so that first token is not ignored in parsing.
	 */
	public void Build( TokenEnumerator v, Scene scene, int startToken )  {
	    v.setState( startToken );
	    
	    // use the scene's DEFNameFactory, if any
	    if ( scene.getDEFNameFactory() != null ) {
	        nodeRule.setDEFNameFactory( scene.getDEFNameFactory() );
	    }
		Build( v, scene, scene, "" );
		
		// necessary to prevent infinite loop on visitors if
		// scene graph has Script field self references
		ParentSetter ps = new ParentSetter( v );
		scene.traverse( ps );
	}

	/**
	 *  Build either an embedded Scene (for PROTOs), or a Scene from a file.  If the
	 *  "terminator" is a "}", we are building an embedded Scene.
	 */
	void Build( TokenEnumerator v, Scene scene, VrmlElement parent, String terminator ) {
		GrammarRule.Enter( "SceneRule.Build" );
		scene.setFirstTokenOffset( v.getCurrentTokenOffset() );
		int tokenOffset = -1;
		while ( true ) {
			tokenOffset = v.getNextToken();
			if ( tokenOffset == -1 ) {
				break;
			}
			if ( v.sameAs( tokenOffset, terminator )) {
				break;
			}
			statementRule.Build( tokenOffset, v, scene, parent );
		}
		if ( tokenOffset == -1 ) {
    		scene.setLastTokenOffset( v.getCurrentTokenOffset() - 1 );
    	} else {
    	    scene.setLastTokenOffset( v.getCurrentTokenOffset() );
    	}
		GrammarRule.Exit( "SceneRule.Build" );
	}
}

