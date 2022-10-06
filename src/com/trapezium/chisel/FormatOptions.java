package com.trapezium.chisel;

import com.trapezium.parse.TokenEnumerator;

public abstract class FormatOptions implements OptionHolder {

    public static FormatOptions newOption(String type ) throws ClassNotFoundException {
        if ("stripcomments".equalsIgnoreCase(type)) {
            return new StripCommentsOption();
        } else if ("indent".equalsIgnoreCase(type)) {
            return new IndentationOption();
        } else if ("maxlinelen".equalsIgnoreCase(type)) {
            return new MaxLineLenOption();
        } else if ("tab".equalsIgnoreCase(type)) {
            return new TabNOption();
        } else {
            throw new ClassNotFoundException();
        }
    }

    public FormatOptions() {
    }

    public abstract String getPropertyName();
    
    public abstract void runOption(boolean enabled);

    /** Set option value */
    public void setOptionConstraints( int optionOffset, Object constraints ) {
    }

    /** Convert an option value to an integer */
    public int optionValueToInt( Object value ) {
        int n;
        try {
            n = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            System.out.println("Couldn't convert option " + value + " to integer.");
            n = 0;
        }
        return n;
    }
    /** Convert an integer to an option value */
    public Object intToOptionValue( int value ) {
        return String.valueOf(value);
    }

}

class IndentationOption extends FormatOptions {
    int indentation = 3;

    /** Get the number of control options available for this chisel. */
    public int getNumberOptions() {
        return( 1 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        return( Integer.TYPE );
    }

    /** Get a specific control option label */
    public String getOptionLabel( int optionOffset ) {
        return( "indentation spaces" );
    }
    
    public String getPropertyName() {
        return( "format.indentation" );
    }

    /** Get current option value */
    public Object getOptionValue( int optionOffset ) {
        return( intToOptionValue( indentation ));
    }

    /** Set option value */
    public void setOptionValue( int optionOffset, Object value ) {
        indentation = optionValueToInt( value );
    }

    /** Really set the option value */
    public void runOption(boolean enabled) {
        if (enabled) {
            TokenPrinter.setIndentSize( indentation );
        } else {
            TokenPrinter.setIndentSize( 0 );
        }
    }

    /** Get current option value */
    public Object getOptionConstraints( int optionOffset ) {
        return( new IntegerConstraints(1, 16, 1 ));
    }
}

class MaxLineLenOption extends FormatOptions {
    int maxLineLen = 80;

    /** Get the number of control options available for this chisel. */
    public int getNumberOptions() {
        return( 1 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        return( Integer.TYPE );
    }

    /** Get a specific control option label */
    public String getOptionLabel( int optionOffset ) {
        return( "maximum line length" );
    }
    
    public String getPropertyName() {
        return( "format.maxLineLen" );
    }

    /** Get current option value */
    public Object getOptionValue( int optionOffset ) {
        return( intToOptionValue( maxLineLen ));
    }

    /** Set option value */
    public void setOptionValue( int optionOffset, Object value ) {
        maxLineLen = optionValueToInt( value );
    }

    /** Really set the option value */
    public void runOption(boolean enabled) {
        if (enabled) {
            TokenPrinter.setMaxLineLength( maxLineLen );
        } else {
            TokenPrinter.setMaxLineLength( Integer.MAX_VALUE );
        }
    }

    /** Get current option value */
    public Object getOptionConstraints( int optionOffset ) {
        return( new IntegerConstraints(40, 256, 8 ));
    }
}

class StripCommentsOption extends FormatOptions {
    /** Get the number of control options available for this chisel. */
    public int getNumberOptions() {
        return( 0 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        return( Integer.TYPE );
    }

    /** Get a specific control option label */
    public String getOptionLabel( int optionOffset ) {
        return( null );
    }

    /** Get current option value */
    public Object getOptionValue( int optionOffset ) {
        return( null );
    }

    /** Set option value */
    public void setOptionValue( int optionOffset, Object value ) {
    }
    
    public String getPropertyName() {
        return( "format.stripComments" );
    }


    /** Really set the option value */
    public void runOption(boolean enabled) {
        // this is handled in ChiselTableStack.java
    }

    /** Get current option value */
    public Object getOptionConstraints( int optionOffset ) {
        return( null );
    }
}

class TabNOption extends FormatOptions {
    int indentation = 3;

    /** Get the number of control options available for this chisel. */
    public int getNumberOptions() {
        return( 1 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        return( Integer.TYPE );
    }

    /** Get a specific control option label */
    public String getOptionLabel( int optionOffset ) {
        return( "spaces per tab" );
    }
    
    public String getPropertyName() {
        return( "format.spacesPerTab" );
    }

    /** Get current option value */
    public Object getOptionValue( int optionOffset ) {
        return( intToOptionValue( indentation ));
    }

    /** Set option value */
    public void setOptionValue( int optionOffset, Object value ) {
        indentation = optionValueToInt( value );
    }

    /** Really set the option value */
    public void runOption(boolean enabled) {
        if (enabled) {
            TokenEnumerator.setTabIndentSize( indentation );
        } else {
            TokenEnumerator.setTabIndentSize( 0 );
        }
    }

    /** Get current option value */
    public Object getOptionConstraints( int optionOffset ) {
        return( new IntegerConstraints(1, 16, 1 ));
    }
}
