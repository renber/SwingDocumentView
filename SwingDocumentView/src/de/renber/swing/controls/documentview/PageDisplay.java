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
package de.renber.swing.controls.documentview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

import de.renber.swing.controls.documentview.predefined.ShadowPageAdorner;
import de.renber.swing.controls.documentview.predefined.SinglePagePreviewLayout;
import de.renber.swing.controls.documentview.types.Page;
import de.renber.swing.controls.documentview.types.PageAdorner;
import de.renber.swing.controls.documentview.types.PagePreviewEventListener;
import de.renber.swing.controls.documentview.types.PageSource;
import de.renber.swing.controls.documentview.types.PageVisibility;
import de.renber.swing.controls.documentview.types.PreviewLayout;
import de.renber.swing.controls.documentview.types.scaling.PageScaler;
import de.renber.swing.controls.documentview.types.scaling.ScalingListener;

/**
 * Control to display pages in a print preview manner
 * 
 * @author René Bergelt
 */
public class PageDisplay extends JPanel implements AdjustmentListener, ScalingListener {

	// the distance to scroll with one mouse wheel 'click'
	private static int WHEEL_SCROLL_DISTANCE = 40;
	// change of the zoom level with one mouse wheel 'click'
	private static float ZOOM_SCROLL_CHANGE = 0.03f;
	// the minmum zoom level
	private static float MIN_ZOOM_LEVEL = 0.05f;
	// the maximum zoom level
	private static float MAX_ZOOM_LEVEL = 4f;
	// The pages which already have been loaded
	List<Page> bufferedPages = new ArrayList<Page>();	
	// The current zoom level (0 .. 1 (=100 %) .. max
	float zoomLevel = 1;
	// the page source
	PageSource pageSource;
	// the page layout
	PreviewLayout layout = new SinglePagePreviewLayout();	
	// size of the pages @ 100%
	Dimension[] originalPageSizes = new Dimension[0];	
	// the page adorner
	PageAdorner adorner;
	// the background color of the view port
	private Color backgroundColor = Color.GRAY; // the background color
	// Scrollbars
	JScrollBar horizontalScrollBar;
	JScrollBar verticalScrollBar;
	Point scrollPosition = new Point(0, 0);
	// threaded scaling
	PageScaler pageScaler = new PageScaler();
	// allow high quality scaling to be used
	boolean allowHiQualityScale = true;
	// is repainting allowed?
	boolean suppressPainting = false;
	// registered event listeners
	List<PagePreviewEventListener> pagePreviewEventListeners = new ArrayList<PagePreviewEventListener>();

