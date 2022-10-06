/*
 * @(#)SpaceStructure.java
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
import com.trapezium.util.GlobalProgressIndicator;
import com.trapezium.util.QSort;
import java.util.Vector;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.BitSet;
import java.util.Hashtable;

/** The SpaceStructure keeps track of vertices, edges, and faces in a somewhat fluid
 *  manner.  There are three sets of objects, corresponding to vertices, edges, and
 *  faces, but these sets can be mixed, and relationships created between these sets.
 *  This is done to allow transformations of one type into another.
 *
 *  A SpaceEntitySet represents a set of SpacePrimitive objects used by a particular
 *  space structure.  The Strategy field "primitiveHandler" indicates the current
 *  visualization of a space entity and its surrounding entities.
 *
 *  Relationships between the sets are created as needed, only r1 is known at the
 *  time the SpaceStructure is first created.
 *
 *  The possible relationships are:
 *
 *     r1. F ---* V
 *        The set of vertices surrounding each face,
 *        created when an IndexedFaceSet is loaded
 *     r2. V ---* F    V,r2: requires r3()
 *        The set of faces surrounding each vertex,
 *        derived from r3
 *     r3. F ---* E    F,r3: requires r1()
 *        The set of edges surrounding each face,
 *        derived from r1
 *     r4. E ---2 F    E,r4:
 *        The set of faces (usually 2) surrounding each edge
 *     r5. V ---* E    V,r5
 *        The set of edges surrounding each vertex
 *     r6. E ---2 V    E,r6: E() creates this relationship
 *        The set of vertices (always 2) making up each edge
 *
 *  The methods r1(), r2(), r3(), r4(), r5(), r6() create these relationships.
 *
 *  A SpaceStructure is made up of three SpaceEntitySet objects, each of which is
 *  a set of SpacePrimitive objects.  The type (vertex, edge, face) is kept as an
 *  attribute of the SpaceEntitySet to simplify the transformation of one type
 *  into another, and to simplify merging separate sets.
 */
//
//  For example, when the dual relationship is calculated, this approach simplifies the
//  transformation.  The dual relationship requires relationship #2.
//  Vertices are converted to faces by changing the base type of the
//  vertex space entity vector to SpacePrimitive.Face, and changing the base type of the
//  face space entity vector to SpacePrimitive.Vertex.  When this change is made, the
//  strategy is also converted.
//
//  To derive all single connected structures requires the stellation/trunction operations,
//  plus the diagonlize/normalize operations.  The diagonalize operation is an operation
//  that can only occur on four sided faces (or on two adjacent triangles).  For four sided
//  faces, this is just drawing a diagonal.  For adjacent triangular faces, this involves
//  rotating the common edge 90 degrees.
//
//  At the entire SpaceStructure level, the diagonalize operation is done only on 4 sided
//  faces, according to an algorithm which includes a vertex of the diagonal at most once.
//
//  My initial pass on this has the edges directionless.  This I think is a mistake,
//  because of the ccw face definition and because of efficiency.  It is faster to
//  have direction and allow the same edges to be added many times.
//

//
//  It seems now this should possibly be two classes.  The first should be
//  a largely directionless listing of relationships.  This allows for a fast
//  implementation, and is useful for many cases.
//
//  The second implementation should be what this started out to be... keeping
//  track of all entities in ordered lists.  This allows for operations on the
//  space structure, such as all variations on truncations and stellations.
//
public class SpaceStructure {
	// complete set of space primitives
	SpaceEntitySet v1;
	SpaceEntitySet v2;
	SpaceEntitySet v3;

	// complete set of prior space primitive sets, context for methods which may need prior
	// set
	SpaceEntitySet prevV1;
	SpaceEntitySet prevV2;
	SpaceEntitySet prevV3;

	// new sets for reprocessing
	SpaceEntitySet appendedV1;
	SpaceEntitySet appendedV2;
	SpaceEntitySet appendedV3;

	// used for building single face
	int[]  singleFaceCoords;
	int[]  singleTexCoords;
	int[]  singleNormalCoords;
	int[]  singleColorCoords;
	int    singleFaceCoordIdx;

/*	static final double DefaultTransparency = 0.0;
	double transparency = DefaultTransparency;

	static final double DefaultEmissiveColor = 0.0;
	double rEmissive = DefaultEmissiveColor;
	double gEmissive = DefaultEmissiveColor;
	double bEmissive = DefaultEmissiveColor;

	public void setEmissiveColor( double r, double g, double b ) {
		rEmissive = r;
		gEmissive = g;
		bEmissive = b;
	}

	public void setTransparency( double t ) {
		transparency = t;
		System.out.println( "Just set transparency to " + transparency );
	}*/

    static int idCounter = 1;
    int id;
    
