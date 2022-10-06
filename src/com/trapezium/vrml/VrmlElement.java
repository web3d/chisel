/*
 * @(#)VrmlElement.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.pattern.Visitor;
import com.trapezium.pattern.VisitorPattern;
import com.trapezium.vrml.visitor.DumpVisitor;
import com.trapezium.vrml.visitor.ComplexityVisitor;
import com.trapezium.vrml.visitor.AdjustmentVisitor;
import com.trapezium.util.GlobalProgressIndicator;
import java.util.Vector;

/**
 *  A VrmlElement is the base class for any object in the VRML 2.0 object hierarchy.
 *  Specific subclasses get created as a ".wrl" file is parsed.  The grammar determines
 *  which type of element to create during parsing.
 *
 *  Each VrmlElement contains zero or more children, which are also VrmlElement.
 *  The object hierarchy for this is very simple.  The root level object is a Scene, with
 *  children that are Nodes or ROUTEs.  Node children are fields.  This object hierarchy
 *  parallels the VRML syntax.
 *
 *  The VrmlElement implements the visitor pattern, which allows the object structure
 *  to be traversed after it is created.  See LintVisitor for how parsing errors get
 *  displayed.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.21, field bug fix, self reference PROTO USE
 *                   1.12, 29 May 1998, base profile nonconformance category
 *                   1.12, 7 May 1998, make Serializable
 *                   1.12, 29 March 1998, added "adjust" method
 *  @version         1.1, 14 Jan 1998
 *
 *  @since           1.0
 */
abstract public class VrmlElement implements VisitorPattern, java.io.Serializable {
    /** Option, disable warnings entirely */
	static public boolean nowarning = false;
	/** Option, disable unused DEF warnings */
	static public boolean noUnusedDEFwarning = false;
	/** Option, only record nonconformance messages */
	static public boolean baseProfile = false;
	/** Option, disable nonconformance messages */
	static public boolean disableBaseProfile = false;
	/** Used to track number of VrmlElements created during parsing, not thread safe */
	static public int createCount = 0;

	/** any error creating a specific element is noted here, see setError, getError */
	public transient String errorString = null;

	/** parent, necessary for scoping searches */
	VrmlElement parent = null;

	/** children are seen only through the Visitor pattern */
	Object children = null;

	/** default constructor */
	public VrmlElement() {
	    createCount++;
	}

