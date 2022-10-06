package com.trapezium.parse;

public interface LineError {
    int getErrorCount( int lineNo, boolean includeWarnings );
    String getErrorViewerString( int lineNo, int errorStringNo );
    String getErrorStatusString( int lineNo, int errorStringNo );
    int getNextError( int lineNo );
    int getPrevError( int lineNo );
}
