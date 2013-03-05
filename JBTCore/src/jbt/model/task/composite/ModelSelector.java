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
package jbt.model.task.composite;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.composite.ExecutionSelector;
import jbt.model.core.ModelTask;

/**
 * This class represents a task with one or more children, which are run
 * sequentially.
 * <p>
 * A selector tries to run all its children sequentially. Therefore, there is an
 * active child task. However, when the current active task fails, the selector
 * does not fail, but goes on to the next child task, which is evaluated. A
 * selector succeeds if one of the tasks succeeds, and fails if all the child
 * tasks fail.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelSelector extends ModelComposite {
	/**
	 * Constructor.
	 * <p>
	 * Constructs a ModelSelector with some children. A ModelSelector must have
	 * at least one child.
	 * 
	 * @param guard
	 *            the guard of the ModelSelector, which may be null.
	 * @param children
	 *            the list of children. Must have at least one element.
	 */
	public ModelSelector(ModelTask guard, ModelTask... children) {
		super(guard, children);
	}

	/**
	 * Returns an ExecutionSelector that is able to run this ModelSelector.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionSelector(this, executor, parent);
	}
}
