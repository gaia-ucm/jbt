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
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.IBTExecutor;
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.execution.core.ITaskState;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.task.composite.ModelDynamicPriorityList;
import jbt.util.Pair;

/**
 * ExecutionDynamicPriorityList is the ExecutionTask that knows how to run a
 * ModelDynamicPriorityList.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ExecutionDynamicPriorityList extends ExecutionComposite {
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
	 * List containing the IBTExecutors in charge of running the guards. The i-th element of this
	 * list manages the guard of the i-th child ( {@link #children}). Note that if a guard is null,
	 * its corresponding IBTExecutor is also null.
	 */
	private List<BTExecutor> guardsExecutors;
	/**
	 * This List contains the current evaluation status of all the guards. If a guard is null, its
	 * corresponding status is {@link Status#SUCCESS} (null guards are evaluated to true).
	 */
	private List<Status> guardsResults;
	/**
	 * Index of the current most relevant guard. All the guards before it have finished in failure.
	 * This represents the guard such that, if its status changes to success, then it would be the
	 * one selected by the dynamic priority list.
	 */
	private int indexMostRelevantGuard = 0;

	/**
	 * Creates an ExecutionDynamicPriorityList that is able to run a ModelDynamicPriorityList task
	 * and that is managed by a BTExecutor.
	 * 
	 * @param modelTask
	 *            the ModelDynamicPriorityList that this ExecutionDynamicPriorityList is going to
	 *            run.
	 * @param executor
	 *            the BTExecutor in charge of running this ExecutionDynamicPriorityList.
	 * @param parent
	 *            the parent ExecutionTask of this task.
	 */
	public ExecutionDynamicPriorityList(ModelTask modelTask, BTExecutor executor,
			ExecutionTask parent) {
		super(modelTask, executor, parent);
		if (!(modelTask instanceof ModelDynamicPriorityList)) {
			throw new IllegalArgumentException("The ModelTask must subclass "
					+ ModelDynamicPriorityList.class.getCanonicalName() + " but it inherits from "
					+ modelTask.getClass().getCanonicalName());
		}
	}

	/**
	 * Spawns the first child with active guard. It also requests to be inserted into the list of
	 * tickable nodes of the BTExecutor, since this task has to check its children's guards all the
	 * time. If there is no active guard, the spawning process is considered to have failed, so
	 * {@link #internalTick()} will return {@link Status#FAILURE}. If some guards are still running
	 * the spawning process is not considered to have started yet.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalSpawn()
	 */
	protected void internalSpawn() {
		/*
		 * The dynamic priority list has to be inserted into the list of tickable nodes because it
		 * has to check its children's guards all the time.
		 */
		this.getExecutor().requestInsertionIntoList(BTExecutorList.TICKABLE, this);

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

		/* If all guards have failed, the spawning process has also failed. */
		if (activeGuard.getFirst() == Status.FAILURE) {
			this.spawnFailed = true;
		} else if (activeGuard.getFirst() == Status.RUNNING) {
			/*
			 * If not all the guards have been evaluated yet, the spawning process is not considered
			 * to have started.
			 */
			this.stillNotSpawned = true;
		} else {
			/*
			 * If all the guards have been evaluated and one succeeded, spawn the corresponding
			 * child.
			 */
			this.spawnFailed = false;
			this.stillNotSpawned = false;
			this.activeChildIndex = activeGuard.getSecond();
			this.activeChild = this.children.get(this.activeChildIndex).createExecutor(
					this.getExecutor(), this);
			this.activeChild.addTaskListener(this);
			this.activeChild.spawn(this.getContext());

			/* Reset the guards evaluators. */
			resetGuardsEvaluation();
		}
	}

	/**
	 * Just terminates the currently active child (if there is one).
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTerminate()
	 */
	protected void internalTerminate() {
		/*
		 * This null check is necessary. Keep in mind that the dynamic priority list may not have an
		 * active child, since the spawning process may have failed or not started yet. In such a
		 * case, if it is terminated, "this.activeChild" will be null.
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
	 * Checks if there is an active guard with a priority higher than that of the active child. If
	 * there is such a task, it terminates the active child and spawns the child of the guard with
	 * higher priority, and {@link Status#RUNNING} is returned. If there is no such task, then the
	 * status of the active child is returned.
	 * <p>
	 * If the spawning process failed, this method just returns {@link Status#FAILURE}. If the
	 * spawning process has not finished yet, this method keeps evaluating the guards, and returns
	 * {@link Status#RUNNING}.
	 * 
	 * @see jbt.execution.core.ExecutionTask#internalTick()
	 */
	protected Status internalTick() {
		/* If the spawning process failed, return failure. */
		if (this.spawnFailed) {
			return Status.FAILURE;
		}

		/* Evaluate guards. */
		Pair<Status, Integer> activeGuard = evaluateGuards();

		/*
		 * If no child has been spawned yet (not all the guards had completed yet in the
		 * internalSpawn() method)...
		 */
		if (this.stillNotSpawned) {
			/* If all the guards have failed, return failure. */
			if (activeGuard.getFirst() == Status.FAILURE) {
				return Status.FAILURE;
			} else if (activeGuard.getFirst() == Status.RUNNING) {
				/*
				 * If not all the guards have finished, do no nothing (return RUNNING).
				 */
			} else {
				/*
				 * If all the guards have been evaluated and one succeeded, spawn the child.
				 */
				this.spawnFailed = false;
				this.stillNotSpawned = false;
				this.activeChildIndex = activeGuard.getSecond();
				this.activeChild = this.children.get(this.activeChildIndex).createExecutor(
						this.getExecutor(), this);
				this.activeChild.addTaskListener(this);
				this.activeChild.spawn(this.getContext());

				/* Reset the guards evaluators. */
				resetGuardsEvaluation();
			}

			return Status.RUNNING;
		}

		/* If this point has been reached, there must be an active child. */
		if (activeGuard.getFirst() == Status.FAILURE) {
			/* If all the guards have failed, return failure. */
			return Status.FAILURE;
		} else if (activeGuard.getFirst() == Status.RUNNING) {
			/*
			 * If the guards are being evaluated, return the status of the active child.
			 */
			return this.activeChild.getStatus();
		} else {
			if (activeGuard.getSecond() != this.activeChildIndex) {
				/*
				 * If the child with the highest priority guard has changed, terminate the currently
				 * active child.
				 */
				this.activeChild.terminate();
				this.activeChildIndex = activeGuard.getSecond();

				/*
				 * Spawn the new child.
				 */
				this.activeChild = this.children.get(this.activeChildIndex).createExecutor(
						this.getExecutor(), this);
				this.activeChild.addTaskListener(this);
				this.activeChild.spawn(this.getContext());

				resetGuardsEvaluation();
				return Status.RUNNING;
			} else {
				/*
				 * If the child with the highest priority guard has not changed, return the status
				 * of the active child.
				 */
				resetGuardsEvaluation();
				return this.activeChild.getStatus();
			}
		}
	}

	/**
	 * Does nothing.
	 * 
	 * @see jbt.execution.core.ExecutionTask#restoreState(ITaskState)
	 */
	protected void restoreState(ITaskState state) {
	}

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
	 * Resets the evaluation of all the guards. This method leaves all the guard executors (
	 * {@link #guardsExecutors}) ready to start again the evaluation of the guards. It internally
	 * terminates the IBTExecutor of each guard, creates a new one, and then ticks it.
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

		this.indexMostRelevantGuard = 0;
	}

	/**
	 * Evaluate all the guards that have not finished yet, that is, those whose result in
	 * {@link #guardsResults} is {@link Status#RUNNING}, by ticking them.
	 * <p>
	 * If all the guards have finished in failure, this method returns a Pair whose first element is
	 * {@link Status#FAILURE}. If guards' evaluation has not completed yet, the first element of the
	 * Pair contains {@link Status#RUNNING}. If all the guards have been evaluated and at least one
	 * has succeeded, the first element of the Pair is {@link Status#SUCCESS}, and the second one is
	 * the index, over the list of guards ({@link #guardsExecutors}) , of the first guard (that with
	 * the highest priority) that has succeeded.
	 * 
	 */
	private Pair<Status, Integer> evaluateGuards() {
		/*
		 * Tick all the guards that are still running. If one changes its status to SUCCESS and it
		 * matches the guard associated to "indexMostRelevantGuard", then the guards' evaluation is
		 * over and that is the selected guard.
		 */
		for (int i = 0; i < this.guardsExecutors.size(); i++) {
			IBTExecutor guardExecutor = this.guardsExecutors.get(i);

			if (guardExecutor != null) {
				if (this.guardsResults.get(i) == Status.RUNNING) {
					longTick(guardExecutor);

					this.guardsResults.set(i, guardExecutor.getStatus());

					if (guardExecutor.getStatus() != Status.RUNNING) {
						/*
						 * If the guard has finished, we check if it matches the
						 * "most relevant guard".
						 */
						if (i == this.indexMostRelevantGuard) {
							if (guardExecutor.getStatus() == Status.SUCCESS) {
								return new Pair<Status, Integer>(Status.SUCCESS, i);
							} else {
								/*
								 * If the guard failed, we have to find the next
								 * "most relevant guard" and update "indexMostRelevantGuard"
								 * accordingly. For that we check the status of the following
								 * guards. If we find a successful guard before any running guard,
								 * then the guards' evaluation is over, and that is the selected
								 * guard. If we find a running guard before, then that's the new
								 * "most relevant guard". Otherwise, the evaluation has failed, and
								 * there is no successful guard.
								 */
								boolean oneRunning = false;

								for (int k = this.indexMostRelevantGuard + 1; k < this.guardsExecutors
										.size(); k++) {
									if (this.guardsExecutors.get(k) != null) {
										Status currentResult = this.guardsExecutors.get(k)
												.getStatus();
										if (currentResult == Status.RUNNING) {
											this.indexMostRelevantGuard = k;
											oneRunning = true;
											break;
										} else if (currentResult == Status.SUCCESS) {
											return new Pair<Status, Integer>(Status.SUCCESS, k);
										}
									} else {
										return new Pair<Status, Integer>(Status.SUCCESS, k);
									}
								}

								if (!oneRunning) {
									return new Pair<Status, Integer>(Status.FAILURE, -1);
								}
							}
						}
					}
				}
			} else {
				/* Remember, null guard means successful evaluation. */
				if (i == this.indexMostRelevantGuard) {
					return new Pair<Status, Integer>(Status.SUCCESS, i);
				}
			}
		}

		return new Pair<Status, Integer>(Status.RUNNING, -1);
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
	 * This method ticks <code>executor</code> {@value #NUM_TICKS_LONG_TICK} times. If the executor
	 * finishes earlier, it is not ticked anymore, and the ticking process stops.
	 * 
	 * @param executor
	 *            the IBTExecutor that is ticked.
	 */
	private void longTick(IBTExecutor executor) {
		if (executor.getStatus() == Status.RUNNING || executor.getStatus() == Status.UNINITIALIZED) {
			int counter = 0;
			do {
				executor.tick();
				counter++;
			} while (executor.getStatus() == Status.RUNNING && counter < NUM_TICKS_LONG_TICK);
		}
	}

	/** Number of ticks performed in each long tick ({@link #longTick(IBTExecutor)}). */
	private static final int NUM_TICKS_LONG_TICK = 20;
}
