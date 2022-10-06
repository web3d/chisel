/*
 * @(#)HumanoidVisitor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.humanoid;

import java.io.PrintStream;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.SFStringValue;
import com.trapezium.vrml.Value;
import com.trapezium.util.StringUtil;

/**
 *  Visits all DEF nodes to check against humanoid 1.0 spec.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 22 Dec 1997
 *
 *  @since           1.0
 */
public class HumanoidVisitor extends Visitor {
	SpecHumanoid humanoid;
	String prefix;
	public HumanoidVisitor( SpecHumanoid humanoid, TokenEnumerator v ) {
	    super( v );
		this.humanoid = humanoid;
	}
	public boolean visitObject( Object a ) {
		if ( a instanceof DEFUSENode ) {
			DEFUSENode dun = (DEFUSENode)a;
			if ( dun.isDEF() ) {
				String childName = dun.getId();
				Node n = dun.getNode();
				if ( childName.indexOf( humanoid.getRootName() ) >= 0 ) {
					humanoid.setPrefixLength( childName.indexOf( humanoid.getRootName() ));
					prefix = childName.substring( 0, childName.indexOf( humanoid.getRootName() ));
					humanoid.validateRoot();
				} else if ( humanoid.isJointName( childName )) {
					if ( childName.indexOf( prefix ) != 0 ) {
						VrmlElement defId = dun.getChildAt( 0 );
						defId.setError( "expected prefix to be '" + prefix + "'" );
					}
					if ( !n.isPROTOnode() ) {
						VrmlElement defId = dun.getChildAt( 0 );
						defId.setError( "name is reserved for Joint PROTO" );
					} else if ( !dataSource.sameAs( n.getFirstTokenOffset(), "Joint" )) {
						VrmlElement defId = dun.getChildAt( 0 );
						defId.setError( childName + " must be Joint PROTO, not " + dataSource.toString( n.getFirstTokenOffset() ));
					} else {
						Field name = n.getField( "name" );
						if ( name == null ) {
							n.setError( "name field not found" );
						} else {
							FieldValue fv = name.getFieldValue();
							if ( fv instanceof SFStringValue ) {
								Value v = (Value)fv.getChildAt( 0 );
								String nameValue = StringUtil.stripQuotes( v.getName() );
								if ( childName.indexOf( nameValue ) < 0 ) {
									v.setError( "Expected name to be \"" + childName.substring( humanoid.getPrefixLength(), childName.length() ) + "\"" );
								}
							} else {
								n.setError( "Missing Joint name field set to \"" + childName + "\"" );
							}
						}
					}
					if ( childName.compareTo( humanoid.getRootName() ) == 0 ) {
						humanoid.validateRoot();
					} else {
						DEFUSENode defParent = dun.getDEFparent();
						while ( defParent != null ) {
							String parentName = defParent.getId();
							if ( humanoid.isJointName( parentName )) {
								if ( !humanoid.checkHierarchy( childName, parentName )) {
									dun.setError( "Not a valid child for " + parentName );
									return( true );
								}
							}
							defParent = defParent.getDEFparent();
						}
					}
				}
			}
		}
		return( true );
	}

	public void summarize( PrintStream ps ) {
		int missingCount = humanoid.summarize( ps );
		if ( missingCount != 0 ) {
			int usedCount = humanoid.getTotalCount() - missingCount;
			System.out.println( "File uses " + usedCount + " of " + humanoid.getTotalCount() + " spec joints" );
		}
	}
}
