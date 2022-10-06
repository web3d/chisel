/*
 * @(#)NodeType.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.BadFieldId;
import com.trapezium.vrml.SingleTokenElement;
import com.trapezium.vrml.grammar.Spelling;
import com.trapezium.vrml.grammar.Table7;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.visitor.ISLocator;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vorlon.ErrorSummary;
import java.util.Hashtable;
import java.util.BitSet;
import java.util.Enumeration;

/**
 *  Verifiers for Background, LOD, and Interpolator nodes.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 18 March 1998, added Table 7 base profile checks,
 *                   bad children list
 *  @version         1.1, 8 Jan 1998
 *
 *  @since           1.0
 */
public class NodeType extends SingleTokenElement {
	static public boolean coplanarEnabled = false;
	static public boolean verifyDisabled = false;
	
	// allows verify of IFS, ILS even when verify disabled
	static public boolean usageChecksEnabled = false;

	/** Actual node data types as described in the VRML97 specification.
	 *  Some fields are restricted to specific node values, even though
	 *  they are declared as SFNode.  This means that the VRML97 specification
	 *  is requiring strong type checking, without providing the actual
	 *  data types.  The actual data types are implemented below, by
	 *  keeping a list of the actual data type of each node type.
	 */
	final static public int Any = -1;
	final static public int Appearance = 0;
	final static public String[] appearanceNodes = { "Appearance" };
	final static public int Color = 1;
	final static public String[] colorNodes = { "Color" };
	final static public int Coordinate = 2;
	final static public String[] coordinateNodes = { "Coordinate" };
	final static public int FontStyle = 3;
	final static public String[] fontStyleNodes = { "FontStyle" };
	final static public int Geometry = 4;
	final static public String[] fixedGeometryNodes = { "Box", "Cone", "Cylinder", "ElevationGrid",
		"Extrusion", "IndexedFaceSet", "IndexedLineSet", "PointSet", "Sphere", "Text" };
	final static public String[] nurbsGeometryNodes = { "Box", "Cone", "Cylinder", "ElevationGrid",
		"Extrusion", "IndexedFaceSet", "IndexedLineSet", "PointSet", "Sphere", "Text",
		"NurbsCurve", "NurbsSurface" };
	static public String[] geometryNodes = fixedGeometryNodes;
	static public void enableNurbs() {
	    geometryNodes = nurbsGeometryNodes;
	}
	final static public int Material = 5;
	final static public String[] materialNodes = { "Material" };
	final static public int Normal = 6;
	final static public String[] normalNodes = { "Normal" };
	final static public int SoundSource = 7;
	final static public String[] soundSourceNodes = { "AudioClip", "MovieTexture" };
	final static public int Texture = 8;
	final static public String[] textureNodes = { "ImageTexture", "MovieTexture", "PixelTexture" };
	final static public int TextureCoordinate = 9;
	final static public String[] textureCoordinateNodes = { "TextureCoordinate" };
	final static public int TextureTransform = 10;
	final static public String[] textureTransformNodes = { "TextureTransform" };
	final static public int GroupingNode = 11;
	final static public String[] groupingNodes = { "Anchor", "Billboard", "Collision", "Group", "Transform", "Switch",
	    // new with Nurbs
	    "CoordinateDeformer", "NurbsGroup" };
	final static public int BadChildNode = 12;
	final static public String[] badChildNodes = { "Appearance", "AudioClip", "Box", "Color", 
	    "Cone", "Coordinate", "Cylinder", "ElevationGrid", "Extrusion", "ImageTexture", 
	    "IndexedFaceSet", "IndexedLineSet", "Material", "MovieTexture", "Normal", "PointSet", 
	    "Sphere", "Text", "TextureCoordinate", "TextureTransform" };
	final static public int GoodChildNode = 13;
	final static public String[] goodChildNodes = { "Anchor", "Background", "Billboard",
	    "Collision", "ColorInterpolator", "CoordinateInterpolator", "CylinderSensor",
	    "DirectionalLight", "Fog", "Group", "Inline", "LOD", "NavigationInfo",
	    "NormalInterpolator", "OrientationInterpolator", "PlaneSensor", "PointLight",
	    "PositionInterpolator", "ProximitySensor", "ScalarInterpolator", "Script",
	    "Shape", "Sound", "SpotLight", "SphereSensor", "Switch", "TimeSensor",
	    "TouchSensor", "Transform", "Viewpoint", "VisibilitySensor", "WorldInfo" };
	    
	// new with blaxxun nurbs, nodes which only accept coord nodes
    final static public int CoordListNode = 14;
    final static public String[] coordListNodes = { "Coordinate" };
    final static public int TransformListNode = 15;
    final static public String[] transformListNodes = { "Transform" };


