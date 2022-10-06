/*
 * @(#)SpaceStructureLoader.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrmlspace;

import com.trapezium.space.*;
import com.trapezium.vrmlspace.*;
import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.*;
import com.trapezium.parse.TokenEnumerator;

import java.util.Vector;
import java.util.Hashtable;

/** The SpaceStructureLoader manages a set of SpaceStructures associated
 *  with IndexedFaceSets in a file.  Two sets of SpaceStructures are kept --
 *  one for each IFS in the file, accessed by index, and one for each
 *  Coordinate node in the file, accessed by coord node.  These lists will
 *  be different if the file uses DEF/USE of Coordinate nodes.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class SpaceStructureLoader {

    Vector independentStructures;
    Hashtable completeStructures;  // key coord node, value SpaceStructure
    TokenEnumerator dataSource;

    /** Class constructor */
    public SpaceStructureLoader() {
        dataSource = null;
        independentStructures = new Vector();
        completeStructures = new Hashtable();
    }

    // set dataSource if it isn't set
    void setDataSource( Node n ) {
        if ( dataSource == null ) {
            Scene s = (Scene)n.getRoot();
            dataSource = s.getTokenEnumerator();
        }
    }

    /** Access space structure by offset.
     *
     *  @param offset the offset of the SpaceStructure
     *  @return the SpaceStructure at that offset, null if offset invalid
     */
    public SpaceStructure getSpaceStructure( int offset ) {
        if (( offset < 0 ) || ( offset >= independentStructures.size() )) {
            return( null );
        } else {
            return( (SpaceStructure)independentStructures.elementAt( offset ));
        }
    }

    /** Access space structure by coord node.
     *
     * @param coordNode the Coordinate Node associated with a SpaceStructure,
     *    in this case, the SpaceStructure represents the union of all
     *    IndexedFaceSets sharing that Coordinate Node.
     * @return the SpaceStructure associated with the coordNode, null if none
     */
    public SpaceStructure getSpaceStructure( Node coordNode ) {
        return( (SpaceStructure)completeStructures.get( coordNode ));
    }

    /** Load a SpaceStructuare, save it in the independentStructures and
     *  completeStructures list.  If the Coordinate node is used, then the
     *  existing entry from the completeStructures list is used, and the
     *  faces for the current SpaceStructure are appended onto that entry,
     *  resulting in that entry being a union of all SpaceStructures associated
     *  with a particular coordinate node.
     *
     * @param n the IndexedFaceSet node to be loaded
     * @return a SpaceStructure containing the faces for the Node n (i.e.
     *    the independentStructures entry, not the completeStructures entry)
     */
    public SpaceStructure loadSpaceStructure( Node n ) {
        setDataSource( n );
        Node coord = n.getNodeValue( "coord" );
        if ( coord == null ) {
            return( null );
        }
        Field coordIndex = n.getField( "coordIndex" );
        Field texCoord = n.getField( "texCoord" );
        Field texCoordIndex = n.getField( "texCoordIndex" );
        Field color = n.getField( "color" );
        Field colorIndex = n.getField( "colorIndex" );
        Field normal = n.getField( "normal" );
        Field normalIndex = n.getField( "normalIndex" );

        SpaceStructure s = (SpaceStructure)completeStructures.get( coord );
        boolean coordNodeSSfound = !( s == null );
        if ( !coordNodeSSfound ) {
            s = new SpaceStructure();
        }
        SpaceStructure loaded = null;
        if ( anyDEFUSE( coord, texCoord, color, normal )) {
            loaded = new SpaceStructure();
            loaded.setVertices( s.getVertices() );
        } else {
            loaded = s;
        }
        load3f( coord, loaded.getVertices() );

        if ( coordIndex != null ) {
            loadIlist( coordIndex, loaded.getFaces(), s.getFaces() );
        }
        if ( !coordNodeSSfound ) {
            completeStructures.put( coord, s );
        }
        independentStructures.addElement( s );
        s.createTextures( texCoord, texCoordIndex );
        load2f( texCoord, s.getTexCoord() );
        loadIlist( texCoordIndex, s.getTexCoordIndex(), 
            loaded.getTexCoordIndex() );
        if ( loaded != s ) {
            loaded.createTextures( texCoord, texCoordIndex );
            load2f( texCoord, loaded.getTexCoord() );
        }
        s.createColors( color, colorIndex, n.getBoolValue( "colorPerVertex" ));
        load3f( color, s.getColor() );
        if ( loaded != s ) {
            loaded.createColors( color, colorIndex, n.getBoolValue( "colorPerVertex" ));
            load3f( color, loaded.getColor() );
        }
        loadIlist( colorIndex, s.getColorIndex(), loaded.getColorIndex() );
        s.createNormals( normal, normalIndex, n.getBoolValue( "normalPerVertex" ));
        load3f( normal, s.getNormal() );
        if ( loaded != s ) {
            loaded.createNormals( normal, normalIndex, n.getBoolValue( "normalPerVertex" ));
            load3f( normal, loaded.getNormal() );
        }
        loadIlist( normalIndex, s.getNormalIndex(), loaded.getNormalIndex() );
        return( loaded );
    }

    /** Check if any of the fields refer to a DEFUSEnode */
    boolean anyDEFUSE( Field f1, Field f2, Field f3, Field f4 ) {
        if ( hasDEFUSE( f1 )) {
            return( true );
        }
        if ( hasDEFUSE( f2 )) {
            return( true );
        }
        if ( hasDEFUSE( f3 )) {
            return( true );
        }
        return( hasDEFUSE( f4 ));
    }
    
    /** Check if field refers to a DEFUSENode */
    boolean hasDEFUSE( Field f ) {
        if ( f != null ) {
            FieldValue fv = f.getFieldValue();
            if ( fv instanceof SFNodeValue ) {
                SFNodeValue sfnv = (SFNodeValue)fv;
                Node n = sfnv.getNode();
                if ( n != null ) {
                    return( n.isDEForUSE() );
                }
            }
        }
        return( false );
    }
    
    /** Load the vertices into a SpaceStructure.
     *
     *  @param valueNode source of vertex data
     *  @param target where to load the data
     */
    void load3f( VrmlElement valueNode, SpaceEntitySet target ) {
        if ( valueNode != null ) {
            float[] loc = new float[3];
            dataSource.setState( valueNode.getFirstTokenOffset() );
            int last = valueNode.getLastTokenOffset();
            while ( dataSource.copy3f( loc, last )) {
                target.add3f( loc );
            }
        }
    }

    /** Load the texCoords into a SpaceStructure.
     *
     *  @param valueNode source of vertex data
     *  @param target where to load the data
     */
    void load2f( VrmlElement valueNode, SpaceEntitySet target ) {
        if ( valueNode != null ) {
            float[] loc = new float[2];
            dataSource.setState( valueNode.getFirstTokenOffset() );
            int last = valueNode.getLastTokenOffset();
            while ( dataSource.copy2f( loc, last )) {
                target.add2f( loc );
            }
        }
    }

    /** Load the index array into a SpaceStructure.
     *
     * @param ilist source of face data
     * @param target1 first of two destinations of face data, this is the
     *     one that only contains the single set of face data
     * @param target2 second of two destinations of face data, this is the
     *     one that contains the union of all faces with the same coord node
     */
    void loadIlist( VrmlElement ilist, SpaceEntitySet target1, SpaceEntitySet target2 ) {
        if ( target1 == target2 ) {
            target2 = null;
        }
        int scanner = ilist.getFirstTokenOffset();
        dataSource.setState( scanner );
        int last = ilist.getLastTokenOffset();
        int val = -1;
        while ( scanner < last ) {
            dataSource.skipTo( TokenTypes.NumberToken );
            scanner = dataSource.getState();
            if (( scanner == -1 ) || ( scanner > last )) {
                break;
            }
            val = dataSource.getIntValue( scanner );
            target1.add( val );
            if ( target2 != null ) {
                target2.add( val );
            }
            scanner = dataSource.getNextToken();
        }
        if ( val != -1 ) {
            target1.add( -1 );
            if ( target2 != null ) {
                target2.add( -1 );
            }
        }
    }
}
