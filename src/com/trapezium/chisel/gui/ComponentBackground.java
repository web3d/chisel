/*
 * ComponentBackground.java
 *
 * A generalized background painter components.
 */


package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.Vector;
import java.net.URL;


public class ComponentBackground implements Runnable, DisplayConstants, ImageObserver {

    Component component;

	MediaTracker tracker = null;
	Thread loader = null;
	protected boolean finishedLoading = false;

    Color color = Color.lightGray;

    Image decalImage = null;
    boolean decalImageError = false;
    int decalX = 0;
    int decalY = 0;
    int decalWidth = -1;
    int decalHeight = -1;
	boolean decalVertCentered = true;
	boolean decalHorzCentered = true;
    Image tileImage = null;
    boolean tileImageError = false;
    int tileX = 0;
    int tileY = 0;
    int tileWidth = -1;
    int tileHeight = -1;

    private int nextID = 0;

    /** create a display properties holder  */
    public ComponentBackground(Component component) {

        this.component = component;

		// prepare for possible image download
        tracker = new MediaTracker(component);
		tileImage = getImage(null, DEFAULT_TILE);
		if (tileImage != null) {
			tileX = DEFAULT_TILEX;
			tileY = DEFAULT_TILEY;
		}
		decalImage = getImage(null, DEFAULT_DECAL);
    }


	// draw a background using these properties

    void drawBackground(Graphics g, Dimension size, Rectangle clip) {


        /* If there is no background image, or it hasn't started downloading,
        // fill the applet with the background color.  Otherwise fill the
        // area by tiling the background image.
        */
		if (tileImage == null) {

            g.setColor(color);
            g.fillRect(clip.x, clip.y, clip.width, clip.height);

        } else {

            /* Tile the background image.  First calculate the starting
            // offset, which should be zero or negative for complete
            // coverage of the exposed background.  Then draw the image
            // row by row, column by column until the background is filled
            */

			int width = tileWidth;
			int height = tileHeight;
			if  (!tileImageError && width > 0 && height > 0) {
				Image tile = tileImage;

	            // starting x coordinate for the tiling
	            int startx = tileX % width;
	            if (startx > 0) {
	                startx -= width;
	            }

	            // starting y coordinate for the tiling
	            int y = tileY % height;
	            if (y > 0) {
	                y -= height;
	            }

	            // loop by row until out of view
	            while (y < clip.y + clip.height) {

	                // only draw if in view
	                if (y + height >= clip.y) {

	                    // loop by column
	                    for (int x = startx; x < clip.x + clip.width; x += width)  {

	                        // only draw the image if in view
	                        if (x + width >= clip.x) {

	                            // draw
	                            g.drawImage(tile, x, y, this);
	                        }
	                    }
	                }
	                y += height;
	            }
			}
        }
        if (decalImage != null && !decalImageError && decalWidth > 0 && decalHeight > 0) {
            int x = (decalHorzCentered ? (size.width - decalWidth) / 2 : decalX);
            int y = (decalVertCentered ? (size.height - decalHeight) / 2 : decalY);
			if (x + decalWidth > clip.x && x < clip.x + clip.width && y + decalHeight > clip.y && y < clip.y + clip.height) {
            	g.drawImage(decalImage, x, y, this);
            }
        }
    }

    // reinitialize image parameters after image download is complete
    public void setImageDims() {

   		synchronized (tileImage) {
	    	if (tileImage != null) {
	    		tileWidth = tileImage.getWidth(this);
	    		tileHeight = tileImage.getHeight(this);
	    	}
	   	}
   		synchronized (decalImage) {
	    	if (decalImage != null) {
    			decalWidth = decalImage.getWidth(this);
    			decalHeight = decalImage.getHeight(this);
    		}
    	}
    }

    // called when an error occurs downloading an image
    public void imageError(Image image) {
    	if (image == tileImage) {
    		System.err.println("Error loading tile image");
    		tileImageError = true;
    		tileImage.flush();
    	}
    	if (image == decalImage) {
    		System.err.println("Error loading decal image");
    		decalImageError = true;
    		decalImage.flush();
    	}
    }

    /** get an image using MediaTracker and a specified tracker ID */
    public Image getImage(URL base, String imgName) {
        if (imgName != null) {
            try {
				Image img;
				if (base == null) {
					img = Toolkit.getDefaultToolkit().getImage(imgName);
					//if (img != null) {
					//	dbg("about to prepareImage");
					//	prepareImage(img, -1, -1, this);
					//	dbg("Image prepared.");
					//	return img;
					//}

				} else {
			        URL url = new URL(base, imgName);
		        	img = Toolkit.getDefaultToolkit().getImage(url);
				}
				if (img != null) {
	                tracker.addImage(img, nextID++);

					/** wake up the image loader thread if it exists yet */
					if (loader != null) {
						finishedLoading = false;
						loader.resume();
					}
	                return img;
	            }

            } catch (Exception e) {
                System.err.println("Error getting background image " + imgName + ": " + e);
            }
        }
        return null;    // if we got here, something is wrong
    }


	public void start() {
        if (loader == null) {
            loader = new Thread(this);
            loader.setPriority(Thread.MAX_PRIORITY - 1);
        }
        loader.start();
    }

    public void stop() {
        if (loader != null) {
            loader.stop();
            loader = null;
        }
    }

	public void loadImages() {
		if (!tracker.checkAll()) {
			try {
				tracker.waitForAll();
			} catch (InterruptedException e) {
				System.err.println("Image download interrupted");
				return;
			}

			setImageDims();

			Object[] errObj = tracker.getErrorsAny();
			if (errObj != null) {
				for (int i = 0; i < errObj.length; i++) {
					if (errObj[i] instanceof Image) {
						imageError((Image) errObj[i]);
					}
				}
			}
		}

		finishedLoading = true;
	}

	//
	// Runnable interface
	//

    public void run() {
        while (Thread.currentThread() == loader) {
			loadImages();
			loader.suspend();
        }
    }


	//
	// ImageObserver interface
	//

    /**
     * Repaints the component when the image has changed.
     * @return true if image has changed; false otherwise.
     */
    public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
		return (flags & (ALLBITS|ABORT)) == 0;
    }
}

