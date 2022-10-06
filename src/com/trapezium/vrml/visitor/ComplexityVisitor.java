/*
 * @(#)ComplexityVisitor.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.fields.MFStringValue;
import com.trapezium.vrml.fields.SFBoolValue;
import com.trapezium.vrml.fields.SFStringValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.pattern.Visitor;

import com.trapezium.parse.TokenEnumerator;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 *  Visits all nodes collecting scene graph complexity info.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 24 April 1998, infinite loop bug fix
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
public class ComplexityVisitor extends Visitor {
	ComplexityData cd;
	Hashtable visited;

    /** class constructor */
	public ComplexityVisitor( ComplexityData cd, TokenEnumerator v ) {
		super( v );
		this.cd = cd;
		visited = new Hashtable();
	}

    /** get the ComplexityData from this visitor, used to add to sum total */
	public ComplexityData getComplexityData() {
		return( cd );
	}
	
    /** */
    public int getInlineCount() {
        return( cd.getInlineCount() );
    }

    /** set the warning count */
	public void setWarningCount( int wc ) {
		cd.setWarningCount( wc );
	}

    /** set the error count */
	public void setErrorCount( int ec ) {
		cd.setErrorCount( ec );
	}
	
	/** set the nonconformance count */
	public void setNonconformanceCount( int ncc ) {
	    cd.setNonconformanceCount( ncc );
	}
	

    /** Visitor pattern, look for geometry fields and update data.
     *  <P>
     *  Doesn't visit PROTOs because PROTO declarations don't create
     *  objects.
     */
	public boolean visitObject( Object a ) {
//	    System.out.println( spacer() + "visiting " + a.getClass().getName() );
		if ( a instanceof PROTO ) {
			return( false );
		} else if ( a instanceof Field ) {
			Field f = (Field)a;
			if ( f instanceof Node ) {
			    if ( visited.get( f ) != null ) {
			        return( false );
			    }
			    visited.put( f, f );
			    if ( f instanceof DEFUSENode ) {
			        DEFUSENode dun = (DEFUSENode)f;
			        if ( !dun.isDEF() ) {
			            Node n = dun.getNode();
			            VrmlElement p = f;
			            VrmlElement test = n;
			            while ( true ) {
			                p = p.getParent();
			                if ( p == null ) {
			                    break;
			                }
			                if ( p == test )  {
			                    return( false );
			                }
			            }
			        }
			    }
			                
			    return( true );
			}
			String fName = f.getFieldId();
			if ( fName != null ) {
				if ( fName.compareTo( "geometry" ) == 0 ) {
					FieldValue fv = f.getFieldValue();
					if ( fv instanceof SFNodeValue ) {
						SFNodeValue sfnv = (SFNodeValue)fv;
						incCounts( sfnv.getNode(), sfnv.getNodeName() );
					}
				} else if ( fName.compareTo( "url" ) == 0 ) {
				    FieldValue fv = f.getFieldValue();
				    if ( fv instanceof MFStringValue ) {
				        MFStringValue mfnv = (MFStringValue)fv;
				        int nvalues = mfnv.numberValues();
				        for ( int i = 0; i < nvalues; i++ ) {
				            FieldValue xfv = mfnv.getFieldValueAt( i );
				            if ( xfv instanceof SFStringValue ) {
				                cd.incInline( dataSource.toString( xfv.getFirstTokenOffset() ));
				            }
				        }
				    }
				}
			}
		}
		return( true );
	}
    
    /**  increment complexity data info from a geometry node */
	void incCounts( Node n, String nodeName ) {
		if ( n == null ) {
			return;
		}
		if ( nodeName == null ) {
			return;
		}

		if ( nodeName.compareTo( "Cone" ) == 0 ) {
			cd.incConeCount();
		} else if ( nodeName.compareTo( "Sphere" ) == 0 ) {
			cd.incSphereCount();
		} else if ( nodeName.compareTo( "Cylinder" ) == 0 ) {
			cd.incCylinderCount();
		} else if ( nodeName.compareTo( "Box" ) == 0 ) {
			cd.incPolygonCount( 6 );
		} else if ( nodeName.compareTo( "IndexedFaceSet" ) == 0 ) {
			Field coordIndex = n.getField( "coordIndex" );
			int numberFaces = 0;
			if ( coordIndex != null ) {
				// get each number token, get int value, set bit
				int scannerOffset = coordIndex.getFirstTokenOffset();
				dataSource.setState( scannerOffset );
				boolean finalNeg1 = true;
				while ( scannerOffset != -1 ) {
					if ( dataSource.isRightBracket( scannerOffset )) {
						break;
					} else if ( dataSource.isNumber( scannerOffset )) {
						int val = dataSource.getIntValue( scannerOffset);
						if ( val == -1 ) {
							numberFaces++;
							finalNeg1 = true;
						} else {
							finalNeg1 = false;
						}
					}
					scannerOffset = dataSource.getNextToken();
				}
				if ( !finalNeg1 ) {
					numberFaces++;
				}
			}
			cd.incPolygonCount( numberFaces );
			if ( !n.getBoolValue( "solid" )) {
				cd.incPolygonCount( numberFaces ); // double because back side
			}
		} else if ( nodeName.compareTo( "ElevationGrid" ) == 0 ) {
			Field xDim = n.getField( "xDimension" );
			Field zDim = n.getField( "zDimension" );
			if (( xDim != null ) && ( zDim != null )) {
				FieldValue xDimValue = xDim.getFieldValue();
				FieldValue zDimValue = zDim.getFieldValue();
				if (( xDimValue != null ) && ( zDimValue != null )) {
					int xValue = dataSource.getIntValue( xDimValue.getFirstTokenOffset() );
					int zValue = dataSource.getIntValue( zDimValue.getFirstTokenOffset() );
					if (( xValue > 1 ) && ( zValue > 1 )) {
						int polygonCount = ( xValue - 1 ) * ( zValue - 1 );
						cd.incPolygonCount( polygonCount );
						if ( !n.getBoolValue( "solid" )) {
							cd.incPolygonCount( polygonCount );
						}
					}
				}
			}
		} else if ( nodeName.compareTo( "Extrusion" ) == 0 ) {
			Field crossSection = n.getField( "crossSection" );
			Field spine = n.getField( "spine" );
			int polyCount = 0;
			int spineEntries = 2;
			int crossSectionEntries = 5;
			if ( crossSection != null ) {
				FieldValue crossSectionValue = crossSection.getFieldValue();
				if ( crossSectionValue instanceof MFFieldValue ) {
					MFFieldValue mfCrossSection = (MFFieldValue)crossSectionValue;
					crossSectionEntries = mfCrossSection.getRawValueCount()/2;
				}
			}
			if ( spine != null ) {
				FieldValue spineValue = spine.getFieldValue();
				if ( spineValue instanceof MFFieldValue ) {
					MFFieldValue mfSpine = (MFFieldValue)spineValue;
					spineEntries = mfSpine.getRawValueCount()/3;
				}
			}
			if (( spineEntries > 0 ) && ( crossSectionEntries > 0 )) {
				polyCount = ( spineEntries - 1 ) * (( crossSectionEntries - 1 ) * 2 );
			}
			if ( n.getBoolValue( "beginCap" )) {
				polyCount++;
			}
			if ( n.getBoolValue( "endCap" )) {
				polyCount++;
			}
			if ( !n.getBoolValue( "solid" )) {
				polyCount = polyCount * 2;
			}
			cd.incPolygonCount( polyCount );
		}
	}

    /** print a summary of the information to a PrintStream */
	public void summary( PrintStream ps ) {
		cd.summary( ps );
	}
}

