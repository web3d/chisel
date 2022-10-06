/*
 * @(#)UsageInfo.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import java.util.BitSet;

/**
 *  Tracks usage info on a set of coordinates.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 7 May 1998, make serializable
 *  @version         1.1, 4 Dec 1997
 *
 *  @since           1.0
 */
public class UsageInfo implements java.io.Serializable {
	BitSet usageBits;
	int count;
	Node owner;
	int factor;
	String indexString;
	String valueName;

	/** Constructor for UsageInfo
	 *
	 *  @param count number of values in the item, the item may be spatial
	 *     coordinates, colors, normals, or texture coordinates.
	 *  @param owner the IndexedFaceSet or IndexedLineSet of the fields
	 *     being tracked.
	 *  @param factor the factor 2x or 3x number of float values in each
	 *     actual value
	 *  @param indexString name of the index field
	 *  @param valueName name of the value field
	 */
	public UsageInfo( int count, Node owner, int factor, 
		String indexString, String valueName ) {
		this.count = count;
		this.owner = owner;
		this.factor = factor;
		this.indexString = indexString;
		this.valueName = valueName;
		usageBits = new BitSet( count );
	}

	public String getValueName() {
		return( valueName );
	}

	public String getIndexString() {
		return( indexString );
	}

	public BitSet getUsageBits() {
		return( usageBits );
	}

	public int getCount() {
		return( count );
	}

	public Node getOwner() {
		return( owner );
	}

	public int getFactor() {
		return( factor );
	}
}
