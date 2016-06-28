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
 *
 * @author berre
 */
public class ContinuousFacingPagePreviewLayout implements PreviewLayout {
    // horizontal spacing between pages (at 100 %)
    int m_horizontalSpacing = 20;
    // vertical spacing between pages (at 100 %)
    int m_verticalSpacing = 40;    

    @Override
    public Dimension getNeededSpace(float zoomLevel, Dimension[] pageSizes) {
    	
    	if (pageSizes.length == 0)
    		return new Dimension(0, 0);
    	
    	int maxWidth = 0;
    	int pageHeights = 0;
    	
    	pageSizes = getPageSizes(zoomLevel, pageSizes);
    	
    	// calculate max width and the height of all pages (in groups of 2)
    	for(int i = 0; i < pageSizes.length; i+=2) {
    		// get width and height of the current row
    		int localWidth;
    		int localHeight;
    		
    		if (i == pageSizes.length - 1) {
    			// last page is alone
    			localWidth = pageSizes[i].width;
    			localHeight = pageSizes[i].height;
    		} else {
    			localWidth = pageSizes[i].width + pageSizes[i+1].width;
    			localHeight = Math.max(pageSizes[i].height, pageSizes[i+1].height);
    		}    	
    		        	
        	if (localWidth > maxWidth)
    			maxWidth = localWidth;
    		
        	pageHeights += localHeight + getZoomedVerticalSpacing(zoomLevel);
    	}
    	
        int neededWidth = (int) (3 * getZoomedHorizontalSpacing(zoomLevel) + maxWidth);
        int neededHeight = (int) (3*getZoomedVerticalSpacing(zoomLevel) + pageHeights);

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
    
    /**
     * Return the maximum width of a row (for left and right pages) 
     */
    private static RowWidth getMaxWidth(Dimension[] pageSizes) {
    	
    	// we have to determine max left and max right separately
    	// because we want to align all pages at the (virtual) middle
    	
    	RowWidth maxWidth = new RowWidth();
        
        for(int i = 0; i < pageSizes.length; i+=2) {
    		// get width and height of the current row
        	int leftWidth = pageSizes[i].width;
        	int rightWidth = 0;    		
    		
    		if (i < pageSizes.length - 1) {
    			// not the last page
    			rightWidth = pageSizes[i + 1].width; 			
    		}
    		        	
        	if (leftWidth > maxWidth.left)
    			maxWidth.left = leftWidth;
        	if (rightWidth > maxWidth.right)
    			maxWidth.right = rightWidth;
    	}
        return maxWidth;
    }
    
    /**
     * Return the height of the row the page with the given index is in     
     */
    private static int getRowHeight(int pageIndex, Dimension[] pageSizes) {
    	if (pageSizes.length == 0)
    		return 0;
    	
    	// get the index of the left page
    	int actualIndex = pageIndex / 2 * 2;
    	
    	if (actualIndex == pageSizes.length - 1)
    		return pageSizes[actualIndex].height;
    	else
    		return Math.max(pageSizes[actualIndex].height, pageSizes[actualIndex + 1].height);
    }    

    @Override
    public List<PageVisibility> getVisiblePages(Dimension viewPortSize, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages) {
    	List<PageVisibility> visPages = new ArrayList<PageVisibility>(1);

        pageSizes = getPageSizes(zoomLevel, pageSizes);
                
        int py = getZoomedVerticalSpacing(zoomLevel) - scrollPosition.y;
        int topPage = 0;
        int rowHeight = getRowHeight(topPage, pageSizes);        
        while (py + rowHeight < 0) {            
            py += rowHeight + getZoomedVerticalSpacing(zoomLevel);
            // move to the next row
            topPage += 2;
            rowHeight = getRowHeight(topPage, pageSizes);
        }

        int p = topPage;
        while (p < pages.size() && py < viewPortSize.height) {
        	// calculate the visibility of the actual page content (without adorners and spacing)
        	float visibleHeight = Math.min(py + pageSizes[p].height, viewPortSize.height) - Math.max(0, py);        	
        	if (visibleHeight > 0)        		
        		visPages.add(new PageVisibility(p, visibleHeight / (float)pageSizes[p].height));

            if (p + 1 < pages.size()) {
            	visibleHeight = Math.min(py + pageSizes[p + 1].height, viewPortSize.height) - Math.max(0, py);        	
            	if (visibleHeight > 0)        		
            		visPages.add(new PageVisibility(p + 1, visibleHeight / (float)pageSizes[p + 1].height));                            
            }

            py += getRowHeight(p, pageSizes) + getZoomedVerticalSpacing(zoomLevel);
            p += 2;
        }

        return visPages;
    }
    
    private int getXOffset(Rectangle targetRect, int startx, int width) {    	
    	if (width + 10 <= targetRect.width) {
        	// center pages horizontally (enough room)
    		return targetRect.x + targetRect.width / 2 - width / 2;
        } else {
            // scroll pages horizontally
        	return startx;
        }    	
    }

    @Override
    public void draw(Graphics2D g, Color backgroundColor, Rectangle targetRect, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages, PageAdorner adorner) {        
    	if (pages.size() == 0)
    		return;
    	
    	pageSizes = getPageSizes(zoomLevel, pageSizes);
    	
    	RowWidth maxWidth = getMaxWidth(pageSizes);
    	int px_start = getXOffset(targetRect, getZoomedHorizontalSpacing(zoomLevel) - scrollPosition.x, maxWidth.total() + 2*getZoomedHorizontalSpacing(zoomLevel));
    	       
        int py = getZoomedVerticalSpacing(zoomLevel) - scrollPosition.y;
        int topPage = 0;
        int rowHeight = getRowHeight(topPage, pageSizes);
        while (py + rowHeight < 0) {
            topPage += 2;
            py += rowHeight + getZoomedVerticalSpacing(zoomLevel);
        }

        int p = topPage; // get the left top page
        while (p < pages.size() && py < targetRect.y + targetRect.height) {
            Page page = pages.get(p);
                                  
            drawPage(g, backgroundColor, p + 1, zoomLevel, px_start, py + rowHeight/2 - pageSizes[p].height / 2, pageSizes[p], page, adorner);

            // a second page?
            if (p + 1 < pages.size()) {
            	drawPage(g, backgroundColor, p + 2, zoomLevel, px_start + maxWidth.left + getZoomedHorizontalSpacing(zoomLevel),  py + rowHeight/2 - pageSizes[p + 1].height / 2, pageSizes[p + 1], pages.get(p + 1), adorner);            	                
            }

            py += getRowHeight(p, pageSizes) + getZoomedVerticalSpacing(zoomLevel);
            p += 2;
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

    	pageSizes = getPageSizes(zoomLevel, pageSizes);
    	int row = pageIndex / 2;
    	int sy = 0;
    	int sx = 0;
    	for(int r = 0; r < row; r++)
    		sy += getRowHeight(r*2, pageSizes);
    	    	
    	if (pageIndex % 2 == 0) {
    		// left page
    		sx = getZoomedHorizontalSpacing(zoomLevel);
    	} else {
    		// right page
    		sx = pageSizes[pageIndex / 2 * 2].width + 2 * getZoomedHorizontalSpacing(zoomLevel);
    	}
    	        
        // add vertical spacing
        sy += getZoomedVerticalSpacing(zoomLevel) + row* getZoomedVerticalSpacing(zoomLevel);

        return new Point(sx, sy);
    }   

    @Override
    public Dimension getViewElementSize(float zoomLevel, int pageIndex, Dimension[] pageSizes) {
        pageSizes = getPageSizes(zoomLevel, pageSizes);        
        int rowHeight = getRowHeight(pageIndex, pageSizes);                             
        
        return new Dimension(getMaxWidth(pageSizes).total() + getZoomedHorizontalSpacing(zoomLevel), rowHeight + getZoomedVerticalSpacing(zoomLevel));
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

class RowWidth {
	public int left = 0;
	public int right = 0;
	
	public int total() {
		return left + right;
	}
}
