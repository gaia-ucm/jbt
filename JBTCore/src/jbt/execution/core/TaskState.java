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

import java.util.Hashtable;
import java.util.Map;

/**
 * Default implementation of the {@link ITaskState} interface. It provides
 * methods for modifying the set of variables stored by the TaskState.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class TaskState implements ITaskState {
	/** The set of variables. */
	private Map<String, Object> variables;

	/**
	 * Constructs an empty TaskState.
	 */
	public TaskState() {
		this.variables = new Hashtable<String, Object>();
	}

	/**
	 * 
	 * @see jbt.execution.core.ITaskState#getStateVariable(java.lang.String)
	 */
	public Object getStateVariable(String name) {
		return this.variables.get(name);
	}

	/**
	 * Sets the value of a variable. If the value is null, the variable is
	 * cleared.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @param value
	 *            the value of the variable.
	 * @return true if there was a variable with name <code>name</code> before
	 *         calling this method (it is therefore been overwritten), and false
	 *         otherwise.
	 */
	public boolean setStateVariable(String name, Object value) {
		if (value == null) {
			return this.variables.remove(name) == null ? false : true;
		}
		return this.variables.put(name, value) == null ? false : true;
	}

	/**
	 * Clears all the variables of the TaskState.
	 */
	public void clear() {
		this.variables.clear();
	}

	/**
	 * Clears the value of a variable.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return true if the variable existed before calling this method, and
	 *         false otherwise.
	 */
	public boolean clearStateVariable(String name) {
		return this.variables.remove(name) == null ? false : true;
	}

}
