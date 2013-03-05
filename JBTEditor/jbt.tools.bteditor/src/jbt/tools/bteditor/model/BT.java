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
package jbt.tools.bteditor.model;

import java.util.List;
import java.util.Vector;

import jbt.tools.bteditor.event.ITreeModifierListener;
import jbt.tools.bteditor.event.TreeModifiedEvent;
import jbt.tools.bteditor.model.BTNode.Identifier;
import jbt.tools.bteditor.model.ConceptualBTNode.Parameter;
import jbt.tools.bteditor.model.ConceptualBTNode.ParameterType;

/**
 * A BT represents a behaviour tree that can be edited, loaded and exported. This type of trees is
 * edited in the {@link BTEditor_}. A BT only stores the root of the tree, which is a {@link BTNode}
 * .
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BT {
	/** The root of the tree. */
	private BTNode root;
	/**
	 * Counter of how many nodes have been created through the {@link #createNode(ConceptualBTNode)}
	 * . This counter is used when generating ids for nodes of this tree.
	 */
	private long nodeCounter = 0;
	/** Listeners that are listening to changes in the tree. */
	private List<ITreeModifierListener> listeners = new Vector<ITreeModifierListener>();

	/**
	 * Default constructor. Constructs a BT with no root.
	 */
	public BT() {
	}

	/**
	 * Constructs a BT with a root.
	 */
	public BT(BTNode root) {
		this.root = root;
		updateNodeCounter();
	}

	/**
	 * Sets the root of the tree.
	 */
	public void setRoot(BTNode root) {
		this.root = root;
		updateNodeCounter();
	}

	/**
	 * Returns the root of the tree or null in case it has not been set.
	 */
	public BTNode getRoot() {
		return this.root;
	}

	/**
	 * Finds a node in the tree, by using an identifier. Returns null if not found.
	 */
	public BTNode findNode(Identifier id) {
		return internalFindNode(this.root, id);
	}

	/**
	 * Creates a BTNode from a ConceptualBTNode for this tree. This method creates a BTNode and sets
	 * its ID to an unique identifier for the tree. Also it sets the underlying conceptual node of
	 * the BTNode to <code>conceptualNode</code>.
	 */
	public BTNode createNode(ConceptualBTNode conceptualNode) {
		BTNode node = new BTNode(new Identifier(nodeCounter));
		nodeCounter++;
		node.setConceptualNode(conceptualNode);
		node.setBT(this);
		return node;
	}

	/**
	 * Adds a listener that will be notified when changes in the tree occur.
	 */
	public void addTreeModifiedListener(ITreeModifierListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * This method must be called when there is a change in the tree. By doing so, all entities that
	 * are listening to changes in the tree will be notified.
	 */
	public void fireTreeChanged(Object source) {
		for (ITreeModifierListener l : this.listeners) {
			l.treeModified(new TreeModifiedEvent(source, this));
		}
	}

	/**
	 * Checks the validity of all the nodes of the tree. Returns a list with the nodes that contain
	 * errors.
	 */
	public List<BTNode> checkTree() {
		List<BTNode> nodes = new Vector<BTNode>();
		internalCheckTree(this.root, nodes);
		return nodes;
	}

	/**
	 * Clear the errors of all of the nodes of the tree.
	 */
	public void clearErrors() {
		internalClearErrors(this.root);
	}

	/**
	 * Given a BTNode, this method recompute its ID as well as the IDs of all its children and
	 * guards. After calling this method, these IDs will not clash any of those of this BT's nodes.
	 * This method can be used, for example, when a whole branch must be added to this tree. In such
	 * case, this method can be used to recompute the IDs of the nodes of the branch so that there
	 * is no conflict among IDs.
	 * <p>
	 * It is important to notice that after recomputing the IDs, they are still consistent among
	 * each other. For instance, if before recomputing the ID a node <i>A</i> had ID "Node_34", and
	 * it referenced by another node <i>B</i>, after recomputing IDs the reference will still be
	 * valid (it will not be "Node_34", but whatever new ID <i>A</i> has will be properly referenced
	 * by <i>B</i>).
	 * 
	 * @param node
	 *            the root of the branch (tree) whose nodes will get their IDs recomputed.
	 */
	public void recomputeIDs(BTNode node) {
		Identifier largestID = computeLargestID(this.root);
		Identifier lowestID = computeLowestID(node);
		reassignIDs(node, largestID.getValue() - lowestID.getValue() + 1);
	}

	/**
	 * Given a BTNode, this method sets, as the current underlying BT of <code>node</code> and all
	 * of its descendants (including guards), this BT.
	 * 
	 * @param node
	 *            the root of the branch (tree) whose underlying BT will be modified.
	 */
	public void reassignUnderlyingBT(BTNode node) {
		node.setBT(this);

		for (BTNode child : node.getChildren()) {
			reassignUnderlyingBT(child);
		}

		if (node.getGuard() != null) {
			reassignUnderlyingBT(node.getGuard());
		}
	}

	/**
	 * Reassigns the all the IDs in the branch whose root is <code>node</code>. This method just
	 * adds <code>offset</code> to the value of all the nodes (including guards) of the branch.
	 */
	private void reassignIDs(BTNode node, long offset) {
		node.getID().setValue(node.getID().getValue() + offset);

		/* If some parameter has type "node_id", then it must be changed too. */
		if (node.getParameters().size() != 0) {
			for (int i = 0; i < node.getConceptualNode().getParameters().size(); i++) {
				Parameter cParameter = node.getConceptualNode().getParameters().get(i);
				if (cParameter.getType() == ParameterType.NODE_ID) {
					Identifier newValue = new Identifier(node.getParameters().get(i).getValue());
					newValue.setValue(newValue.getValue() + offset);
					node.getParameters().get(i).setValue(newValue.toString());
				}
			}
		}

		for (BTNode child : node.getChildren()) {
			reassignIDs(child, offset);
		}

		if (node.getGuard() != null) {
			reassignIDs(node.getGuard(), offset);
		}
	}

	/**
	 * Computes the largest ID of a whole branch of a BT (including guards). The root of the branch
	 * is <code>node</code>.
	 */
	private Identifier computeLargestID(BTNode node) {
		Identifier largestKnownID = node.getID().clone();
		for (BTNode child : node.getChildren()) {
			computeLargestID(child, largestKnownID);
		}

		if (node.getGuard() != null) {
			Identifier guardLargestIdentifier = computeLargestID(node.getGuard());
			if (guardLargestIdentifier.getValue() > largestKnownID.getValue()) {
				largestKnownID.setValue(guardLargestIdentifier.getValue());
			}
		}

		return largestKnownID;
	}

	/**
	 * Stores into <code>largestKnownID</code> (which initially must be a valid Identifier for the
	 * BT) the largest Identifier of the nodes in <code>node</code> (including guards).
	 */
	private void computeLargestID(BTNode node, Identifier largestKnownID) {
		if (node.getID().getValue() > largestKnownID.getValue()) {
			largestKnownID.setValue(node.getID().getValue());
		}

		for (BTNode currentChild : node.getChildren()) {
			computeLargestID(currentChild, largestKnownID);
		}

		if (node.getGuard() != null) {
			Identifier guardLargestIdentifier = computeLargestID(node.getGuard());
			if (guardLargestIdentifier.getValue() > largestKnownID.getValue()) {
				largestKnownID.setValue(guardLargestIdentifier.getValue());
			}
		}
	}

	/**
	 * Computes the lowest ID of a whole branch of a BT (including guards). The root of the branch
	 * is <code>node</code>.
	 */
	private Identifier computeLowestID(BTNode node) {
		Identifier lowestKnownID = node.getID().clone();
		for (BTNode child : node.getChildren()) {
			computeLowestID(child, lowestKnownID);
		}

		if (node.getGuard() != null) {
			Identifier guardLowestIdentifier = computeLowestID(node.getGuard());
			if (guardLowestIdentifier.getValue() < lowestKnownID.getValue()) {
				lowestKnownID.setValue(guardLowestIdentifier.getValue());
			}
		}

		return lowestKnownID;
	}

	/**
	 * Stores into <code>lowestKnownID</code> (which initially must be a valid Identifier for the
	 * BT) the lowest Identifier of the nodes in <code>node</code> (including guards).
	 */
	private void computeLowestID(BTNode node, Identifier lowestKnownID) {
		if (node.getID().getValue() < lowestKnownID.getValue()) {
			lowestKnownID.setValue(node.getID().getValue());
		}

		for (BTNode currentChild : node.getChildren()) {
			computeLowestID(currentChild, lowestKnownID);
		}

		if (node.getGuard() != null) {
			Identifier guardLowestIdentifier = computeLowestID(node.getGuard());
			if (guardLowestIdentifier.getValue() < lowestKnownID.getValue()) {
				lowestKnownID.setValue(guardLowestIdentifier.getValue());
			}
		}
	}

	/**
	 * Clear the errors of the nodes of the tree whose root is <code>node</code> .
	 */
	private void internalClearErrors(BTNode node) {
		node.clearErrors();
		for (BTNode child : node.getChildren()) {
			internalClearErrors(child);
		}
	}

	/**
	 * Checks the validity of all the nodes of the tree whose root is <code>node</code>, and stores
	 * those that are incorrect into <code>ids</code>.
	 */
	private void internalCheckTree(BTNode node, List<BTNode> nodes) {
		if (!node.check()) {
			nodes.add(node);
		}
		for (BTNode child : node.getChildren()) {
			internalCheckTree(child, nodes);
		}
	}

	/**
	 * Finds a node by identifier, starting the search in <code>currentNode</code>. This is a
	 * recursive method that is called again on the children. Returns null if the node cannot be
	 * found.
	 */
	private BTNode internalFindNode(BTNode currentNode, Identifier id) {
		if (currentNode.getID().equals(id)) {
			return currentNode;
		}

		for (BTNode n : currentNode.getChildren()) {
			BTNode nodeFound = internalFindNode(n, id);
			if (nodeFound != null) {
				return nodeFound;
			}
		}

		return null;
	}

	/**
	 * Updates the internal counter that the BT uses to create nodes via
	 * {@link #createNode(ConceptualBTNode)}, so that the counter is bigger than the biggest number
	 * associated to any of the node's identifiers in the tree. This method should be called when
	 * the tree gets removed or added nodes. Note that it is automatically called when
	 * {@link #setRoot(BTNode)} is called.
	 */
	public void updateNodeCounter() {
		this.nodeCounter = 1;
		recursiveUpdateNodeCounter(this.root);
	}

	/**
	 * Updates the field {@link #nodeCounter} so that the counter is bigger than the biggest number
	 * associated to any of the identifiers of descendants of
	 * <code>currentNode<code> (including itself).
	 */
	private void recursiveUpdateNodeCounter(BTNode currentNode) {
		long order = currentNode.getID().getValue();

		if (order >= this.nodeCounter) {
			this.nodeCounter = order + 1;
		}

		for (BTNode child : currentNode.getChildren()) {
			recursiveUpdateNodeCounter(child);
		}
	}
}
