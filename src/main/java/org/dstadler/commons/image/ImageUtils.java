package org.dstadler.commons.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {
	public static byte[] getTextAsPNG(final String version) throws IOException {
		/*
	       Because font metrics is based on a graphics context, we need to create
	       a small, temporary image so we can ascertain the width and height
	       of the final image
	     */
	    BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	    Font font = new Font("Arial", Font.PLAIN, 14);
	    final int width, height;

	    Graphics2D g2d = img.createGraphics();
	    try {
		    g2d.setFont(font);
		    FontMetrics fm = g2d.getFontMetrics();
		    width = fm.stringWidth(version);
		    height = fm.getHeight();
	    } finally {
	    	g2d.dispose();
	    }

	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    g2d = img.createGraphics();
	    try {
		    //g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		    //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    //g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		    //g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		    //g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		    //g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		    //g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		    //g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		    g2d.setFont(font);
		    FontMetrics fm = g2d.getFontMetrics();
		    g2d.setColor(Color.BLACK);
		    g2d.drawString(version, 0, fm.getAscent());
	    } finally {
	    	g2d.dispose();
	    }
	    try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
	    	ImageIO.write(img, "png", stream);
	    	stream.flush();
	    	return stream.toByteArray();
	    }
	}
}
