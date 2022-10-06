/*
 * @(#)SpecHumanoid.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.humanoid;

import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import com.trapezium.parse.TextLineEnumerator;
import com.trapezium.parse.TextLineParser;
import com.trapezium.pattern.Visitor;
import com.trapezium.util.StringUtil;

/**
 *  Loads the "humanoid" spec file for use in humanoid validation.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 30 Dec 1997
 *
 *  @since           1.0
 */
public class SpecHumanoid {
	String version;
	SpecJoint root;
	Vector jointNames;
	Vector jointList;
	boolean loaded;
	int prefixLength = -2;

	public void setPrefixLength( int l ) {
		prefixLength = l;
	}

	public int getTotalCount() {
		return( jointList.size() - 1 );
	}
	public int getPrefixLength() {
		return( prefixLength );
	}

	public boolean failedLoading() {
		return( !loaded );
	}

	public void validateRoot() {
		root.validate();
	}

	public int summarize( PrintStream ps ) {
		return( root.summarize( ps ));
	}

	public String getRootName() {
		return( root.getName() );
	}

	public boolean isJointName( String name ) {
		return( getOffset( name ) != -1 );
	}

	public boolean checkHierarchy( String childName, String parentName ) {
		SpecJoint child = locateJoint( childName );
		SpecJoint parent = locateJoint( parentName );
		if ( child.getLevel() > parent.getLevel() ) {
			child.validate();
			return( true );
		} else {
			return( false );
		}
	}

	public void addJoint( SpecJoint newJoint, SpecJoint lastJoint ) {
		if ( newJoint.getLevel() > lastJoint.getLevel() ) {
			lastJoint.addChild( newJoint );
		} else if ( newJoint.getLevel() == lastJoint.getLevel() ) {
			SpecJoint parent = lastJoint.getParent();
			parent.addChild( newJoint );
		} else {
			addJoint( newJoint, lastJoint.getParent() );
		}
	}

	public SpecJoint createJoint( String line ) {
		int level = getLevel( line );
		String name = getName( line );
		SpecJoint joint = new SpecJoint( name, level );
		jointNames.addElement( name );
		jointList.addElement( joint );
		return( joint );
	}

	public SpecJoint locateJoint( String name ) {
		int offset = getOffset( name );
		return( (SpecJoint)jointList.elementAt( offset ));
	}

	public int getOffset( String name ) {
		for ( int i = 0; i < jointNames.size(); i++ ) {
			String s = (String)jointNames.elementAt( i );
			if ( name.indexOf( s ) == prefixLength ) {
				return( i );
			}
		}
		return( -1 );
	}

	public SpecHumanoid( String humanoidFileName ) {
		jointNames = new Vector();
		jointList = new Vector();
		try {
			File f = new File( humanoidFileName );
			TextLineEnumerator tle = new TextLineEnumerator( new TextLineParser( f ));
			SpecJoint lastJoint = null;
			for ( int i = 0; i < tle.size(); i++ ) {
				String line = tle.getLineAt( i );
				if ( line.length() == 0 ) {
					continue;
				} else if ( isComment( line )) {
					continue;
				} else {
					SpecJoint sj = createJoint( line );
					if ( root == null ) {
						lastJoint = root = sj;
					} else {
						addJoint( sj, lastJoint );
						lastJoint = sj;
					}
				}
			}
			loaded = true;
		} catch ( Exception e ) {
			System.out.println( "Could not load file '" + humanoidFileName + "', humanoid checking disabled." );
			loaded = false;
		}
	}

	public boolean isComment( String line ) {
		for ( int i = 0; i < line.length(); i++ ) {
			if (( line.charAt( i ) == ' ' ) || ( line.charAt( i ) == '\t' ) || ( line.charAt( i ) == '|' )) {
				continue;
			} else if ( line.charAt( i ) == '#' ) {
				return( true );
			} else {
				return( false );
			}
		}
		// empty lines same as comment
		return( true );
	}

	public int getLevel( String line ) {
		int level = 0;
		for ( int i = 0; i < line.length(); i++ ) {
			if (( line.charAt( i ) == ' ' ) || ( line.charAt( i ) == '\t' ) || ( line.charAt( i ) == '|' )) {
				level++;
				continue;
			} else {
				return( level );
			}
		}
		return( level );
	}

	public String getName( String line ) {
		StringBuffer sb = new StringBuffer();
		int level = getLevel( line );
		for ( int i = level; i < line.length(); i++ ) {
			char c = line.charAt( i );
			if (( c >= 'a' ) && ( c <= 'z' )) {
				sb.append( c );
			} else if (( c >= 'A' ) && ( c <= 'Z' )) {
				sb.append( c );
			} else if (( c >= '0' ) && ( c <= '9' )) {
				sb.append( c );
			} else if ( c == '_' ) {
				sb.append( c );
			} else {
				break;
			}
		}
		return( new String( sb ));
	}
}

class SpecJoint {
	String name;
	int level;
	SpecJoint parent;
	Vector children;
	boolean validated = false;

	public int summarize( PrintStream ps ) {
		int missingCount = 0;
		if ( level == 0 ) {
			System.out.println( "Joint hierarchy is:" );
			missingCount += pvalidated( ps, "", name, validated );
		} else {
			missingCount += pvalidated( ps, StringUtil.spacer( level ), name, validated );
		}
		if ( children != null ) {
		    int nChildren = children.size();
			for ( int i = 0; i < nChildren; i++ ) {
				SpecJoint sj = (SpecJoint)children.elementAt( i );
				missingCount += sj.summarize( ps );
			}
		}
		return( missingCount );
	}


	public int pvalidated( PrintStream ps, String space, String name, boolean validated ) {
		if ( validated ) {
			ps.println( space + name );
			return( 0 );
		} else {
			return( 1 );
		}
	}
			
	public void validate() {
		validated = true;
	}

	public SpecJoint( String name, int level ) {
		this.level = level;
		this.name = name;
		parent = null;
		children = null;
	}

	public void addChild( SpecJoint child ) {
		if ( children == null ) {
			children = new Vector();
		}
		children.addElement( child );
		child.setParent( this );
	}

	public void setParent( SpecJoint parent ) {
		this.parent = parent;
	}

	public SpecJoint getParent() {
		return( parent );
	}

	public int getLevel() {
		return( level );
	}

	public String getName() {
		return( name );
	}
}
