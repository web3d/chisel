/*
 * @(#)SFImageValue.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.grammar.Table7;

/**
 *  Scene graph component for an SFImage field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 12 Feb 1998, base profile warning
 *  @version         1.1, 8 Dec 1997
 *
 *  @since           1.0
 */
public class SFImageValue extends SFFieldValue {
	public SFImageValue() {
		super();
	}

	public SFImageValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
		Value width = new Value( tokenOffset );
		Value height = new Value( v );
		addChild( width );
		addChild( height );
		addChild( new Value( v ));
		String widthStr = v.toString( width.getFirstTokenOffset() );
		String heightStr = v.toString( height.getFirstTokenOffset() );
		int widthInt = Integer.parseInt( widthStr );
		int heightInt = Integer.parseInt( heightStr );
		if ( widthInt > Table7.SFImageWidthLimit ) {
		    width.setError( "Nonconformance, width exceeds base profile limit " + Table7.SFImageWidthLimit );
		}
		if ( heightInt > Table7.SFImageHeightLimit ) {
		    height.setError( "Nonconformance, height exceeds base profile limit " + Table7.SFImageHeightLimit );
		}
		int additionalValues = widthInt * heightInt;
		int valueCount = 0;
		int[] values = new int[ additionalValues ];
		int valueIdx = 0;
		int lastValue = -1;
		int runSaving = 0;
		boolean doRunSaving = false;
		for ( int i = 0; i < additionalValues; i++ ) {
			int state = v.getState();
			tokenOffset = v.getNextToken();
			if ( !valid( tokenOffset, v )) {
				Value vv = new Value( tokenOffset );
				vv.setError( "Invalid value" );
				addChild( vv );
			} else {
				valueCount++;
				if ( lastValue == v.getIntValue( tokenOffset )) {
					if ( doRunSaving ) {
						runSaving++;
					}
					doRunSaving = true;
				} else {
					lastValue = v.getIntValue( tokenOffset );
					doRunSaving = false;
				}
				valueIdx = addValue( valueIdx, values, v.getIntValue( tokenOffset ));
			}
			if ( v.isRightBrace( tokenOffset )) {
				v.setState( state );
				Value vv = new Value( tokenOffset );
				vv.setError( "Not enough values, expected " + additionalValues + ", got " + valueCount );
				addChild( vv );
				break;
			}
		}
	}

	int addValue( int valueIdx, int[] values, int value ) {
		for ( int i = 0; i < valueIdx; i++ ) {
			if ( value == values[i] ) {
				return( valueIdx );
			}
		}
		values[ valueIdx ] = value;
		valueIdx++;
		return( valueIdx );
	}

	public boolean valid( int tokenOffset, TokenEnumerator v ) {
		boolean hexVal = false;
		int tokenLength = v.length();
		for ( int i = 0; i < tokenLength; i++ ) {
			if ( v.charAt( i ) == 'x' ) {
				if ( i > 1 ) {
					return( false );
				} else if ( i == 1 ) {
					if ( v.charAt( 0 ) != '0' ) {
						return( false );
					}
				}
				hexVal = true;
			} else if (( v.charAt( i ) >= 'a' ) && ( v.charAt( i ) <= 'f' )) {
				if ( !hexVal ) {
					return( false );
				}
			} else if (( v.charAt( i ) >= 'A' ) && ( v.charAt( i ) <= 'F' )) {
				if ( !hexVal ) {
					return( false );
				}
			} else if (( v.charAt( i ) < '0' ) || ( v.charAt( i ) > '9' )) {
				return( false );
			}
		}
		return( true );
	}
}