	/** Lookup node verifiers using this table */
	static public Hashtable verifierTable = new Hashtable();
	static {
		verifierTable.put( "ColorInterpolator", new ColorInterpolatorVerifier() );
		verifierTable.put( "OrientationInterpolator", new OrientationInterpolatorVerifier() );
		verifierTable.put( "CoordinateInterpolator", new CoordinateInterpolatorVerifier() );
		verifierTable.put( "PositionInterpolator", new PositionInterpolatorVerifier() );
		verifierTable.put( "NormalInterpolator", new NormalInterpolatorVerifier() );
		verifierTable.put( "ScalarInterpolator", new ScalarInterpolatorVerifier() );
		verifierTable.put( "IndexedFaceSet", new IndexedFaceSetVerifier() );
		verifierTable.put( "IndexedLineSet", new IndexedLineSetVerifier() );
		verifierTable.put( "LOD", new LODverifier() );
		verifierTable.put( "Background", new BackgroundVerifier() );
		verifierTable.put( "ElevationGrid", new ElevationGridVerifier() );
		verifierTable.put( "PointSet", new PointSetVerifier() );
		verifierTable.put( "Extrusion", new ExtrusionVerifier() );
		verifierTable.put( "Shape", new ShapeVerifier() );
		verifierTable.put( "Text", new TextVerifier() );
		verifierTable.put( "TimeSensor", new TimeSensorVerifier() );
	}
	/** Lookup node vectors using this table */
	static public Hashtable typeTable = new Hashtable();
	static {
		typeTable.put( "Appearance", appearanceNodes );
		typeTable.put( "Color", colorNodes );
		typeTable.put( "Coordinate", coordinateNodes );
		typeTable.put( "FontStyle", fontStyleNodes );
		typeTable.put( "Geometry", geometryNodes );
		typeTable.put( "Material", materialNodes );
		typeTable.put( "Normal", normalNodes );
		typeTable.put( "SoundSource", soundSourceNodes );
		typeTable.put( "Texture", textureNodes );
		typeTable.put( "TextureCoordinate", textureCoordinateNodes );
		typeTable.put( "TextureTransform", textureTransformNodes );
		typeTable.put( "Grouping", groupingNodes );
		typeTable.put( "GoodChildNode", goodChildNodes );
		
		// new with nurbs
		typeTable.put( "CoordListNode", coordListNodes );
		typeTable.put( "TransformListNode", transformListNodes );
	}

    /** Convert an integer constant into a node type category string.
     *
     *  @param nodeType the integer constant to convert
     */
     
	static public String typeToStr( int nodeType ) {
		switch ( nodeType ) {
		case Any:
			return( "Any" );
		case Appearance:
			return( "Appearance" );
		case Color:
			return( "Color" );
		case Coordinate:
			return( "Coordinate" );
		case FontStyle:
			return( "FontStyle" );
		case Geometry:
			return( "Geometry" );
		case Material:
			return( "Material" );
		case Normal:
			return( "Normal" );
		case SoundSource:
			return( "SoundSource" );
		case Texture:
			return( "Texture" );
		case TextureCoordinate:
			return( "TextureCoordinate" );
		case TextureTransform:
			return( "TextureTransform" );
		case GroupingNode:
			return( "Grouping" );
	    case GoodChildNode:
	        return( "GoodChildNode" );
	    // new with nurbs
	    case CoordListNode:
	        return( "CoordListNode" );
	    case TransformListNode:
	        return( "TransformListNode" );
		default:
			return( "Unknown" );
		}
	}

    /** Check if a node is a particular data type.  
     *
     *  @param nodeName the name of the built in VRML97 node
     *  @param nodeType one of the built in node type constants (see above)
     *
     *  @return true if the built in node name belongs to the indicated type,
     *    otherwise false.
     */
    static public boolean isCompatible( String nodeName, int nodeType ) {
        if ( nodeName == null ) {
            return( false );
        }
        String nodeTypeString = typeToStr( nodeType );
        String[] table = (String[])typeTable.get( nodeTypeString );
        if ( table != null ) {
            return( nodeNameFound( table, nodeName ));
        } else {
            return( false );
        }
    }

    /** Is the node name contained in a particular list.
     *
     *  @param list the list to check
     *  @param nodeName the name of the node to look for in the list
     *
     *  @return true if the nodeName is found in the list, otherwise false
     */
	static boolean nodeNameFound( String[] list, String nodeName ) {
	    for ( int i = 0; i < list.length; i++ ) {
	        if ( nodeName.compareTo( list[i] ) == 0 ) {
	            return( true );
	        }
	    }
	    return( false );
	}
		
