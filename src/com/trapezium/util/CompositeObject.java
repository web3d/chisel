package com.trapezium.util;

public class CompositeObject implements TypedObject {
    Object a;
    Object b;

    public CompositeObject( Object a, Object b ) {
        this.a = a;
        this.b = b;
    }

    public Object getObjectA() {
        return( a );
    }

    public Object getObjectB() {
        return( b );
    }

    /** TypedObject interface */
    public int getType() {
        return( -1 );
    }
    
    /** Find an object of a particular type in this CompositeObject.
     *  This is part of the TypedObject interface.
     *
     *  @param type type of object to search for
     *  @return first object of indicated type found, or null if not found
     */
    public Object getObject( int type ) {
        if ( a instanceof CompositeObject ) {
            CompositeObject ac = (CompositeObject)a;
            Object result = ac.getObject( type );
            if ( result != null ) {
                return( result );
            }
        } else if ( a instanceof TypedObject ) {
            if ( ((TypedObject)a).getType() == type ) {
                return( a );
            }
        }
        if ( b instanceof CompositeObject ) {
            CompositeObject bc = (CompositeObject)b;
            Object result = bc.getObject( type );
            if ( result != null ) {
                return( result );
            }
        } else if ( b instanceof TypedObject ) {
            if ( ((TypedObject)b).getType() == type ) {
                return( b );
            }
        }
        return( null );
    }
}
