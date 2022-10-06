package com.trapezium.util;

public class ByteString {
    byte[] dataSource;
    int start;
    int len;
    int rleCount;

    public ByteString() {
        dataSource = null;
        start = 0;
        len = 0;
        rleCount = 0;
    }

    public void setup( byte[] dataSource, int start ) {
        this.dataSource = dataSource;
        this.start = start;
        rleCount = 0;
        if (( dataSource[ start ] & 128 ) != 0 ) {
            rleCount = (int)(dataSource[ start ] & 127);
        }
        int scanner = start;
        while ( dataSource[ scanner ] != 0 ) {
            scanner++;
        }
        len = scanner - start;
        if ( rleCount > 0 ) {
            len += rleCount;
            len--;
        }
    }

    public int length() {
        return( len );
    }

    public char charAt( int offset ) {
        if ( offset < rleCount ) {
            return( ' ' );
        } else {
            if ( rleCount > 0 ) {
                offset -= rleCount;
                offset++;
            }
            return( (char)(dataSource[ start + offset ]) );
        }
    }
}
