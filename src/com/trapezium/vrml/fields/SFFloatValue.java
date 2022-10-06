/*
 * @(#)SFFloatValue.java
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
import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;

/**
 *  Scene graph component for an SFFloat field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
public class SFFloatValue extends SFFieldValue implements ValueTypes {
    
	public SFFloatValue() {
		super();
	}

	public SFFloatValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
		validateAndAdd( this, tokenOffset, v, AllValues );
	}

    /** Check if this is a valid SFFloat value */
	static public boolean valid( int tokenOffset, TokenEnumerator s ) {
	    if ( tokenOffset == -1 ) {
	        return( false );
	    }
	    return( s.getType( tokenOffset ) == TokenTypes.NumberToken );
	}

    static public void validateAndAdd( VrmlElement parent, int tokenOffset, TokenEnumerator v ) {
        validateAndAdd( parent, tokenOffset, v, AllValues );
    }
    
    /** add if it is a bad value, return type of value:  Unknown, Positive, NegativeOne, Negative, NonNegative */
	static public int validateAndAdd( VrmlElement parent, int tokenOffset, TokenEnumerator v, int valueType ) {
	    int result = Unknown;
	    int state = v.getState();
		int idx = 0;
		Value value = null;
		if ( tokenOffset == -1 ) {
			return( Unknown );
		}
		v.setState( tokenOffset );

		// optional sign
		boolean negative = false;
		if ( v.charAt( 0 ) == '+' ) {
			idx++;
		} else if ( v.charAt( 0 ) == '-' ) {
			idx++;
			negative = true;
			if ( valueType == PositiveValues ) {
			    value = new Value( tokenOffset );
			    value.setError( "Value must be positive" );
			    parent.addChild( value );
			    v.setState( state );
			    return( Negative );
			}
		}

		// initial digit sequence (optional)
		boolean initialDigitSequence = false;
		int tokenLength = v.length();
		boolean nonZeroDigit = false;
		for ( ; idx < tokenLength; idx++ ) {
			if ( !Character.isDigit( v.charAt( idx ))) {
				break;
			}
			if ( v.charAt( idx ) != '0' ) {
			    nonZeroDigit = true;
			}
			initialDigitSequence = true;
		}

		// '.', optional
		boolean foundE = false;
		if ( idx < tokenLength ) {
		    char vchar = v.charAt( idx );
			if ( vchar == '.' ) {
				idx++;
			} else if (( vchar == 'e' ) || ( vchar == 'E' )) {
			    foundE = true;
			} else {
				if ( value == null ) {
				    value = new Value( tokenOffset );
				}
//				String s = v.toString( tokenOffset );
//				v.detailDump( tokenOffset );
				value.setError( "invalid number" );
				parent.addChild( value );
				v.setState( state );
				return( Unknown );
			}
		}

		// subsequent digit sequence, optional if there was a previous one
		boolean subsequentDigitSequence = false;
		if ( !foundE ) {
    		for ( ; idx < tokenLength; idx++ ) {
    			if ( !Character.isDigit( v.charAt( idx ))) {
    				break;
    			}
    			if ( !nonZeroDigit ) {
    			    if ( v.charAt( idx ) != '0' ) {
    			        nonZeroDigit = true;
    			    }
    			}
    			subsequentDigitSequence = true;
    		}
    	}

		if ( !initialDigitSequence && !subsequentDigitSequence ) {
			if ( value == null ) {
			    value = new Value( tokenOffset );
			}
			value.setError( "initial digits not found" );
			parent.addChild( value );
			v.setState( state );
			return( Unknown );
		}

		// optional exponent part
		if ( idx < tokenLength ) {
			if (( v.charAt( idx ) == 'e' ) || ( v.charAt( idx ) == 'E' )) {
				idx++;

				// optional sign
				if (( v.charAt( idx ) == '+' ) || ( v.charAt( idx ) == '-' )) {
					idx++;
				}

				boolean edigitSequenceFound = false;
				for ( ; idx < tokenLength; idx++ ) {
					if ( !Character.isDigit( v.charAt( idx ))) {
						break;
					}
					if ( !nonZeroDigit ) {
					    if ( v.charAt( idx ) != '0' ) {
					        nonZeroDigit = true;
					    }
					}
					edigitSequenceFound = true;
				}
				if ( !edigitSequenceFound ) {
					if ( value == null ) {
					    value = new Value( tokenOffset );
					}
					value.setError( "exponent digit sequence not found" );
					parent.addChild( value );
					v.setState( state );
					return( Unknown );
				}
				if ( idx < tokenLength ) {
					if ( value == null ) {
					    value = new Value( tokenOffset );
					}
					value.setError( "non digit found within number" );
				}
			} else {
				if ( value == null ) {
				    value = new Value( tokenOffset );
				}
				value.setError( "invalid number" );
				result = Unknown;
			}
		}
		if (( value == null ) && ( valueType == PositiveValues ) && !nonZeroDigit ) {
		    value = new Value( tokenOffset );
		    value.setError( "value must be greater than zero" );
		    result = Unknown;
		}
		if ( value != null ) {
			parent.addChild( value );
		} else {
		    if ( valueType == BboxSizeValues ) {
		        float f = v.getFloat( tokenOffset );
		        if ( f == -1 ) {
		            result = NegativeOne;
		        } else if ( f < 0 ) {
		            if ( value == null ) {
		                value = new Value( tokenOffset );
		            }
		            value.setError( "size must be positive" );
		            parent.addChild( value );
		        }
		    }
		}
		v.setState( state );
		return( result );
	}
}
