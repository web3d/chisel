/*
    Slider.java
*/

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.BitSet;

/** A lightweight slider */
public class Slider extends Container implements Adjustable, DisplayConstants {

    public static int ErrorMarkCount = 500;

    public final static int WIDTH_VERTICAL = 15;
    public final static int HEIGHT_HORIZONTAL = 15;
    public final static int BAR_LENGTH = 7;
    public final static int EDGE_GAP = 0;
    public final static int ENDCAP_LENGTH = 11;

    private int orientation;
    private int low;
    private int high;
    private int position;
    private int visibleAmount;
    private int increment = 1;
    private int blockIncrement = 10;
    private int endcapLength = ENDCAP_LENGTH;
    AdjustmentListener adjustmentListener;

    boolean opaque = false;

    Component crossbar;
    ComponentAdapter tracker;

	public Slider(int orientation, int low, int high) {
        this.orientation = orientation;
        if (high <= low) {
            high = low + 1;
        }
	    this.low = low;
	    this.high = high;
	    setLayout(null);
	    setBackground(DEFAULT_CONTROLCOLOR);

	    crossbar = new CrossBar(orientation);
	    add(crossbar);
        tracker = new CrossBarTracker();
        crossbar.addComponentListener(tracker);
        addMouseListener(new SliderClicker());
 	    setValue(0);
	}

    int pixelsPerLine = 1;
	public void setRatio( int displayed, int total, int pixelsPerLine ) {
	    Dimension size = getSize();
	    if ( pixelsPerLine > 0 ) {
    	    this.pixelsPerLine = pixelsPerLine;
    	}
    	if (total == 0) {
    	    return;
    	}
        if (displayed > total) {
            displayed = total;
        }
        //System.out.println( "Slider.setRatio displayed " + displayed + " of " + total + " with " + pixelsPerLine + " pixels per line " );
	    int newSize = ( displayed * ( size.height - 2*endcapLength ))/total;
	    if ( newSize > BAR_LENGTH ) {
    	    ((CrossBar)crossbar).setVSize( newSize );
    	    doLayout();
    	    repaint();
    	}
	}

	BitSet errorMarks;
	BitSet warningMarks;
	BitSet nonconformanceMarks;

	public void setErrorMarks( BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
	    this.errorMarks = errorMarks;
	    this.warningMarks = warningMarks;
	    this.nonconformanceMarks = nonconformanceMarks;
	}

    class CrossBar extends Component {
        int ownerOrientation;
        public CrossBar(int ownerOrientation) {
            this.ownerOrientation = ownerOrientation;
            setSize(getPreferredSize());
            ConstrainedMover mover = new ConstrainedMover(this);
            mover.setDirection(ownerOrientation == HORIZONTAL ? ConstrainedMover.EASTWEST : ConstrainedMover.NORTHSOUTH);
            mover.setConstrainedToParent(true);
            Insets insets = new Insets(0, 0, 0, 0);
            if (ownerOrientation == HORIZONTAL) {
                insets.left = endcapLength;
                insets.right = endcapLength;
            } else {
                insets.top = endcapLength;
                insets.bottom = endcapLength;
            }
            mover.setInsets(insets);
            addMouseListener(mover);
            addMouseMotionListener(mover);
        }
        int vsize = -1;
        public void setVSize( int vsize ) {
            this.vsize = vsize;
            Dimension s = getSize();
            s.height = vsize;
            setSize( s );
            //System.out.println( "Slider setVSize" );
        }
        public Dimension getMinimumSize() {
            if (ownerOrientation == HORIZONTAL) {
                return new Dimension(BAR_LENGTH, HEIGHT_HORIZONTAL);
            } else {
                if ( vsize != -1 ) {
                    return( new Dimension( WIDTH_VERTICAL, vsize ));
                } else {
                    return new Dimension(WIDTH_VERTICAL, BAR_LENGTH);
                }
            }
        }
        public Dimension getPreferredSize() {
            return getMinimumSize();
        }
        public void paint(Graphics g) {
            Dimension size = getSize();
            g.setColor(getBackground());

            if (ownerOrientation == HORIZONTAL) {
                int dxy = size.height - 3;
                for (int x = -dxy; x < size.width - 1; x += 2) {
                    g.drawLine(x + 1, 1, x + dxy, dxy);
                    //g.drawLine(x + 1, dxy, x + dxy, 1);
                }

            } else {
                int dxy = size.width - 3;
                for (int y = -dxy; y < size.height - 1; y += 2) {
                    g.drawLine(1, y + 1, dxy, y + dxy);
                    //g.drawLine(dxy, y + 1, 1, y + dxy);
                }
            }

            g.draw3DRect(0, 0, size.width - 1, size.height - 1, true);
            g.drawRect(1, 1, size.width - 3, size.height - 3);

            //if (size.width > 5 && size.height > 5) {
            //    g.draw3DRect(2, 2, size.width - 5, size.height - 5, false);
            //}
            //g.setColor(BGCOLOR.brighter());
            //g.draw3DRect(1, 1, size.width - 3, size.height - 3, true);
        }
    }

