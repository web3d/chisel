package com.trapezium.util;

public class NameGenerator {
    int nameCounter;

    public NameGenerator() {
        nameCounter = -1;
    }

    public String generateName() {
        nameCounter++;
        return( generateName( nameCounter ));
    }

    String generateName( int counter ) {
        if ( counter < 26 ) {
            StringBuffer sb = new StringBuffer();
            char x = (char)('a' + counter);
            sb.append( x );
            return( new String( sb ));
        } else {
            int lastLetter = counter%36;
            int firstLetter = counter/36;
            StringBuffer sb = new StringBuffer();
            char x = (char)('a' + firstLetter);
            sb.append( x );
            if ( lastLetter < 26 ) {
                x = (char)('a' + lastLetter);
                sb.append( x );
            } else {
                x = (char)('0' + (lastLetter - 26));
                sb.append( x );
            }
            return( new String( sb ));
        }
    }
}