	/** Is the node a grouping node?
	 *
	 *  @param nodeName the name of the node to check
	 */
	static public boolean isGroupingNode( String nodeName ) {
	    return( nodeNameFound( groupingNodes, nodeName ));
	}
	
	/** Is the node not allowed as a child node */
	static public boolean isBadChild( String nodeName ) {
	    return( nodeNameFound( badChildNodes, nodeName ));
	}

    /** Get the list of node names of a specific category (e.g. Texture) */
	static public String getFieldTypeList( int nodeType ) {
	    String nodeTypeString = typeToStr( nodeType );
	    String[] typeList = (String[])typeTable.get( nodeTypeString );
	    if ( typeList != null ) {
	        StringBuffer sb = new StringBuffer();
	        for ( int i = 0; i < typeList.length; i++ ) {
	            sb.append( typeList[i] );
	            if ( i < ( typeList.length - 1 )) {
	                sb.append( ", " );
	            }
	        }
	        return( new String( sb ));
	    }
	    return( null );
	}

	/** The constructor is used only by the Spec level parsing */
	String strName;
	public NodeType( int indicator ) {
		super( -1 );
		strName = typeToStr( indicator );
	}

	public String getName() {
	    if ( strName != null ) {
	        return( strName );
	    } else {
	        return( super.getName() );
	    }
	}


	/** validate an actual type against the type indicated by this NodeType object,
	 *  returns a String indicating the type of error, null if none
	 */
	public String validate( String actualType ) {
		// don't check EXTERNPROTO
		if (( actualType != null ) && ( actualType.compareTo( "EXTERNPROTO" ) == 0 )) {
			return( null );
		}
		// NULL always is OK
		if (( actualType != null ) && ( actualType.compareTo( "NULLNode" ) == 0 )) {
			return( null );
		}
		String[] validTypes = (String[])typeTable.get( getName() );
		if (( actualType != null ) && ( validTypes != null )) {
			for ( int i = 0; i < validTypes.length; i++ ) {
				if ( validTypes[i].compareTo( actualType ) == 0 ) {
					return( null );
				}
			}
			if ( Spelling.isVowel( getName().charAt( 0 ))) {
				return( "not an " + getName() + " node." );
			} else {
				return( "not a " + getName() + " node." );
			}
		} else {
			return( null );
		}
	}
    static public void dumpState() {
	    System.out.println( "NodeType.verifyDisabled " + verifyDisabled + ", usageChecksEnabled " + usageChecksEnabled );
	}

    /** Visitor used to locate any "IS" fields within a node.  Currently we don't
     *  verify any node that contains an "IS" field, which is a bit too strict.
     *  Ideally, we need to specify which fields are used in the verification
     *  by node type, then only disable verification if any of these is an "IS" field.
     */
    static ISLocator isLocator = new ISLocator();
    
    /** Verify the information in a node using a node specific verifier.
     *
     *  @param n the Node to verify
     *  @param nodeType String identifying type of the node
     *  @param s the Scene containing the node
     */
	static public void verify( Node n, String nodeType, Scene s ) {
		if ( verifyDisabled ) {
		    if (( nodeType != null ) && usageChecksEnabled ) {
//		        System.out.println( "NodeType is '" + nodeType + "'" );
    		    if (!(( nodeType.compareTo( "IndexedFaceSet" ) == 0 ) ||
    		         ( nodeType.compareTo( "IndexedLineSet" ) == 0 ))) {
    		        return;
    		    }
    		} else {
    		    return;
    		}
		}
		if ( nodeType == null ) {
		    return;
		}
		Verifier v = (Verifier)verifierTable.get( nodeType );
		if ( v != null ) {
		    isLocator.reset();
		    n.traverse( isLocator );
		    if ( !isLocator.foundISField() ) {
    			v.verify( n, s, s.getErrorSummary(), s.getVerifyList() );
    		}
		}
	}

    /** Verify coordinate usage in IndexedFaceSets 
     *
     *  @param usageTable usage info associated with each node in a scene
     *  @param v access to file text
     *  @param errorSummary summary info used in cases where there are too many errors
     *     or warnings to specify individually
     */
	static public void verifyUsage( Hashtable usageTable, TokenEnumerator v, ErrorSummary errorSummary ) {
//	    System.out.println( "verifyUsage, verifyDisabled " + verifyDisabled + ", usageChecksEnabled " + usageChecksEnabled );
		if ( verifyDisabled && !usageChecksEnabled ) return;
		IndexedFaceSetVerifier.verifyUsage( usageTable, v, errorSummary );
	}
}

/** Verifies TimeSensor node.
 *  NOTE: needs to be extended to check IS fields, OK for now because
 *  nodes within PROTOs aren't checked
 */
