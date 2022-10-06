/*
    LabelledImageButton.java
*/

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;


/** A lightweigt button with an image plus a label */
public class LabelledImageButton extends Component implements LabelConstants, MouseListener, MouseMotionListener, Control {

    public final static int RECTANGLE = 0;
    public final static int OVAL = 1;

    protected final static Insets stdMargin = new Insets(2, 2, 3, 3);

	/** return true if label and image alignment are the same */
	public final boolean coaligned() {
		return (align & TEXT_ALIGN) == ((align & IMAGE_ALIGN) >> 4);
	}

    ActionListener actionListener;
    boolean rollover = false;
    boolean rolloverEnabled = true;
    boolean signal = false;
    boolean pressed = false;
    boolean sticky = false;
    boolean stickyDown = false;
    boolean hilightDown = false;
    boolean adjustInset = true;
    boolean labelVisible = true;
    boolean accessAllowed = true;
    public void setAccess( boolean val ) {
        accessAllowed = val;
    }

    String actionCommand = null;
	String label = null;
	String tip = null;
	Image image = null;
	int align = TEXT_ALIGN_CENTER | TEXT_ALIGN_VCENTER | IMAGE_ALIGN_CENTER | IMAGE_ALIGN_VCENTER;
	int gap = 2;	// the space between the label and image if they are coaligned
    Insets insets = new Insets(0, 0, 0, 0);
    Insets margin = (Insets) stdMargin.clone();
    boolean opaque = true;
    int shape = RECTANGLE;

    // true if image isn't there yet when the button is created
    boolean tracking = false;

    // used by drawing logic to interpret state correctly
    protected transient int mousex = 0;
    protected transient int mousey = 0;

	public LabelledImageButton() {
        this(null, null, null);
	}

	public LabelledImageButton(String label, Image image) {
	    this(label, image, null);
	}

	public LabelledImageButton(String label, Image image, String tip) {
		this.label = label;
		this.image = image;
		this.tip = tip;

        // kick off image loading
        if (image != null) {
    		int w = image.getWidth(this);
    		if (w <= 0) {
    		    tracking = true;
    		}

        }

        setFont(FontPool.getLabelFont());

        addMouseListener(this);
        addMouseMotionListener(this);
        //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
	}

	public LabelledImageButton(String label, Image image, int align) {
		this(label, image);
		this.align = align;
	}


    public void enableRollover(boolean enabled) {
        rolloverEnabled = enabled;
    }

	public void setSticky(boolean sticky) {
	    this.sticky = sticky;
	}

	public void setHilightDown(boolean hilight) {
	    hilightDown = hilight;
	}

	public void setShape(int shape) {
	    this.shape = shape;
	}

	public void setValue(Object value) {
        setBooleanValue( "true".equalsIgnoreCase(value.toString()) );
	}

	public void setBooleanValue(boolean down) {
	    stickyDown = down;
	    setMargins();
	}

	public Object getValue() {
	    return( getBooleanValue() ? "true" : "false" );
	}

	public boolean getBooleanValue() {
	    return stickyDown;
	}

	public String getText() {
		return label;
	}

	public void setText( String l ) {
	    label = l;
	    repaint();
	}

	public String getTip() {
	    return tip;
	}

	public void setTip( String tip ) {
	    this.tip = tip;
	}

	public Image getImage() {
		return image;
	}

	public void setLabelVisible(boolean visible) {
	    labelVisible = visible;
	}

	public boolean isLabelVisible() {
	    return labelVisible;
	}

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public boolean isOpaque() {
        return opaque;
    }

    public void setInsets(Insets insets) {
        this.insets = (Insets) insets.clone();
    }

    public void setInsets(int top, int left, int bottom, int right) {
        this.insets = new Insets(top, left, bottom, right);
    }

