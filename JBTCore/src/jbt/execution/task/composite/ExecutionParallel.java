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

import java.util.LinkedList;
import java.util.List;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.composite.ModelParallel;
import jbt.model.task.composite.ModelParallel.ParallelPolicy;

/**
 * ExecutionParallel is the ExecutionTask that knows how to run a ModelParallel
 * task.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionParallel extends ExecutionComposite {
	/** Policy of the parallel task. */
	private ParallelPolicy policy;
	/** List of the ModelTask children of this task. */
	private List<ModelTask> modelChildren;
	/** List of the ExecutionTask children of this task. */
	private List<ExecutionTask> executionChildren;

	/**
	 * Creates an ExecutionParallel that is able to run a ModelParallel task and
	 * that is managed by a BTExecutor.
	 * 
	 * @param modelTask
	 *            the ModelParallel that this ExecutionParallel is going to run.
	 * @param executor
	 *            the BTExecutor in charge of running this ExecutionParallel.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionParallel(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelParallel)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelParallel.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}

		this.policy = ((ModelParallel) modelTask).getPolicy();
		this.modelChildren = modelTask.getChildren();
		this.executionChildren = new LinkedList<ExecutionTask>();
	}

	/**
	 * Spawns every single child of the task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		if (this.policy == ParallelPolicy.SEQUENCE_POLICY) {
			sequencePolicySpawn();
		}
		else {
			selectorPolicySpawn();
		}
	}

	/**
	 * Terminates all of its children.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		if (this.policy == ParallelPolicy.SEQUENCE_POLICY) {
			sequencePolicyTerminate();
		}
		else {
			selectorPolicyTerminate();
		}

	}

	/**
	 * Ticks this ExecutionParallel. This process varies depending on the
	 * policy. that is being followed. See {@link ModelParallel} for more
	 * information.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		if (this.policy == ParallelPolicy.SEQUENCE_POLICY) {
			return sequencePolicyTick();
		}
		else {
			return selectorPolicyTick();
		}
	}

	/**
	 * Carries out the spawning process when the policy is
	 * {@link ParallelPolicy#SEQUENCE_POLICY}.
	 */
	private void sequencePolicySpawn() {
		/* First, create an ExecutionTask for all of the childre. */
		for (ModelTask t : this.modelChildren) {
			this.executionChildren.add(t.createExecutor(this.getExecutor(), this));
		}

		/* Then, spawn them all. */
		for (ExecutionTask t : this.executionChildren) {
			t.addTaskListener(this);
			t.spawn(this.getContext());
		}
	}

	/**
	 * Carries out the spawning process when the policy is
	 * {@link ParallelPolicy#SELECTOR_POLICY}.
	 */
	private void selectorPolicySpawn() {
		sequencePolicySpawn();
	}

	/**
	 * Carries out the termination process when the policy is
	 * {@link ParallelPolicy#SEQUENCE_POLICY}.
	 */
	private void sequencePolicyTerminate() {
		/* Just terminate all of its children. */
		for (ExecutionTask t : this.executionChildren) {
			t.terminate();
		}
	}

	/**
	 * Carries out the termination process when the policy is
	 * {@link ParallelPolicy#SELECTOR_POLICY}.
	 */
	private void selectorPolicyTerminate() {
		sequencePolicyTerminate();
	}

	/**
	 * Carries out the ticking process when the policy is
	 * {@link ParallelPolicy#SEQUENCE_POLICY}.
	 * 
	 * @return the task status after the tick.
	 */
	private Status sequencePolicyTick() {
		/*
		 * If one child has failed, then return Status.FAILURE. Otherwise, if
		 * there is at least one child still running, return Status.RUNNING.
		 * Otherwise, return Status.SUCCESS.
		 */
		boolean oneRunning = false;

		for (ExecutionTask t : this.executionChildren) {
			Status currentStatus = t.getStatus();
			if (currentStatus == Status.RUNNING) {
				oneRunning = true;
			}
			else if (currentStatus == Status.FAILURE || currentStatus == Status.TERMINATED) {
				sequencePolicyTerminate();
				return Status.FAILURE;
			}
		}

		if (!oneRunning) {
			return Status.SUCCESS;
		}
		else {
			return Status.RUNNING;
		}
	}

	/**
	 * Carries out the ticking process when the policy is
	 * {@link ParallelPolicy#SELECTOR_POLICY}.
	 * 
	 * @return the task status after the tick.
	 */
	private Status selectorPolicyTick() {
		/*
		 * If one child has succeeded, then return Status.SUCCESS. Otherwise, if
		 * there is at least one child still running, return Status.RUNNING.
		 * Otherwise, return Status.FAILURE.
		 */
		boolean oneRunning = false;

		for (ExecutionTask t : this.executionChildren) {
			Status currentStatus = t.getStatus();
			if (currentStatus == Status.SUCCESS) {
				sequencePolicyTerminate();
				return Status.SUCCESS;
			}
			else if (currentStatus == Status.RUNNING) {
				oneRunning = true;
			}
		}

		if (!oneRunning) {
			return Status.FAILURE;
		}
		else {
			return Status.RUNNING;
		}
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {}

	/**
	 * Just calls {@link #tick()} to make the ExecutionParallel task evolve
	 * according to the status of its children.
	 * 
	 * @see jbt.execution.core.ExecutionTask#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public void statusChanged(TaskEvent e) {
		/*
		 * TODO: the TaskEvent could be used to improve the efficiency of this
		 * method, since we only have to analyse the status of the task that
		 * fired the event, not the status of all the tasks (which is what
		 * tick() does).
		 */
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
