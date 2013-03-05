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

import jbt.execution.context.BasicContext;
import jbt.model.core.ModelTask;

/**
 * The ContextFactory implements the simple factory pattern, and allows clients
 * of the framework to create instances of {@link IContext} objects that can be
 * used when running behaviour trees.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ContextFactory {
	/**
	 * Creates a new empty context (with no variables) that contains all the
	 * behaviour trees specified in <code>library</code>.
	 * 
	 * @param library
	 *            the set of behaviour trees that the returned IContext will
	 *            contain.
	 * @return a new empty context that contains all the behaviour trees
	 *         specified in <code>library</code>.
	 */
	public static IContext createContext(IBTLibrary library) {
		BasicContext result = new BasicContext();
		result.addBTLibrary(library);
		return result;
	}

	/**
	 * Creates a new empty context (with no variables) that contains all the
	 * behaviour trees in the libraries <code>libraries</code>.
	 * 
	 * @param libraries
	 *            the list of libraries whose behaviour trees this context will
	 *            contain.
	 * @return a new empty context that contains all the behaviour trees in the
	 *         libraries <code>libraries</code>.
	 */
	public static IContext createContext(List<IBTLibrary> libraries) {
		BasicContext result = new BasicContext();

		for (IBTLibrary library : libraries) {
			result.addBTLibrary(library);
		}

		return result;
	}

	/**
	 * Creates a new empty context (with no variables in it) that contains all
	 * the behaviour trees in <code>behaviourTrees</code>. The name of the trees
	 * are specified in <code>names</code>, so, for instance, the i-th element
	 * in <code>names</code> represents the name of the i-th tree in
	 * <code>behaviourTrees</code>.
	 * 
	 * @param behaviourTrees
	 *            the list with the trees that the context will contain.
	 * @param names
	 *            the list with the names of the trees.
	 * @return a new empty context that contains all the behaviour trees in the
	 *         list <code>behaviourTrees</code>.
	 */
	public static IContext createContext(List<ModelTask> behaviourTrees, List<String> names) {
		BasicContext result = new BasicContext();

		Iterator<ModelTask> treesIterator = behaviourTrees.iterator();
		Iterator<String> namesIterator = names.iterator();

		while (treesIterator.hasNext()) {
			result.addBT(namesIterator.next(), treesIterator.next());
		}

		return result;
	}

	/**
	 * Creates a new empty context (with no variables in it).
	 * 
	 * @return a new empty context.
	 */
	public static IContext createContext() {
		return new BasicContext();
	}
}
