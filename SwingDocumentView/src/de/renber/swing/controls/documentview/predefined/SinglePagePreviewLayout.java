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
 * Shows a single page only
 *
 * @author berre
 */
public class SinglePagePreviewLayout implements PreviewLayout {

    // horizontal spacing between pages (at 100 %)
    int m_horizontalSpacing = 20;
    // vertical spacing between pages (at 100 %)
    int m_verticalSpacing = 40;
    int currentPage = 0;       

    @Override
    public Dimension getNeededSpace(float zoomLevel, Dimension[] pageSizes) {
    	
    	if (pageSizes.length == 0)
    		return new Dimension(0, 0);
    	
        Dimension[] d = getPageSizes(zoomLevel, pageSizes);
        return new Dimension(2*getZoomedHorizontalSpacing(zoomLevel) + d[currentPage].width, 2*getZoomedVerticalSpacing(zoomLevel) + d[currentPage].height);
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
        if (currentPage < pages.size()) {
            visPages.add(new PageVisibility(currentPage, 1.0f));
        }
        return visPages;
    }

    @Override
    public void draw(Graphics2D g, Color backgroundColor, Rectangle targetRect, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages, PageAdorner adorner) {
        pageSizes = getPageSizes(zoomLevel, pageSizes);
        Dimension pageSize = pageSizes[currentPage];

        if (currentPage < pages.size()) {
            Page page = pages.get(currentPage);

            int px;
            if (pageSize.width + 10 <= targetRect.width) {
                px = (targetRect.width - pageSize.width) / 2; // center page horizontally (enough room)
            } else {
                // scroll page horizontally
                px = getZoomedHorizontalSpacing(zoomLevel) - scrollPosition.x;
            }
            
            int py;
            if (pageSize.height + 10 <= targetRect.height) {
                py = (targetRect.height - pageSize.height) / 2; // center page vertically (enough room)
            } else {
                // scroll page horizontally
                py = getZoomedVerticalSpacing(zoomLevel) - scrollPosition.y;
            }            
            
            if (adorner != null) {
                adorner.drawPrePage(g, backgroundColor, currentPage+1, zoomLevel, px, py, pageSize.width, pageSize.height);
            }
            // fill page with white
            g.setColor(Color.WHITE);
            g.fillRect(px, py, pageSize.width, pageSize.height);
            page.draw(g, px, py, pageSize.width, pageSize.height);
            if (adorner != null) {
                adorner.drawPostPage(g, backgroundColor, currentPage+1, zoomLevel, px, py, pageSize.width, pageSize.height);
            }
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
        currentPage = pageIndex;
        return new Point(0, 0);
    }   
    
    @Override
    public Dimension getViewElementSize(float zoomLevel, int pageIndex, Dimension[] pageSizes) {
    	Dimension[] scaled = getPageSizes(zoomLevel, pageSizes);
    	
        return new Dimension((int)(m_horizontalSpacing + scaled[pageIndex].getWidth()), (int)(m_verticalSpacing + scaled[pageIndex].getHeight()));
    }
    
    @Override
    public int getViewElementCount(int numberOfPages) {
    	return numberOfPages;
    }
    
    @Override
    public int getViewElementIndex(int pageIndex) {
    	return pageIndex / 2 * 2;
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
