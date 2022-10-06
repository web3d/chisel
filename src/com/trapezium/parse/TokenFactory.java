/*
 * @(#)TokenFactory.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.parse;

import com.trapezium.util.ReturnInteger;
import com.trapezium.util.KeywordList;
import com.trapezium.util.ByteString;

/**
 *  Assigns a range of characters within a line to a token of a particular
 *  type.  Adapted from initial 1.0 strategy of creating Token objects.
 *  This approach abandoned due to OutOfMemory exception, tokens now
 *  managed within TokenEnumerator with arrays.
 *
 *  The optimization that required this has complicated the code, since
 *  it was not initially designed this way.  The TokenFactory is a single
 *  class that has taken the place of several classes in the previous
 *  architecture.  The port from one architecture to the other was very
 *  straightforward, but has resulted in what appears to be very unusual
 *  and messy class.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 31 Dec 1997
 *
 *  @since           1.1
 *  @see             TokenTypes
 *  @see             TokenEnumerator
 */
public class TokenFactory implements TokenTypes {
    boolean unterminatedString = false;
    boolean allowUnterminatedString = true;

    /** class constructor */
    public TokenFactory() {
        this( true );
    }
    
    public TokenFactory( boolean allowUnterminatedString ) {
        this.allowUnterminatedString = allowUnterminatedString;
    }
        
    void setUnterminatedString( boolean value ) {
        if ( allowUnterminatedString ) {
            unterminatedString = value;
        }
    }

    /**
     *  create token information for the next token in a string
     *
     *  @param  s      the ByteString containing the token
     *  @param  offset the start offset of the text to check for the token
     *  @param  offsetReturn  output parameter with offset of start of next token,
     *                        set to -1 if no token is created
     *  @param  typeReturn    output parameter with type of next token
     *  @param  sizeReturn    output parameter with size of next token
     */
	public void tokenize( ByteString s, int offset, ReturnInteger offsetReturn,
	    ReturnInteger typeReturn, ReturnInteger sizeReturn ) {
	    int len = s.length();
		if (( len - offset ) == 0 ) {
		    offsetReturn.setValue( 0 );
		    typeReturn.setValue( EmptyLine );
		    sizeReturn.setValue( 1 );
		    return;
		} else if ( unterminatedString ) {
			BlackToken_Factory( s, offset, offsetReturn, typeReturn, sizeReturn );
			return;
		}

        // skip all white characters, we don't make tokens out of them
		while (( offset < len ) && isWhiteChar( s.charAt( offset ))) {
		    offset++;
		}

		// If there is nothing left in this line, set offsetReturn to -1 to indicate this
		if ( offset == len ) {
		    offsetReturn.setValue( -1 );
		    return;
		}

        BlackToken_Factory( s, offset, offsetReturn, typeReturn, sizeReturn );
	}

	//
	//  The following static public methods are used for specific character identifications:
	//
	//     isWhiteChar
	//     isBlackChar
	//     isCommentChar
	//     isBracketChar
	//     isBraceChar
	//     isNumber
	//

    /** is the character white space? */
	static public boolean isWhiteChar( char x ) {
		switch ( x ) {
		case ' ':
		case '\t':
		case ',':
		case '\n':
		case '\r':
		case 0x1a:
			return( true );
		default:
			return( false );
		}
	}

    /** is the character black space? */
	static public boolean isBlackChar( char x ) {
		if ( isWhiteChar( x )) {
			return( false );
		} else {
			return( true );
		}
	}

    /** is the character a special vrml character #[]{}" */
	static public boolean isSpecialChar( char x ) {
		return(( x == '#' ) || isQuoteChar( x ) || isBraceChar( x ) || isBracketChar( x ));
	}

    /** is the character a vrml comment character # */
	static public boolean isCommentChar( char x ) {
		if ( x == '#' ) {
			return( true );
		} else {
			return( false );
		}
	}

    /** is the character a vrml quote character "  */
	static public boolean isQuoteChar( char x ) {
		if ( x == '"' ) {
			return( true );
		} else {
			return( false );
		}
	}

    /** is the character a bracket [] */
	static public boolean isBracketChar( char x ) {
		if (( x == '[' ) || ( x == ']' )) {
			return( true );
		} else {
			return( false );
		}
	}

    /** is the character a left bracket? */
	static public boolean isLeftBracket( char x ) {
	    return( x == '[' );
	}

    /** is the character a right bracket? */
	static public boolean isRightBracket( char x ) {
	    return( x == ']' );
	}

    /** is the character a left brace? */
	static public boolean isLeftBrace( char x ) {
	    return( x == '{' );
	}

    /** is the character a right brace? */
	static public boolean isRightBrace( char x ) {
	    return( x == '}' );
	}

