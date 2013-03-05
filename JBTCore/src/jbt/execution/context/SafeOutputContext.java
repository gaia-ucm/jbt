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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jbt.execution.core.IContext;
import jbt.model.core.ModelTask;

/**
 * The SafeOutputContext represents a context that can be used to safely
 * controls modifications in another context (the <i>input context</i>).
 * <p>
 * A SafeOutputContext contains an IContext (the <i>input context</i>), and a
 * list of <i>output variables</i>. These are the variables that can be written
 * into the input context. The rest of variables are stored locally in the
 * SafeOutputContext.
 * <p>
 * Thus, when the SafeOutputContext sets the value of a variable, it will
 * normally set its value in a local variable. However, if the variable is one
 * of the list of output variables, the value will be set in the input context.
 * <p>
 * When retrieving variables, a variable in the list of output variables will
 * always be retrieved from the input context. A variable that is not in the
 * list of output variables will also be retrieved from the input context;
 * however, when such variable is modified (either its value is changed or
 * cleared), the value will be retrieved from the SafeOutputContext (that is,
 * from the moment a variable that is not in the list of output variables is
 * modified, it is managed locally).
 * <p>
 * With respect to clearing variables, output variables are always cleared in
 * the input context. However, since only output variables can be modified in
 * the input context, any other variable will be cleared in the
 * SafeOutputContext.
 * <p>
 * The SafeOutputContext can be used in situations where an entity must use a
 * context (the input context) in a soft-read-only mode. By using the
 * SafeOutputContext, such entity will only be able to modify the output
 * variables in the input context. On the other hand, it will interact with the
 * SafeOutputContext in just the same way it would with the input context.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class SafeOutputContext implements IContext {
	/**
	 * The original input context which the SafeOutputContext is based on.
	 */
	private IContext inputContext;
	/**
	 * The list of output variables. These variables can be written into the
	 * {@link #inputContext}, unlike the rest, that are stored in
	 * {@link #localVariables}.
	 */
	private List<String> outputVariables;
	/**
	 * Set containing the names of those non-output variables whose value has
	 * been set or cleared by the SafeOutputContext.
	 */
	private Set<String> localModifiedVariables;
	/**
	 * Flag that tells whether the SafeOutputContext has been cleared.
	 */
	private boolean cleared;
	/**
	 * The set of local variables managed by the SafeOutputContext.
	 */
	private Map<String, Object> localVariables;

	/**
	 * Constructs a SafeOutputContext whose input context is
	 * <code>inputContext</code> and whose list of output variables is
	 * <code>outputVariables</code>.
	 * 
	 * @param inputContext
	 *            the input context.
	 * @param outputVariables
	 *            the list of output variables.
	 */
	public SafeOutputContext(IContext inputContext, List<String> outputVariables) {
		this.inputContext = inputContext;
		this.outputVariables = outputVariables;
		this.localVariables = new Hashtable<String, Object>();
		this.localModifiedVariables = new HashSet<String>();
		this.cleared = false;
	}

	/**
	 * Retrieves the value of a variable. If it is an output variable, its value
	 * is retrieved from the input context. Otherwise, if the variable has not
	 * been modified by the SafeOutputContext, its value is also retrieved from
	 * the input context. However, if the variable has been modified (either
	 * cleared or set), the value will be retrieved from the SafeOutputContext.
	 * 
	 * @param name
	 *            the name of the variable to retrieve.
	 * 
	 * @return the value of a variable whose name is <code>name</code>, or null
	 *         if it does not exist.
	 * 
	 * @see jbt.execution.core.IContext#getVariable(java.lang.String)
	 */
	public Object getVariable(String name) {
		if (this.outputVariables.contains(name)) {
			return this.inputContext.getVariable(name);
		} else {
			if (this.localModifiedVariables.contains(name) || this.cleared) {
				return this.localVariables.get(name);
			} else {
				Object variable = this.localVariables.get(name);
				if (variable != null) {
					return variable;
				} else {
					return this.inputContext.getVariable(name);
				}
			}
		}
	}

	/**
	 * Sets the value of a variable. If it is an output variable, its value is
	 * written into the input context. Otherwise, its value will be stored into
	 * a local variable managed by the SafeOutputContext.
	 * 
	 * @param name
	 *            the name of the variable.
	 * @param value
	 *            the value for the variable.
	 * @return true if a variable with the same name already existed, and false
	 *         otherwise.
	 * 
	 * @see jbt.execution.core.IContext#setVariable(java.lang.String,
	 *      java.lang.Object)
	 */
	public boolean setVariable(String name, Object value) {
		if (this.outputVariables.contains(name)) {
			return this.inputContext.setVariable(name, value);
		} else {
			if (!this.localModifiedVariables.contains(name)) {
				this.localModifiedVariables.add(name);
			}
			if (value == null) {
				return this.localVariables.remove(name) == null ? false : true;
			}
			return this.localVariables.put(name, value) == null ? false : true;
		}
	}

	/**
	 * Clears the context. Output variables are cleared in the input context.
	 * The rest are removed from the set of local variables managed by the
	 * SafeOutputContext.
	 * 
	 * @see jbt.execution.core.IContext#clear()
	 */
	public void clear() {
		this.localVariables.clear();
		for (String outputVariable : this.outputVariables) {
			this.inputContext.clearVariable(outputVariable);
		}
		this.cleared = true;
	}

	/**
	 * Clears a variable of the context. If it is an output variable, the value
	 * is cleared in the input context. Otherwise, the variable is removed from
	 * the set of local variables managed by the SafeOutputContext.
	 * 
	 * @param name
	 *            the name of the variable to clear.
	 * @return true if a variable was actually cleared, and false in case it did
	 *         not exist.
	 * 
	 * @see jbt.execution.core.IContext#clearVariable(java.lang.String)
	 */
	public boolean clearVariable(String name) {
		if (this.outputVariables.contains(name)) {
			return this.inputContext.clearVariable(name);
		} else {
			if (!this.localModifiedVariables.contains(name)) {
				this.localModifiedVariables.add(name);
			}
			return this.localVariables.remove(name) == null ? false : true;
		}
	}

	/**
	 * Returns the behaviour tree of a particular name, or null in case it
	 * cannot be found. The behaviour tree is extracted from the input context
	 * passed at construction time.
	 * 
	 * @param the
	 *            name of the behaviour tree to retrieve.
	 * @return the behaviour tree, or null if it cannot be found.
	 * 
	 * @see jbt.execution.core.IContext#getBT(java.lang.String)
	 */
	public ModelTask getBT(String name) {
		return this.inputContext.getBT(name);
	}
}
