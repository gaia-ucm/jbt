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

import jbt.model.core.ModelTask;
import jbt.util.Pair;

/**
 * Common interface for all behaviour tree libraries. A behaviour tree library
 * is just a repository from which behaviour trees can be retrieved by name.
 * <p>
 * This is an <i>iterable</i> interface (it extends {@link Iterable}) so that
 * all the behaviour trees of the library can be easily accessed.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public interface IBTLibrary extends Iterable<Pair<String, ModelTask>> {
	/**
	 * Returns the behaviour tree whose name is <code>name</code>. This method
	 * returns the root task of the tree.
	 * 
	 * @param name
	 *            the name of the tree to retrieve.
	 * @return the behaviour tree whose name is <code>name</code>, or null in
	 *         case it does not exist.
	 */
	public ModelTask getBT(String name);
}
