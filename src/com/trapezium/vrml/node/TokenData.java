/*
 * @(#)TokenData.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.visitor.AdjustmentVisitor;
import com.trapezium.vrml.grammar.*;
import com.trapezium.vrml.fields.Field;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.parse.InputStreamFactory;
import com.trapezium.parse.TokenFactory;

import java.io.File;
import java.util.StringTokenizer;

/**
 *  Token information needed for moving VrmlElements between scene graphs.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998, created
 *
 *  @since           1.12
 */
public class TokenData {
    VrmlElement vrmlElement;
    Scene scene;
    TokenEnumerator dataSource;
    int firstToken;
    int lastToken;
    int insertionToken;
    boolean recreate;
    Node nodeParent;

    static public final int UseOriginal = 0;
    static public final int ReCreate = 1;
    static public final int ReCreateField = 2;

    /** Constructor for TokenData associated with a VrmlElement */
    public TokenData( VrmlElement vrmlElement ) {
        this( vrmlElement, UseOriginal );
    }

    public TokenData( VrmlElement vrmlElement, int createFlag ) {
        this( vrmlElement, createFlag, null );
    }

    public TokenData( VrmlElement vrmlElement, int createFlag, Node nodeParent ) {
        disableNodeVerify();
        init();
        this.nodeParent = nodeParent;
        if ( createFlag == UseOriginal ) {
            recreate = false;
        } else {
            recreate = true;
        }
        this.vrmlElement = vrmlElement;

        if ( vrmlElement != null ) {
            scene = (Scene)vrmlElement.getScene();
            dataSource = ((Scene)scene).getTokenEnumerator();
            firstToken = vrmlElement.getFirstTokenOffset();
            lastToken = vrmlElement.getLastTokenOffset();
            insertionToken = lastToken + 1;

            if ( recreate ) {
                int nBytes = getNumberBytes();
                int nLines = getNumberLines();
                int nTokens = getNumberTokens();
                TokenEnumerator newTokenEnumerator = new TokenEnumerator( nBytes + 10, nTokens + 10, nLines + 10);
                byte[] oldBytes = getFileData();
                int oldBytesOffset = getVrmlElementByteOffset();
                byte[] newBytes = newTokenEnumerator.getFileData();
                int newBytesOffset = 0;
                System.arraycopy( oldBytes, oldBytesOffset, newBytes, newBytesOffset, nBytes );
                // need the "+1" in case where original did not end on end of line,
                // copy needs to include line terminator
                newTokenEnumerator.setFileDataIdx( nBytes + 1 );
                int firstOldLine = getVrmlElementLineNumber();
                int firstOldLineIdx = getLineIdx( firstOldLine );

                // Set the new tokenEnumerator lineIdx array.  The first entry is always 0
                // the rest are the original one, minus the amount of missing previous
                // text which is oldBytesOffset
                newTokenEnumerator.setLineIdx( 0, 0 );
                for ( int i = 1; i < nLines; i++ ) {
                    int originalIdx = getLineIdx( firstOldLine + i );
                    newTokenEnumerator.setLineIdx( i, originalIdx - oldBytesOffset ); //firstOldLineIdx );
                }
                newTokenEnumerator.setNumberLines( nLines );
                dataSource = newTokenEnumerator;
                dataSource.retokenize();
                parseTokens( nodeParent );
            }
        }
        restoreNodeVerify();
    }


    /** Constructor for TokenData associated with a File.  The vrmlElement
     *  is set to be the first node found in the file.
     */
    public TokenData( File file ) {
        this( file, null );
    }

    public TokenData( File file, Node nodeParent ) {
        disableNodeVerify();
        init();

        // load file into TokenEnumerator
        try {
            dataSource = new TokenEnumerator( InputStreamFactory.getInputStream( file ), null, null, file );
            parseTokens( nodeParent );
        } catch ( Exception e ) {
        }
        restoreNodeVerify();
    }

    /** Constructor for TokenData associated with a String.  The vrmlElement
     *  is set to be the first node found in the string.
     */
    public TokenData( String s, DEFNameFactory dfn ) {
        this( s, null, dfn );
    }
    

