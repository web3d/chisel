package com.trapezium.chisel;

public interface OptionHolder {

    /** Get the number of control options available for this chisel. */
    int getNumberOptions();

    /** Get the class for an option */
    Class getOptionClass( int optionOffset );

    /** Get a specific control option label */
    String getOptionLabel( int optionOffset );

    /** Get current option value */
    Object getOptionValue( int optionOffset );

    /** Set option value */
    void setOptionValue( int optionOffset, Object value );

    /** Get the option constraints */
    Object getOptionConstraints( int optionOffset );

    /** Set the option constraints */
    void setOptionConstraints( int optionOffset, Object constraints );
}

