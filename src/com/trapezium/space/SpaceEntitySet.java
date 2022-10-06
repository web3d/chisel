package com.trapezium.space;

import com.trapezium.vrml.node.space.BoundingBox;
import java.util.Enumeration;

/** The SpaceEntitySet is a data structure for keeping a list of floats
 *  or ints.  The boundary is either fixed, every N entries, or
 *  -1 meaning that the "ilist" has -1 terminators.
 */
public class SpaceEntitySet implements SpaceCorrespondenceConstants {
    int correspondenceType;

    static public final int InitialSize = 100;
    static public final int Int = 1;
    static public final int Float = 2;
    int type;
    float[] flist;
    int[] ilist;
    SpaceEntitySet iiList;
    int unitSize;
    int listMax;
    int listSize;

    /** Class constructor */
    public SpaceEntitySet( int type, int boundary, int ctype ) {
        correspondenceType = ctype;
        unitSize = boundary;
        this.type = type;
        if ( type == Int ) {
            ilist = new int[InitialSize];
        } else {
            flist = new float[InitialSize];
        }
        if ( boundary == -1 ) {
            iiList = new SpaceEntitySet( Int, 1, StandAlone );
        }
           
        listMax = InitialSize;
        listSize = 0;
    }
    
    public SpaceEntitySet( int type, int boundary, int ctype, float[] farray, int listSize, int listMax ) {
        correspondenceType = ctype;
        unitSize = boundary;
        this.type = type;
        flist = new float[ farray.length ];
        System.arraycopy( farray, 0, flist, 0, farray.length );
        this.listSize = listSize;
        this.listMax = listMax;
    }
    
    /** Copy constructor */
    public SpaceEntitySet( SpaceEntitySet source ) {
        correspondenceType = source.getCorrespondenceType();
        unitSize = source.getUnitSize();
        listMax = source.getListMax();
        listSize = source.getListSize();
        type = source.getType();
        if ( type == Int ) {
            ilist = new int[ listMax ];
            System.arraycopy( source.getIntList(), 0, ilist, 0, listMax );
        } else {
            flist = new float[ listMax ];
            System.arraycopy( source.getFloatList(), 0, flist, 0, listMax );
        }
    }

    /** Get the amount of the value array in use, either ilist or flist size */
    public int getListSize() {
        return( listSize );
    }
    
    /** Get the size of the value array, either ilist or flist size */
    public int getListMax() {
        return( listMax );
    }
    
    /** Get the type */
    public int getType() {
        return( type );
    }
    
    /** Get the correspondence type */
    public int getCorrespondenceType() {
        return( correspondenceType );
    }
    
    /** Get the unit size */
    public int getUnitSize() {
        return( unitSize );
    }

    /** Get the number of entities in this set, does not work for SpaceEntitySets
     *  with the unitSize -1 (though maybe it should)
     */
    public int getNumberEntities() {
        if ( unitSize > 0 ) {
            return( listSize/unitSize );
        } else {
            return( iiList.getNumberEntities() );
        }
    }
    
    /** Get the int value array */
    public int[] getIntList() {
        return( ilist );
    }
    
    /** Get the float value array */
    public float[] getFloatList() {
        return( flist );
    }
    
    /** Add a value to the float array */
    public void add( float f1 ) {
        ensureCapacity( 1 );
        flist[ listSize++ ] = f1;
    }
    
    /** Add 3 floats to float array */
    public void add3f( float[] f ) {
        ensureCapacity( 3 );
        flist[listSize++] = f[0];
        flist[listSize++] = f[1];
        flist[listSize++] = f[2];
    }
    
    /** Add 2 floats to float array */
    public void add2f( float[] f ) {
        ensureCapacity( 2 );
        flist[listSize++] = f[0];
        flist[listSize++] = f[1];
    }

    /** Add a value to the int array */
    public void add( int val ) {
        ensureCapacity( 1 );
        ilist[ listSize++ ] = val;
        if ( unitSize == -1 ) {
            if ( listSize == 1 ) {
                iiList.add( 0 );
            } else if ( ilist[ listSize - 2 ] == -1 ) {
                iiList.add( listSize - 1 );
            }
        }
    }

