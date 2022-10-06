/*
 * @(#)SpacePrimitive.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.space;

import com.trapezium.util.CompositeObject;
import com.trapezium.util.TypedObject;
import java.util.Vector;

/**
 *  The SpacePrimitive is either a Vertex, Edge, or Face, but does not know its own type.
 *  The SpaceEntitySet contains a set of SpacePrimitives, and a Strategy for viewing those
 *  primitives.  The Strategy determines whether the primitive is a Vertex, Edge, or Face, 
 *  as well as the actual types of the SpacePrimitives in the surrounding ccwVec.
 */

public class SpacePrimitive {
    /** Constant to indicate a vertex */
	static public final int Vertex = 0;

	/** Constant to indicate an edge */
	static public final int Edge = 1;

	/** Constant to indicate a face */
	static public final int Face = 2;

	// optional location
	float x;
	float y;
	float z;
	boolean coordExists;

	float factor = 0;
	ccwVec environment;
	boolean envExists;
	
	/** Construct a SpacePrimitive at a specific location */
	public SpacePrimitive( float x, float y, float z ) {
		init();
		setLocation( x, y, z );
	}

    /** Create a SpacePrimitive that is a copy of an existing SpacePrimitive */
	public SpacePrimitive( SpacePrimitive source ) {
		x = source.x;
		y = source.y;
		z = source.z;
		coordExists = source.coordExists;
		factor = source.factor;
		envExists = source.envExists;
		if ( envExists ) {
			environment = new ccwVec( source.environment );
		}
	}

    /** Create a SpacePrimitive surrounded by several entities. */
	public SpacePrimitive( int[] envList, int envcount ) {
		init();
		environment = new ccwVec( envList, envcount );
		envExists = true;
	}

    /** Create a SpacePrimitive surrounded by two entities */
	public SpacePrimitive( int voff1, int voff2 ) {
		init();
		environment = new ccwVec( voff1, voff2 );
		envExists = true;
	}

    /** Attach an object to the SpacePrimitive. */
	TypedObject attachedObject = null;
	public void attachObject( TypedObject a ) {
	    if ( attachedObject != null ) {
	        attachedObject = new CompositeObject( a, attachedObject );
	    } else {
    	    attachedObject = a;
    	}
	}
	
	/** Get the object attached to the SpacePrimitive */
	public TypedObject getAttachedObject() {
		return( attachedObject );
	}

	public boolean locationIs( float x, float y, float z ) {
		return(( this.x == x ) && ( this.y == y ) && ( this.z == z ));
	}
	
	/** Is this SpacePrimitive at the same location as another */
	public boolean equalTo( SpacePrimitive test ) {
	    if ( test == null ) {
	        return( false );
	    }
	    return(( test.x == x ) && ( test.y == y ) && ( test.z == z ));
	}

	public void expand( float f ) {
		factor = f;
		applyFactor();
	}

	public void setCfactor( float f ) {
		factor = f;
	}

	public void applyFactor() {
		if ( factor != 0 ) {
//		System.out.println( "applying factor" );
			x = x * factor;
			y = y * factor;
			z = z * factor;
		}
	}


    /** Set the location of this SpacePrimitive */
	public void setLocation( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
		coordExists = true;
	}

    /** Get the X location of this SpacePrimitive */
	public float getX() {
		return( x );
	}
	
	/** Set the X location of this SpacePrimitive */
	public void setX( float x ) {
	    this.x = x;
	}

    /** Get the Y location of this SpacePrimitive */
	public float getY() {
		return( y );
	}
	
	/** Set the Y location of this SpacePrimitive */
	public void setY( float y ) {
	    this.y = y;
	}

    /** Get the Z location of this SpacePrimitive */
	public float getZ() {
		return( z );
	}
	
	/** Set the Z location of this SpacePrimitive */
	public void setZ( float z ) {
	    this.z = z;
	}
	
	public void multiply( int xFactor, int yFactor, int zFactor ) {
	    x = x*xFactor;
	    y = y*yFactor;
	    z = z*zFactor;
	}
	
	public void divide( int xFactor, int yFactor, int zFactor ) {
	    x = x/xFactor;
	    y = y/yFactor;
	    z = z/zFactor;
	}

	public boolean includes( int v1, int v2 ) {
		if ( envExists ) {
			return( environment.includes( v1, v2 ));
		} else {
			return( false );
		}
	}

	public void insertSpace( int offset, int divisor ) {
		if ( envExists ) {
			environment.insertSpace( offset, divisor );
		}
	}

	public void removeOffset( int offset, int divisor ) {
		if ( envExists ) {	
			environment.removeOffset( offset, divisor );
		}
	}

	public boolean includes( int v1 ) {
		if ( envExists ) {
			return( environment.includes( v1 ));
		} else {
			return( false );
		}
	}

	public int getSize() {
		if ( envExists ) {
			return( environment.getSize() );
		} else {
			return( 0 );
		}
	}

    /** Double the capacity of the current environment */
    // This needs some work, should really be "increase capacity to handle type"
    // where nothing happens if type is already handled.  Need to change
    // name and implementation (what is here works, but assumes type not
    // handled)
    // Also, seems that it is possible to get values for types that aren't
    // handled, this should return error(?) or should we put responsibility
    // on caller because error checking inefficient
    public void doubleCapacity( int type ) {
        doubleCapacity();
    }
	public void doubleCapacity() {
		if ( envExists ) {
			environment.doubleCapacity();
		}
	}

