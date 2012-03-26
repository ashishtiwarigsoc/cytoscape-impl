/*
 Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.cytoscape.view.vizmap.internal.mappings;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.mappings.AbstractVisualMappingFunction;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 */
public class PassthroughMappingImpl<K, V> extends
		AbstractVisualMappingFunction<K, V> implements PassthroughMapping<K, V> {
	
	/**
	 * dataType is the type of the _attribute_ !! currently we force that to be
	 * the same as the VisualProperty; FIXME: allow different once? but how to
	 * coerce?
	 */
	public PassthroughMappingImpl(final String attrName, final Class<K> attrType, final CyTable table,
			final VisualProperty<V> vp) {
		super(attrName, attrType, table, vp);
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.view.vizmap.mappings.PassthroughMapping#toString()
	 */
	@Override
	public String toString() {
		return PassthroughMapping.PASSTHROUGH;
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.view.vizmap.mappings.PassthroughMapping#apply(org.cytoscape.view.model.View)
	 */
	@Override
	public void apply(final CyRow row, final View<? extends CyIdentifiable> view) {
		if ( row == null )
			return;

		if (view == null)
			return; // empty list, nothing to do

		if(attrName.equals(CyIdentifiable.SUID)) {
			// Special case: SUID
			view.setVisualProperty(vp, (V)Long.valueOf(view.getModel().getSUID()));
		} else if (row.isSet(attrName)) {
			// skip Views where source attribute is not defined;
			// ViewColumn will automatically substitute the per-VS or
			// global default, as appropriate
			final CyColumn column = row.getTable().getColumn(attrName);
			final Class<?> attrClass = column.getType();
			K value = null;
			
			if (attrClass.isAssignableFrom(List.class)) {
				List<?> list = row.getList(attrName, column.getListElementType());
				StringBuffer sb = new StringBuffer();
				
				if (list != null && !list.isEmpty()) {
					for (Object item : list)
						sb.append(item.toString() + "\n");
					
					sb.deleteCharAt(sb.length() - 1);
				}
				
				value = (K) sb.toString();
			} else {
				value = row.get(attrName, attrType);
			}

			final V converted = convertToValue(value);

			view.setVisualProperty(vp, converted);
		} else {
			// remove value, so that default value will be used:
			view.setVisualProperty(vp, null);
		}
	}

	// TODO: make this converter pluggable
	private V convertToValue(final K key) {
		try {
			return (V) key;
		} catch (Exception e) {
			return null;
		}

	}
}
