/*
 * @(#)IntegerConstraints.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

/**
 *  Constraints for an integer option value.
 *
 *  Used by objects implementing the OptionHolder interface to deal with
 *  options that take integer values with minimum and maximum values.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.0, 18 Mar 1998
 *
 *  @since           1.0
 */
public final class IntegerConstraints {
    int min;
    int max;
    int incr;

	public IntegerConstraints( int min, int max, int incr ) {
	    this.min = min;
	    this.max = max;
	    this.incr = incr;
	}

	public int getMinimum() {
	    return min;
	}

	public int getMaximum() {
	    return max;
	}

	public int getIncrement() {
	    return incr;
	}

	public int legalize(int n) {
	    return (n < min ? min : (n > max ? max : n));
	}

	public int increment(int n) {
	    return legalize(n + incr);
	}

	public int decrement(int n) {
	    return legalize(n - incr);
	}
}
