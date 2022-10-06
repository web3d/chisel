/*
 * @(#)KeyValueRemover.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.Scene;
import com.trapezium.chisel.*;

import java.util.BitSet;
import java.util.Hashtable;

/**
 *  The KeyValueRemover removes keys and corresponding keyValues if the
 *  following conditions are encountered:
 *
 *  1.  a key of the form:
 *
 *          ... a a a ....
 *                ^ this middle value (one or more) and corresponding
 *                  keyValue entries are removed.
 *
 *  2.  a keyValue of the form
 *
 *           ... a a a ....
 *                 ^ this middle keyValue (one or more) and corresponding
 *                   key entries are removed.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         b73, 1 July 1998
 */

public class KeyValueRemover extends InterpolatorMinimizer {
    protected Hashtable valueKillTable;
    protected BitSet valueKillBits;
    
    /** Class constructor, super class handles only keys */
    public KeyValueRemover() {
        super( "Removing unnecessary keyValues" );
        valueKillTable = new Hashtable();
    }
    
    public void attemptOptimization( Node n ) {
        Field key = n.getField( "key" );
        Field keyValue = n.getField( "keyValue" );
        // If either of these fields are in a PROTO and are defined with IS
        // do nothing
        VrmlElement nodeParent = n.getParent();
        if ( nodeParent instanceof Scene ) {
            VrmlElement grandParent = nodeParent.getParent();
            if ( grandParent instanceof PROTO ) {
                if ( key != null ) {
                    int firstKeyToken = key.getFirstTokenOffset();
                    if ( firstKeyToken != -1 ) {
                        int nextKeyToken = dataSource.getNextToken( firstKeyToken );
                        if ( nextKeyToken != -1 ) {
                            if ( dataSource.sameAs( nextKeyToken, "IS" )) {
                                return;
                            }
                        }
                    }
                }
                if ( keyValue != null ) {
                    int firstKeyToken = keyValue.getFirstTokenOffset();
                    if ( firstKeyToken != -1 ) {
                        int nextKeyToken = dataSource.getNextToken( firstKeyToken );
                        if ( nextKeyToken != -1 ) {
                            if ( dataSource.sameAs( nextKeyToken, "IS" )) {
                                return;
                            }
                        }
                    }
                }
            }
        }

        if ( key != null ) {
            FieldValue keysfv = keyValue.getFieldValue();
            MFFieldValue mfKeyValue = null;
            if ( keysfv instanceof MFFieldValue ) {
                mfKeyValue = (MFFieldValue)keysfv;
            }
            FieldValue keyFv = key.getFieldValue();
            MFFieldValue mfKey = null;
            if ( keyFv instanceof MFFieldValue ) {
                mfKey = (MFFieldValue)keyFv;
            }
            if (( mfKeyValue != null ) && ( mfKeyValue.getRawValueCount() > 0 )) {
                int factor = getInterpolatorFactor( n );
                // here have to adjust factor for multiple-keyValue interpolators
                int checkCount = mfKeyValue.getRawValueCount()/factor;
                if ( checkCount > 0 ) {
                    if ( createBitSet( mfKey, mfKeyValue, checkCount, factor )) {
                        if ( replaceable( key )) {
                            if ( keyFv.getFirstTokenOffset() != -1 ) {
                                replaceRange( keyFv.getFirstTokenOffset(), keyFv.getLastTokenOffset(), new KeyInfo( n, Key, keyKillBits, valueKillBits, checkCount ));
                            }
                        }
                        if ( replaceable( keyValue )) {
                            FieldValue fv = keyValue.getFieldValue();
                            if ( fv.getFirstTokenOffset() != -1 ) {
                                replaceRange( fv.getFirstTokenOffset(), fv.getLastTokenOffset(), new KeyInfo( n, KeyValue, keyKillBits, valueKillBits, checkCount ));
                            }
                        }
                    }
                }
            }
        }
    }

    /** Create if necessary the BitSets associated with a key field in 
     *  an interpolator.  The values "keyKillBits" and "valueKillBits"
     *  are initialized in either case.
     *
     *  @param mfKey the interpolator key field
     *  @param mfKeyValue the interpolator keyValue field
     *  @param keyCount the number of key values
     *  @param factor how many numeric values associated with each keyValue entry
     *
     *  @return true if there is something to remove, otherwise false
     */
    boolean createBitSet( MFFieldValue mfKey, MFFieldValue mfKeyValue, int keyCount, int factor ) {
        if ( keyKillTable.get( mfKey ) != null ) {
            keyKillBits = (BitSet)keyKillTable.get( mfKey );
            valueKillBits = (BitSet)valueKillTable.get( mfKey );
            // all value instances must agree on the removal
            removeDups( valueKillBits, keyCount, mfKeyValue, factor, true );
            return( true );
        } else {
            keyKillBits = new BitSet( keyCount );
            valueKillBits = new BitSet( keyCount );
            boolean result = removeDups( keyKillBits, keyCount, mfKey, 1, false );
            result = removeDups( valueKillBits, keyCount, mfKeyValue, factor, false ) || result;
            keyKillTable.put( mfKey, keyKillBits );
            valueKillTable.put( mfKey, valueKillBits );
            return( result );
        }
    }

    /** Mark for removal middle entries in a sequence of identical entries.
     *
     *  @param killBits the BitSet used to mark entries for removal
     *  @param numberEntries number of entries to check
     *  @param vals the entry values
     *  @param factor number of numeric values for each entry
     *  @param checkPreviousValues  for keyValue removal, all PROTO instance
     *     keyValues must agree on the removal.  If this parameter is true,
     *     previous values of the killBits are checked and possibly changed.
     *
     *  @return true if there is anything marked, otherwise false
     */
    boolean removeDups( BitSet killBits, int numberEntries, MFFieldValue vals, int factor, boolean checkPreviousValues ) {
        int ckcount = 0;
        float[] currentVal = new float[ factor ];
        float[] firstVal = new float[ factor ];
        int numberConsecutiveValues = 0;
        dataSource.setState( vals.getFirstTokenOffset() );
        boolean result = false;
        for ( int i = 0; i < numberEntries; i++ ) {
            for ( int j = 0; j < factor; j++ ) {
                int scanner = dataSource.skipNonNumbers();
                float f = dataSource.getFloat( scanner );
                if ( i == 0 ) {
                    firstVal[j] = f;
                } else {
                    currentVal[ j ] = f;
                }
                scanner = dataSource.getNextToken();
            }
            if ( i > 0 ) {
                boolean match = true;
                for ( int j = 0; j < factor; j++ ) {
                    if ( firstVal[j] != currentVal[j] ) {
                        match = false;
                        break;
                    }
                }
                if ( match ) {
                    if ( numberConsecutiveValues == 0 ) {
                        numberConsecutiveValues = 2;
                    } else {
                        numberConsecutiveValues++;
                    }
                } else {
                    numberConsecutiveValues = 0;
                    for ( int j = 0; j < factor; j++ ) {
                        firstVal[j] = currentVal[j];
                    }
                }
            }
            if (( i > 0 ) && ( numberConsecutiveValues > 2 )) {
                if ( !checkPreviousValues ) {
                    killBits.set( i - 1 );
                }
                result = true;
            } else if ( checkPreviousValues ) {
                if ( i > 0 ) {
                    killBits.clear( i - 1 );
                }
            }
        }
        return( result );
    }
}