class TimeSensorVerifier implements Verifier {
    public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
        if ( toBeVerified.getFirstTokenOffset() == -1 ) {
            return;
        }
        boolean doWarning = false;
        VrmlElement parent = toBeVerified.getParent();
        if ( parent instanceof DEFUSENode ) {
            DEFUSENode dun = (DEFUSENode)parent;
            doWarning = !dun.isUsed();
        } else {
            doWarning = true;
        }
        if ( doWarning ) {
            toBeVerified.setError( "Warning, unused TimeSensor" );
        }
    }
}

/** Verifies Shape node */
class ShapeVerifier implements Verifier {
    public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
        Node geometry = toBeVerified.getNodeValue( "geometry" );
        boolean requiresEmissive = false;
        if ( geometry != null ) {
            String baseName = geometry.getBaseName();
            if ( geometry instanceof PROTOInstance ) {
                Node n = ((PROTOInstance)geometry).getPROTONodeType();
                if ( n != null ) {
                    baseName = n.getBaseName();
                }
            }
            if ( baseName != null ) {
                requiresEmissive = (( baseName.compareTo( "IndexedLineSet" ) == 0 ) || ( baseName.compareTo( "PointSet" ) == 0 ));
            }
        }
        boolean foundEmissive = false;
        if ( requiresEmissive ) {
            Node appearance = toBeVerified.getNodeValue( "appearance" );
            if ( appearance != null ) {
                Node material = appearance.getNodeValue( "material" );
                if ( material != null ) {
                    Field emissiveColor = material.getField( "emissiveColor" );
                    if ( emissiveColor != null ) {
                        foundEmissive = true;
                    }
                }
            }
            if ( !foundEmissive ) {
                geometry.setError( "Warning, need emissiveColor in Material for this to be visible" );
            }
        }
    }
}

/** Verifies Text node */
class TextVerifier implements Verifier {
    public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
        Field strings = toBeVerified.getField( "string" );
        if ( strings != null ) {
            TokenEnumerator te = s.getTokenEnumerator();
            FieldValue fv = strings.getFieldValue();
            if ( fv instanceof MFFieldValue ) {
                MFFieldValue mfv = (MFFieldValue)fv;
                int count = mfv.numberValues();
                for ( int i = 0; i < count; i++ ) {
                    fv = mfv.getFieldValueAt( i );
                    if ( fv == null ) {
                        break;
                    }
                    int tok = fv.getFirstTokenOffset();
                    if ( tok == -1 ) {
                        break;
                    }
                    int len = te.getSize( tok ) - 2;
                    if ( len > Table7.TextMaxCharPerString ) {
                        fv.setError( "Nonconformance, string length " + len + " exceeds base profile limit " + Table7.TextMaxCharPerString );
                    }
                }
            }
        }
    }
}

/**
 *  Verifies Extrusion
 */
class ExtrusionVerifier implements Verifier {
    public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
        Field crossSection = toBeVerified.getField( "crossSection" );
        Field spine = toBeVerified.getField( "spine" );
        if (( crossSection != null ) && ( spine != null )) {
            FieldValue csv = crossSection.getFieldValue();
            FieldValue sv = spine.getFieldValue();
            if (( csv instanceof MFFieldValue ) && ( sv instanceof MFFieldValue )) {
                MFFieldValue crossSectionValues = (MFFieldValue)csv;
                MFFieldValue spineValues = (MFFieldValue)sv;
                int csCount = crossSectionValues.getRawValueCount()/2;
                int spineCount = spineValues.getRawValueCount()/3;
                if ( csCount*spineCount > Table7.ExtrusionLimit ) {
                    toBeVerified.setError( "Nonconformance, crossSection*spine count " + (csCount*spineCount) + " exceeds base profile limit " + Table7.ExtrusionLimit );
                }
            }
        }
    }
}
                

/** 
 *  Verifies PointSet
 */
class PointSetVerifier implements Verifier {
    public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
        Field coord = toBeVerified.getField( "coord" );
        if ( coord != null ) {
            Node coordNode = coord.getNodeValue();
            if ( coordNode != null ) {
                Field point = coordNode.getField( "point" );
                if ( point != null ) {
                    if ( verifiedList.get( point ) != null ) {
                        return;
                    }
                    verifiedList.put( point, point );
                    FieldValue fv = point.getFieldValue();
                    if ( fv instanceof MFFieldValue ) {
                        MFFieldValue mfv = (MFFieldValue)fv;
                        int count = mfv.getRawValueCount()/3;
                        if ( count > Table7.PointSetLimit ) {
                            point.setError( "Nonconformance, " + count + " points exceeds base profile limit of " + Table7.PointSetLimit );
                        }
                    }
                }
            }
        }
    }
}

