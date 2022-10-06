/*
 * @(#)AttractorPolygonReduction.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.chisel.*;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.space.*;
import com.trapezium.vrmlspace.*;
import com.trapezium.parse.TokenTypes;

import java.util.Enumeration;
import java.util.Hashtable;

/** Abstract base class for all attractor reduction chisels.
 *
 *  Attractor polygon reduction chisels use one IFS (the attractor) to reduce
 *  a second IFS (the floater).  It does this by warping the first IFS
 *  onto the second.  The result has the same basic polygon count as the first
 *  IFS, but the same shape as the second IFS.
 *
 *  Actually, the resulting polygon count is the same as triangulating all
 *  faces of the attractor IFS by placing a vertex in the center of the
 *  face and connecting it to the surrounding vertices.
 *
 *  The reduction takes place in two phases.
 *
 *  First, the floater IFS is partitioned into sets of coordinates.  Each
 *  coordinate in a set is closest to a particular attractor coordinate
 *  than to any other attractor coordinate.  Within each of these sets,
 *  the coordinate nearest to the attractor is marked for preservation.
 *
 *  Next, the dual of the attractor is created, and the coordinate sets
 *  calculated again.  Within each of these sets, the coordinate farthest
 *  from the attractor is marked for preservation.
 *  
 *  The set of floater coordinates to be preserved are then merged into 
 *  a single triangulated structure, with (V+F) vertices and 2*E triangle
 *  faces, where V,E,F are the number of vertices, edges, and faces in
 *  the attractor.
 *
 *  Subclasses select specific attractors.
 *
 *  Options:  minimum number of vertices required in the floater
 *            preserve color boundaries
 *            preserve edge boundaries
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 7 Oct 1998
 *
 *  @since           1.0
 */

abstract public class AttractorPolygonReduction extends Optimizer implements IFS, SpaceCorrespondenceConstants {
    // Option 0 - minimum number of vertices required in floater
    // Option 1 - preserve color boundaries
    // Option 2 - preserve edge boundaries
    static public final int MIN_FLOATER_VERTICES = 0;
    static public final int PRESERVE_COLOR_BOUNDARIES = 1;
    static public final int PRESERVE_EDGE_BOUNDARIES = 2;

    // option 0, actual value
    int minFloaterVertices;
    // option 1, actual value
    boolean preserveColorBoundaries;
    // option 2, actual value
    boolean preserveEdgeBoundaries;

    // keeps track of SpaceStructures available to the algorithm
    protected SpaceStructureLoader spaceStructureLoader;

    /** ReducerState is an internal class, tracks whether or not the
     *  reduction algorithm has taken place, since it can't actually occur
     *  until optimize time (due to DEF/USE of Coordinate nodes)
     */
    class ReducerState {
        AttractorReductionAlgorithm theAlgorithm;
        boolean reduced;
 
        ReducerState( AttractorReductionAlgorithm theAlgorithm ) {
            this.theAlgorithm = theAlgorithm;
            reduced = false;
        }

        /** Apply the reduction algorithm if it hasn't yet been applied */
        void reduce() {
            if ( !reduced ) {
                reduced = true;
                theAlgorithm.reduce();
            }
        }

        AttractorReductionAlgorithm getAlgorithm() {
            return( theAlgorithm );
        }
    }

    /** The actual parameter passed to the optimize method, uses the
     *  ReducerState above to track whether or not the reduction algorithm
     *  has occurred since it has to happen at optimize time due to DEF/USE
     *  of coordinate nodes.
     */
    class AttractorParam {
        /** The IFS field type being replaced */
        int type;
        
        /** IFS.OnePerCoord, IFS.OnePerCoordIndex, IFS.OnePerFace -- how the
         *  texture, color, normal field corresponds to others in the IFS
         */
        int correspondenceType;
        
        /** used to execute the algorithm */
        ReducerState reducer;

        /** Class constructor.
         *
         *  @param reducer handles one-time-only reduction algorithm running
         *  @param type type of IFS field being affected.
         */
        AttractorParam( ReducerState reducer, int type ) {
            this( reducer, type, -1 );
        }
        
