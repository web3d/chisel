package com.trapezium.chisel.gui;
import com.trapezium.chisel.DialogOwner;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

/** generic dialog box; subclass to turn into a usable dialog */
public class BaseDialog extends Dialog implements WindowListener, ActionListener, KeyListener
{
    protected Panel controlPanel = null;
	protected Button okButton, cancelButton;
	protected ResourceBundle strings;
    protected boolean initialized = false;
    protected DialogOwner owner;
    boolean cancelled = false;

	protected BaseDialog(DialogOwner owner, String name) {
		super(owner.getFrame(), owner.getResourceBundle().getString(name), true);

        this.owner = owner;

        strings = owner.getResourceBundle();
		controlPanel = new ControlPanel();
    	setLayout(new BorderLayout());

		okButton = new Button(strings.getString("okLabel"));
		okButton.addActionListener(this);
		cancelButton = new Button(strings.getString("cancelLabel"));
		cancelButton.addActionListener(this);

		controlPanel.add(okButton);
		controlPanel.add(cancelButton);
		controlPanel.show();
		add("South", controlPanel);

		addWindowListener(this);
		addKeyListener(this);
	}
	
	public void show() {
        
        // otherwise layout calculations don't work
        addNotify();

		Dimension size = getPreferredSize();
		Insets insets = getInsets();
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		setSize(size);
		setLocation(200, 200);
        super.show();
    }
    

	//public void setLayout(LayoutManager lm) {
	//    if (controlPanel != null) {
    //	    controlPanel.setLayout(lm);
    //	}
	//}

	/***public void setVisible(boolean visible) {
	    if (visible) {
            Dimension size = getPreferredSize();
        	setSize(size);
        	setLocation(200, 200);
        	System.out.println("BD bounds " + getBounds());
        }
        super.setVisible(visible);
    }***/
/********
    protected void addImpl(Component c, Object constraints, int index) {
        if (c == controlPanel || c == okButton || c == cancelButton) {
            super.addImpl(c, constraints, index);
        } else if (controlPanel != null) {
            controlPanel.add(c, constraints, index);
        }
    }

    public void remove(Component comp) {
        if (comp != controlPanel && comp != okButton && comp != cancelButton) {
            controlPanel.remove(comp);
        } else {
            super.remove(comp);
        }
    }

    public int getComponentCount() {
        return ( controlPanel == null ? 0 : controlPanel.getComponentCount() );
    }

    public Component getComponent(int n) {
        return ( controlPanel == null ? null : controlPanel.getComponent(n) );
    }

************/

    static final int hgap = 24;
    static final int vgap = 14;
    static final int hbuttonmargin = 32;
    static final int vbuttonmargin = 4;
    public class ControlPanel extends Panel {
        public ControlPanel() {
            super();
            setLayout(null);
        }

        /** determine the minimum size */
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }


        /** determine the preferred size */
        public Dimension getPreferredSize() {
            Dimension bsize = getButtonSize();
            Dimension pref = new Dimension(3 * hgap + 2 * bsize.width, bsize.height + 2 * vgap);

            Insets insets = getInsets();
            pref.width += insets.left + insets.right;
            pref.height += insets.top + insets.bottom;

            return pref;
        }


        /** lay out the pane. */

        public void doLayout() {

            Dimension size = getSize();
            Insets insets = getInsets();
            size.width -= insets.left + insets.right;
            size.height -= insets.top + insets.bottom;

            Dimension bsize = getButtonSize();
            int xcenter = size.width / 2;
            int ybuttons = insets.top + size.height - vgap - bsize.height;

            okButton.setBounds( xcenter - bsize.width - hgap/2, ybuttons, bsize.width, bsize.height );
            cancelButton.setBounds( xcenter + hgap/2, ybuttons, bsize.width, bsize.height );
            //controlPanel.setBounds( hgap + insets.left, vgap + insets.top, size.width - 2 * hgap, ybuttons - 2 * vgap - insets.top);
        }

        private Dimension getButtonSize() {
            Font font = okButton.getFont();
            FontMetrics fm = getFontMetrics(font);
            Dimension bsize = okButton.getMinimumSize();

            bsize.width = Math.max(fm.stringWidth(okButton.getLabel()), fm.stringWidth(cancelButton.getLabel())) + 2 * hbuttonmargin;
            bsize.height = fm.getHeight() + 2 * vbuttonmargin;
            return bsize;
        }
    }        

	public void actionPerformed(ActionEvent evt) {
	    cancelled = false;
        if (evt.getSource() == okButton)	{
			execute();
		} else if ( evt.getSource() == cancelButton )  {
		    cancelled = true;
		}
			
		if (!keepUp) {
    		dispose();
        } else {
            keepUp = false;
        }
	}

    private boolean keepUp = false;
    protected void dontDismiss() {
        keepUp = true;
    }
	
	protected void execute() {
	}

    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowClosing(WindowEvent event) {
			dispose();
    }

    public boolean wasCancelled() {
        return( cancelled );
    }
    

	public void keyTyped( KeyEvent e ) {}
	public void keyReleased( KeyEvent e ) {}
	public void keyPressed( KeyEvent e ) {
		if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
			execute();
    		if (!keepUp) {
        		dispose();
            } else {
                keepUp = false;
            }
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
			dispose();
		}
	}
}