    /** Class constructor */
	public SpaceStructure() {
		v1 = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Vertex ), this );
		v2 = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Edge ), this );
		v3 = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Face ), this );
		initArrays();
	}
	
	/** internal array and id initialization, used only by constructors */
	void initArrays() {
	    id = idCounter++;
		singleFaceCoords = new int[200];
		singleTexCoords = new int[200];
		singleNormalCoords = new int[200];
		singleColorCoords = new int[200];
		singleFaceCoordIdx = 0;
	}
	
	/** Copy constructor */
	public SpaceStructure( SpaceStructure original ) {
	    v1 = new SpaceEntitySet( original.getEntitySet( SpacePrimitive.Vertex ));
	    v2 = new SpaceEntitySet( original.getEntitySet( SpacePrimitive.Edge ));
	    v3 = new SpaceEntitySet( original.getEntitySet( SpacePrimitive.Face ));
	    initArrays();
	}
	
	/** Copy entities from 

	public void dump() {
		dump( "JJ" );
	}

	public void dump( String s ) {
		System.out.println( s );
		SpaceEntitySet.debug = true;
		SpaceEntitySet v = getEntitySet( SpacePrimitive.Vertex );
		v.dump( "Vertices" );
		SpaceEntitySet e = getEntitySet( SpacePrimitive.Edge );
		e.dump( "Edges" );
		SpaceEntitySet f = getEntitySet( SpacePrimitive.Face );
		f.dump( "Faces" );
		SpaceEntitySet.debug = false;
	}

    /** Get the number of entities in the set of the given type.
     *
     *  @param type  SpacePrimitive.Vertex, SpacePrimitive.Edge, or SpacePrimitive.Face
     *
     *  @return the number of vertices, edges, or faces in the structure.
     */
	public int getNumberEntities( int type ) {
		SpaceEntitySet ses = getEntitySet( type );
		return( ses.getNumberEntities() );
	}

	public void summarize() {
		SpaceEntitySet v = getEntitySet( SpacePrimitive.Vertex );
		SpaceEntitySet e = getEntitySet( SpacePrimitive.Edge );
		SpaceEntitySet f = getEntitySet( SpacePrimitive.Face );
		int triangleCount = 0;
		for ( int i = 0; i < f.getNumberEntities(); i++ ) {
			SpacePrimitive face = f.getEntity( i );
			int count = f.getCount( face, SpacePrimitive.Vertex );
			if ( count == 3 ) {
				triangleCount++;
			}
		}
		if ( f.getNumberEntities() == 0 ) {
			System.out.println( "Warning!  No faces!...." + v.getNumberEntities() + " vertices" );
		} else if ( f.getNumberEntities() == triangleCount ) {
			System.out.println( "Faces all triangles.  " + f.getNumberEntities() + " faces, " + v.getNumberEntities() + " vertices, " + e.getNumberEntities() + " edges." );
		} else {
			System.out.println( v.getNumberEntities() + " vertices, " + e.getNumberEntities() + " edges, " + f.getNumberEntities() + " faces." );
			System.out.println( "There are " + triangleCount + " triangles" );
		}
	}

	//
	//  The polygon reduction algorithm tests for edges to remove and merges adjacent faces
	//
	int mergeCount = 0;
	public int getMergeCount() {
		return( mergeCount );
	}

	int[] faceToMergeWith;

	public int coplanarTriToQuad( int ifsNumber ) {
		return( coplanarTriToQuad( 0.0000000001, ifsNumber ));
	}

    /** The set of faces removed by "edgeRemovalPolygonReduction" or "coplanarTriToQuad" */
    BitSet faceRemovalCandidateSet = null;

    /** The number of bits in the "removedFaceSet" above */
    int faceBitCount = 0;
    public BitSet getFaceBits() {
        return( faceRemovalCandidateSet );
    }
    public int getFaceBitCount() {
        return( faceBitCount );
    }

    /** Connectivity sections */
    Vector connectivityList;

    /** Create the connectivity sections for this structure */
    public void markConnectivity() {
        connectivityList = new Vector();
        r5();
        r3();
	    SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
	    SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
	    int numberEdges = edges.getNumberEntities();
	    int numberFaces = faces.getNumberEntities();
	    System.out.println( "createFtoE" );
   		createFtoE( edges, faces, numberFaces );

	    // keep going until all faces are placed in a connectivity group
	    BitSet edgeConnectivity = new BitSet( numberEdges );
	    System.out.println( "marking connectivity" );
	    while ( true ) {
	        int startEdge = getStartEdge( edgeConnectivity, numberEdges );
	        if ( startEdge == numberEdges ) {
	            break;
	        }
	        System.out.println( "startEdge is " + startEdge + " of " + numberEdges );
    	    BitSet faceConnectivity = new BitSet( numberFaces );
    	    markConnectivity( edgeConnectivity, edges, startEdge, faces, faceConnectivity );
    	    connectivityList.addElement( faceConnectivity );
	    }
	    System.out.println( "done." );
    }

    /** Get the number of connectivity sections */
    public int numberConnectivity() {
        return( connectivityList.size() );
    }

    /** Get the connectivity section number for a particular face */
    public int getConnectivity( int faceNo ) {
        int clistSize = connectivityList.size();
        for ( int i = 0; i < clistSize; i++ ) {
            BitSet b = (BitSet)connectivityList.elementAt( i );
            if ( b.get( faceNo )) {
                return( i );
            }
        }
        return( -1 );
    }

    int getStartEdge( BitSet edgeConnectivity, int numberEdges ) {
        for ( int i = 0; i < numberEdges; i++ ) {
            if ( !edgeConnectivity.get( i )) {
                return( i );
            }
        }
        return( numberEdges );
    }

    int[] nextToCheckList;
    int nextToCheckIdx;
    int maxToCheck;
    int getNextEdgeToCheck( BitSet edgesToCheck, int numberEdges ) {
        if ( nextToCheckIdx >= maxToCheck ) {
            fillNextToCheckList( edgesToCheck, numberEdges );
        }
        if ( nextToCheckIdx >= maxToCheck ) {
            return( -1 );
        }
        int result = nextToCheckList[ nextToCheckIdx ];
        nextToCheckIdx++;
        return( result );
    }   
    
    void fillNextToCheckList( BitSet edgesToCheck, int numberEdges ) {
        int idx = 0;
        nextToCheckIdx = 0;
        maxToCheck = 0;
        for ( int i = 0; i < numberEdges; i++ ) {
            if ( edgesToCheck.get( i )) {
                nextToCheckList[ maxToCheck ] = i;
                maxToCheck++;
            }
            if ( maxToCheck >= 100 ) {
                break;
            }
        }
    }

    /** Mark connectivity for a set of faces linked to a single start edge */
    void markConnectivity( BitSet edgeConnectivity, SpaceEntitySet edges, int startEdge, SpaceEntitySet faces, BitSet faceConnectivity ) {
        int numberEdges = edges.getNumberEntities();
        BitSet edgesToCheck = new BitSet( numberEdges );
        if ( nextToCheckList == null ) {
            nextToCheckList = new int[100];
            nextToCheckIdx = 0;
            maxToCheck = 0;
        }
        edgesToCheck.set( startEdge );
        int count = 0;
        while ( true ) {
            startEdge = getNextEdgeToCheck( edgesToCheck, numberEdges );
            if ( startEdge == -1 ) {
                break;
            }
            markEdge( edgeConnectivity, startEdge, edges, faces, faceConnectivity, edgesToCheck );
            count++;
            if (( count % 100 ) == 0 ) {
                System.out.println( "marked " + count + " edges" );
            }
        }
        int ecount = 0;
        for ( int i = 0; i < numberEdges; i++ ) {
            if ( edgeConnectivity.get( i )) {
                ecount++;
            }
        }
        System.out.println( "marked " + ecount + " of " + numberEdges + " edges" );
    }

    /** mark edges associated with a single edge.
     *
     *  @param edgeConnectivity BitSet indicating which edges have been marked for connectivity
     *  @param startEdge offset of the edge we are checking
     *  @param edges SpaceEntitySet of edges for this structure
     *  @param faces SpaceEntitySet of faces for this structure
     *  @param faceConnectivity set of faces marked for connectivity
     *  @param edgesToCheck working set of edges that remain to be checked
     */
    void markEdge( BitSet edgeConnectivity, int startEdge, SpaceEntitySet edges,
        SpaceEntitySet faces, BitSet faceConnectivity, BitSet edgesToCheck ) {
        edgesToCheck.clear( startEdge );
        if ( !edgeConnectivity.get( startEdge )) {
            edgeConnectivity.set( startEdge );
            SpacePrimitive edge = edges.getEntity( startEdge );
            markFace( edges.getValue( edge, SpacePrimitive.Face, 0 ), faces, faceConnectivity, edgesToCheck, edgeConnectivity );
            markFace( edges.getValue( edge, SpacePrimitive.Face, 1 ), faces, faceConnectivity, edgesToCheck, edgeConnectivity );
        }
    }

    /** mark the face in its connectivity set, add edges to check list */
    void markFace( int fidx, SpaceEntitySet faces, BitSet faceConnectivity, BitSet edgesToCheck, BitSet edgeConnectivity ) {
        if ( fidx != -1 ) {
            if ( !faceConnectivity.get( fidx )) {
                faceConnectivity.set( fidx );
                SpacePrimitive face = faces.getEntity( fidx );
                int numberEdges = faces.getCount( face, SpacePrimitive.Edge );
                for ( int i = 0; i < numberEdges; i++ ) {
                    int edge = faces.getValue( face, SpacePrimitive.Edge, i );
                    if ( !edgeConnectivity.get( edge )) {
                        edgesToCheck.set( edge );
                    }
                }
            }
        }
    }

    /** The set of edges that are removed */
    BitSet normalMergeSet = null;

    /** Get the index of the face merged with a particular face.
     *
     *  @param  faceIdx  face that continues to exist
     *  @return index of face that is merged with faceIdx, -1 if none
     */
    public int getMergeFace( int faceIdx ) {
        if ( normalMergeSet == null ) {
            return( -1 );
        }
        SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
        SpacePrimitive face = faces.getEntity( faceIdx );
        int eCount = faces.getCount( face, SpacePrimitive.Edge );
        for ( int i = 0; i < eCount; i++ ) {
            int eval = faces.getValue( face, SpacePrimitive.Edge, i );
            if ( normalMergeSet.get( eval )) {
                SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
                SpacePrimitive edge = edges.getEntity( eval );
                int fCount = edges.getCount( edge, SpacePrimitive.Face );
                for ( int j = 0; j < fCount; j++ ) {
                    int fval = edges.getValue( edge, SpacePrimitive.Face, j );
                    if ( fval != faceIdx ) {
                        return( fval );
                    }
                }
            }
        }
        return( -1 );
    }

    /** Merge coplanar polygons.
     *  @param limit coplanar triangles detected by a getting area defined by
     *    normals to each triangle.  Zero means they are coplanar, the "limit"
     *    parameter is a zero equivalent, any area less than this is treated
     *    as zero.
     */
	public int coplanarTriToQuad( double limit, int ifsNumber ) {
		// Generate edges
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		int numberVertices = vertices.getNumberEntities();
		GlobalProgressIndicator.activateAlternateProgressIndicator( 0 );
		GlobalProgressIndicator.setUnitSize( numberVertices*2, 4 );
		E();

		// generate the faces surrounding each edge
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		int numberEdges = edges.getNumberEntities();
		r4();

		// Test each edge for removal by seeing if its surrounding faces are coplanar
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		boolean[] edgeCanBeRemoved = new boolean[ numberEdges ];
		faceBitCount = faces.getNumberEntities();
		faceRemovalCandidateSet = new BitSet( faceBitCount );
		for ( int i = 0; i < numberEdges; i++ ) {
		    GlobalProgressIndicator.markProgress();
			edgeCanBeRemoved[i] = false;
			SpacePrimitive edge = edges.getEntity( i );
			SpacePrimitive v1 = edges.getEntity( edge, SpacePrimitive.Vertex, 0 );
			SpacePrimitive v2 = edges.getEntity( edge, SpacePrimitive.Vertex, 1 );
			if (( v1 == null ) || ( v2 == null )) {
				continue;
			}
			if ( v1.equalTo( v2 )) {
			    continue;
			}
			int v1offset = edges.getValue( edge, SpacePrimitive.Vertex, 0 );
			int v2offset = edges.getValue( edge, SpacePrimitive.Vertex, 1 );
			SpacePrimitive f1 = edges.getEntity( edge, SpacePrimitive.Face, 0 );
			SpacePrimitive f2 = edges.getEntity( edge, SpacePrimitive.Face, 1 );
			if (( f1 == null ) || ( f2 == null )) {
				continue;
			}
			// only do this operation for coplanar triangles
			if ( faces.getCount( f1, SpacePrimitive.Vertex ) != 3 ) {
				continue;
			}
			if ( faces.getCount( f2, SpacePrimitive.Vertex ) != 3 ) {
				continue;
			}
			SpacePrimitive v3 = null;
			SpacePrimitive v4 = null;

			// v3 is an f1 vertex other than v1 and v2
			int v3offset = -1;
			for ( int j = 0; j < faces.getCount( f1, SpacePrimitive.Vertex ); j++ ) {
				SpacePrimitive v = faces.getEntity( f1, SpacePrimitive.Vertex, j );
				if (( v != v1 ) && ( v != v2 )) {
				    // equalTo tests required because we see 3DS Max output making degenerate
				    // triangular faces
				    if ( v.equalTo( v1 )) {
				        continue;
				    }
				    if ( v.equalTo( v2 )) {
				        continue;
				    }
					v3 = v;
					v3offset = faces.getValue( f1, SpacePrimitive.Vertex, j );
					break;
				}
			}
			if ( v3 == null ) {
			    continue;
			}

			// v4 is an f2 vertex other than v1 and v2
			int v4offset = -1;
			for ( int j = 0; j < faces.getCount( f2, SpacePrimitive.Vertex ); j++ ) {
				SpacePrimitive v = faces.getEntity( f2, SpacePrimitive.Vertex, j );
				if ( v.equalTo( v1 )) {
				    continue;
				} else if ( v.equalTo( v2 )) {
				    continue;
				} else if ( v.equalTo( v3 )) {
				    continue;
				} else {
    				v4 = v;
    				v4offset = faces.getValue( f2, SpacePrimitive.Vertex, j );
    				break;
    			}
			}

			if ( v4 == null ) {
				continue;
			}

			if ( coplanar( v1, v2, v3, v4, limit )) {
//				System.out.println( "Edge " + i + " can be removed" );
                // here we have to check that the faces have the same
                // per vertex data for the edge v1->v2 and edge v2->v1
                Object f1attachment = f1.getAttachedObject();
                Object f2attachment = f2.getAttachedObject();
                if (( f1attachment != null ) && ( f2attachment != null )) {
                    TypedObject f1data = (TypedObject)((TypedObject)f1attachment).getObject( PerVertexData.Texture );
                    TypedObject f2data = (TypedObject)((TypedObject)f2attachment).getObject( PerVertexData.Texture );
                    if (( f1data != null ) && ( f2data != null )) {
                        PerVertexData f1vdata = (PerVertexData)f1data.getObject( PerVertexData.Texture );
                        PerVertexData f2vdata = (PerVertexData)f2data.getObject( PerVertexData.Texture );
                        if (( f1vdata != null ) && ( f2vdata != null )) {
                            if (( f1vdata.getValue( v1offset ) == f2vdata.getValue( v1offset )) &&
                                ( f1vdata.getValue( v2offset ) == f2vdata.getValue( v2offset ))) {
                                    edgeCanBeRemoved[i] = true;
                            }
                        } else {
                            edgeCanBeRemoved[i] = true;
                        }
                   } else {
                        edgeCanBeRemoved[i] = true;
                   }
                } else {
    				edgeCanBeRemoved[i] = true;
    			}
//			} else {
//				System.out.println( "Edge " + i + " cannot be removed" );
			}
		}

		// for each face, if face has any edge marked for removal, see if face can be
		// removed, note that this marks ALL faces adjacent to removed edge as removable,
		// when in face, one of those faces has to remain.  This is handled by the face
		// merge algorithm
		int numberFaces = faces.getNumberEntities();
		faceToMergeWith = new int[ numberFaces ];
		mergeCount = 0;
		GlobalProgressIndicator.replaceHalfUnit( numberFaces );
		for ( int i = 0; i < numberFaces; i++ ) {
		    GlobalProgressIndicator.markProgress();
			faceToMergeWith[ i ] = -1;
			SpacePrimitive face = faces.getEntity( i );
			for ( int j = 0; j < faces.getCount( face, SpacePrimitive.Edge ); j++ ) {
				int edge = faces.getValue( face, SpacePrimitive.Edge, j );
				if ( edgeCanBeRemoved[ edge ] ) {
                    faceRemovalCandidateSet.set( i );
//                    System.out.println( "face " + i + " is a removal candidate" );
					SpacePrimitive fedge = edges.getEntity( edge );
					int f1value = edges.getValue( fedge, SpacePrimitive.Face, 0 );
					int f2value = edges.getValue( fedge, SpacePrimitive.Face, 1 );
					if ( i == f1value ) {
						faceToMergeWith[ i ] = f2value;
					} else {
						faceToMergeWith[ i ] = f1value;
					}
					break;
				}
			}
		}
//		for ( int i = 0; i < numberFaces; i++ ) {
//			if ( faceRemovalCandidateSet.get( i )) { // if ( faceCanBeRemoved[i] ) {
//				System.out.println( "Face " + i + " can merge with face " + faceToMergeWith[ i ] );
//			} else {
//				System.out.println( "Face " + i + " cannot be removed.." );
//			}
//		}

		// Face merging algorithm.  For now, we only merge two faces (a simplification
		// of the general problem).
		//
		//  1. select face to merge with.  If this face indicates current face, merge can
		//     continue.  If not, merge has already taken place.
		//  2.
		SpaceEntitySet newFaces = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Face ), this );
		newFaces.validate( SpacePrimitive.Vertex );
		int[] offsets = new int[200];
		
		// array of faces that are removed
		BitSet facesRemoved = new BitSet( numberFaces );
		GlobalProgressIndicator.endGame( numberFaces );
		for ( int i = 0; i < numberFaces; i++ ) {
			SpacePrimitive face = faces.getEntity( i );
            if ( faceRemovalCandidateSet.get( i ) ) {
                // if the face we are marked to merge with is already removed,
                // can't do anything
	    		int disappearingFace = faceToMergeWith[i];
                if ( facesRemoved.get( disappearingFace )) {
//    			    System.out.println( "face " + i + " merge partner " + faceToMergeWith[i] + " already removed" );
    			    continue;
    			} else if ( facesRemoved.get( i )) {
//    			    System.out.println( "face " + i + " already removed" );
    			    continue;
	    		} else if ( faceRemovalCandidateSet.get( disappearingFace )) {
    				// merge face with the other one
//	    			System.out.println( "Merging face " + i + " with " + disappearingFace );
		    		mergeCount++;
			    	facesRemoved.set( disappearingFace );
				    faceRemovalCandidateSet.clear( disappearingFace );
				    faceRemovalCandidateSet.clear( i );
    				SpacePrimitive f2 = faces.getEntity( disappearingFace );
    				faceToMergeWith[ disappearingFace ] = i;

	    			// get the values of the two shared points
    				int v1shared = -1;
    				int f1v1offset = -1;
    				int f1v2offset = -1;
    				int v2shared = -1;
    				int f2v1offset = -1;
    				int f2v2offset = -1;
    				int offsetIdx = 0;
    				for ( int j = 0; j < faces.getCount( face, SpacePrimitive.Vertex ); j++ ) {
    					int testv1 = faces.getValue( face, SpacePrimitive.Vertex, j );
    					for ( int k = 0; k < faces.getCount( f2, SpacePrimitive.Vertex ); k++ ) {
    						int testv2 = faces.getValue( f2, SpacePrimitive.Vertex, k );
    						if ( testv1 == testv2 ) {
    							if ( v1shared == -1 ) {
    								v1shared = testv1;
    								f1v1offset = j;
    								f2v1offset = k;
    								break;
    							} else if ( v2shared == -1 ) {
    								v2shared = testv2;
    								f1v2offset = j;
    								f2v2offset = k;
    								break;
    							}
    						}
    					}
    					if ( v2shared != -1 ) {
    						break;
    					}
    				}

    				if (( v1shared == -1 ) || ( v2shared == -1 )) {
    					System.out.println( "Didn't find shared values between faces!!!" );
    				} else {
    					// For f1, what is first offset past the two shared points
    					if ( f1v1offset > f1v2offset ) {
    						int t = f1v1offset;
    						f1v1offset = f1v2offset;
    						f1v2offset = t;
    					}
    					// at this point, f1v1offset < f1v2offset
    					int f1scan = -1;
    					if ( f1v1offset == 0 ) {
    						if ( f1v2offset == 1 ) {
    							f1scan = 2;
    						} else {
    							f1scan = 1;
    						}
    					} else {
    						f1scan = f1v2offset + 1;
    						if ( f1scan == faces.getCount( face, SpacePrimitive.Vertex )) {
    							f1scan = 0;
    						}
    					}
    					int fcount = faces.getCount( face, SpacePrimitive.Vertex );
    					for ( int j = 0; j < ( fcount - 1 ); j++ ) {
    						offsets[ offsetIdx ] = faces.getValue( face, SpacePrimitive.Vertex, ( j + f1scan ) % fcount );
    						offsetIdx++;
    					}

    					// For f2, what is first offset past the two shared points
    					if ( f2v1offset > f2v2offset ) {
    						int t = f2v1offset;
    						f2v1offset = f2v2offset;
    						f2v2offset = t;
    					}
    					// at this point, f1v1offset < f1v2offset
    					int f2scan = -1;
    					if ( f2v1offset == 0 ) {
    						if ( f2v2offset == 1 ) {
    							f2scan = 2;
    						} else {
    							f2scan = 1;
    						}
    					} else {
    						f2scan = f2v2offset + 1;
    						if ( f2scan == faces.getCount( f2, SpacePrimitive.Vertex )) {
    							f2scan = 0;
    						}
    					}
    					fcount = faces.getCount( f2, SpacePrimitive.Vertex );
    					for ( int j = 0; j < ( fcount - 1 ); j++ ) {
    						offsets[ offsetIdx ] = faces.getValue( f2, SpacePrimitive.Vertex, ( j + f2scan ) % fcount );
    						offsetIdx++;
    					}
    					SpacePrimitive newFace = new SpacePrimitive( offsets, offsetIdx );
    					Object faceAttachment = face.getAttachedObject();
    					Object f2Attachment = f2.getAttachedObject();
    					if (( faceAttachment != null ) && ( f2Attachment != null )) {
    					    if ( faceAttachment instanceof CompositeObject ) {
        				        if ( f2Attachment instanceof CompositeObject ) {
    					            Object f1obj = ((CompositeObject)faceAttachment).getObjectA();
    					            Object f2obj = ((CompositeObject)f2Attachment).getObjectA();
    					            newFace.attachObject( new PerVertexData( f1obj, f2obj ));
    					            f1obj = ((CompositeObject)faceAttachment).getObjectB();
    					            f2obj = ((CompositeObject)f2Attachment).getObjectB();
    					            newFace.attachObject( new PerVertexData( f1obj, f2obj ));
    					        }
        				    } else {
            					newFace.attachObject( new PerVertexData( face.getAttachedObject(), f2.getAttachedObject() ));
            				}
    					}
    					newFaces.addEntity( newFace );
    				}
    			}
			} else if ( !facesRemoved.get( i )) {
//				System.out.println( "Preserving face " + i );
				int offsetIdx = faces.getCount( face, SpacePrimitive.Vertex );
				for ( int j = 0; j < offsetIdx; j++ ) {
					offsets[j] = faces.getValue( face, SpacePrimitive.Vertex, j );
				}
				SpacePrimitive newFace = new SpacePrimitive( offsets, offsetIdx );
				if ( face.getAttachedObject() != null ) {
					newFace.attachObject( face.getAttachedObject() );
				}
				newFaces.addEntity( newFace );
			}
		}
		faceRemovalCandidateSet = facesRemoved;
