package org.cytoscape.task.internal.table;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.task.internal.utils.RowTunable;

public class SetValuesTask extends AbstractTableDataTask {
	final CyApplicationManager appMgr;

	@ContainsTunables
	public RowTunable rowTunable = null;

	@Tunable(description="Column to set", context="nogui")
	public String columnName = null;

	@Tunable(description="Value to set", context="nogui")
	public String value = null;

	public SetValuesTask(CyApplicationManager appMgr, CyTableManager tableMgr) {
		super(tableMgr);
		this.appMgr = appMgr;
		rowTunable = new RowTunable(tableMgr);
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		CyTable table = rowTunable.getTable();
		if (table == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
			                        "Unable to find table '"+rowTunable.getTableString()+"'");
			return;
		}

		List<CyRow> rowList = rowTunable.getRowList();
		if (rowList == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No rows returned");
			return;
		}

		if (columnName == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No column specified");
			return;
		}

		if (value == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "No values specified");
			return;
		}
		
		CyColumn column = table.getColumn(columnName);
		if (column == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Column '"+columnName+"' doesn't exist in this table");
			return;
		}

		Class columnType = column.getType();
		Class listType = null;
		if (columnType.equals(List.class))
			listType = column.getListElementType();

		String primaryKey = table.getPrimaryKey().getName();
		CyColumn nameColumn = table.getColumn(CyNetwork.NAME);
		String nameKey = null;
		if (nameColumn != null) nameKey = nameColumn.getName();

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Retreived "+rowList.size()+" rows:");
		for (CyRow row: rowList) {
			String message = "  Row (key:"+row.getRaw(primaryKey).toString();
			if (nameKey != null)
				message += ", name: "+row.get(nameKey, String.class)+") ";
			else
				message += ") ";
			if (listType == null) {
				try {
					row.set(column.getName(), DataUtils.convertString(value, columnType));
				} catch (NumberFormatException nfe) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
					                        "Unable to convert "+value+" to a "+DataUtils.getType(columnType));
					return;
				}
				message += "column "+column.getName()+" set to "+DataUtils.convertString(value, columnType).toString();
			} else {
				try {
					row.set(column.getName(), DataUtils.convertStringList(value, listType));
				} catch (NumberFormatException nfe) {
					taskMonitor.showMessage(TaskMonitor.Level.ERROR, 
					                        "Unable to convert "+value+" to a list of "+
					                        DataUtils.getType(listType)+"s");
					return;
				}
				message += "list column "+column.getName()+" set to "+DataUtils.convertStringList(value, listType).toString();
			}
			taskMonitor.showMessage(TaskMonitor.Level.INFO, message);
		}
	}

}
