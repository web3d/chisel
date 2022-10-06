/*
 * @(#)Visitor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.pattern;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.util.StringUtil;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.util.GlobalProgressIndicator;
import java.util.Hashtable;

/**
 *  Abstract base class for all Visitor pattern objects.
 *  Concrete subclasses must define "visitObject" method, which returns true 
 *  if the visited object's children are to be visitied, otherwise false.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 4 March 1998, infinite loop bug with some PROTOs
 *  @version         1.0, 30 Dec 1997
 *
 *  @since           1.0
 */
 abstract public class Visitor implements java.io.Serializable {
	public int visitLevel;
	protected TokenEnumerator dataSource;
	Hashtable visited;

    /** Class constructor */
	public Visitor( TokenEnumerator dataSource ) {
	    this.dataSource = dataSource;
		visitLevel = 0;
		visited = new Hashtable();
	}
	
	/** Set the data source for this visitor */
	public void setDataSource( TokenEnumerator dataSource ) {
	    this.dataSource = dataSource;
	}
	
	/** Get the data source */
	public TokenEnumerator getDataSource() {
	    return( dataSource );
	}

	/** visit level indentation */
	public String spacer() {
		return( StringUtil.spacer( visitLevel ));
	}


	/**
	 *  Visit a particular object, subclass template method "visitObject" does actual visit,
	 *  base class only tracks visit level.
	 */
	public boolean visit( Object a ) {
		visitLevel++;
	    if ( a instanceof PROTOInstance ) {
    	    if ( visited.get( a ) != null ) {
    	        return( false );
    	    }
    	    visited.put( a, a );
    	}
	    notifyCallback( a );
	    if ( GlobalProgressIndicator.abortCurrentProcess ) {
	        return( false );
	    }
		return( visitObject( a ));
	}

    /** call back to report progress */
	void notifyCallback( Object a ) {
	    if ( a instanceof VrmlElement ) {
	        VrmlElement v = (VrmlElement)a;
    	    int tokenOffset = v.getFirstTokenOffset();
    	    if (( tokenOffset != -1 ) && ( dataSource != null )) {
    	        dataSource.notifyByToken( tokenOffset );
    	    }
    	}
	}
	
	
	/**
	 *  Done visiting at this level.
	 */
	public void done() {
		visitLevel--;
	}


	/**
	 *  Template method, indicates whether an object is acceptable to the visitor.
	 *  This is assuming that all traversals start at acceptable objects, but from
	 *  that point on, any object is acceptable only if the accepts method approves
	 *  it.  This is a default method, which approves all objects for the visitor.
	 *
	 *  If a Visitor subclass needs to restrict the objects passed to "visitObject",
	 *  this method is changed to only allow such objects.
	 */
	public boolean accepts( Object a ) {
		return( true );
	}
	
	public boolean acceptsPassOne( Object a ) {
	    return( true );
	}
	
	public boolean acceptsPassTwo( Object a ) {
	    return( true );
	}
	
	public boolean isTwoPassVisitor() {
	    return( false );
	}

	/**
	 *  Template method for actually visiting an object.  Returns true if object children
	 *  also get visited.
	 */
	abstract public boolean visitObject( Object a );
}


