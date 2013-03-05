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
package jbt.execution.core;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jbt.execution.context.BasicContext;
import jbt.execution.core.ExecutionTask.Status;
import jbt.execution.task.decorator.ExecutionInterrupter;
import jbt.model.core.ModelTask;
import jbt.model.core.ModelTask.Position;
import jbt.model.task.decorator.ModelInterrupter;

/**
 * BTExecutor is the implementation of the IBTExecutor interface.
 * <p>
 * BTs are conceptually modeled by a hierarchy of interconnected ModelTask objects. A BT is
 * represented by the root of the tree, which is a single ModelTask object.
 * <p>
 * Therefore, in order to run a BT, a BTExecutor only needs the root task of the tree (a ModelTask
 * object), and the context (IContext) that will be used by the tree. Keep in mind that a BT is
 * executed within a context that contains information about the game that leaf tasks (actions and
 * conditions) and guards (ModelTask) may need in order to run.
 * <p>
 * The internal implementation of the BTExecutor is based on using, for each ModelTask of the BT, an
 * ExecutionTask obtained by calling the {@link ModelTask#createExecutor(BTExecutor, ExecutionTask)}
 * method. The most important feature of the BTExecutor, however, is that it uses a list of
 * <i>tickable</i> nodes. When {@link #tick()} is called, not all the nodes of the tree are ticked,
 * but only those that are currently relevant to the execution of the tree. By doing so, running the
 * tree is a much more efficient process, since only the nodes that can make the tree evolve receive
 * CPU time.
 * <p>
 * The BTExecutor is also in charge of storing the permanent state of tasks (see {@link ITaskState}
 * and {@link ExecutionTask#storeState()}). For each BT there is only one BTExecutor that actually
 * runs it. Therefore, the BTExecutor can be used as the repository for storing the state of the
 * tasks of the tree.
 * 
 * @see ModelTask
 * @see ExecutionTask
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTExecutor implements IBTExecutor {
	/** The root task of the BT this executor is running. */
	private ModelTask modelBT;
	/** The ExecutionTask associated to the root ModelTask. */
	private ExecutionTask executionBT;
	/** List of tickable tasks. */
	private List<ExecutionTask> tickableTasks;
	/** List of open tasks. */
	private List<ExecutionTask> openTasks;
	/** The context that will be passed to the root task. */
	private IContext context;
	/**
	 * Boolean telling whether this BTExecutor has been ticked ( {@link #tick()} ) before.
	 */
	private boolean firstTimeTicked = true;
	/**
	 * List of the tasks that must be inserted into the list of tickable nodes.
	 */
	private List<ExecutionTask> currentTickableInsertions;
	/**
	 * List of the tasks that must be removed from the list of tickable nodes.
	 */
	/*
	 * TODO: improve the way requests for insertions and removals are handled. Currently simple
	 * Lists are used to manage them, but in case there were many requests, this process would not
	 * be very efficient.
	 */
	private List<ExecutionTask> currentTickableRemovals;
	/**
	 * List of the tasks that must be inserted into the list of open nodes.
	 */
	private List<ExecutionTask> currentOpenInsertions;
	/**
	 * List of the tasks that must be removed from the list of open nodes.
	 */
	private List<ExecutionTask> currentOpenRemovals;
	/**
	 * List of all the ExecutionInterrupter currently active in the behaviour tree. They are indexed
	 * by their ModelInterrupter in the conceptual tree.
	 * <p>
	 * This list is used by ExecutionPerformInterruption, which must have a way of knowing what
	 * ExecutionInterrupter it is interrupting.
	 */
	private Map<ModelInterrupter, ExecutionInterrupter> interrupters;
	/**
	 * States of the tasks of the tree that is being run by this BTExecutor. States are indexed by
	 * Position. These are the positions of the ExecutionTask in the execution tree. These positions
	 * are unique (they do not necessarily match the position of the corresponding ModelTask), so
	 * each node in the execution tree can be unambiguously referenced by such position. Note that
	 * this Map does not store the states of the nodes of the guards of the tree that is being run.
	 */
	private Map<Position, ITaskState> tasksStates;

	/**
	 * Creates a BTExecutor that handles the execution of a behaviour tree. The behaviour tree is
	 * represented by a ModelTask (the root of the tree).
	 * <p>
	 * A context for the tree must be provided. This context is passed to the root of the tree, and,
	 * in general, it will be shared by all the nodes in the tree (it will be passed down the
	 * hierarchy of the tree). Note however that, depending on the semantics of the tree itself,
	 * some nodes may not use this context.
	 * 
	 * @param modelBT
	 *            the root of the behaviour tree to run.
	 * @param context
	 *            the initial context for the tree.
	 */
	public BTExecutor(ModelTask modelBT, IContext context) {
		if (modelBT == null) {
			throw new IllegalArgumentException("The input ModelTask cannot be null");
		}

		if (context == null) {
			throw new IllegalArgumentException("The input IContext cannot be null");
		}

		this.modelBT = modelBT;
		this.modelBT.computePositions();
		this.context = context;
		this.tickableTasks = new LinkedList<ExecutionTask>();
		this.openTasks = new LinkedList<ExecutionTask>();
		this.currentOpenInsertions = new LinkedList<ExecutionTask>();
		this.currentOpenRemovals = new LinkedList<ExecutionTask>();
		this.currentTickableInsertions = new LinkedList<ExecutionTask>();
		this.currentTickableRemovals = new LinkedList<ExecutionTask>();
		this.interrupters = new Hashtable<ModelInterrupter, ExecutionInterrupter>();
		this.tasksStates = new Hashtable<ModelTask.Position, ITaskState>();
	}

	/**
	 * Creates a BTExecutor that handles the execution of a behaviour tree. The behaviour tree is
	 * represented by a ModelTask (the root of the tree).
	 * <p>
	 * A new empty context for the tree is created. This context is passed to the root of the tree,
	 * and, in general, it will be shared by all the nodes in the tree (it will be passed down the
	 * hierarchy of the tree). Note however that, depending on the semantics of the tree itself,
	 * some nodes may not use the context context.
	 * 
	 * @param modelBT
	 *            the root of the behaviour tree to run.
	 */
	public BTExecutor(ModelTask modelBT) {
		if (modelBT == null) {
			throw new IllegalArgumentException("The input ModelTask cannot be null");
		}

		this.modelBT = modelBT;
		this.modelBT.computePositions();
		this.context = new BasicContext();
		this.tickableTasks = new LinkedList<ExecutionTask>();
		this.openTasks = new LinkedList<ExecutionTask>();
		this.currentOpenInsertions = new LinkedList<ExecutionTask>();
		this.currentOpenRemovals = new LinkedList<ExecutionTask>();
		this.currentTickableInsertions = new LinkedList<ExecutionTask>();
		this.currentTickableRemovals = new LinkedList<ExecutionTask>();
		this.interrupters = new Hashtable<ModelInterrupter, ExecutionInterrupter>();
		this.tasksStates = new Hashtable<ModelTask.Position, ITaskState>();
	}

	/**
	 * 
	 * @see jbt.execution.core.IBTExecutor#tick()
	 */
	public void tick() {
		/*
		 * The ticking algorithm works as follows:
		 * 
		 * If it is the very first time that this method is called, an ExecutionTask is created from
		 * the root ModelTask (that is, the root of the behaviour tree that this BTExecutor is going
		 * to run). Then, that task is spawned.
		 * 
		 * From then on, tick() will just call tick() on all the ExecutionTasks in the list of
		 * tickable tasks.
		 * 
		 * It is important to note that insertions and removals from the list of tickable and open
		 * tasks are processed at the very beginning and at the very end of this method, but not
		 * while it is ticking the current list of tickable tasks.
		 */
		Status currentStatus = this.getStatus();

		/* We only tick if the tree has not finished yet or if it has not started running. */
		if (currentStatus == Status.RUNNING || currentStatus == Status.UNINITIALIZED) {
			processInsertionsAndRemovals();

			if (this.firstTimeTicked) {
				this.executionBT = this.modelBT.createExecutor(this, null);
				this.executionBT.spawn(this.context);
				this.firstTimeTicked = false;
			} else {
				for (ExecutionTask t : tickableTasks) {
					t.tick();
				}
			}

			processInsertionsAndRemovals();
		}
	}

	/**
	 * 
	 * @see jbt.execution.core.IBTExecutor#terminate()
	 */
	public void terminate() {
		if (this.executionBT != null) {
			this.executionBT.terminate();
		}
	}

	/**
	 * Returns the ExecutionInterrupter that is currently active and registered in the BTExecutor (
	 * {@link #registerInterrupter(ExecutionInterrupter)}) whose associated ModelInterrupter is
	 * <code>modelInterrupter</code>. Returns null if there is no such an ExecutionInterrupter.
	 * 
	 * @param modelInterrupter
	 *            the ModelInterrupter associated to the ExecutionInterrupter to retrieve.
	 * @return the ExecutionInterrupter whose associated ModelInterrupter is
	 *         <code>modelInterrupter</code>.
	 */
	public ExecutionInterrupter getExecutionInterrupter(ModelInterrupter modelInterrupter) {
		return this.interrupters.get(modelInterrupter);
	}

	/**
	 * Registers an ExecutionInterrupter with this BTExecutor.
	 * 
	 * @param interrupter
	 *            the ExecutionInterrupter to register.
	 */
	public void registerInterrupter(ExecutionInterrupter interrupter) {
		this.interrupters.put((ModelInterrupter) interrupter.getModelTask(), interrupter);
	}

	/**
	 * Unregisters an ExecutionInterrupter from this BTExecutor.
	 * 
	 * @param interrupter
	 *            the ExecutionInterrupter to unregister.
	 */
	public void unregisterInterrupter(ExecutionInterrupter interrupter) {
		this.interrupters.remove(interrupter.getModelTask());
	}

	/**
	 * Enum defining the relevant lists that the BTExecutor handles.
	 * 
	 * <ul>
	 * <li> {@link #OPEN}: the list of open (active) nodes.
	 * <li> {@link #TICKABLE}: the list of tickable nodes, that is, those that receive ticks every
	 * time {@link BTExecutor#tick()} is called.
	 * </ul>
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static enum BTExecutorList {
		/** Enum for the list of open nodes. */
		OPEN, /** Enum for the list of tickable nodes. */
		TICKABLE
	};

	/**
	 * Method used to request the BTExecutor to insert an ExecutionTask into one of the list that it
	 * handles. The insertion is not performed right away, but delayed until:
	 * 
	 * <ul>
	 * <li>Either the current game AI cycle (call to {@link #tick()}) finishes. This happens if the
	 * insertion is requested in the middle of an AI cycle, that is, if <code>tick()</code> is still
	 * running.
	 * <li>Or the next AI cycle starts. This happens if the insertion is requested when the
	 * BTExecutor is not ticking the underlying BT. In this case, the next time <code>tick()</code>
	 * is called, the insertion will be processed just before the BT is actually ticked.
	 * </ul>
	 * 
	 * @param listType
	 *            the type of the list that the task will be inserted into.
	 * @param t
	 *            the task that wants to be inserted into the list of type <code>listType</code>.
	 */
	public void requestInsertionIntoList(BTExecutorList listType, ExecutionTask t) {
		if (listType == BTExecutorList.OPEN) {
			if (!this.currentOpenInsertions.contains(t)) {
				this.currentOpenInsertions.add(t);
			}
		} else {
			if (!this.currentTickableInsertions.contains(t)) {
				this.currentTickableInsertions.add(t);
			}
		}
	}

	/**
	 * Method used to request the BTExecutor to remove an ExecutionTask from one of the list that
	 * the BTExecutor handles. The removal is not performed right away, but delayed until:
	 * 
	 * <ul>
	 * <li>Either the current game AI cycle (call to {@link #tick()}) finishes. This happens if the
	 * removal is requested in the middle of an AI cycle, that is, if <code>tick()</code> is still
	 * running.
	 * <li>Or the next AI cycle starts. This happens if the removal is requested when the BTExecutor
	 * is not ticking the underlying BT. In this case, the next time <code>tick()</code> is called,
	 * the removal will be processed just before the BT is actually ticked.
	 * </ul>
	 * 
	 * @param listType
	 *            the type of the list from which the task will be removed.
	 * @param t
	 *            the task that wants to be removed from the list of type <code>listType</code>.
	 */
	public void requestRemovalFromList(BTExecutorList listType, ExecutionTask t) {
		if (listType == BTExecutorList.OPEN) {
			if (!this.currentOpenRemovals.contains(t)) {
				this.currentOpenRemovals.add(t);
			}
		} else {
			if (!this.currentTickableRemovals.contains(t)) {
				this.currentTickableRemovals.add(t);
			}
		}
	}

	/**
	 * Cancels a previous request of insertion into one of the lists that the BTExecutor handles. If
	 * no such insertion request was made, this method does nothing.
	 * 
	 * @param listType
	 *            the list from which the insertion request will be canceled.
	 * @param t
	 *            the task whose insertion will be canceled.
	 */
	public void cancelInsertionRequest(BTExecutorList listType, ExecutionTask t) {
		if (listType == BTExecutorList.OPEN) {
			this.currentOpenInsertions.remove(t);
		} else {
			this.currentTickableInsertions.remove(t);
		}
	}

	/**
	 * Cancels a previous request of removal from one of the lists that the BTExecutor handles. If
	 * no such removal request was made, this method does nothing.
	 * 
	 * @param listType
	 *            the list from which the removal request will be canceled.
	 * @param t
	 *            the task whose removal will be canceled.
	 */
	public void cancelRemovalRequest(BTExecutorList listType, ExecutionTask t) {
		if (listType == BTExecutorList.OPEN) {
			this.currentOpenRemovals.remove(t);
		} else {
			this.currentTickableRemovals.remove(t);
		}
	}

	/**
	 * Method that processes the insertions and removals into and from the lists of tickable and
	 * open nodes that have been previously requested via the <code>requestXXX</code> methods.
	 * <p>
	 * After calling this method, all pending removals and insertions are processed, so no new
	 * insertion and removal will be carried out unless new ones are requested.
	 */
	private void processInsertionsAndRemovals() {
		/*
		 * Process insertions and removals.
		 */
		for (ExecutionTask t : this.currentTickableInsertions) {
			this.tickableTasks.add(t);
		}
		for (ExecutionTask t : this.currentTickableRemovals) {
			this.tickableTasks.remove(t);
		}
		for (ExecutionTask t : this.currentOpenInsertions) {
			this.openTasks.add(t);
		}
		for (ExecutionTask t : this.currentOpenRemovals) {
			this.openTasks.remove(t);
		}
		/*
		 * Clear the lists of tasks to insert and remove.
		 */
		this.currentOpenInsertions.clear();
		this.currentOpenRemovals.clear();
		this.currentTickableInsertions.clear();
		this.currentTickableRemovals.clear();
	}

	/**
	 * 
	 * @see jbt.execution.core.IBTExecutor#getBehaviourTree()
	 */
	public ModelTask getBehaviourTree() {
		return this.modelBT;
	}

	/**
	 * 
	 * @see jbt.execution.core.IBTExecutor#getStatus()
	 */
	public Status getStatus() {
		if (this.executionBT == null) {
			return Status.UNINITIALIZED;
		} else {
			return this.executionBT.getStatus();
		}
	}

	/**
	 * 
	 * @see jbt.execution.core.IBTExecutor#getRootContext()
	 */
	public IContext getRootContext() {
		return this.context;
	}

	/**
	 * Sets the permanent state of a given task. The task is identified by the position it occupies
	 * in the execution behaviour tree, which unambiguously identifies it.
	 * 
	 * @param taskPosition
	 *            the position of the task whose state must be stored.
	 * @param state
	 *            the state of the task, or null if it should be cleared.
	 * @return true if there was a previous state for this task in the BTExecutor, or false
	 *         otherwise.
	 */
	public boolean setTaskState(Position taskPosition, ITaskState state) {
		if (state == null) {
			return this.tasksStates.remove(taskPosition) == null ? false : true;
		}
		return this.tasksStates.put(taskPosition, state) == null ? false : true;
	}

	/**
	 * Returns the permanent state of a task. The task is identified by the position it occupies in
	 * the execution behaviour tree, which unambiguously identifies it.
	 * 
	 * @param taskPosition
	 *            the position of the task whose state must be retrieved.
	 * @return the state of the task, or null if there is no state stored in the BTExecutor for the
	 *         task.
	 */
	public ITaskState getTaskState(Position taskPosition) {
		return this.tasksStates.get(taskPosition);
	}

	/**
	 * Copies the set of all tasks' states stored in <code>executor</code> into this BTExecutor.
	 * <p>
	 * <b>After calling this method, the set of all tasks' states is shared by both BTExecutor
	 * objects (<code>executor</code> and <code>this</code>), so if one modifies it, the other will
	 * notice the change.</b>
	 */
	public void copyTasksStates(BTExecutor executor) {
		this.tasksStates = executor.tasksStates;
	}

	/**
	 * Clears the permanent state of a task. The task is identified by the position it occupies in
	 * the execution behaviour tree, which unambiguously identifies it.
	 * 
	 * @param taskPosition
	 *            the position of the task whose state must be cleared.
	 * @return true if the BTExecutor contained the state of the task before calling this method, or
	 *         false otherwise.
	 */
	public boolean clearTaskState(Position taskPosition) {
		return this.tasksStates.remove(taskPosition) == null ? false : true;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[Root: " + this.modelBT.getClass().getSimpleName() + ", Status: "
				+ this.getStatus() + "]";
	}
}
