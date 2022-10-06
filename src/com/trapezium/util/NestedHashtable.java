package com.trapezium.util;

import java.util.Hashtable;

public class NestedHashtable {
    Hashtable root;

    public NestedHashtable() {
        root = new Hashtable();
    }

    public Object get( float[] values ) {
        if ( values.length == 3 ) {
            return( get( root, values[0], values[1], values[2] ));
        } else if ( values.length == 2 ) {
            return( get( root, values[0], values[1] ));
        } else {
            return( null );
        }
    }

    public void put( float[] values, Object indicator ) {
        if ( values.length == 3 ) {
            put( root, values[0], values[1], values[2], indicator );
        } else if ( values.length == 2 ) {
            put( root, values[0], values[1], indicator );
        }
    }

    void put( Hashtable table, float v0, float v1, float v2, Object indicator ) {
        Float vf0 = new Float( v0 );
        Hashtable ht = (Hashtable)table.get( vf0 );
        if ( ht == null ) {
            ht = new Hashtable();
            table.put( vf0, ht );
        }
        put( ht, v1, v2, indicator );
    }

    void put( Hashtable table, float v0, float v1, Object indicator ) {
        Float vf0 = new Float( v0 );
        Hashtable ht = (Hashtable)table.get( vf0 );
        if ( ht == null ) {
            ht = new Hashtable();
            table.put( vf0, ht );
        }
        put( ht, v1, indicator );
    }

    void put( Hashtable table, float v0, Object indicator ) {
        Float vf0 = new Float( v0 );
        Hashtable ht = (Hashtable)table.get( vf0 );
        if ( ht == null ) {
            ht = new Hashtable();
            table.put( vf0, ht );
        }
        ht.put( vf0, indicator );
    }

    Object get( Hashtable table, float v0, float v1, float v2 ) {
        Float vf0 = new Float( v0 );
        Hashtable ht = (Hashtable)table.get( vf0 );
        if ( ht == null ) {
            return( null );
        } else {
            return( get( ht, v1, v2 ));
        }
    }

    Object get( Hashtable table, float v0, float v1 ) {
        Float vf0 = new Float( v0 );
        Hashtable ht = (Hashtable)table.get( vf0 );
        if ( ht == null ) {
            return( null );
        } else {
            return( get( ht, v1 ));
        }
    }

    Object get( Hashtable table, float v0 ) {
        Float vf0 = new Float( v0 );
        Hashtable ht = (Hashtable)table.get( vf0 );
        if ( ht == null ) {
            return( null );
        } else {
            return( ht.get( vf0 ));
        }
    }
}
