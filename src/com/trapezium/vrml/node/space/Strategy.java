/*
 * @(#)Strategy.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.space;

/** Strategy provides the way a SpaceEntitySet's elements viewed.
 */
public class Strategy {

	// SpaceStructure reference is necessary for converting from one strategy to another
	SpaceStructure spaceStructure;

	// the environment indicated by the sets of SpacePrimitives associated with this strategy
	// The baseType indicates how to view the center of the primitive:  
	//    SpacePrimitive.Vertex, SpacePrimitive.Edge, SpacePrimitive.Face
	int baseType;

	// The numbers surrounding the base type are offsets into the vertex, edge, or face
	// SpaceEntitySets.  These offsets may indicate vertices, edges, or faces, and they
	// may be mixed together.  Some offsets may be invalid.  The mix-in pattern is indicated
	// by this "environment" field.
	//
	// The first entry in the surrounding environment may be a SpacePrimitive.Vertex,
	// SpacePrimitive.Edge, or SpacePrimitive.Face, so only one of these is true.
	boolean[] env_firstEntry;

	// There can be 0, 1, or 2 entries in the surrounding environment.  This indicates
	// which, with offset SpacePrimitive.Vertex, SpacePrimitive.Edge, SpacePrimitive.Face
	boolean[] hasEntry;

	// Some operations can invalidate entries.  So if there is no entry, the corresponding
	// field here is also false.  If there is an entry, the field here starts out true, but
	// some operation may later change it to false.
	boolean[] entryIsValid;

	public Strategy( SpaceStructure spaceStructure, int baseType ) {
		this.spaceStructure = spaceStructure;
		this.baseType = baseType;
		env_firstEntry = new boolean[3];
		hasEntry = new boolean[3];
		entryIsValid = new boolean[3];
		for ( int i = 0; i < 3; i++ ) {
			hasEntry[i] = false;
			entryIsValid[i] = false;
			env_firstEntry[i] = false;
		}
	}

	void dumpValues( SpacePrimitive sp, int type ) {
		if ( getCount( sp, type ) > 0 ) {
			StringBuffer sb = new StringBuffer( "    " );
			for ( int i = 0; i < getCount( sp, type ); i++ ) {
				sb.append( getValue( sp, type, i ));
				sb.append( ", " );
			}
			String s = new String( sb );
			System.out.println( s );
		}
	}
	public void dump( SpacePrimitive sp, String header ) {
		System.out.println( "  *** " + header + " at " + sp.getX() + ", " + sp.getY() + ", " + sp.getZ() + " ***" );
		System.out.println( "  Vertex: firstEntry " + env_firstEntry[0] + ", hasEntry " + hasEntry[0] + ", entry valid " + entryIsValid[0] );
		System.out.println( "  Edge: firstEntry " + env_firstEntry[1] + ", hasEntry " + hasEntry[1] + ", entry valid " + entryIsValid[1] );
		System.out.println( "  Face: firstEntry " + env_firstEntry[2] + ", hasEntry " + hasEntry[2] + ", entry valid " + entryIsValid[2] );
		System.out.println( "  Vertex count: " + getCount( sp, 0 ));
		dumpValues( sp, 0 );
		System.out.println( "  Edge count: " + getCount( sp, 1 ));
		dumpValues( sp, 1 );
		System.out.println( "  Face count: " + getCount( sp, 2 ));
		dumpValues( sp, 2 );
	}

	/** Mark entries of a particular type as existing and value */
	public void validate( int type ) {
		entryIsValid[ type ] = true;
		hasEntry[ type ] = true;

		// If this is the only type valid, it is also the first
		int validCount = 0;
		for ( int i = 0; i < 3; i++ ) {
			if ( entryIsValid[i] ) {
				validCount++;
			}
		}
		if ( validCount == 1 ) {
			env_firstEntry[ type ] = true;
		}
	}

	//
	//  Indicates that the entries of a particular type are not valid.  
	//
	public void invalidate( int type ) {
		entryIsValid[ type ] = false;
	}

	public boolean entryExistsFor( int type ) {
		return( hasEntry[ type ] );
	}

	public int getBaseType() {
		return( baseType );
	}

    int fixedDivisor;
    public int setDivisor() {
        fixedDivisor = divisor();
        return( fixedDivisor );
    }
    public void unsetDivisor() {
        fixedDivisor = 0;
    }
    
	public int divisor() {
	    if ( fixedDivisor != 0 ) {
	        return( fixedDivisor );
	    }
		int sum = 0;
		for ( int i = 0; i < 3; i++ ) {
			if ( hasEntry[i] ) {
				sum++;
			}
		}
		if ( fixedDivisor > 0 ) {
		    if ( sum != fixedDivisor ) {
		        System.out.println( "Optimization won't work" );
		    }
		}
		return( sum );
	}

