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
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.decorator.ModelInterrupter;

/**
 * ExecutionInterrupter is the ExecutionTask that knows how to run a
 * ModelInterrupter. In order to interrupt the ExecutionInterrupter,
 * {@link #interrupt(Status)} must be called.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionInterrupter extends ExecutionDecorator {
	/**
	 * Flag that tells if the ExecutionInterrupter has already been interrupted.
	 */
	private boolean interrupted;
	/**
	 * Status code that this ExecutionInterrupter should return in case it is
	 * interrupted.
	 */
	private Status statusSet;
	/**
	 * The child ExecutionTask of this ExecutionInterrupter.
	 */
	private ExecutionTask executionChild;

	/**
	 * Creates an ExecutionInterrupter that is able to run a ModelInterrupter
	 * task and that is managed by a BTExecutor.
	 * 
	 * @param modelTask
	 *            the ModelInterrupter that this ExecutionInterrupter is going
	 *            to run.
	 * @param executor
	 *            the BTExecutor in charge of running this ExecutionInterrupter.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionInterrupter(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelInterrupter)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelInterrupter.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
		this.interrupted = false;
	}

	/**
	 * Spawns its child and registers itself into the list of interrupters of
	 * the BTExecutor.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		this.executionChild = ((ModelInterrupter) this.getModelTask()).getChild().createExecutor(
				this.getExecutor(), this);
		this.executionChild.addTaskListener(this);
		/*
		 * Register the ExecutionInterrupter so that
		 * ExecutionPerformInterruption can find it.
		 */
		this.getExecutor().registerInterrupter(this);
		this.executionChild.spawn(this.getContext());
	}

	/**
	 * Terminates the child task and unregister itself from the list of
	 * interrupters of the BTExecutor.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		/*
		 * Unregister the ExecutionInterrupter so that it is no longer available
		 * to ExecutionPerformInterruption.
		 */
		this.getExecutor().unregisterInterrupter(this);
		/*
		 * It is important to cancel any request for insertion that this task
		 * has. If the task has been interrupted, it will have requested to be
		 * inserted into the list of tickable nodes. However, if it is then
		 * terminated, we do not want it to be inserted into the list of
		 * tickable nodes, so the request made in "interrupt()" must be
		 * cancelled.
		 */
		if (this.interrupted) {
			this.getExecutor().cancelInsertionRequest(BTExecutorList.TICKABLE, this);
		}

		this.executionChild.terminate();
	}

	/**
	 * If the ExecutionInterrupter has been interrupted, returns the status that
	 * was passed to the {@link #interrupt(Status)} method. Otherwise, returns
	 * the current status of the child task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		if (this.interrupted) {
			/*
			 * Unregister the ExecutionInterrupter so that it is no longer
			 * available to ExecutionPerformInterruption.
			 */
			this.getExecutor().unregisterInterrupter(this);
			return this.statusSet;
		}
		else {
			Status childStatus = this.executionChild.getStatus();
			if (childStatus != Status.RUNNING) {
				/*
				 * If the child has finished, unregister the
				 * ExecutionInterrupter so that it is no longer available to
				 * ExecutionPerformInterruption.
				 */
				this.getExecutor().unregisterInterrupter(this);
			}
			return childStatus;
		}
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {}

	/**
	 * Just calls {@link #tick()} to make the ExecutionInterrupter evolve.
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
	 * Interrupts the ExecutionInterrupter. This method causes the
	 * ExecutionInterrupter to terminate its child and set the status that will
	 * be returned to <code>status</code>. Also, it requests to be inserted into
	 * the list of tickable nodes, since the terminated child will no longer
	 * react to ticks after being terminated.
	 * <p>
	 * A task that have not been spawned cannot be interrupted. It should be
	 * noted that if the task has already been terminated, this method does
	 * nothing. Also, if the task has already been interrupted, this method does
	 * nothing too.
	 * 
	 * @param status
	 *            the status that the ExecutionInterrupter will return.
	 */
	public void interrupt(Status status) {
		if (!this.interrupted) {
			/*
			 * If the task has not been spawned, throw an exception.
			 */
			if (!this.getSpawned()) {
				throw new RuntimeException(
						"Cannot interrupt an ExecutionInterrupter that has not been spawned");
			}

			/*
			 * Also it is important to note that that if the task has been
			 * terminated it cannot be interrupted, since by doing so the task
			 * would insert itself into the list of tickable nodes (see below),
			 * which should not be done since the task has been terminated.
			 */
			if (!this.getTerminated()) {
				if (status != Status.FAILURE && status != Status.SUCCESS) {
					throw new IllegalArgumentException(
							"The specified status is not valid. Must be either Status.FAILURE or Status.SUCCESS");
				}

				/* Terminate the child. */
				this.executionChild.terminate();

				/*
				 * It is very important for the ExecutionInterrupter to be
				 * inserted into the list of tickable nodes. If not, after being
				 * interrupted, it will not inform its parent about the
				 * termination of its child. Keep in mind that after terminating
				 * its child, the child will not react to ticks (actually it
				 * will leave the list of tickable nodes in the next AI cycle),
				 * so it has to be the interrupter itself that informs its
				 * parent about termination.
				 */
				this.getExecutor().requestInsertionIntoList(BTExecutorList.TICKABLE, this);
				this.interrupted = true;
				this.statusSet = status;
			}
		}
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
