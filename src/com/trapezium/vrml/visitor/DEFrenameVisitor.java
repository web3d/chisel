package com.trapezium.vrml.visitor;

import com.trapezium.pattern.Visitor;
import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.RouteElement;

public class DEFrenameVisitor extends Visitor {
    String oldName;
    String newName;

    public DEFrenameVisitor( TokenEnumerator te, String oldName, String newName ) {
        super( te );
        this.oldName = oldName;
        this.newName = newName;
    }

    public boolean visitObject( Object a ) {
        if ( a instanceof DEFUSENode ) {
            DEFUSENode dun = (DEFUSENode)a;
            System.out.println( "Got DUN: " + dun.getId() );
            if ( dun.getId().compareTo( oldName ) == 0 ) {
                System.out.println( "resetting to " + newName );
                dun.resetId( newName );
                int nameOffset = dun.getFirstTokenOffset() + 1;
                dataSource.replace( nameOffset, newName );
            }
        } else if ( a instanceof ROUTE ) {
            ROUTE r = (ROUTE)a;
            String sourceDEF = r.getSourceDEFname();
            String destDEF = r.getDestDEFname();
            System.out.println( "Got ROUTE " + sourceDEF + " to " + destDEF );
            if ( sourceDEF.compareTo( oldName ) == 0 ) {
                System.out.println( "resetting src to " + newName );
                r.setSourceDEFname( newName );
                RouteElement re = r.getRouteElement( 0 );
                if ( re != null ) {
                    int offset = re.getFirstTokenOffset();
                    if ( offset != -1 ) {
                        dataSource.replace( offset, newName + "." + re.getFieldName() );
                    }
                }
            }
            if ( destDEF.compareTo( oldName ) == 0 ) {
                System.out.println( "resetting dest to " + newName );
                r.setDestDEFname( newName );
                RouteElement re = r.getRouteElement( 1 );
                if ( re != null ) {
                    int offset = re.getFirstTokenOffset();
                    if ( offset != -1 ) {
                        dataSource.replace( offset, newName + "." + re.getFieldName() );
                    }
                }
            }
        }
        return( true );
    }
}
