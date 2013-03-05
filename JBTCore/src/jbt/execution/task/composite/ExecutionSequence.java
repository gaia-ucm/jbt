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
package jbt.execution.task.composite;

import java.util.List;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.composite.ModelSequence;

/**
 * ExecutionSequence is the ExecutionTask that knows how to run a ModelSequence
 * task.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionSequence extends ExecutionTask {
	/** Index of the active child. */
	private int activeChildIndex;
	/** The currently active child. */
	private ExecutionTask activeChild;
	/** List of the ModelTask children of the sequence. */
	private List<ModelTask> children;

	/**
	 * Creates an ExecutionSequence that is able to run a ModelSequence task and
	 * that is managed by a BTExecutor.
	 * 
	 * @param modelTask
	 *            the ModelSequence that this ExecutionSequence is going to run.
	 * @param executor
	 *            the BTExecutor in charge of running this ExecutionSequence.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionSequence(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelSequence)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelSequence.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Spawns the first child of the sequence.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		/*
		 * Spawn the first child of the sequence.
		 */
		this.activeChildIndex = 0;
		this.children = this.getModelTask().getChildren();
		this.activeChild = this.children.get(0).createExecutor(this.getExecutor(), this);
		this.activeChild.addTaskListener(this);
		this.activeChild.spawn(this.getContext());
	}

	/**
	 * Terminates the currently active child.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		/* Just terminate the active child. */
		this.activeChild.terminate();
	}

	/**
	 * Checks if the currently active child has finished. If it has not
	 * finished, returns {@link Status#SUCCESS}. If it has finished in failure,
	 * returns {@link Status#FAILURE}. If it has finished successfully, it
	 * checks if there is any remaining child. If so, it spawns it. Otherwise,
	 * returns {@link Status#SUCCESS}.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		Status childStatus = this.activeChild.getStatus();
		if (childStatus == Status.RUNNING) {
			return Status.RUNNING;
		}
		else if (childStatus == Status.FAILURE || childStatus == Status.TERMINATED) {
			return Status.FAILURE;
		}
		else {
			if (this.activeChildIndex == this.children.size() - 1) {
				/*
				 * If this was the last child, return success.
				 */
				return Status.SUCCESS;
			}
			else {
				/*
				 * If the current child has finished successfully, but it is not
				 * the last one, spawn the next child.
				 */
				this.activeChildIndex++;
				this.activeChild = this.children.get(this.activeChildIndex).createExecutor(
						this.getExecutor(), this);
				this.activeChild.addTaskListener(this);
				this.activeChild.spawn(this.getContext());
				return Status.RUNNING;
			}
		}
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {}

	/**
	 * Just calls {@link #tick()} to check if the ExecutionSequence can evolve
	 * due to the change in the state of the task that was listening to.
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
