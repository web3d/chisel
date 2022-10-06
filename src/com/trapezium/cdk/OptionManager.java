package com.trapezium.cdk;

import java.lang.*;

public class OptionManager extends java.lang.Object
{
    OptionInfo[] optionInfo;
    
    public OptionManager() {
        optionInfo = new OptionInfo[8];
        for ( int i = 0; i < 8; i++ ) {
            optionInfo[i] = new OptionInfo();
        }
    }
    
    public int getNumberOptions() {
        int numberOptions = 0;
        for ( int i = 0; i < 8; i++ ) {
            if ( optionInfo[i].hasDescription() ) {
                numberOptions++;
            }
        }
        return( numberOptions );
    }
    
    public OptionInfo getValidOption( int offset ) {
        int currentOffset = 0;
        for ( int i = 0; i < 8; i++ ) {
            if ( optionInfo[i].hasDescription() ) {
                if ( offset == currentOffset ) {
                    return( optionInfo[i] );
                } else {
                    currentOffset++;
                }
            }
        }
        return( null );
    }
    
    public OptionInfo getOptionInfo( int offset ) {
        offset--;
        return( optionInfo[offset] );
    }   
}
