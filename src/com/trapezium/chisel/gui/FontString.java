package com.trapezium.chisel.gui;

public class FontString {
	String s;
	int fontStyle;

	public FontString( String s, int fontStyle ) {
		this.s = s;
		this.fontStyle = fontStyle;
	}

	public String getString() {
		return( s );
	}

	public int getStyle() {
		return( fontStyle );
	}
}
