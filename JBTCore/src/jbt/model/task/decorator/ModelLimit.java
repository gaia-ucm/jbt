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
import jbt.execution.task.decorator.ExecutionLimit;
import jbt.model.core.ModelTask;

/**
 * Limit is a decorator that limits the number of times a task can be executed.
 * This decorator is used when a task (the child of the decorator) must be run a
 * maximum number of times. When the maximum number of times is exceeded, the
 * decorator will fail forever on.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelLimit extends ModelDecorator {
	/** Maximum number of times that the decorated task can be run. */
	private int maxNumTimes;

	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the ModelLimit, which may be null.
	 * @param maxNumTimes
	 *            the maximum number of times that <code>child</code> will be
	 *            run.
	 * @param child
	 *            the child of this task.
	 */
	public ModelLimit(ModelTask guard, int maxNumTimes, ModelTask child) {
		super(guard, child);
		this.maxNumTimes = maxNumTimes;
	}

	/**
	 * Returns an ExecutionLimit that knows how to run this ModelLimit.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionLimit(this, executor, parent);
	}

	/**
	 * Returns the maximum number of times that the decorated task can be run.
	 * 
	 * @return the maximum number of times that the decorated task can be run.
	 */
	public int getMaxNumTimes() {
		return this.maxNumTimes;
	}
}
