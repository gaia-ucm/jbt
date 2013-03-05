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
package jbt.model.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;

/**
 * ModelTask is a class that models a node (task) of a behaviour tree in an
 * conceptual way. A ModelTask does not have execution capabilities, since it
 * only purpose is to serve as a way of modeling a behaviour tree conceptually.
 * <p>
 * ModelTask is an abstract class, and it just acts as a container of other
 * child tasks, with maybe a guard.
 * <p>
 * As stated above, a ModelTask cannot be run. The idea behind this model is
 * that an external interpreter should be in charge of running the behaviour
 * tree (ModelTask) instead of the task itself. By doing so, there is a clear
 * separation between both the conceptual and execution model, thus allowing to
 * have a unique model shared by many interpreters (which, in other words, means
 * that a single behaviour tree can be run by many entities at the same time).
 * <p>
 * The interpreter that is used to run a behaviour tree -that is, a ModelTask
 * and all the tasks below it- is the BTExecutor class. The BTExecutor class is
 * used to run a ModelTask. A BTExecutor runs a behaviour tree by ticks. This
 * means that the tree is given some time to think and evolve only at certain
 * moments (ticks), and is doing nothing otherwise.
 * <p>
 * Every ModelTask is able to issue an ExecutionTask capable of running it (
 * {@link #createExecutor(BTExecutor, ExecutionTask)}). Actually, the BTExecutor
 * uses ExecutionTask objects in order to run the conceptual behaviour tree. An
 * ExecutionTask is just another type of task that knows how to run its
 * corresponding ModelTask (by interacting with other tasks as well as with the
 * BTExecutor). For instance, a ModelSequence, which represents a sequence task
 * in a behaviour tree, has got an ExecutionTask that knows how to run it, the
 * ExecutionSequence. The <code>createExecutor()</code> method of ModelSequence
 * just returns an instance of ExecutionSequence. Therefore, the
 * <code>createExecutor()</code> method should just return an ExecutionTask that
 * knows how to run the ModelTask.
 * 
 * @see ExecutionTask
 * @see BTExecutor
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public abstract class ModelTask {
	/** List of the children of the ModelTask. */
	private List<ModelTask> children;
	/** The position of the ModelTask in the behaviour tree. */
	private Position position;
	/**
	 * The guard of the ModelTask. It may be null, in which case it will always
	 * be evaluated to true.
	 */
	private ModelTask guard;

	/**
	 * The position of a ModelTask in a behaviour tree. It contains the sequence
	 * of moves that must be performed to go from the root to the node itself.
	 * Each of the moves of the sequence represents what child of the current
	 * node must be selected. For instance, if the position represents the
	 * sequence of moves {1,4,0}, the node that it points to is the first child
	 * (0) of the fifth child (4) of the second child (1) of the root. En empty
	 * list of moves represents the root.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class Position {
		/**
		 * The list of moves that this position represents.
		 */
		private List<Integer> moves;

		/**
		 * Constructs an Position that contains the moves specified in its
		 * constructor, in the same order. If no move is specified, the Position
		 * will represent an empty sequence of moves.
		 */
		public Position(Integer... moves) {
			this.moves = new LinkedList<Integer>();

			for (Integer i : moves) {
				this.moves.add(i);
			}
		}

		/**
		 * Constructs a Position from a sequence of moves represented as a List.
		 * 
		 * @param moves
		 *            the sequence of moves that this Position will represent.
		 */
		public Position(List<Integer> moves) {
			if (moves == null) {
				throw new RuntimeException("The list of moves cannot be null");
			}

			this.moves = new LinkedList<Integer>();
			for (Integer i : moves) {
				this.moves.add(i);
			}
		}

		/**
		 * Constructs a copy of the Position <code>pos</code>.
		 * 
		 * @param pos
		 *            the Position that is copied.
		 */
		public Position(Position pos) {
			this.moves = new LinkedList<Integer>();
			for (Integer i : pos.moves) {
				this.moves.add(i);
			}
		}

		/**
		 * Returns the sequence of moves that this Position represents.
		 * 
		 * @return the sequence of moves that this Position represents.
		 */
		public List<Integer> getMoves() {
			return new LinkedList<Integer>(this.moves);
		}

		/**
		 * Adds a move to this Position. The move is inserted as the last one of
		 * the sequence.
		 * 
		 * @param move
		 *            the move to add.
		 * @return this Position.
		 */
		public Position addMove(Integer move) {
			this.moves.add(move);
			return this;
		}

		/**
		 * Adds a list of moves to this Position. The moves are inserted in the
		 * order specified in the <code>moves</code> list.
		 * 
		 * @param moves
		 *            the list of moves to add.
		 * @return this Position.
		 */
		public Position addMoves(List<Integer> moves) {
			for (Integer i : moves) {
				this.moves.add(i);
			}
			return this;
		}

		/**
		 * Adds the moves of a Position to this Position.
		 * 
		 * @param position
		 *            the position whose moves are going to be added to this
		 *            one.
		 * @return this Position.
		 */
		public Position addMoves(Position position) {
			addMoves(position.getMoves());
			return this;
		}

		// /**
		// * Compares this Position object to another one. Let <i>A</i> and
		// * <i>B</i> be two Position objects. If <i>A</i> is at a higher
		// level
		// in
		// * the tree, then <i>A</i> is less than <i>B</i>. If <i>A</i> is at
		// a
		// * lower lever in the tree, then <i>A</i> is greater than <i>B</i>.
		// If
		// * <i>A</i> is at the same level in the tree as that of <i>B</i>,
		// then:
		// * <ul>
		// * <li>If <i>A</i> is at the left of <i>B</i>, then <i>A</i> is less
		// * than <i>B</i>.
		// * <li>If <i>A</i> represents the same sequence of moves as that of
		// * <i>B</i>, then <i>A</i> equals <i>B</i>.
		// * <li>Otherwise, <i>A</i> is greater than <i>B</i>.
		// * </ul>
		// *
		// * @see java.lang.Comparable#compareTo(java.lang.Object)
		// */
		// public int compareTo(Position o) {
		// if (this.moves.size() > o.moves.size()) {
		// return 1;
		// } else if (this.moves.size() < o.moves.size()) {
		// return -1;
		// } else {
		// Iterator<Integer> thisIt = this.moves.iterator();
		// Iterator<Integer> otherIt = o.moves.iterator();
		//
		// while (thisIt.hasNext()) {
		// Integer thisElem = thisIt.next();
		// Integer otherElem = otherIt.next();
		//
		// if (thisElem < otherElem) {
		// return -1;
		// } else if (thisElem > otherElem) {
		// return 1;
		// }
		// }
		//
		// return 0;
		// }
		// }

		/**
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			String result = new String();
			if (this.moves.size() != 0) {
				for (Integer i : this.moves) {
					result += i + " ";
				}
				return "[" + result.substring(0, result.length() - 1) + "]";
			}
			else {
				return "[]";
			}
		}

		/**
		 * Returns true if <code>o</code> is a Position object that contains the
		 * same sequence of moves as that of this. Returns false otherwise.
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (!(o instanceof Position)) {
				return false;
			}

			Position oPosition = (Position) o;

			List<Integer> thisMoves = this.getMoves();
			List<Integer> oMoves = oPosition.getMoves();

			if (oMoves.size() != thisMoves.size()) {
				return false;
			}

			Iterator<Integer> thisIt = thisMoves.iterator();
			Iterator<Integer> oIt = oMoves.iterator();

			while (thisIt.hasNext()) {
				Integer thisElem = thisIt.next();
				Integer oElem = oIt.next();

				if (!thisElem.equals(oElem)) {
					return false;
				}
			}

			return true;
		}

		/**
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return this.moves.hashCode();
		}
	}

	/**
	 * Creates a new ModelTask with a guard and several children. The guard may
	 * be null, in which case it is always evaluated to true. The task may also
	 * have no children.
	 * 
	 * @param guard
	 *            the guard, which may be null.
	 * @param children
	 *            the list of children.
	 */
	public ModelTask(ModelTask guard, ModelTask... children) {
		this.guard = guard;
		this.children = new Vector<ModelTask>();

		for (ModelTask t : children) {
			this.children.add(t);
		}

		this.position = new Position();
	}

	/**
	 * Returns the list of children of this task, or an empty list if it has no
	 * children. It should be noted that the children of a task are ordered, and
	 * the order influences the way the task runs, reason why a List is
	 * returned. Note that the returned list is the underlying list of children
	 * used by the task, so it should be used carefully (in general, it should
	 * never be modified).
	 * 
	 * @return the list of children of this task.
	 */
	public List<ModelTask> getChildren() {
		return this.children;
	}

	/**
	 * Returns the guard of the task, which may be null.
	 * 
	 * @return the guard of the task, which may be null.
	 */
	public ModelTask getGuard() {
		return this.guard;
	}

	/**
	 * Creates a suitable ExecutionTask that will be able to run this ModelTask
	 * through the management of a BTExecutor.
	 * 
	 * @param executor
	 *            the BTExecutor that will manage the returned ExecutionTask.
	 * @param parent
	 *            the parent ExecutionTask for the returned ExecutionTask.
	 * 
	 * @return an ExecutionTask that is able to run this ModelTask.
	 */
	public abstract ExecutionTask createExecutor(BTExecutor executor, ExecutionTask parent);

	/**
	 * Returns the position that this task occupies in the behaviour tree. If it
	 * has not been computed yet (see {@link #computePositions()}), it returns a
	 * Position object with both x and y equal to -1.
	 * 
	 * @return the position that this task occupies in the behaviour tree.
	 */
	public Position getPosition() {
		return this.position;
	}

	/**
	 * This method computes the positions of all the tasks of the behaviour tree
	 * whose root is this node. After calling this method, the positions of all
	 * the tasks below this one will be available and accessible through
	 * {@link #getPosition()}.
	 * <p>
	 * It is important to note that, when calling this method, this task is
	 * considered to be the root of the behaviour tree, so its position will be
	 * set to an empty sequence of moves, with no offset, and the positions of
	 * the tasks below it will be computed from it.
	 */
	public void computePositions() {
		/* Assume this node is the root of the tree. */
		this.position = new Position(new LinkedList<Integer>());

		/*
		 * Set the position of all of the children of this task and recursively
		 * compute the position of the rest of the tasks.
		 */
		for (int i = 0; i < this.children.size(); i++) {
			ModelTask currentChild = this.children.get(i);
			Position currentChildPos = new Position(this.position);
			currentChildPos.addMove(i);
			currentChild.position = currentChildPos;
			recursiveComputePositions(currentChild);
		}
	}

	/**
	 * This function searches for a ModelTask according to a particular
	 * Position.
	 * <p>
	 * Conceptually, a Position represents a sequence of moves. This method just
	 * applies all moves in <code>moves</code> starting from this ModelTask, and
	 * returns the reached ModelTask, or null in case it does not exist.
	 * 
	 * @param moves
	 *            the sequence of moves that must be performed to retrieve the
	 *            ModelTask.
	 * @return the ModelTask obtained by moving down the tree according to the
	 *         sequence of moves <code>moves</code>, or null in case no
	 *         ModelTask could be found.
	 */
	public ModelTask findNode(Position moves) {
		List<Integer> m = moves.getMoves();

		ModelTask currentTask = this;

		for (Integer currentMove : m) {
			List<ModelTask> children = currentTask.getChildren();

			if (currentMove >= children.size()) {
				return null;
			}

			currentTask = children.get(currentMove);
		}

		return currentTask;
	}

	/**
	 * This method sets the positions of all tasks below <code>t</code> in the
	 * tree.
	 * 
	 * @param t
	 *            the task whose descendants will be computed their positions.
	 */
	private void recursiveComputePositions(ModelTask t) {
		/*
		 * Set the position of all of the children of this task and recursively
		 * compute the position of the rest of the tasks.
		 */
		for (int i = 0; i < t.children.size(); i++) {
			ModelTask currentChild = t.children.get(i);
			Position currentChildPos = new Position(t.getPosition());
			currentChildPos.addMove(i);
			currentChild.position = currentChildPos;
			recursiveComputePositions(currentChild);
		}
	}
}
