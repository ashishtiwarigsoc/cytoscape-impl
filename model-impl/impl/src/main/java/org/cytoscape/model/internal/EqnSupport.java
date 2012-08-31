/*
 Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.model.internal;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cytoscape.equations.Equation;
import org.cytoscape.equations.IdentDescriptor;
import org.cytoscape.equations.Interpreter;

import org.cytoscape.model.internal.tsort.TopoGraphNode;
import org.cytoscape.model.internal.tsort.TopologicalSort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class EqnSupport {
	private static final Logger logger = LoggerFactory.getLogger(EqnSupport.class);

	private EqnSupport() { } // Don't ever create an instance of this class!

	static boolean scalarEquationIsCompatible(final Object equationCandidate,
						  final Class targetType)
	{
		if (!(equationCandidate instanceof Equation))
			return false;

		final Equation equation = (Equation)equationCandidate;
		final Class<?> eqnReturnType = equation.getType();

		if (targetType == Double.class || targetType == Boolean.class
		    || targetType == Integer.class || targetType == Long.class)
			return eqnReturnType == Double.class || eqnReturnType == Long.class
			       || eqnReturnType == Boolean.class;
		else if (targetType == String.class)
			return true; // Everything can be turned into a String!
		else
			return false;
	}

	static boolean listEquationIsCompatible(final Equation equation, final Class listElementType)
	{
//		final Class<?> eqnReturnType = equation.getType();
//		if (eqnReturnType == BooleanList.class)
//			return listElementType == Boolean.class;
//		if (eqnReturnType == DoubleList.class)
//			return listElementType == Double.class;
//		if (eqnReturnType == StringList.class)
//			return listElementType == String.class;
//		if (eqnReturnType == LongList.class)
//			return listElementType == Long.class;
		// TODO: Add support for a hypothetical IntegerList type.

		return false;
	}

	static Object convertEqnResultToColumnType(final Class<?> columnType, final Object result) {
		final Class<?> resultType = result.getClass();
		if (resultType == columnType)
			return result;

		if (columnType == String.class)
			return result.toString();

		if (columnType == Double.class && resultType == Long.class)
			return (double)(Long)result;

		if (columnType == Boolean.class && resultType == Long.class)
			return (Long)result == 0 ? Boolean.FALSE : Boolean.TRUE;

		if (columnType == Boolean.class && resultType == Double.class)
			return (Double)result == 0.0 ? Boolean.FALSE : Boolean.TRUE;

		if (columnType == Integer.class && resultType == Double.class)
			return ((Double)result).intValue();

		if (columnType == Integer.class && resultType == Long.class)
			return ((Long)result).intValue();

		throw new IllegalArgumentException("unexpected equation result type " + resultType
						   + " for a column of type " + columnType + ".");
	}

	static Object evalEquation(final Equation equation, final Object key,
				   final Interpreter interpreter,
				   final Set<String> currentlyActiveAttributes,
				   final String columnName, final Appendable lastInternalError,
				   final CyTableImpl tableImpl)
	{
		if (currentlyActiveAttributes.contains(columnName)) {
			currentlyActiveAttributes.clear();
			try {
				lastInternalError.append("Recursive equation evaluation of \"" + columnName + "\".");
			} catch (Exception e) {
				// Intentionally empty!
			}
			return null;
		} else
			currentlyActiveAttributes.add(columnName);

		final Collection<String> attribReferences = equation.getVariableReferences();
		final Map<String, Object> defaultValues = equation.getDefaultVariableValues();

		final Map<String, IdentDescriptor> nameToDescriptorMap = new TreeMap<String, IdentDescriptor>();
		for (final String attribRef : attribReferences) {
			if (attribRef.equals("ID")) {
				nameToDescriptorMap.put("ID", new IdentDescriptor(key));
				continue;
			}

			Object attribValue = tableImpl.getValue(key, attribRef);
			if (attribValue == null) {
				final Object defaultValue = defaultValues.get(attribRef);
				if (defaultValue != null)
					attribValue = defaultValue;
				else {
					currentlyActiveAttributes.clear();
					try {
						lastInternalError.append(
							"Missing value for referenced attribute \""
							+ attribRef + "\".");
					} catch (Exception e) {
						// Intentionally empty!
					}
					logger.warn("Missing value for \"" + attribRef
					            + "\" while evaluating an equation (ID:" + key
					            + ", attribute name:" + columnName + ")");
					return null;
				}
			}

			try {
				nameToDescriptorMap.put(attribRef, new IdentDescriptor(attribValue));
			} catch (final Exception e) {
				currentlyActiveAttributes.clear();
				try {
					lastInternalError.append("Bad attribute reference to \""
								 + attribRef + "\".");
				} catch (Exception e2) {
					// Intentionally empty!
				}
				logger.warn("Bad attribute reference to \"" + attribRef
				            + "\" while evaluating an equation (ID:" + key
				            + ", attribute name:" + columnName + ")");
				return null;
			}
		}

		try {
			final Object result = interpreter.execute(equation, nameToDescriptorMap);
			currentlyActiveAttributes.remove(columnName);
			return result;
		} catch (final Exception e) {
			currentlyActiveAttributes.clear();
			try {
				lastInternalError.append(e.getMessage());
			} catch (Exception e2) {
				// Intentionally empty!
			}
			logger.warn("Error while evaluating an equation: " + e.getMessage() + " (ID:"
			            + key + ", attribute name:" + columnName + ")");
			return null;
		}
	}

	/**
	 *  @return an in-order list of attribute names that will have to be evaluated before "columnName" can be evaluated
	 */
	private List<String> topoSortAttribReferences(final Object key, final String columnName,
						      final CyTableImpl tableImpl)
	{
		final Object equationCandidate = tableImpl.getValueOrEquation(key, columnName);
		if (!(equationCandidate instanceof Equation))
			return new ArrayList<String>();

		final Equation equation = (Equation)equationCandidate;
		final Set<String> attribReferences = equation.getVariableReferences();
		if (attribReferences.size() == 0)
			return new ArrayList<String>();

		final Set<String> alreadyProcessed = new TreeSet<String>();
		alreadyProcessed.add(columnName);
		final List<TopoGraphNode> dependencies = new ArrayList<TopoGraphNode>();
		for (final String attribReference : attribReferences)
                        followReferences(key, attribReference, alreadyProcessed, dependencies,
					 tableImpl);


		final List<TopoGraphNode> topoOrder = TopologicalSort.sort(dependencies);
		final List<String> retVal = new ArrayList<String>();
		for (final TopoGraphNode node : topoOrder) {
			final AttribTopoGraphNode attribTopoGraphNode = (AttribTopoGraphNode)node;
			final String nodeName = attribTopoGraphNode.getNodeName();
			if (nodeName.equals(columnName))
				return retVal;
			else
				retVal.add(nodeName);
		}

		// We should never get here because "columnName" should have been found in the for-loop above!
		throw new IllegalStateException("\"" + columnName
		                                + "\" was not found in the toplogical order, which should be impossible.");
	}

	/**
	 *  Helper function for topoSortAttribReferences() performing a depth-first search of equation evaluation dependencies.
	 */
	private static void followReferences(final Object key, final String columnName,
					     final Collection<String> alreadyProcessed,
					     final Collection<TopoGraphNode> dependencies,
					     final CyTableImpl tableImpl)
	{
		// Already visited this attribute?
		if (alreadyProcessed.contains(columnName))
			return;

		alreadyProcessed.add(columnName);
		final Object equationCandidate = tableImpl.getValueOrEquation(key, columnName);
		if (!(equationCandidate instanceof Equation))
			return;

		final Equation equation = (Equation)equationCandidate;
		final Set<String> attribReferences = equation.getVariableReferences();
		for (final String attribReference : attribReferences)
			followReferences(key, attribReference, alreadyProcessed, dependencies, tableImpl);
	}

	/**
	 *  @return "x" truncated using Excel's notion of truncation.
	 */
	private static double excelTrunc(final double x) {
		final boolean isNegative = x < 0.0;
		return Math.round(x + (isNegative ? +0.5 : -0.5));
	}

	/**
	 *  @return "d" converted to an Integer using Excel rules, should the number be outside the
	 *          range of an int, null will be returned
	 */
	private static Integer doubleToInteger(final double d) {
		if (d > Integer.MAX_VALUE || d < Integer.MIN_VALUE)
			return null;

		double x = ((Double)d).intValue();
		if (x != d && x < 0.0)
			--x;

		return (Integer)(int)x;
	}

	/**
	 *  @return "l" converted to an Integer using Excel rules, should the number be outside the
	 *          range of an int, null will be returned
	 */
	private static Integer longToInteger(final long l) {
		if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE)
			return (Integer)(int)l;

		return null;
	}

	/**
	 *  @return "equationValue" interpreted according to Excel rules as an integer or null if
	 *          that is not possible
	 */
	private static Integer convertEqnRetValToInteger(final String id, final String columnName,
							 final Object equationValue)
	{
		if (equationValue.getClass() == Double.class) {
			final Integer retVal = doubleToInteger((Double)equationValue);
			if (retVal == null)
				logger.warn("Cannot convert a floating point value ("
					    + equationValue + ") to an integer.  (ID:" + id
					    + ", attribute name:" + columnName + ")");
			return retVal;
		}
		else if (equationValue.getClass() == Long.class) {
			final Integer retVal = longToInteger((Long)equationValue);
			if (retVal == null)
				logger.warn("Cannot convert a large integer (long) value ("
					    + equationValue + ") to an integer. (ID:" + id
					    + ", attribute name:" + columnName + ")");
			return retVal;
		}
		else if (equationValue.getClass() == Boolean.class) {
			final Boolean boolValue = (Boolean)equationValue;
			return (Integer)(boolValue ? 1 : 0);
		}
		else
			throw new IllegalStateException("we should never get here.");
	}

	/**
	 *  @return "equationValue" interpreted according to Excel rules as a double or null if that is not possible
	 */
	private static Double convertEqnRetValToDouble(final String id, final String columnName,
						       final Object equationValue)
	{
		if (equationValue.getClass() == Double.class)
			return (Double)equationValue;
		else if (equationValue.getClass() == Long.class)
			return (double)(Long)(equationValue);
		else if (equationValue.getClass() == Boolean.class) {
			final Boolean boolValue = (Boolean)equationValue;
			return boolValue ? 1.0 : 0.0;
		}
		else if (equationValue.getClass() == String.class) {
			final String valueAsString = (String)equationValue;
			try {
				return Double.parseDouble(valueAsString);
			} catch (final NumberFormatException e) {
				logger.warn("Cannot convert a string (\"" + valueAsString
				            + "\") to a floating point value. (ID:" + id
                                            + ", attribute name:" + columnName + ")");
				return null;
			}
		}
		else
			throw new IllegalStateException("we should never get here.");
	}

	/**
	 *  @return "equationValue" interpreted according to Excel rules as a boolean
	 */
	private static Boolean convertEqnRetValToBoolean(final String id, final String columnName,
							 final Object equationValue)
	{
		if (equationValue.getClass() == Double.class)
			return (Double)equationValue != 0.0;
		else if (equationValue.getClass() == Long.class)
			return (Long)(equationValue) != 0L;
		else if (equationValue.getClass() == Boolean.class) {
			return (Boolean)equationValue;
		}
		else if (equationValue.getClass() == String.class) {
			final String stringValue = (String)equationValue;
			if (stringValue.compareToIgnoreCase("true") == 0)
				return true;
			else if (stringValue.compareToIgnoreCase("false") == 0)
				return false;
			else {
				logger.warn("Cannot convert a string (\"" + stringValue
				            + "\") to a boolean value. (ID:" + id
                                            + ", attribute name:" + columnName + ")");
				return null;
			}
		}
		else
			throw new IllegalStateException("we should never get here.");
	}
}