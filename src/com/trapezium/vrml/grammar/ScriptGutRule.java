/*
 * @(#)ScriptGutRule.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.FieldId;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ISField;

/**
 *  Creates the scene graph component for the internals of a Script node.
 *
 *  This is much different than other built in nodes because Script
 *  nodes allow user defined interface (field) extensions.
 *  
 *  Grammar handled by "Build" method:
 *  <PRE>
 *  scriptGut:
 *    nodeGut
 *    restrictedInterfaceDeclaration
 *    eventIn fieldType eventInId IS eventInId
 *    eventOut fieldType eventOutId IS eventOutId
 *    field fieldType fieldId IS fieldId
 *  </PRE>    
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 9 April 1998, made public
 *  @version         1.1, 19 Jan 1998
 *
 *  @since           1.0
 */
public class ScriptGutRule {
    PROTORule protoRule;
    ROUTERule routeRule = new ROUTERule();
    RestrictedInterfaceDeclarationRule restrictedInterfaceDeclarationRule;
    FieldFactory fieldFactory;
    NodeBodyRule nodeBodyRule;
    
    /** Constructor for grammar rule that creates a Script field */
    public ScriptGutRule( NodeRule nodeRule ) {
        fieldFactory = new FieldFactory( nodeRule );
        restrictedInterfaceDeclarationRule = new ScriptRestrictedInterfaceDeclarationRule( nodeRule );
        nodeBodyRule = new NodeBodyRule( nodeRule );
        protoRule = nodeRule.getPROTORule();
    }
    
	/** Build onto a Node by adding on individual NodeGuts */
	public void Build( int tokenOffset, TokenEnumerator v, Scene scene, Node parent,
		PROTO protoParent ) {
		GrammarRule.Enter( "ScriptGutRule.Build" );
		if ( v.sameAs( tokenOffset, "PROTO" )) {
			protoRule.Build( tokenOffset, v, scene, parent );
		} else if ( v.sameAs( tokenOffset, "ROUTE" )) {
			routeRule.Build( tokenOffset, v, scene, parent );
		} else {
			String fieldName = v.toString( tokenOffset );
			if (( fieldName.compareTo( "eventIn" ) == 0 ) || 
				( fieldName.compareTo( "eventOut" ) == 0 ) || 
				( fieldName.compareTo( "field" ) == 0 )) {
				restrictedInterfaceDeclarationRule.Build( tokenOffset, v, scene, parent );
			} else if ( fieldName.compareTo( "exposedField" ) == 0 ) {
    			FieldId fieldId = new FieldId( tokenOffset, v );
				parent.addChild( fieldId );
				fieldId.setError( "Script nodes cannot have exposedFields." );
			} else {
			    nodeBodyRule.Build( tokenOffset, v, scene, parent );
				if ( fieldName.compareTo( "url" ) == 0 ) {
					if ( javascript( tokenOffset, v )) {
						addScriptFunctions( parent, tokenOffset, v );
					}
				}
			}
		}
		GrammarRule.Exit( "ScriptGutRule.Build" );
	}

    //
    //  This needs some work, not detecting javascript correctly.
    //  Bug is when url [ "url1", "url2" ]
    //  In this case, have to check each url for javascript functions.
    //  Similarly "addScriptFunctions" has to handle this case.
    //  UrlVisitor probably has to deal with this as well.  Need a different design 
    //  in this case.
    //
	boolean javascript( int tokenOffset, TokenEnumerator v ) {
        int state = v.getState();
        v.setState( tokenOffset );
		tokenOffset = v.getNextToken();
		if ( tokenOffset != -1 ) {
    		if ( v.isLeftBracket( tokenOffset )) {
    		    // partial fix, if we have:  url [ "url1", ... ] 
    		    // here we just check "url1" for javascript
    		    tokenOffset = v.getNextToken();
    		    if ( tokenOffset != -1 ) {
    		        if ( v.isQuotedString( tokenOffset )) {
    		            String tokString = v.toString( tokenOffset );
    		            if ( tokString.indexOf( "javascript: " ) > 0 ) {
    		                v.setState( state );
    		                return( true );
    		            } else if ( tokString.indexOf( "vrmlscript:" ) > 0 ) {
    		                v.setState( state );
    		                return( true );
    		            }
    		        }
    		    }
    		} else {
        		String qs = v.toString( tokenOffset );
        		if ( v.isQuotedString( tokenOffset )) {
        			String tokString = v.toString( tokenOffset );
        			if ( tokString.indexOf( "javascript:" ) > 0 ) {
        			    v.setState( state );
        				return( true );
        			} else if ( tokString.indexOf( "vrmlscript:" ) > 0 ) {
        			    v.setState( state );
        				return( true );
        			}
        		}
       		}
       	}
		v.setState( state );
		return( false );
	}


	/** add Javascript functions direcly from token stream.
	 *
	 *  This is used when the Javascript is embedded directly in the VRML file
	 *  as part of the url string, using the "javascript: ..." format.
	 *
	 *  The tokens are examined one by noe until a token is found that
	 *  is not part of a quoted string.  As tokens are examined, any functions
	 *  fuond are added to the parent node.
	 *
	 *  @param  parent  script node parent
	 *  @param  firstTokenOffset  start of the javascript url
	 */
	void addScriptFunctions( Node parent, int firstTokenOffset, TokenEnumerator v ) {
	    int state = v.getState();
	    v.setState( firstTokenOffset );
		int scannerOffset = v.getNextToken();
		while ( scannerOffset != -1 ) {
			if ( v.isQuotedString( scannerOffset ) || v.isContinuationString( scannerOffset )) {
				String s = v.toString( scannerOffset );
				VRML97.addFunction( s, parent );
			} else {
				break;
			}
			scannerOffset = v.getNextToken();
		}
		v.setState( state );
	}
}
