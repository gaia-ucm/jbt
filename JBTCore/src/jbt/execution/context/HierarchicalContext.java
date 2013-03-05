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

import jbt.execution.core.IContext;

/**
 * A HierarchicalContext is a context that stores a parent IContext to fall back
 * to when it cannot find a particular variable in its own set of variables.
 * This class just redefines the method {@link #getVariable(String)} so that if
 * the variable name cannot be found, its value is retrieved from the parent
 * context.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class HierarchicalContext extends BasicContext {
	/**
	 * The parent context. When a variable cannot be retrieved from the current
	 * context, it will be looked up in the parent context.
	 */
	private IContext parent;

	/**
	 * Default constructor. Builds an empty HierarchicalContext, with no parent
	 * context.
	 */
	public HierarchicalContext() {
		super();
	}

	/**
	 * Sets the parent context of this HierarchicalContext. May be null, in
	 * which case no parent context will be used.
	 * 
	 * @param parent
	 *            the parent context, which may be null.
	 */
	public void setParent(IContext parent) {
		this.parent = parent;
	}

	/**
	 * Returns the value of a variable whose name is <code>name</code>. If a
	 * variable with such a name cannot be found in the current context, and
	 * there is a parent context set, the variable will be looked up in the
	 * parent context, and its value returned. If it cannot be found in the
	 * parent context (or in any other parent contexts through recursion), null
	 * is returned.
	 * 
	 * @see es.ucm.bt.context.BasicContext#getVariable(java.lang.String)
	 */
	public Object getVariable(String name) {
		Object result;

		result = super.getVariable(name);

		if (result == null) {
			if (this.parent != null) {
				result = this.parent.getVariable(name);
			}
		}

		return result;
	}
}
