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

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.task.decorator.ExecutionInterrupter;
import jbt.model.core.ModelTask;

/**
 * An ModelInterrupter is a decorator that controls the termination of a child
 * task. An ModelInterrupter simply lets its child task run normally. If the
 * child returns a result, the ModelInterrupter will return it. However, the
 * ModelInterrupter can be asked to terminate the child task and return an
 * specified status when done so.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelInterrupter extends ModelDecorator {
	/**
	 * Constructor.
	 * <p>
	 * Constructs a ModelInterrupter with one child.
	 * 
	 * @param guard
	 *            the guard of the ModelInterrupter, which may be null.
	 * @param child
	 *            the child of the ModelInterrupter.
	 */
	public ModelInterrupter(ModelTask guard, ModelTask child) {
		super(guard, child);
	}

	/**
	 * Returns an ExecutionInterrupter that is able to run this
	 * ModelInterrupter.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionInterrupter(this, executor, parent);
	}
}
