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

import jbt.execution.context.HierarchicalContext;
import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelHierarchicalContextManager;
import jbt.model.task.decorator.ModelDecorator;

/**
 * ExecutionHierarchicalContextManager is the ExecutionTask that knows how to run a
 * ModelHierarchicalContextManager.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionHierarchicalContextManager extends ExecutionDecorator {
	/** The child task. */
	private ExecutionTask child;

	/**
	 * Constructs an ExecutionHierarchicalContextManager that knows how to run a
	 * ModelHierarchicalContextManager.
	 * 
	 * @param modelTask
	 *            the ModelHierarchicalContextManager to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionHierarchicalContextManager.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionHierarchicalContextManager(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelHierarchicalContextManager)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelHierarchicalContextManager.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Spawns the child task. This method creates a new HierarchicalContext,
	 * sets its parent to the context of the ExecutionHierarchicalContextManager, and spawns
	 * the child task using this HierarchicalContext.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		HierarchicalContext newContext = new HierarchicalContext();
		newContext.setParent(this.getContext());
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
	protected void restoreState(ITaskState state) {}

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
