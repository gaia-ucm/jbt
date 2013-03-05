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
import jbt.execution.task.leaf.ExecutionWait;
import jbt.model.core.ModelTask;

/**
 * A ModelWait task is a task that keeps running for a period of time, and then
 * succeeds. The user can specify for how long the ModelWait task should be
 * running. For that period of time, the task will be evaluated to
 * Status.RUNNING. Then, the task will return Status.SUCCESS.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelWait extends ModelLeaf {
	/**
	 * Duration, measured in milliseconds, of the period of time the task will
	 * be running.
	 */
	private long duration;

	/**
	 * Constructor. Constructs a ModelWait task that will keep running for
	 * <code>duration</code> milliseconds.
	 * 
	 * @param guard
	 *            the guard of the ModelWait task, which may be null.
	 * @param duration
	 *            the ModelWait of the Wait task, in milliseconds.
	 */
	public ModelWait(ModelTask guard, long duration) {
		super(guard);
		this.duration = duration;
	}

	/**
	 * Returns an ExecutionWait that can run this ModelWait.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionWait(this, executor, parent);
	}

	/**
	 * Returns the duration of this ModelWait task.
	 * 
	 * @return the duration of this ModelWait task.
	 */
	public long getDuration() {
		return this.duration;
	}
}