    /* is the character a brace {} */
	static public boolean isBraceChar( char x ) {
		if (( x == '{' ) || ( x == '}' )) {
			return( true );
		} else {
			return( false );
		}
	}

    /** could the character be part of a number? */
	static public boolean isNumberChar( char x ) {
		switch( x ) {
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case '.':
		case '-':
		case '+':
		case 'e':
		case 'E':
			return( true );
		default:
			return( false );
		}
	}

    /** create the next token, already known to exist when this is called
     *
     *  @param s      the ByteString containing the token
     *  @param offset offset of the start of the token
     *  @param offsetReturn  output parameter, start offset of the token
     *  @param typeReturn    output parameter, type of the token
     *  @param sizeReturn    output parameter, size of the token
     */
	void BlackToken_Factory( ByteString s, int offset, ReturnInteger offsetReturn,
	    ReturnInteger typeReturn, ReturnInteger sizeReturn ) {
		if ( unterminatedString ) {
		    typeReturn.setValue( QuotedStringContinuation );
		    QuotedStringContinuation_Factory( s, offset, offsetReturn, sizeReturn );
		    return;
		}

		// determine type
		char firstChar = s.charAt( offset );

		if ((( firstChar >= 'a' ) && ( firstChar <= 'z' )) || (( firstChar >= 'A' ) && ( firstChar <= 'Z' ))) {
		    typeReturn.setValue( NameToken );
			NameToken_Factory( s, offset, offsetReturn, sizeReturn );
			keywordCheck( s, typeReturn, offset, sizeReturn.getValue() );
		} else if ( isNumberSequence( s, offset )) {
			NumberToken_Factory( s, offset, offsetReturn, sizeReturn, typeReturn );
		} else if ( firstChar == '#' ) {
		    typeReturn.setValue( CommentToken );
			CommentToken_Factory( s, offset, offsetReturn, sizeReturn );
		} else if ( firstChar == '[' ) {
		    typeReturn.setValue( LeftBracket );
		    sizeReturn.setValue( 1 );
		    offsetReturn.setValue( offset );
		} else if ( firstChar == '{' ) {
		    typeReturn.setValue( LeftBrace );
		    sizeReturn.setValue( 1 );
		    offsetReturn.setValue( offset );
		} else if ( firstChar == ']' ) {
		    typeReturn.setValue( RightBracket );
		    sizeReturn.setValue( 1 );
		    offsetReturn.setValue( offset );
		} else if ( firstChar == '}' ) {
		    typeReturn.setValue( RightBrace );
		    sizeReturn.setValue( 1 );
		    offsetReturn.setValue( offset );
		} else if ( firstChar == '"' ) {
		    setUnterminatedString( true );
			typeReturn.setValue( QuotedString );
			QuotedString_Factory( s, offset, offsetReturn, sizeReturn );
		} else {
		    typeReturn.setValue( NameToken );
			NameToken_Factory( s, offset, offsetReturn, sizeReturn );
			keywordCheck( s, typeReturn, offset, sizeReturn.getValue() );
		}
	}
	
	/** convert the type to Keyword1Token or Keyword2Token of appropriate */
	void keywordCheck( ByteString s, ReturnInteger typeReturn, int offset, int size ) {
	    char schar = s.charAt( offset );
	    KeywordList k1 = Keywords.getKeyList1( schar );
	    KeywordList k2 = Keywords.getKeyList2( schar );
	    if ( k1 != null ) {
	        if ( k1.find( s, offset, size )) {
	            typeReturn.setValue( Keyword1Token );
	            return;
	        }
	    }
	    if ( k2 != null ) {
	        if ( k2.find( s, offset, size )) {
	            typeReturn.setValue( Keyword2Token );
	            return;
	        }
	    }
	}
	

    /** can the character be the first character in a numeric token? */
    boolean isFirstNumberChar( char x ) {
        if ( x == '.' ) return( true );
        if ( x == '+' ) return( true );
        if ( x == '-' ) return( true );
        if (( x >= '0' ) && ( x <= '9' )) return( true );
        return( false );
    }
    
    /** is the character a digit? */
    boolean isDigit( char x ) {
        return(( x >= '0' ) && ( x <= '9' ));
    }
    
    /** is this a number sequence? */
    boolean isNumberSequence( ByteString s, int offset ) {
        if ( !isFirstNumberChar( s.charAt( offset ))) {
            return( false );
        }
        boolean firstDigit = isDigit( s.charAt( offset ));
        offset++;
        int slen = s.length();
        boolean additionalChars = false;
        while (( offset < slen ) && isNumberChar( s.charAt( offset ))) {
            offset++;
            additionalChars = true;
        }
        if ( !firstDigit && !additionalChars ) {
            return( false );
        }
        if ( offset == slen ) {
            return( true );
        }
        return( isWhiteChar( s.charAt( offset )) || isBracketChar( s.charAt( offset )));
    }
    