/**
 *  Verifies information in ElevationGrid node.  Verifies that "xDimension" and
 *  "zDimension" are both specified, and have values at least 2.  Verifies that
 *  the number of "height" values is correct given the "xDimension" and "zDimension".
 *  Verifies that the number of colors, normals, textures if present is correct.
 */
class ElevationGridVerifier implements Verifier {
	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
	    if ( s == null ) {
	        return;
	    }
	    TokenEnumerator v = s.getTokenEnumerator();
		Field xDim = toBeVerified.getField( "xDimension" );
		int xValue = 0;
		int zValue = 0;
		if ( xDim == null ) {
		    toBeVerified.setError( "X dimension not specified" );
		} else {
			FieldValue xDimValue = xDim.getFieldValue();
			if ( xDimValue != null ) {
				xValue = v.getIntValue( xDimValue.getFirstTokenOffset() );
				if ( xValue < 2 ) {
					xDimValue.setError( "X dimension should be at least 2" );
				}
			}
		}
		Field zDim = toBeVerified.getField( "zDimension" );
		if ( zDim == null ) {
		    toBeVerified.setError( "Z dimension not specified" );
		} else {
			FieldValue zDimValue = zDim.getFieldValue();
			if ( zDimValue != null ) {
				zValue = v.getIntValue( zDimValue.getFirstTokenOffset() );
				if ( zValue < 2 ) {
					zDimValue.setError( "Z dimension should be at least 2" );
				}
			}
		}
		Field height = toBeVerified.getField( "height" );
		if ( height == null ) {
		    toBeVerified.setError( "Warning, height field missing" );
		} else {
		    FieldValue heightValues = height.getFieldValue();
		    if ( heightValues instanceof MFFieldValue ) {
		        MFFieldValue mfv = (MFFieldValue)heightValues;
		        if ( mfv.getRawValueCount() != zValue*xValue ) {
		            height.setError( "expected " + zValue*xValue + " height values, got " + mfv.getRawValueCount() );
		        }
		    }
		}
		checkCorrelation( toBeVerified, toBeVerified.getBoolValue( "colorPerVertex" ),
		    "color", "color", 3, xValue, zValue, "colors" );
		checkCorrelation( toBeVerified, toBeVerified.getBoolValue( "normalPerVertex" ),
		    "normal", "vector", 3, xValue, zValue, "normals" );
		checkCorrelation( toBeVerified, true, "texCoord", "point", 2, xValue, zValue, "texture coordinates" );
	}
	
	void checkCorrelation( Node toBeVerified, boolean perVertex, 
	    String nodeFieldName, String vFieldName, int factor,
	    int xValue, int zValue, String vTypeStr ) {
		Field nodeField = toBeVerified.getField( nodeFieldName );
		if ( nodeField != null ) {
		    FieldValue fv = nodeField.getFieldValue();
		    if ( fv instanceof SFNodeValue ) {
		        SFNodeValue sfv = (SFNodeValue)fv;
		        Node cnode = sfv.getNode();
		        if ( cnode != null ) {
		            Field vField = cnode.getField( vFieldName );
		            if ( vField != null ) {
		                FieldValue cfv = vField.getFieldValue();
		                if ( cfv instanceof MFFieldValue ) {
            		        MFFieldValue mfv = (MFFieldValue)cfv;
            		        int expectedCount = zValue * xValue;
            		        if ( !perVertex ) {
            		            expectedCount = ( zValue - 1 ) * ( xValue - 1 );
            		        }
           		            if (( mfv.getRawValueCount()/factor ) != expectedCount ) {
           		                vField.setError( "expected " + expectedCount + " " + vTypeStr + ", got " + ( mfv.getRawValueCount()/3 ));
           		            }
            		    }
            		}
		        }
		    }
		}
	}
}

/**
 *  Verifies information in Background node.
 */
class BackgroundVerifier implements Verifier {
	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
		verifyColorAngle( toBeVerified, "skyColor", "skyAngle" );
		verifyColorAngle( toBeVerified, "groundColor", "groundAngle" );
	}

	public void verifyColorAngle( Node toBeVerified, String colorStr, String angleStr ) {
		Field color = toBeVerified.getField( colorStr );
		Field angle = toBeVerified.getField( angleStr );
		if (( color != null ) && ( angle != null )) {
			FieldValue colorValue = color.getFieldValue();
			FieldValue angleValue = angle.getFieldValue();
			if (( colorValue instanceof MFFieldValue ) &&
				( angleValue instanceof MFFieldValue )) {
				MFFieldValue mfcolorValue = (MFFieldValue)colorValue;
				MFFieldValue mfangleValue = (MFFieldValue)angleValue;
				int colorValueCount = mfcolorValue.getRawValueCount()/3;
				int angleValueCount = mfangleValue.getRawValueCount();
				if ( colorValueCount < 1 ) {
					if ( angleValueCount >= 1 ) {
						colorValue.setError( "Expected " + (angleValueCount + 1) + " colors, got " + colorValueCount );
					} else {
						colorValue.setError( "Expected at least 1 color." );
					}
				}
				if (( colorValueCount >= 1 ) && ( angleValueCount >= 0 )) {
					if ( colorValueCount != ( angleValueCount + 1 )) {
						colorValue.setError( "Expected " + (angleValueCount + 1) + " colors, got " + colorValueCount );
					}
				}
				if ( !mfangleValue.isIncreasing() ) {
					mfangleValue.setError( "Angle values must be increasing" );
				}
			}
		}
	}
}


