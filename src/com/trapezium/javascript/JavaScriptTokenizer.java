package com.trapezium.javascript;

import java.util.Vector;

/** Very limited Javascript parsing, just bracket/brace/paren matching,
 *  and variable assignments.
 */
public class JavaScriptTokenizer {
    static public final int JS_COMMENT = 0;  // "//", or "<!--"
    static public final int JS_MULTILINECOMMENT = 1; // "/*"
    static public final int JS_FUNCTION = 2; // "function"
    static public final int JS_NAME = 3;     // consecutive chars starting with a-zA-Z
    static public final int JS_ASSIGNMENT = 4; // =
    static public final int JS_LEFTBRACKET = 5;
    static public final int JS_RIGHTBRACKET = 6;
    static public final int JS_LEFTBRACE = 7;
    static public final int JS_RIGHTBRACE = 8;
    static public final int JS_LEFTPAREN = 9;
    static public final int JS_RIGHTPAREN = 10;
    static public final int JS_EOF = 11;

    Vector scriptLines;
    Vector tokens;
    boolean inComment;

    public JavaScriptTokenizer() {
        scriptLines = new Vector();
        tokens = new Vector();
        inComment = false;
    }

    public void addLine( String line ) {
        scriptLines.addElement( line );
        parseLine( line );
    }
    
    void addToken( Token t ) {
        if ( t != null ) {
            tokens.addElement( t );
        }
    }
    
    void addToken( int type, int offset ) {
        tokens.addElement( new Token( type, offset ));
    }

    boolean potentialComment;
    boolean potentialFunction;
    Token currentToken;
    void parseLine( String line ) {
        potentialComment = false;
        potentialFunction = false;
        int len = line.length();
        currentToken = null;
        for ( int i = 0; i < len; i++ ) {
            parseChar( i, line.charAt( i ));
        }
        if ( potentialFunction ) {
            startPotentialFunction( ' ' );
        } 
        addToken( currentToken );
    }
    

    void parseChar( int charOffset, char x ) {
        if ( currentToken == null ) {
            startToken( charOffset, x );
        } else if ( currentToken.endToken( x )) {
            tokens.addElement( currentToken );
        }
    }

    StringBuffer potential = new StringBuffer();
    int potentialOffset;
    void startToken( int charOffset, char x ) {
        if ( potentialComment ) {
            startPotentialComment( x );
        } else if ( potentialFunction ) {
            startPotentialFunction( x );
        } else if ( x == '/' ) {
            potential.setLength( 0 );
            potential.append( x );
            potentialComment = true;
            potentialFunction = false;
            potentialOffset = charOffset;
        } else if ( x == '<' ) {
            potential.setLength( 0 );
            potential.append( x );
            potentialComment = true;
            potentialFunction = false;
            potentialOffset = charOffset;
        } else if ( x == 'f' ) {
            potential.setLength( 0 );
            potential.append( x );
            potentialComment = false;
            potentialFunction = true;
            potentialOffset = charOffset;
        } else if ((( x >= 'a' ) && ( x <= 'z' )) || (( x >= 'A' ) && ( x <= 'Z' ))) {
            startName( charOffset );
        } else if ( x == '=' ) {
            addToken( JS_ASSIGNMENT, charOffset );
        } else if ( x == '[' ) {
            addToken( JS_LEFTBRACKET, charOffset );
        } else if ( x == ']' ) {
            addToken( JS_RIGHTBRACKET, charOffset );
        } else if ( x == '{' ) {
            addToken( JS_LEFTBRACE, charOffset );
        } else if ( x == '}' ) {
            addToken( JS_RIGHTBRACE, charOffset );
        } else if ( x == '(' ) {
            addToken( JS_LEFTPAREN, charOffset );
        } else if ( x == ')' ) {
            addToken( JS_RIGHTPAREN, charOffset );
        }
    }

    void startPotentialComment( char x ) {
        potential.append( x );
        String potentialx = new String( potential );
        if ( potentialx.compareTo( "//" ) == 0 ) {
            currentToken = new Token( JS_COMMENT, potentialOffset );
            potentialComment = false;
        } else if ( potentialx.compareTo( "/*" ) == 0 ) {
            currentToken = new Token( JS_MULTILINECOMMENT, potentialOffset );
            inComment = true;
            potentialComment = false;
        } else if ( potentialx.compareTo( "<!" ) == 0 ) {
        } else if ( potentialx.compareTo( "<!-" ) == 0 ) {
        } else if ( potentialx.compareTo( "<!--" ) == 0 ) {
            currentToken = new Token( JS_COMMENT, potentialOffset );
            potentialComment = false;
        } else {
            potentialComment = false;
            startToken( potentialOffset + potential.length() - 1, x );
        }
    }
    
    void startName( int offset ) {
        currentToken = new Token( JS_NAME, offset );
        potentialFunction = false;
    }

    static final String funcstr = new String( "function" );
    void startPotentialFunction( char x ) {
        // got "function", just have to check next char
        String pstr = new String( potential );
        if ( funcstr.compareTo( pstr ) == 0 ) {
            if ( Character.isLetterOrDigit( x ) || ( x == '_' )) {
                startName( potentialOffset );
                return;
            } else {
                addToken( JS_FUNCTION, potentialOffset );
                potentialFunction = false;
            }
        }
        potential.append( x );
        if ( funcstr.indexOf( pstr ) != 0 ) {
            startName( potentialOffset );
        }
    }
}

class Token {
    int type;
    int offset;

    public Token( int type, int offset ) {
        this.type = type;
        this.offset = offset;
    }

    /** Does a character end a particular token, used for currentToken.
     *
     *  Only the following tokens can ever be assigned as currentToken:
     *
     *  JS_COMMENT -- all characters OK for rest of line
     *  JS_MULTILINECOMMENT -- characters OK up to C style comment end
     *  JS_NAME -- any non digit, non-number, non underscore
     *
     *  All other tokens we arbitrarily end immediately.
     */
     boolean possibleEnd = false;
    public boolean endToken( char x ) {
        if ( type == JavaScriptTokenizer.JS_COMMENT ) {
            return( false );
        } else if ( type == JavaScriptTokenizer.JS_MULTILINECOMMENT ) {
            if ( x == '*' ) {
                possibleEnd = true;
            } else if ( x == '/' ) {
                return( possibleEnd );
            } else {
                possibleEnd = false;
            }
            return( false );
        } else if ( type == JavaScriptTokenizer.JS_NAME ) {
            return( Character.isLetterOrDigit( x ) || ( x == '_' ));
        } else {
            return( true );
        }
    }
}
