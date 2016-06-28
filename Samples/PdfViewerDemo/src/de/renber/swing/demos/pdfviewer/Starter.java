package de.renber.swing.demos.pdfviewer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.pdfbox.pdmodel.font.FontCache;
import org.apache.pdfbox.pdmodel.font.PDFontFactory;

/**
 *
 * @author berre
 */
public class Starter {

	static void setNativeLookAndFeel() {
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		setNativeLookAndFeel();
		
		// preload pdfbox's fontcache
		try {
			PDFontFactory.createDefaultFont();
		} catch (IOException e) {
			// --
		}
		
		PdfViewerFrame f = new PdfViewerFrame();
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		f.setLocation((d.width - f.getWidth()) / 2, (d.height - f.getHeight()) / 2);
		f.setVisible(true);
	}
}