//		for ( int i = 0; i < numberFaces; i++ ) {
//			if ( faceRemovalCandidateSet.get( i )) { // if ( faceCanBeRemoved[i] ) {
//				System.out.println( "Face " + i + " merged with face " + faceToMergeWith[ i ] );
//			} else {
//				System.out.println( "Face " + i + " remains.." );
//			}
//		}
		replaceSet( SpacePrimitive.Face, newFaces );
		GlobalProgressIndicator.deactivateAlternateProgressIndicator();
		return( mergeCount );
	}

	public boolean coplanar( SpacePrimitive v1, SpacePrimitive v2, SpacePrimitive v3, SpacePrimitive v4, double limit ) {
		double v1x = v1.getX();
		double v1y = v1.getY();
		double v1z = v1.getZ();
		double v2x = v2.getX() - v1x;
		double v2y = v2.getY() - v1y;
		double v2z = v2.getZ() - v1z;
		double v3x = v3.getX() - v1x;
		double v3y = v3.getY() - v1y;
		double v3z = v3.getZ() - v1z;
		double v4x = v4.getX() - v1x;
		double v4y = v4.getY() - v1y;
		double v4z = v4.getZ() - v1z;
		double result = 0.0;
		result += v2x * v3y * v4z;
		result += v3x * v4y * v2z;
		result += v4x * v2y * v3z;
		result -= v2x * v4y * v3z;
		result -= v3x * v2y * v4z;
		result -= v4x * v3y * v2z;
//		System.out.println( "result is " + result );
		if ( result < 0 ) result = -1* result;
		if ( result <= limit ) {
//		    if ( result != 0 ) {
	//	        System.out.println( "non zero result " + result );
		//    }
			return( true );
		} else {
			return( false );
		}
	}

    /** Multiply each vertex by a factor */
	public void expand( float factor ) {
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		vertices.setCenterLocation();
		for ( int i = 0; i < vertices.getNumberEntities(); i++ ) {
			SpacePrimitive v = vertices.getEntity( i );
			v.expand( factor );
		}
	}

	public void createSet( SpaceEntitySet obsoleteSet, int type ) {
		if ( obsoleteSet == v1 ) {
			v1 = new SpaceEntitySet( new Strategy( this, type ), this );
		} else if ( obsoleteSet == v2 ) {
			v2 = new SpaceEntitySet( new Strategy( this, type ), this );
		} else if ( obsoleteSet == v3 ) {
			v3 = new SpaceEntitySet( new Strategy( this, type ), this );
		}
	}

	int preAppendCount = 0;
	public void appendSet( int type, SpaceEntitySet newSet ) {
		SpaceEntitySet s = getEntitySet( type );
		preAppendCount = s.getNumberEntities();
		if ( s == v1 ) {
			appendedV1 = newSet;
			s.appendSet( newSet );
		} else if ( s == v2 ) {
			appendedV2 = newSet;
			s.appendSet( newSet );
		} else if ( s == v3 ) {
			appendedV3 = newSet;
			s.appendSet( newSet );
		}
	}

	public void replaceSet( int type, SpaceEntitySet newSet ) {
		SpaceEntitySet obsoleteSet = getEntitySet( type );
		if ( obsoleteSet == v1 ) {
			prevV1 = v1;
			v1 = newSet;
		} else if ( obsoleteSet == v2 ) {
			prevV2 = v2;
			v2 = newSet;
		} else if ( obsoleteSet == v3 ) {
			prevV3 = v3;
			v3 = newSet;
		}
	}

	public void invalidate( int type ) {
		SpaceEntitySet newSet = new SpaceEntitySet( new Strategy( this, type ), this );
		replaceSet( type, newSet );
	}


    /** Get the SpaceEntitySet for the vertices, edges, or faces.
     *
     *  @param  type  either SpacePrimitive.Vertex, SpacePrimitive.Edge, or
     *     SpacePrimitive.Face
     *
     *  @return  the SpaceEntitySet of the given type, or null if none exists
     *     for that type.
     */
	public SpaceEntitySet getEntitySet( int type ) {
		if ( v1.getBaseType() == type ) {
			return( v1 );
		} else if ( v2.getBaseType() == type ) {
			return( v2 );
		} else if ( v3.getBaseType() == type ) {
			return( v3 );
		} else {
			return( null );
		}
	}

	public SpaceEntitySet getPrevEntitySet( int type ) {
		if (( prevV1 != null ) && ( prevV1.getBaseType() == type )) {
			return( prevV1 );
		} else if (( prevV2 != null ) && ( prevV2.getBaseType() == type )) {
			return( prevV2 );
		} else if (( prevV3 != null ) && ( prevV3.getBaseType() == type )) {
			return( prevV3 );
		} else {
			return( null );
		}
	}

	public SpaceEntitySet getAppendedSet( int type ) {
		if (( appendedV1 != null ) && ( appendedV1.getBaseType() == type )) {
			return( appendedV1 );
		} else if (( appendedV2 != null ) && ( appendedV2.getBaseType() == type )) {
			return( appendedV2 );
		} else if (( appendedV3 != null ) && ( appendedV3.getBaseType() == type )) {
			return( appendedV3 );
		} else {
			return( null );
		}
	}


	/** Set the center location for each SpacePrimitive by averaging location of surrounding entities.
	 *
	 *  @param  baseType  the SpacePrimitive type whose locations are to be calculated.
	 *  @param  avgType   the type of surrounding SpacePrimitive to use in the average calculations.
	 */
	public void setLocation( int baseType, int avgType ) {
		setLocation( baseType, avgType, false, 0, 0, 0, 0 );
	}

	public void setLocation( int baseType, int avgType, float cx, float cy, float cz, float distance ) {
		setLocation( baseType, avgType, true, cx, cy, cz, distance );
	}

	void setLocation( int baseType, int avgType, boolean setDistance, float cx, float cy, float cz, float distance ) {
		SpaceEntitySet s = getEntitySet( baseType );
		s.dump( "This is base set" );
		SpaceEntitySet ref = getEntitySet( avgType );
		ref.dump( "This is surrounding set" );
		for ( int i = 0; i < s.getNumberEntities(); i++ ) {
			SpacePrimitive sp = s.getEntity( i );
			float xSum = 0;
			float ySum = 0;
			float zSum = 0;
			int count = s.getCount( sp, avgType );
			for ( int j = 0; j < count; j++ ) {
				int index = s.getValue( sp, avgType, j );
				SpacePrimitive spref = ref.getEntity( index );
				xSum += spref.getX();
				ySum += spref.getY();
				zSum += spref.getZ();
			}
			xSum = xSum/count;
			ySum = ySum/count;
			zSum = zSum/count;
			sp.setLocation( xSum, ySum, zSum );

			if ( setDistance ) {
				// get the distance from center
				s.setDistance( sp, distance );
			}
		}
	}

	/** Add a vertex to the SpaceEntitySet for vertices.
	 *  This is used to initially create the vertex set when an IndexedFaceSet
	 *  is encountered.
	 */
	public void addVertex( float x, float y, float z ) {
		getEntitySet( SpacePrimitive.Vertex ).addEntity( new SpacePrimitive( x, y, z ));
	}

	//
	//  Need to replace this all with FaceBuilder internal class
	//  FaceBuilder attributes are:  singleTexCoords, singleNormalCoords,
	//  singleColorCoords, createTexObject, createNormalObject,
	//  createColorObject.
	//
	boolean createTexObject = false;
	public void addTexCoord( int value ) {
		singleTexCoords[ singleFaceCoordIdx ] = value;
		createTexObject = true;
	}

	boolean createNormalObject = false;
	public void addNormalCoord( int value ) {
	    singleNormalCoords[ singleFaceCoordIdx ] = value;
	    createNormalObject = true;
	}

	boolean createColorObject = false;
	public void addColorCoord( int value ) {
	    singleColorCoords[ singleFaceCoordIdx ] = value;
	    createColorObject = true;
	}

    /** Add another coord to the face being built one coord at a time */
	public void addFaceCoord( int value ) {
		if ( value == -1 ) {
			addFace();
		} else {
			singleFaceCoords[ singleFaceCoordIdx ] = value;
			singleFaceCoordIdx++;
		}
	}

	/** Add a face that has been collected on coord at a time */
	SpacePrimitive newlyAddedFace;
	public void addFace() {
		SpacePrimitive sp = new SpacePrimitive(
			singleFaceCoords, singleFaceCoordIdx );
		if ( createTexObject ) {
			sp.attachObject( new PerVertexData( singleFaceCoords,
				singleTexCoords, singleFaceCoordIdx, PerVertexData.Texture ));
			createTexObject = false;
		}
		if ( createNormalObject ) {
		    sp.attachObject( new PerVertexData( singleFaceCoords,
				singleNormalCoords, singleFaceCoordIdx, PerVertexData.Normal ));
		    createNormalObject = false;
		}
		if ( createColorObject ) {
		    sp.attachObject( new PerVertexData( singleFaceCoords,
				singleColorCoords, singleFaceCoordIdx, PerVertexData.Color ));
		    createColorObject = false;
		}
		getEntitySet( SpacePrimitive.Face ).addEntity( sp );
		getEntitySet( SpacePrimitive.Face ).validate( SpacePrimitive.Vertex );
		singleFaceCoordIdx = 0;
		newlyAddedFace = sp;
	}

	/** Hack, since I don't use face location, don't want to make
	 *  SpacePrimitive too big...
	 */
	public void setFaceColor( int value ) {
	    newlyAddedFace.setLocation( value, value, value );
	}


	//
	//  Diagonalize an entire structure according to the following algorithm.
	//  1. diagonalize the first square face
	//  2. for each face that includes a point in a previously diagonalized face,
	//     diagonalize the face so that the diagonal does not include a vertex in
	//     any previous diagonal
	//
	public void diagonalize() {
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		int originalFaceCount = faces.getNumberEntities();
		DiagonalizeInfo d = new DiagonalizeInfo();
		for ( int i = 0; i < originalFaceCount; i++ ) {
			SpacePrimitive face = faces.getEntity( i );
			if ( faces.getCount( face, SpacePrimitive.Vertex ) == 4 ) {
				faces.diagonalize( face, d );
			}
		}
	}

	float max1;
	float max2;
	float max3;
	void initMaxes() {
	    max1 = max2 = max3 = 0;
	}

	void markMax( float d ) {
	    if ( max1 == 0 ) {
	        max1 = max2 = max3 = d;
	    } else if ( d > max1 ) {
	        max3 = max2;
	        max2 = max1;
	        max1 = d;
	    } else if ( d > max2 ) {
	        max3 = max2;
	        max2 = d;
	    } else if ( d > max3 ) {
	        max3 = d;
	    }
	}


	/** Parallel edge joining algorithm.
	 */
	public int parallelEdgePolygonReduction( float threshold ) {
	    SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
	    int numberVertices = vertices.getNumberEntities();
		GlobalProgressIndicator.activateAlternateProgressIndicator( 0 );
		GlobalProgressIndicator.setUnitSize( numberVertices*2, 3 );
        r5();
        r3();
	    SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
	    BitSet vertexCandidates = new BitSet( numberVertices );
	    BitSet verticesRuledOut = new BitSet( numberVertices );
	    Hashtable joinTo = new Hashtable();
	    int count = 0;
	    GlobalProgressIndicator.endGame( numberVertices );
	    for ( int i = 0; i < numberVertices; i++ ) {
	        GlobalProgressIndicator.markProgress();
	        if ( !verticesRuledOut.get( i )) {
       	        if ( checkVertex( i, edges, vertices, verticesRuledOut, vertexCandidates, threshold, joinTo )) {
       	            count++;
       	        }
       	    }
    	}
		GlobalProgressIndicator.deactivateAlternateProgressIndicator();
    	return( count );
	}

	/** Check all edges from a vertex, if two are parallel,
	 *  mark vertex as a candidate
 	 */
	float[] dxes;
	float[] dyes;
	float[] dzes;
	int[] vno;
	boolean checkVertex( int candidate, SpaceEntitySet edges, SpaceEntitySet vertices, BitSet verticesRuledOut, BitSet vertexCandidates, float threshold, Hashtable joinTo ) {
	    SpacePrimitive v = vertices.getEntity( candidate );
	    int numberEdges = vertices.getCount( v, SpacePrimitive.Edge );

	    // create a set of normalized vectors around each vertex
	    if ( dxes == null ) {
    	    dxes = new float[ 10 ];
	        dyes = new float[ 10 ];
	        dzes = new float[ 10 ];
	        vno = new int[10];
	    }
	    if ( numberEdges > 10 ) {
	        numberEdges = 10;
	    }
	    for ( int i = 0; i < numberEdges; i++ ) {
	        SpacePrimitive e = edges.getEntity( vertices.getValue( v, SpacePrimitive.Edge, i ));
	        int v1no = edges.getValue( e, SpacePrimitive.Vertex, 0 );
            vno[i] = -1;
	        if ( v1no != candidate ) {
	            if ( !verticesRuledOut.get( v1no )) {
    	            SpacePrimitive v1 = vertices.getEntity( v1no );
    	            dxes[i] = v.getX() - v1.getX();
    	            dyes[i] = v.getY() - v1.getY();
    	            dzes[i] = v.getZ() - v1.getZ();
    	            vno[i] = v1no;
    	        }
	        } else {
	            v1no = edges.getValue( e, SpacePrimitive.Vertex, 1 );
	            if ( v1no != candidate ) {
	                if ( !verticesRuledOut.get( v1no )) {
    	                SpacePrimitive v1 = vertices.getEntity( v1no );
    	                dxes[i] = v.getX() - v1.getX();
    	                dyes[i] = v.getY() - v1.getY();
    	                dzes[i] = v.getZ() - v1.getZ();
    	                vno[i] = v1no;
    	            }
	            }
	        }
	    }
	    // check for parallel
	    for ( int i = 0; i < numberEdges - 1; i++ ) {
	        if ( vno[i] == -1 ) continue;
	        dxes[i] *= -1;
	        dyes[i] *= -1;
	        dzes[i] *= -1;
	        for ( int j = i + 1; j < numberEdges; j++ ) {
	            if ( vno[j] == -1 ) continue;
	            float diff = Math.abs( dxes[i] - dxes[j] ) +
	                Math.abs( dyes[i] - dyes[j] ) + Math.abs( dzes[i] - dzes[j] );
	            if ( diff < threshold ) {
	                markCandidate( candidate, vno[i], vno[j], verticesRuledOut, vertexCandidates );
	                SpacePrimitive vto = vertices.getEntity( vno[i] );
	                // join by moving point to be identical, v in center
	                v.setLocation( vto.getX(), vto.getY(), vto.getZ() );
	                return( true );
	            }
	        }
	    }
	    return( false );
	}

	void markCandidate( int candidate, int bro1, int bro2, BitSet verticesRuledOut, BitSet vertexCandidates ) {
	    verticesRuledOut.set( bro1 );
	    verticesRuledOut.set( bro2 );
	    vertexCandidates.set( candidate );
	}


	/** Small triangle removal polygon reduction algorithm.
	 *
	 *  @param minimumNumberFaces the smallest number of faces that have to
	 *    exist for this algorithm to occur
	 *  @param percentThreshold % of triangles to consider as removal 
	 *    candidates
	 *  @param preserveColorBoundaries when true, edges between faces with
	 *    different colors are not affected by this algorithm.
	 *
	 *  @return true if anything changed in the structure, otherwise false
	 */
	public boolean smallTrianglePolygonReduction( int minimumNumberFaces, int percentThreshold, boolean preserveColorBoundaries ) {
        SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
        int numberVertices = vertices.getNumberEntities();
		GlobalProgressIndicator.activateAlternateProgressIndicator( 0 );
		GlobalProgressIndicator.setUnitSize( numberVertices*2, 5 );
        r5();
        r3();
	    SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
	    SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
	    int numberEdges = edges.getNumberEntities();
	    int faceCount = faces.getNumberEntities();
	    if ( faceCount > minimumNumberFaces ) {
	        System.out.println( "Removing polygons, " + faceCount + " faces." );
       		createFtoE( edges, faces, faceCount );
	        float[] faceSize = new float[ faceCount ];
	        int markedCount = 0;
	        int numberToMark = faceCount * ( 100 - percentThreshold )/100;
	        initMaxes();
	        GlobalProgressIndicator.endGame( faceCount*3 + numberEdges );
	        for ( int i = 0; i < faceCount; i++ ) {
	            GlobalProgressIndicator.markProgress();
	            SpacePrimitive face = faces.getEntity( i );
				int offsetIdx = faces.getCount( face, SpacePrimitive.Vertex );
				// keep all non-triangle faces
				if ( offsetIdx != 3 ) {
				    faceSize[i] = -1;
				    markedCount++;
				} else {
				    SpacePrimitive v1 = faces.getEntity( face, SpacePrimitive.Vertex, 0 );
				    SpacePrimitive v2 = faces.getEntity( face, SpacePrimitive.Vertex, 1 );
				    SpacePrimitive v3 = faces.getEntity( face, SpacePrimitive.Vertex, 2 );
				    // estimate face size by summing edge length
				    float dx12 = v1.getX() - v2.getX();
				    if ( dx12 < 0 ) dx12 = dx12*-1;
				    float dx13 = v1.getX() - v3.getX();
				    if ( dx13 < 0 ) dx13 = dx13*-1;
				    float dx23 = v2.getX() - v3.getX();
				    if ( dx23 < 0 ) dx23 = dx23*-1;
				    float dy12 = v1.getY() - v2.getY();
				    if ( dy12 < 0 ) dy12 = dy12*-1;
				    float dy13 = v1.getY() - v3.getY();
				    if ( dy13 < 0 ) dy13 = dy13*-1;
				    float dy23 = v2.getY() - v3.getY();
				    if ( dy23 < 0 ) dy23 = dy23*-1;
				    float dz12 = v1.getZ() - v2.getZ();
				    if ( dz12 < 0 ) dz12 = dz12*-1;
				    float dz13 = v1.getZ() - v3.getZ();
				    if ( dz13 < 0 ) dz13 = dz13*-1;
				    float dz23 = v2.getZ() - v3.getZ();
				    if ( dz23 < 0 ) dz23 = dz23*-1;
//				    faceSize[i] = dx12 + dx13 + dx23 + dy12 + dy13 + dy23 + dz12 + dz13 + dz23;
float sum1 = dx12 + dy12 + dz12 + dx13 + dy13 + dz13;
float sum2 = dx23 + dy23 + dz23 + dx12 + dy12 + dz12;
float sum3 = dx13 + dy13 + dz13 + dx23 + dy23 + dz23;
//double size = sum1;
//if ( sum2 < size ) size = sum2;
//if ( sum3 < size ) size = sum3;
faceSize[i] = sum1*sum2*sum3;
				    markMax( faceSize[i] );
		        }
		    }
	        System.out.println( "Got all the face sizes, mark those to keep\n" );
	        System.out.println( markedCount + " currently marked, need to mark " + numberToMark );
	        for ( int i = 0; i < faceCount; i++ ) {
	            GlobalProgressIndicator.markProgress();
	            if ( faceSize[i] != -1 ) {
	                if ( faceSize[i] >= max3 ) {
	                    faceSize[i] = -1;
	                    markedCount++;
	                }
	            }
	        }

	        // mark faces as unavailable if they have any edges that are
	        // not shared with another face
	        int boundaryFaceCount = 0;
	        int colorBoundaryFaceCount = 0;
            for ( int i = 0; i < numberEdges; i++ ) {
                GlobalProgressIndicator.markProgress();
                SpacePrimitive edge = edges.getEntity( i );
                if ( edges.getValue( edge, SpacePrimitive.Face, 1 ) == -1 ) {
                    int fidx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                    if ( fidx >= 0 ) {
                        if ( faceSize[ fidx ] != -1 ) {
                            faceSize[ fidx ] = -1;
                            boundaryFaceCount++;
                            markedCount++;
                        }
                        SpacePrimitive face = faces.getEntity( fidx );
                        int e1 = faces.getValue( face, SpacePrimitive.Edge, 0 );
                        if ( e1 != i ) {
                            edge = edges.getEntity( e1 );
                            int f1idx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                            int f2idx = edges.getValue( edge, SpacePrimitive.Face, 1 );
                            if (( f1idx != -1 ) && ( faceSize[ f1idx ] != -1 )) {
                                faceSize[ f1idx ] = -1;
                                boundaryFaceCount++;
                                markedCount++;
                            }
                            if (( f2idx != -1 ) && ( faceSize[ f2idx ] != -1 )) {
                                faceSize[ f2idx ] = -1;
                                boundaryFaceCount++;
                                markedCount++;
                            }
                        }
                        int e2 = faces.getValue( face, SpacePrimitive.Edge, 1 );
                        if ( e2 != i ) {
                            edge = edges.getEntity( e2 );
                            int f1idx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                            int f2idx = edges.getValue( edge, SpacePrimitive.Face, 1 );
                            if (( f1idx != -1 ) && ( faceSize[ f1idx ] != -1 )) {
                                faceSize[ f1idx ] = -1;
                                boundaryFaceCount++;
                                markedCount++;
                            }
                            if (( f2idx != -1 ) && ( faceSize[ f2idx ] != -1 )) {
                                faceSize[ f2idx ] = -1;
                                boundaryFaceCount++;
                                markedCount++;
                            }
                        }
                        int e3 = faces.getValue( face, SpacePrimitive.Edge, 2 );
                        if ( e3 != i ) {
                            edge = edges.getEntity( e3 );
                            int f1idx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                            int f2idx = edges.getValue( edge, SpacePrimitive.Face, 1 );
                            if (( f1idx != -1 ) && ( faceSize[ f1idx ] != -1 )) {
                                faceSize[ f1idx ] = -1;
                                boundaryFaceCount++;
                                markedCount++;
                            }
                            if (( f2idx != -1 ) && ( faceSize[ f2idx ] != -1 )) {
                                faceSize[ f2idx ] = -1;
                                boundaryFaceCount++;
                                markedCount++;
                            }
                        }
//                        System.out.println( "Edge " + i + " on boundary, face " + fidx + " skipped" );
                    }
               } else if ( preserveColorBoundaries ) {
                    int f1idx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                    int f2idx = edges.getValue( edge, SpacePrimitive.Face, 1 );
                    SpacePrimitive f1 = faces.getEntity( f1idx );
                    SpacePrimitive f2 = faces.getEntity( f2idx );
                    // color hack
                    if ( f1.getX() != f2.getX() ) {
                        if ( faceSize[ f1idx ] != -1 ) markedCount++;
                        if ( faceSize[ f2idx ] != -1 ) markedCount++;
                        faceSize[ f1idx ] = -1;
                        faceSize[ f2idx ] = -1;
                        colorBoundaryFaceCount++;
                    }
  //              } else {
    //                System.out.println( "Edge " + i + " faces are " + edges.getValue( edge, SpacePrimitive.Face, 0 ) + " and " + edges.getValue( edge, SpacePrimitive.Face, 1 ));
                }
            }
//            System.out.println( "Boundary face count " + boundaryFaceCount );
//            System.out.println( "Color boundary face count " + colorBoundaryFaceCount );

            // set the location field of each face based on surrounding vertices
            // have to do this here, due to color hack
    	    setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
//            while ( markedCount < numberToMark ) {
//	            initMaxes();
//	            boolean markedOne = false;
//	            for ( int i = 0; i < faceCount; i++ ) {
//	                markMax( faceSize[i] );
//	            }
//	            for ( int i = 0; i < faceCount; i++ ) {
//	                if ( faceSize[i] != -1 ) {
//	                    if ( faceSize[i] >= max3 ) {
//	                        faceSize[i] = -1;
//	                        markedCount++;
//	                        markedOne = true;
//	                    }
//	                }
//	            }
//	            if ( !markedOne ) {
//	                break;
//	            }
//   	        }
            float[] sorted = new float[faceCount];
            System.arraycopy( faceSize, 0, sorted, 0, faceCount );
            try {
                QSort.sort(sorted);
            } catch ( Exception e ) {
                e.printStackTrace();
        		GlobalProgressIndicator.deactivateAlternateProgressIndicator();
                return( false );
            }
//            for ( int i = 0; i < faceCount; i++ ) {
//                System.out.println( i + ": " + sorted[i] );
//            }
//            System.out.println( "Supposed to mark " + numberToMark );
//            System.out.println( "Already marked " + markedCount );
            int remainingToMark = numberToMark - markedCount;
//            System.out.println( "Threshold is at " + (faceCount - remainingToMark - 1 ));
//            System.out.println( "value is " + sorted[ faceCount - remainingToMark - 1 ] );
            int idx = faceCount - remainingToMark - 1;
            
            // if all the faces are marked for preserving, there is nothing more to do
            if (( idx < 0 ) || ( idx >= faceCount )) {
//                System.out.println( "unexpected idx " + idx + ", faceCount " + faceCount + ", remainingToMark " + remainingToMark );
//                System.out.println( "numberToMark " + numberToMark + ", markedCount " + markedCount );
                return( false );
            }
            max3 = sorted[ faceCount - remainingToMark - 1 ];
            for ( int i = 0; i < faceCount; i++ ) {
                if ( faceSize[i] >=  max3 ) {
                    faceSize[i] = -1;
                    markedCount++;
                }
            }
   	        // now change all vertices surrounding the min face to be at center location
   	        // of the face
   	        int removedCount = 0;
   	        for ( int i = 0; i < faceCount; i++ ) {
   	            GlobalProgressIndicator.markProgress();
   	            if ( faceSize[i] != -1 ) {
       	            SpacePrimitive face = faces.getEntity( i );
   				    SpacePrimitive v1 = faces.getEntity( face, SpacePrimitive.Vertex, 0 );
    			    SpacePrimitive v2 = faces.getEntity( face, SpacePrimitive.Vertex, 1 );
	    		    SpacePrimitive v3 = faces.getEntity( face, SpacePrimitive.Vertex, 2 );
//	    		    face.setX( (v1.getX() + v2.getX() + v3.getX())/3 );
//	    		    face.setY( (v1.getY() + v2.getY() + v3.getY())/3 );
//	    		    face.setZ( (v1.getZ() + v2.getZ() + v3.getZ())/3 );
	    		    v1.setLocation( face.getX(), face.getY(), face.getZ() );
	    		    v2.setLocation( face.getX(), face.getY(), face.getZ() );
	    		    v3.setLocation( face.getX(), face.getY(), face.getZ() );
	    		    removedCount++;
	    		}
	    	}
	    	System.out.println( "Removed " + removedCount + " triangles" );
    		GlobalProgressIndicator.deactivateAlternateProgressIndicator();
	    	return( true );
	    } else {
    		GlobalProgressIndicator.deactivateAlternateProgressIndicator();
	        return( false );
	    }
	}

	//
	//  This is the algorithm for making all the vertices the same distance from the
	//  center, as well as the same distance from each other.  The way it works is to first
	//  generate the set of edges.  Then get the minimum edge length and maximum edge length.
	//  Then for each edge that is the maximum edge length, move the points closer to each
	//  other by a specific factor (for now I'll try 1/2) on the way towards the minimum
	//  edge length.  Then make all vertices the same distance from the center.
	//
	public void normalize() {
		E();
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		float minDistance = 0;
		float maxDistance = 0;
		for ( int i = 0; i < edges.getNumberEntities(); i++ ) {
			SpacePrimitive edge = edges.getEntity( i );
			SpacePrimitive v1 = edges.getEntity( edge, SpacePrimitive.Vertex, 0 );
			SpacePrimitive v2 = edges.getEntity( edge, SpacePrimitive.Vertex, 1 );
			float distance = SpaceEntitySet.distanceBetween( v1, v2 );
			if ( i == 0 ) {
				minDistance = maxDistance = distance;
			} else if ( distance < minDistance ) {
				minDistance = distance;
			} else if ( distance > maxDistance ) {
				maxDistance = distance;
			}
		}
		float factor = (float)0.5;
		float diff = ( maxDistance - minDistance ) /2;
		for ( int i = 0; i < edges.getNumberEntities(); i++ ) {
			SpacePrimitive edge = edges.getEntity( i );
			SpacePrimitive v1 = edges.getEntity( edge, SpacePrimitive.Vertex, 0 );
			SpacePrimitive v2 = edges.getEntity( edge, SpacePrimitive.Vertex, 1 );
			float distance = SpaceEntitySet.distanceBetween( v1, v2 );
			if ( distance == maxDistance ) {
				float nv1x = v1.getX() + ( v2.getX() - v1.getX() ) * diff * factor;
				float nv1y = v1.getY() + ( v2.getY() - v1.getY() ) * diff * factor;
				float nv1z = v1.getZ() + ( v2.getZ() - v1.getZ() ) * diff * factor;
				float nv2x = v2.getX() + ( v1.getX() - v2.getX() ) * diff * factor;
				float nv2y = v2.getY() + ( v1.getY() - v2.getY() ) * diff * factor;
				float nv2z = v2.getZ() + ( v1.getZ() - v2.getZ() ) * diff * factor;
				v1.setLocation( nv1x, nv1y, nv1z );
				v2.setLocation( nv2x, nv2y, nv2z );
			}
		}
	}


	//
	// Structural operations which generate new structures.  Below are the 7 basic stellation
	// operations.  These (via the dual) can be used to generate the 7 basic truncation
	// operations as well.
	//
	//    S1:  identity
	//    S2:  dual, vertices become faces
	//    S3:  edges become faces
	//    S1,2
	//    S1,3
	//    S2,3
	//    S1,2,3
	//

	public void s1() {
	}

	public void t1() {
		s2();
	}

	public void s2() {
		// information we need before we can proceed
		V();
		F();
		r1();
		r2();

		// now exchange vertices and faces
		SpaceEntitySet originalFaces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		vertices.setCenterLocation();
		originalFaces.FtoV( vertices.getX(), vertices.getY(), vertices.getZ(), vertices.distanceTo( 0 ));
		vertices.VtoF();
		invalidate( SpacePrimitive.Edge );
	}

	public void t2() {
	}

	public void s3() {
		SpaceEntitySet originalFaces = getEntitySet( SpacePrimitive.Face );
		V();
		E();
		F();
		originalFaces.dump( "Faces: before r4" );
		r4();
		originalFaces.dump( "Faces: after r4" );
		r6();
		originalFaces.dump( "Faces: after r6" );
		originalFaces = getEntitySet( SpacePrimitive.Face );
		originalFaces.dump( "Faces: before appendSet" );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		int originalVertexCount = vertices.getNumberEntities();
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		vertices.setCenterLocation();
		originalFaces.FtoV();
		originalFaces.invalidate( SpacePrimitive.Edge );
		vertices.dump( "Vertices: before appendSet" );
		edges.dump( "Edges: before appendSet" );
		vertices.appendSet( originalFaces );
		vertices.invalidate( SpacePrimitive.Edge );
		vertices.invalidate( SpacePrimitive.Face );
		vertices.dump( "vertices: After FtoV and append" );
		createSet( originalFaces, SpacePrimitive.Edge );
		edges.dump( "These are edges before EtoF" );
		edges.EtoF( originalVertexCount, vertices.getX(), vertices.getY(), vertices.getZ() );
		SpaceEntitySet newFaces = getEntitySet( SpacePrimitive.Face );
		newFaces.dump( "These are the new faces generated from edges" );
		invalidate( SpacePrimitive.Edge );
	}

	public void t3() {
		s3();
		s2();
	}

	public void s12() {
		t12();
		s2();
	}

	public void t12() {
		V();
		E();
		F();
		r2();
		float factor = (float)((float)2/(float)3);
		shrinkFaces( factor );
		addVfaces();
		addEfaces();
		invalidate( SpacePrimitive.Edge );
	}

	public void s13() {
		SpaceEntitySet additionalVertices = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Vertex ), this );
		setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
		SpaceEntitySet originalFaces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		int originalVertexCount = vertices.getNumberEntities();
		vertices.setCenterLocation();
		additionalVertices.setLocation( vertices.getX(), vertices.getY(), vertices.getZ() );
		for ( int i = 0; i < originalFaces.getNumberEntities(); i++ ) {
			SpacePrimitive face = originalFaces.getEntity( i );
			SpacePrimitive v = new SpacePrimitive( face.getX(), face.getY(), face.getZ() );
			additionalVertices.addEntity( v );
			float nvDistance = 0;
			for ( int j = 0; j < originalFaces.getCount( face, SpacePrimitive.Vertex ); j++ ) {
				SpacePrimitive nv = originalFaces.getEntity( face, SpacePrimitive.Vertex, j );
				nvDistance += SpaceEntitySet.distanceBetween( vertices.getX(), vertices.getY(),
					vertices.getZ(), nv.getX(), nv.getY(), nv.getZ() );
			}
			// new vertice is same distance from center as surrounding old vertices
			nvDistance = nvDistance/originalFaces.getCount( face, SpacePrimitive.Vertex );
			additionalVertices.setDistance( v, nvDistance );
		}
		SpaceEntitySet newFaces = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Face ), this );
		newFaces.validate( SpacePrimitive.Vertex );
		int[] offsets = new int[200];
		int originalNumberFaces = originalFaces.getNumberEntities();
		for ( int i = 0; i < originalNumberFaces; i++ ) {
			SpacePrimitive face = originalFaces.getEntity( i );
			int offsetIdx = originalFaces.getCount( face, SpacePrimitive.Vertex );
			for ( int j = 0; j < offsetIdx; j++ ) {
				offsets[0] = originalFaces.getValue( face, SpacePrimitive.Vertex, j );
				if (( j + 1 ) < offsetIdx ) {
					offsets[1] = originalFaces.getValue( face, SpacePrimitive.Vertex, j + 1 );
				} else {
					offsets[1] = originalFaces.getValue( face, SpacePrimitive.Vertex, 0 );
				}
				offsets[2] = originalVertexCount + i;
				newFaces.addEntity( new SpacePrimitive( offsets, 3 ));
			}
		}
		vertices.appendSet( additionalVertices );
		replaceSet( SpacePrimitive.Face, newFaces );
		invalidate( SpacePrimitive.Edge );
	}

	public void t13() {
		s13();
		s2();
	}

	public void s23() {
		// note this beginning section is identical to s13, additional vertices are identical
		SpaceEntitySet additionalVertices = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Vertex ), this );
		setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
		SpaceEntitySet originalFaces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		int originalVertexCount = vertices.getNumberEntities();
		vertices.setCenterLocation();
		additionalVertices.setLocation( vertices.getX(), vertices.getY(), vertices.getZ() );
		for ( int i = 0; i < originalFaces.getNumberEntities(); i++ ) {
			SpacePrimitive face = originalFaces.getEntity( i );
			SpacePrimitive v = new SpacePrimitive( face.getX(), face.getY(), face.getZ() );
			additionalVertices.addEntity( v );

			// new!! (old distance was too far from center, not sure if this is general case
			// solution)
//			float originalDistance = vertices.distanceTo( face.getX(), face.getY(), face.getZ() );
			float nvDistance = 0;
			for ( int j = 0; j < originalFaces.getCount( face, SpacePrimitive.Vertex ); j++ ) {
				SpacePrimitive nv = originalFaces.getEntity( face, SpacePrimitive.Vertex, j );
				nvDistance += SpaceEntitySet.distanceBetween( vertices.getX(), vertices.getY(),
					vertices.getZ(), nv.getX(), nv.getY(), nv.getZ() );
			}
			// new vertex is same distance from center as surrounding old vertices
			nvDistance = nvDistance/originalFaces.getCount( face, SpacePrimitive.Vertex );

			// new!!
//			nvDistance = ( nvDistance + originalDistance ) / 2;
			additionalVertices.setDistance( v, nvDistance );
		}
		SpaceEntitySet newFaces = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Face ), this );
		newFaces.validate( SpacePrimitive.Vertex );
		int[] offsets = new int[200];
		for ( int i = 0; i < originalFaces.getNumberEntities(); i++ ) {
			SpacePrimitive face = originalFaces.getEntity( i );
			int offsetIdx = originalFaces.getCount( face, SpacePrimitive.Vertex );
			for ( int j = 0; j < offsetIdx; j++ ) {
				int edgeV1 = originalFaces.getValue( face, SpacePrimitive.Vertex, j );
				int edgeV2 = originalFaces.getValue( face, SpacePrimitive.Vertex, 0 );
				if ( j != ( offsetIdx - 1 )) {
					edgeV2 = originalFaces.getValue( face, SpacePrimitive.Vertex, j + 1 );
				}
				int oppositeFaceIdx = originalFaces.getAnotherIdx( face, SpacePrimitive.Vertex,
					edgeV1, edgeV2 );
				offsets[0] = originalVertexCount + i;
				offsets[1] = edgeV1;
				offsets[2] = originalVertexCount + oppositeFaceIdx;
				newFaces.addEntity( new SpacePrimitive( offsets, 3 ));
				offsets[0] = originalVertexCount + i;
				offsets[1] = originalVertexCount + oppositeFaceIdx;
				offsets[2] = edgeV2;
			}
		}
		appendSet( SpacePrimitive.Vertex, additionalVertices );
		replaceSet( SpacePrimitive.Face, newFaces );
		invalidate( SpacePrimitive.Edge );
	}

	public void t23() {
		s23();
		s2();
	}

	public void s123() {
		s23();
		SpaceEntitySet additionalVertices = getAppendedSet( SpacePrimitive.Vertex );
		E();

		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		System.out.println( "After s23, vertex count is " + vertices.getNumberEntities() );
		vertices.setCenterLocation();
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		SpaceEntitySet newVertices = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Vertex ), this );
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		int originalFaceCount = faces.getNumberEntities();
		for ( int i = 0; i < originalFaceCount; i++ ) {
			SpacePrimitive face = faces.getEntity( i );
			int rv1 = -1;
			int rv2 = -1;
			for ( int j = 0; j < faces.getCount( face, SpacePrimitive.Vertex ); j++ ) {
				int val = faces.getValue( face, SpacePrimitive.Vertex, j );
				if ( val >= preAppendCount ) {
					if ( rv1 == -1 ) {
						rv1 = val;
					} else if ( rv2 == -1 ) {
						rv2 = val;
						SpacePrimitive edgeV1 = vertices.getEntity( rv1 );
						SpacePrimitive edgeV2 = vertices.getEntity( rv2 );
						SpacePrimitive newVertex = new SpacePrimitive(
							( edgeV1.getX() + edgeV2.getX() )/2,
							( edgeV1.getY() + edgeV2.getY() )/2,
							( edgeV1.getZ() + edgeV2.getZ() )/2 );
						float newDistance = vertices.distanceTo(
							newVertex.getX(), newVertex.getY(),	newVertex.getZ() );
						newDistance += vertices.distanceTo(
							edgeV1.getX(), edgeV1.getY(), edgeV1.getZ() );
						newDistance = newDistance/2;
//						float newDistance = vertices.distanceTo(
//							edgeV1.getX(), edgeV1.getY(), edgeV1.getZ() );
						vertices.setDistance( newVertex, newDistance );
						int newVertexOffset = vertices.getEntityOffsetWithLocation(
							newVertex.getX(), newVertex.getY(), newVertex.getZ() );
						if ( newVertexOffset == -1 ) {
							newVertexOffset = vertices.getNumberEntities();
							vertices.addEntity( newVertex );
						}
						faces.insert( face, SpacePrimitive.Vertex, rv1, rv2, newVertexOffset );
					}
				}
			}
		}

		invalidate( SpacePrimitive.Edge );
	}

	public void t123() {
		s123();
		s2();
	}

	//
	//  The SpaceStructure deals with 3 sets of information, and 6 relationships.
	//
	//  The 3 sets of information are vertices (V), edges (E), and faces (F).
	//
	//  The relationship r1 is a set of faces defined by sets of vertices.
	//  r1 is the form defined in IndexedFaceSets.
	//
	//  r2 is a transformation of r1, where each face turns into a vertex, and each
	//  vertex turns into a face.  The dual of r1.
	//
	//
	//
	//     r1. F ---* V    F,r1        **** THIS ALWAYS EXISTS ****
	//     r2. V ---* F    V,r2: F,r3
	//     r3. F ---* E    F,r3: E,F,r1
	//     r4. E ---2 F    E,r4
	//     r5. V ---* E    V,r5
	//     r6. E ---2 V    E,r6
	//
	//  The set V must always exist.  The set E can be derived from r1, which we get when
	//  and IndexedFaceSet is loaded.  The set F must always exist.
	//
	//  For now, it seems r1 must always exist, though it seems that some operations may
	//  establish relationships from which it can be derived.
	//
	//  r2 can be derived from r3, and r3 can be derived from r1.
	//

	void V() {
		// V must exist, get traceback if it doesn't
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		if ( vertices.getNumberEntities() == 0 ) {
			getTraceback();
		}
	}

	void E() {
		// E can be derived from F
		F();
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		if ( edges.getNumberEntities() == 0 ) {
			generateEdges();
		}
	}

	void F() {
		// F must exist, get traceback if it doesn't
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		if ( faces.getNumberEntities() == 0 ) {
			getTraceback();
		}
	}

	//
	//     r1. F ---* V    F,r1
	//     r2. V ---* F    V,r2: F,r3
	//     r3. F ---* E    F,r3: E, F,r1
	//     r4. E ---2 F    E,r4
	//     r5. V ---* E    V,r5: r2, r3
	//     r6. E ---2 V    E,r6
	//

	/** Relationship #1:  F --* V
	 *
	 *  Exists when structure is first loaded.
	 */
	void r1() {
		// assume it exists
	}

	/** RelationShip #2:  V --* F
	 *
	 *  Can be derived from r1
	 *  This method however derivew from r3.
	 */
	void r2() {
		//
		// Check if it exists
		//
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		SpacePrimitive sp = vertices.getEntity( 0 );
		if ( vertices.getCount( sp, SpacePrimitive.Face ) == 0 ) {
			// relation doesn't exist, must derive from r3: F ---* E
			r3();

			//  Have to get the ccw ordering of faces around each vertex
			//  This is not done yet...there has to be a better way to do this..
			//
			//  1. Get a set of edges that include the vertex
			//  2. Get a set of faces that include the vertex
			//  3. Use the ccw ordering of the edges on the faces to determine the ccw
			//     ordering of the faces relative to the vertex
			//
			SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
			SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
			vertices.validate( SpacePrimitive.Face );
//System.out.println( "r2: vloop " + vertices.getNumberEntities() );
            int numberVertices = vertices.getNumberEntities();
			for ( int i = 0; i < numberVertices; i++ ) {
				// 1. get a set of edges that include the vertex
				Vector edgesIncludingV = new Vector();
				int numberEdges = edges.getNumberEntities();
				for ( int j = 0; j < numberEdges; j++ ) {
					SpacePrimitive spe = edges.getEntity( j );
					if ( edges.includes( spe, SpacePrimitive.Vertex, i )) {
						edgesIncludingV.addElement( spe );
					}
				}

				// 2. get a set of faces that include the vertex
				Vector facesIncludingV = new Vector();
				int numberFaces = faces.getNumberEntities();
				for ( int j = 0; j < numberFaces; j++ ) {
					SpacePrimitive spf = faces.getEntity( j );
					if ( faces.includes( spf, SpacePrimitive.Vertex, i )) {
						facesIncludingV.addElement( spf );
					}
				}

				// 3. Use the ccw ordering of the edges on the faces to determine the ccw
				//    ordering of the faces relative to the vertex.
				//       F1: e1,e2
				//       F2: e2,e3
				//       F3: e3,e4, etc.
				SpacePrimitive v = vertices.getEntity( i );
				v.setCapacity( facesIncludingV.size() );
				int count = 0;
				SpacePrimitive prevFace = (SpacePrimitive)facesIncludingV.elementAt( 0 );
				facesIncludingV.removeElement( prevFace );
				//
				//  add a face to the V set
				//
				count = facesIncludingV.size();
				v.setValue( count, faces.getEntityOffset( prevFace ));
				count--;
				while ( facesIncludingV.size() > 0 ) {

					//
					//  get prev face e1,e2
					//
					SpacePrimitive prevFaceE1 = null;
					SpacePrimitive prevFaceE2 = null;
					boolean prevFaceE1first = false;
					boolean prevFaceE2last = false;
					for ( int j = 0; j < faces.getCount( prevFace, SpacePrimitive.Edge ); j++ ) {
						SpacePrimitive testEdge = faces.getEntity( prevFace, SpacePrimitive.Edge, j );
						if ( edgesIncludingV.indexOf( testEdge ) >= 0 ) {
							if ( prevFaceE1 == null ) {
								prevFaceE1 = testEdge;
								if ( j == 0 ) {
									prevFaceE1first = true;
								}
							} else {
								prevFaceE2 = testEdge;
								if ( j == ( faces.getCount( prevFace, SpacePrimitive.Edge ) - 1 )) {
									prevFaceE2last = true;
								}
								break;
							}
						}
					}
					if ( prevFaceE1first && prevFaceE2last ) {
						SpacePrimitive swapper = prevFaceE1;
						prevFaceE1 = prevFaceE2;
						prevFaceE2 = swapper;
					}

					//
					//  find next face that has e2
					//
					boolean foundFace = false;
					for ( int j = 0; j < facesIncludingV.size(); j++ ) {
						SpacePrimitive face = (SpacePrimitive)facesIncludingV.elementAt( j );
						for ( int k = 0; k < faces.getCount( face, SpacePrimitive.Edge ); k++ ) {
							SpacePrimitive testEdge = faces.getEntity( face, SpacePrimitive.Edge, k );
							if ( testEdge == prevFaceE2 ) {
								facesIncludingV.removeElement( face );
								prevFace = face;
								foundFace = true;
								break;
							}
						}
						if ( foundFace ) {
							//
							//  add a face to the V set
							//
							v.setValue( count, faces.getEntityOffset( prevFace ));
							count--;
							break;
						}
					}
					if ( !foundFace ) {
						System.out.println( "Didn't find face" );
						break;
					}
				}
			}
//			System.out.println( "Done with ordering faces" );
//			vertices.dump( "These should have ccw face references" );
		}
	}


	//
	//  r3:  F ---* E: E,F,r1
	//
	//  r1 + .5 units
	void r3() {
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		E();

		//
		// Check if it exists
		//
		SpacePrimitive sp = faces.getEntity( 0 );
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		vertices.dump( "These are vertices" );
		if ( faces.getCount( sp, SpacePrimitive.Edge ) == 0 ) {
			r1();

			// The edge set exists, but not in the faces
			boolean doubleit = false;
			if ( !faces.entryExistsFor( SpacePrimitive.Edge )) {
				doubleit = true;
			}
			faces.validate( SpacePrimitive.Edge );
			int numberFaces = faces.getNumberEntities();
			GlobalProgressIndicator.replaceHalfUnit( numberFaces );
			for ( int i = 0; i < numberFaces; i++ ) {
			    GlobalProgressIndicator.markProgress();
//			    if (( i % 100 ) == 0 ) System.out.println( "r3 on face " + i + " of " + numberFaces );
				SpacePrimitive face = faces.getEntity( i );

				// doubleCapacity because we've already told the strategy the edges exist
				// and are valid
				if ( doubleit ) {
					face.doubleCapacity( SpacePrimitive.Edge );
//					System.out.println( "Double capacity" );
				}
				int count = faces.getCount( face, SpacePrimitive.Vertex );
//				faces.dump( face, "This is face I'm checking for..." );
				for ( int j = 0; j < count; j++ ) {
					int v1 = faces.getValue( face, SpacePrimitive.Vertex, j );
					int v2 = 0;
					if ( j == ( count - 1 )) {
						v2 = faces.getValue( face, SpacePrimitive.Vertex, 0 );
					} else {
						v2 = faces.getValue( face, SpacePrimitive.Vertex, j+1 );
					}
//					System.out.println( "v1 & v2 are " + v1 + ", and " + v2 );
//					edges.dump ( "These are edges..." );
					int edgeOffset = edges.getEntityOffset( v1, v2, SpacePrimitive.Vertex );
					if ( SpaceEntitySet.debug ) {
						System.out.println( "face edge " + j + " is " + v1 + " to " + v2 + " at offset " + edgeOffset );
					}
					faces.setValue( face, SpacePrimitive.Edge, j, edgeOffset );
				}
			}
		}
	}


	/** Calculate relationship #4, faces around each edge
	 *
	 *     r4. E ---2 F    E,r3
	 */
	void r4() {
		r3();

		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		if ( SpaceEntitySet.debug ) {
    		edges.dump( "r4 edges..." );
    	}
		SpacePrimitive firstEdge = edges.getEntity( 0 );

		// if the edge has no associated faces, calculate that here
		if ( edges.getCount( firstEdge, SpacePrimitive.Face ) == 0 ) {
			SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
			int numberFaces = faces.getNumberEntities();
			if ( SpaceEntitySet.debug ) {
		        faces.dump( "r4 faces..." );
			}
			// make room for face info in each edge
			edges.validate( SpacePrimitive.Face );
			edges.doubleCapacity( SpacePrimitive.Face );
			GlobalProgressIndicator.replaceHalfUnit( numberFaces );
			for ( int i = 0; i < numberFaces; i++ ) {
			    GlobalProgressIndicator.markProgress();
			    SpacePrimitive face = faces.getEntity( i );
			    int ecount = faces.getCount( face, SpacePrimitive.Edge );
			    for ( int j = 0; j < ecount; j++ ) {
			        int edgeVal = faces.getValue( face, SpacePrimitive.Edge, j );
			        SpacePrimitive edge = edges.getEntity( edgeVal );
			        edges.setValue( edge, SpacePrimitive.Face, i );
			    }
			}
			edges.dump( "This is the final set" );
		}
	}

	//
	//     r5. V ---* E    V,r5
	//
	//  Note:  This currently does not order edges correctly around vertex,
	//    just creates the correct collection.
	//
	//  2.5 units
	//
	void r5() {
	    E();
        SpaceEntitySet v = getEntitySet( SpacePrimitive.Vertex );
	    SpaceEntitySet e = getEntitySet( SpacePrimitive.Edge );

	    // get the necessary capacity for each vertex (note this assumes none)
	    int numberVertices = v.getNumberEntities();
	    int[] vCapacity = new int[ numberVertices ];
	    int numberEdges = e.getNumberEntities();
//	    System.out.println( "updating " + numberEdges + " edges" );
        GlobalProgressIndicator.replaceUnit( numberEdges );
	    for ( int i = 0; i < numberEdges; i++ ) {
	        GlobalProgressIndicator.markProgress();
	        SpacePrimitive edge = e.getEntity( i );
	        int vCount = e.getCount( edge, SpacePrimitive.Vertex );
//	        if (( i % 100 ) == 0 ) {
//	            System.out.println( "set V capacity for edge " + i + " of " + numberEdges );
//	        }
	        for ( int j = 0; j < vCount; j++ ) {
	            int vertex = e.getValue( edge, SpacePrimitive.Vertex, j );
	            vCapacity[ vertex ] += 1;
	        }
	    }

	    v.validate( SpacePrimitive.Edge );
	    GlobalProgressIndicator.replaceHalfUnit( numberVertices );
	    for ( int i = 0; i < numberVertices; i++ ) {
	        GlobalProgressIndicator.markProgress();
	        SpacePrimitive vertex = v.getEntity( i );
	        vertex.setCapacity( vCapacity[ i ] );
	    }

        GlobalProgressIndicator.replaceUnit( numberEdges );
	    for ( int i = 0; i < numberEdges; i++ ) {
	        GlobalProgressIndicator.markProgress();
	        SpacePrimitive edge = e.getEntity( i );
	        int vCount = e.getCount( edge, SpacePrimitive.Vertex );
//	        if (( i % 100 ) == 0 ) {
//	            System.out.println( "update vertices for edge " + i + " of " + numberEdges );
//	        }
	        for ( int j = 0; j < vCount; j++ ) {
	            int vidx = e.getValue( edge, SpacePrimitive.Vertex, j );
	            SpacePrimitive vertex = v.getEntity( vidx );
	            vertex.appendValue( i );
	        }
	    }
	}


	//
	//     r6. E ---2 V    E,r6
	//
	void r6() {
	}

    void showNonRemoval( int numberEdges, BitSet edgeNonRemovalList ) {
       int nonRemovalCount = 0;
       for ( int i = 0; i < numberEdges; i++ ) {
           if ( edgeNonRemovalList.get( i )) {
               nonRemovalCount++;
           }
       }
       System.out.println( nonRemovalCount + " of " + numberEdges + " edges cannot be removed" );
   }

   String name = null;
   public void setName( String nm ) {
    if ( nm == null ) {
        name = Integer.toString( id );
    } else {
        name = nm;
    }
   }


    /** Create F-*E relationship, must not exist when this is called.
     *
     *  @param edges the SpaceEntitySet containing edges
     *  @param faces the SpaceEntitySet containing faces
     *  @param numberFaces the number of faces
     */
    void createFtoE( SpaceEntitySet edges, SpaceEntitySet faces, int numberFaces ) {
   		edges.doubleCapacity( SpacePrimitive.Face );
   		for ( int i = 0; i < numberFaces; i++ ) {
   		    SpacePrimitive face = faces.getEntity( i );
   		    int eCount = faces.getCount( face, SpacePrimitive.Edge );
   		    for ( int j = 0; j < eCount; j++ ) {
   		        int eVal = faces.getValue( face, SpacePrimitive.Edge, j );
   		        SpacePrimitive edge = edges.getEntity( eVal );
  		        edge.appendValue( i );
   		    }
   		}
   	}

   /**  Edge removal polygon reduction algorithm.
    *
    *  @param  preserveColorBoundaries  if true, edges that fall on a color boundary are not affected
    *  @param  percent  percent of edge removal candidates to attempt removing
    *  @param  b  the bounding box of the IndexedFaceSet
    *  @param  boundingBoxOverlap  Hashtable of overlapping bounding boxes
    *  @param  useBoundingBoxLimit controls whether bounding box limit is used or not
    */
   public void edgeRemovalPolygonReduction( boolean preserveColorBoundaries, int xFactor, int yFactor, int zFactor, int percent, int minEdgeCount, BoundingBox b, Hashtable boundingBoxOverlap, boolean useBoundingBoxLimit, int ifsNumber ) {
        SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
        int numberVertices = vertices.getNumberEntities();
		GlobalProgressIndicator.activateAlternateProgressIndicator( 0 );
		GlobalProgressIndicator.setUnitSize( numberVertices*2, 10 );
        System.out.println( name + ", Phase 1:  generate relationships between V/E/F" );
        r5();
        r3();

        // At this point, we have all the edges surrounding each vertex
        // Create the initial edgeNonRemoval list
        SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
        int numberEdges = edges.getNumberEntities();
        SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
        int numberFaces = faces.getNumberEntities();
        if ( numberEdges > minEdgeCount ) {
       		// At this point we have the following relationships known:
       		//    E -* 2V
       		//    V -* E
       		//    F -* V
       		//    F -* E
       		//
       		// We need:   E-*2F
       		//
       		// So we use F-*E to make this.
       		//
       		createFtoE( edges, faces, numberFaces );

            System.out.println( name + ", Phase 2: check bounding box overlap" );
            BitSet edgeNonRemovalList = new BitSet( numberEdges );
            if ( useBoundingBoxLimit ) {
                Vector overlap = (Vector)boundingBoxOverlap.get( b );
                int overlapSize = overlap.size();
                if ( overlapSize > 0 ) {
                    for ( int i = 0; i < overlapSize; i++ ) {
                        BoundingBox o = (BoundingBox)overlap.elementAt( i );
                        for ( int j = 0; j < numberVertices; j++ ) {
                            SpacePrimitive vertex = vertices.getEntity( j );
                            if ( o.contains( vertex.getX(), vertex.getY(), vertex.getZ() )) {
                                int vEdges = vertices.getCount( vertex, SpacePrimitive.Edge );
                                for ( int k = 0; k < vEdges; k++ ) {
                                    int edge = vertices.getValue( vertex, SpacePrimitive.Edge, k );
                                    edgeNonRemovalList.set( edge );
                                }
                            }
                        }
                    }
                } else {
                    System.out.println( "No bounding box overlap" );
                }
                // debug only
                showNonRemoval( numberEdges, edgeNonRemovalList );
            }

            System.out.println( name + ", Phase 3, check edges bounding non-triangular faces" );

            // mark edges that can't be removed because of non triangular faces
            GlobalProgressIndicator.replaceHalfUnit( numberFaces );
            for ( int i = 0; i < numberFaces; i++ ) {
                GlobalProgressIndicator.markProgress();
                SpacePrimitive face = faces.getEntity( i );
                int eCount = faces.getCount( face, SpacePrimitive.Edge );
                if ( eCount != 3 ) {
                    for ( int j = 0; j < eCount; j++ ) {
                        int eVal = faces.getValue( face, SpacePrimitive.Edge, j );
                        edgeNonRemovalList.set( eVal );
                    }
                }
            }

            // debug only
            showNonRemoval( numberEdges, edgeNonRemovalList );
            System.out.println( name+ ", Phase 4 , check edges bounding only one face" );
            GlobalProgressIndicator.replaceUnit( numberEdges );
            for ( int i = 0; i < numberEdges; i++ ) {
                GlobalProgressIndicator.markProgress();
                SpacePrimitive edge = edges.getEntity( i );
                if ( edges.getValue( edge, SpacePrimitive.Face, 1 ) == -1 ) {
                    edgeNonRemovalList.set( i );
                } else if ( preserveColorBoundaries ) {
                    int f1idx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                    int f2idx = edges.getValue( edge, SpacePrimitive.Face, 1 );
                    SpacePrimitive f1 = faces.getEntity( f1idx );
                    SpacePrimitive f2 = faces.getEntity( f2idx );
                    if ( f1.getX() != f2.getX() ) edgeNonRemovalList.set( i );
                }
            }

           showNonRemoval( numberEdges, edgeNonRemovalList );
           System.out.println( name+ ", Phase 5, create final edge non removal list." );

            BitSet finalEdgeNonRemovalList = new BitSet( numberEdges );
            GlobalProgressIndicator.replaceHalfUnit( numberFaces );
            for ( int i = 0; i < numberFaces; i++ ) {
                GlobalProgressIndicator.markProgress();
                SpacePrimitive face = faces.getEntity( i );
                int eCount = faces.getCount( face, SpacePrimitive.Edge );
                for ( int j = 0; j < eCount; j++ ) {
                    int eVal = faces.getValue( face, SpacePrimitive.Edge, j );
                    if ( edgeNonRemovalList.get( eVal )) {
                        for ( int k = 0; k < eCount; k++ ) {
                            int eVal2 = faces.getValue( face, SpacePrimitive.Edge, k );
                            finalEdgeNonRemovalList.set( eVal2 );
                        }
                        break;
                    }
                }
            }

            // debug only
            showNonRemoval( numberEdges, finalEdgeNonRemovalList );
            System.out.println( name+ ", Phase 6, create edge removal candidate list" );

            // Phase 6:  invert finalEdgeNonRemovalList to
            BitSet edgeRemovalCandidate = new BitSet( numberEdges );
            GlobalProgressIndicator.replaceUnit( numberEdges );
            for ( int i = 0; i < numberEdges; i++ ) {
                GlobalProgressIndicator.markProgress();
                if ( !finalEdgeNonRemovalList.get( i )) {
                    edgeRemovalCandidate.set( i );
                }
            }

            // debug only
            int removalCandidateCount = 0;
            GlobalProgressIndicator.replaceUnit( numberEdges );
            for ( int i = 0; i < numberEdges; i++ ) {
                GlobalProgressIndicator.markProgress();
                if ( edgeRemovalCandidate.get( i )) {
                    removalCandidateCount++;
                }
            }
            System.out.println( "After phase 6, removalCandidateCount is " + removalCandidateCount );

            if ( removalCandidateCount > 1 ) {
                if (( xFactor > 1 ) || ( yFactor > 1 ) || ( zFactor > 1 )) {
                    for ( int i = 0; i < numberVertices; i++ ) {
                        SpacePrimitive v = vertices.getEntity( i );
                        v.multiply( xFactor, yFactor, zFactor );
                    }
                }
                // Phase 7: get edge lengths for each edge
                float[] edgeLengths = new float[ numberEdges ];
                GlobalProgressIndicator.replaceUnit( numberEdges );
                for ( int i = 0; i < numberEdges; i++ ) {
                    GlobalProgressIndicator.markProgress();
                    if ( edgeRemovalCandidate.get( i )) {
                        SpacePrimitive edge = edges.getEntity( i );
                        int v1 = edges.getValue( edge, SpacePrimitive.Vertex, 0 );
                        int v2 = edges.getValue( edge, SpacePrimitive.Vertex, 1 );
                        SpacePrimitive vertex1 = vertices.getEntity( v1 );
                        SpacePrimitive vertex2 = vertices.getEntity( v2 );
                        if ( xFactor > 1 ) {
                            vertex1.setX( vertex1.getX() * xFactor );
                            vertex2.setX( vertex2.getX() * xFactor );
                        }
                        if ( yFactor > 1 ) {
                            vertex1.setY( vertex1.getY() * yFactor );
                            vertex2.setY( vertex2.getY() * yFactor );
                        }
                        if ( zFactor > 1 ) {
                            vertex1.setZ( vertex1.getZ() * zFactor );
                            vertex2.setZ( vertex2.getZ() * zFactor );
                        }
                        edgeLengths[i] = SpaceEntitySet.distanceBetween( vertex1, vertex2 );
                        if ( xFactor > 1 ) {
                            vertex1.setX( vertex1.getX()/xFactor );
                            vertex2.setX( vertex2.getX()/xFactor );
                        }
                        if ( yFactor > 1 ) {
                            vertex1.setY( vertex1.getY()/yFactor );
                            vertex2.setY( vertex2.getY()/yFactor );
                        }
                        if ( zFactor > 1 ) {
                            vertex1.setZ( vertex1.getZ()/zFactor );
                            vertex2.setZ( vertex2.getZ()/zFactor );
                        }
                    }
                }

                // debug
 //               for ( int i = 0; i < numberEdges; i++ ) {
//                    System.out.println( "Edge " + i + " length is " + edgeLengths[i] );
//                }

                // Phase 8:  Order edges by length
                int[] orderedList = new int[ removalCandidateCount ];
                int[] tmpOrderedList = new int[ removalCandidateCount ];
                int orderedListIdx = 0;
                GlobalProgressIndicator.replaceUnit( numberEdges );
                for ( int i = 0; i < numberEdges; i++ ) {
                    GlobalProgressIndicator.markProgress();
                    if ( edgeLengths[i] != 0 ) {
                        orderedList[ orderedListIdx++ ] = i;
                    }
                }

                sort( 0, removalCandidateCount - 1, orderedList, tmpOrderedList, edgeLengths );

                // debug
//                for ( int i = 0; i < removalCandidateCount; i++ ) {
//                    System.out.println( "edge length " + i + " is " + edgeLengths[ orderedList[i] ] + " at " + orderedList[i] );
//                }

                // Phase 9:  select actual edge removal candicates from percent
                int pCount = ( removalCandidateCount * percent )/100;
                BitSet removalList = new BitSet( numberEdges );
                for ( int i = 0; i < pCount; i++ ) {
                    removalList.set( orderedList[i] );
                }


                // Phase 10:  remove edges
           		faceBitCount = numberFaces;
        		faceRemovalCandidateSet = new BitSet( faceBitCount );
        		normalMergeSet = new BitSet( numberEdges );

        		int actualRemovedCount = 0;

//SpaceEntitySet.debug = true;

//vertices.dump( "Here are vertices" );
//edges.dump( "Here are edges" );
//faces.dump( "Here are faces" );

                for ( int i = 0; i < pCount; i++ ) {
                    GlobalProgressIndicator.markProgress();
                    int idx = orderedList[i];
                    if ( removalList.get( idx )) {
                        actualRemovedCount++;
                        SpacePrimitive edge = edges.getEntity( idx );
                        int v1idx = edges.getValue( edge, SpacePrimitive.Vertex, 0 );
                        int v2idx = edges.getValue( edge, SpacePrimitive.Vertex, 1 );
                        SpacePrimitive v1 = vertices.getEntity( v1idx );
                        SpacePrimitive v2 = vertices.getEntity( v2idx );
 //                       System.out.println( v1idx + " : " + v1.getX() + ", " + v1.getY() + ", " + v1.getZ() );
   //                     System.out.println( v2idx + " : " + v1.getX() + ", " + v2.getY() + ", " + v2.getZ() );
                        // set location based on number of edges around each vertex,
                        // if a vertex has more edges than another it is more important, so
                        // its location gets preserved, otherwise use averages
  //                      int v1edgeCount = vertices.getCount( v1, SpacePrimitive.Edge );
    //                    int v2edgeCount = vertices.getCount( v2, SpacePrimitive.Edge );
//                        if ( v1edgeCount == v2edgeCount ) {
                            float x = ( v1.getX() + v2.getX() )/2;
                            float y = ( v1.getY() + v2.getY() )/2;
                            float z = ( v1.getZ() + v2.getZ() )/2;
                            v1.setLocation( x, y, z );
                            v2.setLocation( x, y, z );
    //                    } else if ( v1edgeCount > v2edgeCount ) {
      //                      v2.setLocation( v1.getX(), v1.getY(), v1.getZ() );
        //                } else {
          //                  v1.setLocation( v2.getX(), v2.getY(), v2.getZ() );
            //            }
     //                   System.out.println( v1idx + " :f " + x + ", " + y + ", " + z );
       //                 System.out.println( v2idx + " :f " + x + ", " + y + ", " + z );

                        // have to check faces individually because on the
                        // boundaries of non-singly connected polyhedra, edge may
                        // only have one face
                        int f1idx = edges.getValue( edge, SpacePrimitive.Face, 0 );
                        if ( f1idx >= 0 ) {
                            SpacePrimitive f1 = faces.getEntity( f1idx );
                            int f1ecount = faces.getCount( f1, SpacePrimitive.Edge );
                            for ( int j = 0; j < f1ecount; j++ ) {
                                int eVal = faces.getValue( f1, SpacePrimitive.Edge, j );
                                removalList.clear( eVal );
                                normalMergeSet.set( eVal );
                            }
                        }
                        int f2idx = edges.getValue( edge, SpacePrimitive.Face, 1 );
                        if ( f2idx >= 0 ) {
                            SpacePrimitive f2 = faces.getEntity( f2idx );
                            int f2ecount = faces.getCount( f2, SpacePrimitive.Edge );
                            for ( int j = 0; j < f2ecount; j++ ) {
                                int eVal = faces.getValue( f2, SpacePrimitive.Edge, j );
                                removalList.clear( eVal );
                                normalMergeSet.set( eVal );
                            }
                        }

//                        System.out.println( "f1idx is " + f1idx + ", f2idx is " + f2idx );
                    }
                }
                System.out.println( "Possible removed count " + pCount + ", actual removed count " + actualRemovedCount );
                if (( xFactor > 1 ) || ( yFactor > 1 ) || ( zFactor > 1 )) {
                    for ( int i = 0; i < numberVertices; i++ ) {
                        SpacePrimitive v = vertices.getEntity( i );
                        v.divide( xFactor, yFactor, zFactor );
                    }
                }
            }
        }
        GlobalProgressIndicator.deactivateAlternateProgressIndicator();
    }


    void sort( int firstIdx, int lastIdx, int[] list, int[] tmpList, float[] edgeLengths ) {
//        System.out.println( "sort from " + firstIdx + " to " + lastIdx );
        if (( lastIdx - firstIdx ) > 10 ) {
            float middle = getMiddleValue( firstIdx, lastIdx, list, edgeLengths );
            int fscan = firstIdx;
            int lscan = lastIdx;
            for ( int i = firstIdx; i <= lastIdx; i++ ) {
                if ( edgeLengths[ list[i] ] < middle ) {
                    tmpList[ fscan++ ] = list[i];
                } else {
                    tmpList[ lscan-- ] = list[i];
                }
            }
            for ( int i = firstIdx; i <= lastIdx; i++ ) {
                list[i] = tmpList[i];
            }
            if (( fscan != firstIdx ) && ( lscan != lastIdx )) {
                sort( firstIdx, fscan - 1, list, tmpList, edgeLengths );
                sort( lscan + 1, lastIdx, list, tmpList, edgeLengths );
            }
        }
    }

    float getMiddleValue( int firstIdx, int lastIdx, int[] list, float[] edgeLengths ) {
        float minValue = edgeLengths[ list[ firstIdx ]];
        float maxValue = edgeLengths[ list[ firstIdx ]];
        for ( int i = firstIdx + 1; i <= lastIdx; i++ ) {
            if ( edgeLengths[list[i]] < minValue ) {
                minValue = edgeLengths[list[i]];
            } else if ( edgeLengths[i] > maxValue ) {
                maxValue = edgeLengths[list[i]];
            }
        }
        return( ( minValue + maxValue )/2 );
    }

	/** Generate edges from the faces, assume F exists and r1 holds. */
	void generateEdges() {
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
		edges.validate( SpacePrimitive.Vertex );
		int numberFaces = faces.getNumberEntities();
		faces.setDivisor();
		GlobalProgressIndicator.replaceHalfUnit( numberFaces );
		for ( int i = 0; i < numberFaces; i++ ) {
		    GlobalProgressIndicator.markProgress();
			SpacePrimitive face = faces.getEntity( i );
			if ( face != null ) {
				int prevVertex = -1;
				int firstVertex = -1;
				int vCount = faces.getCount( face, SpacePrimitive.Vertex );
				for ( int j = 0; j < vCount; j++ ) {
					int v = faces.getValue( face, SpacePrimitive.Vertex, j );
					if ( firstVertex == -1 ) {
						firstVertex = v;
					}
					if ( prevVertex == -1 ) {
						prevVertex = v;
					} else {
    					edges.addSpacePrimitive( prevVertex, v );
						prevVertex = v;
					}
				}
				if ( vCount > 1 ) {
					edges.addSpacePrimitive( prevVertex, firstVertex );
				}
			}
		}
		faces.unsetDivisor();
	}


	void shrinkFaces( float factor ) {
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet shrunkenFaces = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Face ), this );
		shrunkenFaces.validate( SpacePrimitive.Vertex );
		SpaceEntitySet newVertices = new SpaceEntitySet( new Strategy( this, SpacePrimitive.Vertex ), this );

		// set face locations as average of surrounding verices
		setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
		int[] voffsets = new int[ 200 ];
		int vidx = 0;
		for ( int i = 0; i < faces.getNumberEntities(); i++ ) {
			SpacePrimitive face = faces.getEntity( i );
			float faceX = face.getX();
			float faceY = face.getY();
			float faceZ = face.getZ();
			vidx = 0;
			for ( int j = 0; j < faces.getCount( face, SpacePrimitive.Vertex ); j++ ) {
				// create a new vertex as 1/3 of the way between the old one and the center
				SpacePrimitive v = faces.getEntity( face, SpacePrimitive.Vertex, j );
				float newX = v.getX() + ( faceX - v.getX() ) * factor;
				float newY = v.getY() + ( faceY - v.getY() ) * factor;
				float newZ = v.getZ() + ( faceZ - v.getZ() ) * factor;
				newVertices.addEntity( new SpacePrimitive( newX, newY, newZ ));
				voffsets[ vidx ] = newVertices.getNumberEntities() - 1;
				vidx++;
			}
			shrunkenFaces.addEntity( new SpacePrimitive( voffsets, vidx ));
		}
		replaceSet( SpacePrimitive.Face, shrunkenFaces );
		replaceSet( SpacePrimitive.Vertex, newVertices );
	}

	//
	//  works with shrinkFaces, adds faces that exist around previous vertices
	//
	void addVfaces() {
		int[] offsets = new int[200];
		SpaceEntitySet prevV = getPrevEntitySet( SpacePrimitive.Vertex );
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		for ( int i = 0; i < prevV.getNumberEntities(); i++ ) {
			SpacePrimitive v = prevV.getEntity( i );
			int maxOffset = -1;

			for ( int j = 0; j < prevV.getCount( v, SpacePrimitive.Face ); j++ ) {
				SpacePrimitive face = prevV.getEntity( v, SpacePrimitive.Face, j );
				int offset = -1;
				float minDistance = 0;
				for ( int k = 0; k < faces.getCount( face, SpacePrimitive.Vertex ); k++ ) {
					SpacePrimitive fv = faces.getEntity( face, SpacePrimitive.Vertex, k );
					if ( offset == -1 ) {
						offset = vertices.getEntityOffset( fv );
						minDistance = distanceBetween( v, fv );
					} else {
						float distance = distanceBetween( v, fv );
						if ( distance < minDistance ) {
							minDistance = distance;
							offset = vertices.getEntityOffset( fv );
						}
					}
				}
				offsets[ j ] = offset;
				maxOffset = j + 1;
			}
			faces.addEntity( new SpacePrimitive( offsets, maxOffset ));
		}
	}

	float distanceBetween( SpacePrimitive v1, SpacePrimitive v2 ) {
		return( SpaceEntitySet.distanceBetween( v1.getX(), v1.getY(), v1.getZ(),
			v2.getX(), v2.getY(), v2.getZ() ));
	}

	void addEfaces() {
		SpaceEntitySet.debug = true;
		SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
//		edges.dump( "Do these have F & V" );
		SpaceEntitySet.debug = false;
		SpaceEntitySet prevV = getPrevEntitySet( SpacePrimitive.Vertex );
		SpaceEntitySet prevF = getPrevEntitySet( SpacePrimitive.Face );
		int prevFaceCount = prevF.getNumberEntities();
		SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		int[] offsets = new int[4];
		for ( int i = 0; i < edges.getNumberEntities(); i++ ) {
			SpacePrimitive edge = edges.getEntity( i );
			int v1offset = edges.getValue( edge, SpacePrimitive.Vertex, 0 );
			int v2offset = edges.getValue( edge, SpacePrimitive.Vertex, 1 );
			SpacePrimitive newF1 = faces.getEntity( v1offset + prevFaceCount );
			SpacePrimitive newF2 = faces.getEntity( v2offset + prevFaceCount );

			// get the two pairs of points that are closest
			SpacePrimitive tv1 = null;
			SpacePrimitive tv2 = null;
			SpacePrimitive tv3 = null;
			SpacePrimitive tv4 = null;
			float tv14distance = 0;
			float tv23distance = 0;
			int tv1offset = 0;
			int tv2offset = 0;
			for ( int j = 0; j < faces.getCount( newF1, SpacePrimitive.Vertex ); j++ ) {
				SpacePrimitive f1v = faces.getEntity( newF1, SpacePrimitive.Vertex, j );
				for ( int k = 0; k < faces.getCount( newF2, SpacePrimitive.Vertex ); k++ ) {
					SpacePrimitive f2v = faces.getEntity( newF2, SpacePrimitive.Vertex, k );
					if ( tv1 == null ) {
						tv1offset = j;
						tv1 = f1v;
						tv4 = f2v;
						tv14distance = distanceBetween( tv1, tv4 );
					} else {
						float distance = distanceBetween( f1v, f2v );
						if ( distance <= tv14distance ) {
							tv2 = tv1;
							tv2offset = tv1offset;
							tv3 = tv4;
							tv23distance = tv14distance;
							tv1 = f1v;
							tv1offset = j;
							tv4 = f2v;
							tv14distance = distance;
						}
					}
				}
			}
			SpacePrimitive v1 = null;
			SpacePrimitive v2 = null;
			SpacePrimitive v3 = null;
			SpacePrimitive v4 = null;
			if ( tv1 != null && tv2 != null ) {
				if ( tv2offset == ( tv1offset + 1 )) {
					v1 = tv1;
					v2 = tv2;
					v3 = tv3;
					v4 = tv4;
				} else if (( tv2offset == 0 ) && ( tv1offset == ( faces.getCount( newF1, SpacePrimitive.Vertex ) - 1 ))) {
					v1 = tv1;
					v2 = tv2;
					v3 = tv3;
					v4 = tv4;
				} else if ( tv1offset == ( tv2offset + 1 )) {
					v1 = tv2;
					v2 = tv1;
					v3 = tv4;
					v4 = tv3;
				} else if (( tv1offset == 0 ) && ( tv2offset == ( faces.getCount( newF1, SpacePrimitive.Vertex ) - 1 ))) {
					v1 = tv2;
					v2 = tv1;
					v3 = tv4;
					v4 = tv3;
				} else {
					System.out.println( "Order is unexpected: " + tv1offset + ", " + tv2offset + ", " + faces.getCount( newF1, SpacePrimitive.Vertex ));
				}
				offsets[0] = vertices.getEntityOffset( v1 );
				offsets[1] = vertices.getEntityOffset( v4 );
				offsets[2] = vertices.getEntityOffset( v3 );
				offsets[3] = vertices.getEntityOffset( v2 );
				faces.addEntity( new SpacePrimitive( offsets, 4 ));
			} else {
				System.out.println( "Didn't find tv1 and tv2" );
			}
		}
	}


	//
	//  Generate the VRML for an IndexedFaceSet that implements the SpaceStructure
	//
