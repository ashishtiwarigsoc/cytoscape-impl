package org.cytoscape.io.internal.read.sif;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.TaskIterator;

public class SIFNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final CyLayoutAlgorithmManager layouts;
	
	public SIFNetworkReaderFactory(final CyFileFilter filter,
								   final CyLayoutAlgorithmManager layouts,
								   final CyApplicationManager cyApplicationManager,
								   final CyNetworkFactory cyNetworkFactory,
								   final CyNetworkManager cyNetworkManager,
								   final CyRootNetworkManager cyRootNetworkManager) {
		super(filter, cyApplicationManager, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		this.layouts = layouts;
	}
	
	@Override
	public TaskIterator createTaskIterator(final InputStream inputStream, final String inputName) {
		return new TaskIterator(new SIFNetworkReader(inputStream, layouts, cyApplicationManager, cyNetworkFactory,
				cyNetworkManager, cyRootNetworkManager));
	}
}