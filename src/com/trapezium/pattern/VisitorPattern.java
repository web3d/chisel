/*
 * @(#)VisitorPattern.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.pattern;

/**
 *  Single method needed for any object implementing Visitor Pattern.
 *  <P>
 *  Visitor Pattern from Design Patterns by Gamma, Helm, Johnson, Vlissides.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 18 Jun 1997
 *
 *  @since           1.0
 */
public interface VisitorPattern {
	public void traverse( Visitor v );
}
