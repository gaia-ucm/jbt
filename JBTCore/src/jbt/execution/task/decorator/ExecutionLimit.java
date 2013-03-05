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

import java.util.List;
import java.util.Vector;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.execution.core.ITaskState;
import jbt.execution.core.TaskStateFactory;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelLimit;
import jbt.util.Pair;

/**
 * ExecutionLimit is the ExecutionTask that knows how to run a ModelLimit.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionLimit extends ExecutionDecorator {
	/** Maximum number of times that the child task can be executed. */
	private int maxNumTimes;
	/**
	 * Number of times that the child task has been run so far. Initially, its
	 * value is restored from the context.
	 */
	private int numRunsSoFar;
	/**
	 * The child of this decorator.
	 */
	private ExecutionTask child;

	/**
	 * Name of the variable that is stored in the context and that represents
	 * the number of times that the decorator has been run so far.
	 */
	protected String STATE_VARIABLE_NAME = "RunsSoFar";

	/**
	 * Creates an ExecutionLimit that knows how to run a ModelLimit.
	 * 
	 * @param modelTask
	 *            the ModelLimit to run.
	 * @param executor
	 *            the BTExecutor that will manage this ExecutionLimit.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionLimit(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelLimit)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelLimit.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}

		this.maxNumTimes = ((ModelLimit) this.getModelTask()).getMaxNumTimes();
		this.numRunsSoFar = 0;
	}

	/**
	 * Spawns the child task if it has not been run more than the maximum
	 * allowed number of times. Otherwise, it requests to be inserted into the
	 * list of tickable nodes, since the child is not spawned.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		if (this.numRunsSoFar < this.maxNumTimes) {
			this.numRunsSoFar++;
			this.child = ((ModelLimit) this.getModelTask()).getChild().createExecutor(
					this.getExecutor(), this);
			this.child.addTaskListener(this);
			this.child.spawn(this.getContext());
		}
		else {
			this.getExecutor().requestInsertionIntoList(BTExecutorList.TICKABLE, this);
		}
	}

	/**
	 * Terminates the child task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		if (this.child != null) {
			this.child.terminate();
		}
	}

	/**
	 * Returns the status of the child task, or {@link Status#FAILURE} in case
	 * it could not be spawned.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		if (this.child != null) {
			return this.child.getStatus();
		}
		else {
			return Status.FAILURE;
		}
	}

	/**
	 * Restore from the ITaskState the number of times that the child task of
	 * this decorator has been run so far. It is read from the variable whose
	 * name is {@link #STATE_VARIABLE_NAME}.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {
		try {
			this.numRunsSoFar = (Integer) state.getStateVariable(STATE_VARIABLE_NAME);
		}
		catch (Exception e) {}
	}

	/**
	 * Just calls {@link #tick()} to make this task evolve.
	 * 
	 * @see jbt.execution.core.ExecutionTask#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public void statusChanged(TaskEvent e) {
		this.tick();
	}

	/**
	 * Returns an ITaskState witht a variable with name
	 * {@link #STATE_VARIABLE_NAME} , and whose value is the number of times
	 * that the child task of this decorator has been run so far.
	 * 
	 * @see jbt.execution.core.ExecutionTask#storeState()
	 */
	protected ITaskState storeState() {
		List<Pair<String,Object>> variables=new Vector<Pair<String,Object>>();
		variables.add(new Pair<String,Object>(STATE_VARIABLE_NAME, this.numRunsSoFar));
		return TaskStateFactory.createTaskState(variables);
	}

	/**
	 * Returns an ITaskState witht a variable with name
	 * {@link #STATE_VARIABLE_NAME} , and whose value is the number of times
	 * that the child task of this decorator has been run so far.
	 * 
	 * @see jbt.execution.core.ExecutionTask#storeTerminationState()
	 */
	protected ITaskState storeTerminationState() {
		List<Pair<String,Object>> variables=new Vector<Pair<String,Object>>();
		variables.add(new Pair<String,Object>(STATE_VARIABLE_NAME, this.numRunsSoFar));
		return TaskStateFactory.createTaskState(variables);
	}
}
