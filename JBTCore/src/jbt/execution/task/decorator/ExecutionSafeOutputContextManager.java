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

import jbt.execution.context.SafeOutputContext;
import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelDecorator;
import jbt.model.task.decorator.ModelSafeOutputContextManager;

/**
 * ExecutionSafeOutputContextManager is the ExecutionTask that knows how to run
 * a ModelSafeOutputContextManager.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionSafeOutputContextManager extends ExecutionDecorator {
	/** The child task. */
	private ExecutionTask child;

	/**
	 * Constructs an ExecutionSafeOutputContextManager that knows how to run a
	 * ModelSafeOutputContextManager.
	 * 
	 * @param modelTask
	 *            the ModelSafeOutputContextManager to run.
	 * @param executor
	 *            the BTExecutor that will manage this
	 *            ExecutionSafeOutputContextManager.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionSafeOutputContextManager(ModelTask modelTask, BTExecutor executor,
			ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelSafeOutputContextManager)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelSafeOutputContextManager.class.getCanonicalName()
					+ " but it inherits from " + modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Spawns the child task. This method creates a new SafeOutputContext, and
	 * spawns the child task using this SafeContext. The input context of the
	 * SafeOutputContext is that of this ExecutionSafeOutputContextManager task.
	 * The list of output variables of the SafeOutputContext is retrieved from
	 * the ModelSafeOutputContextManager associated to this task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		SafeOutputContext newContext = new SafeOutputContext(this.getContext(),
				((ModelSafeOutputContextManager) this.getModelTask()).getOutputVariables());
		this.child = ((ModelDecorator) this.getModelTask()).getChild().createExecutor(
				this.getExecutor(), this);
		this.child.addTaskListener(this);
		this.child.spawn(newContext);
	}

	/**
	 * Just terminates the child task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		this.child.terminate();
	}

	/**
	 * Returns the current status of the child.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		return this.child.getStatus();
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {
	}

	/**
	 * Just calls {@link #tick()} to make the tass evolve.
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