/**
 *  Verifies information in LOD node.
 */
class LODverifier implements Verifier {
	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
		Field levels = toBeVerified.getField( "level" );
		Field range = toBeVerified.getField( "range" );
		if (( levels != null ) && ( range != null )) {
			FieldValue levelValues = levels.getFieldValue();
			FieldValue rangeValues = range.getFieldValue();
			if (( levelValues instanceof MFFieldValue ) &&
				( rangeValues instanceof MFFieldValue )) {
				MFFieldValue mfLevelValues = (MFFieldValue)levelValues;
				MFFieldValue mfRangeValues = (MFFieldValue)rangeValues;
				int levelValueCount = mfLevelValues.getRawValueCount();
				int rangeValueCount = mfRangeValues.getRawValueCount();
				if ( levelValueCount < 2 ) {
					levels.setError( "Expected at least two nodes here." );
				} else if ( levelValueCount != ( rangeValueCount + 1 )) {
				    if ( levelValueCount < ( rangeValueCount + 1 )) {
				        if ( levelValueCount == 2 ) {
				            range.setError( "Warning, expected 1 range value here" );
				        } else {
				            range.setError( "Warning, expected " + ( levelValueCount - 1 ) + " range values here" );
				        }
				    } else {
				        if ( levelValueCount == 2 ) {
				            range.setError( "Expected 1 range value here" );
				        } else {
        					range.setError( "Expected " + ( levelValueCount - 1 ) + " range values here" );
        				}
    				}
				} else if ( !mfRangeValues.isIncreasing() ) {
					range.setError( "Range values should be in increasing order" );
				}
			}
		}
	}
}


/**
 *  Base class for verifying information in interpolators.
 */
abstract class InterpolatorVerifier implements Verifier {
	protected int factor = 1;
	protected MFFieldValue keyList = null;
	protected MFFieldValue keyValueList = null;
	protected String interpolatorType = null;

	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
		keyList = null;
		keyValueList = null;

