/*
 * @(#)IndexInfo.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import java.util.BitSet;
import java.util.Hashtable;
 
/**
 *  Tracks association between index, perVertex, and value fields.
 *
 *
 *  The IndexInfo is used to track associates between related fields.  The technique
 *  used repeatedly in the VRML 2.0 spec is centered on coord and coordIndex.  The coord
 *  is a Node, coordIndex is not.  This means coord values can be DEFfed and USEd, but
 *  index values cannot.
 *
 *  The second technique is associations between coord and texCoord, color, and normal
 *  fields.  This assocation is implicit in the data.  If the texCoord, color, or normal
 *  nodes exist, and there is no texCoordIndex, colorIndex, normalIndex, then these
 *  three nodes (texCoord, color, normal) are indexed using the coordIndex.
 *
 *  If the texCoord, color, or normal nodes exist, and there is an associated texCoordIndex,
 *  colorIndex, normalIndex, then these nodes are indexed by texCoordIndex, colorIndex,
 *  normalIndex, with the restriction that texCoord, color, normal node values are associated
 *  one-for-one with coordinates in the coord node if the perVertex flag is true.  If the
 *  perVertex flag is false, the texCoord, color, normal node values are indexed by
 *  and index field -- the index field is texCoordIndex, colorIndex, normalIndex if those
 *  exist, otherwise the index field is the coordIndex.
 *
 *  This class calculates all these dependencies, and returns the information, for each
 *  color/colorIndex, normal/normalIndex, texCoord/texCoordIndex in relation to coord/
 *  coordIndex.  These dependencies have no meaning for an IndexInfo associated with
 *  the coord/coordIndex itself.
 *           
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 7 May 1998, make Serializable
 *  @version         1.1, 19 Dec 1997
 *
 *  @since           1.0
 */
public class IndexInfo implements java.io.Serializable {
    // number values in the coordIndex, texCoordIndex, colorIndex, normalIndex
	int numberIndexValues;
	
	// reflection of the "perVertex" field.  If true, the index values of the colorIndex
	// or normalIndex are associated with each vertex in the co
	boolean perVertex;
	
	// set to true if the texCoordIndex, colorIndex, normalIndex is empty, and there
	// is an existing texCoord, color, normal node.
	boolean useCoordIndex;
	
	// the coordIndex, texCoordIndex, colorIndex, normalIndex MFFieldValue
	MFFieldValue index;
	
	// the number of values in the coordIndex, texCoordIndex, colorIndex, normalIndex MFFieldValue
	int indexCount;
	
	// the coord, texCoord, color, or normal node
	Node valueNode;
	
	// the number of values in the coord, texCoord, color, or normal node
	int numberRawValues;
	
	// the number of values by type, for coord, color, normal this is numberRawValues/3,
	// for texCoord this is numberRawValues/2
	int numberValues;
	
	// indicates what values in the coord, texCoord, color, or normal node are actually used
	// in general, this should probably be used to indicate which ones to keep
	BitSet usageBits;
	
	// for texCoord/texCoordIndex, color/colorIndex, normal/normalIndex nodes, there may
	// be an implicit association between these fields and the corresponding coord/coordIndex.
	// These fields are always here to provide the information necessary for those
	// possible associations.
	MFFieldValue coordIndex;
	Node coordValueNode;
	int numberCoordValues;
	int numberCoordIndexValues;
	
	// the IndexedFaceSet
	Node base;

