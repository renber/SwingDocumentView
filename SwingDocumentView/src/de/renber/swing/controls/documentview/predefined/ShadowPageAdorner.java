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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import de.renber.swing.controls.documentview.types.PageAdorner;

/**
 * Adorns a page in the PageDisplay with a shadow
 *
 * @author berre
 */
public class ShadowPageAdorner implements PageAdorner {

    Color shadowColor;
    int shadowWidth;
    
    public ShadowPageAdorner(int _shadowWidth, Color _shadowColor) {
        shadowWidth = _shadowWidth;
        shadowColor = _shadowColor;
    }
    
    
    @Override
    public void drawPrePage(Graphics2D g, Color backgroundColor, int pageNumber, float zoomLevel, int x, int y, int w, int h) {
        paintShadow(g, backgroundColor, x, y, w, h);
    }

    @Override
    public void drawPostPage(Graphics2D g, Color backgroundColor, int pageNumber, float zoomLevel, int x, int y, int w, int h) {
        paintBorder(g, x, y, w, h);
    }

    public void paintBorder(Graphics2D g, int x, int y, int w, int h) {
        // draw a frame
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);
    }

    public void paintShadow(Graphics2D g, Color backgroundColor, int x, int y, int w, int h) {
        Graphics2D g2 = null;
        try {
            g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int sw = shadowWidth * 2;
            for (int i = sw; i >= 2; i -= 2) {
                float pct = (float) (sw - i) / (sw - 1);
                 g2.setColor(getMixedColor(shadowColor, pct, backgroundColor, 1.0f - pct));
                 g2.setStroke(new BasicStroke(i));
                 g2.drawLine(x + w, y + shadowWidth, x + w, y + h);
                 g2.drawLine(x + shadowWidth, y + h, x + w, y + h);

            }
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }
    }

    private static Color getMixedColor(Color c1, float pct1, Color c2, float pct2) {
        float[] clr1 = c1.getComponents(null);
        float[] clr2 = c2.getComponents(null);
        for (int i = 0; i < clr1.length; i++) {
            clr1[i] = (clr1[i] * pct1) + (clr2[i] * pct2);
        }
        return new Color(clr1[0], clr1[1], clr1[2], clr1[3]);
    }
}