    class CrossBarTracker extends ComponentAdapter {
        public void componentMoved(ComponentEvent evt) {
            Container parent = evt.getComponent().getParent();
            if (parent instanceof Slider) {
                Slider slider = (Slider) parent;
                slider.componentMoved(evt);
            }
        }
    }

    class SliderClicker extends MouseAdapter {
        public SliderClicker() {
        }

        public void mousePressed(MouseEvent evt) {
            int x = evt.getX();
            int y = evt.getY();
            Slider slider;
            try {
                slider = (Slider) evt.getComponent();
            } catch (Exception e) {
                System.out.println("SliderClicker only works with a Slider");
                return;
            }
            int incr = 0;
            if (slider.getSlidableRect().contains(x, y)) {
                incr = slider.getBlockIncrement();
            } else {
                incr = slider.getUnitIncrement();
            }

            Point cbpt = slider.crossbar.getLocation();
            if (x < cbpt.x || y < cbpt.y) {
                incr = -incr;
            }

            // setValue checks bounds, so we don't have to
            slider.setValue(slider.getValue() + incr);
        }
    }


    public void componentMoved(ComponentEvent evt) {
        // put notification logic here
        setValueFromCrossbar();
    }


    public void setValueFromCrossbar() {
	    Rectangle sliderect = getSlidableRect();
	    Rectangle cbrect = crossbar.getBounds();

	    int effrange = high*pixelsPerLine - low*pixelsPerLine - visibleAmount;
	    int newpos = 0;
		if (orientation == VERTICAL) {
            newpos = low + (effrange * (cbrect.y - sliderect.y + cbrect.height/2) + sliderect.height - 1) / ( sliderect.height * pixelsPerLine );
        } else {
            newpos = low + (effrange * (cbrect.x - sliderect.x + cbrect.width/2) + sliderect.width - 1) / sliderect.width;
        }
        if (newpos != position) {
            setValue(newpos);
        }
    }

    /** set the position of the crossbar to reflect the current
        value */
    private void positionCrossBar() {
	    Dimension size = crossbar.getSize();
	    Rectangle sliderect = getSlidableRect();
        int effrange = (high - low) * pixelsPerLine - visibleAmount;
        int x, y;
	    if (orientation == VERTICAL) {
	        x = 0;
    	    y = sliderect.y - size.height / 2;
	        if (effrange > 0) {
	            y += ((position - low) * pixelsPerLine * sliderect.height) / effrange;
	        }
	    } else {
    	    x = sliderect.x - size.width / 2;
	        if (effrange > 0) {
	            x += ((position - low) * pixelsPerLine * sliderect.width) / effrange;
	        }
	        y = 0;
        }
        // remove the tracker so we don't end up here again
        crossbar.removeComponentListener(tracker);

	    crossbar.setBounds(x, y, size.width, size.height);

        // start tracking moves again
        crossbar.addComponentListener(tracker);
    }


    static Color bg = DEFAULT_CONTROLCOLOR;
    static Color bgdark = bg.darker();
    static Color color = DEFAULT_GROOVECOLOR;
    static Color dark = color.darker();
    static Color bright = color.brighter();
    static Color realbright = bright.brighter();
    static Color realdark = dark.darker();

