package de.renber.swing.demos.pdfviewer.types;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.rendering.PDFRenderer;

import de.renber.swing.controls.documentview.types.Page;
import de.renber.swing.controls.documentview.types.PageSource;

/**
* Page source which returns the pages of a pdf document
* @author renber
*/
public class PdfPageSource implements PageSource {

   //public static final int DPI = 96;
   static int dpi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution(); 
	
   PDDocument document;
   Dimension originalPageSize;
   PDFRenderer renderer; 

   /**
    *
    * @param filename The pdf file to show
    * @param pageSize The page size in pixels
    */
   public PdfPageSource(PDDocument document) {
       this.document = document;              
       renderer = new PDFRenderer(document);
   }

   @Override
   public int getPageCount() {
       return document.getNumberOfPages();
   }

   private static boolean isPageLandscape(PDPage page) {
	   return page.getRotation() == 90 || page.getRotation() == 270;
   }
   
   @Override
   public Dimension getPageSize(int pageIndex) {	  
	   // calculate the correct page size in pixels
	   // for the screen dpi and the orientation of the page	   
	   PDPage page = document.getPage(pageIndex);	   	   
	   if (isPageLandscape(page))
		   return new Dimension((int)(page.getMediaBox().getHeight() / 72.0f * dpi), (int)(page.getMediaBox().getWidth() / 72.0f * dpi));
	   else	   
		   return new Dimension((int)(page.getMediaBox().getWidth() / 72.0f * dpi), (int)(page.getMediaBox().getHeight() / 72.0f * dpi));	   
   }

   @Override
   public Page getPage(int pageIndex) {
       if (pageIndex < getPageCount()) {
        return new BufferedPdfPage(pageIndex, renderer, getPageSize(pageIndex)); 
       } else {
           return null;
       }
   }

	@Override
	public void freeResources() {
		try {
			document.close();
		} catch (IOException e) {
			// --
		}		
	}
}