        /** Class constructor.
         *
         *  @param reducer handles one-time-only reduction algorithm running
         *  @param type type of IFS field being affected
         *  @param correspondenceType indicates the type of correpondence
         *     between the values being replaced and the coord/coordIndex
         *     fields of the IFS.  Possible values are:  IFS.OnePerCoord,
         *     IFS.OnePerFace, and IFS.OnePerCoordIndex.  The value is -1
         *     and ignored if this is for the coord or coordIndex fields.
         */
         AttractorParam( ReducerState reducer, int type, int correspondenceType ) {
            this.reducer = reducer;
            this.type = type;
            this.correspondenceType = correspondenceType;
        }

        /** Get the correspondence type for this parameter */
        int getCorrespondenceIndicator() {
            return( correspondenceType );
        }
        
        /** Execute the polygon reduction algorithm */
        void reduce() {
            reducer.reduce();
        }

        /** Get the type of field being replaced, one of the IFS.* values */
        int getReplacementType() {
            return( type );
        }

        AttractorReductionAlgorithm getAlgorithm() {
            return( reducer.getAlgorithm() );
        }
        
        SpaceStructure getOriginalSpaceStructure() {
            return( reducer.getAlgorithm().getOriginal() );
        }
    }

    /** Class constructor */
    public AttractorPolygonReduction( String reductionDescription ) {
        super( "IndexedFaceSet", reductionDescription );
        reset();
    }

    /** subclasses override this to provide specific attractors */
    abstract public SpaceStructure generateAttractor();