    public TokenData( String s, Node nodeParent, DEFNameFactory dfn ) {
        disableNodeVerify();
        init();

        // use StringBuffer to load a TokenEnumerator
        int slen = s.length();
        dataSource = new TokenEnumerator( slen + 10, slen/2, slen/2 );
        TokenFactory tf = new TokenFactory();
        StringTokenizer st = new StringTokenizer( s, "\n" );
        while ( st.hasMoreTokens() ) {
            dataSource.addLine( st.nextToken(), tf );
        }

        // parse with VRML97parser
        parseTokens( nodeParent, dfn );
        restoreNodeVerify();
    }

    /** Initialize TokenData fields */
    void init() {
        vrmlElement = null;
        scene = null;
        dataSource = null;
        firstToken = -1;
        lastToken = -1;
        insertionToken = -1;
    }

    /** Parse the data in the token enumerator */
    void parseTokens() {
        parseTokens( null );
    }

    void parseTokens( Node parent ) {
        DEFNameFactory dfn = null;
        if ( parent != null ) {
            Scene s = (Scene)parent.getScene();
            if ( s != null ) {
                dfn = s.getDEFNameFactory();
            }
        }
        parseTokens( parent, dfn );
    }
    
    void parseTokens( Node parent, DEFNameFactory dfn ) {
        // parse file with VRML97parser
        VRML97parser parser = new VRML97parser( dfn );

        // if parent is null, we are building a Node, otherwise we are building a Field
        if ( parent == null ) {
            scene = new Scene( null, dataSource );

            // build scene, but don't ignore first token
            parser.Build( dataSource, scene, -1 );

            // use only first VrmlElement in the file as the VrmlElement
            vrmlElement = scene.getChildAt( 0 );
            if ( vrmlElement != null ) {
                firstToken = vrmlElement.getFirstTokenOffset();
                lastToken = vrmlElement.getLastTokenOffset();
                insertionToken = lastToken + 1;
            }
        } else {
            scene = (Scene)parent.getScene();
            NodeRule nr = new NodeRule( scene.getDEFNameFactory() );
            dataSource.setState( 0 );

	        // this adds the new node to the current scene node, but its tokens
	        // refer to a different token enumerator.
	        if (( parent instanceof PROTOInstance ) ||
                ( parent.getNodeName().compareTo( "Script" ) != 0 )) {
                NodeBodyRule nbr = new NodeBodyRule( nr );
                nbr.Build( 0, dataSource, scene, parent );
            } else {
                ScriptGutRule sgr = new ScriptGutRule( nr );
                Scene pscene = (Scene)parent.getScene();
                PROTO protoParent = null;
                if ( pscene != null ) {
                    protoParent = pscene.getPROTOparent();
                }
                sgr.Build( 0, dataSource, scene, parent, protoParent );
            }
            vrmlElement = parent.getLastField();
            if ( vrmlElement != null ) {
                firstToken = vrmlElement.getFirstTokenOffset();
                lastToken = vrmlElement.getLastTokenOffset();
                insertionToken = lastToken + 1;
                scene = null;
            }
        }
    }
    
    /** Disable node verification during parsing, because scene and TokenEnumerator
     *  states are not correct until TokenData has done insert operation.
     */
    boolean nodeVerifyState = false;
    void disableNodeVerify() {
        nodeVerifyState = NodeType.verifyDisabled;
        NodeType.verifyDisabled = true;
    }
    
    void restoreNodeVerify() {
        NodeType.verifyDisabled = nodeVerifyState;
    }

    /** Get the first token */
    public int getFirstToken() {
        return( firstToken );
    }

    /** Get the Node vrmlElement.
     *
     *  @return  the Node vrmlElement, or null if the vrmlElement is not a Node
     */
    public Node getNode() {
        if ( vrmlElement instanceof Node ) {
            return( (Node)vrmlElement );
        } else {
            return( null );
        }
    }

    /** Get the Field vrmlElement
     *
     *  @return the Field vrmlElement, or null if the vrmlElement is not a Field
     */
    public Field getField() {
        if ( vrmlElement instanceof Field ) {
            return( (Field)vrmlElement );
        } else {
            return( null );
        }
    }

