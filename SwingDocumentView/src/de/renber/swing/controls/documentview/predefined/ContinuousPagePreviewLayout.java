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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.renber.swing.controls.documentview.types.Page;
import de.renber.swing.controls.documentview.types.PageAdorner;
import de.renber.swing.controls.documentview.types.PageVisibility;
import de.renber.swing.controls.documentview.types.PreviewLayout;

/**
 * PreviewLayout to view all pages continuously
 * @author René Bergelt
 */
public class ContinuousPagePreviewLayout implements PreviewLayout {

    // horizontal spacing between pages (at 100 %)
    int m_horizontalSpacing = 20;
    // vertical spacing between pages (at 100 %)
    int m_verticalSpacing = 40;    

    @Override
    public Dimension getNeededSpace(float zoomLevel, Dimension[] pageSizes) {
    	if (pageSizes.length == 0)
    		return new Dimension(0, 0);
    	
    	float maxWidth = 0;
    	int pageHeights = 0;
    	
    	pageSizes = getPageSizes(zoomLevel, pageSizes);
    	
    	// calculate max width and the height of all pages
    	for(int i = 0; i < pageSizes.length; i++) {
    		if (pageSizes[i].width > maxWidth)
    			maxWidth = pageSizes[i].width ;
    		
    		pageHeights += pageSizes[i].height + getZoomedVerticalSpacing(zoomLevel);
    	}
    	
        int neededWidth = (int) (2 * getZoomedHorizontalSpacing(zoomLevel) + maxWidth);
        int neededHeight = (int) (getZoomedVerticalSpacing(zoomLevel) + pageHeights);

        return new Dimension(neededWidth, neededHeight);
    }

    @Override
    public Dimension[] getPageSizes(float zoomLevel, Dimension[] pageSizes) {
    	// calculate scaled page sizes
    	Dimension[] scaled = Arrays.copyOf(pageSizes, pageSizes.length);
    	for(int i = 0; i < pageSizes.length; i++) {
    		scaled[i] = new Dimension((int) (pageSizes[i].width * zoomLevel), (int) (pageSizes[i].height * zoomLevel)); 
    	}    
    	return scaled;
    }

    @Override
    public List<PageVisibility> getVisiblePages(Dimension viewPortSize, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages) {    	    	
    	List<PageVisibility> visPages = new ArrayList<PageVisibility>();
    	
    	if (pages.size() == 0)
    		return visPages;

        pageSizes = getPageSizes(zoomLevel, pageSizes);
        TopPageInfo tp = getTopPage(getZoomedVerticalSpacing(zoomLevel) - scrollPosition.y, getZoomedVerticalSpacing(zoomLevel), pageSizes);      

        int p = tp.topPage;
        int py = tp.posY;
        
        while (p < pages.size() && py < viewPortSize.height) {        	
        	// calculate the visibility of the actual page content
        	// (without adorners and spacing)
        	float visibleHeight = Math.min(py + pageSizes[p].height, viewPortSize.height) - Math.max(0, py);        	
        	if (visibleHeight > 0)
        		visPages.add(new PageVisibility(p, visibleHeight / (float)pageSizes[p].height ));

            py += pageSizes[p].height + getZoomedVerticalSpacing(zoomLevel);
            p++;
        }

        return visPages;
    }
    
    /**
     * Return the largest width from the given page sizes array     
     */
    private static int getMaxWidth(Dimension[] pageSizes) {
    	int maxWidth = 0; 
    	for(int i = 0; i < pageSizes.length; i++) {
    		if (pageSizes[i].width > maxWidth)
    			maxWidth = pageSizes[i].width;
    	}
    	return maxWidth;
    }
    
    /**
     * Return the index of the first page which is visible, when drawing begins at startY (may be negative if scrolled)
     * and its start y position (may be negative)
     */
    private TopPageInfo getTopPage(int startY, int verticalSpacing, Dimension[] pageSizes) {
    	TopPageInfo tp = new TopPageInfo();
    	tp.posY = startY;
        tp.topPage = 0;
        while (tp.posY + pageSizes[tp.topPage].height + verticalSpacing < 0) {            
        	tp.posY += pageSizes[tp.topPage].height + verticalSpacing;
        	tp.topPage++;
        }
        return tp;
    }
    
