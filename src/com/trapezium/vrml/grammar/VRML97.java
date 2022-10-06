/*
 * @(#)VRML97.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.node.NodeType;
import com.trapezium.util.ReturnInteger;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.ScriptFunction;
import com.trapezium.vrml.ScriptFileParsed;
import com.trapezium.vrml.RouteDestination;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.node.ScriptInstance;
import com.trapezium.parse.TokenEnumerator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 *  Static public methods about the VRML specification.
 *
 *  This class contains information about all VRML 2.0 nodes, fields, and
 *  default field values.  It has static public methods for accessing this
 *  information, and is used mainly by the parser.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 25 Feb 1998, added Table 7 base profile limits,
 *                   added MFTime support
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.1
 */
public class VRML97 {
    /** found an interface type that isn't field, exposedField, eventIn, or eventOut */
    static public int UnknownInterfaceType = 0;

    /** field interface constant */
    static public int field = 1;

    /** exposedField interface constant */
    static public int exposedField = 2;

    /** eventIn interface constant */
    static public int eventIn = 3;

    /** eventOut interface constant */
    static public int eventOut = 4;

	/** Convert an interface type constant into a String */
    static public String getInterfaceTypeStr( int itype ) {
        if ( itype == field ) {
            return( "field" );
        } else if ( itype == exposedField ) {
            return( "exposedField" );
        } else if ( itype == eventIn ) {
            return( "eventIn" );
        } else if ( itype == eventOut ) {
            return( "eventOut" );
        } else {
            return( "unknown interface type" );
        }
    }

    /** Constants representing built int field types */
    static public int UnknownType = 99;
	/** Built in VRML97 data type */
	static public int SFBool = 100;
	/** Built in VRML97 data type */
	static public int SFColor = 101;
	/** Built in VRML97 data type */
	static public int MFColor = 102;
	/** Built in VRML97 data type */
	static public int SFFloat = 103;
	/** Built in VRML97 data type */
	static public int MFFloat = 104;
	/** Built in VRML97 data type */
	static public int SFImage = 105;
	/** Built in VRML97 data type */
	static public int SFInt32 = 106;
	/** Built in VRML97 data type */
	static public int MFInt32 = 107;
	/** Built in VRML97 data type */
	static public int SFNode = 108;
	/** Built in VRML97 data type */
	static public int MFNode = 109;
	/** Built in VRML97 data type */
	static public int SFRotation = 110;
	/** Built in VRML97 data type */
	static public int MFRotation = 111;
	/** Built in VRML97 data type */
	static public int SFString = 112;
	/** Built in VRML97 data type */
	static public int MFString = 113;
	/** Built in VRML97 data type */
	static public int SFTime = 114;
	/** Built in VRML97 data type */
	static public int MFTime = 115;
	/** Built in VRML97 data type */
	static public int SFVec2f = 116;
	/** Built in VRML97 data type */
	static public int MFVec2f = 117;
	/** Built in VRML97 data type */
	static public int SFVec3f = 118;
	/** Built in VRML97 data type */
	static public int MFVec3f = 119;
	/** Vorlon data type indicating SFVec3f with only positive values */
	static public int PositiveSFVec3f = 120;
	/** Vorlon data type indicating bboxSize conventions, all positive or all -1 */
	static public int BboxSizeSFVec3f = 121;

	/** Convert a field data type constant into its corresponding String.
	 *
	 *  @param  fieldType a VRML97 field type constant, e.g. SFxxxx or MFxxxx,
	 *    or one of the Vorlon extensions.
	 */
	static public String getFieldTypeString( int fieldType ) {
	    switch( fieldType ) {
        case 100:
            return( "SFBool" );
        case 101:
            return( "SFColor" );
        case 102:
            return( "MFColor" );
        case 103:
            return( "SFFloat" );
        case 104:
            return( "MFFloat" );
        case 105:
            return( "SFImage" );
        case 106:
            return( "SFInt32" );
        case 107:
            return( "MFInt32" );
        case 108:
            return( "SFNode" );
        case 109:
            return( "MFNode" );
        case 110:
            return( "SFRotation" );
        case 111:
            return( "MFRotation" );
        case 112:
            return( "SFString" );
        case 113:
            return( "MFString" );
        case 114:
            return( "SFTime" );
        case 115:
            return( "MFTime" );
        case 116:
            return( "SFVec2f" );
        case 117:
            return( "MFVec2f" );
        case 118:
            return( "SFVec3f" );
        case 119:
            return( "MFVec3f" );
        case 120:
            return( "PositiveSFVec3f" );
        case 121:
            return( "BboxSizeSFVec3f" );
        default:
            return( null );
        }
    }

    static String[] bgUrlFields = {
        "backUrl", "bottomUrl", "frontUrl", "leftUrl", "rightUrl", "topUrl" };
    static Hashtable nodesWithUrls = new Hashtable();
    static {
        nodesWithUrls.put( "Anchor", "url" );
        nodesWithUrls.put( "AudioClip", "url" );
        nodesWithUrls.put( "Background", bgUrlFields );
        nodesWithUrls.put( "ImageTexture", "url" );
        nodesWithUrls.put( "Inline", "url" );
        nodesWithUrls.put( "MovieTexture", "url" );
        nodesWithUrls.put( "Script", "url" );
    }
    static public Object getUrlFieldList( String nodeName ) {
        return( nodesWithUrls.get( nodeName ));
    }
    