    public Insets getInsets() {
        return( insets );
    }

    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
        if (tracking && w > 0) {
            tracking = false;
            Container c = getParent();
            if (c != null) {
                c.doLayout();
            }
        }
        return super.imageUpdate(img, flags, x, y, w, h);
    }

    public void setMargins(Insets margin) {
        this.margin = margin;
        setMargins();
    }


    void setMargins() {
        if (adjustInset) {
            int high = Math.max(margin.top, margin.bottom);
            int low = Math.min(margin.top, margin.bottom);
            if (stickyDown || (pressed && contains(mousex, mousey))) {
                margin.top = high;
                margin.left = high;
                margin.bottom = low;
                margin.right = low;
            } else {
                margin.top = low;
                margin.left = low;
                margin.bottom = high;
                margin.right = high;
            }
        }
    }


    void setState(boolean pressed, boolean forcepaint) {
        if (this.pressed != pressed || forcepaint) {
            this.pressed = pressed;
            if (sticky && pressed) {
                stickyDown = !stickyDown;
                //System.out.println( "button " + stickyDown );
            }
            setMargins();
            repaint();
        }
    }

    /** turn the signal on or off.  A button in the signal state is
        displayed highlighted whether up or down.  The signal is set
        by the application and has no meaning to the button. */
	public void setSignal(boolean signal) {
        if (signal != this.signal) {
    	    this.signal = signal;
    	    repaint();
    	}
	}


    public void drawBorder(Graphics g) {
 		Dimension size = getSize();
		drawBorder( g, insets.left, insets.top, size.width - insets.left - insets.right, size.height - insets.top - insets.bottom );
	}

	public void drawBorder( Graphics g, int x, int y, int width, int height ) {
		Color bg = getBackground();
        if (!accessAllowed) {
            bg = Color.red.darker().darker();
        } else if (signal) {
	        bg = new Color(bg.getRed()/2, 255 - (7*(255 - bg.getGreen())/8), bg.getBlue());
	    }
        Color hiliteColor = bg.brighter();
        Color shadowColor = bg.darker().darker();
        Color liteShadowColor = bg.darker();

		if ((rolloverEnabled && rollover) || (hilightDown && (stickyDown || (sticky && pressed)))) {
  		    bg = hiliteColor;
  		    hiliteColor = bg.brighter();
		}

        if (opaque) {
            g.setColor(bg);

            switch (shape) {
                case RECTANGLE:
                    g.fillRect(x, y, width, height);
                    break;
                case OVAL:
                    g.fillOval(x, y, width, height);
                    break;
            }

            // experiment in screen-based 3D perspective
            //Point ptScreen = getLocationOnScreen();
            //ScreenD.fill3DRect(g, x, y, width, height, 10, ptScreen.x, ptScreen.y, true);
        }

        // button is drawn in the down state when any of the following are true:
        //
        //   -- the button is in a sticky down state
        //   -- the button is pressed and the mouse is within the bounds of the button
        //   -- the margins are symmetrical
        if (stickyDown || (pressed && contains(mousex, mousey)) || (margin.top == margin.bottom && margin.left == margin.right)) {
    		g.setColor(shadowColor);

            switch (shape) {
                case RECTANGLE:
            		g.drawLine(x, y, x + width - 1, y);
            		//g.drawLine(x + 1, y + 1, x + width - 1, y + 1);
            		g.drawLine(x, y, x, y + height - 1);
            		//g.drawLine(x + 1, y + 1, x + 1, y + height - 1);
            		break;
                case OVAL:
            		g.drawArc(x, y, width - 1, height - 1, 45, 180);
               		break;
            }

        // button up
        } else {
            // for ovals
            int w34 = (3 * width) / 4;
            int h34 = (3 * height) / 4;
            int w4 = width / 4;
            int h4 = height / 4;

            g.setColor(hiliteColor);
            switch (shape) {
                case RECTANGLE:
        	    	g.drawLine(x, y, x + width - 2, y);
        		    g.drawLine(x, y, x, y + height - 2);
            		break;
                case OVAL:
            		g.drawArc(x, y, width - 2, height - 2, 45, 180);
                    break;
            }

    		g.setColor(shadowColor);
            switch (shape) {
                case RECTANGLE:
        	    	g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
            		g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
            		break;
                case OVAL:
            		g.drawArc(x, y, width - 1, height - 1, 45, -180);
                    break;
            }

    		g.setColor(liteShadowColor);
            switch (shape) {
                case RECTANGLE:
    		        g.drawLine(x + width - 2, y + 1, x + width - 2, y + height - 2);
	            	g.drawLine(x + 1, y + height - 2, x + width - 2, y + height - 2);
            		break;
                case OVAL:
            		g.drawArc(x, y, width - 2, height - 2, 45, -180);
                    break;
            }

        }
	}

    public void update(Graphics g) {
        paint( g );
    }
	/** draw the image and the label in a specified rectangle. */
	public void paint(Graphics g) {

        drawBorder(g);

		Dimension size = getSize();
        Rectangle bounds = new Rectangle(insets.left + margin.left, insets.top + margin.top, size.width - insets.left - insets.right - margin.left - margin.right, size.height - insets.top - insets.bottom - margin.top - margin.bottom);

		// get the image dimensions
		int imgdx = 0;
		int imgdy = 0;
		if (image != null) {
			imgdx = image.getWidth(this);
			imgdy = image.getHeight(this);
			if (imgdx <= 0 || imgdy <= 0) {
				imgdx = 4;
				imgdy = 4;
			}
		}

		// get the label dimensions
		int labdx = 0;
		int labdy = 0;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null) {
			fm = getFontMetrics(font);
			labdx = fm.stringWidth(label);
			labdy = fm.getHeight();
		}

        g.setColor(getForeground());
        g.setFont(font);
		int x, y;
		int a;

		// if text and image are aligned together, do them specially
		if (imgdx > 0 && labdx > 0 && coaligned() && labelVisible) {

			// image and label are arrayed vertically, with the image
			// above the label
			int totaldx = (imgdx > labdx ? imgdx : labdx);
			int totaldy = imgdy + gap + labdy;

			a = align & TEXT_ALIGN_HORZ;
			if (a == TEXT_ALIGN_LEFT) {
				x = bounds.x;
			} else if (a == TEXT_ALIGN_RIGHT) {
				x = bounds.x + bounds.width - totaldx;
			} else { 	// TEXT_ALIGN_CENTER
				x = bounds.x + (bounds.width - totaldx) / 2;
			}
			a = align & TEXT_ALIGN_VERT;
			if (a == TEXT_ALIGN_TOP) {
				y = bounds.y;
			} else if (a == TEXT_ALIGN_BOTTOM) {
				y = bounds.y + bounds.height - totaldy;
            } else if (a == TEXT_ALIGN_VCENTERASCENT) {
				y = bounds.y + (bounds.height - totaldy + fm.getDescent()) / 2;
			} else { 	// TEXT_ALIGN_VCENTER
				y = bounds.y + (bounds.height - totaldy) / 2;
			}

			// draw the image
			int xx = x + (imgdx < labdx ? (labdx - imgdx) / 2 : 0);
			g.drawImage(image, xx, y, this);

			// draw the text
			xx = x + (labdx < imgdx ? (imgdx - labdx) / 2 : 0);
			g.drawString(label, xx, y + imgdy + gap + fm.getAscent());

		// not coaligned, do separately
		} else {
			if (imgdx > 0) {
				a = align & IMAGE_ALIGN_HORZ;
				if (a == IMAGE_ALIGN_LEFT) {
					x = bounds.x;
				} else if (a == IMAGE_ALIGN_RIGHT) {
					x = bounds.x + bounds.width - imgdx;
				} else { 	// IMAGE_ALIGN_CENTER
					x = bounds.x + (bounds.width - imgdx) / 2;
				}
				a = align & IMAGE_ALIGN_VERT;
				if (a == IMAGE_ALIGN_TOP) {
					y = bounds.y;
				} else if (a == IMAGE_ALIGN_BOTTOM) {
					y = bounds.y + bounds.height - imgdy;
				} else { 	// IMAGE_ALIGN_VCENTER
					y = bounds.y + (bounds.height - imgdy) / 2;
				}
				g.drawImage(image, x, y, this);
			}

			if (labdx > 0 && labelVisible) {
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
                } else if (a == TEXT_ALIGN_VCENTERASCENT) {
    				y = bounds.y + (bounds.height - labdy + fm.getDescent()) / 2;
				} else { 	// TEXT_ALIGN_VCENTER
					y = bounds.y + (bounds.height - labdy) / 2;
				}

				g.drawString(label, x, y + fm.getAscent());
			}
		}
	}
    /**
    * The preferred size of the button.
    */
    public Dimension getPreferredSize() {
        Dimension dim = getMinimumSize();
        if (shape == RECTANGLE) {
            dim.width += 4;
            dim.height += 4;
        }
        return dim;
    }


    /**
     * The minimum size of the button.
     */
    public Dimension getMinimumSize() {
		// get the image dimensions
		int imgdx = 0;
		int imgdy = 0;
		if (image != null) {
			imgdx = image.getWidth(this);
			imgdy = image.getHeight(this);
			if (imgdx <= 0 || imgdy <= 0) {
				imgdx = 0;
				imgdy = 0;
			}
		}

		// get the label dimensions
		int labdx = 0;
		int labdy = 0;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null && labelVisible) {
			fm = getFontMetrics(font);
			labdx = fm.stringWidth(label);
			labdy = fm.getHeight() - fm.getLeading();
            // ignore descender if the text alignment does
			if ((align & TEXT_ALIGN_VERT) == TEXT_ALIGN_VCENTERASCENT) {
			    labdy -= fm.getDescent();
			}
		}
        return new Dimension(imgdx + labdx + margin.left + margin.right + insets.left + insets.right, imgdy + labdy + margin.top + margin.bottom + insets.top + insets.bottom);
    }


    /**
     * Sets the command name for the action event fired
     * by this button. By default this action command is
     * set to match the label of the button.
     * @param     command  A string used to set the button's
     *                  action command.
     * @see       java.awt.event.ActionEvent
     * @since     JDK1.1
     */
    public void setActionCommand(String command) {
        actionCommand = command;
    }

    /**
     * Returns the command name of the action event fired by this button.
     */
    public String getActionCommand() {
        return (actionCommand == null? label : actionCommand);
    }


    /**
     * Adds the specified action listener to receive action events
     * from this button.
     * @param listener the action listener
     */
    public void addActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.add(actionListener, listener);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    }

    /**
     * Removes the specified action listener so it no longer receives
     * action events from this button.
     * @param listener the action listener
     */
    public void removeActionListener(ActionListener listener) {
        actionListener = AWTEventMulticaster.remove(actionListener, listener);
    }


    /**
     * Paints the button and distributes an action event to all listeners.
     *
    public void processMouseEvent(MouseEvent e) {
        mousex = e.getX();
        mousey = e.getY();
        switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
		        setState(true, false);
	            break;
            case MouseEvent.MOUSE_RELEASED:
	            if (contains(mousex, mousey)) {
	                if (actionListener != null) {
    	                actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, label));
    	            }
	                //if (sticky) {
	                //    stickyDown = !stickyDown;
	                //    System.out.println("stickyDown is now " + stickyDown);
	                //}
	            }
		        setState(false, false);
	            break;
            case MouseEvent.MOUSE_ENTERED:
                if (pressed) {
                    setState(true, true);
                }
	            break;
            case MouseEvent.MOUSE_EXITED:
                if (pressed) {
                    setState(true, true);
                }
	            break;
        }
        super.processMouseEvent(e);
    } ***/


    static Tooltip tooltip = null;


	// MouseListener interface
	public void mouseClicked( MouseEvent e ) {
	}

	public void mouseEntered( MouseEvent e ) {
        // start tooltip, create it if necessary
        if (tooltip == null) {
            Container parent = getParent();
            while (parent != null && !(parent instanceof Frame)) {
                parent = parent.getParent();
            }
            if (parent == null) {
                System.out.println("No frame!  Null pointer exception will follow.");
            } else {
                tooltip = new Tooltip((Frame) parent);
            }
        }
        tooltip.activate(this);

        // rollover
	    rollover = true;
        if (pressed) {
            setState(true, true);
        } else {
            repaint();
        }
	}

	public void mouseExited( MouseEvent e ) {
        tooltip.deactivate();

        // rollover
	    rollover = false;
        if (pressed) {
            setState(true, true);
        } else {
            repaint();
        }
    }

	public void mousePressed( MouseEvent e ) {
        tooltip.deactivate();
        setState(true, false);
	}

    /** handle mouse release event.  If the mouse is released within the originating component,
        an action is triggered.  Note that this button is always the source of the action
        event regardless of the component generating the mouse message.  This is to allow
        other components to be tied to this button (by adding this button as a mouse listener).
    */
	public void mouseReleased( MouseEvent e ) {
        if (e.getComponent().contains(e.getX(), e.getY())) {
            if (actionListener != null) {
                actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, label));
            }
        }
        setState(false, false);
	}

	// MouseMotionListener interface
	public void mouseDragged( MouseEvent e ) {
	}
	public void mouseMoved( MouseEvent e ) {
	    tooltip.reactivate(this);
	}
}

