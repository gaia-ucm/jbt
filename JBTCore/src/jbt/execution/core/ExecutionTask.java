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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jbt.exception.IllegalReturnStatusException;
import jbt.exception.NotTickableException;
import jbt.exception.SpawnException;
import jbt.exception.TickException;
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.execution.core.event.ITaskListener;
import jbt.execution.core.event.TaskEvent;
import jbt.model.core.ModelTask;
import jbt.model.core.ModelTask.Position;

/**
 * A behaviour tree is conceptually modeled by the ModelTask class. A ModelTask,
 * however, does not know how to run, since it is just a conceptual abstraction.
 * <p>
 * The ExecutionTask represents a tasks that knows how to run a particular
 * ModelTask. For each type of ModelTask, such as ModelSequence or
 * ModelParallel, there is an ExecutionTask that knows how to run it (e.g.,
 * ExecutionSequence and ExecutionParallel).
 * <p>
 * ExecutionTask works together with the BTExecutor class to run its
 * corresponding ModelTask (an ExecutionTask does have a managing BTExecutor).
 * An ExecutionTask defines several methods for the different stages it goes
 * through.
 * <ul>
 * <li>{@link #spawn(IContext)} is initially called when the task needs to
 * create the hierarchical structure of ExecutionTask objects that, as a whole,
 * are able to run the parent ExecutionTask.
 * <li>{@link #tick()} is used from then on, in order to give the ExecutionTask
 * some time to think and evolve according to its semantics.
 * <li>{@link #terminate()} is used when the task needs to be abruptly
 * terminated.
 * </ul>
 * 
 * The three methods above depend on the implementation that each subclass makes
 * of the {@link #internalSpawn()}, {@link #internalTick()} and
 * {@link #internalTerminate()} methods respectively. These three protected
 * abstract methods are in charge of carrying out the actual spawning, ticking
 * and termination processes. It has to be noted that it is very important the
 * way that the ExecutionTask class interacts with other ExecutionTask classes
 * as well as with the BTExecutor. With respect to the BTExecutor, the
 * ExecutionTask asks to be inserted and removed from both the lists of open and
 * tickable nodes of the BTExecutor. Subclasses only have to worry about
 * requesting to be inserted into the list of tickable nodes. Other types of
 * insertions and removals are automatically handled by the ExecutionTask class.
 * 
 * @see ModelTask
 * @see BTExecutor
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ExecutionTask implements ITaskListener {
	/** The ModelTask this ExecutionTask is running. */
	private ModelTask modelTask;
	/** The BTExecutor that is managing this ExecutionTask. */
	private BTExecutor executor;
	/** The context of the task. */
	private IContext context;
	/**
	 * List of all the listeners that are listening to TaskEvent from this task.
	 */
	private List<ITaskListener> listeners;
	/** Current status of the task. */
	private Status status;
	/** Flag telling whether the task can be spawned or not. */
	private boolean spawnable;
	/** Flag indicating whether the task can ticked or not. */
	private boolean tickable;
	/** Flag indicating whether the task has been terminated or not. */
	private boolean terminated;
	/** The parent ExecutionTask. null if this is the root of the tree. */
	private ExecutionTask parent;
	/**
	 * The position that the task occupies in the execution tree. Note that this
	 * position does not necessarily match that of the underlying ModelTask.
	 * This position is computed from the parent ExecutionTask when the
	 * ExecutionTask is created.
	 */
	private Position position;

	/**
	 * Enum defining the possible states of an ExecutionTask. Throughout its
	 * execution, an ExecutionTask may be in several states:
	 * 
	 * <ul>
	 * <li> {@link #FAILURE}: means the task has failed, that is, it could not
	 * complete successfully.
	 * <li> {@link #SUCCESS}: means the task has completed successfully.
	 * <li> {@link #RUNNING}: means the task is still running.
	 * <li> {@link #TERMINATED}: means the task has been abruptly terminated. It
	 * is conceptually similar to {@link #FAILURE}, so whenever a task has been
	 * terminated, it is also considered to have failed.
	 * <li> {@link #UNINITIALIZED}: means the task has not been spawned yet, that
	 * is, it has not started executing.
	 * </ul>
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static enum Status {
		/** Status code meaning the task has failed. */
		FAILURE, /** Status code meaning the task has succeeded. */
		SUCCESS, /** Status code meaning the task is still running. */
		RUNNING, /**
		 * Status code meaning the task has been abruptly terminated.
		 * It is conceptually similar to {@link #FAILURE}, so whenever a task
		 * has been terminated, it is also considered to have failed.
		 */
		TERMINATED,
		/**
		 * Status code meaning the task has not been spawned yet, that is, it
		 * has not started executing.
		 */
		UNINITIALIZED,
	}

	/**
	 * Constructs an ExecutionTask with an associated ModelTask and a
	 * BTExecutor. The ModelTask represents the conceptual task that the
	 * ExecutionTask is running, and the BTExecutor is that in charge of the
	 * ExecutionTask. Also, the parent of the ExecutionTask must be provided.
	 * 
	 * @param modelTask
	 *            the ModelTask this ExecutionTask will run.
	 * @param executor
	 *            the BTExecutor managing this task.
	 * @param parent
	 *            the parent ExecutionTask, or null in case this is the root of
	 *            the tree.
	 */
	public ExecutionTask(ModelTask modelTask, BTExecutor executor, ExecutionTask parent) {
		this.modelTask = modelTask;
		this.executor = executor;
		this.listeners = new LinkedList<ITaskListener>();
		this.spawnable = true;
		this.tickable = false;
		this.terminated = false;
		this.status = Status.UNINITIALIZED;
		this.parent = parent;

		/* Compute the position of this node. */
		if (parent == null) {
			this.position = new Position();
		} else {
			this.position = new Position(parent.position);
			int nextMove = getMove();
			this.position.addMove(nextMove);
		}
	}

	/**
	 * This method is called the very first time the task has to be executed.
	 * <p>
	 * This method is in charge of creating all the structure down the hierarchy
	 * of the BT that is needed to actually run the task. This process will
	 * probably include creating and spawning, in a recursive manner, some of
	 * the children of this task.
	 * <p>
	 * <code>spawn()</code> is not an abstract method. In reality, the actual
	 * spawning process is not carried out by <code>spawn()</code> itself, but
	 * by the abstract {@link #internalSpawn()} method. What
	 * <code>spawn()</code> does is to just restore the previous state of the
	 * task (if any), and then call <code>internalSpawn()</code>.
	 * <code>internalSpawn()</code> has a different implementation for every
	 * subclass of ExecutionTask, since it is that method the one that in fact
	 * carries out the spawning process. <b>Thus, subclasses must define the
	 * abstract <code>internalSpawn()</code> method</b>.
	 * <p>
	 * This method also stores the execution context (<code>context</code>) to
	 * be used by the task, which will be accessible through
	 * {@link #getContext()} after calling this method.
	 * 
	 * @param context
	 *            the context that the task will use.
	 */
	public final void spawn(IContext context) throws SpawnException {
		/* If the task cannot be spawned, throw an exception. */
		if (!this.spawnable) {
			throw new SpawnException("The task cannot be spawned. It has already been spawned.");
		}

		/*
		 * Store the context.
		 */
		this.context = context;

		this.spawnable = false;
		this.tickable = true;

		/* Set the current status of the task to Status.RUNNING. */
		this.status = Status.RUNNING;

		/*
		 * Request to be inserted into the list of open tasks.
		 */
		this.executor.requestInsertionIntoList(BTExecutorList.OPEN, this);

		/*
		 * Restore the past state of the task in case it has any.
		 */
		ITaskState previousState = this.executor.getTaskState(getPosition());
		restoreState(previousState);

		/*
		 * Carry out the actual spawn.
		 */
		internalSpawn();
	}

	/**
	 * This is the method that carries out the actual spawning process of the
	 * ExecutionTask. Sublclasses must define it, since the spawning process
	 * varies depending on the type of the task.
	 * <p>
	 * <code>internalSpawn()</code> is called from the {@link #spawn(IContext)}
	 * method. When <code>internalSpawn()</code> is called, the context of the
	 * task is already accessible through {@link #getContext()}, so it can be
	 * used by the task.
	 * <p>
	 * <code>internalSpawn()</code> is the method that creates all the structure
	 * of interconnected tasks (ExecutionTask) that are necessary to run this
	 * task. Each subclass is spawned in a different way. For instance, when a
	 * sequence task is spawned, it has to spawn its first child, but when a
	 * parallel task is spawned, it has to spawn all of its children.
	 * <p>
	 * An ExecutionTask contains a reference to a ModelTask which is trying to
	 * run. When <code>internalSpawn()</code> is called, it has to create,
	 * according to the semantics of the task, new ExecutionTask objects for the
	 * children of the ModelTask.
	 * <p>
	 * For instance, let us suppose that there is a ModelSequence class
	 * subclassing ModelTask. The ExecutionTask associated to ModelSequence is
	 * ExecutionSequence. An ExecutionSequence has a reference to the
	 * ModelSequence it is running. When ExecutionSequence is spawned, it has to
	 * create, <i>according to the semantics of the task, new ExecutionTask
	 * objects for the children of the ModelTask</i>. In this case it means that
	 * the first child of the sequence should also be spawned (in a recursive
	 * manner). Therefore, what the ExecutioSequence has to is to take the first
	 * child of the ModelSequence (let us call it <i>child</i>), which will be a
	 * ModelTask. Then, it will have to create the appropriate ExecutionTask for
	 * <i>child</i>, by calling <i>child.createExecutor()</i>. Finally, it will
	 * have to call the <code>spawn()</code> method recursively on the
	 * ExecutionTask returned by <i>child.createExecutor()</i>. Other tasks
	 * would behave differently. For instance, when an ExecutionParallel
	 * (associated to a ModelParallel) is spawned, it has to create an
	 * ExecutionTask for all of the children of its ModelParallel, and
	 * recursively spawn every single one of them.
	 * <p>
	 * Leaf tasks (also known as <i>low level task</i>, since they usually -but
	 * not always- perform a game-dependent process), such as actions and
	 * conditions, are spawned in a different way. They do not recursively spawn
	 * any child, since they have none. When a leaf task is spawned, it should
	 * start the execution of the process associated to the node. Keep in mind
	 * that low level tasks may perform long processes that require several
	 * ticks in order to complete. It is in this method that those processes
	 * start (maybe in independent threads).
	 * <p>
	 * It should be noted, however, that many processes may be instantaneous, so
	 * they may complete even within the <code>internalSpawn()</code> method.
	 * Nevertheless, in these cases the BT should not evolve, reason why the
	 * termination notification to its parent is carried out in the
	 * <code>tick()</code> method, probably in the next AI cycle. If
	 * <code>spawn()</code> were allowed to notify parents when the task
	 * terminates, then a single call to <code>spawn()</code> may take too long
	 * to complete due to the uninterrupted evolution of the tree, which is
	 * something that has to be avoided.
	 * <p>
	 * An important part of the spawning process is to decide if the
	 * ExecutionTask will enter the list of tickable nodes of the BTExecutor.
	 * Only tasks that request to be inserted into that list are ticked at every
	 * game AI cycle (when {@link BTExecutor#tick()} is called). In order to
	 * request it, the task has to call
	 * {@link #requestInsertionIntoTickableList()}. In general, all leaf tasks
	 * should be ticked every cycle, since the progress of parent tasks depends
	 * on the termination of their children. However, non-leaf tasks may also
	 * need ticking. For instance, the dynamic priority list task needs to
	 * constantly receive ticks, since it has to check its children's guards all
	 * the time -the dynamic priority list can evolve not only because of the
	 * termination of the currently active child, but also because of the
	 * reevaluation of guards-. In general, if the only way of making a task
	 * evolve is through the notification of termination from one or several of
	 * its children, then the task should not be in the list of tickable nodes.
	 * On the other hand, if a task can evolve because of factors other than the
	 * termination of one or several of its children, then it should request to
	 * be inserted into the list of tickable nodes.
	 */
	protected abstract void internalSpawn();

	/**
	 * After spawning an ExecutionTask, <code>tick()</code> has to be called in
	 * order to update it and keep track of its status.
	 * <p>
	 * This method is in charge of updating this ExecutionTask according to its
	 * semantics. This process may include spawning none, one, or several of its
	 * children depending on their termination status. It may also have to
	 * terminate some of its children.
	 * <p>
	 * <code>tick()</code> is not an abstract method. In reality, the actual
	 * ticking process is not carried out by <code>tick()</code> itself, but by
	 * the abstract {@link #internalTick()} method. <b>Thus, subclasses must
	 * define the abstract <code>internalSpawn()</code> method</b>.
	 * <p>
	 * What <code>tick()</code> does is to call <code>internalTick()</code> to
	 * carry out the actual ticking process. Then, <code>tick()</code> checks
	 * if, after the tick, the task has finished (that is, it checks if the
	 * status returned by <code>internalTick()</code> is {@link Status#FAILURE}
	 * or {@link Status#SUCCESS}). If so, <code>tick()</code> fires a TaskEvent
	 * to the parent of this ExecutionTask, so the parent does whatever it has
	 * to do after the termination of its child, and also requests to be removed
	 * from both the lists of tickable and open nodes of the BTExecutor (because
	 * this task will not be ticked again unless it is spawned again). Also, if
	 * the ExecutionTask has finished, <code>tick()</code> stores the current
	 * state of the task just in case it is spawned again in the future.
	 * <p>
	 * <b>It should be noted that when a task has been terminated (
	 * {@link #terminate()}), <code>tick()</code> does nothing</b>, and it just
	 * returns {@link Status#TERMINATED} (it does not even fire a TaskEvent).
	 * 
	 * @return the status of the task after being ticked.
	 */
	public final Status tick() throws TickException {
		/* If the task cannot be ticked, throw an exception. */
		if (!this.tickable) {
			throw new NotTickableException("The task cannot be ticked. It must be spawned first.");
		}

		/* If the task has been terminated, do nothing. */
		if (!this.terminated) {
			/* Otherwise, perform the actual tick by calling "internalTick()". */
			Status newStatus = this.internalTick();

			/* Check if the value that is returned by "internalTick()" is valid. */
			if (!validInternalTickStatus(newStatus)) {
				throw new IllegalReturnStatusException(newStatus.toString()
						+ " cannot be returned by ExecutionTask.internalTick()");
			}

			this.status = newStatus;

			/*
			 * If the task has finished (either successfully or in failure), a
			 * TaskEvent has to be fired in order to notify its parent about the
			 * termination. Before firing the event, the current state of the
			 * task has to be stored into the BTExecutor, just in case it needs
			 * to be restored in the future. The task also requests to be
			 * removed from both the list of tickable and open nodes.
			 * 
			 * Otherwise the task has not finished, so nothing in particular is
			 * done.
			 */
			if (newStatus != Status.RUNNING) {
				ITaskState taskState = storeState();
				this.executor.setTaskState(getPosition(), taskState);
				this.executor.requestRemovalFromList(BTExecutorList.TICKABLE, this);
				this.executor.requestRemovalFromList(BTExecutorList.OPEN, this);

				fireTaskEvent(newStatus);
			}

			return newStatus;
		} else {
			return Status.TERMINATED;
		}
	}

	/**
	 * <code>internalTick()</code> is the method that actually carries out the
	 * ticking process of an ExecutionTask. Subclasses must define it, since the
	 * ticking process varies depending on the type of the task.
	 * <p>
	 * <code>internalTick()</code> is called from the {@link #tick()} method.
	 * When it is called, it must assume that the task has already been spawned
	 * ({@link #spawn(IContext)}) and that the context of the task is already
	 * accessible through {@link #getContext()}.
	 * <p>
	 * <code>internalTick()</code> is the method that is used to update an
	 * ExecutionTask. Behaviour trees are driven by ticks, which means that they
	 * only evolve when they are ticked (otherwise put, behaviour trees are
	 * given CPU time only when they are ticked). <code>internalTick()</code> is
	 * the method that implements the ticking process of the task. Therefore,
	 * when it is called, and according to the semantics of the task, it will
	 * have to do some processes to make the task go on. This processes may
	 * include spawning other children or even terminating currently running
	 * children.
	 * <p>
	 * For instance, let us suppose that there is a ModelSequence class
	 * subclassing ModelTask. The ExecutionTask associated to ModelSequence is
	 * ExecutionSequence. An ExecutionSequence has a reference to the
	 * ModelSequence it is running. When ExecutionSequence is ticked, it has to
	 * update the task <i>according to the semantics of the task</i>. In this
	 * case it means that it has to analyze the current status of the current
	 * active child (through {@link #getStatus()}). If the child is still
	 * running, the ticking process just does nothing, since the sequence cannot
	 * go on unless the current child finishes. Nevertheless, if the child has
	 * successfully finished, the ExecutionSequence will have to spawn the next
	 * task of the sequence. In order to do so, the ExecutionSequence will
	 * access it through its ModelSequence. A new ExecutionTask will be created
	 * for the next child of the ModelSequence (via the
	 * <code>ModelTask.createExecutor()</code>) method, and then it will be
	 * spawned (in this case, <code>internalTick()</code> will return
	 * {@link Status#RUNNING}). However, if the child has not finished
	 * successfully, the sequence has to be aborted, so the ticking process will
	 * just return the failure status code {@link Status#FAILURE} (from the
	 * outside, the <code>tick()</code> method will catch this termination code
	 * and, as a result, it will fire a TaskEvent to notify the parent of the
	 * ExecutionSequence).
	 * <p>
	 * The ticking process of the ExecutionParallel task is very different. When
	 * <code>internalTick()</code> is called, the ExecutionParallel has to check
	 * the current status of all of its children. If one of them has failed,
	 * then all the children must be terminated, and the failure code
	 * {@link Status#FAILURE} must be returned. If all of its children have
	 * successfully finished, then the ExecutionParallel will just return
	 * {@link Status#SUCCESS}. Otherwise, it will return {@link Status#RUNNING}.
	 * <p>
	 * Leaf tasks (<i>low level task</i>), such as actions and conditions, are
	 * ticked in a different way. They do not have to analyze the termination
	 * status of any child, since they have none. When a leaf task is ticked, it
	 * should check the termination status of the process associated to the
	 * task, and return a termination status accordingly. <b>It should be noted
	 * that when a task has been terminated ( {@link #terminate()}),
	 * <code>tick()</code> does nothing</b>.
	 * <p>
	 * It should be noted that when a task has been terminated (
	 * {@link #terminate()}), <code>tick()</code> does nothing. In particular,
	 * <code>tick()</code> will not call <code>internalTick()</code>.
	 * <b>Therefore, it can be assumed that if <code>internalTick()</code> is
	 * called, then this task has not been terminated</b>, so the implementation
	 * of this method should not even consider other cases.
	 * <p>
	 * An important aspect of this method is that, even though it returns an
	 * Status object, only certain return values are allowed. In particular,
	 * only {@link Status#SUCCESS}, {@link Status#FAILURE} and
	 * {@link Status#RUNNING} can be returned.
	 * 
	 * @return the status of the task after being ticked.
	 */
	protected abstract Status internalTick();

	/**
	 * This method stores the persistent state of an ExecutionTask. Some tasks
	 * need to keep some information throughout the execution of the tree.
	 * <p>
	 * Some tasks in BTs are persistent in the sense that, after finishing, if
	 * they are spawned again, they remember past information. Take for example
	 * the "limit" task. A "limit" task allows to run its child node only a
	 * certain number of times (for example, 5). After being spawned, it has to
	 * remember how many times it has been run so far, so that, once the
	 * threshold is exceeded, it fails.
	 * <p>
	 * The problem here is that tasks are destroyed when they leave the list of
	 * tickable tasks. Thus, if the task needs to be used again, a new instance
	 * for the task must be created, which, of course, will not remember past
	 * information since it is a new object. This method is used for storing
	 * information that needs to be used in the future when the task gets
	 * created again. In particular, this method is called in the
	 * {@link #tick()} function just after noticing that the task has finished
	 * (when <code>internalTick()</code> returns a termination status). By doing
	 * so, the task stores its state as soon as possible just in case it needs
	 * to be spawned immediately afterwards.
	 * <p>
	 * This method must return the information it needs to remember in a a
	 * {@link ITaskState} object, which must be comprehensible by the
	 * {@link #restoreState(ITaskState)}, that is, it is
	 * <code>restoreState()</code> that knows how to restore the state of the
	 * task by reading the information that <code>storeState()</code> returns.
	 * <p>
	 * This method is called when the task finishes, so its implementation
	 * should take into account that it will be called only when
	 * {@link #internalTick()} returns a Status different from
	 * {@link Status#RUNNING}.
	 * <p>
	 * This method may return null if the task does not need to store any state
	 * information for future use.
	 * 
	 * @return an ITaskState object with the persistent state information of the
	 *         task, for future use. The returned ITaskState must be readable by
	 *         <code>restoreState()</code>.
	 */
	protected abstract ITaskState storeState();

	/**
	 * This method follows the same semantics as {@link #storeState()}. However,
	 * it is called when the task is terminated ( {@link #terminate()} ). When a
	 * task is abruptly terminated, it may want to store some state information
	 * for future use. It is in this method where such information should be
	 * returned.
	 * 
	 * @return an ITaskState object with the persistent state information of the
	 *         task, for future use. The returned ITaskState must be readable by
	 *         <code>restoreState()</code>.
	 */
	protected abstract ITaskState storeTerminationState();

	/**
	 * This method restores the persistent state of an ExecutionTask from an
	 * {@link ITaskState} object. Some tasks need to keep some information
	 * throughout the execution of the tree.
	 * <p>
	 * Some tasks in BTs are persistent in the sense that, after finishing, if
	 * they are spawned again, they remember past information. Take for example
	 * the "limit" task. A "limit" task allows to run its child node only a
	 * certain number of times (for example, 5). After being spawned, it has to
	 * remember how many times it has been run so far, so that, once the
	 * threshold is exceeded, it fails.
	 * <p>
	 * The problem here is that tasks are destroyed when they leave the list of
	 * tickable tasks. Thus, if the task needs to be used again, a new instance
	 * for the task must be created, which, of course, will not remember past
	 * information since it is a new object. This method is used for retrieving
	 * from the ITaskState object past information that has previously returned
	 * by either the {@link #storeState()} or the
	 * {@link #storeTerminationState()} method. In particular, this method is
	 * called in the {@link #spawn(IContext)} method, just before
	 * {@link #internalSpawn()} gets called. By doing so, the task that is
	 * created is able to restore past information needed to work. Since this
	 * method is called before the task is spawned (that is, before
	 * <code>internalSpawn()</code> is called), it must be assumed that the task
	 * always keeps its past state.
	 * <p>
	 * This method reads the information that either <code>storeState()</code>
	 * or <code>storeTerminationState()</code> has previously returned..
	 * Therefore, it must follow the same format as that of both methods. It the
	 * input ITaskState is null, it means that there is no past information to
	 * retrieve, so the task should be left unchanged.
	 * <p>
	 * This method may be left empty if the task does not need to restore any
	 * past state.
	 * 
	 * @param state
	 *            an ITaskState object containing past state information that
	 *            should be retrieved, or null in case there is no past
	 *            information to remember.
	 */
	protected abstract void restoreState(ITaskState state);

	/**
	 * Returns the context of the task.
	 * 
	 * @return the context of the task.
	 */
	public IContext getContext() {
		return this.context;
	}

	/**
	 * Adds a task listener to this task. When there is a relevant change in the
	 * status of this task, the listener will be notified by calling its
	 * {@link ITaskListener#statusChanged(TaskEvent)} method.
	 * 
	 * @param listener
	 *            the listener to add.
	 */
	public void addTaskListener(ITaskListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes a listener from this task.
	 * 
	 * @param listener
	 *            the task listener to remove.
	 */
	public void removeTaskListener(ITaskListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * 
	 * @see jbt.execution.core.event.ITaskListener#statusChanged(jbt.execution.core.event.TaskEvent)
	 */
	public abstract void statusChanged(TaskEvent e);

	/**
	 * Returns the current status of the task.
	 * 
	 * @return the current status of the task.
	 */
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Returns the BTExecutor of this ExecutionTask.
	 * 
	 * @return the BTExecutor of this ExecutionTask.
	 */
	public BTExecutor getExecutor() {
		return this.executor;
	}

	/**
	 * Returns the ModelTask associated to this ExecutionTask.
	 * 
	 * @return the ModelTask associated to this ExecutionTask.
	 */
	public ModelTask getModelTask() {
		return this.modelTask;
	}

	/**
	 * Returns the position of the ExecutionTask in the execution tree. Note
	 * that such position is not necessarily that of the underlying ModelTask.
	 * 
	 * @return the position of the ExecutionTask in the execution tree.
	 */
	public Position getPosition() {
		return this.position;
	}

	/**
	 * Returns true if the task has been spawned, and false otherwise.
	 * 
	 * @return true if the task has been spawned, and false otherwise.
	 */
	public boolean getSpawned() {
		return this.spawnable == false;
	}

	/**
	 * Returns true if the task has been terminated, and false otherwise.
	 * 
	 * @return true if the task has been terminated, and false otherwise.
	 */
	public boolean getTerminated() {
		return this.terminated;
	}

	/**
	 * Terminates the execution of this task and all the tasks below it.
	 * <p>
	 * When this method is called, the task is marked as terminated. From then
	 * on, ticking the task will have no effect, and its status will be
	 * {@link Status#TERMINATED}. Also, when terminating the task, it requests
	 * to be removed from both the list of tickable and open nodes of the
	 * BTExecutor. It also stores the task state after being terminated, by
	 * calling {@link #storeTerminationState()}.
	 * <p>
	 * Finally, this method calls the abstract method
	 * {@link #internalTerminate()}. <code>internalTerminate()</code> is the
	 * method that actually terminates the execution of the task and all the
	 * tasks below it, usually by calling <code>terminate()</code> on the active
	 * children and by stopping any process associated to the task. <b>
	 * <code>internalTerminate()</code> must be defined for each subclass, since
	 * their termination processes differ from one another</b>.
	 * <p>
	 * This method cannot be called if the task has not been spawned yet (an
	 * exception is thrown in that case). However, it is valid terminating a
	 * task that has already been terminated, in which case nothing happens.
	 */
	public final void terminate() {
		if (!this.tickable) {
			throw new RuntimeException("Cannot terminate a task that has not been spawned yet.");
		}
		if (!this.terminated) {
			this.terminated = true;
			this.status = Status.TERMINATED;
			this.executor.requestRemovalFromList(BTExecutorList.TICKABLE, this);
			this.executor.requestRemovalFromList(BTExecutorList.OPEN, this);
			ITaskState taskState = this.storeTerminationState();
			this.executor.setTaskState(getPosition(), taskState);
			this.internalTerminate();
		}
	}

	/**
	 * This method is called form {@link #terminate()}, and it is the one that
	 * actually terminates the ExecutionTask as well as all the tasks below it.
	 * <p>
	 * <code>internalTerminate()</code> has to stop all the processes associated
	 * to the ExecutionTask. For non-leaf tasks, this usually means that it has
	 * to terminate all its active children (by recursively calling
	 * <code>terminate()</code> on them). For leaf tasks, this means that it has
	 * to terminate the processes associated to them, so that they stop doing
	 * things.
	 * <p>
	 * For instance, an ExecutionParallel task has to call
	 * <code>terminate</code> on all of its alive (still running) children. An
	 * ExecutionSequence has to call <code>terminate()</code> on its current
	 * alive child. A leaf task that, say, is carrying out some process in an
	 * independent execution thread, should stop the thread.
	 * <p>
	 * This method can be called only once, and only once the task has already
	 * been spawned, so the implementation does not have to even consider what
	 * happens in other cases.
	 */
	protected abstract void internalTerminate();

	/**
	 * Fires a TaskEvent in all the listeners of this task. The TaskEvent will
	 * inform about an important change in the status of the task.
	 * 
	 * @param newStatus
	 *            the new status of the task.
	 */
	private void fireTaskEvent(Status newStatus) {
		for (ITaskListener l : this.listeners) {
			l.statusChanged(new TaskEvent(this, newStatus, this.getStatus()));
		}
	}

	/**
	 * Returns the index that the ModelTask associated to this ExecutionTask
	 * occupies in the list of children of its parent's children. Returns 0 if
	 * the this ExecutionTask's ModelTask cannot be found.
	 * <p>
	 * The fact that the 0-case is contemplated is due to the existence of the
	 * Subtree Lookup operator. The ModelSubtreeLookup does not have any
	 * children, because it is a leaf node. However, the ExecutionSubtreeLookup
	 * does have one child, which is the root of the tree that it is going to
	 * emulate. As a result, the root of the tree emulated by the Subtree Lookup
	 * cannot find itself as a child of its parent (the parent is the
	 * ExecutionSubtreeLookup); as a workaround, we can return 0, since it is
	 * the 0-th child of the ExecutionSubtreeLookup.
	 */
	private int getMove() {
		List<ModelTask> parentsChildren = this.parent.getModelTask().getChildren();
		Iterator<ModelTask> iterator = parentsChildren.iterator();
		ModelTask thisModelTask = this.getModelTask();

		for (int i = 0; i < parentsChildren.size(); i++) {
			if (iterator.next() == thisModelTask) {
				return i;
			}
		}

		return 0;
	}

	/**
	 * Checks if a Status returned by {@link #internalTick()} is valid or not.
	 * <code>internalTick()</code> can only return {@link Status#SUCCESS},
	 * {@link Status#FAILURE} and {@link Status#RUNNING}.
	 * 
	 * @param status
	 *            the status to check.
	 * @return true if <code>status</code> can be returned by
	 *         <code>internalTick()</code>, and false otherwise.
	 */
	private static boolean validInternalTickStatus(Status status) {
		if (status == Status.TERMINATED || status == Status.UNINITIALIZED) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "[" + this.getClass().getSimpleName() + ", Status: " + this.status.toString() + "]";
	}
}