	/** draw the slider. */
	public void paint(Graphics g) {

        Rectangle rect = getControlRect();
        Dimension cbsize = crossbar.getSize();
        Dimension size = getSize();

        if (orientation == VERTICAL) {
            //background
            if (opaque) {
                g.setColor(bgdark);
                g.fill3DRect(0, endcapLength, size.width - EDGE_GAP, size.height - 2 * endcapLength, true);
            }

            // endcaps
            //g.setColor(bg);
            //g.draw3DRect(0, 0, cbsize.width - 1, endcapLength - 1, true);
            //g.draw3DRect(0, size.height - endcapLength - 1, cbsize.width - 1, endcapLength, true);

            // endcap symbols
            g.setColor(color);
            draw3DTriangle(g, 0, 0, cbsize.width - 1, endcapLength - 1, UP);
            draw3DTriangle(g, 0, size.height - endcapLength, cbsize.width - 1, endcapLength - 1, DOWN);
            // groove
            int xmid = rect.x + rect.width/2;
            int y = rect.y + endcapLength - 1;
            int h = rect.height - 2 * endcapLength;
            g.setColor(color);
            g.drawLine(xmid, y, xmid, y + h);
            g.draw3DRect(xmid - 1, y, 2, h, opaque ? false : true);
            g.setColor(opaque ? realdark : realbright);
            g.drawLine(xmid - 2, y, xmid - 2, y + h);
            g.setColor(opaque ? realbright : realdark);
            g.drawLine(xmid + 2, y, xmid + 2, y + h);

            // draw error marks in red distributed over the groove
            if ( errorMarks != null ) {
                int eyval = -1;
                int wyval = -1;
                int nyval = -1;
                g.setColor( Color.red );
                h = size.height - 2 * endcapLength;
                for ( int i = 0; i < Slider.ErrorMarkCount; i++ ) {
                    if ( errorMarks.get( i )) {
                        int testy = ( i * h )/Slider.ErrorMarkCount + endcapLength;
                        if ( testy != eyval ) {
                            eyval = testy;
                            g.drawLine( rect.x + 1, eyval, xmid -1, eyval );
                        }
                    }
                }
                g.setColor( Color.blue );
                for ( int i = 0; i < Slider.ErrorMarkCount; i++ ) {
                    if ( warningMarks.get( i )) {
                        int testy = ( i * h )/Slider.ErrorMarkCount + endcapLength;
                        if ( testy != wyval ) {
                            wyval = testy;
                            g.drawLine( xmid + 3, wyval, rect.x + rect.width - 1, wyval );
                        }
                    }
                }
                g.setColor( Color.yellow );
                int midwidth = ( xmid - rect.x )/2;
                for ( int i = 0; i < Slider.ErrorMarkCount; i++ ) {
                    if ( nonconformanceMarks.get( i )) {
                        int testy = ( i * h )/Slider.ErrorMarkCount + endcapLength;
                        if ( testy != nyval ) {
                            nyval = testy;
                            g.drawLine( rect.x + 1, nyval, rect.x + midwidth, nyval );
                        }
                    }
                }
            }
        } else {
            //background
            if (opaque) {
                g.setColor(bgdark);
                g.fill3DRect(endcapLength, 0, size.width - 2 * endcapLength, size.height - EDGE_GAP, true);
            }

            // endcaps
            //g.setColor(bg);
            //g.draw3DRect(0, 0, endcapLength - 1, cbsize.height - 1, true);
            //g.draw3DRect(size.width - endcapLength, 0, endcapLength - 1, cbsize.height - 1, true);

            // endcap symbols
            draw3DTriangle(g, 0, 0, endcapLength - 1, cbsize.height - 1, LEFT);
            draw3DTriangle(g, size.width - endcapLength, 0, endcapLength - 1, cbsize.height - 1, RIGHT);

            // groove
            int ymid = rect.y + rect.height/2;
            int x = rect.x + endcapLength - 1;
            int w = rect.width - 2 * endcapLength;
            g.setColor(color);
            g.drawLine(x, ymid, x + w, ymid);
            g.draw3DRect(x, ymid - 1, w, 2, opaque ? false : true);
            g.setColor(opaque ? realdark : realbright);
            g.drawLine(x, ymid - 2, x + w, ymid - 2);
            g.setColor(opaque ? realbright : realdark);
            g.drawLine(x, ymid + 2, x + w, ymid + 2);
        }

        super.paint(g);
	}