	public IndexInfo( boolean perVertex,
			MFFieldValue index, Node valueNode, String valueName, 
			int factor, String indexString,
			MFFieldValue coordIndex, Node coordValueNode, Node base, Hashtable usageTable ) {
		this.perVertex = perVertex;
   		this.index = index;
		numberIndexValues = 0;
		if ( this.index != null ) {
			numberIndexValues = index.getRawValueCount();
		}
		if ( numberIndexValues == 0 ) {
			useCoordIndex = true;
		}
		if ( valueNode == null ) {
			useCoordIndex = false;
		}
		if ( !perVertex ) {
			useCoordIndex = false;
		}
		this.valueNode = valueNode;
		numberRawValues = 0;
		numberValues = 0;
		if ( valueNode != null ) {
			Field f = valueNode.getField( valueName );
			if ( f != null ) {
				FieldValue fv = f.getFieldValue();
				MFFieldValue fValues = null;
				if ( fv instanceof MFFieldValue ) {
					fValues = (MFFieldValue)fv;
					if ( index == null ) {
					    fValues.setIndexed( false );
					}
				}
				//
				//  The case where fValues is null is when it is defined by an IS field
				//
				if ( fValues != null ) {
					numberRawValues = fValues.getRawValueCount();
					numberValues = numberRawValues/factor;
					if ( numberValues > 0 ) {
						UsageInfo ui = (UsageInfo)usageTable.get( valueNode );
						if ( ui == null ) {
							usageTable.put( valueNode, new UsageInfo( numberValues, base, factor, (useCoordIndex ? "coordIndex" : indexString ), valueName ));
						}
					}
				}
			}
		}
		if (( usageTable != null ) && ( valueNode != null )) {
    		UsageInfo ui = (UsageInfo)usageTable.get( valueNode );
	    	if ( ui != null ) {
		        usageBits = ui.getUsageBits();
    		}   
    	}
   		this.coordIndex = coordIndex;
		numberCoordIndexValues = 0;
		if ( this.coordIndex != null ) {
			numberCoordIndexValues = coordIndex.getRawValueCount();
		}
		this.coordValueNode = coordValueNode;
		numberCoordValues = 0;
		if ( coordValueNode != null ) {
			Field f = coordValueNode.getField( "point" );
			if ( f != null ) {
				FieldValue fv = f.getFieldValue();
				MFFieldValue fValues = null;
				if ( fv instanceof MFFieldValue ) {
					fValues = (MFFieldValue)fv;
				}
				if ( fValues != null ) {
					numberCoordValues = fValues.getRawValueCount() / 3;
				}
			}
		}
		this.base = base;
	}

	public boolean isUsingCoordIndex() {
		return( useCoordIndex );
	}

	public boolean isPerVertex() {
		return( perVertex );
	}

	public boolean isUsingCoordFaces() {
		return( !perVertex );
	}

	public MFFieldValue getIndexValue() {
		return( index );
	}

	/** Get the number of values in the <B>index</B> field */
	public int getNumberIndexValues() {
		return( numberIndexValues );
	}

	//
	//  The number of values in the valueNode in terms of their units, i.e. one color
	//  counts as one value not 3.
	//
	public int getNumberValues() {
		return( numberValues );
	}

	//
	//  The number of values in the coordValueNode in terms of units, i.e. the number
	//  of actual coordinates x-y-z counts as 1.
	//
	public int getNumberCoordValues() {
		return( numberCoordValues );
	}

	//
	//  The number of values in the coordIndex
	//
	public int getNumberCoordIndexValues() {
		return( numberCoordIndexValues );
	}

	public Node getValueNode() {
		return( valueNode );
	}

	public BitSet getUsageBits() {
	    return( usageBits );
	}
	
	/*  Is each value applied to a single coordinate?
	 *  This happens if there are values, no index, and perVertex is true.
	 */
	public boolean isValueSynchedWithCoord() {
	    return(( numberValues > 0 ) && perVertex && ( numberIndexValues == 0 ));
	}
	
	/** Is each value applied to a single index?
	 *  This happens if there are values and an index field.
	 *  There are two variations on this case:
	 *   1. the index corresponds to the coordinate index
	 *   2. the index corresponds to the coordinate faces.
	 */
	public boolean isValueSynchedWithIndex() {
	    return(( numberValues > 0 ) && ( numberIndexValues > 0 ) && ( usageBits != null ));
	}
}
