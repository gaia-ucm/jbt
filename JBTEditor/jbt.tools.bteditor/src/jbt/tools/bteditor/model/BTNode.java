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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbt.tools.bteditor.model.ConceptualBTNode.NodeInternalType;
import jbt.tools.bteditor.model.ConceptualBTNode.ParameterType;

/**
 * The node of a behaviour tree that can be edited by the {@link BTEditor_}.
 * These are the nodes that a {@link BT} is composed of.
 * <p>
 * A BTNode stores a list with the parameters of the node and their value. It
 * also stores the list of children of the node (which are other BTNode
 * objects). A BTNode also has a parent (or null in case it is the root of the
 * tree). A BTNode has an identifier ({@link Identifier}) that must be unique
 * among the nodes of the tree.
 * <p>
 * A BTNode also stores a reference to a {@link ConceptualBTNode}. The
 * ConceptualBTNode is used for getting conceptual information about the node.
 * It could be said that, on the one hand, the ConceptualBTNode stores the
 * conceptual structure of the node, while on the other, the BTNode stores a
 * particular instance of a ConceptualBTNode (that is, the children the node
 * has, as well as values for each parameter).
 * <p>
 * For a particular BT, BTNode objects should be created through
 * {@link BT#createNode(ConceptualBTNode)}.
 * <p>
 * This class extends {@link Observable} so that {@link Observer}s can be
 * notified when changes take place. Basically, all the methods that somehow
 * change the BTNode will notify all Observers by calling
 * {@link #notifyObservers()}. Even this may not seem very efficient, we do not
 * have strong constraints on the performance of this application.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTNode extends Observable implements Serializable {
	private static final long serialVersionUID = 1L;
	/** Name of the node. This is used only for the root of the tree. */
	private String name;
	/** List of the parameters of the node. */
	private List<Parameter> parameters;
	/** List of the children of the node. */
	private List<BTNode> children;
	/** Guard of the node. */
	private BTNode guard;
	/** Parent of the node. May be null if it has no parent. */
	private BTNode parent;
	/** The underlying conceptual node. */
	private ConceptualBTNode conceptualNode;
	/**
	 * The current error message of the node. This is set when the node is
	 * checked, and contains a description of any error found in the node.
	 */
	private String errorMessage;
	/** Identifier of the node. */
	private Identifier ID;
	/** BT that the BTNode belongs to. */
	private BT tree;
	/** Pattern for verifying {@link ParameterType#LIST_OF_VARIABLES} values. */
	private static final Pattern pattern = Pattern.compile("(( )*\"[a-zA-Z_0-9\\s]+\"( )*)+");

	/**
	 * Identifier of a node. The identifier of a node is an integer, and its
	 * representation as a String is "Node_X", where "X" is the integer. Note
	 * that the integer must not be a negative value.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class Identifier implements Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * The identifier is just an integer value. The string version of the
		 * identifier is "Node_X", where "X" is #value.
		 */
		private long value;

		/**
		 * Constructs an Identifier from a long value.
		 */
		public Identifier(long value) {
			if (value < 0) {
				throw new RuntimeException("Negative value are not allowed.");
			}
			this.value = value;
		}

		/**
		 * Constructs an Identifier from a String. The String must be of the
		 * form "Node_X", where "X" is a non-negative number. <code>id</code> is
		 * initially trimmed.
		 * 
		 * @param id
		 */
		public Identifier(String id) {
			try {
				String newID = id.trim();
				String[] pieces = newID.split("_");

				if (pieces.length != 2) {
					throw new RuntimeException();
				}

				if (!pieces[0].equals("Node")) {
					throw new RuntimeException();
				}

				this.value = Long.parseLong(pieces[1]);

				if (this.value < 0) {
					throw new RuntimeException("Negative value are not allowed.");
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Invalid String representation for an Identifier: " + id, e);
			}
		}

		public String toString() {
			return "Node_" + value;
		}

		public long getValue() {
			return this.value;
		}

		public void setValue(long value) {
			if (value < 0) {
				throw new RuntimeException("Negative value are not allowed.");
			}
			this.value = value;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}

			if (!(o instanceof Identifier)) {
				return false;
			}

			return this.value == ((Identifier) o).value;
		}

		/**
		 * Makes a deep copy of this Identifier.
		 * 
		 * @see java.lang.Object#clone()
		 */
		public Identifier clone() {
			return new Identifier(this.value);
		}
	}

	/**
	 * A parameter of a BTNode. A BTNode parameter is composed of a name and a
	 * value. Both name and value are strings.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class Parameter implements Serializable {
		private static final long serialVersionUID = 1L;
		/** Name of the parameter. */
		private String value;
		/** Value of the parameter. */
		private String name;
		/**
		 * Flag that indicates whether the parameter must be read from the
		 * context or not. If the parameter must be read from the context, its
		 * value must be a non-empty string.
		 */
		private boolean fromContext;

		/**
		 * Returns the name of the parameter, or null if not set.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets the name of the parameter.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Returns the value of the parameter, or null if not set.
		 */
		public String getValue() {
			return value;
		}

		/**
		 * Sets the value of the parameter.
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * Sets if the parameter must be read from the context or not.
		 */
		public void setFromContext(boolean fromContext) {
			this.fromContext = fromContext;
		}

		/**
		 * Returns true if the value must be read from the context, and false
		 * otherwise.
		 */
		public boolean getFromContext() {
			return this.fromContext;
		}

		/**
		 * Makes a deep copy of this Parameter.
		 * 
		 * @see java.lang.Object#clone()
		 */
		public Parameter clone() {
			Parameter copy = new Parameter();
			copy.fromContext = this.fromContext;

			if (this.value != null)
				copy.value = new String(this.value);
			else
				copy.value = null;

			if (this.name != null)
				copy.name = new String(this.name);
			else
				copy.name = null;

			return copy;
		}
	}
	
	public static class VarParameter extends Parameter
	{
		private static final long serialVersionUID = 1L;
		/** Name of the field that describes the parameter. */
		private String variableName;
		private boolean isConstant;
		
		public VarParameter(Parameter par)
		{
			super.value = par.value;
			super.name = par.name;
			super.fromContext= par.fromContext;
		}
		
		/**
		 * Gets if the parameter is a key-value pair or not
		 */
		public boolean getIsConstant() {
			return isConstant;
		}

		/**
		 * Sets if the parameter is a key-value pair or not
		 */
		public void setIsConstant(boolean isConstant) {
			this.isConstant = isConstant;
		}
		
		/**
		 * Returns the name of the field stored in the parameter
		 */
		public String getVariableName() {
			return variableName;
		}
		
		public void setVariableName(String value) {
			this.variableName = value;
		}
		
		/**
		 * Makes a deep copy of this Parameter.
		 * 
		 * @see java.lang.Object#clone()
		 */
		public VarParameter clone()
		{
			VarParameter copy = new VarParameter(super.clone());
			
			if (this.variableName != null)
				copy.variableName = this.variableName;
			else
				copy.variableName = null;
			
			copy.isConstant = this.isConstant;
			
			return copy;
		}
	}

	/**
	 * Default constructor. Constructs a BTNode with no identifier.
	 */
	public BTNode() {
		this.parameters = new Vector<Parameter>();
		this.children = new Vector<BTNode>();
	}

	/**
	 * Constructs a BTNode with an identifier.
	 */
	public BTNode(Identifier id) {
		this.parameters = new Vector<Parameter>();
		this.children = new Vector<BTNode>();
		this.ID = id;
	}

	/**
	 * Returns the BT that this BTNode belongs to, or null if not set yet.
	 * 
	 * @return the BT that this BTNode belongs to, or null if not set yet.
	 */
	public BT getBT() {
		return this.tree;
	}

	/**
	 * Sets the BT that this BTNode belongs to.
	 * 
	 * @param tree
	 *            the BT that this BTNode belongs to.
	 */
	public void setBT(BT tree) {
		this.tree = tree;
	}

	/**
	 * Returns the underlying conceptual node, or null if not set yet.
	 */
	public ConceptualBTNode getConceptualNode() {
		return conceptualNode;
	}

	/**
	 * Sets the underlying conceptual node.
	 */
	public void setConceptualNode(ConceptualBTNode conceptualNode) {
		this.conceptualNode = conceptualNode;
		setChanged();
		notifyObservers();
	}

	/**
	 * Gets the parent of the node. Returns null if not set. If the node has no
	 * parent, it also returns null.
	 */
	public BTNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent of the node. May be null for the root of the tree.
	 */
	public void setParent(BTNode parent) {
		this.parent = parent;
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the identifier of the node, or null if not set.
	 */
	public Identifier getID() {
		return this.ID;
	}

	/**
	 * Returns the name of the node, or null if not set.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the node.
	 */
	public void setName(String name) {
		this.name = name;
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the parameters of the node, or an empty list if it has none.
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}

	/**
	 * Sets the list of parameters of the node. Can be an empty list if no
	 * parameter is wanted.
	 */
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the number of children of the node.
	 */
	public int getNumChildren() {
		return this.children.size();
	}

	/**
	 * Returns a list with all the children of the node, or an empty list if it
	 * has none.
	 */
	public List<BTNode> getChildren() {
		return this.children;
	}

	/**
	 * Returns the error message, or null if no error message.
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Adds a child to the list of children of this node.
	 */
	public void addChild(BTNode child) {
		this.children.add(child);
		setChanged();
		notifyObservers();
	}

	/**
	 * Removes a child from the list of children of the node.
	 */
	public void removeChild(BTNode child) {
		this.children.remove(child);
		setChanged();
		notifyObservers();
	}

	/**
	 * Adds a parameter to the list of parameters of this node.
	 */
	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
		setChanged();
		notifyObservers();
	}

	/**
	 * Makes <code>other</code> be a sibling of this node. This method inserts
	 * <code>other</code> into the list of children of this's parent, and
	 * removes it from the list of children of its current parent (if it has
	 * any). This node must have a parent. Otherwise, this method does nothing.
	 */
	public void addAsSibling(BTNode other) {
		if (this.parent != null) {
			if (other.hasAsDescendant(this)) {
				return;
			}

			if (other.parent != null) {
				other.parent.getChildren().remove(other);
			}

			int thisPos = this.parent.getChildren().indexOf(this);
			this.parent.getChildren().add(thisPos + 1, other);
			other.parent = this.parent;
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Sets the identifier of this node.
	 */
	public void setID(Identifier id) {
		this.ID = id;
		setChanged();
		notifyObservers();
	}

	/**
	 * Sets the guard of the node.
	 * 
	 * @param guard
	 */
	public void setGuard(BTNode guard) {
		this.guard = guard;
	}

	/**
	 * Returns the guard of the node, or null if it has no guard.
	 */
	public BTNode getGuard() {
		return this.guard;
	}

	/**
	 * Returns true if <code>other</code> is a descendant of this node, and
	 * false otherwise.
	 */
	public boolean hasAsDescendant(BTNode other) {
		if (other == this) {
			return true;
		}

		for (BTNode n : this.getChildren()) {
			if (n.hasAsDescendant(other)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Clears the list of parameters of this node.
	 */
	public void clearParameters() {
		this.parameters.clear();
		setChanged();
		notifyObservers();
	}

	/**
	 * Clears the errors of this node.
	 */
	public void clearErrors() {
		this.errorMessage = null;
		setChanged();
		notifyObservers();
	}

	/**
	 * Checks if this node has no error in its structure. The structure of the
	 * node is checked against the underlying ConceptualNode. Returns true if no
	 * error is detected, and false otherwise. In case there is any error, a
	 * description of it will be accessible through {@link #getErrorMessage()}.
	 * 
	 */
	public boolean check() {
		boolean result = true;
		boolean hadErrors = this.errorMessage != null;

		/* This is for the root node. */
		if (this.conceptualNode.getHasName()
				&& this.conceptualNode.getType().equals(NodeInternalType.ROOT.toString())) {
			if (this.name == null || this.name.equals("")) {
				this.errorMessage = "Name cannot be empty";
				result = false;
			}
		}

		if (this.conceptualNode.getNumChildren() != 0) {
			if (this.conceptualNode.getNumChildren() == -1) {
				if (this.children.size() == 0) {
					this.errorMessage = "Must have at least one child";
					result = false;
				}
			} else {
				if (this.children.size() != this.conceptualNode.getNumChildren()) {
					this.errorMessage = "Must have exactly " + this.conceptualNode.getNumChildren()
							+ " children";
					result = false;
				}
			}
		} else {
			if (this.children.size() != 0) {
				this.errorMessage = "This type of node cannot have children";
				result = false;
			}
		}

		if (conceptualNode.getParameters().size() != 0) {
			if (this.parameters.size() != conceptualNode.getParameters().size()) {
				this.errorMessage = "Not all the parameters have a value";
				result = false;
			} else {
				for (int i = 0; i < conceptualNode.getParameters().size(); i++) {
					jbt.tools.bteditor.model.ConceptualBTNode.Parameter p = conceptualNode
							.getParameters().get(i);
					String actualValue;
					try {
						actualValue = this.parameters.get(i).getValue();
						if (this.parameters.get(i).getFromContext()) {
							if (actualValue.equals("")) {
								this.errorMessage = "Invalid value for parameter " + p.getName();
								result = false;
							}
						} else {
							if (ParameterType.isVariable(p.getType()))
							{
								BTNode.VarParameter varParameter = (BTNode.VarParameter)this.parameters.get(i);
								String defaultTextValue = varParameter.getVariableName();
								if (!checkVarParameter(p, actualValue, defaultTextValue, varParameter.getIsConstant(), this.tree)){
									this.errorMessage = "Invalid value for parameter " + p.getName();
									result = false;
								}
							}
							if (!checkParameter(p, actualValue, this.tree)) {
								this.errorMessage = "Invalid value for parameter " + p.getName();
								result = false;
							}
						}
					} catch (Exception e) {
						this.errorMessage = e.getMessage();
						result = false;
					}
				}
			}
		}

		/* Do not forget to check the guard. */
		if (this.guard != null) {
			if (!this.guard.check()) {
				this.errorMessage = "Error in the guard: " + this.guard.errorMessage;
				result = false;
			}
		}

		if (result) {
			if (hadErrors) {
				this.errorMessage = null;
				setChanged();
				notifyObservers();
			}
		} else {
			setChanged();
			notifyObservers();
		}

		return result;
	}

	/**
	 * Checks if the value of a parameter is consistent with the type of the
	 * parameter. Returns true if no error is detected, and false otherwise. The
	 * BT that the parameter is supposed to belong must be provided, because the
	 * correctness of some values can only be checked within a BT (for instance,
	 * the {@link ParameterType#NODE_ID}).
	 */
	public static boolean checkParameter(
			jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameterDefinition, String value,
			BT tree) {
		ParameterType type = parameterDefinition.getType();

		if (type == ParameterType.INTEGER) {
			try {
				Integer.parseInt(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else if (type == ParameterType.REAL) {
			try {
				Double.parseDouble(value);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else if (type == ParameterType.STRING) {
			if (value.length() == 0) {
				return false;
			} else {
				return true;
			}
		} else if (type == ParameterType.STATUS_CODE) {
			String[] values = ParameterType.STATUS_CODE.getPossibleValues();

			for (String currentValue : values) {
				if (value.equals(currentValue)) {
					return true;
				}
			}

			return false;
		} else if (type == ParameterType.NODE_ID) {
			if (value.length() == 0) {
				return false;
			} else {
				try {
					Identifier id = new Identifier(value);

					/*
					 * Check that node exists and that its type is compatible with
					 * the types that the parameter supports.
					 */
					BTNode referencedNode = tree.findNode(id);

					if (referencedNode != null) {
						if (parameterDefinition.getNodeClasses().contains(
								referencedNode.getConceptualNode().getType())
								|| parameterDefinition.getNodeClasses().size() == 0) {
							return true;
						}
					}

					return false;
				} catch (Exception e) {
					return false;
				}
			}
		} else if (type == ParameterType.BOOLEAN) {
			String[] values = ParameterType.BOOLEAN.getPossibleValues();

			for (String currentValue : values) {
				if (value.equals(currentValue)) {
					return true;
				}
			}

			return false;
		} else if (type == ParameterType.COORDINATE) {
			String[] numbers = value.split("( )+");

			for (String number : numbers) {
				try {
					Double.parseDouble(number);
				} catch (Exception e) {
					return false;
				}
			}

			return true;
		} else if (type == ParameterType.DIRECTION) {
			try {
				Double.parseDouble(value);
			} catch (Exception e) {
				return false;
			}

			return true;
		} else if (type == ParameterType.PARALLEL_POLICY) {
			String[] values = ParameterType.PARALLEL_POLICY.getPossibleValues();

			for (String currentValue : values) {
				if (value.equals(currentValue)) {
					return true;
				}
			}

			return false;
		} else if (type == ParameterType.LIST_OF_VARIABLES) {
			Matcher matcher = pattern.matcher(value);

			return matcher.matches();
		} else if (ParameterType.isVariable(type)) {
			
			if (value.length() == 0) {
				return false;
			} else {
				return true;
			}
		}

		throw new IllegalArgumentException("Unexpected parameter type " + type.toString());
	}
	
	/**
	 * Checks if the value of a var parameter is consistent with the type of the
	 * parameter. This is done this way to ensure that both the fields of the key-value pair
	 * are well formed
	 * the {@link ParameterType#NODE_ID}).
	 */
	public static boolean checkVarParameter(
			jbt.tools.bteditor.model.ConceptualBTNode.Parameter parameterDefinition, String value, String variableName, boolean isConstant,
			BT tree) {
		
		ParameterType type = parameterDefinition.getType();

		if (ParameterType.isVariable(type)) {
			
			if (!isConstant)
			{
				if (variableName.length() == 0)
					return false;
			}
			
				switch (parameterDefinition.getType())
				{
					case VARIABLE_FLOAT:
					{
						try {
							
							Float.parseFloat(value);
							if (value.length() == 0)
								return false;

						} catch (Exception e) {
							
							return false;
						}
						break;
					}
	
					case VARIABLE_INT:
					{
						
						try {
							
							Integer.parseInt(value);
							if (value.length() == 0)
								return false;
							
						} catch (Exception e) {
							
							return false;
						}
						break;
					}
						
					case VARIABLE_STRING:
					{
						if (value.length() == 0)
							return false;
						
						break;
					}
				}
				return true;
			}
		throw new IllegalArgumentException("Unexpected parameter type " + type.toString());
		}
	

	/**
	 * Makes a deep copy of this BTNode. Children are recursively cloned, as
	 * well as the guard. The underlying ConceptualBTNode and the BT that this
	 * BTNode belongs to are not deeply copied, but just assigned. The parent of
	 * the returned node is null, but its children are properly linked.
	 * <p>
	 * This method should be carefully used. Keep in mind that node identifiers
	 * are unique in a BT, so the returned copy is only valid as long as the BT
	 * it belongs to does not have any node with the same name. Otherwise, the
	 * identifiers should be changed.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public BTNode clone() {
		BTNode copy = new BTNode();

		if (this.conceptualNode != null)
			copy.conceptualNode = this.conceptualNode;
		else
			copy.conceptualNode = null;

		if (this.errorMessage != null)
			copy.errorMessage = this.errorMessage;
		else
			copy.errorMessage = null;

		if (this.guard != null)
			copy.guard = this.guard.clone();
		else
			copy.guard = null;

		if (this.ID != null)
			copy.ID = this.ID.clone();
		else
			copy.ID = null;

		if (this.name != null)
			copy.name = new String(this.name);
		else
			copy.name = null;

		copy.parameters = new LinkedList<Parameter>();

		if (this.parameters.size() != 0) {
			for (Parameter p : this.parameters) {
				copy.parameters.add(p.clone());
			}
		}

		copy.children = new LinkedList<BTNode>();

		if (this.children.size() != 0) {
			for (BTNode child : this.children) {
				BTNode clonedChild = child.clone();
				clonedChild.setParent(copy);
				copy.children.add(clonedChild);
			}
		}

		copy.tree = this.tree;

		return copy;
	}

	public String toString() {
		return "ID: " + this.ID + " - Type: " + this.conceptualNode.getReadableType()
				+ " - Num. Children: " + this.children.size();
	}
}
