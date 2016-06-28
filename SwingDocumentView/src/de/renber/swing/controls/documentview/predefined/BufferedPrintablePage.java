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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.renber.swing.controls.documentview.types.GraphicsUtilities;
import de.renber.swing.controls.documentview.types.Page;

/**
 * A page implementation which uses an internal image buffer
 * @author renber
 */
public class BufferedPrintablePage implements Page {

	final static int DPI = 96;
	
    BufferedImage scaledImg = null;
    Dimension scaleDimension = new Dimension(0, 0);
    Printable printable;
    PageFormat pageFormat;
    int pageIndex;
    Dimension pageSize;
    String renderingText = "Rendering preview...";
    
    @Override
    public void draw(Graphics2D g, int x, int y, int w, int h) {
        if (scaledImg != null) {            
                g.drawImage(scaledImg, x, y, w, h, null);            
        } else {
        	// print "rendering"
        	Color oldColor = g.getColor();
        	g.setColor(Color.black);        	
        	if (g.getFontMetrics().stringWidth(renderingText) < w - 10) {
        		g.drawString(renderingText, x + 5, y + 15);	
        	}                	        	
        	g.setColor(oldColor);
        }
    }

    @Override
    public boolean isScaled(int w, int h) {
        return scaleDimension.width == w && scaleDimension.height == h;
    }        
    
    @Override
    public void hiQualityScale(final int w, final int h) {     
    	
    	if (isScaled(w, h))
    		return;
    	
    	scaleDimension.width = w;
        scaleDimension.height = h;
    	
        Graphics g = null;
        
    	try {
    		int pw = (int)pageFormat.getWidth();
    		int ph = (int)pageFormat.getHeight();    		
    		
    		BufferedImage tmpBuffer = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_RGB);
            g = tmpBuffer.getGraphics();
            
            // make sure that the page background is white
            // but restore the original color afterwards
            Color oldColor = g.getColor();         
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, pw, ph);                              
            printable.print(g, pageFormat, pageIndex);            
            g.setColor(oldColor);            
            
            Image buf = tmpBuffer.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH);

    		// the scaled instance will be evaluated on the first drawing attempt so
    		// do this here
    		BufferedImage scaledBuf = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    		Graphics gbuf = null;
    		try {
    			gbuf = scaledBuf.createGraphics();
    			gbuf.drawImage(buf, 0, 0, null);
    		} finally {
    			if (gbuf != null) {
    				gbuf.dispose();
    			}
    		}
    		
    		scaledImg = scaledBuf;
        } catch (PrinterException ex) {
        	
        	if (g != null)
        		g.dispose();
        	
            Logger.getLogger(BufferedPrintablePage.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public BufferedPrintablePage(Printable printable, int pageIndex, PageFormat pageFormat) {
                       
        this.printable = printable;  
        this.pageIndex = pageIndex;
        this.pageFormat = pageFormat;
        
        // convert the 72 dpi based dimensions to screen dpi    	
        pageSize = new Dimension((int)(pageFormat.getWidth() / 72.0f * DPI), (int)(pageFormat.getHeight() / 72.0f * DPI));
    }

    @Override
    public void freeResources() {
        if(scaledImg != null) {
            scaledImg.flush();
        }
    }

	@Override
	public Dimension getPageSize() {
		return pageSize;
	}
}
