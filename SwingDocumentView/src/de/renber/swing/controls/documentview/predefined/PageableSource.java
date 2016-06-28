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

import java.awt.Dimension;
import java.awt.print.Pageable;

import de.renber.swing.controls.documentview.types.Page;
import de.renber.swing.controls.documentview.types.PageSource;
/**
 *
 * @author berre
 */
public class PageableSource implements PageSource {

    private static final int dpi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution(); 
    
    Pageable pageable;    
    
    public PageableSource(Pageable _pageable) {
        pageable = _pageable;                              
    }
    
    @Override
    public int getPageCount() {
        return pageable.getNumberOfPages();
    }

    @Override
    public Dimension getPageSize(int pageIndex) {    	
    	// convert the 72 dpi based dimensions to screen dpi    	
    	return new Dimension((int)(pageable.getPageFormat(pageIndex).getWidth() / 72.0f * dpi), (int)(pageable.getPageFormat(pageIndex).getHeight() / 72.0f * dpi));
    	               
    }
    
    @Override
    public Page getPage(int pageIndex) {            	    
        return new BufferedPrintablePage(pageable.getPrintable(pageIndex), pageIndex, pageable.getPageFormat(pageIndex));
    }

    @Override
    public void freeResources() {    
    	// --
    }
    
}
