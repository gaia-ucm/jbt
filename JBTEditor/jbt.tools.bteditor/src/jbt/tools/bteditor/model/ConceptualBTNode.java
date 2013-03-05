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

import gatech.mmpm.ActionParameterType;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jbt.tools.bteditor.NodesLoader;

/**
 * ConceptualBTNode represents a behaviour tree node conceptually, containing
 * information about its type of node, how many children it can contain, what
 * parameters it has, and so on.
 * <p>
 * A ConceptualBTNode is used for building actual nodes of trees, as well as
 * checking the validity of actual nodes.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ConceptualBTNode implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Some types of nodes that are internal to the application.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static enum NodeInternalType {
		ROOT {
			public String toString() {
				return "Root";
			}
		},
		ACTION {
			public String toString() {
				return "Action";
			}
		},
		CONDITION {
			public String toString() {
				return "Condition";
			}
		},
		SUBTREE_LOOKUP {
			public String toString() {
				return "SubtreeLookup";
			}
		}
	}

	/** Type that is displayed in the application. */
	private String readableType;
	/**
	 * Actual type of the node. This is the type that is written into the XML
	 * file when exporting a BT.
	 */
	private String type;
	/**
	 * Conceptual description of the parameters of this node. May be empty
	 * (empty list) for nodes with no parameters.
	 */
	private List<Parameter> parameters;
	/**
	 * Number of children of the node. Ranges from -1 to infinity. A value of -1
	 * means that the node can have from 1 to infinity children. A value
	 * different from -1 means that the node must have exactly that number of
	 * children.
	 */
	private int numChildren;
	/**
	 * Route of the icon that is displayed for this kind of node.
	 */
	private String icon;
	/**
	 * Indicates if the node has a name.
	 */
	private boolean hasName;
	/**
	 * Static name of the node. If {@link #hasName} is false, the no has no
	 * name, an {@link #name} is null. Otherwise, this variable stores the
	 * static name of the node (or null in case it has not been set yet).
	 * <p>
	 * Actions and conditions have a static name.
	 */
	private String name;

	/**
	 * Enum that contains all the types of the parameters that are accepted by
	 * behaviour tree nodes.
	 * <p>
	 * It must be noted that actions and conditions that are loaded from
	 * external MMPM domain files have some other parameter types that are
	 * converted into ParameterType before using them. For instance, the MMPM
	 * {@link ActionParameterType#ENTITY_ID} is converted into a
	 * {@link ParameterType#STRING} since behaviour trees do not care about
	 * entities IDs. For more information about this conversion, see
	 * {@link NodesLoader#fromMMPMParameterType(ActionParameterType type)}.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static enum ParameterType implements Serializable {
		BOOLEAN {
			public String[] getPossibleValues() {
				return new String[] { "true", "false" };
			}

			public String getReadableType() {
				return "Boolean";
			}

		},
		INTEGER {
			public String getReadableType() {
				return "Integer";
			}
		},
		REAL {
			public String getReadableType() {
				return "Real";
			}
		},
		STRING {
			public String getReadableType() {
				return "String";
			}
		},
		STATUS_CODE {
			public String[] getPossibleValues() {
				return new String[] { "success", "failure" };
			}

			public String getReadableType() {
				return "Status code";
			}

		},
		NODE_ID {
			public String getReadableType() {
				return "Node id";
			}
		},
		PARALLEL_POLICY {
			public String[] getPossibleValues() {
				return new String[] { "sequence", "selector" };
			}

			public String getReadableType() {
				return "Parallel policy";
			}

		},
		COORDINATE {
			public String getReadableType() {
				return "Coordinate";
			}
		},
		DIRECTION {
			public String getReadableType() {
				return "Direction";
			}
		},
		/**
		 * A list of variables, each one surrounded by quotation marks, and
		 * separated by blank spaces. For instance:
		 * <p>
		 * "var1" "var2" "var3"
		 */
		LIST_OF_VARIABLES {
			public String getReadableType() {
				return "List of variables";
			}
		},
		OBJECT {
			public String getReadableType() {
				return "Object";
			}
		},
		
		VARIABLE_INT {
			public String getReadableType() {
				return "Int";
			}
		},
		
		VARIABLE_FLOAT {
			public String getReadableType() {
				return "Real";
			}
		},
	
		VARIABLE_STRING {
			public String getReadableType() {
				return "String";
			}
		};

		/**
		 * Returns the set of possible values for an particular ParameterType.
		 * In case the ParameterType has an infinite number of possible values,
		 * it returns null.
		 * <p>
		 * This method is used, for instance, for {@link #PARALLEL_POLICY},
		 * which has only two possible values, "SEQUENCE" and "PARALLEL".
		 * <p>
		 * The default implementation of this method returns null.
		 */
		public String[] getPossibleValues() {
			return null;
		};

		/**
		 * Returns a user friendly version of the type.
		 */
		public abstract String getReadableType();
		
		/**
		 * Checks if a type is a variable
		 */
		public static boolean isVariable(ParameterType type)
		{
			if (type == VARIABLE_INT || type == VARIABLE_FLOAT || type == VARIABLE_STRING)
				return true;
			
			return false;
		}
	}

	/**
	 * The specification of a ConceptualBTNode parameter. A Parameter is
	 * basically a name and a type, meaning that a parameter is identified by a
	 * name and it has a value of a particular type.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class Parameter implements Serializable {
		private static final long serialVersionUID = 1L;
		/** Name of the parameter. */
		private String name;
		/** Type of the parameter. */
		private ParameterType type;
		/**
		 * Tells if the parameter can or cannot be read from the context. This
		 * is mainly used by the BTEditor: some parameters can be read from the
		 * context , but others cannot, and must have a specific value.
		 */
		private boolean contextable = false;
		/**
		 * This is used for checking certain special parameters. When a
		 * parameter is a reference to a node, that node may have to be of a
		 * particular kind, in which case this field stores that kind.
		 */
		private List<String> nodeClasses = new LinkedList<String>();

		/**
		 * When a parameter is a reference to a node (
		 * {@link ParameterType#NODE_ID}), that node may have to be of a
		 * particular kind, in which case this method returns the list of
		 * allowed types. If an empty list is returned, it means all kinds
		 * are allowed.
		 */
		public List<String> getNodeClasses() {
			return nodeClasses;
		}

		/**
		 * When a parameter is a reference to a node (
		 * {@link ParameterType#NODE_ID}), that node may have to be of a
		 * particular kind, in which case this method adds one allowed
		 * type.
		 */
		public void addNodeClass(String nodeClass) {
			this.nodeClasses.add(nodeClass);
		}

		/**
		 * Returns the name of the node parameter.
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
		 * Returns the type of the parameter.
		 */
		public ParameterType getType() {
			return type;
		}

		/**
		 * Sets the type of the parameter.
		 */
		public void setType(ParameterType type) {
			this.type = type;
		}

		/**
		 * Sets of the parameter can or cannot be read from the context.
		 */
		public void setContextable(boolean contextable) {
			this.contextable = contextable;
		}

		/**
		 * Returns true if the parameter can be read from the context, of false
		 * if it cannot.
		 */
		public boolean getContextable() {
			return this.contextable;
		}

		/**
		 * This function returns a deep copy of this Parameter.
		 * 
		 * @see java.lang.Object#clone()
		 */
		public Parameter clone() {
			Parameter copy = new Parameter();
			copy.contextable = this.contextable;
			copy.name = this.name == null ? null : new String(this.name);
			copy.type = this.type;
			copy.nodeClasses = new LinkedList<String>();

			for (String nodeClass : this.nodeClasses) {
				copy.nodeClasses.add(new String(nodeClass));
			}

			return copy;
		}
	}
	
	/**
	 * Constructs an empty ConceptualBTNode.
	 */
	public ConceptualBTNode() {
		this.parameters = new Vector<Parameter>();
	}

	/**
	 * Returns the type of the node, or null if not set. This is the type that
	 * is written into the XML file when exporting a behaviour tree.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of the conceptual node.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the list of parameters of the conceptual node, or an empty list
	 * if it has no parameters.
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}

	/**
	 * Seths the list of parameters of the conceptual node.
	 */
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the number of children that this node can have. A value of -1
	 * means that the node can have an undetermined amount of children (but at
	 * least one). A value other than -1 means that the node must have exactly
	 * that number of children.
	 */
	public int getNumChildren() {
		return numChildren;
	}

	/**
	 * Sets the number of children that this node can have. A value of -1 means
	 * that the node can have an undetermined amount of children (but at least
	 * one). A value other than -1 means that the node must have exactly that
	 * number of children.
	 */
	public void setNumChildren(int numChildren) {
		this.numChildren = numChildren;
	}

	/**
	 * Returns the path of the icon associated to this conceptual node, or null
	 * if not set.
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * Sets the path of the icon associated to this conceptual node.
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * Adds a parameter to the list of parameters of this node.
	 */
	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
	}

	/**
	 * Returns a readable version of the type of the conceptual node, or null if
	 * not set.
	 */
	public String getReadableType() {
		return readableType;
	}

	/**
	 * Sets the readable version of the type of the node.
	 */
	public void setReadableType(String type) {
		this.readableType = type;
	}

	/**
	 * Returns if this node has a name. If false, {@link #getName()} is null.
	 * Otherwise, {@link #getName()} returns the name (or null in case it has
	 * not been set yet).
	 */
	public boolean getHasName() {
		return hasName;
	}

	/**
	 * Sets if the node has a name. If false, {@link #getName()} is null.
	 * Otherwise, {@link #getName()} returns the name (or null in case it has
	 * not been set yet).
	 */
	public void setHasName(boolean hasName) {
		this.hasName = hasName;
	}

	/**
	 * If {@link #getHasName()} is true, returns the name of the node or null in
	 * case it has not been set. If false, it returns null.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the node.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This method returns a deep copy of this ConceptualBTNode.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public ConceptualBTNode clone() {
		ConceptualBTNode copy = new ConceptualBTNode();

		copy.hasName = this.hasName;
		copy.icon = this.icon == null ? null : new String(this.icon);
		copy.name = this.name == null ? null : new String(this.name);
		copy.numChildren = this.numChildren;
		copy.parameters = new LinkedList<ConceptualBTNode.Parameter>();

		for (Parameter p : this.parameters) {
			copy.parameters.add(p.clone());
		}

		copy.readableType = this.readableType == null ? null : new String(this.readableType);
		copy.type = this.type == null ? null : new String(this.type);

		return copy;
	}
}
