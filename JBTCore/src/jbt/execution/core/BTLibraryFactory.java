/*
 * Copyright (C) 2012 Ricardo Juan Palma Durán
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jbt.execution.core;

import java.util.Iterator;
import java.util.List;

import jbt.execution.context.GenericBTLibrary;
import jbt.model.core.ModelTask;

/**
 * The BTLibraryFactory implements the simple factory pattern, and allows
 * clients of the framework to create instances of {@link IBTLibrary} composed
 * of behaviour trees.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTLibraryFactory {
	/**
	 * Creates a BT library that contains all the BTs contained in the libraries
	 * of <code>libraries</code>. If several trees are referenced by the same
	 * name, only the last one (according to its order in the input libraries)
	 * will remain.
	 * 
	 * @param libraries
	 *            the list with all the libraries whose BTs will contain the
	 *            returned BT library.
	 * @return a BT library that contains all the BTs contained in the libraries
	 *         of <code>libraries</code>.
	 */
	public static IBTLibrary createBTLibrary(List<IBTLibrary> libraries) {
		GenericBTLibrary result = new GenericBTLibrary();

		for (IBTLibrary library : libraries) {
			result.addBTLibrary(library);
		}

		return result;
	}

	/**
	 * Creates a BT library that contains all the behaviour trees in
	 * <code>behaviourTrees</code>. The name of the trees are specified in
	 * <code>names</code>, so, for instance, the i-th element in
	 * <code>names</code> represents the name of the i-th tree in
	 * <code>behaviourTrees</code>. If several trees are referenced by the same
	 * name, only the last one (according to its order in the input lists) will
	 * remain.
	 * 
	 * @param behaviourTrees
	 *            the list with the trees that the BT library will contain.
	 * @param names
	 *            the list with the names of the trees.
	 * @return a BT library that contains all the behaviour trees in the list
	 *         <code>behaviourTrees</code>.
	 */
	public static IBTLibrary createBTLibrary(List<ModelTask> behaviourTrees, List<String> names) {
		GenericBTLibrary result = new GenericBTLibrary();

		Iterator<ModelTask> treesIterator = behaviourTrees.iterator();
		Iterator<String> namesIterator = names.iterator();

		while (treesIterator.hasNext()) {
			result.addBT(namesIterator.next(), treesIterator.next());
		}

		return result;
	}
}
