package com.trapezium.vrml.fields;

class ExtremeValues {

    float minX;
	float maxX;
	float minY;
	float maxY;
	float minZ;
	float maxZ;
	int current;
	int currentPass;

	ExtremeValues() {
	    current = 0;
	    currentPass = 0;
	}

	boolean hasValues() {
	    return( currentPass > 0 );
	}

	void putFloat( float f ) {
	    if ( current == 0 ) {
	        if ( currentPass == 0 ) {
	            minX = maxX = f;
	        } else if ( f < minX ) {
	            minX = f;
	        } else if ( f > maxX ) {
	            maxX = f;
	        }
	        current = 1;
	    } else if ( current == 1 ) {
	        if ( currentPass == 0 ) {
	            minY = maxY = f;
	        } else if ( f < minY ) {
	            minY = f;
	        } else if ( f > maxY ) {
	            maxY = f;
	        }
	        current = 2;
	    } else if ( current == 2 ) {
	        if ( currentPass == 0 ) {
	            minZ = maxZ = f;
	        } else if ( f < minZ ) {
	            minZ = f;
	        } else if ( f > maxZ ) {
	            maxZ = f;
	        }
	        current = 0;
	        currentPass++;
	    }
	}
	
	float getXsize() {
	    return( maxX - minX );
	}
	
	float getYsize() {
	    return( maxY - minY );
	}
	
	float getZsize() {
	    return( maxZ - minZ );
	}
	
	float getXcenter() {
	    return(( minX + maxX )/2 );
	}
	
	float getYcenter() {
	    return( (minY + maxY)/2 );
	}
	
	float getZcenter() {
	    return( (minZ + maxZ)/2 );
	}
}
