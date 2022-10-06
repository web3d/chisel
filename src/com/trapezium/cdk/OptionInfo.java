package com.trapezium.cdk;

import java.lang.*;
public class OptionInfo extends java.lang.Object implements java.io.Serializable
{
    static final int OptionUndefined = 0;
    static final int OptionBoolTrue = 1;
    static final int OptionBoolFalse = 2;
    static final int OptionNumeric = 3;
    int type;
    String description;
    int defaultVal;
    int minVal;
    int maxVal;
    int incVal;
    
    public OptionInfo() {
        type = OptionUndefined;
        description = null;
        defaultVal = 0;
        minVal = 0;
        maxVal = 0;
        incVal = 0;
    }
    
    public boolean getBoolVal() {
        return( type == OptionBoolTrue );
    }

    public int getMinVal() {
        return( minVal );
    }
    
    public int getMaxVal() {
        return( maxVal );
    }
    
    public int getIncVal() {
        return( incVal );
    }
    
    public int getDefaultVal() {
        return( defaultVal );
    }
    
    public int getType() {
        return( type );
    }
    
    public void setBool( boolean val ) {
        if ( val ) {
            type = OptionBoolTrue;
        } else {
            type = OptionBoolFalse;
        }
    }
    
    public boolean isBool() {
        return(( type == OptionBoolTrue ) || ( type == OptionBoolFalse ));
    }
    
    public void setNumeric() {
        type = OptionNumeric;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
    public String getDescription() {
        return( description );
    }
    
    public boolean hasDescription() {
        if ( description != null ) {
            return( description.length() > 0 );
        } else {
            return( false );
        }
    }
    
    public void setMinVal( String s ) {
        minVal = getVal( s );
    }
    
    public void setMaxVal( String s ) {
        maxVal = getVal( s );
    }
    
    public void setIncVal( String s ) {
        incVal = getVal( s );
    }
    
    public void setDefaultVal( String s ) {
        defaultVal = getVal( s );
    }
    
    int getVal( String s ) {
        try {
            int x = Integer.parseInt( s );
            return( x );
        } catch( Exception e ) {
            return( 0 );
        }
    }
}