	/*
	    The following Hashtables have one Hashtable for each built in VRML
	    node.  Each node table has field names for its keys, and a FieldDescriptor
	    object for the corresponding value.  The FieldDescriptor contains the
	    interface type (field, exposedField, eventIn, eventOut), field type
	    (MFxxx, SFxxx, etc.), and initial value if any.

	    At the end of all this, there is a hash table called "builtInNodes",
	    with keys being node names, and key values being the Hashtable for
	    that node.
    */
	static Hashtable anchorFields = new Hashtable();
	static {
	    anchorFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    anchorFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    anchorFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    anchorFields.put( "description", new FieldDescriptor( exposedField, SFString, "" ));
	    anchorFields.put( "parameter", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.MFStringLimit, 1 ));
	    anchorFields.put( "url", new DEFuserFieldDescriptor( exposedField, MFString, "[]" ));
	    anchorFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    anchorFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	}
	static Hashtable appearanceFields = new Hashtable();
	static {
	    appearanceFields.put( "material", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    appearanceFields.put( "texture", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    appearanceFields.put( "textureTransform", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	}
	static Hashtable audioClipFields = new Hashtable();
	static {
	    audioClipFields.put( "description", new FieldDescriptor( exposedField, SFString, "" ));
	    audioClipFields.put( "loop", new FieldDescriptor( exposedField, SFBool, "FALSE" ));
	    audioClipFields.put( "pitch", new FieldDescriptor( exposedField, SFFloat, "1.0" ));
	    audioClipFields.put( "startTime", new FieldDescriptor( exposedField, SFTime, "0" ));
	    audioClipFields.put( "stopTime", new FieldDescriptor( exposedField, SFTime, "0" ));
	    audioClipFields.put( "url", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.UrlLimit, 1 ));
	    audioClipFields.put( "duration_changed", new FieldDescriptor( eventOut, SFTime ));
	    audioClipFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable backgroundFields = new Hashtable();
	static {
	    backgroundFields.put( "set_bind", new FieldDescriptor( eventIn, SFBool ));
	    backgroundFields.put( "groundAngle", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    backgroundFields.put( "groundColor", new FieldDescriptor( exposedField, MFColor, "[]" ));
	    backgroundFields.put( "backUrl", new FieldDescriptor( exposedField, MFString, "[]" ));
	    backgroundFields.put( "bottomUrl", new FieldDescriptor( exposedField, MFString, "[]" ));
	    backgroundFields.put( "frontUrl", new FieldDescriptor( exposedField, MFString, "[]" ));
	    backgroundFields.put( "leftUrl", new FieldDescriptor( exposedField, MFString, "[]" ));
	    backgroundFields.put( "rightUrl", new FieldDescriptor( exposedField, MFString, "[]" ));
	    backgroundFields.put( "topUrl", new FieldDescriptor( exposedField, MFString, "[]" ));
	    backgroundFields.put( "skyAngle", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    backgroundFields.put( "skyColor", new FieldDescriptor( exposedField, MFColor, "[ 0 0 0 ]" ));
	    backgroundFields.put( "isBound", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable billboardFields = new Hashtable();
	static {
	    billboardFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    billboardFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    billboardFields.put( "axisOfRotation", new FieldDescriptor( exposedField, SFVec3f, "0 1 0" ));
	    billboardFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    billboardFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    billboardFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	}
	static Hashtable boxFields = new Hashtable();
	static {
	    boxFields.put( "size", new FieldDescriptor( field, PositiveSFVec3f, "2 2 2" ));
	}
	static Hashtable collisionFields = new Hashtable();
	static {
	    collisionFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    collisionFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    collisionFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    collisionFields.put( "collide", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    collisionFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    collisionFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	    collisionFields.put( "proxy", new FieldDescriptor( field, SFNode, "NULL" ));
	    collisionFields.put( "collideTime", new FieldDescriptor( eventOut, SFTime ));
	}
	static Hashtable colorFields = new Hashtable();
	static {
	    colorFields.put( "color", new MFFieldDescriptor( exposedField, MFColor, "[]", Table7.MFColorLimit, 3 ));
	}
	static Hashtable colorInterpolatorFields = new Hashtable();
	static {
	    colorInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
	    colorInterpolatorFields.put( "key", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    colorInterpolatorFields.put( "keyValue", new FieldDescriptor( exposedField, MFColor, "[]" ));
	    colorInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, SFColor ));
	}
	static Hashtable coneFields = new Hashtable();
	static {
	    coneFields.put( "bottomRadius", new FieldDescriptor( field, SFFloat, "1" ));
	    coneFields.put( "height", new FieldDescriptor( field, SFFloat, "2" ));
	    coneFields.put( "side", new FieldDescriptor( field, SFBool, "TRUE" ));
	    coneFields.put( "bottom", new FieldDescriptor( field, SFBool, "TRUE" ));
	}
	static Hashtable coordinateFields = new Hashtable();
	static {
	    coordinateFields.put( "point", new MFFieldDescriptor( exposedField, MFVec3f, "[]", Table7.MFVec3fLimit, 3 ));
	}
	static Hashtable coordinateInterpolatorFields = new Hashtable();
	static {
	    coordinateInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
	    coordinateInterpolatorFields.put( "key", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    coordinateInterpolatorFields.put( "keyValue", new FieldDescriptor( exposedField, MFVec3f, "[]" ));
	    coordinateInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, MFVec3f ));
	}
	static Hashtable cylinderFields = new Hashtable();
	static {
	    cylinderFields.put( "bottom", new FieldDescriptor( field, SFBool, "TRUE" ));
	    cylinderFields.put( "height", new FieldDescriptor( field, SFFloat, "2" ));
	    cylinderFields.put( "radius", new FieldDescriptor( field, SFFloat, "1" ));
	    cylinderFields.put( "side", new FieldDescriptor( field, SFBool, "TRUE" ));
	    cylinderFields.put( "top", new FieldDescriptor( field, SFBool, "TRUE" ));
	}
	static Hashtable cylinderSensorFields = new Hashtable();
	static {
	    cylinderSensorFields.put( "autoOffset", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    cylinderSensorFields.put( "diskAngle", new FieldDescriptor( exposedField, SFFloat, "0.262" ));
	    cylinderSensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    cylinderSensorFields.put( "maxAngle", new FieldDescriptor( exposedField, SFFloat, "-1" ));
	    cylinderSensorFields.put( "minAngle", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    cylinderSensorFields.put( "offset", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    cylinderSensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	    cylinderSensorFields.put( "rotation_changed", new FieldDescriptor( eventOut, SFRotation ));
	    cylinderSensorFields.put( "trackPoint_changed", new FieldDescriptor( eventOut, SFVec3f ));
	}
	static Hashtable directionalLightFields = new Hashtable();
	static {
	    directionalLightFields.put( "ambientIntensity", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    directionalLightFields.put( "color", new FieldDescriptor( exposedField, SFColor, "1 1 1" ));
	    directionalLightFields.put( "direction", new FieldDescriptor( exposedField, SFVec3f, "0 0 -1" ));
	    directionalLightFields.put( "intensity", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    directionalLightFields.put( "on", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	}
	static Hashtable elevationGridFields = new Hashtable();
	static {
	    elevationGridFields.put( "set_height", new FieldDescriptor( eventIn, MFFloat ));
	    elevationGridFields.put( "color", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    elevationGridFields.put( "normal", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    elevationGridFields.put( "texCoord", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    elevationGridFields.put( "height", new MFFieldDescriptor( field, MFFloat, "[]", Table7.ElevationGridHeightLimit, 1 ));
	    elevationGridFields.put( "ccw", new FieldDescriptor( field, SFBool, "TRUE" ));
	    elevationGridFields.put( "colorPerVertex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    elevationGridFields.put( "creaseAngle", new FieldDescriptor( field, SFFloat, "0" ));
	    elevationGridFields.put( "normalPerVertex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    elevationGridFields.put( "solid", new FieldDescriptor( field, SFBool, "TRUE" ));
	    elevationGridFields.put( "xDimension", new FieldDescriptor( field, SFInt32, "0" ));
	    elevationGridFields.put( "xSpacing", new FieldDescriptor( field, SFFloat, "0.0" ));
	    elevationGridFields.put( "zDimension", new FieldDescriptor( field, SFInt32, "0" ));
	    elevationGridFields.put( "zSpacing", new FieldDescriptor( field, SFFloat, "0.0" ));
	}
	static Hashtable extrusionFields = new Hashtable();
	static {
	    extrusionFields.put( "set_crossSection", new FieldDescriptor( eventIn, MFVec2f ));
	    extrusionFields.put( "set_orientation", new FieldDescriptor( eventIn, MFRotation ));
	    extrusionFields.put( "set_scale", new FieldDescriptor( eventIn, MFVec2f ));
	    extrusionFields.put( "set_spine", new FieldDescriptor( eventIn, MFVec3f ));
	    extrusionFields.put( "beginCap", new FieldDescriptor( field, SFBool, "TRUE" ));
	    extrusionFields.put( "ccw", new FieldDescriptor( field, SFBool, "TRUE" ));
	    extrusionFields.put( "convex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    extrusionFields.put( "creaseAngle", new FieldDescriptor( field, SFFloat, "0" ));
	    extrusionFields.put( "crossSection", new FieldDescriptor( field, MFVec2f, "[ 1 1, 1 -1, -1 -1, -1 1, 1 1 ]" ));
	    extrusionFields.put( "endCap", new FieldDescriptor( field, SFBool, "TRUE" ));
	    extrusionFields.put( "orientation", new MFFieldDescriptor( field, MFRotation, "0 0 1 0", Table7.MFRotationLimit, 4 ));
	    extrusionFields.put( "scale", new FieldDescriptor( field, MFVec2f, "1 1" ));
	    extrusionFields.put( "solid", new FieldDescriptor( field, SFBool, "TRUE" ));
	    extrusionFields.put( "spine", new FieldDescriptor( field, MFVec3f, "[ 0 0 0, 0 1 0 ]" ));
	}
	static Hashtable fogFields = new Hashtable();
	static {
	    fogFields.put( "color", new FieldDescriptor( exposedField, SFColor, "1 1 1" ));
	    fogFields.put( "fogType", new FieldDescriptor( exposedField, SFString, "LINEAR" ));
	    fogFields.put( "visibilityRange", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    fogFields.put( "set_bind", new FieldDescriptor( eventIn, SFBool ));
	    fogFields.put( "isBound", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable fontStyleFields = new Hashtable();
	static {
	    fontStyleFields.put( "family", new FieldDescriptor( field, MFString, "\"SERIF\"" ));
	    fontStyleFields.put( "horizontal", new FieldDescriptor( field, SFBool, "TRUE" ));
	    fontStyleFields.put( "justify", new FieldDescriptor( field, MFString, "\"BEGIN\"" ));
	    fontStyleFields.put( "language", new FieldDescriptor( field, SFString, "\"\"" ));
	    fontStyleFields.put( "leftToRight", new FieldDescriptor( field, SFBool, "TRUE" ));
	    fontStyleFields.put( "size", new FieldDescriptor( field, SFFloat, "1.0" ));
	    fontStyleFields.put( "spacing", new FieldDescriptor( field, SFFloat, "1.0" ));
	    fontStyleFields.put( "style", new FieldDescriptor( field, SFString, "\"PLAIN\"" ));
	    fontStyleFields.put( "topToBottom", new FieldDescriptor( field, SFBool, "TRUE" ));
	}
	static Hashtable groupFields = new Hashtable();
	static {
	    groupFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    groupFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    groupFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    groupFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    groupFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	}
	static Hashtable imageTextureFields = new Hashtable();
	static {
	    imageTextureFields.put( "url", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.UrlLimit, 1 ));
	    imageTextureFields.put( "repeatS", new FieldDescriptor( field, SFBool, "TRUE" ));
	    imageTextureFields.put( "repeatT", new FieldDescriptor( field, SFBool, "TRUE" ));
	}
	static Hashtable indexedFaceSetFields = new Hashtable();
	static {
	    indexedFaceSetFields.put( "set_colorIndex", new FieldDescriptor( eventIn, MFInt32 ));
	    indexedFaceSetFields.put( "set_coordIndex", new FieldDescriptor( eventIn, MFInt32 ));
	    indexedFaceSetFields.put( "set_normalIndex", new FieldDescriptor( eventIn, MFInt32 ));
	    indexedFaceSetFields.put( "set_texCoordIndex", new FieldDescriptor( eventIn, MFInt32 ));
	    indexedFaceSetFields.put( "color", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    indexedFaceSetFields.put( "coord", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    indexedFaceSetFields.put( "normal", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    indexedFaceSetFields.put( "texCoord", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    indexedFaceSetFields.put( "ccw", new FieldDescriptor( field, SFBool, "TRUE" ));
	    indexedFaceSetFields.put( "colorIndex", new MFFieldDescriptor( field, MFInt32, "[]", Table7.MFInt32Limit, 1 ));
	    indexedFaceSetFields.put( "colorPerVertex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    indexedFaceSetFields.put( "convex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    indexedFaceSetFields.put( "coordIndex", new MFFieldDescriptor( field, MFInt32, "[]", Table7.MFInt32Limit, 1 ));
	    indexedFaceSetFields.put( "creaseAngle", new FieldDescriptor( field, SFFloat, "0" ));
	    indexedFaceSetFields.put( "normalIndex", new MFFieldDescriptor( field, MFInt32, "[]", Table7.MFInt32Limit, 1 ));
	    indexedFaceSetFields.put( "normalPerVertex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    indexedFaceSetFields.put( "solid", new FieldDescriptor( field, SFBool, "TRUE" ));
	    indexedFaceSetFields.put( "texCoordIndex", new MFFieldDescriptor( field, MFInt32, "[]", Table7.MFInt32Limit, 1 ));
	}
	static Hashtable indexedLineSetFields = new Hashtable();
	static {
	    indexedLineSetFields.put( "set_colorIndex", new FieldDescriptor( eventIn, MFInt32 ));
	    indexedLineSetFields.put( "set_coordIndex", new FieldDescriptor( eventIn, MFInt32 ));
	    indexedLineSetFields.put( "color", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    indexedLineSetFields.put( "coord", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    indexedLineSetFields.put( "colorIndex", new MFFieldDescriptor( field, MFInt32, "[]", Table7.ILSIndexLimit, 1 ));
	    indexedLineSetFields.put( "colorPerVertex", new FieldDescriptor( field, SFBool, "TRUE" ));
	    indexedLineSetFields.put( "coordIndex", new MFFieldDescriptor( field, MFInt32, "[]", Table7.ILSIndexLimit, 1 ));
	}
	static Hashtable inlineFields = new Hashtable();
	static {
	    inlineFields.put( "url", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.UrlLimit, 1 ));
	    inlineFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    inlineFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	}
	static Hashtable LODFields = new Hashtable();
	static {
	    LODFields.put( "level", new FieldDescriptor( exposedField, MFNode, "[]" ));
	    LODFields.put( "center", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    LODFields.put( "range", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	}
	static Hashtable materialFields = new Hashtable();
	static {
	    materialFields.put( "ambientIntensity", new FieldDescriptor( exposedField, SFFloat, "0.2" ));
	    materialFields.put( "diffuseColor", new FieldDescriptor( exposedField, SFColor, "0.8 0.8 0.8" ));
	    materialFields.put( "emissiveColor", new FieldDescriptor( exposedField, SFColor, "0 0 0" ));
	    materialFields.put( "shininess", new FieldDescriptor( exposedField, SFFloat, "0.2" ));
	    materialFields.put( "specularColor", new FieldDescriptor( exposedField, SFColor, "0 0 0" ));
	    materialFields.put( "transparency", new FieldDescriptor( exposedField, SFFloat, "0" ));
	}
	static Hashtable movieTextureFields = new Hashtable();
	static {
	    movieTextureFields.put( "loop", new FieldDescriptor( exposedField, SFBool, "FALSE" ));
	    movieTextureFields.put( "speed", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    movieTextureFields.put( "startTime", new FieldDescriptor( exposedField, SFTime, "0" ));
	    movieTextureFields.put( "stopTime", new FieldDescriptor( exposedField, SFTime, "0" ));
	    movieTextureFields.put( "url", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.UrlLimit, 1 ));
	    movieTextureFields.put( "repeatS", new FieldDescriptor( field, SFBool, "TRUE" ));
	    movieTextureFields.put( "repeatT", new FieldDescriptor( field, SFBool, "TRUE" ));
	    movieTextureFields.put( "duration_changed", new FieldDescriptor( eventOut, SFTime ));
	    movieTextureFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable navigationInfoFields = new Hashtable();
	static {
	    navigationInfoFields.put( "set_bind", new FieldDescriptor( eventIn, SFBool ));
	    navigationInfoFields.put( "avatarSize", new FieldDescriptor( exposedField, MFFloat, "[ 0.25, 1.6, 0.75 ]" ));
	    navigationInfoFields.put( "headlight", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    navigationInfoFields.put( "speed", new FieldDescriptor( exposedField, SFFloat, "1.0" ));
	    navigationInfoFields.put( "type", new FieldDescriptor( exposedField, MFString, "\"WALK\"" ));
	    navigationInfoFields.put( "visibilityLimit", new FieldDescriptor( exposedField, SFFloat, "0.0" ));
	    navigationInfoFields.put( "isBound", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable normalFields = new Hashtable();
	static {
	    normalFields.put( "vector", new MFFieldDescriptor( exposedField, MFVec3f, "[]", Table7.MFVec3fLimit, 3 ));
	}
	static Hashtable normalInterpolatorFields = new Hashtable();
	static {
	    normalInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
	    normalInterpolatorFields.put( "key", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    normalInterpolatorFields.put( "keyValue", new FieldDescriptor( exposedField, MFVec3f, "[]" ));
	    normalInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, MFVec3f ));
	}
	static Hashtable orientationInterpolatorFields = new Hashtable();
	static {
	    orientationInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
	    orientationInterpolatorFields.put( "key", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    orientationInterpolatorFields.put( "keyValue", new MFFieldDescriptor( exposedField, MFRotation, "[]", Table7.MFRotationLimit, 4 ));
	    orientationInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, SFRotation ));
	}
	static Hashtable pixelTextureFields = new Hashtable();
	static {
	    pixelTextureFields.put( "image", new FieldDescriptor( exposedField, SFImage, "0 0 0" ));
	    pixelTextureFields.put( "repeatS", new FieldDescriptor( field, SFBool, "TRUE" ));
	    pixelTextureFields.put( "repeatT", new FieldDescriptor( field, SFBool, "TRUE" ));
	}
	static Hashtable planeSensorFields = new Hashtable();
	static {
	    planeSensorFields.put( "autoOffset", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    planeSensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    planeSensorFields.put( "maxPosition", new FieldDescriptor( exposedField, SFVec2f, "-1 -1" ));
	    planeSensorFields.put( "minPosition", new FieldDescriptor( exposedField, SFVec2f, "0 0" ));
	    planeSensorFields.put( "offset", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    planeSensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	    planeSensorFields.put( "trackPoint_changed", new FieldDescriptor( eventOut, SFVec3f ));
	    planeSensorFields.put( "translation_changed", new FieldDescriptor( eventOut, SFVec3f ));
	}
	static Hashtable pointLightFields = new Hashtable();
	static {
	    pointLightFields.put( "ambientIntensity", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    pointLightFields.put( "attenuation", new FieldDescriptor( exposedField, SFVec3f, "1 0 0" ));
	    pointLightFields.put( "color", new FieldDescriptor( exposedField, SFColor, "1 1 1" ));
	    pointLightFields.put( "intensity", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    pointLightFields.put( "location", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    pointLightFields.put( "on", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    pointLightFields.put( "radius", new FieldDescriptor( exposedField, SFFloat, "100" ));
	}
	static Hashtable pointSetFields = new Hashtable();
	static {
	    pointSetFields.put( "color", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    pointSetFields.put( "coord", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	}
	static Hashtable positionInterpolatorFields = new Hashtable();
	static {
	    positionInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
	    positionInterpolatorFields.put( "key", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    positionInterpolatorFields.put( "keyValue", new MFFieldDescriptor( exposedField, MFVec3f, "[]", Table7.MFInterpolatorLimit, 3 ));
	    positionInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, SFVec3f ));
	}
	static Hashtable proximitySensorFields = new Hashtable();
	static {
	    proximitySensorFields.put( "center", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    proximitySensorFields.put( "size", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    proximitySensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    proximitySensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	    proximitySensorFields.put( "position_changed", new FieldDescriptor( eventOut, SFVec3f ));
	    proximitySensorFields.put( "orientation_changed", new FieldDescriptor( eventOut, SFRotation ));
	    proximitySensorFields.put( "enterTime", new FieldDescriptor( eventOut, SFTime ));
	    proximitySensorFields.put( "exitTime", new FieldDescriptor( eventOut, SFTime ));
	}
	static Hashtable scalarInterpolatorFields = new Hashtable();
	static {
	    scalarInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
	    scalarInterpolatorFields.put( "key", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
	    scalarInterpolatorFields.put( "keyValue", new FieldDescriptor( exposedField, MFFloat, "[]" ));
	    scalarInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, SFFloat ));
	}
	static Hashtable scriptFields = new Hashtable();
	static {
	    scriptFields.put( "url", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.UrlLimit, 1 ));
	    scriptFields.put( "directOutput", new FieldDescriptor( field, SFBool, "FALSE" ));
	    scriptFields.put( "mustEvaluate", new FieldDescriptor( field, SFBool, "FALSE" ));
	}
	static Hashtable shapeFields = new Hashtable();
	static {
	    shapeFields.put( "appearance", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    shapeFields.put( "geometry", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	}
	static Hashtable soundFields = new Hashtable();
	static {
	    soundFields.put( "direction", new FieldDescriptor( exposedField, SFVec3f, "0 0 1" ));
	    soundFields.put( "intensity", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    soundFields.put( "location", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    soundFields.put( "maxBack", new FieldDescriptor( exposedField, SFFloat, "10" ));
	    soundFields.put( "maxFront", new FieldDescriptor( exposedField, SFFloat, "10" ));
	    soundFields.put( "minBack", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    soundFields.put( "minFront", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    soundFields.put( "priority", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    soundFields.put( "source", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    soundFields.put( "spatialize", new FieldDescriptor( field, SFBool, "TRUE" ));
	}
	static Hashtable sphereFields = new Hashtable();
	static {
	    sphereFields.put( "radius", new FieldDescriptor( field, SFFloat, "1" ));
	}
	static Hashtable sphereSensorFields = new Hashtable();
	static {
	    sphereSensorFields.put( "autoOffset", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    sphereSensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    sphereSensorFields.put( "offset", new FieldDescriptor( exposedField, SFRotation, "0 1 0 0" ));
	    sphereSensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	    sphereSensorFields.put( "rotation_changed", new FieldDescriptor( eventOut, SFRotation ));
	    sphereSensorFields.put( "trackPoint_changed", new FieldDescriptor( eventOut, SFVec3f ));
	}
	static Hashtable spotLightFields = new Hashtable();
	static {
	    spotLightFields.put( "ambientIntensity", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    spotLightFields.put( "attenuation", new FieldDescriptor( exposedField, SFVec3f, "1 0 0" ));
	    spotLightFields.put( "beamWidth", new FieldDescriptor( exposedField, SFFloat, "1.570796" ));
	    spotLightFields.put( "color", new FieldDescriptor( exposedField, SFColor, "1 1 1" ));
	    spotLightFields.put( "cutOffAngle", new FieldDescriptor( exposedField, SFFloat, "0.785398" ));
	    spotLightFields.put( "direction", new FieldDescriptor( exposedField, SFVec3f, "0 0 -1" ));
	    spotLightFields.put( "intensity", new FieldDescriptor( exposedField, SFFloat, "1" ));
	    spotLightFields.put( "location", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    spotLightFields.put( "on", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    spotLightFields.put( "radius", new FieldDescriptor( exposedField, SFFloat, "100" ));
	}
	static Hashtable switchFields = new Hashtable();
	static {
	    switchFields.put( "choice", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    switchFields.put( "whichChoice", new FieldDescriptor( exposedField, SFInt32, "-1" ));
	}
	static Hashtable textFields = new Hashtable();
	static {
	    textFields.put( "string", new MFFieldDescriptor( exposedField, MFString, "[]", Table7.TextStringLimit, 1 ));
	    textFields.put( "fontStyle", new FieldDescriptor( exposedField, SFNode, "NULL" ));
	    textFields.put( "length", new FieldDescriptor( exposedField, MFFloat, "[]" ));
	    textFields.put( "maxExtent", new FieldDescriptor( exposedField, SFFloat, "0.0" ));
	}
	static Hashtable textureCoordinateFields = new Hashtable();
	static {
	    textureCoordinateFields.put( "point", new MFFieldDescriptor( exposedField, MFVec2f, "[]", Table7.MFVec2fLimit, 2 ));
	}
	static Hashtable textureTransformFields = new Hashtable();
	static {
	    textureTransformFields.put( "center", new FieldDescriptor( exposedField, SFVec2f, "0 0" ));
	    textureTransformFields.put( "rotation", new FieldDescriptor( exposedField, SFFloat, "0" ));
	    textureTransformFields.put( "scale", new FieldDescriptor( exposedField, SFVec2f, "1 1" ));
	    textureTransformFields.put( "translation", new FieldDescriptor( exposedField, SFVec2f, "0 0" ));
	}
	static Hashtable timeSensorFields = new Hashtable();
	static {
	    timeSensorFields.put( "cycleInterval", new FieldDescriptor( exposedField, SFTime, "1" ));
	    timeSensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    timeSensorFields.put( "loop", new FieldDescriptor( exposedField, SFBool, "FALSE" ));
	    timeSensorFields.put( "startTime", new FieldDescriptor( exposedField, SFTime, "0" ));
	    timeSensorFields.put( "stopTime", new FieldDescriptor( exposedField, SFTime, "0" ));
	    timeSensorFields.put( "cycleTime", new FieldDescriptor( eventOut, SFTime ));
	    timeSensorFields.put( "fraction_changed", new FieldDescriptor( eventOut, SFFloat ));
	    timeSensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	    timeSensorFields.put( "time", new FieldDescriptor( eventOut, SFTime ));
	}
	static Hashtable touchSensorFields = new Hashtable();
	static {
	    touchSensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    touchSensorFields.put( "hitNormal_changed", new FieldDescriptor( eventOut, SFVec3f ));
	    touchSensorFields.put( "hitPoint_changed", new FieldDescriptor( eventOut, SFVec3f ));
	    touchSensorFields.put( "hitTexCoord_changed", new FieldDescriptor( eventOut, SFVec2f ));
	    touchSensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	    touchSensorFields.put( "isOver", new FieldDescriptor( eventOut, SFBool ));
	    touchSensorFields.put( "touchTime", new FieldDescriptor( eventOut, SFTime ));
	}
	static Hashtable transformFields = new Hashtable();
	static {
	    transformFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    transformFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    transformFields.put( "center", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    transformFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]",  Table7.MFNodeLimit, 1 ));
	    transformFields.put( "rotation", new FieldDescriptor( exposedField, SFRotation, "0 0 1 0" ));
	    transformFields.put( "scale", new FieldDescriptor( exposedField, PositiveSFVec3f, "1 1 1" ));
	    transformFields.put( "scaleOrientation", new FieldDescriptor( exposedField, SFRotation, "0 0 1 0" ));
	    transformFields.put( "translation", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    transformFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    transformFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	}
	static Hashtable viewpointFields = new Hashtable();
	static {
	    viewpointFields.put( "set_bind", new FieldDescriptor( eventIn, SFBool ));
	    viewpointFields.put( "fieldOfView", new FieldDescriptor( exposedField, SFFloat, "0.785398" ));
	    viewpointFields.put( "jump", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    viewpointFields.put( "orientation", new FieldDescriptor( exposedField, SFRotation, "0 0 1 0" ));
	    viewpointFields.put( "position", new FieldDescriptor( exposedField, SFVec3f, "0 0 10" ));
	    viewpointFields.put( "description", new FieldDescriptor( field, SFString, "\"\"" ));
	    viewpointFields.put( "bindTime", new FieldDescriptor( eventOut, SFTime ));
	    viewpointFields.put( "isBound", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable visibilitySensorFields = new Hashtable();
	static {
	    visibilitySensorFields.put( "center", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    visibilitySensorFields.put( "enabled", new FieldDescriptor( exposedField, SFBool, "TRUE" ));
	    visibilitySensorFields.put( "size", new FieldDescriptor( exposedField, SFVec3f, "0 0 0" ));
	    visibilitySensorFields.put( "enterTime", new FieldDescriptor( eventOut, SFTime ));
	    visibilitySensorFields.put( "exitTime", new FieldDescriptor( eventOut, SFTime ));
	    visibilitySensorFields.put( "isActive", new FieldDescriptor( eventOut, SFBool ));
	}
	static Hashtable worldInfoFields = new Hashtable();
	static {
	    worldInfoFields.put( "info", new MFFieldDescriptor( field, MFString, "[]", Table7.MFStringLimit, 1 ));
	    worldInfoFields.put( "title", new FieldDescriptor( field, SFString, "\"\"" ));
	}

    // blaxxun nurbs specific proposed additions
    static Hashtable nurbsSurfaceFields = new Hashtable();
    static {
        nurbsSurfaceFields.put( "uDimension", new FieldDescriptor( field, SFInt32, "0" ));
        nurbsSurfaceFields.put( "vDimension", new FieldDescriptor( field, SFInt32, "0" ));
        nurbsSurfaceFields.put( "uKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsSurfaceFields.put( "vKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsSurfaceFields.put( "uOrder", new FieldDescriptor( field, SFInt32, "3" ));
        nurbsSurfaceFields.put( "vOrder", new FieldDescriptor( field, SFInt32, "3" ));
        nurbsSurfaceFields.put( "controlPoint", new MFFieldDescriptor( exposedField, MFVec3f, "[]", Table7.MFVec3fLimit, 3 ));
        nurbsSurfaceFields.put( "weight", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsSurfaceFields.put( "uTessellation", new FieldDescriptor( exposedField, SFInt32, "0" ));
        nurbsSurfaceFields.put( "vTessellation", new FieldDescriptor( exposedField, SFInt32, "0" ));
        nurbsSurfaceFields.put( "texCoord", new FieldDescriptor( exposedField, SFNode, "NULL" ));
        nurbsSurfaceFields.put( "ccw", new FieldDescriptor( field, SFBool, "TRUE" ));
        nurbsSurfaceFields.put( "solid", new FieldDescriptor( field, SFBool, "TRUE" ));
    }

    static Hashtable nurbsGroupFields = new Hashtable();
    static {
        nurbsGroupFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    nurbsGroupFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    nurbsGroupFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    nurbsGroupFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    nurbsGroupFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
	    nurbsGroupFields.put( "tessellationScale", new FieldDescriptor( exposedField, SFFloat, "1.0" ));
	}
	
	static Hashtable nurbsTextureSurfaceFields = new Hashtable();
	static {
	    nurbsTextureSurfaceFields.put( "uDimension", new FieldDescriptor( field, SFInt32, "0" ));
	    nurbsTextureSurfaceFields.put( "vDimension", new FieldDescriptor( field, SFInt32, "0" ));
        nurbsTextureSurfaceFields.put( "uKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsTextureSurfaceFields.put( "vKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsTextureSurfaceFields.put( "uOrder", new FieldDescriptor( field, SFInt32, "3" ));
        nurbsTextureSurfaceFields.put( "vOrder", new FieldDescriptor( field, SFInt32, "3" ));
        nurbsTextureSurfaceFields.put( "controlPoint", new MFFieldDescriptor( exposedField, MFVec2f, "[]", Table7.MFVec2fLimit, 3 ));
        nurbsTextureSurfaceFields.put( "weight", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
    }

    static Hashtable nurbsCurve2DFields = new Hashtable();
    static {
        nurbsCurve2DFields.put( "dimension", new FieldDescriptor( field, SFInt32, "0" ));
        nurbsCurve2DFields.put( "knot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsCurve2DFields.put( "order", new FieldDescriptor( field, SFInt32, "3" ));        
        nurbsCurve2DFields.put( "controlPoint", new MFFieldDescriptor( exposedField, MFVec2f, "[]", Table7.MFVec2fLimit, 3 ));
        nurbsCurve2DFields.put( "weight", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsCurve2DFields.put( "tessellation", new FieldDescriptor( exposedField, SFInt32, "0" ));
    }
    
    static Hashtable trimmedSurfaceFields = new Hashtable();
    static {
        trimmedSurfaceFields.put( "addTrimmingCurves", new FieldDescriptor( eventIn, MFNode ));
        trimmedSurfaceFields.put( "removeTrimmingCurves", new FieldDescriptor( eventIn, MFNode ));
        trimmedSurfaceFields.put( "trimmingCurves", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
        trimmedSurfaceFields.put( "surface", new FieldDescriptor( exposedField, SFNode, "NULL" ));
    }
    
    static Hashtable nurbsPositionInterpolatorFields = new Hashtable();
    static {
        nurbsPositionInterpolatorFields.put( "set_fraction", new FieldDescriptor( eventIn, SFFloat ));
        nurbsPositionInterpolatorFields.put( "dimension", new FieldDescriptor( exposedField, SFInt32, "0" ));
        nurbsPositionInterpolatorFields.put( "knot", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsPositionInterpolatorFields.put( "order", new FieldDescriptor( exposedField, SFInt32, "4" ));
        nurbsPositionInterpolatorFields.put( "keyValue", new MFFieldDescriptor( exposedField, MFVec3f, "[]", Table7.MFVec3fLimit, 3 ));
        nurbsPositionInterpolatorFields.put( "keyWeight", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        nurbsPositionInterpolatorFields.put( "value_changed", new FieldDescriptor( eventOut, SFVec3f ));
    }
    
    static Hashtable coordinateDeformerFields = new Hashtable();
    static {
        coordinateDeformerFields.put( "addChildren", new FieldDescriptor( eventIn, MFNode ));
	    coordinateDeformerFields.put( "removeChildren", new FieldDescriptor( eventIn, MFNode ));
	    coordinateDeformerFields.put( "children", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
	    coordinateDeformerFields.put( "bboxCenter", new FieldDescriptor( field, SFVec3f, "0 0 0" ));
	    coordinateDeformerFields.put( "bboxSize", new FieldDescriptor( field, BboxSizeSFVec3f, "-1 -1 -1" ));
        coordinateDeformerFields.put( "uDimension", new FieldDescriptor( field, SFInt32, "0" ));
        coordinateDeformerFields.put( "vDimension", new FieldDescriptor( field, SFInt32, "0" ));
        coordinateDeformerFields.put( "wDimension", new FieldDescriptor( field, SFInt32, "0" ));
        coordinateDeformerFields.put( "uKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        coordinateDeformerFields.put( "vKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        coordinateDeformerFields.put( "wKnot", new MFFieldDescriptor( field, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        coordinateDeformerFields.put( "uOrder", new FieldDescriptor( field, SFInt32, "2" ));
        coordinateDeformerFields.put( "vOrder", new FieldDescriptor( field, SFInt32, "2" ));
        coordinateDeformerFields.put( "wOrder", new FieldDescriptor( field, SFInt32, "2" ));
        coordinateDeformerFields.put( "controlPoint", new MFFieldDescriptor( exposedField, MFVec3f, "[]", Table7.MFVec3fLimit, 3 ));
        coordinateDeformerFields.put( "weight", new MFFieldDescriptor( exposedField, MFFloat, "[]", Table7.MFFloatLimit, 1 ));
        coordinateDeformerFields.put( "inputCoord", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
        coordinateDeformerFields.put( "inputTransform", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
        coordinateDeformerFields.put( "outputCoord", new MFFieldDescriptor( exposedField, MFNode, "[]", Table7.MFNodeLimit, 1 ));
    }

	static Hashtable builtInNodes = new Hashtable();
	static {
		builtInNodes.put( "Anchor", anchorFields );
		builtInNodes.put( "Appearance", appearanceFields );
		builtInNodes.put( "AudioClip", audioClipFields );
		builtInNodes.put( "Background", backgroundFields );
		builtInNodes.put( "Billboard", billboardFields );
   		builtInNodes.put( "Box", boxFields );
		builtInNodes.put( "Collision", collisionFields );
		builtInNodes.put( "Color", colorFields );
		builtInNodes.put( "ColorInterpolator", colorInterpolatorFields );
		builtInNodes.put( "Cone", coneFields );
		builtInNodes.put( "Coordinate", coordinateFields );
		builtInNodes.put( "CoordinateInterpolator", coordinateInterpolatorFields );
		builtInNodes.put( "Cylinder", cylinderFields );
		builtInNodes.put( "CylinderSensor", cylinderSensorFields );
		builtInNodes.put( "DirectionalLight", directionalLightFields );
		builtInNodes.put( "ElevationGrid", elevationGridFields );
		builtInNodes.put( "Extrusion", extrusionFields );
		builtInNodes.put( "Fog", fogFields );
		builtInNodes.put( "FontStyle", fontStyleFields );
		builtInNodes.put( "Group", groupFields );
		builtInNodes.put( "ImageTexture", imageTextureFields );
		builtInNodes.put( "IndexedFaceSet", indexedFaceSetFields );
		builtInNodes.put( "IndexedLineSet", indexedLineSetFields );
		builtInNodes.put( "Inline", inlineFields );
		builtInNodes.put( "LOD", LODFields );
		builtInNodes.put( "Material", materialFields );
		builtInNodes.put( "MovieTexture", movieTextureFields );
		builtInNodes.put( "NavigationInfo", navigationInfoFields );
		builtInNodes.put( "Normal", normalFields );
		builtInNodes.put( "NormalInterpolator", normalInterpolatorFields );
		builtInNodes.put( "OrientationInterpolator", orientationInterpolatorFields );
		builtInNodes.put( "PixelTexture", pixelTextureFields );
		builtInNodes.put( "PlaneSensor", planeSensorFields );
		builtInNodes.put( "PointLight", pointLightFields );
		builtInNodes.put( "PointSet", pointSetFields );
		builtInNodes.put( "PositionInterpolator", positionInterpolatorFields );
		builtInNodes.put( "ProximitySensor", proximitySensorFields );
		builtInNodes.put( "ScalarInterpolator", scalarInterpolatorFields );
		builtInNodes.put( "Script", scriptFields );
		builtInNodes.put( "Shape", shapeFields );
		builtInNodes.put( "Sound", soundFields );
		builtInNodes.put( "Sphere", sphereFields );
		builtInNodes.put( "SphereSensor", sphereSensorFields );
		builtInNodes.put( "SpotLight", spotLightFields );
		builtInNodes.put( "Switch", switchFields );
		builtInNodes.put( "Text", textFields );
		builtInNodes.put( "TextureCoordinate", textureCoordinateFields );
		builtInNodes.put( "TextureTransform", textureTransformFields );
		builtInNodes.put( "TimeSensor", timeSensorFields );
		builtInNodes.put( "TouchSensor", touchSensorFields );
		builtInNodes.put( "Transform", transformFields );
		builtInNodes.put( "Viewpoint", viewpointFields );
		builtInNodes.put( "VisibilitySensor", visibilitySensorFields );
		builtInNodes.put( "WorldInfo", worldInfoFields );
	}
		
	static boolean nurbsAreEnabled = false;
	static public void enableNurbs() {
		// blaxxun nurbs proposals
		if ( !nurbsAreEnabled ) {
    		builtInNodes.put( "NurbsSurface", nurbsSurfaceFields );
    		builtInNodes.put( "NurbsGroup", nurbsGroupFields );
    		builtInNodes.put( "NurbsTextureSurface", nurbsTextureSurfaceFields );
    		builtInNodes.put( "NurbsCurve2D", nurbsCurve2DFields );
    		builtInNodes.put( "TrimmedSurface", trimmedSurfaceFields );
    		builtInNodes.put( "NurbsPositionInterpolator", nurbsPositionInterpolatorFields );
    		builtInNodes.put( "CoordinateDeformer", coordinateDeformerFields );
    		nurbsAreEnabled = true;
    	}
	}
	
	static public void disableNurbs() {
	    if ( nurbsAreEnabled ) {
	        builtInNodes.remove( "NurbsSurface" );
	        builtInNodes.remove( "NurbsGroup" );
	        builtInNodes.remove( "NurbsTextureSurface" );
	        builtInNodes.remove( "NurbsCurve2D" );
	        builtInNodes.remove( "TrimmedSurface" );
	        builtInNodes.remove( "NurbsPositionInterpolator" );
	        builtInNodes.remove( "CoordinateDeformer" );
	        nurbsAreEnabled = false;
	    }
	}
	
	static public boolean isNurbsEnabled() {
	    return( nurbsAreEnabled );
	}

	static public Hashtable builtInTypes = new Hashtable();
	static {
	    builtInTypes.put( "SFBool", new Integer( SFBool ));
	    builtInTypes.put( "SFColor", new Integer( SFColor ));
	    builtInTypes.put( "MFColor", new Integer( MFColor ));
	    builtInTypes.put( "SFFloat", new Integer( SFFloat ));
	    builtInTypes.put( "MFFloat", new Integer( MFFloat ));
	    builtInTypes.put( "SFImage", new Integer( SFImage ));
	    builtInTypes.put( "SFInt32", new Integer( SFInt32 ));
	    builtInTypes.put( "MFInt32", new Integer( MFInt32 ));
	    builtInTypes.put( "SFNode", new Integer( SFNode ));
	    builtInTypes.put( "MFNode", new Integer( MFNode ));
	    builtInTypes.put( "SFRotation", new Integer( SFRotation ));
	    builtInTypes.put( "MFRotation", new Integer( MFRotation ));
	    builtInTypes.put( "SFString", new Integer( SFString ));
	    builtInTypes.put( "MFString", new Integer( MFString ));
	    builtInTypes.put( "SFTime", new Integer( SFTime ));
	    builtInTypes.put( "MFTime", new Integer( MFTime ));
	    builtInTypes.put( "SFVec2f", new Integer( SFVec2f ));
	    builtInTypes.put( "MFVec2f", new Integer( MFVec2f ));
	    builtInTypes.put( "SFVec3f", new Integer( SFVec3f ));
	    builtInTypes.put( "MFVec3f", new Integer( MFVec3f ));
    }
    static public int typeStrToInt( String typeStr ) {
        Integer i = (Integer)builtInTypes.get( typeStr );
        if ( i != null ) {
            return( i.intValue() );
        } else {
            return( -1 );
        }
    }

    /** Some fields are only allowed to contain specific types. */
    static Hashtable actualTypesByField = new Hashtable();
    static {
        actualTypesByField.put( "Collision_proxy", new Integer( NodeType.GoodChildNode ));
		actualTypesByField.put( "ElevationGrid_color", new Integer( NodeType.Color ));
		actualTypesByField.put( "ElevationGrid_normal", new Integer( NodeType.Normal ));
		actualTypesByField.put( "ElevationGrid_texCoord", new Integer( NodeType.TextureCoordinate ));
		actualTypesByField.put( "IndexedFaceSet_color", new Integer( NodeType.Color ));
		actualTypesByField.put( "IndexedFaceSet_coord", new Integer( NodeType.Coordinate ));
		actualTypesByField.put( "IndexedFaceSet_normal", new Integer( NodeType.Normal ));
		actualTypesByField.put( "IndexedFaceSet_texCoord", new Integer( NodeType.TextureCoordinate ));
		actualTypesByField.put( "IndexedLineSet_color", new Integer( NodeType.Color ));
		actualTypesByField.put( "IndexedLineSet_coord", new Integer( NodeType.Coordinate ));
		actualTypesByField.put( "PointSet_color", new Integer( NodeType.Color ));
		actualTypesByField.put( "PointSet_coord", new Integer( NodeType.Coordinate ));
		actualTypesByField.put( "Text_fontStyle", new Integer( NodeType.FontStyle ));
		actualTypesByField.put( "Shape_appearance", new Integer( NodeType.Appearance ));
		actualTypesByField.put( "Shape_geometry", new Integer( NodeType.Geometry ));
		actualTypesByField.put( "Sound_source", new Integer( NodeType.SoundSource ));
		actualTypesByField.put( "Appearance_material", new Integer( NodeType.Material ));
		actualTypesByField.put( "Appearance_texture", new Integer( NodeType.Texture ));
		actualTypesByField.put( "Appearance_textureTransform", new Integer( NodeType.TextureTransform ));
		
		// nurbs additions
		actualTypesByField.put( "CoordinateDeformer_inputCoord", new Integer( NodeType.CoordListNode ));
		actualTypesByField.put( "CoordinateDeformer_outputCoord", new Integer( NodeType.CoordListNode ));
		actualTypesByField.put( "CoordinateDeformer_inputTransform", new Integer( NodeType.TransformListNode ));
	}

    /** Does a field restrict its values to a specific type?
     *
     *  @param  node   the node to check
     *  @param  field  the field to check
     *  @param  result if this method returns true, this output parameter
     *                 contains the type that ths value is limited to.  NodeType.java
     *                 includes the 
     *
     *  @return  true if the node/field refers to a Node that can only be a specific type.
     */
	static public boolean fieldHasType( String node, String field, ReturnInteger result ) {
	    String nodeField = node + "_" + field;
	    Integer x = (Integer)actualTypesByField.get( nodeField );
	    if ( x != null ) {
	        result.setValue( x.intValue() );
	        return( true );
	    } else {
	        return( false );
	    }
	}

	/** create a node by name */
	static public Node NodeFactory( String nodeName ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException {
		if ( nodeName.compareTo( "NULL" ) == 0 ) {
			return( null );
		}
		Class nodeClass = null;
		try {
			nodeClass = Class.forName("com.trapezium.vrml.node.generated." + nodeName);
		} catch ( Exception e ) {
			throw new ClassNotFoundException();
		}
		if (nodeClass != null) {
			Node node =  (Node) (nodeClass.newInstance());
			return node;
		}
        return null;
    }

    /** is the node part of the VRML97 spec? */
	static public boolean isBuiltInNode( String nodeName ) {
	    return( builtInNodes.get( nodeName ) != null );
	}

	/** is the type part of the VRML97 spec? */
	static public boolean isBuiltInType( String typeName ) {
	    return( builtInTypes.get( typeName ) != null );
	}

	/** Is the node & field combination valid?
	 *
	 *  @param nodeName the name of a built in VRML 97 node
	 *  @param fieldId the name of a field
	 *
	 *  @return  true if the node has a field with the given name, otherwise false.
	 *    Also returns false if the node name is invalid.
	 */
	static public boolean isValidFieldId( String nodeName, String fieldId ) {
	    if ( nodeName == null ) {
	        return( false );
	    }
	    Hashtable fieldTable = (Hashtable)builtInNodes.get( nodeName );
	    if ( fieldTable != null ) {
	        return( fieldTable.get( fieldId ) != null );
	    } else {
	        return( false );
	    }
	}
	
	/** Get the field data type constant associated with a node & field.
	 *
	 *  @return the constant indicating the VRML97 data type, 
	 *    VRML97.SFxxxxx, VRML97.MFxxxxxx, or UnknownType.
	 */
	static public int getFieldType( String nodeName, String fieldId ) {
	    if ( nodeName == null ) {
	        return( UnknownType );
	    }
	    Hashtable fieldTable = (Hashtable)builtInNodes.get( nodeName );
	    if ( fieldTable != null ) {
	        FieldDescriptor fd = (FieldDescriptor)fieldTable.get( fieldId );
	        if ( fd != null ) {
	            return( fd.getFieldType() );
	        }
	    }
	    return( UnknownType );
	}

	/** Get the field type string for a node and field */
	static public String getFieldTypeString( String nodeName, String fieldId ) {
	    int fieldType = getFieldType( nodeName, fieldId );
	    return( getFieldTypeString( fieldType ));
	}

	/** Get the interface type constant for a node and field.
	 *
	 *  @return  VRML97.eventIn if the nodeName/fieldId indicates an eventIn,
	 *    VRML97.eventOut if the nodeName/fieldId indicates an eventOut,
	 *    VRML97.exposedField if the nodeName/fieldId indicates an exposedField,
	 *    VRML97.field if the nodeName/fieldId indicates a field.
	 *    In the case of exposedFields, there are implicitly defined 
	 *    eventIns and eventOuts.  If the nodeName/fieldId combination is
	 *    not found, a check is made for one of these implicitly defined
	 *    fields, and if one is found to correspond to an exposedField,
	 *    (which occurs if it has a prefix of "set_" or suffix of "_changed")
	 *    then VRML97.eventIn or VRML97.eventOut is returned.
	 */
	static public int getInterfaceType( String nodeName, String fieldId ) {
	    Hashtable fieldTable = (Hashtable)builtInNodes.get( nodeName );
	    if ( fieldTable != null ) {
	        FieldDescriptor fd = (FieldDescriptor)fieldTable.get( fieldId );
	        if ( fd == null ) {
	            if ( fieldId.indexOf( "set_" ) == 0 ) {
	                String nameWithoutPrefix = fieldId.substring( 4 );
	                fd = (FieldDescriptor)fieldTable.get( nameWithoutPrefix );
	                if ( fd != null ) {
	                    if ( fd.getDeclarationType() == exposedField ) {
	                        return( eventIn );
    	                }
    	            }
	            } else if ( fieldId.indexOf( "_changed" ) > 0 ) {
	                String nameWithoutSuffix = fieldId.substring( 0, fieldId.indexOf( "_changed" ));
	                fd = (FieldDescriptor)fieldTable.get( nameWithoutSuffix );
	                if ( fd != null ) {
    	                if ( fd.getDeclarationType() == exposedField ) {
    	                    return( eventOut );
    	                }
    	            }
	            }
	        } else {
	            return( fd.getDeclarationType() );
	        }
	    }
	    return( UnknownInterfaceType );
	}

    /** Get the default boolean value for a node and field.
	 *
	 *  @return  <B>true</B> if the nodeName/fieldId indicate a boolean field,
	 *    and the default value of that field is TRUE.  <B>false</B> if the
	 *    nodeName/fieldId indicate a boolean field, and the default value
	 *    of that field is FALSE.  <B>false</B> if the nodeName/fieldId do not
	 *    indicate a boolean field, or do not indicate a valid field.
	 */
	static public boolean getDefaultBoolValue( String nodeName, String fieldId ) {
	    Hashtable fieldTable = (Hashtable)builtInNodes.get( nodeName );
	    if ( fieldTable != null ) {
	        FieldDescriptor fd = (FieldDescriptor)fieldTable.get( fieldId );
	        if ( fd != null ) {
	            return( fd.getDefaultBoolValue() );
	        }
	    }
	    return( false );
	}

	/**
	 *  get the FieldDescriptor associated with a node and field id
	 *
	 *  @param  nodeName  name of the node containing the field
	 *  @param  fieldId   name of the field
	 *
	 *  @return a FieldDescriptor for the nodeName/fieldId combination, or null
	 *          if that combination indicates an unknown field.
	 */
	static public FieldDescriptor getFieldDescriptor( String nodeName, String fieldId ) {
	    Hashtable fieldTable = (Hashtable)builtInNodes.get( nodeName );
	    if ( fieldTable != null ) {
	        return( (FieldDescriptor)fieldTable.get( fieldId ));
	    } else {
	        return( null );
	    }
	}

	/** Get a Hashtable with a FieldDescriptor entry for each field for a node */
	static public Hashtable getFieldTable( String nodeName ) {
	    return( (Hashtable)builtInNodes.get( nodeName ));
	}

	
	/**
	 *  Is a field value equivalent to the default field value?
	 *
	 *  @param   nodeName   the node containing the field
	 *  @param   fieldId    the field Id within the node
	 *  @param   firstTokenOffset  the first token of the field value
	 *  @param   lastTokenOffset   the last token of the field value
	 *
	 *  @return  <B>true</B> if the field value is equaivalent to the default field value.
	 *           Field values are equivalent if their token text is identical, or
	 *           if their numeric values are equal (i.e. 0.0 == 0.0000).
	 */
	static public boolean fieldIsDefault( String nodeName, String fieldId,
	    int firstTokenOffset, int lastTokenOffset, TokenEnumerator v ) {
	    if ( nodeName == null ) {
	        return( false );
	    }
	    // get the hash table of default field values for the node
	    Hashtable fieldTable = (Hashtable)builtInNodes.get( nodeName );
	    if ( fieldTable != null ) {
	        // get the descriptor for the particular field
	        FieldDescriptor fieldData = (FieldDescriptor)fieldTable.get( fieldId );
	        if ( fieldData != null ) {
	            // get the token enumerator containing the default value
	            TokenEnumerator te = fieldData.getTokenEnumerator();
	            if ( te == null ) {
	                return( false );
	            }
        		int state = v.getState();
        		int scannerOffset = firstTokenOffset;
        		v.setState( scannerOffset );
        		te.setState( -1 );
        		// compare token by token
        		while ( te.hasMoreElements() ) {
        			int test = te.getNextToken();
        			if ( test == -1 ) {
        				break;
        			}
        			if ( te.sameAs( v )) {
        				if ( scannerOffset == lastTokenOffset ) {
        					v.setState( state );
        					return( true );
        				}
        			} else if ( te.isNumber( test ) && v.isNumber( scannerOffset )) {
        			    try {
            				if ( te.getFloat( test ) == v.getFloat( scannerOffset )) {
            					if ( scannerOffset == lastTokenOffset ) {
            						v.setState( state );
            						return( true );
            					}
            				} else {
            					break;
            				}
            			} catch ( Exception e ) { // assume NumberFormatException
            			    return( false );
            			}
        			} else {
        				break;
        			}
        			scannerOffset = v.getNextToken();
        		}
                v.setState( state );
        		return( false );
        	}
       	}
       	return( false );
    }

	/** get the node String that most closely matches the unknown node string
	 *
	 *  @param  nodeType    an unknown node type
	 *  @param  matchScore  an integer indicating how closely the return value matches
	 *
	 *  @return a String indicating a valid node, that is a close match with the
	 *          unknown node type input parameter.  If none is close enough, returns null.
	 */
	static public String getClosestNode( String nodeType, ReturnInteger matchScore ) {
	    return( getClosestMatch( nodeType, builtInNodes, matchScore ));
	}

	/** get the fieldType String that most closely matches the unknown field type string
	*
	*  @param  fieldType    an unknown field type
	*  @param  matchScore   an integer indicating how closely the return value matches
	*
	*  @return a String indicating a valid field type, that is a close match with
	*          the unknown field type input parameter.  If none is close enough, returns null.
	*/
	static public String getClosestType( String fieldType, ReturnInteger matchScore ) {
	    return( getClosestMatch( fieldType, builtInTypes, matchScore ));
	}


	/** get the field id String that most closely mathces the unknown field type id
	 *
	 *  @param   nodeName   the node containing the field
	 *  @param   fieldId    the unknown field Id
	 *  @param   matchScore an integer indicating how closely the return value matches
	 *
	 *  @return  a String indicating a valid field Id, that is a close match with
	 *           the unknown field id input parameter.  If none is close enough, returns null.
	 */
	static public String getClosestFieldId( String nodeName, String fieldId, ReturnInteger matchScore ) {
	    if ( nodeName == null ) {
	        return( null );
	    }
	    Hashtable h = (Hashtable)builtInNodes.get( nodeName );
	    if ( h != null ) {
	        return( getClosestMatch( fieldId, h, matchScore ));
	    } else {
	        return( null );
	    }
	}


	/**  get the field id String that most closely matches the unknown field id.
	 *
	 *  @param  fieldId   the unknown field id
	 *  @param  hash      the hash table containing all valid field ids for the node
	 *  @param  matchScore  an output parameter with an integer value indicating how
	 *                      close the match is.
	 *
	 *  @return  a String indicating a valid field Id, that is a close match with
	 *           the unknown field id input parameter.  If none is close enough, returns null.
	 */
	static public String getClosestMatch( String fieldId, Hashtable hash, ReturnInteger matchScore ) {
	    if ( hash == null ) {
	        return( null );
	    }
	    Enumeration nodes = hash.keys();
	    int score = 0;
	    String result = null;
	    if ( hasLetters( fieldId )) {
    	    while ( nodes.hasMoreElements() ) {
    	        String s = (String)nodes.nextElement();
    	        int testScore = Spelling.getMatchScore( s, fieldId );
    	        if ( testScore > score ) {
    	            score = testScore;
    	            result = s;
    	        }
    	    }
    	}
	    matchScore.setValue( score );
	    return( result );
	}
	
	static public boolean hasLetters( String s ) {
	    if ( s != null ) {
	        int slen = s.length();
	        for ( int i = 0; i < slen; i++ ) {
	            char x = s.charAt( i );
	            if ( Character.isLetter( x )) {
	                return( true );
	            }
	        }
	    }
	    return( false );
	}

    /** Add a ScriptFunction child to a Node.  This is used during
     *  processing of the embedded quoted string in a Javascript url,
     *  or an inline ".js" url.  It can be much improved, at the moment,
     *  it only locates the name of the function.
     *
     *  May find more than one function on javascript line
     *
     *  @param s String text of a line of javascript code
     *  @param parent Node parent that may gets the Javascript function
     *     VrmlElement if one is found
     */
	static public void addFunction( String s, Node parent ) {
		int functionIndex = s.indexOf( "function" );
		if ( functionIndex >= 0 ) {
			StringTokenizer st = new StringTokenizer( s.substring( functionIndex ), " \t()" );
			String tok = st.nextToken();
			if ( tok != null ) {
				if ( tok.compareTo( "function" ) == 0 ) {
					if ( st.hasMoreTokens() ) {
						tok = st.nextToken();
						parent.addChild( new ScriptFunction( tok ));
						// look for additional functions on the same line
						addFunction( s.substring( functionIndex + 8 ), parent );
					}
				}
			}
		}
	}
	
	static public boolean scriptFileParsed( Node n ) {
		int numberChildren = n.numberChildren();
		for ( int i = 0; i < numberChildren; i++ ) {
			VrmlElement v = n.getChildAt( i );
			if ( v instanceof ScriptFileParsed ) {
				return( true );
			}
		}
		return( false );
	}
	
	static public void checkScript( Node n, ROUTE r, RouteDestination rd, TokenEnumerator v ) {
		int numberChildren = n.numberChildren();
		boolean foundFunction = false;
		for ( int i = 0; i < numberChildren; i++ ) {
			VrmlElement ve = n.getChildAt( i );
			if ( ve instanceof ScriptFunction ) {
				ScriptFunction jfunc = (ScriptFunction)ve;
				if ( jfunc.nameIs( r.getDestFieldName() )) {
					foundFunction = true;
				}
			}
		}
		if ( !foundFunction ) {
			n.setError( "Expected a function named '" + r.getDestFieldName() + "'" );
			int firstTokenOffset = n.getFirstTokenOffset();
			int lineNo = v.getLineNumber( firstTokenOffset );
			rd.setError( "Script at line " + ( v.getLineNumber( n.getFirstTokenOffset() ) + 1 ) + " missing this eventIn function" );
		}
	}

	static void checkISFieldTypes( ISField isField, Node parentNode ) {
	    Field protoField = isField.getPROTOfield();
	    Field nodeField = isField.getNodeField();
		if ( protoField != null ) {
			protoField.markInUse();

			// get default field of that type...
			String s1 = getFieldTypeString( parentNode.getBaseName(), nodeField.getFieldId() );
			if (( s1 == null ) && ( parentNode instanceof ScriptInstance )) {
			    s1 = getFieldTypeString( "Script", nodeField.getFieldId() );

			    // At this point, it may be the interface extension to the Script
			    if ( s1 == null ) {
			        ScriptInstance si = (ScriptInstance)parentNode;
			        Field interfaceField = si.getInterfaceField( nodeField.getFieldId() );
			        if ( interfaceField == null ) {
			            return;
			        }
			        s1 = getFieldTypeString( interfaceField.getFieldType() );
			    }
			}
			if ( s1 == null ) {
			    return;
			}

			// get the more restrictive vorlon types into the spec types
			// vorlon types indicated by prefixes
			s1 = convertToVRML97( s1 );
			String s2 = getFieldTypeString( protoField.getFieldType() );
			if ( s1.compareTo( s2 ) != 0 ) {
				isField.setError( "field is " + s1 + ", but IS assigning " + s2 );
			}
		}
	}

	/** Convert a Vorlon type into VRML97 type.  
     * This convention is used specify field type value restrictions
     * that are indicated in the text of the VRML97 specification,
     * but not in the data type of fields.  For example, a Transform
     * "scale" value is restricted to positive values, but its data
     * type is SFVec3f.  Vorlon defines the type as a PositiveSFVec3f
     * to implement this restriction.
     */
	static public String convertToVRML97( String s ) {
	    if ( s != null ) {
	        if ( s.indexOf( "Positive" ) == 0 ) {
	            return( s.substring( 8 ));
	        } else if ( s.indexOf( "BboxSize" ) == 0 ) {
	            return( s.substring( 8 ));
	        }
	    }
	    return( s );
	}
}
