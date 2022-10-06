package com.trapezium.chisel.gui;
import com.trapezium.chisel.DialogOwner;

import java.awt.*;
import java.awt.event.*;

public class OneButtonDialog extends Dialog implements WindowListener, ActionListener
{
	Button button;

	public OneButtonDialog(DialogOwner owner, String title, String buttonLabel, boolean modal) {
		super(owner.getFrame(), title, modal);

		setLayout(null);

		button = new Button(buttonLabel);
		button.addActionListener(this);
		add(button);

		addWindowListener(this);
	}

    static final int vgap = 8;
    static final int hbuttonmargin = 32;
    static final int vbuttonmargin = 4;

    public void doLayout() {
        // buttons may cache their size return values rather than allocate a new 
        // object.  Since getPreferredSize() ultimately returns this same object
        // (altered) store the appropriate information in local variables.
        
        Dimension bsize = getButtonSize();
        int bh = bsize.height;
        int bw = bsize.width;
		
        // don't access bsize directly after calling getPreferredSize()
		Dimension size = getPreferredSize();

		Container parent = getParent();
        Point pt = parent.getLocationOnScreen();
        Dimension dim = parent.getSize();
        int x = pt.x + (dim.width - size.width)/2;
        int y = pt.y + (dim.height - size.height)/2;
        if (x < 0 && y < 0) {
            x = 100;
            y = 100;
            if (pt.x > 0) x += pt.x;
            if (pt.y > 0) y += pt.y;
        }
        
        setBounds(x, y, size.width, size.height);

		Insets insets = getInsets();
        int xcenter = size.width / 2;
        int ybutton = size.height - insets.bottom - vgap - bh;
        if (getComponentCount() > 1) {
            getComponent(1).setBounds(insets.left, insets.top, size.width - insets.left - insets.right, ybutton - insets.top);
        }
        button.setBounds( xcenter - bw/2, ybutton, bw, bh );
    }

    /** determine the minimum size */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }


    /** determine the preferred size */
    public Dimension getPreferredSize() {

        Dimension pref;
        if (getComponentCount() > 1) {
            pref = getComponent(1).getPreferredSize();
        } else {
            pref = new Dimension(0, 0);
        }

        Dimension bsize = getButtonSize();
		Insets insets = getInsets();

        pref.width = Math.max(pref.width, bsize.width + 2 * hbuttonmargin);
        pref.width += insets.left + insets.right;
        pref.height += bsize.height + insets.top + insets.bottom + vgap;

        return pref;
    }

    private Dimension getButtonSize() {

        Font font = button.getFont();
        FontMetrics fm = getFontMetrics(font);
        Dimension bsize = button.getMinimumSize();

        bsize.width = fm.stringWidth(button.getLabel()) + 2 * hbuttonmargin;
        bsize.height = fm.getHeight() + 2 * vbuttonmargin;
        return bsize;
    }

	public void actionPerformed(ActionEvent evt) {
		dispose();
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
}
