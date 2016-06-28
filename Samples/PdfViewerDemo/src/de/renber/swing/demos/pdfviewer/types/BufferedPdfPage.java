package de.renber.swing.demos.pdfviewer.types;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

import de.renber.swing.controls.documentview.types.Page;

/**
 * A page implementation which holds an image buffer of its contents
 *
 * @author René Bergelt
 */
public class BufferedPdfPage implements Page {

	BufferedImage imgBuffer = null;
	Dimension scaleDimension = new Dimension(0, 0);
	Dimension pageSize;	
	Dimension originalPrintSize;

	int pageIndex;
	PDFRenderer renderer;
	
	public static String renderingPlaceholderText = "Rendering page...";  

	@Override
	public void draw(Graphics2D g, int x, int y, int w, int h) {				
		if (imgBuffer == null) {
			Color oldColor = g.getColor();

			g.setColor(Color.WHITE);
			g.fillRect(x, y, w, h);
			
			// print the renderin ghint if there is enough place
			Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(renderingPlaceholderText, g);			
			if (stringBounds.getWidth() + 30 < w && stringBounds.getHeight() + 30 < h) {			
				g.setColor(Color.BLACK);						
				g.drawString(renderingPlaceholderText, x + 20, y + 20);
			}

			g.setColor(oldColor);
		} else {
			g.drawImage(imgBuffer, x, y, w, h, null);			
		}
	}

	@Override
	public boolean isScaled(int w, int h) {
		return scaleDimension.width == w && scaleDimension.height == h;
	}

	@Override
	public void hiQualityScale(final int w, final int h) {
		if (isScaled(w, h))
			return;
		
		scaleDimension.width = w;
		scaleDimension.height = h;	
		
		BufferedImage scaledBuf = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = null;
		try {													
			g = (Graphics2D)scaledBuf.createGraphics();			
			// ensure white background
			g.setColor(Color.white);
			g.setBackground(Color.white);
			g.fillRect(0, 0, w, h);			
			
			renderer.renderPageToGraphics(pageIndex, g, w / (float)originalPrintSize.width);			
		} catch (Exception e) {
			scaledBuf = null;
		} finally {
			if (g != null) {
				g.dispose();
			}
		}
		
		if (scaledBuf != null)
			imgBuffer = scaledBuf;		
	}

	public BufferedPdfPage(int pageIndex, PDFRenderer renderer, Dimension pageSize) {
		this.pageIndex = pageIndex;
		this.renderer = renderer;	
		this.pageSize = pageSize;
		
		originalPrintSize = new Dimension((int)(pageSize.width / 96.0f * 72.0f), (int)(pageSize.height / 96.0f * 72.0f));
	}

	@Override
	public void freeResources() {
		if (imgBuffer != null) {
			imgBuffer.flush();
		}
	}

	@Override
	public Dimension getPageSize() {
		return pageSize;
	}
}
