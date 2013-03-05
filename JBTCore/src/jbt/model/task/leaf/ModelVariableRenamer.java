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
package jbt.model.task.leaf;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.leaf.ExecutionVariableRenamer;
import jbt.model.core.ModelTask;

/**
 * A ModelVariableRenamer is a task that renames a variable of the context. This
 * task just takes one variable of the context and changes its name.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelVariableRenamer extends ModelLeaf {
	/** The name of the variable that must be renamed. */
	private String variableName;
	/** The new name for the variable that must be renamed. */
	private String newVariableName;

	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the task, which may be null.
	 * @param variableName
	 *            the name of the variable to rename.
	 * @param newVariableName
	 *            the new name for the variable.
	 */
	public ModelVariableRenamer(ModelTask guard, String variableName, String newVariableName) {
		super(guard);
		this.variableName = variableName;
		this.newVariableName = newVariableName;
	}

	/**
	 * Returns a new {@link ExecutionVariableRenamer} that knows how to run this
	 * ModelVariableRenamer.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      jbt.execution.core.ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionVariableRenamer(this, executor, parent);
	}

	/**
	 * Returns the name of the variable to rename.
	 * 
	 * @return the name of the variable to rename.
	 */
	public String getVariableName() {
		return this.variableName;
	}

	/**
	 * Returns the new name for the variable that must be renamed.
	 * 
	 * @return the new name for the variable that must be renamed.
	 */
	public String getNewVariableName() {
		return this.newVariableName;
	}
}
