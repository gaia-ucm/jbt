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
package jbt.tools.bteditor;

import gatech.mmpm.ActionParameterType;
import gatech.mmpm.tools.parseddomain.ParsedAction;
import gatech.mmpm.tools.parseddomain.ParsedActionParameter;
import gatech.mmpm.tools.parseddomain.ParsedDomain;
import gatech.mmpm.tools.parseddomain.ParsedMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import jbt.tools.bteditor.model.ConceptualBTNode;
import jbt.tools.bteditor.model.ConceptualBTNode.NodeInternalType;
import jbt.tools.bteditor.model.ConceptualBTNode.Parameter;
import jbt.tools.bteditor.model.ConceptualBTNode.ParameterType;
import jbt.tools.bteditor.model.ConceptualNodesTree;
import jbt.tools.bteditor.model.ConceptualNodesTree.ConceptualBTNodeItem;
import jbt.tools.bteditor.util.IconsPaths;
import jbt.tools.bteditor.util.Pair;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * NodesLoader is in charge of loading nodes that can be used by the application
 * when building behaviour trees.
 * <p>
 * There are two types of nodes:
 * <ul>
 * <li>Standard nodes: these are internal to the application, that is, the
 * application knows of them in advance. These are nodes such as Parallel,
 * Sequence, or Wait.
 * <li>Non-standard nodes: these nodes are specified by external clients in MMPM
 * domain files. These are low level nodes in a BT, that is, actions and
 * conditions. Actions are obtained from the action set of the MMPM domain file,
 * and conditions are obtained from the sensor set of the MMPM domain file.
 * </ul>
 * 
 * Standard nodes are defined in a XML file located at
 * {@link #STANDARD_NODES_FILE}. That file specifies the XML format of the
 * standard nodes. Standard nodes are loaded into the application at star-up
 * time. In fact, if they are not properly loaded, the application does not
 * start.
 * <p>
 * Non-standard nodes are loaded from MMPM domain files. Only actions and
 * sensors are read from the MMPM file. Actions are transformed into nodes of
 * type {@link NodeInternalType#ACTION}, and sensors are transformed into nodes
 * of type {@link NodeInternalType#CONDITION}. These are the nodes upon which
 * external users build domain dependent behaviour trees.
 * <p>
 * Standard and non-standard nodes become {@link ConceptualBTNode}s when they
 * are loaded so that they can be easily used in the application.
 * <p>
 * NodesLoader stores both standard and non-standard nodes into
 * {@link ConceptualNodesTree}s in order to organize them in categories.
 * <p>
 * {@link #loadStandardNodes()} loads all the standard nodes specified in the
 * file {@link #STANDARD_NODES_FILE} and creates a ConceptualNodesTree for them.
 * Also, the list of standard nodes contains a special node that is not defined
 * in the file, the root node, which is statically created and included in the
 * list.
 * <p>
 * {@link #loadNonStandardNodes(String)} loads all the actions and sensors
 * (conditions) from a MMPM domain file, and creates a ConceptualNodesTree whose
 * root category is the short name of the file. The root category contains two
 * sub categories, one for actions (which includes all the actions loaded from
 * the file), and another one for conditions (which contains all the boolean
 * sensors loaded form the file).
 * <p>
 * The NodesLoader class can also be used for retrieving nodes by type.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 * Modified by Fernando Matarrubia
 * 
 * The main purpose of the modification is to add the functionality
 * of register all the standard nodes paired with their category in a hashtable 
 * When the c++ file is written, that info would be needed in order to 
 * separate the different node types by their category
 */
public class NodesLoader {
	
	/** ConceptualNodesTree for standard nodes. */
	private static ConceptualNodesTree standardNodesTree = new ConceptualNodesTree();
	
	/** Standard nodes. They are indexed by type. */
	private static Hashtable<String, ConceptualBTNode> standardNodes = new Hashtable<String, ConceptualBTNode>();
	
	/** All nodes sorted by their category */
	private static Hashtable<String, String> nodesByCategory = new Hashtable<String, String>();
	/**
	 * ConceptualNodesTrees for non standard nodes. They are indexed by the name
	 * of the file from which they are loaded.
	 */
	private static Hashtable<String, ConceptualNodesTree> nonStandardTrees = new Hashtable<String, ConceptualNodesTree>();
	/**
	 * Non-standard nodes. They are indexed both by type and name (the type is
	 * the first element in the Pair, and the name is the second one).
	 */
	private static Hashtable<Pair<String, String>, ConceptualBTNode> nonStandardNodes = new Hashtable<Pair<String, String>, ConceptualBTNode>();

	/** The file that contains the definition of all standard nodes. */
	public static final String STANDARD_NODES_FILE = "files/standardNodes.xml";
	
	/** Public enum that contains the different node categories */
	public enum NodeCategories
	{
		COMPOSITE,	DECORATOR, LEAF, DUMMY
	};

	/**
	 * Loads all the standard nodes defined in the file
	 * {@link #STANDARD_NODES_FILE}. After doing so, standard nodes can be
	 * accessed through {@link #getStandardNodes()} and
	 * {@link #getNode(String, String)}.
	 * <p>
	 * Throws an exception if there is any error loading the standard nodes.
	 */
	public static void loadStandardNodes() throws IOException {
		loadRoot();

		URL url = FileLocator.find(Activator.getDefault().getBundle(),
				new Path(STANDARD_NODES_FILE), Collections.EMPTY_MAP);

		URL fileUrl = null;
		fileUrl = FileLocator.toFileURL(url);
		FileInputStream file = new FileInputStream(fileUrl.getPath());

		parseStandardNodesFile(file);
	}

	/**
	 * Loads the actions and sensors from a MMPM domain file. Creates a new
	 * ConceptualNodesTree whose root category is the short name of the file.
	 * The root category contains two sub categories, one for actions (which
	 * includes all the actions loaded from the file), and another one for
	 * conditions (which contains all the boolean sensors loaded form the file).
	 * The tree can be accessed through {@link #getNonStandardNodesTree(String)}
	 * , and its nodes can be accessed through {@link #getNode(String, String)}.
	 */
	public static ConceptualNodesTree loadNonStandardNodes(String fileName) throws IOException {
		FileInputStream fileStream = new FileInputStream(fileName);
		SAXBuilder builder = new SAXBuilder();
		ConceptualNodesTree tree = new ConceptualNodesTree();
		File file = new File(fileName);

		try {
			Document doc = builder.build(fileStream);

			ParsedDomain parsedDomain = new ParsedDomain();
			parsedDomain.init(doc.getRootElement(), null);

			List<ParsedAction> actions = parsedDomain.getActionSet().getAction();

			List<ParsedMethod> conditions = parsedDomain.getSensorSet().getMethods();

			for (ParsedAction action : actions) {
				ConceptualBTNode xmlAction = loadNonStandardAction(action);
				nonStandardNodes.put(
						new Pair<String, String>(xmlAction.getType(), action.getName()), xmlAction);
				tree.insertNode(
						file.getName() + ConceptualNodesTree.CATEGORY_SEPARATOR + "Actions",
						new ConceptualBTNodeItem(xmlAction));
			}

			for (ParsedMethod condition : conditions) {
				if (condition.getReturnedType() == ActionParameterType.BOOLEAN) {
					ConceptualBTNode xmlCondition = loadNonStandardCondition(condition);
					nonStandardNodes.put(
							new Pair<String, String>(xmlCondition.getType(), condition.getName()),
							xmlCondition);
					tree.insertNode(file.getName() + ConceptualNodesTree.CATEGORY_SEPARATOR
							+ "Conditions", new ConceptualBTNodeItem(xmlCondition));
				}
			}

			nonStandardTrees.put(fileName, tree);
			return tree;
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the standard nodes tree or null if it has not been loaded yet
	 * (see {@link #loadStandardNodes()}).
	 */
	public static ConceptualNodesTree getStandardNodesTree() {
		return standardNodesTree;
	}

	/**
	 * Returns the non-standard nodes tree that was loaded from the file whose
	 * name is <code>fileName</code>. Returns null if that tree cannot be found.
	 */
	public static ConceptualNodesTree getNonStandardNodesTree(String fileName) {
		return nonStandardTrees.get(fileName);
	}

	/**
	 * Returns all the non-standard nodes trees currently loaded by the
	 * NodesLoader.
	 */
	public static List<ConceptualNodesTree> getNonStandardNodesTrees() {
		return Arrays.asList(nonStandardTrees.values().toArray(
				new ConceptualNodesTree[nonStandardTrees.size()]));
	}

	/**
	 * Removes the non-standard nodes tree that was loaded from the file
	 * <code>fileName</code> from the list held by the NodesLoader.
	 */
	public static void removeNonStandardNodesTree(String fileName) {
		nonStandardTrees.remove(fileName);
	}

	/**
	 * Returns all the file names of the non-standard nodes trees held by the
	 * NodesLoader.
	 */
	public static String[] getNonStandardTreeNames() {
		return nonStandardTrees.keySet().toArray(new String[nonStandardTrees.size()]);
	}

	/**
	 * Returns a List with all the standard nodes.
	 */
	public static List<ConceptualBTNode> getStandardNodes() {
		return new Vector<ConceptualBTNode>(standardNodes.values());
	}

	/**
	 * Returns a List with all the non-standard nodes.
	 */
	public static List<ConceptualBTNode> getNonStandardNodes() {
		return new Vector<ConceptualBTNode>(nonStandardNodes.values());
	}

	/**
	 * Retrieves a node from all the nodes held by the NodesLoader. For standard
	 * nodes this retrieval process is done through the type ( <code>type</code>
	 * ) only, in which case <code>name</code> is ignored.
	 * <p>
	 * For non-standard nodes, (actions and conditions), the retrieval process
	 * is carried out by using both the type of the node and its name. This is
	 * necessary since all actions share the same type (
	 * {@link NodeInternalType#ACTION}), and so do conditions (
	 * {@link NodeInternalType#CONDITION}), so the only way of telling them
	 * apart is by using their name.
	 */
	public static ConceptualBTNode getNode(String type, String name) {
		if (type.equals(NodeInternalType.ACTION.toString())
				|| type.equals(NodeInternalType.CONDITION.toString())) {
			return nonStandardNodes.get(new Pair<String, String>(type, name));
		} else {
			return standardNodes.get(type);
		}
	}

	private static ConceptualBTNode loadNonStandardCondition(ParsedMethod condition) {
		ConceptualBTNode xmlNode = new ConceptualBTNode();

		xmlNode.setReadableType(condition.getName());
		xmlNode.setType(NodeInternalType.CONDITION.toString());
		xmlNode.setIcon(IconsPaths.CONDITION);
		xmlNode.setNumChildren(0);
		xmlNode.setHasName(true);
		xmlNode.setName(condition.getName());

		List<ParsedActionParameter> parameters = condition.getParameters();

		for (ParsedActionParameter parameter : parameters) {
			Parameter xmlNodeParameter = new Parameter();
			xmlNodeParameter.setName(parameter.getName());
			xmlNodeParameter.setType(fromMMPMParameterType(parameter.getType()));
			/* MMPM parameters are always contextable. */
			xmlNodeParameter.setContextable(true);
			xmlNode.addParameter(xmlNodeParameter);
		}

		return xmlNode;
	}

	private static ConceptualBTNode loadNonStandardAction(ParsedAction action) {
		ConceptualBTNode xmlNode = new ConceptualBTNode();

		xmlNode.setReadableType(action.getName());
		xmlNode.setType(NodeInternalType.ACTION.toString());
		xmlNode.setIcon(IconsPaths.ACTION);
		xmlNode.setNumChildren(0);
		xmlNode.setHasName(true);
		xmlNode.setName(action.getName());

		List<ParsedActionParameter> parameters = action.getParameters();

		for (ParsedActionParameter parameter : parameters) {
			Parameter xmlNodeParameter = new Parameter();
			xmlNodeParameter.setName(parameter.getName());
			xmlNodeParameter.setType(fromMMPMParameterType(parameter.getType()));
			/* MMPM parameters are always contextable. */
			xmlNodeParameter.setContextable(true);
			xmlNode.addParameter(xmlNodeParameter);
		}

		return xmlNode;
	}

	private static ParameterType fromMMPMParameterType(ActionParameterType type) {
		if (type == ActionParameterType.BOOLEAN) {
			return ParameterType.BOOLEAN;
		} else if (type == ActionParameterType.ENTITY_ID) {
			return ParameterType.STRING;
		} else if (type == ActionParameterType.ENTITY_TYPE) {
			return ParameterType.STRING;
		} else if (type == ActionParameterType.FLOAT) {
			return ParameterType.REAL;
		} else if (type == ActionParameterType.INTEGER) {
			return ParameterType.INTEGER;
		} else if (type == ActionParameterType.STRING) {
			return ParameterType.STRING;
		} else if (type == ActionParameterType.PLAYER) {
			return ParameterType.STRING;
		} else if (type == ActionParameterType.COORDINATE) {
			return ParameterType.COORDINATE;
		} else if (type == ActionParameterType.DIRECTION) {
			return ParameterType.DIRECTION;
		} else if (type == ActionParameterType.OBJECT) {
			return ParameterType.OBJECT;
		}

		throw new IllegalArgumentException("Unexpected action parameter type");
	}

	private static List<Exception> parseStandardNodesFile(FileInputStream file) {
		List<Exception> exceptions = new Vector<Exception>();

		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);

			Element root = doc.getRootElement();

			parseElement(null, root);
		} catch (Exception e) {
			exceptions.add(e);
		}

		return exceptions;
	}

	private static void parseElement(String currentPath, Element e) {
		String nodeType = e.getName();

		if (nodeType.equals("Category")) {
			String categoryName = e.getAttributeValue("name");
			List<Element> children = e.getChildren();

			for (Element child : children) {
				String nextPath = currentPath == null ? categoryName : currentPath
						+ ConceptualNodesTree.CATEGORY_SEPARATOR + categoryName;
				parseElement(nextPath, child);
			}
		} else if (nodeType.equals("Node")) {
			ConceptualBTNode xmlNode = new ConceptualBTNode();
			xmlNode.setType(e.getChild("Type").getValue());
			String numChildren = e.getChild("Children").getValue();
			xmlNode.setNumChildren(numChildren.equals("I") ? -1 : Integer.parseInt(numChildren));
			xmlNode.setIcon(e.getChild("Icon").getValue());
			xmlNode.setReadableType(e.getChild("ReadableType").getValue());
			xmlNode.setHasName(false);
			parseParameters(xmlNode, e);
			
			ConceptualBTNodeItem nodeToInsert = new ConceptualBTNodeItem(xmlNode);
			standardNodesTree.insertNode(currentPath, nodeToInsert);
			standardNodes.put(xmlNode.getType(), xmlNode);
			nodesByCategory.put(xmlNode.getType(), currentPath);
		}
	}
	
	public static NodeCategories getCategoryOf(String type)
	{
		if (nodesByCategory.containsKey(type))
		{
			String category = nodesByCategory.get(type);
			String parentType = category.split(ConceptualNodesTree.CATEGORY_SEPARATOR)[1];
			
			if (parentType.contentEquals("Composite"))
			{
				return NodeCategories.COMPOSITE;
			}
			else if (parentType.contentEquals("Decorator"))
			{
				return NodeCategories.DECORATOR;
			}
			else if (parentType.contentEquals("Leaf"))
			{
				return NodeCategories.LEAF;
			}
		}
		
		return NodeCategories.DUMMY;
	}

	private static void parseParameters(ConceptualBTNode node, Element e) {
		Element paramsElement = e.getChild("Parameters");
		if (paramsElement != null) {
			List<Element> parameters = paramsElement.getChildren();

			for (Element parameter : parameters) {
				parseParameter(node, parameter);
			}
		}
	}

	private static void parseParameter(ConceptualBTNode node, Element e) {
		Parameter parameterToAdd = new Parameter();
		parameterToAdd.setName(e.getAttributeValue("name"));
		ParameterType type = ParameterType.valueOf(e.getAttributeValue("type"));
		parameterToAdd.setType(type);

		if (type == ParameterType.NODE_ID) {
			String nodeTypes = e.getAttributeValue("nodetypes");
			nodeTypes = nodeTypes.trim();

			if (!nodeTypes.equals("")) {
				String[] individualNodeTypes = nodeTypes.split("( )+");

				for (String s : individualNodeTypes) {
					parameterToAdd.addNodeClass(s);
				}
			}

			String contextable = e.getAttributeValue("contextable");

			if (contextable.equals("true")) {
				parameterToAdd.setContextable(true);
			} else if (contextable.equals("false")) {
				parameterToAdd.setContextable(false);
			} else {
				throw new RuntimeException("Invalid value for \"contextable\" attribute");
			}
		} 
		
		node.addParameter(parameterToAdd);
	}

	private static void loadRoot() {
		ConceptualBTNode root = new ConceptualBTNode();
		root.setIcon(IconsPaths.ROOT);
		root.setNumChildren(1);
		root.setType(NodeInternalType.ROOT.toString());
		root.setReadableType(NodeInternalType.ROOT.toString());
		root.setHasName(true);

		standardNodes.put(NodeInternalType.ROOT.toString(), root);
	}
}