		Field key = toBeVerified.getField( "key" );
		Field keyValue = toBeVerified.getField( "keyValue" );
		if (( key != null ) && ( keyValue != null )) {
			FieldValue kfv = key.getFieldValue();
			FieldValue kvfv = keyValue.getFieldValue();
			if (( kfv instanceof MFFieldValue ) &&
				( kvfv instanceof MFFieldValue )) {
				keyList = (MFFieldValue)kfv;
				if ( keyList == null ) {
					if ( toBeVerified.getISfield( "key" ) == null ) {
						toBeVerified.setError( "Missing key field" );
					}
				} else {
				    if ( !keyList.isNonDecreasing() ) {
					    keyList.setError( "key sequence must be non-decreasing" );
					}
					flagUnnecessaryKeys( s.getTokenEnumerator(), s.getErrorSummary() );
				}
				keyValueList = (MFFieldValue)kvfv;
				if ( keyValueList == null ) {
					if ( toBeVerified.getISfield( "keyValue" ) == null ) {
						toBeVerified.setError( "Missing keyValue field" );
					}
				} else {
				    flagUnnecessaryKeyValues( s.getTokenEnumerator(), s.getErrorSummary() );
				}
			} else {
     			if ( toBeVerified.getISfield( "key" ) == null ) {
					toBeVerified.setError( "Missing key field" );
				}
				if ( toBeVerified.getISfield( "keyValue" ) == null ) {
					toBeVerified.setError( "Missing keyValue field" );
				}
			}
		}
	}

	/** Flag unnecessary keys with a warning.
	 *  Unnecessary keys are those that are repeated more than twice,
	 *  as in "A A B B B C ...".  In this case the middle "B" is unnecessary
	 */
	public void flagUnnecessaryKeys( TokenEnumerator tokenEnumerator, ErrorSummary errorSummary ) {
	    flagUnnecessaryValues( errorSummary, keyList, tokenEnumerator, 1, "Warning, unused key" );
	}
	
	/** Flag unnecessary key values with a warning.
	 *  Unnecessary keyValues are those that are repeated more than twice, 
	 *  as in "A A B B B C ...".  In this case the middle "B" is unnecessary,
	 */
	public void flagUnnecessaryKeyValues( TokenEnumerator tokenEnumerator, ErrorSummary errorSummary ) {
	    int fval = getActualFactor();
	    flagUnnecessaryValues( errorSummary, keyValueList, tokenEnumerator, fval, "Warning, unnecessary keyValue" );
	}
	
	/** Flag unnecessary key or keyValue values with a warning.
	 *
	 *  @param errorSummary if not null, restricts number of warning Value objects created
	 *  @param valList VrmlElement containing all the values to check
	 *  @param tokenEnumerator text source of the values
	 *  @param fval factor, i.e. number of numeric values for each actual value
	 *  @param warningStr String warning to be attached to unnecessary values
	 */
	void flagUnnecessaryValues( ErrorSummary errorSummary, MFFieldValue valList, TokenEnumerator tokenEnumerator, int fval, String warningStr ) {
	    if (( fval > 0 ) && ( tokenEnumerator != null )) {
	        int oldState = tokenEnumerator.getState();
	        tokenEnumerator.setState( valList.getFirstTokenOffset() );
	        float[] prevVals = new float[ fval ];
	        float[] currentVals = new float[ fval ];
	        int lastTokenOffset = valList.getLastTokenOffset();
	        boolean setPrevVals = true;
	        boolean setCurrentVals = true;
	        boolean checkValue = false;
	        int currentToken = -1;
	        while ( tokenEnumerator.getState() < lastTokenOffset ) {
	            if ( tokenEnumerator.getState() == -1 ) {
	                break;
	            }
	            if ( setPrevVals ) {
        	        if ( !setValues( tokenEnumerator, prevVals, fval )) {
        	            break;
        	        }
        	        setPrevVals = false;
        	    }
        	    if ( setCurrentVals ) {
        	        currentToken = tokenEnumerator.skipNonNumbers();
        	        if (( currentToken == -1 ) || ( currentToken >= lastTokenOffset )) {
        	            break;
        	        }
           	        if ( !setValues( tokenEnumerator, currentVals, fval )) {
    	                break;
    	            }
    	            boolean consecutiveMatch = true;
    	            for ( int i = 0; i < fval; i++ ) {
    	                if ( currentVals[i] != prevVals[i] ) {
    	                    consecutiveMatch = false;
    	                    break;
    	                }
    	            }
    	            if ( consecutiveMatch ) {
    	                setCurrentVals = false;
    	                checkValue = true;
    	            } else {
    	                for ( int i = 0; i < fval; i++ ) {
    	                    prevVals[i] = currentVals[i];
    	                }
    	                checkValue = false;
    	            }
    	        }
    	        if ( checkValue ) {
    	            int nextCurrentToken = tokenEnumerator.skipNonNumbers();
    	            if (( nextCurrentToken == -1 ) || ( nextCurrentToken >= lastTokenOffset )) {
    	                break;
    	            }
    	            if ( !setValues( tokenEnumerator, currentVals, fval ))  {
    	                break;
    	            }
    	            boolean consecutiveMatch = true;
    	            for ( int i = 0; i < fval; i++ ) {
    	                if ( currentVals[i] != prevVals[i] ) {
    	                    consecutiveMatch = false;
    	                    break;
    	                }
    	            }
    	            if ( consecutiveMatch ) {
    	                if (( errorSummary == null ) || errorSummary.countWarning( warningStr )) {
        	                valList.addWarning( currentToken, warningStr );
        	            }
    	                currentToken = nextCurrentToken;
    	            } else {
    	                for ( int i = 0; i < fval; i++ ) {
    	                    prevVals[i] = currentVals[i];
    	                }
    	                checkValue = false;
    	                setCurrentVals = true;
    	            }
    	        }
    	    }
	        
	        tokenEnumerator.setState( oldState );
	        return;
	    }
	}
	
	boolean setValues( TokenEnumerator tokenEnumerator, float[] fvals, int fcount ) {
	    for ( int i = 0; i < fcount; i++ ) {
	        int scanner = tokenEnumerator.skipNonNumbers();
	        if ( scanner < 0 ) {
	            return( false );
	        }
	        fvals[i] = tokenEnumerator.getFloat( scanner );
	        scanner = tokenEnumerator.getNextToken();
	    }
	    return( true );
	}
	abstract public int getActualFactor();
}


/**
 *  Base class for verifying interpolators that have a single data value for each key value.
 */