    /** Write out the object for serialization, mark progress if GlobalProgressIndicator
     *  is set up.
     */
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
        GlobalProgressIndicator.markProgress();
        oos.defaultWriteObject();
    }

    /** Read in serialized object, mark progress if GlobalProgressIndicator is set up.
     */
    private void readObject(java.io.ObjectInputStream oos) throws java.io.IOException {
        try {
            GlobalProgressIndicator.markProgress();
            oos.defaultReadObject();
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
            throw new java.io.IOException();
        }
    }


	/** get the class name without package */
	public String getBaseName() {
		String className = getClass().getName();
		int firstIndex = className.lastIndexOf( '.' ) + 1;
		int lastIndex = className.length();
		return( className.substring( firstIndex, lastIndex ));
	}

	/* Set first token */
	abstract public void setFirstTokenOffset( int tokenOffset );

	/** Get first token */
	abstract public int getFirstTokenOffset();

	/** Set last token */
	abstract public void setLastTokenOffset( int tokenOffset ) throws FunctionCallException;

	/** Get last token */
	abstract public int getLastTokenOffset();

	/** Adjust token values */
	abstract public void adjust( int boundary, int amount );

    public VrmlElement vrmlClone( VrmlElement protoInstance ) {
        return( null );
    }

	/** Add a child VrmlElement */
	public void addChild( Object child ) {

		if ( child == null ) {
			return;
		}
		// optimization, one child added directly, if more than one
		// create Vector
		if ( children == null ) {
			children = child;
		} else if ( children instanceof Vector ) {
		    ((Vector)children).addElement( child );
		} else {
		    Object original = children;
		    children = new Vector();
		    ((Vector)children).addElement( original );
		    ((Vector)children).addElement( child );
		}
		if ( child instanceof VrmlElement ) {
			VrmlElement pChild = (VrmlElement)child;
			pChild.setParent( this );
		}
	}

	/** Remove a child.
	 *
	 *  @param child child to remove
	 *  @throws VrmlElementNotFoundException if the child is not part
	 *     of this VrmlElement
	 */
	public void removeChild( Object child ) throws VrmlElementNotFoundException {
	    if ( !contains( child )) {
	        throw new VrmlElementNotFoundException();
	    }
        if ( children instanceof Vector ) {
			((Vector)children).removeElement( child );
    	} else {
    	    children = null;
    	}
	}

	/** Check if a specific child exists.
	 *
	 *  @param child child to check for
	 *  @return true if child found, otherwise false
	 */
	public boolean contains( Object child ) {
	    if ( children != null ) {
	        if ( children instanceof Vector ) {
	            return( ((Vector)children).contains( child ));
	        } else {
	            return( children == child );
	        }
	    } else {
	        return( false );
	    }
	}

    /** does nothing unless DEFUSENode */
    public void deregisterSelf() {
    }


    /** Remove a child, and update the text as well.
     *
     *  @param f the VrmlElement to remove
     *  @throws VrmlElementNotFoundException if the VrmlElement parameter does not have this
     *     VrmlElement as one of its ancestors.
     */
    public void removeVrmlElement( VrmlElement f ) throws VrmlElementNotFoundException {
        f.deregisterSelf();
        VrmlElement scanner = f.getParent();
        VrmlElement container = null;
        if ( contains( f )) {
            container = this;
        }
        while (( scanner != null ) && ( scanner != this )) {
            if ( scanner.contains( f )) {
                container = scanner;
            }
            scanner = scanner.getParent();
        }
        if (( scanner == null ) || ( container == null )) {
            throw new VrmlElementNotFoundException();
        }
        container.removeChild( f );
        int numberTokens = f.getLastTokenOffset() - f.getFirstTokenOffset() + 1;
        Scene s = (Scene)f.getScene();
        TokenEnumerator tokenEnumerator = s.getTokenEnumerator();
        tokenEnumerator.startLineWith( f.getFirstTokenOffset() );
        tokenEnumerator.startLineWith( f.getLastTokenOffset() + 1 );
        AdjustmentVisitor av = new AdjustmentVisitor( tokenEnumerator, f.getFirstTokenOffset() + numberTokens - 1, -numberTokens );
        s.traverse( av );
        int firstLineNumber = tokenEnumerator.getLineNumber( f.getFirstTokenOffset() );
        int numberLines = tokenEnumerator.getLineNumber( f.getLastTokenOffset() ) - firstLineNumber + 1;
        int nTokens = f.getLastTokenOffset() - f.getFirstTokenOffset() + 1;
        tokenEnumerator.removeTokens( f.getFirstTokenOffset(), nTokens );
        tokenEnumerator.removeLines( firstLineNumber, numberLines );
    }

	/** Get the specific child.
	 *
	 * @param offset the offset of the child, from 0 to (numberChlidren-1)
	 * @return the child VrmlElement, null if none or the offset value out
	 *     of range.
	 */
	public VrmlElement getChildAt( int offset ) {
		if ( children == null ) {
			return( null );
		} else if ( children instanceof Vector ) {
		    Vector vchildren = (Vector)children;
            if ( vchildren.size() <= offset ) {
    			return( null );
    		} else {
    			return( (VrmlElement)vchildren.elementAt( offset ));
    		}
		} else if ( offset == 0 ) {
		    return( (VrmlElement)children );
		} else {
		    return( null );
		}
	}

	/** Get the last child of this VrmlElement */
	public VrmlElement getLastChild() {
	    if ( children == null ) {
	        return( null );
	    } else if ( children instanceof Vector ) {
	        Vector vchildren = (Vector)children;
	        return( getChildAt( vchildren.size() - 1 ));
	    } else {
	        return( (VrmlElement)children );
	    }
	}

	/** Get the number of children */
	public int numberChildren() {
		if ( children == null ) {
			return( 0 );
		} else if ( children instanceof Vector ) {
		    Vector vchildren = (Vector)children;
			return( vchildren.size() );
		} else {
		    return( 1 );
		}
	}

	/** set a child's parent */
	public void setParent( VrmlElement p ) {
		parent = p;
	}

	/** get this element's parent */
	public VrmlElement getParent() {
		return( parent );
	}

	/** set the error string */
	public void setError( String s ) {
	    if ( s == null ) {
	        errorString = null;
	        return;
	    }
		if ( nowarning ) {
			if ( s.indexOf( "Warning" ) == 0 ) {
				return;
			}
		}
		if ( noUnusedDEFwarning ) {
		    if ( s.indexOf( "DEF is not used" ) > 0 ) {
		        return;
		    }
		}
		if ( baseProfile ) {
		    if ( s.indexOf( "Nonconformance" ) == -1 ) {
		        return;
		    }
		}
		if ( disableBaseProfile ) {
		    if ( s.indexOf( "Nonconformance" ) == 0 ) {
		        return;
		    }
		}
		if ( errorString == null ) {
			errorString = s;
		} else {
			if ( errorString.indexOf( s ) >= 0 ) {
				return;
			}
			StringBuffer b = null;
			if (( errorString.indexOf( "Warning" ) == 0 ) || ( errorString.indexOf( "Nonconformance" ) == 0 )) {
				b = new StringBuffer( s );
				b.append( ", " + errorString );
			} else {
				b = new StringBuffer( errorString );
				b.append( ", " + s );
			}
			errorString = new String( b );
		}
	}

	/** get the error string */
	public String getError() {
		return( errorString );
	}

    /** is this VrmlElement traversable */
	abstract public boolean isTraversable();

    /** template method, Field objects override this to traverse their
     *  field value.
     */
    public void fieldValueTraverse( Visitor v ) {
    }

	/** Visitor pattern, traverse structure with a particular visitor */
	public void twoPassTraverse( Visitor v ) {
		// the visitor controls whether children get visited or not
		if ( v.visit( this )) {
		    fieldValueTraverse( v );
			if ( children != null ) {
			    int nChildren = numberChildren();
				for ( int i = 0; i < nChildren; i++ ) {
					VrmlElement child = getChildAt( i );
					if ( v.acceptsPassOne( child )) {
					    child.twoPassTraverse( v );
					}
				}
				for ( int i = 0; i < nChildren; i++ ) {
					VrmlElement child = getChildAt( i );
					if ( v.acceptsPassTwo( child )) {
					    child.twoPassTraverse( v );
					}
				}
			}
		}
		// used to keep track of how many levels down we are visiting
		v.done();
	}
	
	/** Visitor pattern, traverse structure with a particular visitor */
	public void traverse( Visitor v ) {
		// the visitor controls whether children get visited or not
		if ( v.visit( this )) {
		    fieldValueTraverse( v );
			if ( children != null ) {
			    int nChildren = numberChildren();
				for ( int i = 0; i < nChildren; i++ ) {
					VrmlElement child = getChildAt( i );
    				if ( !( v instanceof ComplexityVisitor ) && !child.isTraversable()) {
					    v.visit( child );
					    v.done();
						continue;
					}
					if ( v.accepts( child )) {
						child.traverse( v );
					}
				}
			}
		}
		// used to keep track of how many levels down we are visiting
		v.done();
	}

    /** Get the root of the scene graph */
	public VrmlElement getRoot() {
		if ( parent == null ) {
			return( this );
		} else {
			return( parent.getRoot() );
		}
	}

	/** Get the root scene containing this element.
	 *  This is necessary in some cases because the Scene contains the
	 *  name space for DEFs and PROTOs.
	 *  This is done within VrmlElement, because we need this name space
	 *  for a particular element.
	 *  The "isScene" method is a simple template method, returns false
	 *  for all VrmlElement instances except for Scene.
	 *
	 *  @return  the Scene object containing this element, or null if
	 *           none found.
	 */
	public VrmlElement getScene() {
		VrmlElement scanner = this;
		while ( !( scanner.isScene() )) {
			scanner = scanner.parent;
			if ( scanner == null ) {
				break;
			} else if ( scanner == this ) {
			    // cycle
			    scanner = null;
			    break;
			}
		}

		if ( scanner == null ) {
			return( null );
		} else {
			return( scanner );
		}
	}

    /** Does this element or any of its children have any errors */
    public boolean containsErrors() {
        if ( errorString != null ) {
            if (( errorString.indexOf( "Warning" ) != 0 ) && ( errorString.indexOf( "Nonconformance" ) != 0 )) {
                return( true );
            }
        }
        int nChildren = numberChildren();
        for ( int i = 0; i < nChildren; i++ ) {
            VrmlElement v = getChildAt( i );
            if ( v.containsErrors() ) {
                return( true );
            }
        }
        return( false );
    }

    /** template method, Scene overrides this to return true */
	public boolean isScene() {
		return( false );
	}
	
	/** Get the TokenEnumerator for this element */
	public TokenEnumerator getTokenEnumerator() {
	    Scene s = (Scene)getScene();
	    if ( s != null ) {
	        return( s.getTokenEnumerator() );
	    } else {
	        return( null );
	    }
	}

    /** Add a warning Value child.  This exists for those cases where
     *  there is no VrmlElement for the tokenOffset associated with the
     *  warning.  NOTE:  this may be restricted (i.e. not called) by
     *  the ErrorSummary object contained in the Scene.  The ErrorSummary
     *  restriction prevents OutOfMemory conditions when there are too
     *  many warnings in a file.
     *
     *  @param tokenOffset token offset where warning is to be attached
     *  @param warning String message containing warning
     */
    public void addWarning( int tokenOffset, String warning ) {
        if ( !nowarning ) {
            Value v = new Value( tokenOffset );
            v.setError( warning );
            addChild( v );
        }
    }

    /** debugging dump method */
	public void dump( String header ) {
		dump( header, false );
	}

    /** another debugging dump method */
	public void dumpUserDefined( String header ) {
		System.out.println( "BEFORE: " + header );
		Scene s = (Scene)getScene();
		DumpVisitor dv = new DumpVisitor( System.out, s.getTokenEnumerator() );
		dv.dumpUserDefinedFields();
		traverse( dv );
		System.out.println( "AFTER: " + header );
	}

    /** and another debugging dump method */
	public void dump( String header, boolean tokensToo ) {
		System.out.println( "BEFORE: " + header );
		Scene s = (Scene)getScene();
		DumpVisitor dv = new DumpVisitor( System.out, s.getTokenEnumerator() );
		traverse( dv );
		System.out.println( "AFTER: " + header );
	}

}