    /** Make sure the int or float array can handle additional entries */
    void ensureCapacity( int len ) {
        while (( listSize + len ) >= listMax ) {
            int newSize = listMax;
            if ( listMax > 300000 ) {
                newSize += 300000;
            } else {
                newSize = newSize * 2;
            }
            if ( flist != null ) {
                float[] ftemp = new float[ newSize ];
                System.arraycopy( flist, 0, ftemp, 0, listMax );
                flist = ftemp;
                ftemp = null;
            }
            if ( ilist != null ) {
                int[] itemp = new int[ newSize ];
                System.arraycopy( ilist, 0, itemp, 0, listMax );
                ilist = itemp;
                itemp = null;
            }
            Runtime.getRuntime().gc();
            listMax = newSize;
        }
    }
    
    /** Create an enumeration for accessing ilist */
    public Enumeration getEnumeration() {
        return( new IlistEnumeration( ilist, listSize ));
    }
    
    /** Get the location of an element from an ilist */
    public void getLocation( int startLocation, SpaceEntitySet iLocations, float[] result ) {
        int counter = 0;
        iLocations.clearFloats( result );
        while (( startLocation < listSize ) && ( ilist[ startLocation ] != -1 )) {
            iLocations.sumFloats( ilist[startLocation++], result );
        }
    }
    
    /** Get the location of an element from the flist */
    public void getLocation( int floatOffset, float[] result ) {
        int actualOffset = floatOffset * unitSize;
        for ( int i = 0; i < unitSize; i++ ) {
            result[i] = flist[actualOffset++];
        }
    }
    
    /** Clear a floats result before summing */
    public void clearFloats( float[] result ) {
        for ( int i = 0; i < unitSize; i++ ) {
            result[i] = 0f;
        }
    }
    
    /** Sum floats at a specific offset */
    public void sumFloats( int floatOffset, float[] result ) {
        int actualOffset = floatOffset * unitSize;
        for ( int i = 0; i < unitSize; i++ ) {
            result[i] += flist[actualOffset++];
        }
    }
    
    /** Get the int offset of a particular value */
    public int getOffsetOfValue( int value ) {
        if ( ilist != null ) {
            for ( int i = 0; i < listSize; i++ ) {
                if ( ilist[i] == value ) {
                    return( i );
                }
            }
        }
        return( -1 );
    }
    
    /** Get the int value at a particular offset */
    public int getValueAt( int offset ) {
        if ( ilist != null ) {
            return( ilist[ offset ] );
        } else {
            return( -1 );
        }
    }
    
    /** Get the BoundingBox for this set */
    public BoundingBox getBoundingBox() {
        BoundingBox result = new BoundingBox();
        int numberEntities = getNumberEntities();
        float[] fresult = new float[ unitSize ];
        for ( int i = 0; i < numberEntities; i++ ) {
            getLocation( i, fresult );
            result.setXYZ( fresult );
        }
        return( result );
    }

    /** Adjust all the values in this set, assumes unitSize 3 */
    public void adjustValues( float xToZero, float xFactor, float xShift,
        float yToZero, float yFactor, float yShift,
        float zToZero, float zFactor, float zShift ) {
        int numberEntities = getNumberEntities();
        int offset = 0;
        for ( int i = 0; i < numberEntities; i++ ) {
            float value = flist[offset];
            value += xToZero;
            value *= xFactor;
            value += xShift;
            flist[offset] = value;
            offset++;
            value = flist[offset];
            value += yToZero;
            value *= yFactor;
            value += yShift;
            flist[offset] = value;
            offset++;
            value = flist[offset];
            value += zToZero;
            value *= zFactor;
            value += zShift;
            flist[offset] = value;
            offset++;
        }
    }
    
