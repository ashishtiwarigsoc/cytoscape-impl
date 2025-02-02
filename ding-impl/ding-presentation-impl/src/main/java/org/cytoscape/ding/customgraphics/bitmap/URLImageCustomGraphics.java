package org.cytoscape.ding.customgraphics.bitmap;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedSet;

import javax.imageio.ImageIO;

import org.cytoscape.ding.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.customgraphics.ImageUtil;
import org.cytoscape.ding.customgraphics.bitmap.ImageCustomGraphicImpl;
import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;
import org.cytoscape.graph.render.stateful.PaintFactory;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;

public class URLImageCustomGraphics <ImageCustomGraphicLayer> 
        extends AbstractDCustomGraphics {

	private static final String DEF_IMAGE_FILE = "images/no_image.png";
	protected static BufferedImage DEF_IMAGE;
	
	static  {
		try {
			DEF_IMAGE =ImageIO.read(URLImageCustomGraphics.class
					.getClassLoader().getResource(DEF_IMAGE_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final String DEF_TAG = "bitmap image";
	private static final float DEF_FIT_RATIO = 1.0f;

	private ImageCustomGraphicLayer icg;

	private BufferedImage originalImage;
	private BufferedImage scaledImage;

	private URL sourceUrl;	
	
	public URLImageCustomGraphics(Long id, String url) throws IOException {
		super(id, url);
		fitRatio = DEF_FIT_RATIO;
		
		// Special case.  We include a number of images as part of our bundles.  The
		// resulting URL's are not really helpful, so we need to massage the displayName
		// here.
		if (displayName.startsWith("bundle:")) {
			int index = displayName.lastIndexOf("/");
			displayName = displayName.substring(index+1);
		}
		// System.out.println("URLImageCustomGraphics: "+displayName+"("+id+") = "+url.toString());

		tags.add(DEF_TAG);
		createImage(url);
		buildCustomGraphics(originalImage);
	}


	/**
	 * 
	 * @param name
	 *            - display name of this object. NOT UNIQUE!
	 * @param img
	 */
	public URLImageCustomGraphics(Long id, String name, BufferedImage img) {
		super(id, name);
		
		if (img == null)
			throw new IllegalArgumentException("Image cannot be null.");

		fitRatio = DEF_FIT_RATIO;
		
		if (displayName.startsWith("bundle:")) {
			int index = displayName.lastIndexOf("/");
			displayName = displayName.substring(index+1);
		}

		// System.out.println("URLImageCustomGraphics: "+displayName+"("+id+")");

		// System.out.println("URLImageCustomGraphics: name = "+name);
		tags.add(DEF_TAG);
		this.originalImage = img;
		buildCustomGraphics(originalImage);
	}

	private void buildCustomGraphics(BufferedImage targetImg) {
		layers.clear();

		Rectangle2D bound = null;
		width = targetImg.getWidth();
		height = targetImg.getHeight();

		bound = new Rectangle2D.Double(-width / 2, -height / 2, width, height);
		final TexturePaintFactory paintFactory = new TexturePaintFactory(targetImg);

		ImageCustomGraphicImpl cg = new ImageCustomGraphicImpl(bound, paintFactory);
		
		layers.add(cg);
	}

	private void createImage(String url) throws MalformedURLException {
		if (url == null)
			throw new IllegalStateException("URL string cannot be null.");

		URL imageLocation = new URL(url);
		
		sourceUrl = imageLocation;
		try {
			originalImage = ImageIO.read(imageLocation);
		} catch (IOException e) {
			originalImage = DEF_IMAGE;
		}

		if (originalImage == null) {
			originalImage = DEF_IMAGE;
		}
	}

	@Override
	public Image getRenderedImage() {
		
		if (width == originalImage.getWidth() && height == originalImage.getHeight()) {
			return originalImage;
		}
		
		if(scaledImage == null) {
			resizeImage(width, height);
		} else if (scaledImage.getWidth() != width || scaledImage.getHeight() != height) {
			resizeImage(width, height);
		} 
		
		return scaledImage;
	}

	
	
	private Image resizeImage(int width, int height) {
		final Image img = originalImage.getScaledInstance(width, height,
				Image.SCALE_AREA_AVERAGING);
		try {
			scaledImage = ImageUtil.toBufferedImage(img);
		} catch (InterruptedException e) {
			// Could not get scaled one
			e.printStackTrace();
			return originalImage;
		}
		buildCustomGraphics(scaledImage);
		return scaledImage;
	}

	public Image resetImage() {
		if (scaledImage != null) {
			scaledImage.flush();
			scaledImage = null;
		}
		buildCustomGraphics(originalImage);
		return originalImage;
	}
	

	public URL getSourceURL() {
		return this.sourceUrl;
	}

	@Override
	public String toSerializableString() {
		if (sourceUrl != null)
			return makeSerializableString(sourceUrl.toString());

		return makeSerializableString(displayName);
	}

	@Override
	public String toString() {
		if (this.sourceUrl == null && displayName == null) {
			return "Empty image";
		} else if (this.sourceUrl != null && !this.sourceUrl.toString().startsWith("bundle")) {
			return "Image: "+this.sourceUrl.toString();
		} else {
			return "Image: "+displayName;
		}
	}

}
