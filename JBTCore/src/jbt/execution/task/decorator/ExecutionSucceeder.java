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
package jbt.execution.task.decorator;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelSucceeder;

/**
 * ExecutionSucceeder is the ExecutionTask that knows how to run a ModelSucceeder.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionSucceeder extends ExecutionDecorator {
	/** The child that is being decorated. */
	private ExecutionTask child;

	/**
	 * Creates an ExecutionSucceeder that knows how to run a ModelSucceeder.
	 * 
	 * @param modelTask
	 *            the ModelSucceeder to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionSucceeder.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionSucceeder(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelSucceeder)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelSucceeder.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Just spawns its child.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		this.child = ((ModelSucceeder) this.getModelTask()).getChild().createExecutor(
				this.getExecutor(), this);

		this.child.addTaskListener(this);
		this.child.spawn(this.getContext());
	}

	/**
	 * Just ticks its child.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		Status childStatus = this.child.getStatus();

		if (childStatus == Status.RUNNING) {
			return Status.RUNNING;
		}

		return Status.SUCCESS;
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#storeState()
	 */
	protected ITaskState storeState() {
		return null;
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#storeTerminationState()
	 */
	protected ITaskState storeTerminationState() {
		return null;
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(jbt.execution.core.ITaskState)
	 */
	protected void restoreState(ITaskState state) {
	}

	/**
	 * Just ticks the task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public void statusChanged(TaskEvent e) {
		this.tick();
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		this.child.terminate();
	}
}
