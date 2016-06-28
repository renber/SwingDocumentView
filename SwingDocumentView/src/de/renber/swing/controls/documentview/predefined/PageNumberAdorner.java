/*******************************************************************************
 * This file is part of the Java SwingPrintPreview Library
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 René Bergelt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package de.renber.swing.controls.documentview.predefined;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.renber.swing.controls.documentview.types.PageAdorner;

/**
 * PageAdorner which adds the page number under each page view
 *
 * @author berre
 */
public class PageNumberAdorner implements PageAdorner {

    Font font;
    Color fontColor;
    int yOffset;    
    String displayText;
    
    /**
     * Creates a page adorner with the default y offset of 5 and the
     * default display text
     * @param font Font to use for page number
     * @param fontColor Color to use for the page number
     */
    public PageNumberAdorner(Font font, Color fontColor) {
        this(font, fontColor, 5, "Page %d");     
    }
    
    /**
    * Creates a page adorner
    * @param font Font to use for page number
    * @param fontColor Color to use for the page number
    * @param yOffset Distance to the bottom of the page
    * @param displayText Mask for the page number text    
    */
   public PageNumberAdorner(Font font, Color fontColor, int yOffset, String displayText) {
       this.font = font;
       this.fontColor = fontColor;
       this.yOffset = yOffset;
       this.displayText = displayText;
   }
    
    /**
     * Return the mask for the page number text     
     */
    public String getDisplayText() {
    	return displayText;
    }
    
    /**
     * Set the mask for the page number text    
     * (%d will be replaced with the actual page number; default is "Page %d") 
     */
    public void setDisplayText(String displayText) {
    	if (displayText == null)
    		throw new IllegalArgumentException("displayText");
    	this.displayText = displayText;
    }

    @Override
    public void drawPostPage(Graphics2D g, Color backgroundColor, int pageNumber, float zoomLevel, int x, int y, int w, int h) {
        Graphics2D g2 = null;
        try {
            g2 = (Graphics2D) g.create(); // push graphic settings

            // add page number            
            g2.setColor(fontColor);            
            Font zoomedFont = font.deriveFont(font.getSize() * zoomLevel);
            
            String s = String.format(displayText, pageNumber);
            FontMetrics metrics = g2.getFontMetrics(zoomedFont);            
            Rectangle2D strRect = metrics.getStringBounds(s, g2);
            g2.setFont(zoomedFont);            
            g2.drawString(s, (int)(x + w - strRect.getWidth()), (int)(y + h + yOffset + strRect.getHeight()));            
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }

    }

	@Override
	public void drawPrePage(Graphics2D g, Color backgroundColor, int pageNumber, float zoomLevel, int x, int y, int w,
			int h) {
		// --
	}
}