    final static int LEFT = 0;
    final static int UP = 1;
    final static int RIGHT = 2;
    final static int DOWN = 3;
    void draw3DTriangle(Graphics g, int x, int y, int width, int height, int direction) {

        if (direction ==  UP || direction == DOWN) {
            x += width /2;
            if (direction == DOWN) {
                y += height;
                height = -height;
            }

            g.setColor(color);
            g.drawLine(x, y, x, y + height);
            g.drawLine(x, y, x + 1, y + height);
            g.setColor(direction == DOWN ? bright : dark);
            g.drawLine(x + 5, y + height, x - 5, y + height);
            height += (direction == DOWN ? 1 : -1);
            g.setColor(bright);
            g.drawLine(x, y, x - 1, y + height);
            g.drawLine(x, y, x - 2, y + height);
            g.setColor(realbright);
            g.drawLine(x, y, x - 3, y + height);
            g.drawLine(x, y, x - 4, y + height);
            g.setColor(dark);
            g.drawLine(x, y, x + 2, y + height);
            g.drawLine(x, y, x + 3, y + height);
            height += (direction == DOWN ? -1 : 1);
            g.setColor(realdark);
            g.drawLine(x, y, x + 4, y + height);
            g.drawLine(x, y, x + 5, y + height);
            g.drawLine(x, y, x - 5, y + height);

        } else {
            y += height/2;
            if (direction == RIGHT) {
                x += width;
                width = -width;
            }

            g.setColor(color);
            g.drawLine(x, y, x + width, y);
            g.drawLine(x, y, x + width, y + 1);
            g.setColor(direction == RIGHT ? bright : dark);
            g.drawLine(x + width, y - 5, x + width, y + 5);
            width += (direction == RIGHT ? 1 : -1);
            g.setColor(bright);
            g.drawLine(x, y, x + width, y - 1);
            g.drawLine(x, y, x + width, y - 2);
            g.setColor(realbright);
            g.drawLine(x, y, x + width, y - 3);
            g.drawLine(x, y, x + width, y - 4);
            g.setColor(dark);
            g.drawLine(x, y, x + width, y + 2);
            g.drawLine(x, y, x + width, y + 3);
            width += (direction == RIGHT ? -1 : 1);
            g.setColor(realdark);
            g.drawLine(x, y, x + width, y + 4);
            g.drawLine(x, y, x + width, y + 5);
            g.drawLine(x, y, x + width, y - 5);
        }
    }

    Rectangle getControlRect() {
		Dimension size = getSize();
		int x = 0;
		int y = 0;

		if (orientation == VERTICAL) {
		    if (size.width > WIDTH_VERTICAL) {
                x += (size.width - WIDTH_VERTICAL) / 2;
    		    size.width = WIDTH_VERTICAL;
    		}
		    y += EDGE_GAP / 2;
		}
		if (orientation == HORIZONTAL) {
		    x += EDGE_GAP / 2;
		    if (size.height > HEIGHT_HORIZONTAL) {
                y += (size.height - HEIGHT_HORIZONTAL) / 2;
    		    size.height = HEIGHT_HORIZONTAL;
    		}
		}
  		size.width -= EDGE_GAP;
	    size.height -= EDGE_GAP;
        return new Rectangle(x, y, size.width, size.height);
    }

    Rectangle getSlidableRect() {
        Rectangle rect = getControlRect();
        Dimension cbdim = crossbar.getSize();
        if (orientation == VERTICAL) {
            rect.y += endcapLength + cbdim.height / 2;
            rect.height -= 2 * endcapLength + cbdim.height;
        } else {
            rect.x += endcapLength + cbdim.width / 2;
            rect.width -= 2 * endcapLength + cbdim.width;
        }
        return rect;
    }

