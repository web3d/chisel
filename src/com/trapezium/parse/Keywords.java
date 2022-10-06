package com.trapezium.parse;

import com.trapezium.util.KeywordList;
import com.trapezium.util.KeywordListList;

public class Keywords {
    static public KeywordListList key1Hash = new KeywordListList();
    static public KeywordListList key2Hash = new KeywordListList();
    static {
        key1Hash.add( "MFColor" );
        key1Hash.add( "MFFloat" );
        key1Hash.add( "MFInt32" );
        key1Hash.add( "MFNode" );
        key1Hash.add( "MFRotation" );
        key1Hash.add( "MFString" );
        key1Hash.add( "MFVec2f" );
        key1Hash.add( "MFVec3f" );
        key1Hash.add( "SFBool" );
        key1Hash.add( "SFColor" );
        key1Hash.add( "SFFloat" );
        key1Hash.add( "SFImage" );
        key1Hash.add( "SFInt32" );
        key1Hash.add( "SFNode" );
        key1Hash.add( "SFRotation" );
        key1Hash.add( "SFString" );
        key1Hash.add( "SFTime" );
        key1Hash.add( "SFVec2f" );
        key1Hash.add( "SFVec3f" );
        key1Hash.add( "ambientIntensity" );
        key1Hash.add( "appearance" );
        key1Hash.add( "attenuation" );
        key1Hash.add( "autoOffset" );
        key1Hash.add( "avatarSize" );
        key1Hash.add( "axisOfRotation" );
        key1Hash.add( "backUrl" );
        key1Hash.add( "bboxCenter" );  // both VRML 1.0 and VRML 2.0
        key1Hash.add( "bboxSize" );    // both VRML 1.0 and VRML 2.0
        key1Hash.add( "beamWidth" );
        key1Hash.add( "beginCap" );
        key1Hash.add( "bottomRadius" ); // both VRML 1.0 and VRML 2.0
        key1Hash.add( "bottomUrl" );
        key1Hash.add( "ccw" );
        key1Hash.add( "center" );       // both VRML 1.0 and VRML 2.0
        key1Hash.add( "children" );
        key1Hash.add( "choice" );
        key1Hash.add( "collide" );
        key1Hash.add( "color" );         // both VRML 1.0 and VRML 2.0
        key1Hash.add( "colorIndex" );
        key1Hash.add( "colorPerVertex" );
        key1Hash.add( "convex" );
        key1Hash.add( "coord" );
        key1Hash.add( "coordIndex" );    // both VRML 1.0 and VRML 2.0
        key1Hash.add( "creaseAngle" );   // both VRML 1.0 and VRML 2.0
        key1Hash.add( "crossSection" );
        key1Hash.add( "cutOffAngle" );   // both VRML 1.0 and VRML 2.0
        key1Hash.add( "cycleInterval" );
        key1Hash.add( "cycleTime" );
        key1Hash.add( "description" );   // both VRML 1.0 and VRML 2.0
        key1Hash.add( "diffuseColor" );  // both VRML 1.0 and VRML 2.0
        key1Hash.add( "directOutput" );
        key1Hash.add( "direction" );     // both VRML 1.0 and VRML 2.0
        key1Hash.add( "diskAngle" );
        key1Hash.add( "emissiveColor" ); // both VRML 1.0 and VRML 2.0
        key1Hash.add( "enabled" );
        key1Hash.add( "endCap" );
        key1Hash.add( "enterTime" );
        key1Hash.add( "eventIn" );
        key1Hash.add( "eventOut" );
        key1Hash.add( "exitTime" );
        key1Hash.add( "exposedField" );
        key1Hash.add( "family" );       // both VRML 1.0 and VRML 2.0
        key1Hash.add( "field" );
        key1Hash.add( "fieldOfView" );
        key1Hash.add( "focalDistance" );
        key1Hash.add( "fogType" );
        key1Hash.add( "fontStyle" );
        key1Hash.add( "frontUrl" );
        key1Hash.add( "geometry" );
        key1Hash.add( "groundAngle" );
        key1Hash.add( "groundColor" );
        key1Hash.add( "headlight" );
        key1Hash.add( "height" );        // both VRML 1.0 and VRML 2.0
        key1Hash.add( "horizontal" );
        key1Hash.add( "image" );         // both VRML 1.0 and VRML 2.0
        key1Hash.add( "info" );
        key1Hash.add( "intensity" );     // both VRML 1.0 and VRML 2.0
        key1Hash.add( "jump" );
        key1Hash.add( "justify" );
        key1Hash.add( "key" );
        key1Hash.add( "keyValue" );
        key1Hash.add( "language" );
        key1Hash.add( "leftToRight" );
        key1Hash.add( "leftUrl" );
        key1Hash.add( "length" );
        key1Hash.add( "level" );
        key1Hash.add( "location" );     // both VRML 1.0 and VRML 2.0
        key1Hash.add( "loop" );
        key1Hash.add( "material" );
        key1Hash.add( "maxAngle" );
        key1Hash.add( "maxBack" );
        key1Hash.add( "maxExtent" );
        key1Hash.add( "maxFront" );
        key1Hash.add( "maxPosition" );
        key1Hash.add( "minAngle" );
        key1Hash.add( "minBack" );
        key1Hash.add( "minFront" );
        key1Hash.add( "minPosition" );
        key1Hash.add( "mustEvaluate" );
        key1Hash.add( "normal" );
        key1Hash.add( "normalIndex" ); // both VRML 1.0 and VRML 2.0
        key1Hash.add( "normalPerVertex" );
        key1Hash.add( "offset" );
        key1Hash.add( "on" );          // both VRML 1.0 and VRML 2.0
        key1Hash.add( "orientation" ); // both VRML 1.0 and VRML 2.0
        key1Hash.add( "parameter" );
        key1Hash.add( "pitch" );
        key1Hash.add( "point" );       // both VRML 1.0 and VRML 2.0
        key1Hash.add( "position" );    // both VRML 1.0 and VRML 2.0
        key1Hash.add( "priority" );
        key1Hash.add( "proxy" );
        key1Hash.add( "radius" );      // both VRML 1.0 and VRML 2.0
        key1Hash.add( "range" );       // both VRML 1.0 and VRML 2.0
        key1Hash.add( "repeatS" );
        key1Hash.add( "repeatT" );
        key1Hash.add( "rightUrl" );
        key1Hash.add( "rotation" );    // both VRML 1.0 and VRML 2.0
        key1Hash.add( "scale" );
        key1Hash.add( "scaleOrientation" );  // both VRML 1.0 and VRML 2.0
        key1Hash.add( "shininess" );   // both VRML 1.0 and VRML 2.0
        key1Hash.add( "side" );
        key1Hash.add( "size" );        // both VRML 1.0 and VRML 2.0
        key1Hash.add( "skyAngle" );
        key1Hash.add( "skyColor" );
        key1Hash.add( "solid" );
        key1Hash.add( "source" );
        key1Hash.add( "spacing" );     // both VRML 1.0 and VRML 2.0
        key1Hash.add( "spatialize" );
        key1Hash.add( "specularColor" );  // both VRML 2.0 and VRML 2.0
        key1Hash.add( "speed" );
        key1Hash.add( "spine" );
        key1Hash.add( "startTime" );
        key1Hash.add( "stopTime" );
        key1Hash.add( "string" );    // both VRML 1.0 and VRML 2.0
        key1Hash.add( "style" );     // both VRML 1.0 and VRML 2.0
        key1Hash.add( "texCoord" );
        key1Hash.add( "texCoordIndex" );
        key1Hash.add( "texture" );
        key1Hash.add( "textureTransform" );
        key1Hash.add( "title" );
        key1Hash.add( "top" );
        key1Hash.add( "topToBottom" );
        key1Hash.add( "topUrl" );
        key1Hash.add( "translation" );     // both VRML 1.0 and VRML 2.0
        key1Hash.add( "transparency" );    // both VRML 1.0 and VRML 2.0
        key1Hash.add( "type" );
        key1Hash.add( "url" );
        key1Hash.add( "vector" );      // both VRML 1.0 and VRML 2.0
        key1Hash.add( "visibilityLimit" );
        key1Hash.add( "visibilityRange" );
        key1Hash.add( "whichChoice" );
        key1Hash.add( "xDimension" );
        key1Hash.add( "xSpacing" );
        key1Hash.add( "zDimension" );
        key1Hash.add( "zSpacing" );
        
        // blaxxun nurbs extensions
        key1Hash.add( "uDimension" );
        key1Hash.add( "vDimension" );
        key1Hash.add( "wDimension" );
        key1Hash.add( "uKnot" );
        key1Hash.add( "vKnot" );
        key1Hash.add( "wKnot" );
        key1Hash.add( "uOrder" );
        key1Hash.add( "vOrder" );
        key1Hash.add( "wOrder" );
        key1Hash.add( "controlPoint" );
        key1Hash.add( "weight" );
        key1Hash.add( "uTessellation" );
        key1Hash.add( "vTessellation" );
        key1Hash.add( "tessellationScale" );
        key1Hash.add( "dimension" );
        key1Hash.add( "knot" );
        key1Hash.add( "order" );
        key1Hash.add( "tessellation" );
        key1Hash.add( "trimmingCurves" );
        key1Hash.add( "surface" );
        key1Hash.add( "keyWeight" );
        key1Hash.add( "inputCoord" );
        key1Hash.add( "inputTransform" );
        key1Hash.add( "outputCoord" );
    }

