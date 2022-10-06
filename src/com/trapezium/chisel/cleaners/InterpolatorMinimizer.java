/*
 * @(#)InterpolatorMinimizer.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.chisel.*;

import java.util.BitSet;
import java.util.Hashtable;

public class InterpolatorMinimizer extends Optimizer {
    static public final int Key = 1;
    static public final int KeyValue = 2;

    class KeyInfo {
        /** The interpolator being optimized */
        Node n;

        /** Key or KeyValue indication */
        int type;

        /** indicates what key or keyValue entries being removed */
        BitSet keyEntryKillBits;

        /** if not null, used to resolve conflicting keyValue removal
         *  indications from different PROTO instances.  All PROTO
         *  instances must agree to the removal before it takes place.
         *  This BitSet gets "or"ed with keyKillBits during the optimize.
         */
        BitSet valueEntryKillBits;

        /** The number of entries */
        int count;

        /** Internal class constructor.
         *
         *  @param n node being optimized
         *  @param type type of optimization, key or keyValue
         *  @param keyEntryKillBits indicate which key entries get removed
         *  @param valueEntryKillBits indicate which keyValue entries get removed,
         *     possibly null
         *  @param count number of key or keyValue entries
         */
        KeyInfo( Node n, int type, BitSet keyEntryKillBits, BitSet valueEntryKillBits, int count ) {
            this.n = n;
            this.type = type;
            this.keyEntryKillBits = keyEntryKillBits;
            this.valueEntryKillBits = valueEntryKillBits;
            this.count = count;
        }

        void optimize( TokenPrinter tp, int startTokenOffset, int endTokenOffset ) {
            // keyKillBits can get additional values from valueKillBits
            if ( valueEntryKillBits != null ) {
                keyEntryKillBits.or( valueEntryKillBits );
            }
            int factor = getFactor();
            int scanner = startTokenOffset;
            for ( int i = 0; i < count; i++ ) {
                for ( int j = 0; j < factor; j++ ) {
                    scanner = tp.printNonNumbers( scanner, endTokenOffset );
                    if ( scanner > endTokenOffset ) {
                        break;
                    }
                    if ( !keyEntryKillBits.get( i )) {
                        tp.print( dataSource, scanner, TokenTypes.NumberToken );
                    }
                    scanner = dataSource.getNextToken( scanner );
                    if ( scanner > endTokenOffset ) {
                        break;
                    }
                }
                if ( scanner > endTokenOffset ) {
                    break;
                }
            }
            while ( scanner <= endTokenOffset ) {
                tp.print( dataSource, scanner );
                scanner = dataSource.getNextToken();
            }
        }

        int getFactor() {
            if ( type == KeyValue ) {
                return( getInterpolatorFactor( n ));
            }
            return( 1 );
        }
    }


    /** Class constructor for main Chisel */
    public InterpolatorMinimizer() {
        super( "Interpolator", "Creating unique interpolator keys..." );
    }

    /** Constructor used by subclasses */
    public InterpolatorMinimizer( String title ) {
        super( "Interpolator", title );
    }

    /** Get the number of numeric values for each keyValue entry.
     *  Note this may vary in the case of CoordinateInterpolator and
     *  NormalInterpolator since these can have more than one coordinate
     *  or normal per key.
     */
    public int getInterpolatorFactor( Node n ) {
        String nodeName = n.getNodeName();
        if ( nodeName.compareTo( "PositionInterpolator" ) == 0 ) {
            return( 3 );
        } else if ( nodeName.compareTo( "OrientationInterpolator" ) == 0 ) {
            return( 4 );
        } else if ( nodeName.compareTo( "ColorInterpolator" ) == 0 ) {
            return( 3 );
        } else if ( nodeName.compareTo( "CoordinateInterpolator" ) == 0 ) {
            return( getActualFactor( n ));
        } else if ( nodeName.compareTo( "NormalInterpolator" ) == 0 ) {
            return( getActualFactor( n ));
        } else {
            return( 1 );
        }
    }

    int getActualFactor( Node n ) {
        Field key = n.getField( "key" );
        Field keyValue = n.getField( "keyValue" );
        if (( key != null ) && ( keyValue != null )) {
            FieldValue keyFv = key.getFieldValue();
            FieldValue keyValueFv = keyValue.getFieldValue();
            if (( keyFv instanceof MFFieldValue ) && ( keyValueFv instanceof MFFieldValue )) {
                MFFieldValue keyList = (MFFieldValue)keyFv;
                MFFieldValue keyValueList = (MFFieldValue)keyValueFv;
                int keyListCount = keyList.getRawValueCount();
                if ( keyListCount > 0 ) {
                    int possibleFactor = keyValueList.getRawValueCount()/keyListCount;
                    if (( possibleFactor % 3 ) == 0 ) {
                        if ( possibleFactor > 0 ) {
                            return( possibleFactor );
                        }
                    }
                }
            }
        }
        return( 3 );
    }

    protected boolean doit;
    public void attemptOptimization( Node n ) {
        Field key = n.getField( "key" );
        Field keyValue = n.getField( "keyValue" );
        if ( key != null ) {
            FieldValue keysfv = key.getFieldValue();
            MFFieldValue keyv = null;
            if ( keysfv instanceof MFFieldValue ) {
                keyv = (MFFieldValue)keysfv;
            }
            if (( keyv != null ) && ( keyv.getRawValueCount() > 0 )) {
                doit = false;
                int checkCount = keyv.getRawValueCount();
                createBitSet( keyv );
                if ( doit ) {
                    if ( replaceable( key )) {
                        if ( key.getFirstTokenOffset() != -1 ) {
                            replaceRange( key.getFirstTokenOffset(), key.getLastTokenOffset(), new KeyInfo( n, Key, keyKillBits, null, checkCount ));
                        }
                    }
                    if ( replaceable( keyValue )) {
                        FieldValue fv = keyValue.getFieldValue();
                        if ( fv.getFirstTokenOffset() != -1 ) {
                            replaceRange( fv.getFirstTokenOffset(), fv.getLastTokenOffset(), new KeyInfo( n, KeyValue, keyKillBits, null, checkCount ));
                        }
                    }
                }
            }
        }
    }

    protected Hashtable keyKillTable = new Hashtable();
    protected BitSet keyKillBits;
    void createBitSet( MFFieldValue keyv ) {
        if ( keyKillTable.get( keyv ) != null ) {
            keyKillBits = (BitSet)keyKillTable.get( keyv );
            doit = true;
        } else {
            keyKillBits = new BitSet( keyv.getRawValueCount() );
            int ckcount = 0;
            float lastVal = 0f;
            int checkCount = keyv.getRawValueCount();
            dataSource.setState( keyv.getFirstTokenOffset() );
            for ( int i = 0; i < checkCount; i++ ) {
                int scanner = dataSource.skipNonNumbers();
                float f = dataSource.getFloat( scanner );
                scanner = dataSource.getNextToken();
                if (( i > 0 ) && ( f == lastVal )) {
                    keyKillBits.set( i );
                    doit = true;
                }
                lastVal = f;
            }
            keyKillTable.put( keyv, keyKillBits );
        }
    }

    boolean replaceable( Field ck ) {
        if ( ck != null ) {
            return( !ck.isISfield() );
        }
        return( true );
    }

	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		if ( param instanceof KeyInfo ) {
			((KeyInfo)param).optimize( tp, startTokenOffset, endTokenOffset );
		}
	}
}