/*	public void generateVrml( String outfile ) {
		generateVrmlFaceSet( outfile );
	}

	public void generateVrmlFaceSet( String outfile ) {
		try {
			FileOutputStream fo = new FileOutputStream( outfile );
			PrintStream out = new PrintStream( fo );
			generateVrmlFaceSet( out );
		} catch ( Exception e ) {
		}
	}

	public void generateVrmlFaceSet( PrintStream out ) {
		generateVrml( out, SpacePrimitive.Face );
	}

	public void generateVrmlLineSet( String outfile ) {
		try {
			FileOutputStream fo = new FileOutputStream( outfile );
			PrintStream out = new PrintStream( fo );
			generateVrmlLineSet( out );
		} catch ( Exception e ) {
		}
	}

	public void generateVrmlLineSet( PrintStream out ) {
		generateVrml( out, SpacePrimitive.Edge );
	}

	public void generateVrmlLineSetVtoF( SpaceStructure s, PrintStream out ) {
		s.setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		SpaceEntitySet faces = s.getEntitySet( SpacePrimitive.Face );
		try {
			out.println( "Transform {" );
			out.println( "\tchildren [" );
			out.println( "\t\tShape {" );
			out.println( "\t\t\tappearance Appearance {" );
			out.println( "\t\t\t\tmaterial Material {" );
			out.println( "\t\t\t\t\tdiffuseColor 0 1 1" );
			if ( transparency != DefaultTransparency ) {
				out.println( "\t\t\t\t\ttransparency " + transparency );
			}
			if (( rEmissive != DefaultEmissiveColor ) || ( gEmissive != DefaultEmissiveColor ) ||
				( bEmissive != DefaultEmissiveColor )) {
				out.println( "\t\t\t\t\temissiveColor " + rEmissive + " " + gEmissive + " " + bEmissive );
			}
			out.println( "\t\t\t\t}" );
			out.println( "\t\t\t}" );
			out.println( "\t\t\tgeometry IndexedLineSet {" );
			out.println( "\t\t\t\tcoord Coordinate {" );
			out.println( "\t\t\t\t\tpoint [" );
			for ( int i = 0; i < vertices.getNumberEntities(); i++ ) {
				SpacePrimitive sp = vertices.getEntity( i );
				out.println( "\t\t\t\t\t\t" + sp.getX() + " " + sp.getY() + " " + sp.getZ() );
			}
			for ( int i = 0; i < faces.getNumberEntities(); i++ ) {
				SpacePrimitive sp = faces.getEntity( i );
				out.println( "\t\t\t\t\t\t" + sp.getX() + " " + sp.getY() + " " + sp.getZ() );
			}
			out.println( "\t\t\t\t\t]" );
			out.println( "\t\t\t\t}" );
			out.println( "\t\t\t\tcoordIndex [" );
			for ( int i = 0; i < vertices.getNumberEntities(); i++ ) {
				out.println( "\t\t\t\t\t" + i + " " + ( i + vertices.getNumberEntities()) + " -1" );
			}
			out.println( "\t\t\t\t]" );
			out.println( "\t\t\t}" );
			out.println( "\t\t}" );
			out.println( "\t]" );
			out.println( "}" );
		} catch ( Exception e ) {
		}
	}

	public void generateVrmlShapeSetVtoF( SpaceStructure s, PrintStream out ) {
		s.setLocation( SpacePrimitive.Face, SpacePrimitive.Vertex );
		SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
		SpaceEntitySet faces = s.getEntitySet( SpacePrimitive.Face );
		try {
			out.println( "Transform {" );
			out.println( "\tchildren [" );
			for ( int i = 0; i < faces.getNumberEntities(); i++ ) {
				SpacePrimitive sp = faces.getEntity( i );
				out.println( "\t\tTransform {" );
				out.println( "\t\t\ttranslation " + sp.getX()*1.5 + " " + sp.getY()*1.5 + " " + sp.getZ()*1.5);
				out.println( "\t\t\trotation " + sp.getZ() + " 0 " + -1*sp.getX() + " " +
					( Math.atan(
						SpaceEntitySet.distanceBetween( 0, sp.getY(), 0, sp.getX(), sp.getY(), sp.getZ() )/
						SpaceEntitySet.distanceBetween( 0, 0, 0, 0, sp.getY(), 0 )) +
						(( sp.getY() < 0 ) ? Math.PI/2 : 0 )));
				out.println( "\t\t\tchildren [" );
				out.println( "\t\t\t\tShape {" );
				out.println( "\t\t\t\t\tappearance Appearance {" );
				out.println( "\t\t\t\t\t\tmaterial Material {" );
				out.println( "\t\t\t\t\t\t\tdiffuseColor 0 1 1" );
				out.println( "\t\t\t\t\t\t\temissiveColor 0.6 0.2 0.6" );
				out.println( "\t\t\t\t\t\t}" );
				out.println( "\t\t\t\t\t}" );
				out.println( "\t\t\t\t\tgeometry Cone {" );
				out.println( "\t\t\t\t\t\tbottomRadius 0.1" );
				out.println( "\t\t\t\t\t\theight 2.5" );
				out.println( "\t\t\t\t\t}" );
				out.println( "\t\t\t\t}" );
				out.println( "\t\t\t]" );
				out.println( "\t\t}" );
			}
			out.println( "\t]" );
			out.println( "}" );
		} catch ( Exception e ) {
		}
	}

	public void generateVrml( PrintStream out, int type ) {
		try {
			out.println( "Transform {" );
			out.println( "\tchildren [" );
			out.println( "\t\tShape {" );
			out.println( "\t\t\tappearance Appearance {" );
			out.println( "\t\t\t\tmaterial Material {" );
			out.println( "\t\t\t\t\tdiffuseColor 0.6 0.3 0.5" );
			if ( transparency != DefaultTransparency ) {
				out.println( "\t\t\t\t\ttransparency " + transparency );
			}
			if (( rEmissive != DefaultEmissiveColor ) || ( gEmissive != DefaultEmissiveColor ) ||
				( bEmissive != DefaultEmissiveColor )) {
				out.println( "\t\t\t\t\temissiveColor " + rEmissive + " " + gEmissive + " " + bEmissive );
			}
			out.println( "\t\t\t\t}" );
			out.println( "\t\t\t}" );
			if ( type == SpacePrimitive.Face ) {
				out.println( "\t\t\tgeometry IndexedFaceSet {" );
			} else if ( type == SpacePrimitive.Edge ) {
				out.println( "\t\t\tgeometry IndexedLineSet {" );
			}
			out.println( "\t\t\t\tcoord Coordinate {" );
			out.println( "\t\t\t\t\tpoint [" );
			SpaceEntitySet vertices = getEntitySet( SpacePrimitive.Vertex );
			for ( int i = 0; i < vertices.getNumberEntities(); i++ ) {
				SpacePrimitive sp = vertices.getEntity( i );
				out.println( "\t\t\t\t\t\t" + sp.getX() + " " + sp.getY() + " " + sp.getZ() );
			}
			out.println( "\t\t\t\t\t]" );
			out.println( "\t\t\t\t}" );
			out.println( "\t\t\t\tcoordIndex [" );
			if ( type == SpacePrimitive.Face ) {
				SpaceEntitySet faces = getEntitySet( SpacePrimitive.Face );
				for ( int i = 0; i < faces.getNumberEntities(); i++ ) {
					SpacePrimitive sp = faces.getEntity( i );
					StringBuffer b = new StringBuffer( "\t\t\t\t\t" );
					for ( int j = 0; j < faces.getCount( sp, SpacePrimitive.Vertex ); j++ ) {
						b.append( faces.getValue( sp, SpacePrimitive.Vertex, j ));
						b.append( " " );
					}
					b.append( "-1" );
					String s = new String( b );
					out.println( s );
				}
			} else if ( type == SpacePrimitive.Edge ) {
				E();
				SpaceEntitySet edges = getEntitySet( SpacePrimitive.Edge );
				for ( int i = 0; i < edges.getNumberEntities(); i++ ) {
					SpacePrimitive edge = edges.getEntity( i );
					StringBuffer b = new StringBuffer( "\t\t\t\t\t" );
					b.append( edges.getValue( edge, SpacePrimitive.Vertex, 0 ));
					b.append( " " );
					b.append( edges.getValue( edge, SpacePrimitive.Vertex, 1 ));
					b.append( " -1" );
					String s = new String( b );
					out.println( s );
				}
			}
			out.println( "\t\t\t\t]" );
			out.println( "\t\t\t}" );
			out.println( "\t\t}" );
			out.println( "\t]" );
			out.println( "}" );
		} catch ( Exception e ) {
		}
	}
	*/

	//
	//  bogus op to get traceback in error cases
	//
	void getTraceback() {
		SpaceStructure t = null;
		t.getTraceback();
	}
}


