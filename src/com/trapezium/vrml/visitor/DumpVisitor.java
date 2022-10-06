/*
 * @(#)DumpVisitor.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.EventIn;
import com.trapezium.vrml.fields.EventOut;
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.fields.NotExposedField;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.Node;
import java.io.PrintStream;

/**
 *  Debugging dump of scene graph.
 *  <P>
 *  Not a nice picture, for debugging...
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 24 Nov 1997
 *
 *  @since           1.0
 */
public class DumpVisitor extends Visitor {

	/** where we dump */
	PrintStream ps;

	/** token level dump also? */
	boolean tokenLevelDump;

	/** only dump user defined fields */
	boolean userDefinedDump = true;

	/** only dump fields that have embedded tokens */
	boolean embeddedTokenDump;

	public DumpVisitor( PrintStream p, TokenEnumerator v ) {
		super( v );
		ps = p;
		tokenLevelDump = false;
		embeddedTokenDump = false;
	}

	public void dumpEmbedded() {
		embeddedTokenDump = true;
	}

	/** Make this visitor dump all tokens too */
	public void dumpTokens() {
		tokenLevelDump = true;
	}

	/** Make this visitor dump only user defined fields, ignore others */
	public void dumpUserDefinedFields() {
		userDefinedDump = true;
	}

	public boolean visitObject( Object a ) {
		if (( a instanceof Node ) || ( a instanceof EventIn ) || ( a instanceof EventOut ) || ( a instanceof ExposedField ) || ( a instanceof NotExposedField )) {
			VrmlElement vElem = (VrmlElement)a;
			int tokenOffset = vElem.getFirstTokenOffset();
			int firstTokLine = -1;
			if ( tokenOffset != -1 ) {
			    firstTokLine = dataSource.getLineNumber( tokenOffset );
			}
			int lastTokenOffset = vElem.getLastTokenOffset();
    		int lastTokLine = -1;
    		if ( lastTokenOffset != -1 ) {
    		    lastTokLine = dataSource.getLineNumber( lastTokenOffset );
    		}
    		String addition = "";
    		if ( firstTokLine != -1 ) {
    		    if (( firstTokLine == lastTokLine ) || ( lastTokLine == -1 )) {
    		        addition = ", line " + firstTokLine;
    		    } else if ( firstTokLine != lastTokLine ) {
        		    addition = ", line " + firstTokLine + " to " + lastTokLine;
        		}
    		}
			if ( tokenOffset == -1 ) {
			    System.out.println( spacer() + vElem.getBaseName() + " (no tokens)" );
			} else {
				if ( vElem instanceof DEFUSENode ) {
					DEFUSENode dun = (DEFUSENode)vElem;
					if ( dun.isDEF() ) {
						ps.println( spacer() + "DEF " + dun.getId() + addition );
					} else {
						ps.println( spacer() + "USE " + dun.getId() + addition );					}
				} else {
				    if ( dataSource == null ) System.out.println( "HEY datasource is null!" );
					String firstTokString = dataSource.toString( tokenOffset );
					String lastTokString = "** undefined **";
					if ( lastTokenOffset != -1 ) {
						lastTokString = dataSource.toString( lastTokenOffset );
						if ( vElem instanceof MFFieldValue ) {
						    MFFieldValue mf = (MFFieldValue)vElem;
						    int nchildren = mf.getRawValueCount();
						    if ( nchildren == 0 ) {
						        nchildren = mf.numberChildren();
						    }
   							ps.println( spacer() + firstTokString + addition + ", " + mf.getRawValueCount() + " values " );
   						} else {
   							ps.println( spacer() + firstTokString + addition );
   						}
					} else {
						ps.println( spacer() + firstTokString + addition );
					}
				}
    		} 
//			return( true );
		}
/*		if ( a instanceof DEFUSENode ) {
		DEFUSENode dun = (DEFUSENode)a;
		ps.println( spacer() + a.getClass().getName() + ", " + dun.isDEF() + ", name '" + dun.getName() );
		} else if ( a instanceof Node ) {
		    Node n = (Node)a;
		    ps.println( spacer() + "node name '" + n.getName() + "'" );
		} else {
		ps.println( spacer() + a.getClass().getName() + ", xxxxxx" );
		}*/
		return( true );
	}
}