	public PageDisplay() {
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateScrollBars();
				repaint();
			}
		});

		createScrollBars();

		this.addMouseWheelListener(new PageDisplayMouseWheelListener());

		pageScaler.addScalingListener(this);
		pageScaler.enable();
	}

	/**
	 * Creates the horizontal and vertical scrollbars
	 */
	private void createScrollBars() {
		verticalScrollBar = new JScrollBar();
		horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);

		horizontalScrollBar.addAdjustmentListener(this);
		verticalScrollBar.addAdjustmentListener(this);

		setLayout(new BorderLayout());
		add(verticalScrollBar, BorderLayout.LINE_END);

		JPanel bottomPanel = new JPanel();
		add(bottomPanel, BorderLayout.PAGE_END);

		bottomPanel.setLayout(new BorderLayout());

		bottomPanel.add(horizontalScrollBar, BorderLayout.CENTER);

		JPanel cornerPanel = new JPanel();
		cornerPanel.setAlignmentX(1);

		Dimension conerSize = new Dimension(16, 14);
		cornerPanel.setSize(conerSize);
		cornerPanel.setPreferredSize(conerSize);
		cornerPanel.setMinimumSize(conerSize);
		// cornerPanel.setMaximumSize(conerSize);
		bottomPanel.add(cornerPanel, BorderLayout.EAST);
	}

	/**
	 * Updates the scrollbars max values or disables them when not needed
	 */
	private void updateScrollBars() {

		if (layout == null) {
			horizontalScrollBar.setEnabled(false);
			verticalScrollBar.setEnabled(false);
			return;
		}
		
		Dimension neededSize = layout.getNeededSpace(zoomLevel, originalPageSizes);
		int availableWidth = getPaintArea().width;
		int availableHeight = getPaintArea().height;

		DefaultBoundedRangeModel hModel = new DefaultBoundedRangeModel();
		DefaultBoundedRangeModel vModel = new DefaultBoundedRangeModel();

		int oldHorz = horizontalScrollBar.getValue();
		int oldVert = verticalScrollBar.getValue();

		if (neededSize.width <= availableWidth) {
			horizontalScrollBar.setEnabled(false);
			scrollPosition.x = 0;
		} else {
			horizontalScrollBar.setEnabled(true);			
			hModel.setMaximum(neededSize.width);
			hModel.setValue(oldHorz < neededSize.width - availableWidth ? oldHorz : neededSize.width - availableWidth);
			scrollPosition.x = hModel.getValue();
			horizontalScrollBar.setUnitIncrement(10);
		}

		if (neededSize.height <= availableHeight) {
			verticalScrollBar.setEnabled(false);
			scrollPosition.y = 0;
		} else {
			verticalScrollBar.setEnabled(true);						
			vModel.setMaximum(neededSize.height);
			vModel.setValue(oldVert < neededSize.height - availableHeight ? oldVert : neededSize.height - availableHeight);			
			scrollPosition.y = vModel.getValue();
		}

		hModel.setExtent(availableWidth);
		vModel.setExtent(availableHeight);

		horizontalScrollBar.setModel(hModel);
		verticalScrollBar.setModel(vModel);
	}

	// Since we're always going to fill our entire
	// bounds, allow Swing to optimize painting for us
	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	protected void paintComponent(Graphics gr) {

		if (suppressPainting) {
			// painting disabled
			return;
		}

		Graphics2D g = (Graphics2D) gr;

		g.setColor(backgroundColor);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (pageSource == null) {
			// no pages to draw
			return;
		}

		Dimension d = getPaintArea();
		int w = d.width;
		int h = d.height;

		if (layout != null) {
			// page scaling
			if (allowHiQualityScale) {
				Dimension[] requestedPageSizes = layout.getPageSizes(zoomLevel, originalPageSizes);
				List<PageVisibility> pages = new ArrayList<PageVisibility>(layout.getVisiblePages(new Dimension(w, h), zoomLevel, originalPageSizes, scrollPosition, bufferedPages));
				
				// scale the first unscaled page with high priority
				boolean highPriorityScale = true;
				
				for (PageVisibility pv: pages) {
					int pageIndex = pv.getPageIndex();
					Page p = bufferedPages.get(pageIndex);															
					if (!p.isScaled(requestedPageSizes[pageIndex].width, requestedPageSizes[pageIndex].height)) {
						scalePage(p, requestedPageSizes[pageIndex].width, requestedPageSizes[pageIndex].height, highPriorityScale);
						highPriorityScale = false;
					}
				}
			}

			layout.draw(g, backgroundColor, new Rectangle(0, 0, w, h), zoomLevel, originalPageSizes, scrollPosition, bufferedPages, adorner);
		}
	}

	/**
	 * Return the total number of pages
	 *
	 * @return
	 */
	public int getPageCount() {
		return pageSource == null ? 0 : pageSource.getPageCount();
	}

	/**
	 * Returns the index of the page with is currently most visible in the view port
	 *
	 * @return
	 */
	public int getCurrentPage() {
		if (pageSource != null && layout != null) {
			List<PageVisibility> visPages = layout.getVisiblePages(getPaintArea(), zoomLevel, originalPageSizes, scrollPosition, bufferedPages);
			if (visPages.isEmpty()) {
				return -1;
			} else {
				// find the first page which has the highest content visibility
				float maxPagePercentage = 0;
				int vIndex = 0;
				
				for(int i = 0; i < visPages.size(); i++)
					if (visPages.get(i).getVisiblePercentage() > maxPagePercentage) {
						maxPagePercentage = visPages.get(i).getVisiblePercentage();
						vIndex = i;
					}
				
				return visPages.get(vIndex).getPageIndex();
			}
		} else {
			return -1;
		}
	}
	
	/**
	 * Returns the indices of the currently visible pages and
	 * the percentage of their content visible in the viewport
	 */
	public PageVisibility[] getVisiblePages() {
		List<PageVisibility> visPages = layout.getVisiblePages(getPaintArea(), zoomLevel, originalPageSizes, scrollPosition, bufferedPages);		
		return visPages.toArray(new PageVisibility[visPages.size()]);
	}

	private Dimension getPaintArea() {
		return new Dimension(getWidth() - verticalScrollBar.getWidth(), getHeight() - horizontalScrollBar.getHeight());
	}

	public boolean canScrollHorizontally() {
		return horizontalScrollBar.isEnabled();
	}

	public boolean canScrollVertically() {
		return verticalScrollBar.isEnabled();
	}

	/**
	 * Brings the given page into view
	 *
	 * @param index
	 */
	public void gotoPage(int index) {
		if (layout != null && index < getPageCount()) {
			Point newScrollPos = layout.ensureVisible(getPaintArea(), zoomLevel, originalPageSizes, index);

			beginUpdate();
			horizontalScrollBar.setValue(newScrollPos.x);
			verticalScrollBar.setValue(newScrollPos.y);
			endUpdate();

			fireCurrentPageChanged();
		}
	}
	
	/**
	 * Show the next view element (page or group of pages)
	 */
	public void gotoNextViewElement() {
		if (layout != null) {
			int newPage = Math.min(getPageCount() - 1, layout.getPageIndexOfNextViewElement(getCurrentPage()));
			gotoPage(newPage);
		}
	}
	
	/**
	 * Show the previous view element (page or group of pages)
	 */
	public void gotoPreviousViewElement() {
		if (layout != null) {
			int newPage = Math.max(0, layout.getPageIndexOfPreviousViewElement(getCurrentPage()));
			gotoPage(newPage);
		}
	}

	/**
	 * Sets the zoom level in such a way that the current page (or view element
	 * depending on the current PreviewLayout) fits the view port
	 */
	public void fitPage() {
		if (layout != null) {

			// calculate the zoom level needed to fit the current view
			// element from the given layout to the view port
			beginUpdate();
			Dimension d = layout.getViewElementSize(1, getCurrentPage(), originalPageSizes);
			Dimension viewPortSize = getPaintArea();
			// scaling factor
			double sw = viewPortSize.getWidth() / d.getWidth();
			double sh = viewPortSize.getHeight() / d.getHeight();
			double sf = Math.min(sw, sh);
			// zoom factor
			float newZoomLevel = (float) (d.getWidth() * sf / d.getWidth());
			setZoomLevel(newZoomLevel);

			gotoPage(getCurrentPage());
			endUpdate();

			updateScrollBars();
		}
	}

	/**
	 * Sets the zoom level in such a way that the width of the current page (or
	 * view element depending on the current PreviewLayout) fits the view port
	 */
	public void fitWidth() {
		if (layout != null) {
			// calculate the zoom level needed to fit the width of the current view
			// element from the given layout to the view port
			beginUpdate();
			Dimension d = layout.getViewElementSize(1, getCurrentPage(), originalPageSizes);
			Dimension viewPortSize = getPaintArea();
			// scaling factor (only the width is considered)
			double sw = viewPortSize.getWidth() / d.getWidth();
			// zoom factor
			float newZoomLevel = (float) (d.getWidth() * sw / d.getWidth());
			setZoomLevel(newZoomLevel);

			gotoPage(getCurrentPage());
			endUpdate();

			updateScrollBars();
		}
	}

	/**
	 * Scales the given page asynchronously and updates the preview when done
	 */
	private void scalePage(final Page page, final int w, final int h, boolean highPriority) {
		pageScaler.enqeue(page, new Dimension(w, h), highPriority);
	}

	/**
	 * Suppress all repaint attempts until endUpdate() has been called Should be
	 * used if you're going to change a lot of settings at once to avoid
	 * unnecessary repaint cycles
	 */
	public void beginUpdate() {
		suppressPainting = true;
	}

	/**
	 * Allow painting <br/>
	 * automatically calls repaint()
	 */
	public void endUpdate() {
		suppressPainting = false;
		repaint();
	}

	// ***************
	// GETTER / SETTER
	// ***************
	/**
	 * @return the backgroundColor
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * @param backgroundColor
	 *            the backgroundColor to set
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		this.repaint();
	}

	/**
	 * Set a new page source for this page display control
	 *
	 * @param newValue
	 */
	public void setPageSource(PageSource newValue) {
		pageSource = newValue;

		setZoomLevel(1);		

		// get the page sizes
		originalPageSizes = new Dimension[pageSource.getPageCount()];
		for(int i = 0; i < originalPageSizes.length; i++)
			originalPageSizes[i] = pageSource.getPageSize(i);
		
		bufferedPages.clear();

		Thread loadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < getPageCount(); i++) {
					bufferedPages.add(pageSource.getPage(i));
					updateScrollBars();
					fireCurrentPageChanged();
					repaint();
				}
			}
		});
		loadThread.start();

		updateScrollBars();
		fireCurrentPageChanged();

		repaint();
	}

	public PageSource getPageSource() {
		return pageSource;
	}

	/**
	 * Sets the preview layout which arranges the pages
	 *
	 * @param newValue
	 */
	public void setPreviewLayout(PreviewLayout newValue) {
		layout = newValue;

		updateScrollBars();
		repaint();
	}

	public PreviewLayout getPreviewLayout() {
		return layout;
	}

	/**
	 * Set the adorner to use or null if none shall be used
	 *
	 * @param newValue
	 */
	public void setPageAdorner(PageAdorner newValue) {
		adorner = newValue;
	}

	public PageAdorner getPageAdorner() {
		return adorner;
	}

	/**
	 * Set the zoom level
	 *
	 * @param newValue
	 *            The zoom level (1 = 100%)
	 */
	public void setZoomLevel(float newValue) {
		if (newValue <= 0) {
			throw new IllegalArgumentException("The zoom level must be greater than 0.");
		}
		
		if (newValue < MIN_ZOOM_LEVEL) {
			newValue = MIN_ZOOM_LEVEL;
		}
		if (newValue > MAX_ZOOM_LEVEL) {
			newValue = MAX_ZOOM_LEVEL;
		}

		if (Math.abs(zoomLevel - newValue) > 0.01) {

			zoomLevel = newValue;
			updateScrollBars();

			fireCurrentPageChanged();
			fireZoomLevelChanged();

			repaint();
		}
	}

	public float getZoomlevel() {
		return zoomLevel;
	}

	/**
	 * Return if high quality scaling is allowed
	 */
	public boolean getAllowHiQualityScale() {
		return allowHiQualityScale;
	}

	/**
	 * Enable / disable hiqh quality scaling in zoomed views
	 */
	public void setAllowHiQualityScale(boolean newValue) {
		allowHiQualityScale = newValue;

		if (allowHiQualityScale) {
			// if hi quality scale has been enabled
			// repaint the current view
			repaint();
		}
	}

	/**
	 * ********* FINALIZER *********
	 */
	@Override
	protected void finalize() {
		try {
			freeResources();
		} finally {
			try {
				super.finalize();
			} catch (Throwable ex) {
				// --
			}
		}
	}

	/**
	 * Free the resources held by the preview control
	 */
	public void freeResources() {
		// terminate the background scaling thread
		pageScaler.disable();
		pageScaler.removeScalingListener(null);

		if (pageSource != null) {
			pageSource.freeResources();
		}

		for (Page page : bufferedPages) {
			page.freeResources();
		}
	}

	// *******************
	// SCROLLBAR SCROLLING
	// *******************
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == verticalScrollBar) {
			// change the y scroll
			int oldPage = getCurrentPage();
			scrollPosition.y = e.getValue();
			//if (getCurrentPage() != oldPage) {
				fireCurrentPageChanged();
			//}
			repaint();
		} else if (e.getSource() == horizontalScrollBar) {
			// change the x scroll
			int oldPage = getCurrentPage();
			scrollPosition.x = e.getValue();
			//if (getCurrentPage() != oldPage) {
				fireCurrentPageChanged();
			//}
			repaint();
		}
	}

	@Override
	public void scalingDone(Page p, Dimension targetResolution) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				repaint();
			}
		});
	}

	// *******************
	// Mouse wheel scrolling
	// *******************
	class PageDisplayMouseWheelListener implements MouseWheelListener {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0) {
				// Zoom
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {

					float newZoomLevel = zoomLevel + ((-1) * e.getWheelRotation() * ZOOM_SCROLL_CHANGE);
					if (newZoomLevel < 0.01) {
						newZoomLevel = 0.01f;
					}
					if (newZoomLevel > MAX_ZOOM_LEVEL) {
						newZoomLevel = MAX_ZOOM_LEVEL;
					}

					setZoomLevel(newZoomLevel);
					fireZoomLevelChanged();
				}

			} else {
				// Scroll
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					verticalScrollBar.setValue(verticalScrollBar.getValue()
							+ (int) (e.getWheelRotation() * WHEEL_SCROLL_DISTANCE * zoomLevel));
				}
			}
		}
	}

	// ******
	// EVENTS
	// ******
	public void addPagePreviewEventListeners(PagePreviewEventListener listener) {
		if (!pagePreviewEventListeners.contains(listener)) {
			pagePreviewEventListeners.add(listener);
		}
	}

	public void removePagePreviewEventListeners(PagePreviewEventListener listener) {
		if (pagePreviewEventListeners.contains(listener)) {
			pagePreviewEventListeners.remove(listener);
		}
	}

	public void fireCurrentPageChanged() {
		for (PagePreviewEventListener l : pagePreviewEventListeners) {
			l.currentPageChanged();
		}
	}

	public void fireZoomLevelChanged() {
		for (PagePreviewEventListener l : pagePreviewEventListeners) {
			l.zoomLevelChanged();
		}
	}
}
