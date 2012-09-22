package org.cytoscape.ding.customgraphics.vector;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import org.cytoscape.ding.customgraphics.paint.LinearGradientPaintFactory;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;

public class GradientRoundRectangleLayer extends GradientLayerCustomGraphics {
	
	// Name of this custom graphics.
	private static final String NAME = "Round Rectangle Gradient";
	private int r =20;

	
	public GradientRoundRectangleLayer(final Long id) {
		super(id, NAME);
	}
	
	protected void renderImage(Graphics graphics) {
		super.renderImage(graphics);
		
		final Graphics2D g2d = (Graphics2D) graphics;
		// Render
		update();
		g2d.setPaint(paintFactory.getPaint(shape.getBounds2D()));
		g2d.fillRoundRect(rendered.getMinX(), rendered.getMinY(), 
				rendered.getWidth(), rendered.getHeight(), r, r);
	}
	
	
	public void update() {
		// First, remove all layers.
		layers.clear();
		
		r = (int)(Math.min(width, height)/4f);
		shape = new RoundRectangle2D.Double(-width / 2, -height / 2,
																	width, height, r, r);
		paintFactory = new LinearGradientPaintFactory(colorList, stopList);
		final PaintCustomGraphic cg = new PaintCustomGraphic(shape, paintFactory);
		layers.add(cg);
	}

}