    static {
        key2Hash.add( "Anchor" );
        key2Hash.add( "Appearance" );
        key2Hash.add( "AudioClip" );
        key2Hash.add( "Background" );
        key2Hash.add( "Billboard" );
        key2Hash.add( "Box" );
        key2Hash.add( "Collision" );
        key2Hash.add( "Color" );
        key2Hash.add( "ColorInterpolator" );
        key2Hash.add( "Cone" );       // both VRML 1.0 and VRML 2.0
        key2Hash.add( "Coordinate" );
        key2Hash.add( "CoordinateDeformer" );  // blaxxun nurbs extension
        key2Hash.add( "CoordinateInterpolator" );
        key2Hash.add( "Cylinder" );   // both VRML 1.0 and VRML 2.0
        key2Hash.add( "CylinderSensor" );
        key2Hash.add( "DEF" );
        key2Hash.add( "DirectionalLight" );  // both VRML 1.0 and VRML 2.0
        key2Hash.add( "EXTERNPROTO" );
        key2Hash.add( "ElevationGrid" );
        key2Hash.add( "Extrusion" );
        key2Hash.add( "Fog" );
        key2Hash.add( "FontStyle" );        // both VRML 1.0 and VRML 2.0
        key2Hash.add( "Group" );
        key2Hash.add( "ImageTexture" );
        key2Hash.add( "IndexedFaceSet" );   // both VRML 1.0 and VRML 2.0
        key2Hash.add( "IndexedLineSet" );   // both VRML 1.0 and VRML 2.0
        key2Hash.add( "Inline" );
        key2Hash.add( "LOD" );              // both VRML 1.0 and VRML 2.0
        key2Hash.add( "Material" );         // both VRML 1.0 and VRML 2.0
        key2Hash.add( "MovieTexture" );
        key2Hash.add( "NavigationInfo" );
        key2Hash.add( "Normal" );           // both VRML 1.0 and VRML 2.0
        key2Hash.add( "NormalInterpolator" );
        key2Hash.add( "NurbsCurve2D" );     // blaxxun nurbs extension
        key2Hash.add( "NurbsGroup" );       // blaxxun nurbs extension
        key2Hash.add( "NurbsPositionInterpolator" ); // blaxxun nurbs extension
        key2Hash.add( "NurbsSurface" );     // blaxxun nurbs extension
        key2Hash.add( "NurbsTextureSurface" ); // blaxxun nurbs extension
        key2Hash.add( "OrientationInterpolator" );
        key2Hash.add( "PROTO" );
        key2Hash.add( "PixelTexture" );
        key2Hash.add( "PlaneSensor" );
        key2Hash.add( "PointLight" );         // both VRML 1.0 and VRML 2.0
        key2Hash.add( "PointSet" );           // both VRML 1.0 and VRML 2.0
        key2Hash.add( "PositionInterpolator" );
        key2Hash.add( "ProximitySensor" );
        key2Hash.add( "ScalarInterpolator" );
        key2Hash.add( "Script" );
        key2Hash.add( "Shape" );
        key2Hash.add( "Sound" );
        key2Hash.add( "Sphere" );       // both VRML 1.0 and VRML 2.0
        key2Hash.add( "SphereSensor" );
        key2Hash.add( "SpotLight" );    // both VRML 1.0 and VRML 2.0
        key2Hash.add( "Switch" );       // both VRML 1.0 and VRML 2.0
        key2Hash.add( "Text" );
        key2Hash.add( "TextureCoordinate" );
        key2Hash.add( "TextureTransform" );
        key2Hash.add( "TimeSensor" );
        key2Hash.add( "TouchSensor" );
        key2Hash.add( "Transform" );    // both VRML 1.0 and VRML 2.0
        key2Hash.add( "TrimmedSurface" ); // blaxxun nurbs extension
        key2Hash.add( "USE" );
        key2Hash.add( "Viewpoint" );
        key2Hash.add( "VisibilitySensor" );
        key2Hash.add( "WorldInfo" );
	};

   static public KeywordList getKeyList1( char idx ) {
        int offset = 0;
        if (( idx >= 'A' ) && ( idx <= 'Z' )) {
            offset = idx - 'A';
            return( key1Hash.getCapList( offset ));
        } else {
            offset = idx - 'a';
            return( key1Hash.getSmallList( offset ));
        }
    }

    static public KeywordList getKeyList2( char idx ) {
        int offset = 0;
        if (( idx >= 'A' ) && ( idx <= 'Z' )) {
            offset = idx - 'A';
            return( key2Hash.getCapList( offset ));
        } else {
            offset = idx - 'a';
            return( key2Hash.getSmallList( offset ));
        }
    }
}