    /** Get the VrmlElement */
    public VrmlElement getVrmlElement() {
        return( vrmlElement );
    }


    /** Get the number of tokens associated with the vrmlElement */
    public int getNumberTokens() {
        if ( vrmlElement == null ) {
            return( 0 );
        } else {
            return( lastToken - firstToken + 1 );
        }
    }

    /** Get the number of bytes associated with the vrmlElement */
    public int getNumberBytes() {
        if ( vrmlElement == null ) {
            return( 0 );
        } else {
            // the first offset is the offset of the first token, or the offset of
            // its line if is the first token on that line.
            int l1 = dataSource.getLineNumber( firstToken );
            int l2 = dataSource.getLineNumber( lastToken );
            int l1ByteOffset = dataSource.getLineIdx( l1 );
            if ( firstToken > 0 ) {
                if ( dataSource.getLineNumber( firstToken - 1 ) == l1 ) {
                    l1ByteOffset = dataSource.getByteOffset( firstToken );
                }
            }
            int l2ByteOffset = dataSource.getLineIdx( l2 + 1 );
            if (( l2 + 1 ) == dataSource.getNumberLines() ) {
                l2ByteOffset = dataSource.getFileDataIdx();
            }
            if ( lastToken < ( dataSource.getNumberTokens() - 1 )) {
                if ( dataSource.getLineNumber( lastToken + 1 ) == l2 ) {
                    l2ByteOffset = dataSource.getByteOffset( lastToken ) + dataSource.getSize( lastToken );
                }
            }
            return( l2ByteOffset - l1ByteOffset );
        }
    }

    /** Get the number of lines associated with the vrmlElement */
    public int getNumberLines() {
        if ( vrmlElement == null ) {
            return( 0 );
        } else {
            return( dataSource.getLineNumber( lastToken ) - dataSource.getLineNumber( firstToken ) + 1 );
        }
    }

    /** Get the insertion token, which is the first token after the vrmlElement (default),
     *  or someplace within the vrmlElement for Fields.
     */
    public int getInsertionToken() {
        return( insertionToken );
    }

    /** Set the insertion token to be first token after one of a particular type.
     *
     *  @param insertionToken where to start looking for the token of the type
     *  @param tokenType the type of token to look for, from com.trapezium.parse.TokenTypes
     */
    public void setInsertionToken( int insertionToken, int tokenType ) {
        while ( dataSource.getType( insertionToken ) != tokenType ) {
            insertionToken++;
        }
        insertionToken++;
        this.insertionToken = insertionToken;
        dataSource.startLineWith( insertionToken );
    }

    /** Get the byte offset in the TokenEnumerator byte array of the vrmlElement.
     *
     *  @return  the byteOffset into fileData of the vrmlElement, or the byte offset
     *    of the line containing the vrmlElement if that vrmlElement is the first
     *    vrmlElement on the line.
     */
    public int getVrmlElementByteOffset() {
        int byteOffset = dataSource.getByteOffset( firstToken );
        int line1 = dataSource.getLineNumber( firstToken );
        if ( firstToken == 0 ) {
            byteOffset = dataSource.getLineIdx( 0 );
        } else if ( firstToken > 0 ) {
            int line2 = dataSource.getLineNumber( firstToken - 1 );
            if ( line2 != line1 ) {
                byteOffset = dataSource.getLineIdx( line1 );
            }
        }
        return( byteOffset );
    }

    /** Get the line number of the vrmlElement */
    public int getVrmlElementLineNumber() {
        return( dataSource.getLineNumber( firstToken ));
    }

    /** Get the TokenEnumerator byte array of the vrmlElement */
    public byte[] getFileData() {
        return( dataSource.getFileData() );
    }
    
    /** Get the TokenEnumerator */
    public TokenEnumerator getTokenEnumerator() {
        return( dataSource );
    }

    /** Get the lineIdx value for a line */
    public int getLineIdx( int lineNo ) {
        return( dataSource.getLineIdx( lineNo ));
    }

