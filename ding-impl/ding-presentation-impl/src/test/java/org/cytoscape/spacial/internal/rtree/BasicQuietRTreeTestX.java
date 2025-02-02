package org.cytoscape.spacial.internal.rtree;

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

import org.cytoscape.util.intr.LongBTree;
import org.cytoscape.util.intr.LongEnumerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 *
 */
public class BasicQuietRTreeTestX {
	/**
	 *  DOCUMENT ME!
	 *
	 * @param args DOCUMENT ME!
	 *
	 * @throws Exception DOCUMENT ME!
	 * @throws IllegalStateException DOCUMENT ME!
	 */
	public static void main(String[] args) throws Exception {
		RTree tree = new RTree(3);

		for (int a = 0;; a++) { // BEGIN EMPTY TREE TESTS: We run our first tests when this tree empty.

			float[] extentsArr = new float[4];
			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, extentsArr, 0, false);

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("did not expect query to generate results");

			if ((extentsArr[0] != Float.POSITIVE_INFINITY)
			    || (extentsArr[1] != Float.POSITIVE_INFINITY)
			    || (extentsArr[2] != Float.NEGATIVE_INFINITY)
			    || (extentsArr[3] != Float.NEGATIVE_INFINITY))
				throw new IllegalStateException("expected query to return inverted infinite extents");

			if (tree.exists(0, extentsArr, 0))
				throw new IllegalStateException("did not expect there to be an entry");

			if (tree.size() != 0)
				throw new IllegalStateException("tree's size() is not 0");

			if (a == 1)
				break;

			for (int j = 0; j < 1000; j++) {
				final int stop = (j + 1) * 1000;

				for (int k = j * 1000; k < stop; k++)
					tree.insert(k, k, k, k + 1, k + 1, 0.0);

				for (int k = j * 1000; k < stop; k++)
					tree.delete(k);
			}
		} // END EMPTY TREE TESTS.

		tree.insert(0, 0.0f, 0.0f, 1.0f, 1.0f, 0.0);
		tree.insert(1, 2.0f, 2.0f, 3.0f, 3.0f, 0.0);
		tree.insert(2, 0.5f, 1.0f, 1.5f, 2.0f, 0.0);

		for (int a = 0;; a++) { // BEGIN ROOT LEAF TEST: Still before any split.

			float[] extentsArr = new float[5];

			for (int i = 0; i < 3; i++)
				if (!tree.exists(i, extentsArr, 0))
					throw new IllegalStateException("entry " + i + " does not exist");

			if (tree.exists(3, extentsArr, 0))
				throw new IllegalStateException("entry 3 exits");

			if ((extentsArr[0] != 0.5) || (extentsArr[1] != 1.0) || (extentsArr[2] != 1.5)
			    || (extentsArr[3] != 2.0))
				throw new IllegalStateException("entry's extents don't match");

			if (tree.size() != 3)
				throw new IllegalStateException("tree's size() is not 3");

			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, extentsArr, 0, false);

			if (iter.numRemaining() != 3)
				throw new IllegalStateException("expected query to generate 3 hits");

			LongBTree cache = new LongBTree();

			for (long i = 0; i < 3; i++)
				cache.insert(i);

