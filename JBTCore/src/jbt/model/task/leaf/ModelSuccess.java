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
import jbt.execution.task.leaf.ExecutionSuccess;
import jbt.model.core.ModelTask;

/**
 * A ModelSuccess represents a task that always succeeds.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelSuccess extends ModelLeaf {
	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the ModelSuccess, which may be null.
	 */
	public ModelSuccess(ModelTask guard) {
		super(guard);
	}

	/**
	 * Returns an {@link ExecutionSuccess} that knows how to run this
	 * ModelSuccess.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      jbt.execution.core.ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionSuccess(this, executor, parent);
	}
}
