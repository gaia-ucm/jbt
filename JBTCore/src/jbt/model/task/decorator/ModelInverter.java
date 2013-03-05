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
import jbt.execution.task.decorator.ExecutionInverter;
import jbt.model.core.ModelTask;

/**
 * ModelInverter is a decorator used to invert the status code returned by its
 * child.
 * <p>
 * When the decorated task finishes, its status code gets inverted according to:
 * 
 * <ul>
 * <li><code>Status.SUCCESS</code> -> <code>Status.FAILURE</code>.
 * <li><code>Status.FAILURE</code> -> <code>Status.SUCCESS</code>.
 * <li><code>Status.TERMINATED</code> -> <code>Status.SUCCESS</code>.
 * </ul>
 * 
 * If the child task has not finished yet, the ModelInverter returns
 * <code>Status.RUNNING</code> (that is, <code>Status.RUNNING</code> is not
 * inverted).
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelInverter extends ModelDecorator {
	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the ModelInverter, which may be null.
	 * @param child
	 *            the child task to invert.
	 */
	public ModelInverter(ModelTask guard, ModelTask child) {
		super(guard, child);
	}

	/**
	 * Returns an ExecutionInverter that is able to run this ModelInverter.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionInverter(this, executor, parent);
	}
}