    /** create a token 
     *  
     *  @param s      ByteString containing token
     *  @param offset starting offset of token
     *  @param sizeReturn  output parameter, size of resulting token
     */
	void createToken( ByteString s, int offset, ReturnInteger sizeReturn ) {
		int numberCharsProcessed = 0;
		if ( s != null ) {
			while ( numberCharsProcessed < ( s.length() - offset ) &&
				isAppropriate( s.charAt( numberCharsProcessed + offset ))) {
				numberCharsProcessed++;
			}
		}
		sizeReturn.setValue( numberCharsProcessed );
	}

    /**
     *  is the character appropriate for the token currently being created?
     */
	boolean isAppropriate( char x ) {
	    switch( currentType ) {
        case QuotedString:
        case QuotedStringContinuation:
            return( QuotedString_isAppropriate( x ));
        case CommentToken:
            return( CommentToken_isAppropriate( x ));
        case NumberToken:
            return( NumberToken_isAppropriate( x ));
        case NameToken:
            return( NameToken_isAppropriate( x ));
        default:
            return( false );
        }
    }


	// isAppropriate was simpler when Tokens were objects and this was done with
    // subclasses, now done with switch based on currentType
    int currentType;

	boolean firstQuoteFound = false;
	boolean prevCharWasEscape = false;
	boolean thatsAllFolks = false;

    /** create a quoted string token */
	public void QuotedString_Factory( ByteString s, int offset, ReturnInteger offsetReturn, ReturnInteger sizeReturn ) {
	    firstQuoteFound = false;
	    prevCharWasEscape = false;
	    thatsAllFolks = false;
	    currentType = QuotedString;
	    offsetReturn.setValue( offset );
	    createToken( s, offset, sizeReturn );
	}

    /** has a quote been located? */
	public boolean firstQuoteLocated() {
		return( firstQuoteFound );
	}

    /** is the character valid for a quoted string? */
	public boolean QuotedString_isAppropriate( char x ) {
		if ( thatsAllFolks ) {
			return( false );
		} else if ( !firstQuoteLocated() ) {
			if ( x == '"' ) {
				firstQuoteFound = true;
				return( true );
			} else {
				return( false );
			}
		} else {
			if ( x == '\\' ) {
				prevCharWasEscape = true;
				return( true );
			} else if ( x == '"' ) {
				if ( !prevCharWasEscape ) {
					thatsAllFolks = true;
					unterminatedString = false;
					return( true );
				}
				prevCharWasEscape = false;
				return( true );
			} else {
				prevCharWasEscape = false;
				return( true );
			}
		}
	}

    /** create a quoted string continuation token */
	public void QuotedStringContinuation_Factory( ByteString s, int offset, ReturnInteger offsetReturn, ReturnInteger sizeReturn ) {
	    firstQuoteFound = true;
	    prevCharWasEscape = false;
	    thatsAllFolks = false;
	    currentType = QuotedStringContinuation;
	    offsetReturn.setValue( offset );
	    createToken( s, offset, sizeReturn );
	}

    /** create a comment token */
    public void CommentToken_Factory( ByteString s, int offset, ReturnInteger offsetReturn, ReturnInteger sizeReturn ) {
        currentType = CommentToken;
        offsetReturn.setValue( offset );
        createToken( s, offset, sizeReturn );
    }

    /** is the character appropriate for a comment token */
	public boolean CommentToken_isAppropriate( char x ) {
		if (( x == '\n' ) || ( x == '\r' )) {
			return( false );
		} else {
			return( true );
		}
	}

    /** create a number token */
    public void NumberToken_Factory( ByteString s, int offset, 
	        ReturnInteger offsetReturn, ReturnInteger sizeReturn, ReturnInteger typeReturn ) {
	    currentType = NumberToken;
	    offsetReturn.setValue( offset );
	    createToken( s, offset, sizeReturn );
	    setNumberTypeReturn( s, offset, sizeReturn.getValue(), typeReturn );
    }
    
    
    boolean isBadValue( int ndigits, ByteString s, int offset, int size, boolean eAllowed, boolean dotAllowed ) {
        if ( ndigits == size ) {
            return( false );
        } else {
            boolean result = true;
            char nextchar = s.charAt( offset + ndigits );
            if ( eAllowed ) {
                if (( nextchar == 'e' ) || ( nextchar == 'E' )) {
                    return( false );
                }
            }
            if ( dotAllowed ) {
                if ( nextchar == '.' ) {
                    return( false );
                }
            }
            return( true );
        }
    }
    
    public int getNumberValue( ByteString s, int offset, int size ) {
        int result = 0;
        int base = 10;
        for ( int i = 0; i < size; i++ ) {
            char c = s.charAt( offset + i );
            if (( c >= '0' ) && ( c <= '9' )) {
                int n = c - '0';
                result = result*base + n;
            } else {
                break;
            }
        }
        return( result );
    }
    