    /** subclasses override this if they have additional options */
    public int getNumberOptions() {
        return( 3 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        Class c;
        try {
            if ( optionOffset == MIN_FLOATER_VERTICES ) {
                c = Integer.TYPE;
            } else {
                c = Boolean.TYPE;
            }
        } catch (Exception e) {
            c = null;
        }
        return c;
    }

    public String getOptionLabel( int offset ) {
        switch( offset ) {
        case MIN_FLOATER_VERTICES:
            return( "Minimum coordinate count" );
        case PRESERVE_COLOR_BOUNDARIES:
            return( "Preserve color boundaries" );
        case PRESERVE_EDGE_BOUNDARIES:
            return( "Preserve edge boundaries" );
        default:
            return( "** unknown **" );
        }
    }

    public Object getOptionValue( int offset ) {
        switch( offset ) {
        case MIN_FLOATER_VERTICES:
            return( intToOptionValue(minFloaterVertices) );
        case PRESERVE_COLOR_BOUNDARIES:
            return( booleanToOptionValue(preserveColorBoundaries) );
        case PRESERVE_EDGE_BOUNDARIES:
            return( booleanToOptionValue(preserveEdgeBoundaries) );
        default:
            return( null );
        }
    }

    public void setOptionValue( int offset, Object value ) {
        switch( offset ) {
        case MIN_FLOATER_VERTICES:
            minFloaterVertices = optionValueToInt(value);
            break;
        case PRESERVE_COLOR_BOUNDARIES:
            preserveColorBoundaries = optionValueToBoolean(value);
            break;
        case PRESERVE_EDGE_BOUNDARIES:
            preserveEdgeBoundaries = optionValueToBoolean(value);
            break;
        default:
            break;
        }
    }

    public Object getOptionConstraints( int offset ) {
        switch (offset) {
            case MIN_FLOATER_VERTICES:
                return( new IntegerConstraints(25, 5000, 25 ));
        }
        return "";
    }

    /** reset the Chisel */
    public void reset() {
        spaceStructureLoader = new SpaceStructureLoader();
    }

    /** Attempt optimization by creating a SpaceStructure for the node,
     *  then if optimization is possible, register the node for optimization.
     */ 
    public void attemptOptimization( Node n ) {
        setDataSource( n );
        SpaceStructure nSpace = spaceStructureLoader.loadSpaceStructure( n );
        if ( optimizationOK( nSpace, n )) {
            registerOptimization( nSpace, n );
        }
    }


    /** Optimize a node.
     *
     *  @param tp text printing destination
     *  @param param object used for optimization
     *  @param startTokenOffset first token offset in area being regenerated
     *  @param endTokenOffset last token offset in area being regenerated
     */
    public void optimize( TokenPrinter tp, Object param, 
        int startTokenOffset, int endTokenOffset ) {
        if ( param instanceof AttractorParam ) {
            AttractorParam ap = (AttractorParam)param;
            ap.reduce();
            int replacementType = ap.getReplacementType();
            int correspondenceIndicator = ap.getCorrespondenceIndicator();
            AttractorReductionAlgorithm theAlgorithm = ap.getAlgorithm();
            SpaceEntitySet newCoords = theAlgorithm.getNewCoords();
            int numberNewCoords = newCoords.getNumberEntities();
            if ( replacementType == IFS.Coord ) {
                replaceCoord( tp, theAlgorithm.getNewCoords() );
            } else if ( replacementType == IFS.CoordIndex ) {
                replaceCoordIndex( tp, theAlgorithm.enumerateFaces() );
            } else if ( replacementType == IFS.TexCoord ) {
                replaceTexCoord( tp, numberNewCoords,
                    theAlgorithm.getNewToOldMapping(), theAlgorithm.getOriginal(),
                    startTokenOffset, endTokenOffset );
            } else if ( replacementType == IFS.TexCoordIndex ) {
                replaceTexCoordIndex( tp, theAlgorithm.enumerateFaces(),
                    theAlgorithm.getOriginal(),
                    theAlgorithm.getNewToOldMapping() );
            } else if ( replacementType == IFS.Color ) {
                SpaceStructure original = theAlgorithm.getOriginal();
                SpaceEntitySet color = original.getColor();
                if ( color.getCorrespondenceType() == CorrespondsToV ) {
                    replaceUsingV( tp, numberNewCoords, theAlgorithm.getNewToOldMapping(),
                        original.getColor(), startTokenOffset, endTokenOffset );
                } else {
                    replaceUsingF( tp, theAlgorithm.getNewToOldMapping(),
                        original.getFaces(), original.getColor(), 
                        startTokenOffset, endTokenOffset );
                }
            } else if ( replacementType == IFS.ColorIndex ) {
                SpaceStructure original = theAlgorithm.getOriginal();
                SpaceEntitySet colorIndex = original.getColorIndex();
//                if ( colorIndex.getCorrespondenceType() == CorrespondsToV ) {
//                    replaceColorIndex( tp, 
//                } else {
//                    replaceColorIndex( tp, 
//                }
            } else if ( replacementType == IFS.Normal ) {
                SpaceStructure original = theAlgorithm.getOriginal();
                SpaceEntitySet normal = original.getNormal();
                if ( normal.getCorrespondenceType() == CorrespondsToV ) {
                    replaceUsingV( tp, numberNewCoords, theAlgorithm.getNewToOldMapping(),
                        original.getNormal(), startTokenOffset, endTokenOffset );
                } else {
                    replaceUsingF( tp, theAlgorithm.getNewToOldMapping(),
                        original.getFaces(), original.getNormal(),
                        startTokenOffset, endTokenOffset );
                }
            } else if ( replacementType == IFS.NormalIndex ) {
                SpaceStructure original = theAlgorithm.getOriginal();
                SpaceEntitySet normalIndex = original.getNormalIndex();
//                if ( normalIndex.getCorrespondenceType() == CorrespondsToV ) {
//                    replaceNormalIndex( tp, 
//                } else {
//                    replaceNormalIndex( tp, 
//                }
            }
        }
    }

    // set dataSource if it isn't set
    void setDataSource( Node n ) {
        if ( dataSource == null ) {
            Scene s = (Scene)n.getRoot();
            dataSource = s.getTokenEnumerator();
        }
    }

    /** Check if optimization is allowed according to the user configurable 
     *  guidelines.  The return value is based on whether the SpaceStructure
     *  exists and meets the minimum number of vertices requirement set by 
     *  the user.
     *
     *  @param nSpace the SpaceStructure to check for possible optimization
     *  @param n the corresponding Node (parameter not used)
     *
     *  @return true if the SpaceStructure should be optimized, otherwise false.
     */
    public boolean optimizationOK( SpaceStructure nSpace, Node n ) {
        if ( nSpace == null ) {
            return( false );
        }
        SpaceEntitySet v = nSpace.getVertices();
        if ( v == null ) {
            return( false );
        }
        return( v.getNumberEntities() >= minFloaterVertices );
    }

 
    /** Register the "coord" and "coordIndex" section of the Node for
     *  replacement by this chisel.  This is only done if those fields exist
     *  and the chisel successfully created an attractor SpaceStructure.
     *
     *  @param nSpace the floater SpaceStructure to be reduced
     *  @param n the Node corresponding to the floater SpaceStructure
     */
    void registerOptimization( SpaceStructure nSpace, Node n ) {
        Node coord = n.getNodeValue( "coord" );
        Field coordIndex = n.getField( "coordIndex" );
        SpaceStructure attractor = generateAttractor();
        if (( coord != null ) && ( coordIndex != null ) && 
            ( attractor != null )) {
            AttractorReductionAlgorithm alg = new AttractorReductionAlgorithm();
            alg.setOriginal( nSpace );
            alg.setAttractor( attractor );

            ReducerState rc = new ReducerState( alg );
            AttractorParam coordParam = new AttractorParam( rc, IFS.Coord );
            AttractorParam coordIndexParam = new AttractorParam( rc, IFS.CoordIndex );
            replaceRange( coord.getFirstTokenOffset(),
                coord.getLastTokenOffset(), coordParam );
            replaceRange( coordIndex.getFirstTokenOffset(),
                coordIndex.getLastTokenOffset(), coordIndexParam );
                
            if ( nSpace.replaceTexCoord() ) {
                Field texCoord = n.getField( "texCoord" );
                replaceRange( texCoord.getFirstTokenOffset(), texCoord.getLastTokenOffset(), 
                    new AttractorParam( rc, IFS.TexCoord ));
            }
            if ( nSpace.replaceTexCoordIndex() ) {
                Field texCoordIndex = n.getField( "texCoordIndex" );
                replaceRange( texCoordIndex.getFirstTokenOffset(), 
                    texCoordIndex.getLastTokenOffset(),
                    new AttractorParam( rc, IFS.TexCoordIndex ));
            }
            if ( nSpace.replaceColor() ) {
                Field color = n.getField( "color" );
                replaceRange( color.getFirstTokenOffset(), color.getLastTokenOffset(),
                    new AttractorParam( rc, IFS.Color ));
            }
            if ( nSpace.replaceColorIndex() ) {
                Field colorIndex = n.getField( "colorIndex" );
                replaceRange( colorIndex.getFirstTokenOffset(), colorIndex.getLastTokenOffset(),
                    new AttractorParam( rc, IFS.ColorIndex ));
            }
            if ( nSpace.replaceNormal() ) {
                Field normal = n.getField( "normal" );
                replaceRange( normal.getFirstTokenOffset(), normal.getLastTokenOffset(),
                    new AttractorParam( rc, IFS.Normal ));
            }
            if ( nSpace.replaceNormalIndex() ) {
                Field normalIndex = n.getField( "normalIndex" );
                replaceRange( normalIndex.getFirstTokenOffset(), normalIndex.getLastTokenOffset(),
                    new AttractorParam( rc, IFS.NormalIndex ));
            }
        }
    }


    /** Regenerate the text for the coordinates, based on the locations
     *  provided by the SpaceEntitySet, which has by this time been modified
     *  by the reduction algorithm.
     */
    void replaceCoord( TokenPrinter tp, SpaceEntitySet vertices ) {
        tp.printTo( TokenTypes.LeftBracket );
        int numberV = vertices.getNumberEntities();
        float[] v = new float[3];
        for ( int i = 0; i < numberV; i++ ) {
            vertices.getLocation( i, v );
            tp.print3f( v );
        }
        tp.skipTo( TokenTypes.RightBracket );
        tp.printTo( TokenTypes.RightBrace );
    }

    /** Regenerate the text for the faces.
     *
     *  @param tp text print destination
     *  @param faces enumeration of all regenerated faces
     */
    void replaceCoordIndex( TokenPrinter tp, Enumeration faces ) {
        if ( faces != null ) {
            tp.printTo( TokenTypes.LeftBracket );
            while ( faces.hasMoreElements() ) {
                FaceGenerator fg = (FaceGenerator)faces.nextElement();
                int numFaces = fg.getNumberFaces();
                int[] face = new int[3];
                for ( int i = 0; i < numFaces; i++ ) {
                    if ( fg.getFace( i, face )) {
                         tp.print3i( face );
                         tp.print( -1 );
                    }
                }
            }
            tp.skipTo( TokenTypes.RightBracket );
            tp.printTo( TokenTypes.RightBracket );
        }
    }

    /** Regenerate the text for the texCoordIndex field.
     *
     *  @param tp text print destination
     *  @param faces enumeration of all regenerated faces
     */
    void replaceTexCoordIndex( TokenPrinter tp, Enumeration faces,
        SpaceStructure originalSpaceStructure, int[] newToOldMapping ) {
        if ( faces != null ) {
            tp.printTo( TokenTypes.LeftBracket );
            while ( faces.hasMoreElements() ) {
                FaceGenerator fg = (FaceGenerator)faces.nextElement();
                int numFaces = fg.getNumberFaces();
                int[] face = new int[3];
                for ( int i = 0; i < numFaces; i++ ) {
                    if ( fg.getFace( i, face )) {
                         printTexCoordIndex( tp, face, originalSpaceStructure, newToOldMapping );
                    }
                }
            }
            tp.skipTo( TokenTypes.RightBracket );
            tp.printTo( TokenTypes.RightBracket );
        }
    }

    /** Print the texCoordIndex values for a regenerated face.
     *  In this case, the texCoord field has not changed, so the
     *  new face coordIndex values have to be mapped into old face
     *  texCoordIndex values.
     */
    void printTexCoordIndex( TokenPrinter tp, int[] face, SpaceStructure original,
        int[] newToOldMapping ) {
        // the face index is the resulting magic coord, from this we get 
        // the original floater coord
        for ( int i = 0; i < 3; i++ ) {
            // get the offset of the original coord
            int originalOffset = newToOldMapping[ face[i] ];
            // get any offset into the original coordIndex for that coord, since
            // the texCoordIndex has to correspond one-for-one with that coordIndex
            int coordIndexOffset = original.getCoordIndexOffset( originalOffset );
            // get and print the texCoordIndex value from the original at that
            // offset, this is OK because the texCoord is not changed when this
            // method is called, so references to the original texCoord are valid
            tp.print( original.getTexCoordIndexValue( coordIndexOffset ));
        }
        tp.print( -1 );
    }

    /** Regenerate the text for the "texCoord" field, this is always
     *  one-to-one with original coords. 
     *
     *  @param tp print destination
     *  @param numberNewCoords the number of new coordinates which need corresponding texCoords
     *  @param newToOldMapping the mapping from the magic coords to the originals
     *  @param startTokenOffset first in sequence of tokens to replace
     *  @param endTokenOffset last in sequence of tokens to replace
     */
    void replaceTexCoord( TokenPrinter tp, int numberNewCoords,
        int[] newToOldMapping, SpaceStructure original,
        int startTokenOffset, int endTokenOffset ) {
        // print up to the first number
        tp.limitRange( startTokenOffset, endTokenOffset );
        tp.printTo( TokenTypes.NumberToken );
        
        // for each preserved coord, get and print the corresponding texCoord
        // the magic mapping gives me an offset of the original preserved
        // coordinate, in order?
        float tc[] = new float[2];
        for ( int i = 0; i < numberNewCoords; i++ ) {
            int originalOffset = newToOldMapping[i];
            original.getTexCoord( originalOffset, tc );
            tp.print( tc[0] );
            tp.print( tc[1] );
        }
        tp.skipTo( TokenTypes.RightBracket );
        tp.print( "] }" );
    }
    
    /** replace the "color" field.
     *
     *  If we are replacing the color field, it means there is no index field,
     *  in this case, the color field either corresponds to the Vertices, or 
     *  corresponds to the Faces (depends on "colorPerVertex")
     */
     
    /** This version replaces a "color" field where each entry corresponds to a coordinate
     *
     *  @param tp print destination
     *  @param numberNewCoords number of new coordinates
     */
    void replaceUsingV( TokenPrinter tp, int numberNewCoords, int[] newToOldMapping, 
        SpaceEntitySet original, int startTokenOffset, int endTokenOffset ) {
        // print up to the first number
        tp.limitRange( startTokenOffset, endTokenOffset );
        tp.printTo( TokenTypes.NumberToken );
        float tc[] = new float[3];
        for ( int i = 0; i < numberNewCoords; i++ ) {
            int originalOffset = newToOldMapping[i];
            original.getLocation( originalOffset, tc );
            tp.print( tc[0] );
            tp.print( tc[1] );
            tp.print( tc[2] );
        }
        tp.skipTo( TokenTypes.RightBracket );
        tp.print( "] }" );
    }
    
    /** Replace values that originally corresponded to faces.
     *
     *  @param tp print destination
     *  @param newToOldMapping mapping form new coordinates to old
     *  @param faces original faces
     *  @param colors original colors
     *  @param startTokenOffset first of sequence being regenerated
     *  @param endTokenOffset last of sequence being regenerated
     */
    void replaceUsingF( TokenPrinter tp, int[] newToOldMapping,
        SpaceEntitySet faces, SpaceEntitySet colors,
        int startTokenOffset, int endTokenOffset ) {
        
    }
}


