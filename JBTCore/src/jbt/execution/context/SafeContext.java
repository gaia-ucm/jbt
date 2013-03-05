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
import java.util.Map;
import java.util.Set;

import jbt.execution.core.IContext;
import jbt.model.core.ModelTask;

/**
 * The SafeContext represents a context that can be used to safely controls
 * modifications in another context (the <i>input context</i>).
 * <p>
 * A SafeContext contains an IContext (the <i>input context</i>). Initially, all
 * variables are read form the input context. However, when a variable is set or
 * cleared, its value is not modified in the input context, but it is locally
 * modified instead. From then on, the variable will be locally read instead of
 * reading if from the input context. Thus, the input context is never modified.
 * <p>
 * SafeContext can be used to situations in which an entity should use another
 * context (the input context) in read-only mode. If such entity uses a
 * SafeContext, it will not modify the input context, but on the other hand will
 * interact with the SafeContext in just the same way it would with the input
 * context.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class SafeContext implements IContext {
	/**
	 * The original input context which the SafeOutputContext is based on.
	 */
	private IContext inputContext;
	/**
	 * Flag that tells whether the SafeOutputContext has been cleared.
	 */
	private boolean cleared;
	/**
	 * The set of local variables managed by the SafeOutputContext.
	 */
	private Map<String, Object> localVariables;
	/**
	 * Set containing the names of those variables whose value has been set or
	 * cleared by the SafeOutputContext.
	 */
	private Set<String> localModifiedVariables;

	/**
	 * Constructs a SafeContext whose input context is <code>inputContext</code>
	 * .
	 * 
	 * @param inputContext
	 *            the input context.
	 */
	public SafeContext(IContext inputContext) {
		this.inputContext = inputContext;
		this.localVariables = new Hashtable<String, Object>();
		this.cleared = false;
		this.localModifiedVariables = new HashSet<String>();
	}

	/**
	 * Retrieves the value of a variable.If the variable has not been modified
	 * by the SafeContext, its value is retrieved from the input context.
	 * However, if the variable has been modified (either cleared or set), the
	 * value will be retrieved from the SafeContext.
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

	/**
	 * Sets the value of a variable. Its value is not written into the input
	 * context. Instead, its value is stored into a local variable managed by
	 * the SafeContext.
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
		if (!this.localModifiedVariables.contains(name)) {
			this.localModifiedVariables.add(name);
		}
		if (value == null) {
			return this.localVariables.remove(name) == null ? false : true;
		}
		return this.localVariables.put(name, value) == null ? false : true;
	}

	/**
	 * Clears the context. Variables are not removed from the input context, but
	 * from the set of local variables managed by the SafeContext.
	 * 
	 * @see jbt.execution.core.IContext#clear()
	 */
	public void clear() {
		this.localVariables.clear();
		this.cleared = true;
	}

	/**
	 * Clears a variable of the context. If it not removed from the input
	 * context, but from the set of local variables managed by the SafeContext.
	 * 
	 * @param name
	 *            the name of the variable to clear.
	 * @return true if a variable was actually cleared, and false in case it did
	 *         not exist.
	 * 
	 * @see jbt.execution.core.IContext#clearVariable(java.lang.String)
	 */
	public boolean clearVariable(String name) {
		if (!this.localModifiedVariables.contains(name)) {
			this.localModifiedVariables.add(name);
		}
		return this.localVariables.remove(name) == null ? false : true;
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
