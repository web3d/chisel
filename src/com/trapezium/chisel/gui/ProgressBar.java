package com.trapezium.chisel.gui;

import com.trapezium.util.ProgressIndicator;
import java.awt.*;

public class ProgressBar extends LabelledImageButton implements DisplayConstants, ProgressIndicator {
    int currentPercent = -1;
    int alternatePercent = -1;
    String title;
    Color color = DEFAULT_PROGRESSBARCOLOR;

    // in some cases progress bar temporarily overwrites text display
    // pushText saves text, setPercent(-2) restores it, done this way
    // because temporary overwrite done by FactoryResponseListener taht
    // doesn't know about ChiselTable (which it is overwriting)...
    // can be done better.
    String pushedText;

    public ProgressBar() {
        super( "", null );
    }

    public void setTitle( String title ) {
        this.title = title;
    }
    
    public String getTitle() {
        return( title );
    }

    public void setColor( Color color ) {
        this.color = color;
    }
    
    public Color getColor() {
        return( color );
    }
    
    public void pushText( String s ) {
        pushedText = s;
    }
    

    /** getPercent can be called to find out if the progress bar is
      * active, which is true if 0 <= percent < 100
      */
    public int getPercent() {
        return currentPercent;
    }
    
    public void reset() {
        setPercent( -1 );
        setAlternatePercent( -1 );
    }

    public void setAlternatePercent( int n ) {
        if ( alternatePercent < n ) {
            if ( currentPercent < 0 ) {
                currentPercent = 0;
            }
            alternatePercent = n;
            if ( title != null ) {
                setText( title + " " + currentPercent + "%, IFS " + alternatePercent + "%" );
            } else {
                setText( currentPercent + "%, IFS " + alternatePercent + "%" );
           }
        }
    }
        
    //
    //  ProgressBar convention is -1 means clear out text,
    //  -2 means put back whatever text was there previously
    //  otherwise, just show the title and percent
    //
    public void setPercent( int n ) {
        alternatePercent = -1;
        if ( n != currentPercent ) {
            if (( n == -1 ) || ( n == -2 )) {
                if ( n == -1 ) {
                    setText( "" );
                }  else if ( pushedText != null ) {
                    setText( pushedText );
                } else {
                    setText( "" );
                }
                currentPercent = -1;
            } else if ( n > currentPercent ) {
                if ( title != null ) {
                    setText( title + " " + n + "%" );
                } else {
                    setText( n + "%" );
                }
                currentPercent = n;
            }
        }
    }
    

    	/** draw the image and the label in a specified rectangle. */
	public void paint(Graphics g) {

    	Dimension size = getSize();
    	Insets insets = getInsets();
    	int height = size.height - insets.top - insets.bottom;
        if ( currentPercent > 0 ) {
//    		Color bg = getBackground();
//            Color hiliteColor = bg.brighter().brighter();
            g.setColor(DEFAULT_PROGRESSBARCOLOR);
            g.fillRect(0, insets.top, (size.width*currentPercent)/100, height );
        }
        if ( alternatePercent > 0 ) {
            g.setColor( color );
            g.fillRect( 0, insets.top + height*3/4, (size.width*alternatePercent)/100, height/4 );
        }
        Rectangle bounds = new Rectangle(insets.left + margin.left, insets.top + margin.top, size.width - insets.left - insets.right - margin.left - margin.right, size.height - insets.top - insets.bottom - margin.top - margin.bottom);

		// get the label dimensions
		int labdx = 0;
		int labdy = 0;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null) {
			fm = getFontMetrics(font);
			labdx = fm.stringWidth(getText());
			labdy = fm.getHeight();
		}

        g.setColor(getForeground());
        g.setFont(font);
		int x, y;
		int a;

		if (labdx > 0) {
			a = align & TEXT_ALIGN_HORZ;
			if (a == TEXT_ALIGN_LEFT) {
				x = bounds.x;
			} else if (a == TEXT_ALIGN_RIGHT) {
				x = bounds.x + bounds.width - labdx;
			} else { 	// TEXT_ALIGN_CENTER
				x = bounds.x + (bounds.width - labdx) / 2;
			}
			a = align & TEXT_ALIGN_VERT;
			if (a == TEXT_ALIGN_TOP) {
				y = bounds.y;
			} else if (a == TEXT_ALIGN_BOTTOM) {
				y = bounds.y + bounds.height - labdy;
			} else { 	// TEXT_ALIGN_VCENTER
				y = bounds.y + (bounds.height - labdy) / 2;
			}
			g.drawString(getText(), x, y + fm.getAscent());
		}
	}

    /** by overriding this we effectively ignore button state change behavior
        (i.e. the appearance of being pressed) */
    void setState(boolean pressed, boolean forcepaint) {
    }
}
