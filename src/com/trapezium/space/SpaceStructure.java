/*
 * @(#)SpaceStructure.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.space;

import com.trapezium.vrml.node.space.BoundingBox;

/** SpaceStructure is a second pass at a general object that can be used for
 *  VRML space structure operations.
 */
public class SpaceStructure implements SpaceCorrespondenceConstants {
    Vertices vertices;
    Faces faces;
    TexturesByValue texCoord;
    TexturesByIndex texCoordIndex;
    ColorsByValue color;
    ColorsByIndex colorIndex;
    NormalsByValue normal;
    NormalsByIndex normalIndex;

    /** Class constructor */
    public SpaceStructure() {
        vertices = new Vertices();
        faces = new Faces();
    }
    
    /** Copy constructor */
    public SpaceStructure( SpaceStructure src ) {
        vertices = new Vertices( src.getVertices() );
        faces = new Faces( src.getFaces() );
    }
    
    /** Get the bounding box for the structure */
    public BoundingBox getBoundingBox() {
        return( vertices.getBoundingBox() );
    }
    
    /** r2 convert faces into vertices and vertices into faces, dual operation */
    public void r2() {
        Vertices oldVertices = vertices;
        Faces oldFaces = faces;
        vertices = new Vertices( oldVertices, oldFaces );
        faces = new Faces( oldVertices, oldFaces );
    }

    /** Get the Vertices for this SpaceStructure */
    public Vertices getVertices() {
        return( vertices );
    }

    /** Set the Verties for this SpaceStructure */
    public void setVertices( Vertices vertices ) {
        this.vertices = vertices;
    }
   
    /** Get the Faces for this SpaceStructure */
    public SpaceEntitySet getFaces() {
        return( faces );
    }

    /** Get the TexCoords for this SpaceStructure */
    public SpaceEntitySet getTexCoord() {
        return( texCoord );
    }
    
    /** Get the value of a specific texCoord */
    public void getTexCoord( int offset, float[] result ) {
        texCoord.getLocation( offset, result );
    }
 
    public SpaceEntitySet getTexCoordIndex() {
        return( texCoordIndex );
    }

    /** Get the Color SpaceEntitySet */
    public SpaceEntitySet getColor() {
        return( color );
    }

    public SpaceEntitySet getColorIndex() {
        return( colorIndex );
    }

    public SpaceEntitySet getNormal() {
        return( normal );
    }

    public SpaceEntitySet getNormalIndex() {
        return( normalIndex );
    }

    /** Create the texture SpaceEntitySets based on Field existence.
     *
     *  @param f_texCoord corresponds to the node "texCoord" field
     *  @param f_texCoordIndex corresponds to the node "texCoordIndex" field
     */
    public void createTextures( Object f_texCoord, Object f_texCoordIndex ) {
        if ( f_texCoord != null ) {
            if ( f_texCoordIndex == null ) {
                texCoord = new TexturesByValue( CorrespondsToV );
            } else {
                texCoord = new TexturesByValue( ControlledByIndex );
                texCoordIndex = new TexturesByIndex( CorrespondsToF );
            }
        }
    }

    /** Create the color SpaceEntitySets based on Field existence.
     *  
     *  @param f_color corresponds to the node "color" field
     *  @param f_colorIndex corresponds to the node "colorIndex" field
     *  @param f_colorPerVertex boolean value of the node "colorPerVertex" field
     */
    public void createColors( Object f_color, Object f_colorIndex, boolean f_colorPerVertex ) {
        if ( f_colorPerVertex ) {
            if ( f_colorIndex == null ) {
                color = new ColorsByValue( CorrespondsToV );
            } else {
                color = new ColorsByValue( ControlledByIndex );
                colorIndex = new ColorsByIndex( CorrespondsToV );
            }
        } else {
            if ( f_colorIndex == null ) {
                color = new ColorsByValue( CorrespondsToSpaceEntitySet );
            } else {
                color = new ColorsByValue( ControlledByIndex );
                colorIndex = new ColorsByIndex( CorrespondsToSpaceEntitySet );
            }
        }
    }

    /** Create the normal SpaceEntitySets based on Field existence.
     *
     *  @param f_normal corresponds to the node "normal" field
     *  @param f_normalIndex corresponds to the node "normalIndex" field
     *  @param f_normalPerVertex boolean value of the node "normalPerVertex" field
     */
    public void createNormals( Object f_normal, Object f_normalIndex, boolean f_normalPerVertex ) {
        if ( f_normalPerVertex ) {
            if ( f_normalIndex == null ) {
                normal = new NormalsByValue( CorrespondsToV );
            } else {
                normal = new NormalsByValue( ControlledByIndex );
                normalIndex = new NormalsByIndex( CorrespondsToV );
            }
        } else {
            if ( f_normalIndex == null ) {
                normal = new NormalsByValue( CorrespondsToSpaceEntitySet );
            } else {
                normal = new NormalsByValue( ControlledByIndex );
                normalIndex = new NormalsByIndex( CorrespondsToSpaceEntitySet );
            }
        }
    }
    
    /** Check if the "texCoord" field needs to be replaced if the coord and coordIndex
     *  fields are modified.  
     *
     *  @return true if the "texCoord" field exists, but the "texCoordIndex" field does not,
     *     otherwise returns false.
     */
    public boolean replaceTexCoord() {
        return(( texCoordIndex == null ) && ( texCoord != null ));
    }
    
    /** Check if the "texCoordIndex" field needs to be replaced if the coord and coordIndex
     *  fields are modified.
     *
     *  @return true if the "texCoordIndex" field exists, otherwise returns false.
     */
    public boolean replaceTexCoordIndex() {
        return( texCoordIndex != null );
    }
    
    /** Check if the "color" field needs to be replaced if the coord and coordIndex fields
     *  are modified.
     *
     *  @return true if the "color" field exists, but the "colorIndex" field does not,
     *     otherwise returns false.
     */
    public boolean replaceColor() {
        return(( colorIndex == null ) && ( color != null ));
    }
    
    /** Check if the "colorIndex" field needs to be replaced if the coord and coordIndex fields
     *  are modified.
     *
     *  @return true if the "colorIndex" field exists
     */
    public boolean replaceColorIndex() {
        return( colorIndex != null );
    }

    /** Check if the "normal" field needs to be replaced if the coord and coordIndex fields
     *  are modified.
     *
     *  @return true if the "normal" field exists, but the "normalIndex" field does not,
     *     otherwise returns false.
     */
    public boolean replaceNormal() {
        return(( normalIndex == null ) && ( normal != null ));
    }
    
    /** Check if the "normalIndex" field needs to be replaced if the coord and coordIndex fields
     *  are modified.
     *
     *  @return true if the "normalIndex" field exists
     */
    public boolean replaceNormalIndex() {
        return( normalIndex != null );
    }
    
    /** Get the coordIndex offset of a particular value. */
    public int getCoordIndexOffset( int value ) {
        return( faces.getOffsetOfValue( value ));
    }
    
    /** Get the texCoordIndex value at a particular offset */
    public int getTexCoordIndexValue( int offset ) {
        return( texCoordIndex.getValueAt( offset ));
    }
}
