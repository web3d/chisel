/*
 * @(#)FetchDialog.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.gui;
import com.trapezium.chisel.DialogOwner;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class FetchDialog extends BaseDialog {
    TextField line;
    TextField othersToAccept;
    Checkbox recurse;
    Checkbox limitBySubdir;
    Checkbox limitBySite;
    Checkbox fetchWRL;
    Checkbox fetchPictures;
    Checkbox fetchSound;
    Checkbox fetchHTML;
    Checkbox fetchMovies;
    Checkbox fetchOther;
    String url;
    
    public FetchDialog(DialogOwner owner, String url) {
        super(owner, "fetchDialogTitle");
        this.url = url;
        
        Container c1 = new Panel();
        c1.setLayout( new PaddedGridLayout( 2, 1, 8, 0, 16, 8, false ) );
        
        Container c2 = new Panel();
        c2.setLayout( new PaddedGridLayout( 2, 1, 0, 8, 0, 16, false ) );
        
        Container c3 = new Panel();
        c3.setLayout(new BorderLayout());
        c3.add("West", new Label(strings.getString("urlLabel")));
        line = new TextField();
        line.setColumns(40);
        line.setText(url);
        line.requestFocus();
        c3.add("Center", line);
        
        recurse = new Checkbox(strings.getString("recurseLabel"), true);
        
        c2.add(c3);
        c2.add(recurse);
        
        Container c4 = new Panel();
        c4.setLayout( new PaddedGridLayout( 2, 1, 8, 0, 32, 8, false ));
        
        CheckboxGroup cb = new CheckboxGroup();
        limitBySubdir = new Checkbox( "Limit retrieval to original directory and subdirectories", true, cb );
        c4.add( limitBySubdir );
        limitBySite = new Checkbox( "Limit retrieval to site", false, cb );
        c4.add( limitBySite );
        
        c1.add(c2);
        c1.add(c4);
        
        Container c5 = new Panel();
        c5.setLayout( new PaddedGridLayout( 7, 1, 8, 0, 48, 12 ));
        
        c5.add( new Label("Fetch file types:") );
        fetchWRL = new Checkbox( "Models (*.wrl, *.wrz, *.x3d, *.x3dv)", true );
        c5.add( fetchWRL );
        fetchPictures = new Checkbox( "Images (*.gif, *.jpg, *.png)", true );
        c5.add( fetchPictures );
        fetchSound = new Checkbox( "Sounds (*.wav, *.mid, *.mp3, *.aif, *.aiff)", true );
        c5.add( fetchSound );
        fetchHTML = new Checkbox( "Documents (*.html, *.htm)", true );
        c5.add( fetchHTML );
        fetchMovies = new Checkbox( "Movies (*.mpeg, *.mpg, *.mov, *.avi)", false );
        c5.add( fetchMovies );
        
        Container c6 = new Panel();
        c6.setLayout(new BorderLayout());
        fetchOther = new Checkbox( "Other extensions:", false );
        c6.add("West", fetchOther);
        othersToAccept = new TextField();
        othersToAccept.setColumns(30);
        c6.add("Center", othersToAccept);
        
        c5.add(c6);
        
        add( "North", c1 );
        add( "Center", c5 );
        setResizable(false);
        line.addKeyListener(this);
    }
    
    
    public String getUrl() {
        return url;
    }
    
    /** Get the "recurse" checkbox state */
    public boolean getRecurseState() {
        return recurse.getState();
    }
    
    public boolean subdirRetrieval() {
        return( limitBySubdir.getState() );
    }
    
    public boolean siteRetrieval() {
        return( limitBySite.getState() );
    }
    
    public boolean pictureRetrieval() {
        return( fetchPictures.getState() );
    }
    
    public boolean wrlRetrieval() {
        return( fetchWRL.getState() );
    }
    
    public boolean soundRetrieval() {
        return( fetchSound.getState() );
    }
    
    public boolean movieRetrieval() {
        return( fetchMovies.getState() );
    }
    
    public boolean htmlRetrieval() {
        return( fetchHTML.getState() );
    }
    
    
    /** Get the state of the "other files" checkbox */
    public boolean getOtherRecurseState() {
        return( fetchOther.getState() );
    }
    
    protected void execute() {
        url = line.getText();
        System.out.println("Fetching " + url );
    }
    
    Vector additionalAcceptors;
    public boolean hasAdditionalAcceptors() {
        additionalAcceptors = null;
        if ( othersToAccept.getText() != null ) {
            additionalAcceptors = new Vector();
            StringBuffer filter = new StringBuffer();
            String excludeList = othersToAccept.getText();
            int strlen = excludeList.length();
            for ( int i = 0; i < strlen; i++ ) {
                char x = excludeList.charAt( i );
                if ( Character.isLetterOrDigit( x ) || ( x == '_' )) {
                    filter.append( x );
                } else if ( filter.length() > 0 ) {
                    additionalAcceptors.addElement( new String( filter ));
                    filter = new StringBuffer();
                }
            }
            if ( filter.length() > 0 ) {
                additionalAcceptors.addElement( new String( filter ));
            }
        }
        if ( additionalAcceptors == null ) {
            return( false );
        } else {
            return( additionalAcceptors.size() > 0 );
        }
    }
    
    public int getNumberAdditionalAcceptors() {
        return( additionalAcceptors.size() );
    }
    
    public String getAdditionalAcceptor( int additionalFilterOffset ) {
        return( (String)additionalAcceptors.elementAt( additionalFilterOffset ));
    }
}
