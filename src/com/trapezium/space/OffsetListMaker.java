package com.trapezium.space;

class OffsetListMaker {
    int[] offsets;
    int offsetIdx;
    int offsetMax;
    int lastIdx;

    OffsetListMaker() {
        this( 100 );
    }
    
    OffsetListMaker( int n ) {
        offsets = new int[n];
        offsetIdx = 0;
        offsetMax = n;
        lastIdx = -1;
    }

    void reset() {
        offsetIdx = 0;
    }

    int getCount() {
        return( offsetIdx );
    }

    int[] getArray() {
        return( offsets );
    }

    boolean isComplete() {
        return( lastIdx == -1 );
    }

    void addOffset( int offset ) {
        lastIdx = offset;
        if ( offsetIdx >= offsetMax ) {
            int[] temp = offsets;
            offsets = new int[ offsetMax*2 ];
            System.arraycopy( temp, 0, offsets, 0, offsetMax );
            offsetMax = offsetMax*2;
        }
        offsets[ offsetIdx++ ] = offset;
    }
    
    int[] getOffsetList() {
        int[] result = new int[ offsetIdx ];
        System.arraycopy( offsets, 0, result, 0, offsetIdx );
        return( result );
    }
}