    public int getNumberValue2( ByteString s, int offset, int size ) {
        if ( s.charAt( offset ) == '0' ) {
            return( 999 );
        } else {
            return( getNumberValue( s, offset, size ));
        }
    }        

    public int getNumberDigits( ByteString s, int offset, int size ) {
        int numberDigits = 0;
        for ( int i = 0; i < size; i++ ) {
            char c = s.charAt( offset + i );
            if (( c >= '0' ) && ( c <= '9' )) {
                numberDigits++;
            } else {
                break;
            }
        }
        return( numberDigits );
    }
    
   
    public void setNumberTypeReturn( ByteString s, int offset, int size, ReturnInteger typeReturn ) {
        // now simplified to set type to either NumberToken or BadNumber
        
        // eat initial +,-,.
        char firstChar = s.charAt( offset );
        boolean gotDot = false;
        boolean gotDigits = false;
        if (( firstChar == '+' ) || ( firstChar == '-' ) || ( firstChar == '.' )) {
            offset++;
            size--;
            if ( firstChar == '.' ) {
                gotDot = true;
            }
        }
        if (( firstChar == '+' ) || ( firstChar == '-' )) {
            char nextChar = s.charAt( offset );
            if ( nextChar == '.' ) {
                gotDot = true;
                offset++;
                size--;
            }
        }
        
        // get digits
        while (( size > 0 ) && isDigit( s.charAt( offset ))) {
            gotDigits = true;
            offset++;
            size--;
        }
        
        if ( !gotDigits ) {
            typeReturn.setValue( BadNumber );
            return;
        }
        
        // If there is more, it has to be either a '.' or an 'e' or an 'E'
        boolean gotE = false;
        if ( size == 0 ) {
            typeReturn.setValue( NumberToken );
            return;
        }
        char nextChar = s.charAt( offset );
        if (( nextChar == '.' ) && gotDot ) {
            typeReturn.setValue( BadNumber );
            return;
        }
        if ( nextChar == '.' ) {
            gotDot = true;
        } else if (( nextChar == 'e' ) || ( nextChar == 'E' )) {
            gotE = true;
        } else {
            typeReturn.setValue( BadNumber );
            return;
        }
        offset++;
        size--;

        // if we got an E, there has to be more
        if ( gotE ) {
            if ( size == 0 ) {
                typeReturn.setValue( BadNumber );
                return;
            }
        }
        
        // if we got an E, the next char may be a sign
        if ( gotE ) {
            nextChar = s.charAt( offset );
            if (( nextChar == '+' ) || ( nextChar == '-' )) {
                offset++;
                size--;
            }
        }
        
        // check for more digits
        while (( size > 0 ) && isDigit( s.charAt( offset ))) {
            offset++;
            size--;
        }
        
        // If there is more, and we haven't got an 'e' or 'E' yet, that is
        // the only valid thing
        if ( size == 0 ) {
            typeReturn.setValue( NumberToken );
            return;
        }
        
        if ( gotE ) {
            typeReturn.setValue( BadNumber );
            return;
        }
        
        nextChar = s.charAt( offset );
        if (( nextChar != 'e' ) && ( nextChar != 'E' )) {
            typeReturn.setValue( BadNumber );
            return;
        }
        offset++;
        size--;
        if ( size == 0 ) {
            typeReturn.setValue( BadNumber );
            return;
        }
        
        // After the 'e', sign is allowed
        nextChar = s.charAt( offset );
        if (( nextChar == '+' ) || ( nextChar == '-' )) {
            offset++;
            size--;
        }
        
        if ( size == 0 ) {
            typeReturn.setValue( BadNumber );
            return;
        }
        
        while (( size > 0 ) && isDigit( s.charAt( offset ))) {
            size--;
            offset++;
        }
        
        if ( size != 0 ) {
            typeReturn.setValue( BadNumber );
        } else {
            typeReturn.setValue( NumberToken );
        }
    }

    /** is the character appropriate for a number token */
	public boolean NumberToken_isAppropriate( char x ) {
		return( isNumberChar( x ));
	}

    /** create a name token
     *
     *  @param s  the text containing the token
     *  @param offset  offset in ByteString of the start of the token
     *  @param offsetReturn  output parameter, offset of start of token
     *  @param sizeReturn    output parameter, size of the token
     */
    public void NameToken_Factory( ByteString s, int offset, ReturnInteger offsetReturn, ReturnInteger sizeReturn ) {
        offsetReturn.setValue( offset );
        currentType = NameToken;
        createToken( s, offset, sizeReturn );
    }

    /** is the character appropriate for a name token */
	public boolean NameToken_isAppropriate( char x ) {
		return( !isWhiteChar( x ) && !isSpecialChar( x ));
	}

}
