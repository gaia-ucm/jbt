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
import jbt.execution.core.ExecutionTask.Status;
import jbt.execution.task.leaf.ExecutionPerformInterruption;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelInterrupter;

/**
 * A ModelPerformInterruption is a task that interacts with a ModelInterrupter
 * decorator, interrupting it when it (the ModelPerformInterruption) is spawned.
 * A ModelPerformInterruption always succeeds when it is spawned. When the
 * ModelInterrupter gets interrupted, the status code it returns is also set by
 * the ModelPerformInterruption.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelPerformInterruption extends ModelLeaf {
	/**
	 * The ModelInterrupter that this ModelPerformInterruption is going to
	 * interrupt.
	 */
	private ModelInterrupter interrupter;
	/**
	 * The status code that the ModelInterrupter should return in case it is
	 * interrupted.
	 */
	private Status desiredResult;

	/**
	 * Constructor.
	 * 
	 * @param guard
	 *            the guard of the ModelPerformInterruption, which may be null.
	 * @param interrupter
	 *            the ModelInterrupter that this ModelPerformInterruption will
	 *            interrupt. May be null.
	 * @param desiredResult
	 *            the result that the ModelInterrupter should return in case it
	 *            is interrupted.
	 */
	public ModelPerformInterruption(ModelTask guard, ModelInterrupter interrupter,
			Status desiredResult) {
		super(guard);
		this.interrupter = interrupter;
		this.desiredResult = desiredResult;
	}

	/**
	 * Returns an ExecutionPerformInterruption that is able to run this
	 * ModelPerformInterruption.
	 * 
	 * @see jbt.model.core.ModelTask#createExecutor(jbt.execution.core.BTExecutor,
	 *      ExecutionTask)
	 */
	public ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent) {
		return new ExecutionPerformInterruption(this, executor, parent);
	}

	/**
	 * Sets the interrupter that this PerformInterruption is going to interrupt.
	 * 
	 * @param interrupter
	 *            the ModelInterrupter that this PerformInterruption is going to
	 *            interrupt. May be null.
	 */
	public void setInterrupter(ModelInterrupter interrupter) {
		this.interrupter = interrupter;
	}

	/**
	 * Returns the ModelInterrupter that this PerformInterruption is going to
	 * interrupt, or null if not set.
	 * 
	 * @return the ModelInterrupter that this PerformInterruption is going to
	 *         interrupt, or null if not set.
	 */
	public ModelInterrupter getInterrupter() {
		return this.interrupter;
	}

	/**
	 * Returns the result that the ModelInterrupter should return in case it is
	 * interrupted.
	 * 
	 * @return e result that the ModelInterrupter should return in case it is
	 *         interrupted.
	 */
	public Status getDesiredResult() {
		return this.desiredResult;
	}
}
