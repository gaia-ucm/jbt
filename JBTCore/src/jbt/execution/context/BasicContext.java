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
package jbt.execution.context;

import java.util.Hashtable;
import java.util.Map;

import jbt.execution.core.IBTLibrary;
import jbt.execution.core.IContext;
import jbt.model.core.ModelTask;

/**
 * Basic implementation of the IContext interface. This class uses a Hashtable
 * to store the set of variables.
 * <p>
 * Also, since a context must contain a set of behaviour trees, this class
 * defines some methods to add behaviour trees to the context.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BasicContext implements IContext {
	/**
	 * The set of variables that the context consists of.
	 */
	private Map<String, Object> variables;
	/**
	 * The BT library that is internally used to manage all the trees of the
	 * context.
	 */
	private GenericBTLibrary library;

	/**
	 * Default constructor. Constructs an empty BasicContext.
	 */
	public BasicContext() {
		this.variables = new Hashtable<String, Object>();
		this.library = new GenericBTLibrary();
	}

	/**
	 * 
	 * @see es.ucm.bt.core.IContext#getVariable(java.lang.String)
	 */
	public Object getVariable(String name) {
		return this.variables.get(name);
	}

	/**
	 * 
	 * @see es.ucm.bt.core.IContext#setVariable(java.lang.String,
	 *      java.lang.Object)
	 */
	public boolean setVariable(String name, Object value) {
		if (value == null) {
			return this.variables.remove(name) == null ? false : true;
		}
		return this.variables.put(name, value) == null ? false : true;
	}

	/**
	 * 
	 * @see es.ucm.bt.core.IContext#clear()
	 */
	public void clear() {
		this.variables.clear();
	}

	/**
	 * 
	 * @see jbt.execution.core.IContext#clearVariable(java.lang.String)
	 */
	public boolean clearVariable(String name) {
		return this.variables.remove(name) == null ? false : true;
	}

	/**
	 * Adds all the behaviour trees in <code>library</code> to the set of
	 * behaviour trees stored in the context. If there is already a tree with
	 * the same name as that of one of the trees in <code>library</code>, it is
	 * overwritten.
	 * 
	 * @param library
	 *            the library containing all the behaviour trees to add to this
	 *            context.
	 * @return true if a previously stored behaviour tree has been overwritten,
	 *         and false otherwise.
	 */
	public boolean addBTLibrary(IBTLibrary library) {
		return this.library.addBTLibrary(library);
	}

	/**
	 * Adds the behaviour tree <code>tree</code> to the set of behaviour trees
	 * stored in the context. If there is already a tree with the name
	 * <code>name</code>, then it is overwritten by <code>tree</code>.
	 * 
	 * @param name
	 *            the name that will identify the tree <code>tree</code> in the
	 *            context.
	 * @param tree
	 *            the tree to insert.
	 * @return true if there was already a tree with name <code>name</code>, and
	 *         false otherwise.
	 */
	public boolean addBT(String name, ModelTask tree) {
		return this.library.addBT(name, tree);
	}

	/**
	 * 
	 * @see jbt.execution.core.IContext#getBT(java.lang.String)
	 */
	public ModelTask getBT(String name) {
		return this.library.getBT(name);
	}
}