class SFInterpolatorVerifier extends InterpolatorVerifier {
	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
		super.verify( toBeVerified, s, errorSummary, verifiedList );
		if (( keyList != null ) && ( keyValueList != null )) {
		    // If either is defined by IS, we cannot verify
		    VrmlElement c0 = keyList.getChildAt( 0 );
		    if ( c0 instanceof ISField ) {
		        return;
		    }
		    c0 = keyValueList.getChildAt( 0 );
		    if ( c0 instanceof ISField ) {
		        return;
		    }
			int keyListCount = keyList.getRawValueCount();
			int keyValueListCount = keyValueList.getRawValueCount();
			if (( keyListCount * factor ) != keyValueListCount ) {
			    String kvStr = " keyValues";
			    String vStr = " values";
			    if (( keyListCount * factor ) == 1 ) {
			        kvStr = " keyValue";
			    }
			    if ( keyListCount == 1 ) {
			        vStr = " value";
			    }
			    String eStr = interpolatorType + "Interpolator key has " + keyListCount + vStr + ", expected " + keyListCount + kvStr + ", got " + keyValueListCount/factor;
			    if (( keyListCount * factor ) > keyValueListCount ) {
			        toBeVerified.setError( eStr );
			    } else {
			        toBeVerified.setError( "Warning, " + eStr );
			    }
			} else if ( keyListCount == 1 ) {
			    toBeVerified.setError( "Warning, expected more than 1 value in " + interpolatorType + "Interpolator" );
			}
		}
	}
	public int getActualFactor() {
	    return( factor );
	}
}

/**
 *  Base class for verifying interpolators that have one or more data values for each key value.
 */
class MFInterpolatorVerifier extends InterpolatorVerifier {
	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
		super.verify( toBeVerified, s, errorSummary, verifiedList );
		if (( keyList != null ) && ( keyValueList != null )) {
			int keyListCount = keyList.getRawValueCount();
			int keyValueListCount = keyValueList.getRawValueCount();
			int mValue = 0;
			if ( keyListCount > 0 ) {
			    mValue = keyValueListCount/(factor*keyListCount);
			}
			int expectedCount = mValue * keyListCount * factor;
			int expectedCount1 = (mValue + 1)*keyListCount * factor;
			if ( mValue * keyListCount * factor != keyValueListCount ) {
				if ( expectedCount == 0 ) {
					toBeVerified.setError( "keyValue must have at least " + expectedCount1 + " values, has " + keyValueListCount + " values." );
				} else {
					toBeVerified.setError( "keyValue must have " + expectedCount + " or " + expectedCount1 + " values, has " + keyValueListCount + " values." );
				}
			}
		}
	}
	public int getActualFactor() {
	    int mValue = 0;
	    if (( keyList != null ) && ( keyValueList != null )) {
    	    int keyListCount = keyList.getRawValueCount();
    	    int keyValueListCount = keyValueList.getRawValueCount();
    	    if ( keyListCount > 0 ) {
    	        mValue = keyValueListCount/(factor*keyListCount);
    	    }
    	}
	    return( mValue*factor );
	}
}

/** Verifies ColorInterpolator nodes */
class ColorInterpolatorVerifier extends SFInterpolatorVerifier {
	public ColorInterpolatorVerifier() {
		factor = 3;
		interpolatorType = "Color";
	}
}

/** Verifies OrientationInterpolator nodes */
class OrientationInterpolatorVerifier extends SFInterpolatorVerifier {
	public OrientationInterpolatorVerifier() {
		factor = 4;
		interpolatorType = "Orientation";
	}

	public void verify( Node toBeVerified, Scene s, ErrorSummary errorSummary, Hashtable verifiedList ) {
		super.verify( toBeVerified, s, errorSummary, verifiedList );

		// todo: verify no rotations specified that are >= 180 degrees....
	}
}

/** Verifies PositionInterpolator nodes */
class PositionInterpolatorVerifier extends SFInterpolatorVerifier {
	public PositionInterpolatorVerifier() {
		factor = 3;
		interpolatorType = "Position";
	}
}

/** Verifies ScalarInterpolator nodes */
class ScalarInterpolatorVerifier extends SFInterpolatorVerifier {
	public ScalarInterpolatorVerifier() {
		factor = 1;
		interpolatorType = "Scalar";
	}
}

/** Verifies CoordinateInterpolator nodes */
class CoordinateInterpolatorVerifier extends MFInterpolatorVerifier {
	public CoordinateInterpolatorVerifier() {
		factor = 3;
		interpolatorType = "Coordinate";
	}
}

/** Verifies NormalInterpolator nodes */
class NormalInterpolatorVerifier extends MFInterpolatorVerifier {
	public NormalInterpolatorVerifier() {
		factor = 3;
		interpolatorType = "Normal";
	}
}


