/*
 * @(#)SpaceEntitySet.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.space;

import java.util.Vector;
import java.util.Hashtable;

/**
 *  The SpaceEntitySet holds a set of SpacePrimitive objects and a Strategy for viewing
 *  those objects.
 */
public class SpaceEntitySet {
    /** enable/disable debug dump */
	static public boolean debug = false;

	/** Array of SpacePrimitives, referred to by offset */
	Vector entities;

    /** In some cases, the entire set location is used */
	float x;
	float y;
	float z;
	
	/** Optimization for edges, to map two vertices to an edge quickly */
	Hashtable vToEmapper;
	
	/** The strategy interprets the offsets around a SpacePrimitive */
	Strategy eView;

	/** the owner of this set is used for call backs */
	SpaceStructure spaceStructure;

    /** Constructor */
	public SpaceEntitySet( Strategy s, SpaceStructure ss ) {
		entities = new Vector();
		eView = s;
		spaceStructure = ss;
	}
	
	/** Copy constructor */
	public SpaceEntitySet( SpaceEntitySet src ) {
	}

	/** Add a SpacePrimitive to the SpaceEntitySet */
	public void addEntity( SpacePrimitive e ) {
		entities.addElement( e );
	}

    /** Create the BoundingBox for the set */
    public BoundingBox getBoundingBox() {
        BoundingBox result = new BoundingBox();
		int numberEntities = getNumberEntities();
		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive sp = getEntity( i );
			result.setXYZ( sp.getX(), sp.getY(), sp.getZ() );
		}
		return( result );
	}

    /** add a SpacePrimitive, keep end points in Hashtable for fast access */
	public void addSpacePrimitive( int v1, int v2 ) {
	    if ( vToEmapper == null ) {
	        vToEmapper = new Hashtable();
	    }
	    String mapStr = null;
	    if ( v1 < v2 ) {
	        mapStr = new String( v1 + "_" + v2 );
	    } else {
	        mapStr = new String( v2 + "_" + v1 );
	    }
	    if ( vToEmapper.get( mapStr ) == null ) {
    	    vToEmapper.put( mapStr, new Integer( entities.size() ));
    	    addEntity( new SpacePrimitive( v1, v2 ));
    	}
	}
	
	int fixedDivisor;
	public void setDivisor() {
	    fixedDivisor = eView.setDivisor();
	}
	public void unsetDivisor() {
	    fixedDivisor = 0;
	    eView.unsetDivisor();
	}

	/**  Calculate then set the center location for all entities in the set */
	public void setCenterLocation() {
		x = 0;
		y = 0;
		z = 0;
		int numberEntities = getNumberEntities();
		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive sp = getEntity( i );
			x += sp.getX();
			y += sp.getY();
			z += sp.getZ();
		}
		x = x/numberEntities;
		y = y/numberEntities;
		z = z/numberEntities;
	}

    /** Get the X location for the center of the set */
	public float getX() {
		return( x );
	}

    /** Get the Y location for the center of the set */
	public float getY() {
		return( y );
	}

    /** Get the Z location for the center of the set */
	public float getZ() {
		return( z );
	}

    /** Set the X,Y,Z location for the center of the set */
	public void setLocation( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	void setDistance( SpacePrimitive sp, float distance ) {
		float xSum = sp.getX();
		float ySum = sp.getY();
		float zSum = sp.getZ();

		float locDistance = distanceTo( xSum, ySum, zSum );

		// really have to shift by cx, cy, cz before this, but this is 0,0,0 in test cases
		float factor = distance/locDistance;
		xSum = xSum * factor;
		ySum = ySum * factor;
		zSum = zSum * factor;
		sp.setLocation( xSum, ySum, zSum );
	}

    /** Get the distance between the center of this set and a particular entry in the set.
     *
     *  @param offset offset to entry to check
     */
	public float distanceTo( int offset ) {
		SpacePrimitive sp = getEntity( offset );
		return( distanceTo( sp.getX(), sp.getY(), sp.getZ() ));
	}

	public float distanceTo( float spx, float spy, float spz ) {
		return( distanceBetween( spx, spy, spz, x, y, z ));
	}

	public void insert( SpacePrimitive sp, int type, int rv1, int rv2, int nv ) {
		SpacePrimitive spCopy = new SpacePrimitive( sp );
		addEntity( spCopy );
		replaceValue( sp, type, rv1, nv );
		replaceValue( spCopy, type, rv2, nv );
	}

    /** Get the distance between two SpacePrimitives */
	static public float distanceBetween( SpacePrimitive sp1, SpacePrimitive sp2 ) {
		return( distanceBetween( sp1.getX(), sp1.getY(), sp1.getZ(), sp2.getX(), sp2.getY(),
			sp2.getZ() ));
	}

	static public float distanceBetween( float spx, float spy, float spz, float xx, float yy, float zz ) {
		float xDis = spx - xx;
		float yDis = spy - yy;
		float zDis = spz - zz;
		xDis = xDis*xDis;
		yDis = yDis*yDis;
		zDis = zDis*zDis;
		float dis = xDis + yDis + zDis;
		try {
			return( (float)Math.sqrt( dis ));
		} catch( Exception e ) {
			return( (float)0 );
		}
	}


	public void dump( SpacePrimitive sp, String header ) {
		if ( debug ) {
			eView.dump( sp, header );
		}
	}

	public void dump() {
		dump( ".." );
	}

	public void dump( String header ) {
		if ( debug ) {
			System.out.println( header + ":: Dumping set.." + getNumberEntities() + " entities" );
			for ( int i = 0; i < getNumberEntities(); i++ ) {
				SpacePrimitive p = getEntity( i );
				dump( p, " entity " + i );
			}
			System.out.println( "Finished dumping set" );
		}
	}

	// really have to make sure addition is compatible at the view level before the append
	public void appendSet( SpaceEntitySet addition ) {
		addition.createCompatibleView( eView );
		for ( int i = 0; i < addition.getNumberEntities(); i++ ) {
			entities.addElement( addition.getEntity( i ));
//			dump( addition.getEntity( i ), "Just appended this guy" );
		}
	}

	//
	//  Two views are merging, if one view has an entry, the other doesn't, invalidate
	//  the entry.
	//
	public void createCompatibleView( Strategy mainView ) {
		for ( int i = 0; i < 3; i++ ) {
			if ( mainView.entryExistsFor( i ) && !eView.entryExistsFor( i )) {
				mainView.invalidate( i );
			}
			if ( !mainView.entryExistsFor( i ) && eView.entryExistsFor( i )) {
			    int numberEntities = getNumberEntities();
				for ( int j = 0; j < numberEntities; j++ ) {
					SpacePrimitive sp = getEntity( j );
					zapView( sp, i );
				}
			}
		}
	}

    /** Get the type of entities contained in this set */
	public int getBaseType() {
		return( eView.getBaseType() );
	}

    /** Get the number of entities in the set */
	public int getNumberEntities() {
		return( entities.size() );
	}

	/** Get an entity at a particular offset */
	public SpacePrimitive getEntity( int offset ) {
		if ( offset < entities.size() ) {
			return( (SpacePrimitive)entities.elementAt( offset ));
		} else {
			return( null );
		}
	}

	//
	//  in some cases object doesn't exist at all, convention is -1 offset indicates this
	//  not sure if this is right thing to do... problem is that for example, edges always
	//  between two faces in singly connected structure, but when on plane, one "face"
	//  is the entire plane (outside structure), not sure if this should be represented
	//  as a face or not. 
	//
	public int getEntityOffset( Object e ) {
		if ( e == null ) {
			return( -1 );
		} else {
			return( entities.indexOf( e ));
		}
	}


	/** Get the SpacePrimitive of a particular type and offset that surrounds a member of this set.
	 *  
	 *  @param source  the member of the current set to access
	 *  @param type    the SpacePrimitive type surrounding the current SpacePrimitive
	 *  @param offset  the offset of the SpacePrimitive type
	 *
	 *  @return one of the SpacePrimitive objects surrounding the SpacePrimitive source
	 */
	public SpacePrimitive getEntity( SpacePrimitive source, int type, int offset ) {
		int entityIdx = getValue( source, type, offset );
		if ( entityIdx == -1 ) {
			return( null );
		}
		SpaceEntitySet ses = spaceStructure.getEntitySet( type );
		return( ses.getEntity( entityIdx ));
	}

	//
	//  The source has an entity of type/value, but it needs the entity of that type 
	//  immediately after that one.
	//
	public SpacePrimitive getEntityAfter( SpacePrimitive source, int type, int value ) {
		for ( int i = 0; i < getCount( source, type ); i++ ) {
			if ( getValue( source, type, i ) == value ) {
				if ( i == ( getCount( source, type ) - 1 )) {
					return( getEntity( source, type, 0 ));
				} else {
					return( getEntity( source, type, i + 1 ));
				}
			}
		}
		return( null );
	}


	public SpacePrimitive getConvertedEntity( SpacePrimitive source, int originalType,
		int convertedType, int offset ) {
		int entityIdx = getValue( source, originalType, offset );
		SpaceEntitySet ses = spaceStructure.getEntitySet( convertedType );
		return( ses.getEntity( entityIdx ));
	}

	public void zapView( SpacePrimitive source, int type ) {
		for ( int i = 0; i < getRawCount( source, type ); i++ ) {
			setValue( source, type, i, -1 );
		}
		source.compress();
	}


	//
	//  edge searching by vertex, problem here -- type ignored
	//  order of v1,v2 ignored.
	//
	public int getEntityOffset( int v1, int v2, int type ) {
	    if ( vToEmapper != null ) {
	        String htStr = null;
	        if ( v1 < v2 ) {
	            htStr = new String( v1 + "_" + v2 );
	        } else {
	            htStr = new String( v2 + "_" + v1 );
	        }
	        Integer offset = (Integer)vToEmapper.get( htStr );
	        if ( offset != null ) {
	            return( offset.intValue() );
	        }
	    }
	    int n = getNumberEntities();
		for ( int i = 0; i < n; i++ ) {
			SpacePrimitive sp = (SpacePrimitive)entities.elementAt( i );
			if ( sp.includes( v1, v2 )) {
				return( i );
			}
		}
		return( -1 );
	}

	public boolean orderIs( SpacePrimitive source, int type, int v1, int v2 ) {
		int v1offset = -1;
		int v2offset = -1;
		for ( int i = 0; i < getCount( source, type ); i++ ) {
			int value = getValue( source, type, i );
			if ( value == v1 ) {
				v1offset = i;
			} else if ( value == v2 ) {
				v2offset = i;
			}
		}
		if ( v2offset == ( v1offset + 1 )) {
			return( true );
		} else if (( v2offset == 0 ) && ( v1offset == ( getCount( source, type ) - 1 ))) {
			return( true );
		} else {
			return( false );
		}
	}


	//
	//  Does the source include the value
	//
	public boolean includes( SpacePrimitive source, int type, int v1, int v2 ) {
		boolean foundv1 = false;
		boolean foundv2 = false;
		int count = getCount( source, type );
		for ( int i = 0; i < count; i++ ) {
			int value = getValue( source, type, i );
			if ( value == v1 ) {
				foundv1 = true;
			} else if ( value == v2 ) {
				foundv2 = true;
			}
		}
		return( foundv1 && foundv2 );
	}

	public int getAnotherIdx( SpacePrimitive notThisOne, int type, int v1, int v2 ) {
	    int numberEntities = getNumberEntities();
		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive sp = getEntity( i );
			if ( sp != notThisOne ) {
				if ( includes( sp, type, v1, v2 )) {
					return( i );
				}
			}
		}
		return( -1 );
	}

	public boolean includes( SpacePrimitive source, int type, int value ) {
		for ( int i = 0; i < getCount( source, type ); i++ ) {
			if ( getValue( source, type, i ) == value ) {
				return( true );
			}
		}
		return( false );
	}


	boolean vecIncludes( int v1, int v2 ) {
	    int numberEntities = entities.size();
		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive sp = (SpacePrimitive)entities.elementAt( i );
			if ( sp.includes( v1, v2 )) {
				return( true );
			}
		}
		return( false );
	}

    /** Get the number of entities of a particular type in a SpacePrimitive.
     *
     *  @param source  the SpacePrimitive to access
     *  @param type    SpacePrimitive.Vertex, SpacePrimitive.Edge, SpacePrimitive.Face
     */
	public int getCount( SpacePrimitive source, int type ) {
		return( eView.getCount( source, type ));
	}

	public int getRawCount( SpacePrimitive source, int type ) {
		return( eView.getRawCount( source, type ));
	}

	public int getValue( SpacePrimitive source, int type, int offset ) {
		return( eView.getValue( source, type, offset ));
	}

	public int getEntityOffsetWithLocation( float x, float y, float z ) {
	    int numberEntities = getNumberEntities();
		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive e = getEntity( i );
			if ( e.locationIs( x, y, z )) {
				return( i );
			}
		}
		return( -1 );
	}

	public void replaceValue( SpacePrimitive source, int type, int v1, int replacement ) {
		for ( int i = 0; i < getCount( source, type ); i++ ) {
			if ( getValue( source, type, i ) == v1 ) {
				setValue( source, type, i, replacement );
//				System.out.println( "ReplaceValue " + v1 + " with " + replacement );
				return;
			}
		}
		System.out.println( "ReplaceValue failed.." );
	}

    /** Set the value of a particular offset */
	public void setValue( SpacePrimitive source, int type, int offset, int value ) {
		eView.setValue( source, type, offset, value );
	}
	
	/** Set the value of the next available offset */
	public void setValue( SpacePrimitive source, int type, int value ) {
	    eView.setValue( source, type, value );
	}

	//
	//  mark the entries of a particular type as not valid, independent of whether they
	//  exist or not
	//
	public void invalidate( int type ) {
		eView.invalidate( type );
	}


	/** Mark entries of a particular type as existing and valid */
	public void validate( int type ) {
		eView.validate( type );
	}
	
	/** Double capacity for holding info of a new type */
	public void doubleCapacity( int type ) {
	    eView.validate( type );
	    int numberEntities = getNumberEntities();
	    for ( int i = 0; i < numberEntities; i++ ) {
	        SpacePrimitive sp = getEntity( i );
	        sp.doubleCapacity();
	    }
	}

	// 
	//  convert vertices to faces
	//
	public void VtoF() {
		eView.VtoF();
	}

	public boolean entryExistsFor( int type ) {
		return( eView.entryExistsFor( type ));
	}

	//
	//  convert faces to vertices
	//
	public void FtoV() {
		eView.FtoV();
	}

	public void FtoV( float cx, float cy, float cz, float distance ) {
		eView.FtoV( cx, cy, cz, distance );
	}


	public float sqrt( float dis ) {
		try {
			return( (float)Math.sqrt( dis ));
		} catch( Exception e ) {
			return( (float)0 );
		}
	}
	//
	//  convert edges to faces, assumes that previously faces were turned into vertices,
	//  and that the existing edges already had a ccw ordering of EF
	//
	public void EtoF( int originalVertexCount, float cx, float cy, float cz ) {
	SpaceEntitySet vertices = spaceStructure.getEntitySet( SpacePrimitive.Vertex );
		// first adjust all the original F offsets in the edges based on original vertex count,
		// since this set of original faces were converted to vertices, then appended to the
		// existing set of vertices.
		int numberEntities = getNumberEntities();
		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive sp = getEntity( i );
//			dump( sp, "Edge " + i );
			for ( int j = 0; j < getCount( sp, SpacePrimitive.Face ); j++ ) {
				int originalValue = getValue( sp, SpacePrimitive.Face, j );
				originalValue += originalVertexCount;
				setValue( sp, SpacePrimitive.Face, j, originalValue );
			}
		}

		for ( int i = 0; i < numberEntities; i++ ) {
			SpacePrimitive sp = getEntity( i );
			for ( int j = 0; j < getCount( sp, SpacePrimitive.Face ); j++ ) {

				// Now we have to adjust the location of the vertices which were originally
				// faces.  These are located at mid-face, which means that these two points,
				// and the two original edge vertices, do not form a plane.  To adjust these
				// locations we get the distance from the origin to the mid point of the edge,
				// and the distance from the origin to the mid point of the "face" edge (the
				// connecting line of the mid point of the two former faces).  The difference
				// between these two distances it the amount the face vertices need to move.
				//
				SpacePrimitive originalV1 = getEntity( sp, SpacePrimitive.Vertex, 0 );
//				vertices.dump( originalV1, "edge " + i + " v1" );
				SpacePrimitive originalV2 = getEntity( sp, SpacePrimitive.Vertex, 1 );
//				vertices.dump( originalV2, "edge " + i + " v2" );
				float VmidX = ( originalV1.getX() + originalV2.getX() )/2;
				float VmidY = ( originalV1.getY() + originalV2.getY() )/2;
				float VmidZ = ( originalV1.getZ() + originalV2.getZ() )/2;

				// Can't just use getEntity( sp, SpacePrimitive.Face, 0 ) here because
				// faces have already been converted to vertices.  
				SpacePrimitive originalF1 = getConvertedEntity( sp, SpacePrimitive.Face, 
					SpacePrimitive.Vertex, 0 );
//				vertices.dump( originalF1, "edge f1" );
				SpacePrimitive originalF2 = getConvertedEntity( sp, SpacePrimitive.Face, 
					SpacePrimitive.Vertex, 1 );
//				vertices.dump( originalF2, "edge f2" );
				float FmidX = ( originalF1.getX() + originalF2.getX() )/2;
				float FmidY = ( originalF1.getY() + originalF2.getY() )/2;
				float FmidZ = ( originalF1.getZ() + originalF2.getZ() )/2;

				// Fmid needs adjustment if F1 & F2 are not symmetrical
				float longDist = distanceBetween( originalF1.getX(), originalF1.getY(),
					originalF1.getZ(), VmidX, VmidY, VmidZ );
				float shortDist = distanceBetween( originalF2.getX(), originalF2.getY(),
					originalF2.getZ(), VmidX, VmidY, VmidZ );
				if ( shortDist > longDist ) {
					float swapper = shortDist;
					shortDist = longDist;
					longDist = swapper;
				}
				float FmidDist = distanceBetween( originalF1.getX(), originalF1.getY(),
					originalF1.getZ(), FmidX, FmidY, FmidZ );

				float additionalDistance = ( longDist * longDist - shortDist * shortDist ) / ( 4 * FmidDist );
				float additionalFactor = additionalDistance / FmidDist;
				System.out.println(  "additionalDistance " + additionalDistance + ", additionalFactor " + additionalFactor );
				FmidX += ( originalF2.getX() - FmidX ) * additionalFactor;
				FmidY += ( originalF2.getY() - FmidY ) * additionalFactor;
				FmidZ += ( originalF2.getZ() - FmidZ ) * additionalFactor;

				System.out.println( "Vmid: " + VmidX + ", " + VmidY + ", " + VmidZ );
				System.out.println( "Fmid: " + FmidX + ", " + FmidY + ", " + FmidZ );
				
				// distance to move line
				float VmidDistance = distanceBetween( VmidX, VmidY, VmidZ, cx, cy, cz );
				float FmidDistance = distanceBetween( FmidX, FmidY, FmidZ, cx, cy, cz );
				float moveDistance = VmidDistance - FmidDistance;

//				System.out.println( "moveDistance " + moveDistance );

				// factor based on triangle from origin to Fmid to F vertex, since move
				// triangle is proportional
				float F1Distance = distanceBetween( originalF1.getX(), originalF1.getY(),
					originalF1.getZ(), cx, cy, cz );
				float F2Distance = distanceBetween( originalF2.getX(), originalF2.getY(),
					originalF2.getZ(), cx, cy, cz );
//				System.out.println( "FDistance is " + FDistance + ", FmidDistance is " + FmidDistance );
				float f1factor = F1Distance / FmidDistance;
				float f2factor = F2Distance / FmidDistance;
//				System.out.println( "Factor is " + factor );
				float move1Distance = moveDistance * f1factor;
				float move2Distance = moveDistance * f2factor;

				// really have to shift by cx, cy, cz before this, but this is 0,0,0 in test
				// cases
				float c1Factor = ( F1Distance + move1Distance )/F1Distance;
				float c2Factor = ( F2Distance + move2Distance )/F2Distance;

				// just save the factor, we have to apply adjustments all at once
//				System.out.println( "cFactor is " + cFactor );
				originalF1.setCfactor( c1Factor );
				originalF2.setCfactor( c2Factor );
			}
		}
		int numberVertices = vertices.getNumberEntities();
		for ( int i = 0; i < numberVertices; i++ ) {
			SpacePrimitive v = vertices.getEntity( i );
			v.applyFactor();
		}
		eView.EtoF();
	}

	public void diagonalize( SpacePrimitive face, DiagonalizeInfo d ) {
		if ( !d.diagonalized() ) {
			diagonalize( face, d, 0 );
		} else if ( d.hasVertex( getEntity( face, SpacePrimitive.Vertex, 0 ))) {
			diagonalize( face, d, 1 );
		} else {
			diagonalize( face, d, 0 );
		}
	}

	void diagonalize( SpacePrimitive face, DiagonalizeInfo d, int vOffset ) {
		SpacePrimitive newFace = new SpacePrimitive( face );
		if ( vOffset == 0 ) {
			d.saveVertex( getEntity( face, SpacePrimitive.Vertex, 0 ));
			d.saveVertex( getEntity( face, SpacePrimitive.Vertex, 2 ));
			face.removeOffset( 1, eView.divisor() );
			newFace.removeOffset( 3, 1 );
		} else {
			d.saveVertex( getEntity( face, SpacePrimitive.Vertex, 1 ));
			d.saveVertex( getEntity( face, SpacePrimitive.Vertex, 3 ));
			face.removeOffset( 0, eView.divisor() );
			newFace.removeOffset( 2, 1 );
		}
		addEntity( newFace );
		d.markAsDiagonalized();
	}
}


