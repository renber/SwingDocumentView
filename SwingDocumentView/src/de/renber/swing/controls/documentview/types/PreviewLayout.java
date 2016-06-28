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
package de.renber.swing.controls.documentview.types;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

/**
 * Interface for classes which arrange preview pages
 * @author berre
 */
public interface PreviewLayout {
    
    /**
     * Returns the space needed in pixels
     * @param zoomLevel The current zoom level
     * @param pageSizes Size of the pages (@ 100% Zoom)
     * @return 
     */
	public Dimension getNeededSpace(float zoomLevel, Dimension[] pageSizes);

    /**
     * Return the values for page sizes at the given zoom level
     * @param zoomLevel
     * @param pageSize
     * @return 
     */
    public Dimension[] getPageSizes(float zoomLevel, Dimension[] pageSizes);
    
    /**
     * Return the amount of space a view element (e.g. a single page or two facing pages) need including
     * any borders or spacing alignments in this layout
     * @param zoomLevel The zoom level
     * @param pageIndex index of the view element
     * @param pageSize The page sizes (content only) at 100 %
     * @return 
     */
    public Dimension getViewElementSize(float zoomLevel, int pageIndex, Dimension[] pageSizes);
    
    /**
     * Return the pages which would be visible in the given view rect
     * and the percentage of the page which is visible
     * @param viewPortSize
     * @param zoomLevel
     * @param pageSize
     * @param scrollPosition
     * @param pages 
     */
    public List<PageVisibility> getVisiblePages(Dimension viewPortSize, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages);
    
    /**
     * Draws the preview layout on the given surface
     * @param g
     * @param targetRect 
     */
    public void draw(Graphics2D g, Color backgroundColor, Rectangle targetRect, float zoomLevel, Dimension[] pageSizes, Point scrollPosition, List<Page> pages, PageAdorner adorner);
    
    /**
     * Returns a scroll position where the given page can be seen
     * @param targetRect
     * @param zoomLevel
     * @param pageSize
     * @param pageIndex
     * @return 
     */
    public Point ensureVisible(Dimension viewPortSize, float zoomLevel, Dimension[] pageSizes, int pageIndex);    
    
    /**
     * Return the amount of view elements in this layout for the given number of pages
     */
    public int getViewElementCount(int numberOfPages);
    
    /**
     * Return the index of the view element for the given page index     
     */
    public int getViewElementIndex(int pageIndex);
    
    /**
     * Return the index of the first page of the next view element
     * after the page with the given index
     * @return pageIndex >= 0
     */
    public int getPageIndexOfNextViewElement(int pageIndex);
    
    /**
     * Return the index of the first page of the previous view element
     * before the page with the given index
     * @return pageIndex >= 0
     */
    public int getPageIndexOfPreviousViewElement(int pageIndex);        
}
