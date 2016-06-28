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
import java.awt.Graphics2D;
import java.util.Arrays;

import de.renber.swing.controls.documentview.types.PageAdorner;

/**
 * Page adorner which allows to add multiple adorners to a page
 * @author René Bergelt
 */
public class CompoundAdorner implements PageAdorner {

	PageAdorner[] adorners;
	
	public CompoundAdorner(PageAdorner... adorners) {
		this.adorners = Arrays.copyOf(adorners, adorners.length);
	}
	
	@Override
	public void drawPrePage(Graphics2D g, Color backgroundColor, int pageNumber, float zoomLevel, int x, int y, int w,
			int h) {
		for(PageAdorner pa: adorners) {
			pa.drawPrePage(g, backgroundColor, pageNumber, zoomLevel, x, y, w, h);
		}
		
	}

	@Override
	public void drawPostPage(Graphics2D g, Color backgroundColor, int pageNumber, float zoomLevel, int x, int y, int w,
			int h) {
		for(PageAdorner pa: adorners) {
			pa.drawPostPage(g, backgroundColor, pageNumber, zoomLevel, x, y, w, h);
		}		
	}

}
