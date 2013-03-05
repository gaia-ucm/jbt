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
import java.util.Vector;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.IBTExecutor;
import jbt.execution.core.ITaskState;
import jbt.execution.core.ExecutionTask.Status;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.composite.ModelStaticPriorityList;
import jbt.util.Pair;

/**
 * ExecutionStaticPriorityList is the ExecutionTask that knows how to run a
 * ModelStaticPriorityList.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionStaticPriorityList extends ExecutionComposite {
	/** List of the children (ModelTask) of this task. */
	private List<ModelTask> children;
	/** Flag telling if the spawning process has failed. */
	private boolean spawnFailed;
	/** Flag that tells if there is a spawned child. */
	private boolean stillNotSpawned;
	/** Index of the currently active child. */
	private int activeChildIndex;
	/** Currently active child. */
	private ExecutionTask activeChild;
	/**
	 * List containing the IBTExecutors in charge of running the guards. The
	 * i-th element of this list manages the guard of the i-th child (
	 * {@link #children}). Note that if a guard is null, its corresponding
	 * IBTExecutor is also null.
	 */
	private List<BTExecutor> guardsExecutors;
	/**
	 * This List contains the current evaluation status of all the guards. If a
	 * guard is null, its corresponding status is {@link Status#SUCCESS} (null
	 * guards are evaluated to true).
	 */
	private List<Status> guardsResults;

	/**
	 * Creates an ExecutionStaticPriorityList that is able to run a
	 * ModelStaticPriorityList task and that is managed by a BTExecutor.
	 * 
	 * @param modelTask
	 *            the ModelStaticPriorityList that this
	 *            ExecutionStaticPriorityList is going to run.
	 * @param executor
	 *            the BTExecutor in charge of running this
	 *            ExecutionStaticPriorityList.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionStaticPriorityList(ModelTask modelTask, BTExecutor executor,
			ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelStaticPriorityList)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelStaticPriorityList.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Spawns the first child with active guard. If there is no active guard,
	 * the spawning process is considered to have failed, so
	 * {@link #internalTick()} will return {@link Status#FAILURE}. If some
	 * guards are still running the spawning process is not considered to have
	 * started yet.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		this.children = this.getModelTask().getChildren();

		/* Initialize guard executors. */
		this.guardsExecutors = new Vector<BTExecutor>();
		this.guardsResults = new Vector<Status>();
		for (ModelTask child : this.children) {
			if (child.getGuard() != null) {
				this.guardsExecutors.add(new BTExecutor(child.getGuard(), this.getContext()));
				this.guardsResults.add(Status.RUNNING);
			} else {
				this.guardsExecutors.add(null);
				this.guardsResults.add(Status.SUCCESS);
			}
		}

		/* Evaluate guards. */
		resetGuardsEvaluation();
		Pair<Status, Integer> activeGuard = evaluateGuards();

		/*
		 * Flag that tells if the static priority list must be inserted into the
		 * list of tickable nodes.
		 */
		boolean insertIntoTickableNodesList = false;

		/*
		 * If all the guards have failed, the spawning process has also failed.
		 * In such a case, the task must be inserted into the list of tickable
		 * nodes.
		 */
		if (activeGuard.getFirst() == Status.FAILURE) {
			this.spawnFailed = true;
			insertIntoTickableNodesList = true;
		} else if (activeGuard.getFirst() == Status.RUNNING) {
			/*
			 * If not all the guards have been evaluated yet, the spawning
			 * process is not considered to have started. In such a case, the
			 * task must be inserted into the list of tickable nodes.
			 */
			this.stillNotSpawned = true;
			insertIntoTickableNodesList = true;
		} else {
			/*
			 * If all the guards have been evaluated and one succeeded, spawn
			 * the corresponding child.
			 */
			this.spawnFailed = false;
			this.stillNotSpawned = false;
			this.activeChildIndex = activeGuard.getSecond();
			this.activeChild = this.children.get(this.activeChildIndex).createExecutor(
					this.getExecutor(), this);
			this.activeChild.addTaskListener(this);
			this.activeChild.spawn(this.getContext());
		}

		/* Insert into the list of tickable nodes if required. */
		if (insertIntoTickableNodesList) {
			this.getExecutor().requestInsertionIntoList(BTExecutorList.TICKABLE, this);
		}
	}

	/**
	 * If the spawning process has not finished yet (because there are some
	 * guards running), then this method keeps evaluating the guards, and
	 * returns {@link Status#RUNNING}. Whenever there is an active child
	 * (because the spawning process has finished), its status is returned.
	 * <p>
	 * If the spawning process failed, this method just returns
	 * {@link Status#FAILURE}.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		/* If the spawning process failed, return failure. */
		if (this.spawnFailed) {
			return Status.FAILURE;
		}

		/*
		 * If no child has been spawned yet (not all the guards had completed in
		 * the internalSpawn() method)...
		 */
		if (this.stillNotSpawned) {
			/* Evaluate guards. */
			Pair<Status, Integer> activeGuard = evaluateGuards();

			/* If all the guards have failed, return failure. */
			if (activeGuard.getFirst() == Status.FAILURE) {
				return Status.FAILURE;
			} else if (activeGuard.getFirst() == Status.RUNNING) {
				/*
				 * If not all the guards have finished, do no nothing (return
				 * RUNNING).
				 */
			} else {
				/*
				 * If all the guards have been evaluated and one succeeded,
				 * spawn the child. In this case, the static priority list
				 * must be removed from the list of tickable nodes.
				 */
				this.spawnFailed = false;
				this.stillNotSpawned = false;
				this.activeChildIndex = activeGuard.getSecond();
				this.activeChild = this.children.get(this.activeChildIndex).createExecutor(
						this.getExecutor(), this);
				this.activeChild.addTaskListener(this);
				this.activeChild.spawn(this.getContext());
				
				this.getExecutor().requestRemovalFromList(BTExecutorList.TICKABLE, this);
			}

			return Status.RUNNING;
		}

		/* If this point has been reached, there must be an active child. */
		return this.activeChild.getStatus();
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
	 * @see jbt.execution.core.ExecutionTask#restoreState(jbt.execution.core.ITaskState)
	 */
	protected void restoreState(ITaskState state) {
	}

	/**
	 * Just ticks this task.
	 * 
	 * @see jbt.execution.core.ExecutionTask#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public void statusChanged(TaskEvent e) {
		this.tick();
	}

	/**
	 * Just terminates the currently active child (if there is one).
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		/*
		 * This null check is necessary. Keep in mind that the static priority
		 * list may not have an active child, since the spawning process may
		 * have failed or not started yet. In such a case, if it is terminated,
		 * "this.activeChild" will be null.
		 */
		if (this.activeChild != null) {
			this.activeChild.terminate();
		}

		/* Terminate the guards executors. */
		for (IBTExecutor guardExecutor : this.guardsExecutors) {
			if (guardExecutor != null) {
				guardExecutor.terminate();
			}
		}
	}

	/**
	 * Resets the evaluation of all the guards. This method leaves all the guard
	 * executors ({@link #guardsExecutors}) ready to start again the evaluation
	 * of the guards. It internally terminates the IBTExecutor of each guard,
	 * creates a new one, and then ticks it.
	 */
	private void resetGuardsEvaluation() {
		for (int i = 0; i < this.guardsExecutors.size(); i++) {
			BTExecutor guardExecutor = this.guardsExecutors.get(i);

			if (guardExecutor != null) {
				guardExecutor.terminate();
				this.guardsResults.set(i, Status.RUNNING);
				BTExecutor newExecutor = new BTExecutor(guardExecutor.getBehaviourTree(),
						this.getContext());
				newExecutor.copyTasksStates(guardExecutor);
				newExecutor.tick();
				this.guardsExecutors.set(i, newExecutor);
			}
		}
	}

	/**
	 * Evaluate all the guards that have not finished yet, that is, those whose
	 * result in {@link #guardsResults} is {@link Status#RUNNING}, by ticking
	 * them.
	 * <p>
	 * If all the guards have finished in failure, this method returns a Pair
	 * whose first element is {@link Status#FAILURE}. If there is at least one
	 * guard still being evaluated, the first element of the Pair contains
	 * {@link Status#RUNNING}. If all the guards have been evaluated and at
	 * least one has succeeded, the first element of the Pair is
	 * {@link Status#SUCCESS}, and the second one is the index, over the list of
	 * guards ({@link #guardsExecutors}) , of the first guard (that with the
	 * highest priority) that has succeeded.
	 * 
	 */
	private Pair<Status, Integer> evaluateGuards() {
		boolean oneRunning = false;

		/* First, evaluate all the guards that have not finished yet. */
		for (int i = 0; i < this.guardsExecutors.size(); i++) {
			IBTExecutor guardExecutor = this.guardsExecutors.get(i);
			if (guardExecutor != null) {
				if (this.guardsResults.get(i) == Status.RUNNING) {
					guardExecutor.tick();
					this.guardsResults.set(i, guardExecutor.getStatus());
					if (this.guardsResults.get(i) == Status.RUNNING) {
						oneRunning = true;
					}
				}
			}
		}

		/* If there is at least one still running... */
		if (oneRunning) {
			return new Pair<Status, Integer>(Status.RUNNING, -1);
		}

		/* If all of them have finished we check which one succeeded first. */
		for (int i = 0; i < this.guardsResults.size(); i++) {
			if (this.guardsResults.get(i) == Status.SUCCESS) {
				return new Pair<Status, Integer>(Status.SUCCESS, i);
			}
		}

		/* Otherwise, the evaluation has failed. */
		return new Pair<Status, Integer>(Status.FAILURE, -1);
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
