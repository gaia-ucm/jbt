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
package jbt.model.task.decorator;

import java.util.List;

import jbt.execution.context.SafeOutputContext;
import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.decorator.ExecutionSafeOutputContextManager;
import jbt.model.core.ModelTask;

/**
 * A ModelSafeOutputContextManager is a decorator that creates a new context for
 * its child task. The context that it creates is a {@link SafeOutputContext},
 * and the input context that the SafeOutputContext receives is that of the
 * ModelSafeOutputContextManager.
 * <p>
 * The spawning and updating of the child task are carried out as usual.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelSafeOutputContextManager extends ModelDecorator {
	/**
	 * The list of output variables of the SafeOutputContext.
	 */
	private List<String> outputVariables;

	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the ModelSafeOutputContextManager, which may be
	 *            null.
	 * @param child
	 *            the child of the ModelSafeOutputContextManager.
	 * @param outputVariables
	 *            the list of output variables of the SafeOutputContext that is
	 *            created.
	 */
	public ModelSafeOutputContextManager(ModelTask guard, List<String> outputVariables,
			ModelTask child) {
		super(guard, child);
		this.outputVariables = outputVariables;
	}

	/**
	 * Returns an ExecutionSafeOutputContextManager that knows how to run this
	 * ModelSafeOutputContextManager.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionSafeOutputContextManager(this, executor, parent);
	}

	/**
	 * Returns a list with the set of output variables of the SafeOutputContext.
	 * The list cannot be modified.
	 * 
	 * @return a list with the set of output variables of the SafeOutputContext.
	 */
	public List<String> getOutputVariables() {
		return this.outputVariables;
	}
}