	public int getCount( SpacePrimitive source, int type ) {
		if ( entryIsValid[ type ] ) {
			return( source.getSize()/divisor() );
		}
		return( 0 );
	}

	public int getRawCount( SpacePrimitive source, int type ) {
		if ( hasEntry[ type ] ) {
			return( source.getSize()/divisor() );
		}
		return( 0 );
	}


	int getActualOffset( int type, int offset ) {
		if ( divisor() > 1 ) {
			if ( env_firstEntry[ type ] ) {
				return( offset * 2 );
			} else {
				return( offset * 2 + 1 );
			}
		} else {
			return( offset );
		}
	}

    /** Get the actual offset of a particular type and value.
     *
     *  @param source SpacePrimitive to check
     *  @param type type of value to look for
     *  @param value value to look for
     *
     *  @return actual offset in the array of values
     */
	int getActualOffsetByValue( SpacePrimitive source, int type, int value ) {
	    int count = getCount( source, type );
		for ( int i = 0; i < count; i++ ) {
			if ( getValue( source, type, i ) == value ) {
				return( getActualOffset( type, i ));
			}
		}
		return( -1 );
	}

	//
	//  Get the value of a particular type out of the SpacePrimitive.  
	//
	public int getValue( SpacePrimitive source, int type, int offset ) {
		int actualOffset = getActualOffset( type, offset );
		return( source.getValue( actualOffset ));
	}

    /** Set the value of a particular offset */
	public void setValue( SpacePrimitive source, int type, int offset, int value ) {
		int actualOffset = getActualOffset( type, offset );
		source.setValue( actualOffset, value );
	}
	
	/** Set the value of the next available offset */
	public void setValue( SpacePrimitive source, int type, int value ) {
	    int offset1 = getActualOffsetByValue( source, type, value );
	    if ( offset1 != -1 ) {
	        return;
	    }
	    int actualOffset = getActualOffsetByValue( source, type, -1 );
	    if ( actualOffset != -1 ) {
    	    source.setValue( actualOffset, value );
    	}
	}

		
	//
	//  Convert vertices to faces by changing the base type to face, exchanging V/F
	//  entries in "hasEntry" and env_firstEntry.  Invalidate edge entries.
	//
	public void VtoF() {
		if ( baseType == SpacePrimitive.Vertex ) {
			baseType = SpacePrimitive.Face;
			moveEntries( SpacePrimitive.Vertex, SpacePrimitive.Face );
			initEntries( SpacePrimitive.Face );
			entryIsValid[ SpacePrimitive.Edge ] = false;
		}
	}

	public void EtoF() {
		if ( baseType == SpacePrimitive.Edge ) {
			baseType = SpacePrimitive.Face;
			initAllEntries();
			hasEntry[ SpacePrimitive.Vertex ] = true;
			entryIsValid[ SpacePrimitive.Vertex ] = true;
			env_firstEntry[ SpacePrimitive.Vertex ] = true;
		}
	}

	void moveEntries( int dest, int source ) {
		hasEntry[ dest ] = hasEntry[ source ];
		entryIsValid[ dest ] = entryIsValid[ source ];
		env_firstEntry[ dest ] = env_firstEntry[ source ];
	}

	void initAllEntries() {
		for ( int i = 0; i < 3; i++ ) {
			initEntries( i );
		}
	}

	void initEntries( int offset ) {
		hasEntry[ offset ] = false;
		entryIsValid[ offset ] = false;
		env_firstEntry[ offset ] = false;
	}

	public void FtoV() {
		if ( baseType == SpacePrimitive.Face ) {
			// set the face coordinates based on surrounding vertices, SpacePrimitives
			// acting as Vertex need a location.  The Face normally will not have this set
			// (and if it is set it is via the function below)
			spaceStructure.setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
			baseType = SpacePrimitive.Vertex;
			moveEntries( SpacePrimitive.Face, SpacePrimitive.Vertex );
			initEntries( SpacePrimitive.Vertex );
			entryIsValid[ SpacePrimitive.Edge ] = false;
		}
	}

	public void FtoV( float cx, float cy, float cz, float distance ) {
		if ( baseType == SpacePrimitive.Face ) {
			// set the face coordinates based on surrounding vertices, SpacePrimitives
			// acting as Vertex need a location.  The Face normally will not have this set
			// (and if it is set it is via the function below)
			spaceStructure.setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex,
				cx, cy, cz, distance );
			baseType = SpacePrimitive.Vertex;
			moveEntries( SpacePrimitive.Face, SpacePrimitive.Vertex );
			initEntries( SpacePrimitive.Vertex );
			entryIsValid[ SpacePrimitive.Edge ] = false;
		}
	}
}