			int foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 3)
				throw new IllegalStateException("iter claimed it had 3 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != 0.0) || (extentsArr[1] != 0.0) || (extentsArr[2] != 3.0)
			    || (extentsArr[3] != 3.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.25f, 2.0f, 2.1f, 3.3f, extentsArr, 1, false);

			if (iter.numRemaining() != 2)
				throw new IllegalStateException("exptected query to return 2 hits");

			cache.insert(1);
			cache.insert(2);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 2)
				throw new IllegalStateException("iter claimed it had 2 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[1] != 0.5) || (extentsArr[2] != 1.0) || (extentsArr[3] != 3.0)
			    || (extentsArr[4] != 3.0))
				throw new IllegalStateException("extents from query wrong");

			if (a == 1)
				break;

			for (int j = 0; j < 1000; j++) {
				final int stop = ((j + 1) * 1000) + 3;

				for (int k = (j * 1000) + 3; k < stop; k++)
					tree.insert(k, -(k + 1), -(k + 1), -k, -k, 0.0);

				for (int k = (j * 1000) + 3; k < stop; k++)
					tree.delete(k);
			}
		} // END ROOT LEAF TEST.

		tree.insert(3, 2.5f, 0.5f, 3.5f, 1.5f, 0.0);

		for (int a = 0;; a++) { // BEGIN SIMPLE ROOT SPLIT TEST: Minimum # entries with a split.

			float[] extentsArr = new float[4];

			for (int i = 0; i < 4; i++)
				if (!tree.exists(i, extentsArr, 0))
					throw new IllegalStateException("entry " + i + " does not exist");

			if (tree.exists(4, extentsArr, 0))
				throw new IllegalStateException("entry 4 exists");

			if ((extentsArr[0] != 2.5) || (extentsArr[1] != 0.5) || (extentsArr[2] != 3.5)
			    || (extentsArr[3] != 1.5))
				throw new IllegalStateException("entry's extents incorrect");

			if (tree.size() != 4)
				throw new IllegalStateException("tree's size() is not 4");

			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, extentsArr, 0, false);

			if (iter.numRemaining() != 4)
				throw new IllegalStateException("expected query to generate 4 hits");

			LongBTree cache = new LongBTree();

			for (int i = 0; i < 4; i++)
				cache.insert(i);

			int foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 4)
				throw new IllegalStateException("iter claimed it had 3 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != 0.0) || (extentsArr[1] != 0.0) || (extentsArr[2] != 3.5)
			    || (extentsArr[3] != 3.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(2.0f, 0.5f, 2.2f, 1.9f, extentsArr, 0, false);

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("expected query to generate 0 hits");

			if ((extentsArr[0] != Float.POSITIVE_INFINITY)
			    || (extentsArr[1] != Float.POSITIVE_INFINITY)
			    || (extentsArr[2] != Float.NEGATIVE_INFINITY)
			    || (extentsArr[3] != Float.NEGATIVE_INFINITY))
				throw new IllegalStateException("query extents - expected inverted infinite");

			iter = tree.queryOverlap(Float.NEGATIVE_INFINITY, 1.1f, Float.POSITIVE_INFINITY, 1.2f,
			                         extentsArr, 0, false);

			if (iter.numRemaining() != 2)
				throw new IllegalStateException("expected query to generate 2 hits");

			cache.insert(2);
			cache.insert(3);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 2)
				throw new IllegalStateException("iter claimed it had 2 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("query returned wrong objKeys");

			if ((extentsArr[0] != 0.5) || (extentsArr[1] != 0.5) || (extentsArr[2] != 3.5)
			    || (extentsArr[3] != 2.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.0f, 1.0f, 1.0f, 1.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 2)
				throw new IllegalStateException("expected query to generate 2 hits");

			cache.insert(0);
			cache.insert(2);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 2)
				throw new IllegalStateException("iter claimed it had 2 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("query returned wrong objKeys");

			if ((extentsArr[0] != 0.0) || (extentsArr[1] != 0.0) || (extentsArr[2] != 1.5)
			    || (extentsArr[3] != 2.0))
				throw new IllegalStateException("extents from query wrong");

			if (a == 1)
				break;

			for (int j = 0; j < 1000; j++) {
				final int stop = ((j + 1) * 1000) + 4;

				for (int k = (j * 1000) + 4; k < stop; k++)
					tree.insert(k, k, -(k + 1), k + 3, -(k - 2), 0.0);

				for (int k = (j * 1000) + 4; k < stop; k++)
					tree.delete(k);
			}
		} // END SIMPLE ROOT SPLIT TEST.

		{ // BEGIN EXCEPTION HANDLING TEST.

			boolean exceptionCaught = false;

			try {
				tree.insert(0, 0.0f, 0.0f, 1.0f, 1.0f, 0.0);
			} catch (IllegalStateException e) {
				exceptionCaught = true;
			}

			if (!exceptionCaught)
				throw new IllegalStateException("expected exception for duplicate objKey");

			exceptionCaught = false;

			try {
				tree.insert(-1, 0.0f, 0.0f, 1.0f, 1.0f, 0.0);
			} catch (IllegalArgumentException e) {
				exceptionCaught = true;
			}

			if (!exceptionCaught)
				throw new IllegalStateException("expected exception for negative objKey");

			exceptionCaught = false;

			try {
				tree.insert(5, 1.0f, 1.0f, 0.0f, 0.0f, 0.0);
			} catch (IllegalArgumentException e) {
				exceptionCaught = true;
			}

			if (!exceptionCaught)
				throw new IllegalStateException("expected exception for min > max");
		} // END EXCEPTION HANDLING TEST.

		tree.insert(4, 3.0f, -0.25f, 4.0f, 0.75f, 0.0);
		tree.insert(5, -0.5f, 2.5f, 0.5f, 3.5f, 0.0);
		tree.insert(6, 2.75f, 2.25f, 3.75f, 3.25f, 0.0);
		tree.insert(7, 1.25f, 1.75f, 2.25f, 2.75f, 0.0);
		tree.insert(8, 1.0f, 6.0f, 2.0f, 7.0f, 0.0);
		tree.insert(9, -2.0f, 1.0f, -1.0f, 2.0f, 0.0);

		for (int a = 0;; a++) { // BEGIN DEPTH THREE TEST.

			float[] extentsArr = new float[4];

			for (int i = 9; i >= 0; i--)
				if (!tree.exists(i, extentsArr, 0))
					throw new IllegalStateException("entry " + i + " does not exist");

			if (tree.exists(-1, extentsArr, 0) || tree.exists(10, extentsArr, 0))
				throw new IllegalStateException("bad entry exists");

			if ((extentsArr[0] != 0.0) || (extentsArr[1] != 0.0) || (extentsArr[2] != 1.0)
			    || (extentsArr[3] != 1.0))
				throw new IllegalStateException("objKey 0 extents incorrect");

			if (tree.size() != 10)
				throw new IllegalStateException("tree's size() is not 10");

			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, extentsArr, 0, false);

			if (iter.numRemaining() != 10)
				throw new IllegalStateException("expected query to generate 10 hits");

			LongBTree cache = new LongBTree();

			for (int i = 0; i < 10; i++)
				cache.insert(i);

			int foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 10)
				throw new IllegalStateException("iter claimed it had 10 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -2.0) || (extentsArr[1] != -0.25) || (extentsArr[2] != 4.0)
			    || (extentsArr[3] != 7.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.0f, 1.25f, 3.0f, 5.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 5)
				throw new IllegalStateException("expected query to generate 5 hits");

			cache.insert(1);
			cache.insert(2);
			cache.insert(3);
			cache.insert(6);
			cache.insert(7);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 5)
				throw new IllegalStateException("iter claimed it had 5 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("query returned wrong objKeys");

			if ((extentsArr[0] != 0.5) || (extentsArr[1] != 0.5) || (extentsArr[2] != 3.75)
			    || (extentsArr[3] != 3.25))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(-1.5f, 0.25f, 0.25f, 3.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 3)
				throw new IllegalStateException("expected query to generate 3 hits");

			cache.insert(0);
			cache.insert(5);
			cache.insert(9);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 3)
				throw new IllegalStateException("query claimed it had 3 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("query returned wrong objKeys");

			if ((extentsArr[0] != -2.0) || (extentsArr[1] != 0.0) || (extentsArr[2] != 1.0)
			    || (extentsArr[3] != 3.5))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.5f, 6.5f, 1.5f, 6.5f, extentsArr, 0, false);

			if (iter.numRemaining() != 1)
				throw new IllegalStateException("expected query to generate 1 hit");

			cache.insert(8);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 1)
				throw new IllegalStateException("query claimed it had 1 element but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("query returned wrong objKey");

			if ((extentsArr[0] != 1.0) || (extentsArr[1] != 6.0) || (extentsArr[2] != 2.0)
			    || (extentsArr[3] != 7.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(3.0f, 5.0f, 8.0f, 9.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("did not expect any query hits");

			iter = tree.queryOverlap(-100.0f, -100.0f, -99.0f, -99.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("did not expect any query hits");

			iter = tree.queryOverlap(-1.0f, 0.75f, 3.0f, 6.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 10)
				throw new IllegalStateException("expected 10 query hits");

			if (a == 1)
				break;

			for (int j = 0; j < 1000; j++) {
				final int stop = ((j + 1) * 1000) + 10;

				for (int k = (j * 1000) + 10; k < stop; k++)
					tree.insert(k, k, k, k + 2, k + 2, 0.0);

				for (int k = (j * 1000) + 10; k < stop; k++)
					tree.delete(k);
			}
		} // END DEPTH THREE TEST.

		for (int a = 0;; a++) {
			tree.insert(10, 2.0f, 4.0f, 3.0f, 5.0f, 0.0);
			tree.insert(11, 1.5f, 3.75f, 3.5f, 4.25f, 0.0);
			tree.insert(12, 2.5f, 3.5f, 3.0f, 5.5f, 0.0);
			tree.insert(13, -4.0f, 6.0f, -2.0f, 8.0f, 0.0);
			tree.insert(14, -4.25f, 5.75f, 2.25f, 8.25f, 0.0);
			tree.insert(15, 2.0f, -1.0f, 2.0f, -1.0f, 0.0);
			tree.insert(16, -1.25f, 0.5f, -1.25f, 3.0f, 0.0);
			tree.insert(17, -0.5f, -0.5f, 1.5f, 0.5f, 0.0);
			tree.insert(18, 0.25f, 4.0f, 1.25f, 5.0f, 0.0);
			tree.insert(19, 4.0f, 1.0f, 5.0f, 2.0f, 0.0);
			tree.insert(20, 4.0f, 3.0f, 5.0f, 4.0f, 0.0);
			tree.insert(21, 4.25f, -1.5f, 4.75f, 5.0f, 0.0);
			tree.insert(22, 3.0f, -1.75f, 5.0f, -1.0f, 0.0);
			tree.insert(23, 1.25f, 0.25f, 2.25f, 1.25f, 0.0);
			tree.insert(24, -2.0f, 9.0f, -1.0f, 10.0f, 0.0);
			tree.insert(25, 1.0f, 9.0f, 2.0f, 10.0f, 0.0);
			tree.insert(26, -2.0f, 5.0f, -1.0f, 6.0f, 0.0);
			tree.insert(27, -2.5f, 5.25f, -1.75f, 9.25f, 0.0);

			if (a == 1)
				break;

			for (int k = 10; k < 28; k++)
				tree.delete(k);
		}
		// There are now 28 entries in the R-tree.  Depth must be at least 4.
		{ // BEGIN SERIALIZATION TEST.

			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
			objOut.writeObject(tree);
			objOut.flush();
			objOut.close();

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream objIn = new ObjectInputStream(byteIn);
			tree = (RTree) objIn.readObject();
			objIn.close();
		} // END SERIALIZATION TEST.

		for (int a = 0;; a++) { // BEGIN DEPTH FOUR TEST.

			float[] extentsArr = new float[4];

			for (int i = 27; i >= 0; i--)
				if (!tree.exists(i, extentsArr, 0))
					throw new IllegalStateException("entry " + i + " does not exist");

			if (tree.exists(28, extentsArr, 0) || tree.exists(Integer.MAX_VALUE, extentsArr, 0)
			    || tree.exists(Integer.MIN_VALUE, extentsArr, 0))
				throw new IllegalStateException("bad entry exists");

			if ((extentsArr[0] != 0.0) || (extentsArr[1] != 0.0) || (extentsArr[2] != 1.0)
			    || (extentsArr[3] != 1.0))
				throw new IllegalStateException("objKey 0 extents incorrect");

			if (tree.size() != 28)
				throw new IllegalStateException("tree's size() is not 28");

			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, extentsArr, 0, false);

			if (iter.numRemaining() != 28)
				throw new IllegalStateException("expected query to give 28 hits");

			LongBTree cache = new LongBTree();

			for (int i = 0; i < 28; i++)
				cache.insert(i);

			int foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 28)
				throw new IllegalStateException("iter claimed it had 28 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -4.25) || (extentsArr[1] != -1.75) || (extentsArr[2] != 5.0)
			    || (extentsArr[3] != 10.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(-2.0f, 6.0f, -2.0f, 6.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 4)
				throw new IllegalStateException("expected query to generate 4 hits");

			cache.insert(13);
			cache.insert(14);
			cache.insert(26);
			cache.insert(27);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 4)
				throw new IllegalStateException("iter claimed it had 4 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -4.25) || (extentsArr[1] != 5.0) || (extentsArr[2] != 2.25)
			    || (extentsArr[3] != 9.25))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(2.5f, 3.75f, 6.0f, 6.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 5)
				throw new IllegalStateException("expected query to generate 5 hits");

			cache.insert(10);
			cache.insert(11);
			cache.insert(12);
			cache.insert(20);
			cache.insert(21);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 5)
				throw new IllegalStateException("iter claimed it had 5 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != 1.5) || (extentsArr[1] != -1.5) || (extentsArr[2] != 5.0)
			    || (extentsArr[3] != 5.5))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.75f, -1.5f, 3.25f, -0.5f, extentsArr, 0, false);

			if (iter.numRemaining() != 2)
				throw new IllegalStateException("expected query to generate 2 hits");

			cache.insert(15);
			cache.insert(22);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 2)
				throw new IllegalStateException("iter claimed it had 2 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != 2.0) || (extentsArr[1] != -1.75) || (extentsArr[2] != 5.0)
			    || (extentsArr[3] != -1.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(-3.0f, -5.0f, 7.0f, 2.75f, extentsArr, 0, false);

			if (iter.numRemaining() != 16)
				throw new IllegalStateException("expected query to generate 16 hits");

			cache.insert(0);
			cache.insert(1);
			cache.insert(2);
			cache.insert(3);
			cache.insert(4);
			cache.insert(5);
			cache.insert(6);
			cache.insert(7);
			cache.insert(9);
			cache.insert(15);
			cache.insert(16);
			cache.insert(17);
			cache.insert(19);
			cache.insert(21);
			cache.insert(22);
			cache.insert(23);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 16)
				throw new IllegalStateException("iter claimed it had 16 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -2.0) || (extentsArr[1] != -1.75) || (extentsArr[2] != 5.0)
			    || (extentsArr[3] != 5.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.0f, 4.5f, 3.0f, 6.25f, extentsArr, 0, false);

			if (iter.numRemaining() != 5)
				throw new IllegalStateException("expected query to generate 5 hits");

			cache.insert(8);
			cache.insert(10);
			cache.insert(12);
			cache.insert(14);
			cache.insert(18);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 5)
				throw new IllegalStateException("iter claimed it had 5 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -4.25) || (extentsArr[1] != 3.5) || (extentsArr[2] != 3.0)
			    || (extentsArr[3] != 8.25))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(0.75f, 0.25f, 1.5f, 0.75f, extentsArr, 0, false);

			if (iter.numRemaining() != 3)
				throw new IllegalStateException("expected query to generate 3 hits");

			cache.insert(0);
			cache.insert(17);
			cache.insert(23);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 3)
				throw new IllegalStateException("iter claimed it had 3 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -0.5) || (extentsArr[1] != -0.5) || (extentsArr[2] != 2.25)
			    || (extentsArr[3] != 1.25))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(1.75f, -10.0f, 1.75f, 30.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 6)
				throw new IllegalStateException("expected query to generate 6 hits");

			cache.insert(7);
			cache.insert(8);
			cache.insert(11);
			cache.insert(14);
			cache.insert(23);
			cache.insert(25);
			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 6)
				throw new IllegalStateException("iter claimed it had 6 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -4.25) || (extentsArr[1] != 0.25) || (extentsArr[2] != 3.5)
			    || (extentsArr[3] != 10.0))
				throw new IllegalStateException("extents from query wrong");

			iter = tree.queryOverlap(0.75f, 3.0f, 1.75f, 3.5f, extentsArr, 0, false);

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("did not expect query results");

			iter = tree.queryOverlap(2.5f, 1.75f, 3.75f, 1.75f, null, -1, false);

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("did not expect query results");

			iter = tree.queryOverlap(-2.0f, -1.0f, 4.25f, 9.0f, extentsArr, 0, false);

			if (iter.numRemaining() != 28)
				throw new IllegalStateException("expected 28 (all) query hits");

			for (int i = 0; i < 28; i++)
				cache.insert(i);

			foo = 0;

			while (iter.numRemaining() > 0) {
				cache.delete(iter.nextLong());
				foo++;
			}

			if (foo != 28)
				throw new IllegalStateException("iter claimed it had 28 elements but really didn't");

			if (cache.size() != 0)
				throw new IllegalStateException("iter returned wrong objKeys");

			if ((extentsArr[0] != -4.25) || (extentsArr[1] != -1.75) || (extentsArr[2] != 5.0)
			    || (extentsArr[3] != 10.0))
				throw new IllegalStateException("extents from query wrong");

			if (a == 1)
				break;

			for (int j = 0; j < 1000; j++) {
				final int stop = ((j + 1) * 1000) + 28;

				for (int k = (j * 1000) + 28; k < stop; k++)
					tree.insert(k, k, k, k + 5, k + 5, 0.0);

				for (int k = (j * 1000) + 28; k < stop; k++)
					tree.delete(k);
			}
		} // END DEPTH FOUR TEST.

		{ // BEGIN ORDER-PRESERVING SUBQUERY TEST.

			if (tree.size() != 28)
				throw new IllegalStateException("expected 28 elements in tree");

			final long[] allOrdered = new long[28];
			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, null, 0, false);

			if (iter.numRemaining() != 28)
				throw new IllegalStateException("expected 28 elements in iteration");

			for (int i = 0; i < 28; i++)
				allOrdered[i] = iter.nextLong();

			iter = tree.queryOverlap(0.0f, 0.0f, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
			                         null, 0, false);

			if (iter.numRemaining() != 20)
				throw new IllegalStateException("expected 20 elements in iteration");

			int prevInx = -1;

			for (int i = 0; i < 20; i++) {
				final long element = iter.nextLong();
				int foundInx = -2;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx > prevInx)) {
					throw new IllegalStateException("order in subquery not preserved");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");

			iter = tree.queryOverlap(-10.0f, -5.0f, 1.0f, 4.0f, null, 0, false);

			if (iter.numRemaining() != 7)
				throw new IllegalStateException("expected 7 elements in iteration");

			prevInx = -1;

			for (int i = 0; i < 7; i++) {
				final long element = iter.nextLong();
				int foundInx = -2;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx > prevInx)) {
					throw new IllegalStateException("order in subquery not preserved");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");

			iter = tree.queryOverlap(-99.0f, -5.0f, 1.0f, 30.0f, null, 0, false);

			if (iter.numRemaining() != 14)
				throw new IllegalStateException("expected 14 elements in iteration");

			prevInx = -1;

			for (int i = 0; i < 14; i++) {
				final long element = iter.nextLong();
				int foundInx = -2;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx > prevInx)) {
					throw new IllegalStateException("order in subquery not preserved");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");

			iter = tree.queryOverlap(-3.0f, 4.0f, 10.0f, 20.0f, null, 0, false);

			if (iter.numRemaining() != 13)
				throw new IllegalStateException("expected 13 elements in iteration");

			prevInx = -1;

			for (int i = 0; i < 13; i++) {
				final long element = iter.nextLong();
				int foundInx = -2;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx > prevInx)) {
					throw new IllegalStateException("order in subquery not preserved");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");
		} // END ORDER-PRESERVING SUBQUERY TEST.

		{ // BEGIN REVERSE QUERY TEST.

			if (tree.size() != 28)
				throw new IllegalStateException("expected 28 elements in tree");

			final long[] allOrdered = new long[28];
			LongEnumerator iter = tree.queryOverlap(Float.NEGATIVE_INFINITY,
			                                       Float.NEGATIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY,
			                                       Float.POSITIVE_INFINITY, null, 0, false);

			if (iter.numRemaining() != 28)
				throw new IllegalStateException("expected 28 elements in iteration");

			for (int i = 0; i < 28; i++)
				allOrdered[i] = iter.nextLong();

			iter = tree.queryOverlap(0.0f, 0.0f, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
			                         null, 0, true);

			if (iter.numRemaining() != 20)
				throw new IllegalStateException("expected 20 elements in iteration");

			int prevInx = Integer.MAX_VALUE - 1;

			for (int i = 0; i < 20; i++) {
				final long element = iter.nextLong();
				int foundInx = Integer.MAX_VALUE;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx < prevInx)) {
					throw new IllegalStateException("not reverse order");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");

			iter = tree.queryOverlap(-10.0f, -5.0f, 1.0f, 4.0f, null, 0, true);

			if (iter.numRemaining() != 7)
				throw new IllegalStateException("expected 7 elements in iteration");

			prevInx = Integer.MAX_VALUE - 1;

			for (int i = 0; i < 7; i++) {
				final long element = iter.nextLong();
				int foundInx = Integer.MAX_VALUE;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx < prevInx)) {
					throw new IllegalStateException("not reverse order");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");

			iter = tree.queryOverlap(-99.0f, -5.0f, 1.0f, 30.0f, null, 0, true);

			if (iter.numRemaining() != 14)
				throw new IllegalStateException("expected 14 elements in iteration");

			prevInx = Integer.MAX_VALUE - 1;

			for (int i = 0; i < 14; i++) {
				final long element = iter.nextLong();
				int foundInx = Integer.MAX_VALUE;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx < prevInx)) {
					throw new IllegalStateException("not reverse order");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");

			iter = tree.queryOverlap(-3.0f, 4.0f, 10.0f, 20.0f, null, 0, true);

			if (iter.numRemaining() != 13)
				throw new IllegalStateException("expected 13 elements in iteration");

			prevInx = Integer.MAX_VALUE - 1;

			for (int i = 0; i < 13; i++) {
				final long element = iter.nextLong();
				int foundInx = Integer.MAX_VALUE;

				for (int j = 0;; j++) {
					if (allOrdered[j] == element) {
						foundInx = j;

						break;
					}
				}

				if (!(foundInx < prevInx)) {
					throw new IllegalStateException("not reverse order");
				}

				prevInx = foundInx;
			}

			if (iter.numRemaining() != 0)
				throw new IllegalStateException("more elements remain in iteration");
		} // END REVERSE QUERY TEST.
	}
}
