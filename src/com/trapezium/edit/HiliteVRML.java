
package com.trapezium.edit;

import java.awt.*;
import java.util.*;
import java.io.*;

public class HiliteVRML extends Hilite
{
	public HiliteVRML(Lines l, int t, boolean a)
	{
		super(l,t,a);

		String temp[] = new String[]
        {
   	  "MFColor",
	  "MFFloat",
	  "MFInt32",
	  "MFLong",    // VRML 1.0
	  "MFNode",
	  "MFRotation",
	  "MFString",
	  "MFVec2f",
	  "MFVec3f",
	  "SFBitMask",  // VRML 1.0
	  "SFBool",
	  "SFColor",
	  "SFEnum",     // VRML 1.0
	  "SFFloat",
	  "SFImage",
	  "SFInt32",
	  "SFLong",     // VRML 1.0
	  "SFMatrix",   // VRML 1.0
	  "SFNode",
	  "SFRotation",
	  "SFString",
	  "SFTime",
	  "SFVec2f",
	  "SFVec3f",

            "ambientColor",     // VRML 1.0
            "ambientIntensity",
    	    "appearance",
    	    "attenuation",
    	    "autoOffset",
    	    "avatarSize",
    	    "axisOfRotation",
	        "backUrl",
		    "bboxCenter",  // both VRML 1.0 and VRML 2.0
	        "bboxSize",    // both VRML 1.0 and VRML 2.0
    	    "beamWidth",
    	    "beginCap",
    	    "bottomRadius", // both VRML 1.0 and VRML 2.0
	        "bottomUrl",
    	    "ccw",
    	    "center",       // both VRML 1.0 and VRML 2.0
            "children",
    	    "choice",
    	    "collide",
    	    "color",         // both VRML 1.0 and VRML 2.0
    	    "colorIndex",
	        "colorPerVertex",
	        "controlPoint", // blaxxun nurbs
	        "convex",
    	    "coord",
	        "coordIndex",    // both VRML 1.0 and VRML 2.0
	        "creaseAngle",   // both VRML 1.0 and VRML 2.0
    	    "crossSection",
    	    "cutOffAngle",   // both VRML 1.0 and VRML 2.0
    	    "cycleInterval",
    	    "cycleTime",
    	    "depth",         // VRML 1.0
	        "description",   // both VRML 1.0 and VRML 2.0
    	    "diffuseColor",  // both VRML 1.0 and VRML 2.0
    	    "dimension",     // blaxxun nurbs
    	    "directOutput",
    	    "direction",     // both VRML 1.0 and VRML 2.0
    	    "diskAngle",
    	    "dropOffRate",   // VRML 1.0
    	    "emissiveColor", // both VRML 1.0 and VRML 2.0
    	    "enabled",
    	    "endCap",
    	    "enterTime",
         "eventIn",
         "eventOut",
    	    "exitTime",
         "exposedField",
            "faceType",     // VRML 1.0
    	    "family",       // both VRML 1.0 and VRML 2.0
         "field",
    	    "fieldOfView",
    	    "filename",     // VRML 1.0
    	    "focalDistance",
    	    "fogType",
    	    "fontStyle",
    	    "frontUrl",
    	    "geometry",
	        "groundAngle",
	        "groundColor",
    	    "headlight",
    	    "height",        // both VRML 1.0 and VRML 2.0
    	    "heightAngle",   // VRML 1.0
    	    "horizontal",
    	    "image",         // both VRML 1.0 and VRML 2.0
    	    "info",
    	    "inputCoord",   // blaxxun nurbs
    	    "inputTransform", // blaxxun nurbs
    	    "intensity",     // both VRML 1.0 and VRML 2.0
    	    "jump",
    	    "justify",
    	    "justification", // VRML 1.0
    	    "key",
	        "keyValue",
	        "keyWeight",    // blaxxun nurbs
	        "knot",         // blaxxun nurbs
    	    "language",
    	    "leftToRight",
    	    "leftUrl",
    	    "length",
    	    "level",
    	    "location",     // both VRML 1.0 and VRML 2.0
	        "loop",
	        "map",          // VRML 1.0
	        "material",
	        "materialIndex", // VRML 1.0
	        "matrix",       // VRML 1.0
    	    "maxAngle",
    	    "maxBack",
    	    "maxExtent",
	        "maxFront",
    	    "maxPosition",
	        "minAngle",
    	    "minBack",
	        "minFront",
    	    "minPosition",
    	    "mustEvaluate",
    	    "name",        // VRML 1.0
    	    "normal",
    	    "normalIndex", // both VRML 1.0 and VRML 2.0
    	    "normalPerVertex",
    	    "numPoints",   // VRML 1.0
	        "offset",
    	    "on",          // both VRML 1.0 and VRML 2.0
    	    "order",        // blaxxun nurbs
    	    "orientation", // both VRML 1.0 and VRML 2.0
    	    "outputCoord", // blaxxun nurbs
	        "parameter",
	        "parts",       // VRML 1.0
	        "pitch",
    	    "point",       // both VRML 1.0 and VRML 2.0
    	    "position",    // both VRML 1.0 and VRML 2.0
    	    "priority",
    	    "proxy",
    	    "radius",      // both VRML 1.0 and VRML 2.0
    	    "range",       // both VRML 1.0 and VRML 2.0
    	    "renderCulling", // VRML 1.0
    	    "repeatS",
    	    "repeatT",
	        "rightUrl",
    	    "rotation",    // both VRML 1.0 and VRML 2.0
    	    "scale",
    	    "scaleFactor", // VRML 1.0
    	    "scaleOrientation",  // both VRML 1.0 and VRML 2.0
    	    "shapeType",   // VRML 1.0
    	    "shininess",   // both VRML 1.0 and VRML 2.0
    	    "side",
    	    "size",        // both VRML 1.0 and VRML 2.0
	        "skyAngle",
	        "skyColor",
    	    "solid",
    	    "source",
    	    "spacing",     // both VRML 1.0 and VRML 2.0
    	    "spatialize",
    	    "specularColor",  // both VRML 2.0 and VRML 2.0
    	    "speed",
    	    "spine",
    	    "startIndex",  // VRML 1.0
	        "startTime",
	        "stopTime",
    	    "string",    // both VRML 1.0 and VRML 2.0
    	    "style",     // both VRML 1.0 and VRML 2.0
    	    "surface",              // blaxxun nurbs
    	    "tessellation",         // blaxxun nurbs
    	    "tessellationScale",    // blaxxun nurbs
    	    "texCoord",
    	    "texCoordIndex",
	        "texture",
	        "textureCoordIndex",  // VRML 1.0
	        "textureTransform",
    	    "title",
    	    "top",
    	    "topToBottom",
	        "topUrl",
    	    "translation",     // both VRML 1.0 and VRML 2.0
    	    "transparency",    // both VRML 1.0 and VRML 2.0
    	    "trimmingCurves",   // blaxxun nurbs
    	    "type",
    	    "uDimension",   // blaxxun nurbs
    	    "uKnot",        // blaxxun nurbs
    	    "uOrder",       // blaxxun nurbs
    	    "uTessellation",// blaxxun nurbs
	        "url",
	        "vDimension",   // blaxxun nurbs
	        "vKnot",        // blaxxun nurbs
	        "vOrder",       // blaxxun nurbs
	        "vTessellation",// blaxxun nurbs
	        "value",       // VRML 1.0
    	    "vector",      // both VRML 1.0 and VRML 2.0
    	    "vertexOrdering", // VRML 1.0
    	    "visibilityLimit",
    	    "visibilityRange",
    	    "wDimension",   // blaxxun nurbs
    	    "wKnot",        // blaxxun nurbs
    	    "wOrder",       // blaxxun nurbs
    	    "weight",       // blaxxun nurbs
    	    "whichChild",   // VRML 1.0
    	    "whichChoice",
    	    "width",        // VRML 1.0
    	    "wrapS",        // VRML 1.0
    	    "wrapT",        // VRML 1.0
    	    "xDimension",
	        "xSpacing",
	        "zDimension",
	        "zSpacing",

		};


		String temp2[] = new String[]
		{
			"Anchor",
			"Appearance",
			"AsciiText",   // VRML 1.0
  		    "AudioClip",
  		    "Background",
  		    "Billboard",
  		    "Box",
      		"Collision",
      		"Color",
      		"ColorInterpolator",
      		"Cone",       // both VRML 1.0 and VRML 2.0
      		"Coordinate",
      		"Coordinate3", // VRML 1.0
      		"CoordinateDeformer",       // blaxxun nurbs extension
      		"CoordinateInterpolator",
      		"Cube",       // VRML 1.0
      		"Cylinder",   // both VRML 1.0 and VRML 2.0
      		"CylinderSensor",
        "DEF",
      		"DirectionalLight",  // both VRML 1.0 and VRML 2.0
      "EXTERNPROTO",
      		"ElevationGrid",
      		"Extrusion",
      		"Fog",
            "FontStyle",        // both VRML 1.0 and VRML 2.0
            "Group",
            "ImageTexture",
            "IndexedFaceSet",   // both VRML 1.0 and VRML 2.0
            "IndexedLineSet",   // both VRML 1.0 and VRML 2.0
            "Info",             // VRML 1.0
            "Inline",
            "LOD",              // both VRML 1.0 and VRML 2.0
            "Material",         // both VRML 1.0 and VRML 2.0
            "MaterialBinding",  // VRML 1.0
            "MatrixTransform",  // VRML 1.0
            "MovieTexture",
            "NavigationInfo",
            "Normal",           // both VRML 1.0 and VRML 2.0
            "NormalBinding",    // VRML 1.0
            "NormalInterpolator",
            "NurbsCurve2D",     // blaxxun nurbs extension
            "NurbsGroup",       // blaxxun nurbs extension
            "NurbsPositionInterpolator", // blaxxun nurbs extension
            "NurbsSurface",     // blaxxun nurbs extension
            "NurbsTextureSurface", // blaxxun nurbs extension
            "OrientationInterpolator",
            "OrthographicCamera", // VRML 1.0
      "PROTO",
            "PerspectiveCamera",  // VRML 1.0
            "PixelTexture",
            "PlaneSensor",
            "PointLight",         // both VRML 1.0 and VRML 2.0
            "PointSet",           // both VRML 1.0 and VRML 2.0
            "PositionInterpolator",
            "ProximitySensor",
            "Rotation",     // VRML 1.0
            "ScalarInterpolator",
            "Scale",        // VRML 1.0
            "Script",
            "Separator",    // VRML 1.0
            "Shape",
            "ShapeHints",   // VRML 1.0
            "Sound",
            "Sphere",       // both VRML 1.0 and VRML 2.0
            "SphereSensor",
            "SpotLight",    // both VRML 1.0 and VRML 2.0
            "Switch",       // both VRML 1.0 and VRML 2.0
            "Text",
            "Texture2",     // VRML 1.0
            "Texture2Transform", // VRML 1.0
            "TextureCoordinate",
            "TextureCoordinate2", // VRML 1.0
            "TextureTransform",
            "TimeSensor",
            "TouchSensor",
            "Transform",    // both VRML 1.0 and VRML 2.0
            "Translation",  // VRML 1.0
            "TrimmedSurface",   // blaxxun nurbs extension
        "USE",
            "Viewpoint",
            "VisibilitySensor",
            "WWWAnchor",    // VRML 1.0
            "WWWInline",    // VRML 1.0
            "WorldInfo"

		};

		int i,max;

        max = temp.length;
		keys = new char[max][];

		for (i=0;i<max;i++)
			keys[i] = temp[i].toCharArray();

		max = temp2.length;

		keys2 = new char[max][];

		for (i=0;i<max;i++)
			keys2[i] = temp2[i].toCharArray();
	}

