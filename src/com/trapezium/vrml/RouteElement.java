/*
 * @(#)RouteElement.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;

import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.grammar.VRML97;

/**
 *  Abstract base class for a ROUTE source or ROUTE destination.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
abstract public class RouteElement extends SingleTokenElement {
	String fieldType = null;
	String nodeName = null;
	String fieldName = null;
	public RouteElement( int tokenOffset, TokenEnumerator v, Scene scene ) {
		super( tokenOffset );
		if ( tokenOffset != -1 ) {
			validateRoute( v.toString( tokenOffset ), scene, getOptionalPrefix(), getOptionalSuffix() );
		}
	}

    /** Get the data type of the field */
	public String getFieldType() {
		return( fieldType );
	}

	/** Get the DEF name of the node */
	public String getNodeName() {
	    return( nodeName );
	}

    /** Set the DEF name of the node */
	public void setNodeName( String newName ) {
	    nodeName = newName;
	}

	/** Get the field name */
	public String getFieldName() {
	    return( fieldName );
	}

    /** Verify that the DEF name and field name are valid */
	public void validateRoute( String s, Scene scene, String optionalPrefix, String optionalSuffix ) {
		int dotLoc = s.indexOf( '.' );
		if ( dotLoc > 0 ) {
			nodeName = s.substring( 0, dotLoc );
			DEFUSENode dun = scene.getDEF( nodeName );
//			if ( dun == null ) {
//				setError( "Did not find a DEF with this name" );
//			}
			if (( dotLoc + 1 ) == s.length() ) {
				setError( "missing field name" );
			} else {
				fieldName = s.substring( dotLoc + 1, s.length() );
				if ( dun == null ) {
				    return;
				}
				Node n = dun.getNode();
				if ( n == null ) {
				    return;
				}

				String fieldNameWithSuffix = null;
				String fieldNameWithPrefix = null;
				String fieldNameAlone = null;
				if ( optionalPrefix == null ) {
				    fieldNameWithPrefix = null;
				} else {
				    if ( fieldName.indexOf( optionalPrefix ) == 0 ) {
				        fieldNameAlone = fieldName.substring( optionalPrefix.length() );
				        fieldNameWithPrefix = fieldName;
				    } else {
				        fieldNameAlone = fieldName;
				    }
				}
				if ( optionalSuffix == null ) {
				    fieldNameWithSuffix = null;
				} else {
				    int idx = fieldName.indexOf( optionalSuffix );
				    if (( idx > 0 ) && ( idx == ( fieldName.length() - optionalSuffix.length() ))) {
				        fieldNameAlone = fieldName.substring( 0, fieldName.length() - optionalSuffix.length() );
				        fieldNameWithSuffix = fieldName;
				    } else {
				        fieldNameAlone = fieldName;
				    }
				}

				// if the field name is found, and its an exposedField,
				// we get the type from there
				if ( n.isValidFieldId( fieldNameAlone )) {
				    int interfaceType = n.getInterfaceType( fieldNameAlone );
				    if (( interfaceType == VRML97.exposedField ) || ( interfaceType == getExpectedInterfaceType() )) {
				        fieldType = n.getFieldType( fieldNameAlone );
				    } else {
				        // may be field was explicit with suffix or prefix, check this
				        // type before jumping to any conclusions about it being an error
				        boolean setierr = true;
				        if ( fieldNameWithSuffix != null ) {
				            if ( n.isValidFieldId( fieldNameWithSuffix )) {
				                int suffixNameInterfaceType = n.getInterfaceType( fieldNameWithSuffix );
				                if ( suffixNameInterfaceType == getExpectedInterfaceType() ) {
				                    fieldType = n.getFieldType( fieldNameWithSuffix );
				                    setierr = false;
				                }
				            }
				        } else if ( fieldNameWithPrefix != null ) {
				            if ( n.isValidFieldId( fieldNameWithPrefix )) {
				                int prefixNameInterfaceType = n.getInterfaceType( fieldNameWithPrefix );
				                if ( prefixNameInterfaceType == getExpectedInterfaceType() ) {
				                    fieldType = n.getFieldType( fieldNameWithPrefix );
				                    setierr = false;
				                }
				            }
				        }
				        if ( setierr ) {
    				        setInterfaceError();
    				    }
                    }
                }

				// if the field name is not found, and we have an optional
				// prefix, we try again with that
				else if (( fieldNameWithSuffix != null ) && n.isValidFieldId( fieldNameWithSuffix )) {
				    if ( n.getInterfaceType( fieldNameWithSuffix ) == VRML97.eventOut ) {
				        fieldType = n.getFieldType( fieldNameWithSuffix );
				    } else {
				        noFieldError( fieldName );
				    }
				}

				// if the field name is not found, and we have an optional
				// suffix, we try again with that
				else if (( fieldNameWithPrefix != null ) && n.isValidFieldId( fieldNameWithPrefix )) {
				    if ( n.getInterfaceType( fieldNameWithPrefix ) == VRML97.eventIn ) {
				        fieldType = n.getFieldType( fieldNameWithPrefix );
				    } else {
				        setInterfaceError();
				    }
				}

				// otherwise we have a bad field
				else {
				    noFieldError( fieldName );
				}
			}
		} else if ( dotLoc == 0 ) {
			setError( "DEF name needed here" );
		} else {
			setError( "bad format, should be Node.Field" );
		}
	}

	public boolean isDest() {
		return( false );
	}

	abstract public void noFieldError( String fieldName );
	abstract public String getOptionalPrefix();
	abstract public String getOptionalSuffix();
	abstract public void setInterfaceError();
	abstract public int getExpectedInterfaceType();
}
