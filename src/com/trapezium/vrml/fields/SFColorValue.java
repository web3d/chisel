/*
 * @(#)SFColorValue.java
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

/**
 *  Scene graph component for an SFColor field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.3, 24 Nov 1998, fixed '1' color adding to scene graph
 *  @version         1.1, 9 Dec 1997
 *
 *  @since           1.0
 */
public class SFColorValue extends SFFieldValue {

	int validChildCount = 0;
	public int numberValidChildren() {
		return( validChildCount );
	}

	public SFColorValue() {
		super();
	}

	public SFColorValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
		Value red = new Value( tokenOffset );
		validate( red, "red", v );
		if ( red.getError() == null ) {
			validChildCount++;
		}
		addChild( red );
		int state = v.getState();
		Value green = new Value( v );
		validate( green, "green", v );
		addChild( green );
		if ( green.getError() == null ) {
			validChildCount++;
		} else {
			tokenOffset = green.getFirstTokenOffset();
			if ( tokenOffset != -1 ) {
				if ( v.isRightBracket( tokenOffset )) {
					v.setState( state );  // bail, let caller deal with right bracket..
					return;
				}
			}
		}
		state = v.getState();
		Value blue = new Value( v );
		validate( blue, "blue", v );
		addChild( blue );
		if ( blue.getError() == null ) {
			validChildCount++;
		} else {
			tokenOffset = blue.getFirstTokenOffset();
			if ( tokenOffset != -1 ) {
				if ( v.isRightBracket( tokenOffset )) {
					v.setState( state );
					return;
				}
			}
		}
	}

    /** Check if a SFColor value is valid.
     *
     *  @param tokenOffset first token of the SFColor
     *  @param v data source
     *
     *  @return true if the SFColor is valid, otherwise false
     */
	static public boolean valid( int tokenOffset, TokenEnumerator v ) {
		if ( !validColor( tokenOffset, v )) {
			return( false );
		}
		int state = v.getState();
		tokenOffset = v.getNextToken();
		if ( !validColor( tokenOffset, v )) {
			v.setState( state );
			return( false );
		}
		tokenOffset = v.getNextToken();
		if ( !validColor( tokenOffset, v )) {
			v.setState( state );
			return( false );
		}
		return( true );
	}

	static public boolean validColor( int tokenOffset, TokenEnumerator v ) {
		int idx = 0;
		boolean foundDigits = false;
		// skip leading 0's
		int vlen = v.length();
		while (( idx < vlen ) && ( v.charAt( idx ) == '0' )) {
			idx++;
			foundDigits = true;
		}

		// look for exponent
		int eidx = idx;
		while ( eidx < vlen ) {
		    if ( v.charAt( eidx ) == 'e' ) return( exponentCheck( tokenOffset, v ));
		    if ( v.charAt( eidx ) == 'E' ) return( exponentCheck( tokenOffset, v ));
		    eidx++;
		}

		if (( idx < vlen ) && ( Character.isDigit( v.charAt( idx )))) {
			if ( v.charAt( idx ) == '1' ) {
				idx++;
				if (( idx < vlen ) && ( v.charAt( idx ) == '.' )) {
					idx++;
					while (( idx < vlen ) && ( v.charAt( idx ) == '0' )) {
						idx++;
					}
					if ( idx < vlen ) {
						return( false );
					}
				} else if ( idx < vlen ) {
					return( false );
				} else {
				    return( true );
				}
			}
		}

		// skip '.'
		if (( idx < vlen ) && ( v.charAt( idx ) == '.' )) {
			idx++;
		}

		// skip digits
		while (( idx < vlen ) && ( Character.isDigit( v.charAt( idx )))) {
			idx++;
			foundDigits = true;
		}
		if ( !foundDigits ) {
			return( false );
		} else if ( idx < vlen ) {
			return( false );
		} else {
			return( true );
		}
	}

	static boolean exponentCheck( int tokenOffset, TokenEnumerator v ) {
	    try {
	        float fval = v.getFloat( tokenOffset );
	        return(( fval >= 0 ) && ( fval <= 1 ));
	    } catch( Exception e ) {
	        return( false );
	    }
	}

    void evalidate( Value child, String name, TokenEnumerator v ) {
        try {
            float f = v.getFloat( child.getFirstTokenOffset() );
            if (( f < 0 ) || ( f > 1 )) {
                child.setError( "color " + name + " must be in range 0 to 1" );
            }
        } catch ( Exception e ) {
            child.setError( "invalid number format for color " + name );
        }
    }

	void validate( Value child, String name, TokenEnumerator v ) {
		int value = child.getFirstTokenOffset();
		if ( value == -1 ) {
		    return;
		}
		int state = v.getState();
		v.setState( value );

		int idx = 0;
		boolean foundDigits = false;
		int vlen = v.length();
		// skip leading 0's
		while (( idx < vlen ) && ( v.charAt( idx ) == '0' )) {
			idx++;
			foundDigits = true;
		}

		// look for exponent
		int eidx = idx;
		while ( eidx < vlen ) {
		    if ( v.charAt( eidx ) == 'e' ) {
		        evalidate( child, name, v );
		        v.setState( state );
		        return;
		    }
		    if ( v.charAt( eidx ) == 'E' ) {
		        evalidate( child, name, v );
		        v.setState( state );
		        return;
		    }
		    eidx++;
		}
		if (( idx < vlen ) && ( Character.isDigit( v.charAt( idx )))) {
			if ( v.charAt( idx ) == '1' ) {
				idx++;
				if (( idx < vlen ) && ( v.charAt( idx ) == '.' )) {
					idx++;
					while (( idx < vlen ) && ( v.charAt( idx ) == '0' )) {
						idx++;
					}
					if ( idx < vlen ) {
						if ( Character.isDigit( v.charAt( idx ))) {
							child.setError( "color " + name + " must be in range 0 to 1" );
						} else {
							child.setError( "non digit characters found for color " + name );
						}
					}
				} else if ( idx < vlen ) {
					if ( Character.isDigit( v.charAt( idx ))) {
						child.setError( "color " + name + " must be in range 0 to 1" );
					} else {
						child.setError( "non digit characters found for color " + name );
					}
				}
				v.setState( state );
				return;
			}
			child.setError( "color " + name + " must be in range 0 to 1" );
			v.setState( state );
			return;
		}

		// skip '.'
		if (( idx < vlen ) && ( v.charAt( idx ) == '.' )) {
			idx++;
		}

		// skip digits
		while (( idx < vlen ) && ( Character.isDigit( v.charAt( idx )))) {
			idx++;
			foundDigits = true;
		}
		if ( !foundDigits ) {
			child.setError( "no digits found for color " + name );
		} else if ( idx < v.length() ) {
			child.setError( "non digits found for color " + name );
		}
		v.setState( state );
	}
}



