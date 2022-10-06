/*
 * @(#)TokenTypes.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.parse;

/**
 *  Constants for types of tokens created by parsing.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 2 Dec 1997
 *
 *  @since           1.0
 *  @see             TokenFactory
 *  @see             TokenEnumerator
 */
public interface TokenTypes {
    static public final byte WhiteToken = 1;
    static public final byte LeftBracket = 2;
    static public final byte RightBracket = 3;
    static public final byte LeftBrace = 4;
    static public final byte RightBrace = 5;
    static public final byte NameToken = 6;
    static public final byte Keyword1Token = 7;
    static public final byte Keyword2Token = 8;
    static public final byte NumberToken = 9; // generic for all number variations
    static public final byte BadNumber = 19;
    static public final byte QuotedString = 20;
    static public final byte EmptyLine = 21;
    static public final byte QuotedStringContinuation = 22;
    static public final byte CommentToken = 23;
};

