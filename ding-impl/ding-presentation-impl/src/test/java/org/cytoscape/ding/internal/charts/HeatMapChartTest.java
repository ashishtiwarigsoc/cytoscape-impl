package org.cytoscape.ding.internal.charts;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.cytoscape.ding.internal.charts.bar.BarChart.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.ding.customgraphics.AbstractCustomGraphics2Test;
import org.cytoscape.ding.customgraphics.ColorScheme;
import org.cytoscape.ding.customgraphics.Orientation;
import org.cytoscape.ding.internal.charts.heatmap.HeatMapChart;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.junit.Before;
import org.junit.Test;

public class HeatMapChartTest extends AbstractCustomGraphics2Test {

	private CyColumnIdentifierFactory colIdFactory = new DummyCyColumnIdentifierFactory();
	
	@Before
	public void setUp() throws Exception {
		props1 = new HashMap<String, Object>();
		props1.put(DATA_COLUMNS, 
				asList(colIdFactory.createColumnIdentifier("data1"),
					   colIdFactory.createColumnIdentifier("data2")));
		props1.put(COLOR_SCHEME, "RANDOM");
		props1.put(AUTO_RANGE, false);
		props1.put(GLOBAL_RANGE, true);
		props1.put(RANGE, asList(-10.5, 15.0));
		props1.put(COLORS, asList(Color.RED, Color.BLUE, Color.GREEN));
		props1.put(ORIENTATION, "VERTICAL");
		props1.put(BORDER_COLOR, Color.WHITE);
		props1.put(BORDER_WIDTH, 2.5f);
		props1.put(DOMAIN_LABELS_COLUMN, "labels2");
		props1.put(RANGE_LABELS_COLUMN, colIdFactory.createColumnIdentifier("labels3"));
		props1.put(DOMAIN_LABEL_POSITION, "UP_45");
		props1.put(SHOW_DOMAIN_AXIS, true);
		props1.put(SHOW_RANGE_AXIS, false);
		props1.put(AXIS_WIDTH, 1.5f);
		props1.put(AXIS_COLOR, Color.YELLOW);
	}

	@Test
	public void testChartProperties() {
		final HeatMapChart chart = new HeatMapChart(props1, colIdFactory);
		
		// The chart properties has to return exactly the same values,
		// except column names, which are converted to CyColumIdentifier when set as String
		Map<String, Object> props2 = chart.getProperties();
		assertEquals(props1.get(DATA_COLUMNS), props2.get(DATA_COLUMNS));
		assertEquals(props1.get(COLOR_SCHEME), props2.get(COLOR_SCHEME));
		assertEquals(props1.get(AUTO_RANGE), props2.get(AUTO_RANGE));
		assertEquals(props1.get(GLOBAL_RANGE), props2.get(GLOBAL_RANGE));
		assertEquals(props1.get(RANGE), props2.get(RANGE));
		assertEquals(props1.get(COLORS), props2.get(COLORS));
		assertEquals(props1.get(ORIENTATION), props2.get(ORIENTATION));
		assertEquals(props1.get(BORDER_COLOR), props2.get(BORDER_COLOR));
		assertEquals(props1.get(BORDER_WIDTH), props2.get(BORDER_WIDTH));
		assertEquals("labels2", ((CyColumnIdentifier)props2.get(DOMAIN_LABELS_COLUMN)).getColumnName());
		assertEquals("labels3", ((CyColumnIdentifier)props2.get(RANGE_LABELS_COLUMN)).getColumnName());
		assertEquals(props1.get(DOMAIN_LABEL_POSITION), props2.get(DOMAIN_LABEL_POSITION));
		assertEquals(props1.get(SHOW_DOMAIN_AXIS), props2.get(SHOW_DOMAIN_AXIS));
		assertEquals(props1.get(SHOW_RANGE_AXIS), props2.get(SHOW_RANGE_AXIS));
		assertEquals(props1.get(AXIS_WIDTH), props2.get(AXIS_WIDTH));
		assertEquals(props1.get(AXIS_COLOR), props2.get(AXIS_COLOR));
		assertNull(props2.get(VALUES));
		
		// When calling the internal get methods, some property values are converted to internal types,
		// which are not exposed to the API client code
		assertEquals(props2.get(DATA_COLUMNS), chart.getList(DATA_COLUMNS, CyColumnIdentifier.class));
		assertEquals(ColorScheme.RANDOM, chart.get(COLOR_SCHEME, ColorScheme.class));
		assertEquals(Boolean.FALSE, chart.get(AUTO_RANGE, Boolean.class));
		assertEquals(Boolean.TRUE, chart.get(GLOBAL_RANGE, Boolean.class));
		assertEquals(props1.get(RANGE), chart.getList(RANGE, Double.class));
		assertEquals(props1.get(COLORS), chart.getList(COLORS, Color.class));
		assertEquals(Orientation.VERTICAL, chart.get(ORIENTATION, Orientation.class));
		assertEquals(Color.WHITE, chart.get(BORDER_COLOR, Color.class));
		assertEquals(new Float(2.5f), chart.get(BORDER_WIDTH, Float.class));
		assertEquals("labels2", chart.get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class).getColumnName());
		assertEquals("labels3", chart.get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class).getColumnName());
		assertEquals(LabelPosition.UP_45, chart.get(DOMAIN_LABEL_POSITION, LabelPosition.class));
		assertEquals(Boolean.TRUE, chart.get(SHOW_DOMAIN_AXIS, Boolean.class));
		assertEquals(Boolean.FALSE, chart.get(SHOW_RANGE_AXIS, Boolean.class));
		assertEquals(new Float(1.5f), chart.get(AXIS_WIDTH, Float.class));
		assertEquals(Color.YELLOW, chart.get(AXIS_COLOR, Color.class));
		assertTrue(chart.getList(VALUES, Double.class).isEmpty()); // Must never be null!
	}
}