    /** Insert token data from another location into the current token data.
     *  This does nothing to the scene graph.  It just updates the TokenEnumerator
     *  so that it will accept a Scene graph insertion without reparsing
     */
    public void insert( TokenData source ) {
        int numberBytes = source.getNumberBytes();
        int numberLines = source.getNumberLines();

        // increase the capacity of fileData and lineIdx arrays
        dataSource.ensureByteCapacity( numberBytes );
        dataSource.ensureLineCapacity( numberLines );

        // add the bytes to the dataSource at the insertion point
        byte[] newBytes = source.getFileData();
        int newBytesOffset = source.getVrmlElementByteOffset();
        byte[] originalBytes = dataSource.getFileData();
        int originalAdditionalSpace = dataSource.getFileDataIdx();
        
        // make space in the dataSource at the insertion line
        int insertionLine = dataSource.getLineNumber( insertionToken );
        int insertLineOffset = dataSource.getLineIdx( insertionLine );
        
        // update the bytes
        if ( insertionToken >= dataSource.getNumberTokens() ) {
            insertionLine = dataSource.getNumberLines();
            insertLineOffset = originalAdditionalSpace;
        } else {
            // shift all bytes to the right by "numberBytes"
            for ( int i = originalAdditionalSpace; i >= insertLineOffset; i-- ) {
                originalBytes[ i + numberBytes ] = originalBytes[i];
            }
        }
        // copy the new bytes to their new location
        System.arraycopy( newBytes, newBytesOffset, originalBytes, insertLineOffset, numberBytes );
        dataSource.setFileDataIdx( originalAdditionalSpace + numberBytes );

        // update the lines
        int lastLine = dataSource.getNumberLines() - 1;
        int[] originalLineIdx = dataSource.getLineIdx();
        int newLineNumber = source.getVrmlElementLineNumber();
        if ( insertionToken >= dataSource.getNumberTokens() ) {
            for ( int i = insertionLine; i < ( insertionLine + numberLines ); i++ ) {
                originalLineIdx[ i ] = source.getLineIdx( newLineNumber++ ) + insertLineOffset - newBytesOffset;
            }
        } else {
            for ( int i = lastLine; i >= insertionLine; i-- ) {
                originalLineIdx[ i + numberLines ] = originalLineIdx[i] + numberBytes;
            }
            for ( int i = insertionLine; i < ( insertionLine + numberLines ); i++ ) {
                originalLineIdx[ i ] = source.getLineIdx( newLineNumber++ ) + insertLineOffset - newBytesOffset;
            }
        }
        dataSource.setNumberLines( lastLine + numberLines + 1 );

        // retokenize the dataSource
        dataSource.retokenize();

        // At this point, the dataSource is OK, but the scene graph referring to it
        // is messed up in both this TokenData and in the source TokenData.  Since these
        // make up the scene graph in use, we have to adjust the source TokenData
        // token values so that they are first 0 based.  Then we adjust them by
        // insertionToken.
        source.adjust( 0, -1*source.getFirstToken() );
        source.adjust( 0, insertionToken );

        // the dataSource is going to have its scene graph changed by inserting
        // a bunch of tokens at insertionTokens.  Since this change of the scene graph
        // hasn't yet taken place, we can do the adjustment here.
        int numberTokens = source.getNumberTokens();
        adjust( insertionToken, numberTokens );
    }

    /** Adjust token values in the scene graph.  We know the location of
     *  the token insertion, and the number of tokens being inserted.  The
     *  adjustment is to just increment any token offset at or beyond this
     *  insertion boundary by the number of tokens being inserted.
     *
     *  @param  tokenBoundary  tokens with this value or greater are adjusted
     *  @param  amount  the adjustment is by this amount
     */
    void adjust( int tokenBoundary, int amount ) {
        AdjustmentVisitor av = new AdjustmentVisitor( dataSource, tokenBoundary, amount );
        if ( scene == null ) {
            VrmlElement parent = vrmlElement.getParent();
            parent.traverse( av );
        } else {
            scene.traverse( av );
        }
    }
}