	protected void scanLine(int i)
	{
	    putLineInfo(i, lines.getLineInfo(i));
	}

	public LineInfo createLineInfo( int i )
	{
	    LineInfo result = new LineInfo( lines.getLine( i ));
	    putLineInfo( i, result );
	    return( result );
	}


	void putLineInfo( int i, LineInfo hi )
	{
		char	c,d,e;
		String	w;
		boolean	isword,inword,incharliteral;
		int		pos,start,max;

		max = fillBuffer(i);

		pos = -1;
		inword = incharliteral =false;
		start = 0;
		c = d = 0;
		keyCt = 0;

		if (inLiteral)
		{
			keyStarts[keyCt] = 0;
		}

		while (++pos <= max)
		{
			e = d;
			d = c;

			if (pos < max)
				c = buffer[pos];
			else
				c = 0;

			if ((c == '"') && (d != '\\') && (d != '\''))
			{
				if (!inLiteral)
				{
					inLiteral = true;
					keyStarts[keyCt] = pos;
				}
				else
				{
					inLiteral = false;
					keyEnds[keyCt] = pos+1;
					keyTypes[keyCt] = QUOTE;
					keyCt++;
				}
				inword = false;
			}

			if (inLiteral)
			    continue;

			if ((c =='\'') && ((d != '\\' ) || ((d == '\\' ) && (e == '\\' ))))
			{
				if(!incharliteral)
				{
					incharliteral = true;
					keyStarts[keyCt] = pos;
				}
				else
				{
					incharliteral = false;
					keyEnds[keyCt] = pos+1;
					keyTypes[keyCt] = QUOTE;
					keyCt++;
				}
				inword = false;
			}

			if (incharliteral)
				continue;

            // If the current character is upper case or lower case, we might be in a word
			if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <='z')) || ((c >= '0') && (c<='9')) || ( c == '_' ))
			{
				if (!inword && ((d < 'A') || (d > 'Z')) && ((d < 'a') || (d > 'z')))
				{
					keyStarts[keyCt] = start = pos;
					inword = true;
				}

				continue;
			}
			else
			{
				if (inword)
				{
					if ((c < 'A') || (c > 'Z'))
					{
						if (matchKeys(start,pos))
						{
							keyEnds[keyCt] = pos;
							keyTypes[keyCt] = KEYWORD;
							keyCt++;
						}
						else if (matchKeys2(start,pos))
						{
						    keyEnds[keyCt] = pos;
						    keyTypes[keyCt] = KEYWORD2;
						    keyCt++;
						}
					}
					inword = false;
					continue;
				}
			}

			if ((c == '#') && !(inComment || inLiteral))
			{
				keyStarts[keyCt] = pos;
				keyEnds[keyCt] = max;
				keyTypes[keyCt] = COMMENT;
				keyCt++;
				pos = max;	// bail, we don't want any more parsing on this line
			}
		}

		if (inComment)
		{
			keyEnds[keyCt] = max;
			keyTypes[keyCt] = COMMENT;
			keyCt++;
		}

		if (inLiteral)
		{
			keyEnds[keyCt] = max;
			keyTypes[keyCt] = QUOTE;
			keyCt++;
		}

		hi.inComment = inComment;
		hi.inLiteral = inLiteral;
		hi.keyCt = keyCt;
		hi.keyStarts = new short[keyCt];
		hi.keyEnds = new short[keyCt];
		hi.keyTypes = new byte[keyCt];

		for (int j=0; j<keyCt; j++)
		{
			hi.keyStarts[j] = (short)keyStarts[j];
			hi.keyEnds[j] = (short)keyEnds[j];
			hi.keyTypes[j] = keyTypes[j];
		}
	}
}