    /** Get a portion of the ilist */
    public int[] getIlist( int offset ) {
        int beginIdx = iiList.getValueAt( offset );
        offset++;
        int endIdx = beginIdx;
        if ( offset < iiList.getListSize() ) {
            endIdx = iiList.getValueAt( offset );
            endIdx--;
        } else {
            endIdx = iiList.getListSize();
            endIdx--;
            if ( iiList.getValueAt( endIdx ) != -1 ) {
                endIdx++;
            }
        }
        // endIdx now at last valid value in sequence
        endIdx--;
        if ( endIdx >= beginIdx ) {
            int[] result = new int[ endIdx - beginIdx + 1 ];
            for ( int i = beginIdx; i <= endIdx; i++ ) {
                result[i - beginIdx] = iiList.getValueAt( i );
            }
            return( result );
        } else {
            return( null );
        }
    }
    
    /** Get all the ilist offsets using a particular index value */
    public int[] getIlistOffsets( int value ) {
        OffsetListMaker olm = new OffsetListMaker( 10 );
        for ( int i = 0; i < listSize; i++ ) {
            if ( ilist[i] == value )  {
                olm.addOffset( iiList.getValueIncluding( i ));
            }
        }
        return( olm.getOffsetList() );
    }
    
    /** Get the value including a particular value, this assumes ilist is increasing,
     *  returns the offset of the last value that is less than or equal to the test
     *  value.
     */
    public int getValueIncluding( int testValue ) {
        int result = 0;
        for ( int i = 0; i < listSize; i++ ) {
            if ( ilist[i] <= testValue ) {
                result = i;
            } else {
                break;
            }
        }
        return( result );
    }
    
    /** Get the index value before and after a particular index.
     *
     *  @param faceOffset which ilist entry to check
     *  @param value the value to find in the face
     *  @param result where the "before" and "after" values are stored
     */
    public void getConnectingIndex( int faceOffset, int value, int[] result ) {
        result[0] = -1;
        result[1] = -1;
        int firstOffset = iiList.getValueAt( faceOffset );
        int lastOffset = listSize - 1;
        if ( faceOffset < ( iiList.getNumberEntities() - 1 )) {
            lastOffset = iiList.getValueAt( faceOffset + 1 ) - 1;
        }
        if ( ilist[ lastOffset ] == -1 ) {
            lastOffset--;
        }
        int valueOffset = -1;
        for ( int i = firstOffset; i <= lastOffset; i++ ) {
            if ( ilist[i] == value ) {
                valueOffset = i;
                break;
            }
        }
        if ( valueOffset != -1 ) {
            if ( valueOffset == firstOffset ) {
                result[0] = ilist[ lastOffset ];
                result[1] = ilist[ valueOffset + 1 ];
            } else if ( valueOffset == lastOffset ) {
                result[0] = ilist[ valueOffset - 1 ];
                result[1] = ilist[ firstOffset ];
            } else {
                result[0] = ilist[ valueOffset - 1 ];
                result[1] = ilist[ valueOffset + 1 ];
            }
        }
    }
}

/** The IlistEnumeration is used to enumerate "-1" terminated integer
 *  lists.  It has more elements as long as there is a sequence left
 *  that is terminated by the end of the list of a "-1" (whichever comes
 *  first)
 */
class IlistEnumeration implements Enumeration {
    int[] ilist;
    int listSize;
    int idx;
   
    /** Class constructor */
    IlistEnumeration( int[] ilist, int listSize ) {
        this.ilist = ilist;
        this.listSize = listSize;
        idx = 0;
        if ( ilist == null ) {
            listSize = 0;
        }
    }
    
    /** There are more sequences if the internal index hasn't reached the
     *  end of the integer list.
     */
    public boolean hasMoreElements() {
        return( idx < listSize );
    }
    
    /** Return an Integer indicating the next element in the list */
    public Object nextElement() {
        Integer result = new Integer( idx );
        idx++;
        while ( idx < listSize ) {
            if ( ilist[idx] != -1 ) {
                idx++;
            } else {
                idx++;
                break;
            }
        }
        return( result );
    }
}
