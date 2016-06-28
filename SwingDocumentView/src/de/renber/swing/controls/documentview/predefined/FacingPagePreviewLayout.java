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
 * Shows two facing pages at a time
 *
 * @author berre
 */
public class FacingPagePreviewLayout implements PreviewLayout {

    // horizontal spacing between pages (at 100 %)
    int m_horizontalSpacing = 20;
    // vertical spacing between pages (at 100 %)
    int m_verticalSpacing = 40;
    int currentPage = 0; // the currently visible 'left' page    

    @Override
    public Dimension getNeededSpace(float zoomLevel, Dimension[] pageSizes) {    	
    	if (pageSizes.length == 0)
    		return new Dimension(0, 0);
    	
        Dimension[] scaled = getPageSizes(zoomLevel, pageSizes);
        
        if (currentPage == pageSizes.length - 1) {
        	return new Dimension(2 * getZoomedHorizontalSpacing(zoomLevel) + 2 * scaled[currentPage].width, 2 * getZoomedVerticalSpacing(zoomLevel) + scaled[currentPage].height);	
        } else        
        	return new Dimension((int)(2.5 * getZoomedHorizontalSpacing(zoomLevel) + scaled[currentPage].width + scaled[currentPage+1].width), 2 * getZoomedVerticalSpacing(zoomLevel) + scaled[currentPage].height + scaled[currentPage + 1].height);
    }

    @Override
    public Dimension[] getPageSizes(float zoomLevel, Dimension[] pageSizes) {
    	Dimension[] scaled = Arrays.copyOf(pageSizes, pageSizes.length);
    	for(int i = 0; i < pageSizes.length; i++) {
    		scaled[i] = new Dimension((int) (pageSizes[i].width * zoomLevel), (int) (pageSizes[i].height * zoomLevel)); 
    	}    
    	return scaled;
    }

    @Override
    public List<PageVisibility> getVisiblePages(Dimension viewPortSize, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages) {
    	List<PageVisibility> visPages = new ArrayList<PageVisibility>(1);
    	
        // 'left' page
        if (currentPage < pages.size()) {
            visPages.add(new PageVisibility(currentPage, 1.0f));
        }
        // 'right' page
        if (currentPage + 1 < pages.size()) {
            visPages.add(new PageVisibility(currentPage + 1, 1.0f));
        }

        return visPages;
    }

    @Override
    public void draw(Graphics2D g, Color backgroundColor, Rectangle targetRect, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages, PageAdorner adorner) {
        Dimension[] scaled = getPageSizes(zoomLevel, pageSizes);
        
        int twoPagesWidth;
        if (currentPage == pages.size() - 1)
        	// only one page left, display it on the left side
        	twoPagesWidth = 2 * (scaled[currentPage].width + getZoomedHorizontalSpacing(zoomLevel));
        else
        	twoPagesWidth = (scaled[currentPage].width + scaled[currentPage+1].width + 2*getZoomedHorizontalSpacing(zoomLevel));
        
        int twoPagesHeight;
        if (currentPage == pages.size() - 1)
        	// only one page left, display it on the left side
        	twoPagesHeight = scaled[currentPage].height;
        else
        	twoPagesHeight = Math.max(scaled[currentPage].height, scaled[currentPage+1].height);

        if (currentPage < pages.size()) {
            int px;
            if (twoPagesWidth + 10 <= targetRect.width) {
                px = (targetRect.width - twoPagesWidth) / 2; // center page horizontally (enough room)
            } else {
                // scroll page horizontally
                px = getZoomedHorizontalSpacing(zoomLevel) - scrollPosition.x;
            }

            int py_center;
            if (twoPagesHeight + 10 <= targetRect.height) {
            	// center pages vertically (enough room)
            	py_center = targetRect.y + targetRect.height / 2;
            } else {
                // scroll page horizontally
            	py_center = getZoomedVerticalSpacing(zoomLevel) - scrollPosition.y + twoPagesHeight / 2;
            }

            // draw both pages            
            drawPage(g, backgroundColor, currentPage + 1, zoomLevel, px, py_center - scaled[currentPage].height / 2, scaled[currentPage], pages.get(currentPage), adorner);

            if (currentPage + 1 < pages.size()) {
                drawPage(g, backgroundColor, currentPage + 2, zoomLevel, px + scaled[currentPage].width + getZoomedHorizontalSpacing(zoomLevel), py_center - scaled[currentPage+1].height / 2, scaled[currentPage+1], pages.get(currentPage + 1), adorner);
            }
        }
    }

    void drawPage(Graphics2D g, Color backgroundColor, int pageNum, float zoomLevel, int x, int y, Dimension pageSize, Page page, PageAdorner adorner) {
        if (adorner != null) {
            adorner.drawPrePage(g, backgroundColor, pageNum, zoomLevel, x, y, pageSize.width, pageSize.height);
        }
        // fill page with white
        g.setColor(Color.WHITE);
        g.fillRect(x, y, pageSize.width, pageSize.height);
        page.draw(g, x, y, pageSize.width, pageSize.height);
        if (adorner != null) {
            adorner.drawPostPage(g, backgroundColor, pageNum, zoomLevel, x, y, pageSize.width, pageSize.height);
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
        currentPage = (pageIndex / 2) * 2; // jump two pages
        int sx = (int)((pageIndex % 2) * pageSizes[currentPage].width * zoomLevel + getZoomedHorizontalSpacing(zoomLevel));
        return new Point(sx, 0);
    }
    
     @Override
     public Dimension getViewElementSize(float zoomLevel, int pageIndex, Dimension[] pageSizes) {
    	 if (pageSizes.length == 0)
    		 return new Dimension(0, 0);
    	 
        pageSizes = getPageSizes(zoomLevel, pageSizes);
        
        // get the left page
        pageIndex = 2 * pageIndex / 2;
        
        if (pageIndex == pageSizes.length - 1)
        	return new Dimension(2 * (pageSizes[pageIndex].width + getZoomedHorizontalSpacing(zoomLevel)), 2*getZoomedVerticalSpacing(zoomLevel) + pageSizes[pageIndex].height);
        else
        {
        	int height = Math.max(pageSizes[pageIndex].height, pageSizes[pageIndex+1].height);        	
        	return new Dimension((pageSizes[pageIndex].width + pageSizes[pageIndex+1].width + 2 * getZoomedHorizontalSpacing(zoomLevel)), 2*getZoomedVerticalSpacing(zoomLevel) + height);
        }
    }
     
     @Override
     public int getViewElementCount(int numberOfPages) {
     	return numberOfPages / 2 + numberOfPages % 2;
     }
     
     @Override
     public int getViewElementIndex(int pageIndex) {
     	return pageIndex / 2 + pageIndex % 2;
     }
     
     @Override
     public int getPageIndexOfNextViewElement(int pageIndex) {
     	return pageIndex / 2 * 2 + 2;
     }
     
     @Override
     public int getPageIndexOfPreviousViewElement(int pageIndex) {
    	 return Math.max(0, pageIndex / 2 * 2 - 2);
     }
}