    /**
    * The preferred size of the slider.
    */
    public Dimension getPreferredSize() {
        Dimension dim = getMinimumSize();
        Container parent = getParent();
        if (parent != null) {
            Dimension parentsize = parent.getSize();

            // assume that the parent has a scrollbar of the other
            // persuasion and that its "skinny" dimension is the same
            if (orientation == HORIZONTAL) {
                dim.width = parentsize.width - dim.height;
            } else {
                dim.height = parentsize.height - dim.width;
            }
        }
        return dim;
    }


    /**
     * The minimum size of the slider.
     */
    public Dimension getMinimumSize() {
        Dimension dim = crossbar.getMinimumSize();
        dim.width += EDGE_GAP;
        dim.height += EDGE_GAP;
        return dim;
    }

    public void doLayout() {
        // calculate the endcap length
        Dimension size = getSize();
        int length = (orientation == HORIZONTAL) ? size.width : size.height;
        if (length < 4 * ENDCAP_LENGTH) {
            endcapLength = Math.max(3, length / 4);
        } else {
            endcapLength = ENDCAP_LENGTH;
        }
        positionCrossBar();
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public boolean isOpaque() {
        return opaque;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setMinimum(int min) {
       	low = min/pixelsPerLine;
    	if (high <= low) {
    	    high = low + 1;
    	}
    	if (visibleAmount < 0) {
    	    visibleAmount = 0;
    	} else if (visibleAmount > (high - low) * pixelsPerLine) {
    	    visibleAmount = (high - low) * pixelsPerLine;
    	}
    	positionCrossBar();
    }

    public int getMinimum() {
        return low;
    }

    public void setMaximumLine( int maxline ) {
        setMaximum( maxline*pixelsPerLine );
    }
    /** max is in pixels */
    public void setMaximum(int max) {
       	high = (max + pixelsPerLine - 1)/pixelsPerLine;  // round up
    	if (high <= low) {
    	    if ( high > 0 ) {
        	    low = high - 1;
        	} else {
        	    low = high;
        	}
    	}
    	if (visibleAmount < 0) {
    	    visibleAmount = 0;
    	} else if (visibleAmount > (high - low) * pixelsPerLine) {
    	    visibleAmount = (high - low) * pixelsPerLine;
    	}
    	positionCrossBar();
    }

    public int getMaximum() {
        return high;
    }

    public synchronized void setUnitIncrement(int u) {
	    increment = u;
    }

    public int getUnitIncrement() {
        return increment;
    }

    public synchronized void setBlockIncrement(int b) {
        blockIncrement = b;
    }

    public int getBlockIncrement() {
        return blockIncrement;
    }

    public void setVisibleAmountLine( int v ) {
        setVisibleAmount( v*pixelsPerLine );
    }
    public void setVisibleAmount(int v) {
        visibleAmount = v;
    }

    public int getVisibleAmount() {
        return visibleAmount;
    }

    public void setValue(int value) {
        //System.out.println("Slider setting value to " + value + " in range (" + low + "," + high + ")");
        if (value < low) {
            value = low;
        } else if (value > high - visibleAmount/pixelsPerLine) {
            value = high - visibleAmount/pixelsPerLine;
            //if ( value < 0 ) {
            //    value = 0;
            //}
        }
        if (value != position) {
    	    position = value;
    	    if (adjustmentListener != null) {
                //System.out.println("adjustmentListener is " + adjustmentListener);
           	    AdjustmentEvent e = new AdjustmentEvent(this, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED, AdjustmentEvent.TRACK, position);
           	    adjustmentListener.adjustmentValueChanged(e);
            }
          	positionCrossBar();
	    }
    }

    public int getValue() {
        return position;
    }

    public synchronized void addAdjustmentListener(AdjustmentListener l) {

    	adjustmentListener = AWTEventMulticaster.add(adjustmentListener, l);
    }

    public synchronized void removeAdjustmentListener(AdjustmentListener l) {
	    adjustmentListener = AWTEventMulticaster.remove(adjustmentListener, l);
    }
}

