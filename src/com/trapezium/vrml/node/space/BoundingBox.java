package com.trapezium.vrml.node.space;


public class BoundingBox {
    float minX;
    float minY;
    float minZ;
    float maxX;
    float maxY;
    float maxZ;
    boolean initialized;

    /** Constructor for BoundingBox initialized using <B>setXYZ</B> method */
    public BoundingBox() {
        initialized = false;
    }

    /** Constructor for BoundingBox with known bounds */
    public BoundingBox( float xmin, float xmax, float ymin, float ymax, float zmin, float zmax ) {
        initialized = true;
        minX = xmin;
        maxX = xmax;
        minY = ymin;
        maxY = ymax;
        minZ = zmin;
        maxZ = zmax;
    }
    
    /** Increase the size of the bounding box by a given percent */
    public void increase( int percent ) {
        if ( percent > 0 ) {
            float range = maxX - minX;
            float inc = range * percent / 200;
            minX -= inc;
            maxX += inc;
            range = maxY - minY;
            inc = range * percent / 200;
            minY -= inc;
            maxY += inc;
            range = maxZ - minZ;
            inc = range * percent / 200;
            minZ -= inc;
            maxZ += inc;
        }
    }
            
    /** Get the X dimension of the bounding box */
    public float getXdimension() {
//        System.out.println( "maxX " + maxX + ", " + minX + " minX" );
        return( maxX - minX );
    }
    
    /** Get the Y dimension of the bounding box */
    public float getYdimension() {
        return( maxY - minY );
    }
    
    /** Get the Z dimension of the bounding box */
    public float getZdimension() {
        return( maxZ - minZ );
    }
    
    /** Get the X value closest to the one input */
    public float getClosestX( float x ) {
        if (( x - minX ) < (( maxX - minX )/2 )) {
            return( minX );
        } else {
            return( maxX );
        }
    }
    
    /** Get the Y value closest to the one input */
    public float getClosestY( float y ) {
        if (( y - minY ) < (( maxY - minY )/2 )) {
            return( minY );
        } else {
            return( maxY );
        }
    }
    
    /** Get the Z value closest to the one input */
    public float getClosestZ( float z ) {
        if (( z - minZ ) < (( maxZ - minZ )/2 )) {
            return( minZ );
        } else {
            return( maxZ );
        }
    }
    
    
    public float getMinX() {
        return( minX );
    }

    public float getMaxX() {
        return( maxX );
    }

    public float getMinY() {
        return( minY );
    }

    public float getMaxY() {
        return( maxY );
    }

    public float getMinZ() {
        return( minZ );
    }

    public float getMaxZ() {
        return( maxZ );
    }

    public void setXYZ( float[] v ) {
        setXYZ( v[0], v[1], v[2] );
    }
    
    public void setXYZ( float x, float y, float z ) {
        if ( !initialized ) {
            minX = maxX = x;
            minY = maxY = y;
            minZ = maxZ = z;
            initialized = true;
        } else {
            if ( x < minX ) {
                minX = x;
            } else if ( x > maxX ) {
                maxX = x;
            }
            if ( y < minY ) {
                minY = y;
            } else if ( y > maxY ) {
                maxY = y;
            }
            if ( z < minZ ) {
                minZ = z;
            } else if ( z > maxZ ) {
                maxZ = z;
            }
        }
    }

    public boolean contains( float x, float y, float z ) {
        return(( x>=minX ) && (x<=maxX) && (y>=minY) && (y<=maxY) && (z>=minZ) && (z<=maxZ) );
    }

    public BoundingBox createOverlap( BoundingBox check ) {
        float testXmin;
        float testXmax;
        float testYmin;
        float testYmax;
        float testZmin;
        float testZmax;
        if ( xOverlap( check.getMinX(), check.getMaxX() )) {
            if ( minX > check.getMinX() ) {
                testXmin = minX;
            } else {
                testXmin = check.getMinX();
            }
            if ( maxX < check.getMaxX() ) {
                testXmax = maxX;
            } else {
                testXmax = check.getMaxX();
            }
        } else {
            return( null );
        }
        if ( yOverlap( check.getMinY(), check.getMaxY() )) {
            if ( minY > check.getMinY() ) {
                testYmin = minY;
            } else {
                testYmin = check.getMinY();
            }
            if ( maxY < check.getMaxY() ) {
                testYmax = maxY;
            } else {
                testYmax = check.getMaxY();
            }
        } else {
            return( null );
        }
        if ( zOverlap( check.getMinZ(), check.getMaxZ() )) {
            if ( minZ > check.getMinZ() ) {
                testZmin = minZ;
            } else {
                testZmin = check.getMinZ();
            }
            if ( maxZ < check.getMaxZ() ) {
                testZmax = maxZ;
            } else {
                testZmax = check.getMaxZ();
            }
        } else {
            return( null );
        }
        return( new BoundingBox( testXmin, testXmax, testYmin, testYmax, testZmin, testZmax ));
    }

    boolean xOverlap( float xmin, float xmax ) {
        if ( maxX < xmin ) {
            return( false );
        } else if ( xmax < minX ) {
            return( false );
        } else {
            return( true );
        }
    }

    boolean yOverlap( float ymin, float ymax ) {
        if ( maxY < ymin ) {
            return( false );
        } else if ( ymax < minY ) {
            return( false );
        } else {
            return( true );
        }
    }

    boolean zOverlap( float zmin, float zmax ) {
        if ( maxZ < zmin ) {
            return( false );
        } else if ( zmax < minZ ) {
            return( false );
        } else {
            return( true );
        }
    }
}
