package com.trapezium.humanoid;

import java.util.Hashtable;

public class HumanoidJointInfo {
    static Hashtable jointSwingMarkers = new Hashtable();
    static {
        jointSwingMarkers.put( "r_knee", "-" );
        jointSwingMarkers.put( "l_knee", "-" );
        jointSwingMarkers.put( "r_elbow", "+" );
        jointSwingMarkers.put( "l_elbow", "+" );
        jointSwingMarkers.put( "l_shoulder", "*" );
        jointSwingMarkers.put( "r_shoulder", "*" );
    }

    static public String getSwingMark( String joint ) {
        return( (String)jointSwingMarkers.get( joint ));
    }
}