	public void dumpArray( String header ) {
		System.out.println( header );
		if ( envExists ) {
			environment.dumpArray();
		} else {
			System.out.println( "** no environment **" );
		}
	}

    /** Set size of list for surrounding entities */
	public void setCapacity( int size ) {
		if ( !envExists ) {
			environment = new ccwVec( size );
			envExists = true;
		} else {
			environment.setCapacity( size );
		}
	}

	public int getValue( int offset ) {
		if ( envExists ) {
			return( environment.getValue( offset ));
		} else {
			return( 0 );
		}
	}
	
	/** Append a value, replacing first unassigned value found */
	public void appendValue( int value ) {
	    if ( envExists ) {
	        environment.appendValue( value );
	    }
	}

	public void compress() {
		if ( envExists ) {
			environment.compress();
		}
	}

	public void setValue( int offset, int value ) {
		if ( envExists ) {
			environment.setValue( offset, value );
		}
	}


	void init() {
		x = 0f;
		y = 0f;
		z = 0f;
		coordExists = false;
		environment = null;
		envExists = false;
	}
}

/**
 *  The ccwVec is a vector of offsets into SpaceEntitySets.  
 *  The intention, not fully implemented, is that it is a counterclockwise
 *  list of entities surrounding a specific SpacePrimitive.
 */
class ccwVec {
	int[] environment;

	public ccwVec( int[] envList, int envSize ) {
		environment = new int[ envSize ];
		for ( int i = 0; i < envSize; i++ ) {
			environment[i] = envList[i];
		}
	}

	public ccwVec( ccwVec source ) {
		environment = new int[ source.environment.length ];
		for ( int i = 0; i < source.environment.length; i++ ) {
			environment[i] = source.environment[i];
		}
	}

	public void removeOffset( int offset, int divisor ) {
		int newSize = environment.length - divisor;
		int[] newEnv = new int[ newSize ];
		int newIdx = 0;
		for ( int i = 0; i < environment.length; i++ ) {
			if ( i != ( offset * divisor )) {
				newEnv[ newIdx ] = environment[i];
				newIdx++;
				if ( divisor > 1 ) {
					i++;
					newEnv[ newIdx ] = environment[i];
					newIdx++;
				}
			}
		}
		environment = newEnv;
	}

	public void insertSpace( int offset, int divisor ) {
		int newSize = environment.length + divisor;
		int[] newEnv = new int[ newSize ];
		for ( int i = 0; i < environment.length; i++ ) {
			newEnv[i] = environment[i];
		}
		for ( int i = 0; i < divisor; i++ ) {
			for ( int j = newSize - 1; j > offset; j-- ) {
				newEnv[ j ] = newEnv[ j - 1 ];
			}
		}
		environment = newEnv;
	}

	public void dumpArray() {
		StringBuffer sb = new StringBuffer( "    " );
		for ( int i = 0; i < environment.length; i++ ) {
			sb.append( environment[i] );
			sb.append( ", " );
		}
		String s = new String( sb );
		System.out.println( s );
	}

	public void compress() {
		int negCount = 0;
		for ( int i = 0; i < environment.length; i++ ) {
			if ( environment[i] < 0 ) {
				negCount++;
			}
		}
		if ( negCount < environment.length ) {
			int[] newEnvironment = new int[ environment.length - negCount ];
			int nidx = 0;
			for ( int i = 0; i < environment.length; i++ ) {
				if ( environment[i] >= 0 ) {
					newEnvironment[ nidx ] = environment[i];
					nidx++;
				}
			}
			environment = newEnvironment;
		}
	}

	public ccwVec( int envSize ) {
		setCapacity( envSize );
	}

    /** Set the size of the list of surrounding entities */
	public void setCapacity( int envSize ) {
		environment = new int[ envSize ];
		for ( int i = 0; i < envSize; i++ ) {
			environment[i] = -1;
		}
	}

    /** Get the number of surrounding entities */
	public int getSize() {
		return( environment.length );
	}

	public int getValue( int offset ) {
		return( environment[ offset ] );
	}

	public void setValue( int offset, int value ) {
		environment[ offset ] = value;
	}
	
	/** Assign value to next available environment entry */
	public void appendValue( int value ) {
	    for ( int i = 0; i < environment.length; i++ ) {
	        if ( environment[i] == -1 ) {
	            environment[i] = value;
	            return;
	        }
	    }
	}

	public ccwVec( int v1, int v2 ) {
		environment = new int[2];
		environment[0] = v1;
		environment[1] = v2;
	}

    /** Double the capacity of the environment.  Previously existing values get placed
     *  at even numbered offsets in the new environment.
     */
	public void doubleCapacity() {
		int[] newEnvironment = new int[ environment.length * 2 ];
		for ( int i = 0; i < environment.length*2; i++ ) {
			newEnvironment[i] = -1;
		}
		for ( int i = 0; i < environment.length; i++ ) {
			newEnvironment[i*2] = environment[i];
		}
		environment = newEnvironment;
	}

	public boolean includes( int v1, int v2 ) {
		if ( environment.length == 2 ) {
			if ( v1 == environment[0] ) {
				if ( v2 == environment[1] ) {
					return( true );
				}
			} else if ( v2 == environment[0] ) {
				if ( v1 == environment[1] ) {
					return( true );
				}
			}
		}
		return( false );
	}

	public boolean includes( int v1 ) {
		if ( environment.length == 2 ) {
			if ( v1 == environment[0] ) {
				return( true );
			} else if ( v1 == environment[1] ) {
				return( true );
			}
		}
		return( false );
	}
}
