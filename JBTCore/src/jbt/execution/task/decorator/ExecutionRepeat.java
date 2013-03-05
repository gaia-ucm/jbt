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
import jbt.execution.core.ExecutionTask.Status;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelDecorator;
import jbt.model.task.decorator.ModelRepeat;

/**
 * ExecutionRepeat is the ExecutionTask that knows how to run a ModelForever.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionRepeat extends ExecutionDecorator {
	/** The child task. */
	private ExecutionTask child;

	/**
	 * Constructs an ExecutionRepeat that knows how to run a ModelForever.
	 * 
	 * @param modelTask
	 *            the ModelForever to run.
	 * @param executor
	 *            the BTExecutor that will manager this ExecutionRepeat.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionRepeat(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelRepeat)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelRepeat.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Just spawns its child task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		this.child = ((ModelRepeat) this.getModelTask()).getChild().createExecutor(
				this.getExecutor(), this);
		this.child.addTaskListener(this);
		this.child.spawn(this.getContext());
	}

	/**
	 * Terminates the child task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		this.child.terminate();
	}

	/**
	 * If the child task has finished, it spawns it again. Always returns
	 * {@link Status#RUNNING}.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		Status childStatus = this.child.getStatus();

		/*
		 * If the child has finished, spawn it again
		 */
		if (childStatus != Status.RUNNING) {
			this.child = ((ModelDecorator) this.getModelTask()).getChild().createExecutor(
					this.getExecutor(), this);
			this.child.addTaskListener(this);
			this.child.spawn(this.getContext());
		}

		return Status.RUNNING;
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {}

	/**
	 * Just calls {@link #tick()} to make the task evolve.
	 * 
	 * @see jbt.execution.core.ExecutionTask#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public void statusChanged(TaskEvent e) {
		this.tick();
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
}