    private int getXCenter(Rectangle targetRect, int startx, int width) {    	
    	if (width + 10 <= targetRect.width) {
        	// center pages horizontally (enough room)
    		return targetRect.x + targetRect.width / 2;
        } else {
            // scroll pages horizontally
        	return targetRect.x + startx + width / 2;
        }    	
    }

    @Override
    public void draw(Graphics2D g, Color backgroundColor, Rectangle targetRect, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages, PageAdorner adorner) {
        // get the actual (zoomed) page sizes
    	pageSizes = getPageSizes(zoomLevel, pageSizes);

        // get the starting page and x and y coordinates
        int maxWidth = getMaxWidth(pageSizes);    	    	    	
        int px_center = getXCenter(targetRect, getZoomedHorizontalSpacing(zoomLevel) - scrollPosition.x, maxWidth);        
        TopPageInfo tp = getTopPage(getZoomedVerticalSpacing(zoomLevel) - scrollPosition.y, getZoomedVerticalSpacing(zoomLevel), pageSizes);

        // draw the page previews   
        int p = tp.topPage;
        int py = tp.posY;
        
        while (p < pages.size() && py < targetRect.y + targetRect.height) {
            Page page = pages.get(p);

            if (adorner != null) {
                adorner.drawPrePage(g, backgroundColor, p + 1, zoomLevel, px_center - pageSizes[p].width / 2, py, pageSizes[p].width, pageSizes[p].height);
            }

            // fill page with white
            g.setColor(Color.WHITE);
            g.fillRect(px_center - pageSizes[p].width / 2, py, pageSizes[p].width, pageSizes[p].height);            
            page.draw(g, px_center - pageSizes[p].width / 2, py, pageSizes[p].width, pageSizes[p].height);
            
            if (adorner != null) {
                adorner.drawPostPage(g, backgroundColor, p + 1, zoomLevel, px_center - pageSizes[p].width / 2, py, pageSizes[p].width, pageSizes[p].height);
            }

            py += pageSizes[p].height + getZoomedVerticalSpacing(zoomLevel);
            p++;
        }
    }

    /**
     * Returns the vertical spacing for the current zoom level
     *
     * @return
     */
    private int getZoomedVerticalSpacing(float zoomLevel) {
        return (int) (m_verticalSpacing * zoomLevel);
    }

    /**
     * Returns the horizontal spacing for the current zoom level
     *
     * @return
     */
    private int getZoomedHorizontalSpacing(float zoomLevel) {
        return (int) (m_horizontalSpacing * zoomLevel);
    }

    @Override
    public Point ensureVisible(Dimension viewPortSize, float zoomLevel, Dimension[] pageSizes, int pageIndex) {
        Dimension[] scaled = getPageSizes(zoomLevel, pageSizes);    	
    	int sx = 0;
        
        int top = 0;
        for(int i = 0; i < pageIndex; i++) {
        	top += scaled[i].height + getZoomedVerticalSpacing(zoomLevel);
        }        
        int sy = getZoomedVerticalSpacing(zoomLevel) + top;
        return new Point(sx, sy);
    }

    @Override
    public Dimension getViewElementSize(float zoomLevel, int pageIndex, Dimension[] pageSizes) {    	
    	Dimension[] scaled = getPageSizes(zoomLevel, pageSizes);    	    	    	
        return new Dimension((int)(m_horizontalSpacing + scaled[pageIndex].width), (int)(m_verticalSpacing + scaled[pageIndex].height));
    }     
    
    @Override
    public int getViewElementCount(int numberOfPages) {
    	return numberOfPages;
    }
    
    @Override
    public int getViewElementIndex(int pageIndex) {
    	return pageIndex;
    }
    
    @Override
    public int getPageIndexOfNextViewElement(int pageIndex) {
    	return pageIndex + 1;
    }
    
    @Override
    public int getPageIndexOfPreviousViewElement(int pageIndex) {
   	 return Math.max(0, pageIndex - 1);
    }    
}

class TopPageInfo {
	int topPage = 0;
	int posY = 0;
}
