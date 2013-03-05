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
package jbt.tools.btlibrarygenerator.modelbtgenerator;

import gatech.mmpm.ActionParameterType;
import gatech.mmpm.tools.parseddomain.ParsedAction;
import gatech.mmpm.tools.parseddomain.ParsedActionParameter;
import gatech.mmpm.tools.parseddomain.ParsedMethod;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbt.execution.core.ExecutionTask.Status;
import jbt.model.core.ModelTask;
import jbt.model.core.ModelTask.Position;
import jbt.model.task.composite.ModelDynamicPriorityList;
import jbt.model.task.composite.ModelParallel;
import jbt.model.task.composite.ModelRandomSelector;
import jbt.model.task.composite.ModelRandomSequence;
import jbt.model.task.composite.ModelSelector;
import jbt.model.task.composite.ModelSequence;
import jbt.model.task.composite.ModelParallel.ParallelPolicy;
import jbt.model.task.composite.ModelStaticPriorityList;
import jbt.model.task.decorator.ModelHierarchicalContextManager;
import jbt.model.task.decorator.ModelInterrupter;
import jbt.model.task.decorator.ModelInverter;
import jbt.model.task.decorator.ModelLimit;
import jbt.model.task.decorator.ModelRepeat;
import jbt.model.task.decorator.ModelSafeContextManager;
import jbt.model.task.decorator.ModelSafeOutputContextManager;
import jbt.model.task.decorator.ModelSucceeder;
import jbt.model.task.decorator.ModelUntilFail;
import jbt.model.task.leaf.ModelFailure;
import jbt.model.task.leaf.ModelPerformInterruption;
import jbt.model.task.leaf.ModelSubtreeLookup;
import jbt.model.task.leaf.ModelSuccess;
import jbt.model.task.leaf.ModelVariableRenamer;
import jbt.model.task.leaf.ModelWait;
import jbt.model.task.leaf.action.ModelAction;
import jbt.model.task.leaf.condition.ModelCondition;
import jbt.tools.btlibrarygenerator.lowlevelgenerator.ActionsGenerator;
import jbt.tools.btlibrarygenerator.lowlevelgenerator.ConditionsGenerator;
import jbt.tools.btlibrarygenerator.util.Util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * This class is used for generating Java expressions for behaviour trees specified in an XML
 * format.
 * <p>
 * The XML format is that of <i>BT Editor</i>. The expression that is generated from the XML
 * behaviour tree is just a composition of {@link ModelTask} objects that represent the behaviour
 * tree in the XML file. An example of such an expression is:
 * 
 * <pre>
 * new ModelSequence(null, new ModelSelector(null, new Condition1ModelCondition(null),
 * 		new Action1ModelAction(null)), new ModelParallel(null, ParallelPolicy.SEQUENCE_POLICY,
 * 		new Action2ModelAction(null), new Action3ModelAction(null)))
 * </pre>
 * 
 * Where <code>Condition1ModelCondition</code>, <code>Action1ModelAction</code>,
 * <code>Action2ModelAction</code> and <code>Action1Mode3Action</code> are user-defined conditions
 * and actions.
 * <p>
 * The expression also assigns each ModelPerformInterruption task its corresponding ModelInterrupter
 * task. Thus, the expression that is obtained is a fully functional behaviour tree (ModelTask).
 * 
 * @see ActionsGenerator
 * @see ConditionsGenerator
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ModelBTGenerator {
	/** Representation of the Tree tag of the XML file. */
	private static String TAG_TREE = "Tree";
	/** Representation of the Node tag of the XML file. */
	private static String TAG_NODE = "Node";

	/** Representation of the Children tag of the XML file. */
	private static String TAG_CHILDREN = "Children";
	/** Representation of the Parameters tag of the XML file. */
	private static String TAG_PARAMETERS = "Parameters";
	/** Representation of the Parameter tag of the XML file. */
	private static String TAG_PARAMETER = "Parameter";
	/** Representation of the Guard tag of the XML file. */
	private static String TAG_GUARD = "Guard";

	/** Representation of the "type" attribute of the XML file. */
	private static String ATTR_TYPE = "type";
	/** Representation of the "name" attribute of the XML file. */
	private static String ATTR_NAME = "name";
	/** Representation of the "runs" attribute of the XML file. */
	private static String ATTR_RUNS = "runs";
	/** Representation of the "duration" attribute of the XML file. */
	private static String ATTR_DURATION = "duration";
	/** Representation of the "policy" attribute of the XML file. */
	private static String ATTR_POLICY = "policy";
	/** Representation of the "from context" attribute of the XML file. */
	private static String ATTR_FROM_CONTEXT = "fromcontext";
	/** Representation of the "id" attribute of the XML file. */
	private static String ATTR_ID = "id";
	/** Representation of the "listOfVariables" attribute of the XML file. */
	private static String ATTR_LIST_OF_VARIABLES = "listOfVariables";

	/** Representation of the Root value of the XML file. */
	private static String VAL_ROOT = "Root";
	/** Representation of the Selector value of the XML file. */
	private static String VAL_SELECTOR = "Selector";
	/** Representation of the Sequence value of the XML file. */
	private static String VAL_SEQUENCE = "Sequence";
	/** Representation of the Parallel value of the XML file. */
	private static String VAL_PARALLEL = "Parallel";
	/** Representation of the Action value of the XML file. */
	private static String VAL_ACTION = "Action";
	/** Representation of the Condition value of the XML file. */
	private static String VAL_CONDITION = "Condition";
	/** Representation of the Random sequence value of the XML file. */
	private static String VAL_RANDOM_SEQUENCE = "RandomSequence";
	/** Representation of the Random selector value of the XML file. */
	private static String VAL_RANDOM_SELECTOR = "RandomSelector";
	/** Representation of the Interrupter value of the XML file. */
	private static String VAL_INTERRUPTER = "Interrupter";
	/** Representation of the Inverter value of the XML file. */
	private static String VAL_INVERTER = "Inverter";
	/** Representation of the Limit value of the XML file. */
	private static String VAL_LIMIT = "Limit";
	/** Representation of the Perform interruption value of the XML file. */
	private static String VAL_PERFORM_INTERRUPTION = "PerformInterruption";
	/** Representation of the Repeat value of the XML file. */
	private static String VAL_REPEAT = "Repeat";
	/** Representation of the Until fail value of the XML file. */
	private static String VAL_UNTIL_FAIL = "UntilFail";
	/** Representation of the Wait value of the XML file. */
	private static String VAL_WAIT = "Wait";
	/** Representation of the Subtree lookup value of the XML file. */
	private static String VAL_SUBTREE_LOOKUP = "SubtreeLookup";
	/** Representation of the Dynamic priority list value of the XML file. */
	private static String VAL_DYNAMIC_PRIORITY_LIST = "DynamicPriorityList";
	/** Representation of the Static priority list value of the XML file. */
	private static String VAL_STATIC_PRIORITY_LIST = "StaticPriorityList";
	/**
	 * Representation of the Hierarchical context manager value of the XML file.
	 */
	private static String VAL_HIERARCHICAL_CONTEXT_MANAGER = "HierarchicalContextManager";
	/** Representation of the Safe context manager value of the XML file. */
	private static String VAL_SAFE_CONTEXT_MANAGER = "SafeContextManager";
	/** Representation of the Safe output context manager value of the XML file. */
	private static String VAL_SAFE_OUTPUT_CONTEXT_MANAGER = "SafeOutputContextManager";
	/** Representation of the Variable renamer value of the XML file. */
	private static String VAL_VARIABLE_RENAMER = "VariableRenamer";
	/** Representation of the Success value of the XML file. */
	private static String VAL_SUCCESS = "Success";
	/** Representation of the Succeeder value of the XML file. */
	private static String VAL_SUCCEEDER = "Succeeder";
	/** Representation of the Success value of the XML file. */
	private static String VAL_FAILURE = "Failure";
	/** Representation of the Sequence parallel policy value of the XML file. */
	private static String VAL_SEQUENCE_POLICY = "sequence";
	/** Representation of the Selector parallel policy value of the XML file. */
	private static String VAL_SELECTOR_POLICY = "selector";
	/** Representation of the Success status value of the XML file. */
	private static String VAL_SUCCESS_STATUS = "success";
	/** Representation of the Failure status value of the XML file. */
	private static String VAL_FAILURE_STATUS = "failure";
	/** Representation of the Variable name value of the XML file. */
	private static String VAL_VARIABLE_NAME = "variableName";
	/** Representation of the New variable name value of the XML file. */
	private static String VAL_NEW_VARIABLE_NAME = "newVariableName";

	/**
	 * Representation of the Node ID value of the Perform Interruption task of the XML file.
	 */
	private static String VAL_NODE_ID = "nodeID";
	/**
	 * Representation of the Expected result value of the Perform Interruption task of the XML file.
	 */
	private static String VAL_EXPECTED_RESULT = "expectedResult";

	/** Name of the java class that represents a selector node of the tree. */
	private static String CLASS_SELECTOR = ModelSelector.class.getCanonicalName();
	/** Name of the java class that represents a sequence node of the tree. */
	private static String CLASS_SEQUENCE = ModelSequence.class.getCanonicalName();
	/** Name of the java class that represents a parallel node of the tree. */
	private static String CLASS_PARALLEL = ModelParallel.class.getCanonicalName();
	/** Name of the java class that represents an action node of the tree. */
	private static String CLASS_ACTION = ModelAction.class.getCanonicalName();
	/** Name of the java class that represents a condition node of the tree. */
	private static String CLASS_CONDITION = ModelCondition.class.getCanonicalName();
	/**
	 * Name of the java class that represents a random sequence node of the tree.
	 */
	private static String CLASS_RANDOM_SEQUENCE = ModelRandomSequence.class.getCanonicalName();
	/**
	 * Name of the java class that represents a random selector node of the tree.
	 */
	private static String CLASS_RANDOM_SELECTOR = ModelRandomSelector.class.getCanonicalName();
	/**
	 * Name of the java class that represents an interrupter node of the tree.
	 */
	private static String CLASS_INTERRUPTER = ModelInterrupter.class.getCanonicalName();
	/** Name of the java class that represents an inverter node of the tree. */
	private static String CLASS_INVERTER = ModelInverter.class.getCanonicalName();
	/** Name of the java class that represents a limit node of the tree. */
	private static String CLASS_LIMIT = ModelLimit.class.getCanonicalName();
	/**
	 * Name of the java class that represents a perform interruption node of the tree.
	 */
	private static String CLASS_PERFORM_INTERRUPTION = ModelPerformInterruption.class
			.getCanonicalName();
	/** Name of the java class that represents a repeat node of the tree. */
	private static String CLASS_REPEAT = ModelRepeat.class.getCanonicalName();
	/** Name of the java class that represents an until fail node of the tree. */
	private static String CLASS_UNTIL_FAIL = ModelUntilFail.class.getCanonicalName();
	/** Name of the java class that represents a wait node of the tree. */
	private static String CLASS_WAIT = ModelWait.class.getCanonicalName();
	/**
	 * Name of the java class that represents a subtree lookup node of the tree.
	 */
	private static String CLASS_SUBTREE_LOOKUP = ModelSubtreeLookup.class.getCanonicalName();
	/**
	 * Name of the java class that represents a dynamic priority list node of the tree.
	 */
	private static String CLASS_DYNAMIC_PRIORITY_LIST = ModelDynamicPriorityList.class
			.getCanonicalName();
	/**
	 * Name of the java class that represents a static priority list node of the tree.
	 */
	private static String CLASS_STATIC_PRIORITY_LIST = ModelStaticPriorityList.class
			.getCanonicalName();

	/**
	 * Name of the java class that represents a hierarchical context manager node of the tree.
	 */
	private static String CLASS_HIERARCHICAL_CONTEXT_MANAGER = ModelHierarchicalContextManager.class
			.getCanonicalName();

	/**
	 * Name of the java class that represents a safe context manager node of the tree.
	 */
	private static String CLASS_SAFE_CONTEXT_MANAGER = ModelSafeContextManager.class
			.getCanonicalName();

	/**
	 * Name of the java class that represents a safe output context manager node of the tree.
	 */
	private static String CLASS_SAFE_OUTPUT_CONTEXT_MANAGER = ModelSafeOutputContextManager.class
			.getCanonicalName();

	/**
	 * Name of the java class that represents the parallel node possible policies.
	 */
	private static String CLASS_PARALLEL_POLICY = ParallelPolicy.class.getCanonicalName();
	/**
	 * Name of the java class that represents the variable renamer task.
	 */
	private static String CLASS_VARIABLE_RENAMER = ModelVariableRenamer.class.getCanonicalName();
	/**
	 * Name of the java class that represents the success task.
	 */
	private static String CLASS_SUCCESS = ModelSuccess.class.getCanonicalName();
	/**
	 * Name of the java class that represents the failure task.
	 */
	private static String CLASS_FAILURE = ModelFailure.class.getCanonicalName();
	/**
	 * Name of the java class that represents the succeeder task.
	 */
	private static String CLASS_SUCCEEDER = ModelSucceeder.class.getCanonicalName();
	/**
	 * Name of the java class that represents a task status.
	 */
	private static String CLASS_STATUS = Status.class.getCanonicalName();

	/**
	 * Pattern for verifying list of variables values. This is used, for instance, for checking the
	 * correctness of the list of variables of the SafeOutputContextManager node.
	 */
	private static final Pattern pattern = Pattern.compile("(( )*\"[a-zA-Z_0-9\\s]+\"( )*)+");

	/**
	 * List that stores which interrupters are associated to which perform interruption nodes.
	 */
	private List<InterrupterMatch> interruptersMatchings;
	/**
	 * The root element of the tree in the BT XML file. This is not the Tree tag, but the root Node
	 * tag.
	 */
	private Element root;
	/**
	 * The name of the package that contains the low level actions of the BT whose expression is
	 * going to be built.
	 */
	private String actionsPackage;
	/**
	 * The name of the package that contains the low level conditions of the BT whose expression is
	 * going to be built.
	 */
	private String conditionsPackage;
	/**
	 * The MMPM definition of the low level actions used when building the BT.
	 */
	private List<ParsedAction> actionsDefinition;
	/**
	 * The MMPM definition of the low level conditions used when building the BT.
	 */
	private List<ParsedMethod> conditionsDefinition;
	/**
	 * The name of the file that contains the tree.
	 */
	private String treeFileName;

	/**
	 * This method creates a String representing the declaration of a BT ( {@link ModelTask}
	 * variable) read from a BT XML file in the format of <i>BT Editor</i>.
	 * <p>
	 * This method reads the XML file <code>treeFileName</code> and creates a ModelTask variable of
	 * name <code>treeVariableName</code> that represents the BT in the file.
	 * <p>
	 * In order to generate the expression for the tree, some additional information must be
	 * provided:
	 * <ul>
	 * <li><code>actionsPackage</code> is the name of the Java package that contains all the low
	 * level actions that are used in the tree.
	 * <li><code>conditionsPackage</code> is the name of the Java package that contains all the low
	 * level conditions that are used in the tree.
	 * <li><code>actionsDefinition</code> is a list containing the MMPM definition of all the low
	 * level actions that are used in the tree.
	 * <li><code>conditionsDefinition</code> is a list containing the MMPM definition of all the low
	 * level conditions that are used in the tree.
	 * <li><code>declareTree</code> is a flag that indicates whether the tree returned expression
	 * should also declare the variable <code>treeVariableName</code> or assume that it already
	 * exists. If true, it will be declared.
	 * </ul>
	 * 
	 * @param treeVariableName
	 *            name of the variable that will store the tree.
	 * @param treeFileName
	 *            XML file that contains the BT.
	 * @param actionsPackage
	 *            name of the Java package that contains all the low level actions that are used in
	 *            the tree.
	 * @param conditionsPackage
	 *            name of the Java package that contains all the low level conditions that are used
	 *            in the tree.
	 * @param actionsDefinition
	 *            list containing the MMPM definition of all the low level actions that are used in
	 *            the tree.
	 * @param conditionsDefinition
	 *            list containing the MMPM definition of all the low level conditions that are used
	 *            in the tree.
	 * @param declareTree
	 *            flag that indicates whether the tree returned expression should also declare the
	 *            variable <code>treeVariableName</code> or assume that it already exists. If true,
	 *            it will be declared.
	 * @return a String representing a expression that assigns to the variable
	 *         <code>treeVariableName</code> a ModelTask representing the behaviour tree in
	 *         <code>treeFileName</code>, as explained above.
	 * @throws ModelBTGenerationException
	 *             if there is any error when creating the expression for the behaviour tree.
	 */
	public String getModelBTDeclaration(String treeVariableName, String treeFileName,
			String actionsPackage, String conditionsPackage, List<ParsedAction> actionsDefinition,
			List<ParsedMethod> conditionsDefinition, boolean declareTree)
			throws ModelBTGenerationException {
		try {
			this.treeFileName = treeFileName;
			FileInputStream file = new FileInputStream(treeFileName);
			BTFileParseResult result = parseBTFile(file, actionsPackage, conditionsPackage,
					actionsDefinition, conditionsDefinition);
			file.close();
			return completeModelBTDeclaration(treeVariableName, result, declareTree);
		} catch (Exception e) {
			throw new ModelBTGenerationException(
					"Could not generate an expresion for the behaviour tree: " + e.getMessage(), e);
		}
	}

	/**
	 * This method parses a BT XML file in the format of <i>BT Editor</i> and mainly generates a
	 * "new" expression that is able to build the tree in the file.
	 * <p>
	 * This function returns a {@link BTFileParseResult} object that contains:
	 * <ul>
	 * <li>A "new" expression (as a String) that is able to construct the tree specified in the
	 * file.
	 * <li>A list of all the matches between interrupters and perform interruption tasks in the
	 * tree.
	 * </ul>
	 * 
	 * It must be noted that the "new" expression is somehow incomplete, since interrupters are not
	 * assigned to perform interruptions. That is why the list of match is given. By using it, the
	 * external caller should take care of creating another expression that links each perform
	 * interruption with its corresponding interrupter task.
	 * 
	 * @param file
	 *            the XML file that contains the behaviour tree.
	 * @param actionsPackage
	 *            name of the Java package that contains all the low level actions that are used in
	 *            the tree.
	 * @param conditionsPackage
	 *            name of the Java package that contains all the low level conditions that are used
	 *            in the tree.
	 * @param actionsDefinition
	 *            list containing the MMPM definition of all the low level actions that are used in
	 *            the tree.
	 * @param conditionsDefinition
	 *            list containing the MMPM definition of all the low level conditions that are used
	 *            in the tree.
	 * @return a BTFileParseResult as described above.
	 */
	private BTFileParseResult parseBTFile(FileInputStream file, String actionsPackage,
			String conditionsPackage, List<ParsedAction> actionsDefinition,
			List<ParsedMethod> conditionsDefinition) {

		this.actionsDefinition = actionsDefinition;
		this.conditionsDefinition = conditionsDefinition;
		this.actionsPackage = actionsPackage;
		this.conditionsPackage = conditionsPackage;

		SAXBuilder builder = new SAXBuilder();

		StringBuffer result = new StringBuffer();

		try {
			Document doc = builder.build(file);
			Element documentRoot = doc.getRootElement();

			this.root = (Element) ((Element) (((Element) documentRoot.getChildren().get(0))
					.getChildren().get(0))).getChildren().get(0);

			this.interruptersMatchings = new LinkedList<InterrupterMatch>();

			processElement(new LinkedList<Position>(), this.root, result, this.root);

			return new BTFileParseResult(result.toString(), this.interruptersMatchings);
		} catch (Exception e) {
			throw new RuntimeException("Could not generate an expresion for the behaviour tree: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Given the result of the method
	 * {@link #parseBTFile(FileInputStream, String, String, List, List)}, this method completes the
	 * declaration of the behaviour tree, adding whatever stuff is necessary in order for the tree
	 * to be usable.
	 * <p>
	 * This method returns a String such as:
	 * <ul>
	 * <li>The "new" expression <code>generationResult</code> is assigned to a variable of name
	 * <code>treeVariableName</code>.
	 * <li>New statements are added so that the interrupters-perform interruptions matches in
	 * <code>generationResult</code> are carried out.
	 * </ul>
	 * 
	 * If <code>declareTree</code> is true, then <code>treeVariableName</code> will be declared (as
	 * a {@link ModelTask}). Otherwise, this method will assume that the variable already exists.
	 * 
	 * @param treeVariableName
	 *            name of the variable that will store the tree.
	 * @param generationResult
	 *            the BTFileParseResult from which the final expression for declaring the tree is
	 *            constructed.
	 * @param declareTree
	 *            flag that indicates whether the tree returned expression should also declare the
	 *            variable <code>treeVariableName</code> or assume that it already exists. If true,
	 *            it will be declared.
	 * @return a String as described above.
	 */
	private String completeModelBTDeclaration(String treeVariableName,
			BTFileParseResult generationResult, boolean declareTree) {
		String result = new String();

		result += (declareTree ? ModelTask.class.getCanonicalName() + " " : "") + treeVariableName
				+ " = " + generationResult.getModelBTExpression() + ";";

		if (generationResult.getInterruptersMatches().size() != 0) {
			result += "\n\n";
			for (InterrupterMatch match : generationResult.getInterruptersMatches()) {
				result += getInterrupterExpression(treeVariableName, match) + ";";
			}
		}

		return result;
	}

	/**
	 * Given an InterrupterMatch, this method creates a expression that assigns the perform
	 * interrupter its corresponding interrupter. <code>treeVariableName</code> is the name of the
	 * ModelTask variable that stores the tree.
	 */
	private String getInterrupterExpression(String treeVariableName, InterrupterMatch match) {
		/* First, get a "new" expression for the position of the interrupter. */
		String interrupterPositionDeclaration = getPositionNewExpression(match
				.getInterrupterPosition());

		/*
		 * Then, get a "new" expression for the position of the perform interruption.
		 */
		String performInterruptionPositionDeclaration = getPositionNewExpression(match
				.getPerformInterruptionPosition());

		/*
		 * Expression that points to the subtree where the match between interrupter and perform
		 * interruption takes place.
		 */
		String destinationTreeExpression = treeVariableName;

		for (Position pathToTree : match.getPathToTree()) {
			destinationTreeExpression += ".findNode(" + getPositionNewExpression(pathToTree)
					+ ").getGuard()";
		}

		/* Finally create the expression that sets the match. */
		String result = "((" + ModelPerformInterruption.class.getCanonicalName() + ")"
				+ destinationTreeExpression + ".findNode(" + performInterruptionPositionDeclaration
				+ ")).setInterrupter((" + ModelInterrupter.class.getCanonicalName() + ")"
				+ destinationTreeExpression + ".findNode(" + interrupterPositionDeclaration + "))";

		return result;
	}

	/**
	 * Returns a "new" expression for a Position object.
	 */
	private String getPositionNewExpression(Position pos) {
		String result = "new " + Position.class.getCanonicalName() + "(";

		if (pos.getMoves().size() != 0) {
			for (Integer move : pos.getMoves()) {
				result += move + ", ";
			}

			result = result.substring(0, result.length() - 2);
		}

		result += ")";

		return result;
	}

	/**
	 * This method creates the "new" statement for a BT given its representation in XML format.
	 * <p>
	 * <code>e</code> is the element that is currently being analyzed, that for which the expression
	 * will be produced (it must be a Node element of the XML file).
	 * <p>
	 * <code>pathToCurrentTree</code> represents the path to the behaviour tree that is currently
	 * being analyzed. An empty list represents the original behaviour tree. A non-empty list
	 * represents a guard of some node of the tree, or even a guard of a guard of the original tree.
	 * See {@link InterrupterMatch} for more information about this. <code>currentRoot</code> is the
	 * root of the tree that is currently being analyzed.
	 * <p>
	 * The expression that is returned is complete except for the fact that interrupters are not
	 * assigned to perform interruptions.
	 * <p>
	 * All the matches among interrupters and perform interruptions are stored in
	 * {@link #interruptersMatchings}, so that they can be used later on to complete the expression
	 * for tree.
	 */
	private void processElement(List<Position> pathToCurrentTree, Element currentRoot,
			StringBuffer result, Element e) {
		if (e.getName().equals(TAG_CHILDREN)) {
			/* If tag is "children", process all the children. */
			List<Element> children = e.getChildren();

			for (Element child : children) {
				StringBuffer partialResult = new StringBuffer();
				processElement(pathToCurrentTree, currentRoot, partialResult, child);
				result.append(", " + partialResult);
			}
		} else if (e.getName().equals(TAG_NODE)) {
			/* First of all, process the guard. */
			Element guard = e.getChild(TAG_GUARD);

			String guardExpression;

			if (guard != null) {
				StringBuffer guardExpressionBuffer = new StringBuffer();
				List<Position> nextPath = new LinkedList<Position>(pathToCurrentTree);
				nextPath.add(findNode(currentRoot, e.getAttributeValue(ATTR_ID), new Position()));
				processElement(nextPath, guard.getChild(TAG_NODE), guardExpressionBuffer,
						guard.getChild(TAG_NODE));
				guardExpression = guardExpressionBuffer.toString();
			} else {
				guardExpression = "null";
			}

			/* Then, construct an expression for the node itself. */
			String type = e.getAttributeValue(ATTR_TYPE);

			if (type != null) {
				if (type.equals(VAL_SELECTOR)) {
					result.append("new " + CLASS_SELECTOR + "(" + guardExpression);
				} else if (type.equals(VAL_SEQUENCE)) {
					result.append("new " + CLASS_SEQUENCE + "(" + guardExpression);
				} else if (type.equals(VAL_RANDOM_SEQUENCE)) {
					result.append("new " + CLASS_RANDOM_SEQUENCE + "(" + guardExpression);
				} else if (type.equals(VAL_RANDOM_SELECTOR)) {
					result.append("new " + CLASS_RANDOM_SELECTOR + "(" + guardExpression);
				} else if (type.equals(VAL_INTERRUPTER)) {
					result.append("new " + CLASS_INTERRUPTER + "(" + guardExpression);
				} else if (type.equals(VAL_LIMIT)) {
					String runs = e.getChild(TAG_PARAMETERS).getChild(TAG_PARAMETER).getText();
					result.append("new " + CLASS_LIMIT + "(" + guardExpression + ", " + runs);
				} else if (type.equals(VAL_INVERTER)) {
					result.append("new " + CLASS_INVERTER + "(" + guardExpression);
				} else if (type.equals(VAL_PERFORM_INTERRUPTION)) {
					/* Note that the interrupter is set to null. */

					List<Element> parameters = e.getChild(TAG_PARAMETERS)
							.getChildren(TAG_PARAMETER);

					/* First, retrieve the expected result parameter. */
					Element desiredResultParameter = null;

					for (Element currentParameter : parameters) {
						if (currentParameter.getAttributeValue(ATTR_NAME).equals(
								VAL_EXPECTED_RESULT)) {
							desiredResultParameter = currentParameter;
							break;
						}
					}

					Status desiredResult = statusFromString(desiredResultParameter.getText());

					result.append("new " + CLASS_PERFORM_INTERRUPTION + "(" + guardExpression
							+ ", null, " + CLASS_STATUS + "." + desiredResult.toString());

					/*
					 * Now we have to retrieve the ID of the interrupter that this perform
					 * interruption will interrupt.
					 */
					Element nodeIDParameter = null;

					for (Element currentParameter : parameters) {
						if (currentParameter.getAttributeValue(ATTR_NAME).equals(VAL_NODE_ID)) {
							nodeIDParameter = currentParameter;
							break;
						}
					}

					String nodeID = nodeIDParameter.getText();

					/*
					 * Now create the InterrupterMatch that represents the match between the
					 * interrupter and the perform interruption.
					 */
					Position interrupterPosition = findNode(currentRoot, nodeID, new Position());

					if (interrupterPosition == null) {
						throw new RuntimeException("Could not find node with ID \"" + nodeID
								+ "\" in the behaviour tree.");
					}

					Position performInterruptionPosition = findNode(currentRoot,
							e.getAttributeValue(ATTR_ID), new Position());

					this.interruptersMatchings.add(new InterrupterMatch(interrupterPosition,
							performInterruptionPosition, pathToCurrentTree));
				} else if (type.equals(VAL_REPEAT)) {
					result.append("new " + CLASS_REPEAT + "(" + guardExpression);
				} else if (type.equals(VAL_UNTIL_FAIL)) {
					result.append("new " + CLASS_UNTIL_FAIL + "(" + guardExpression);
				} else if (type.equals(VAL_WAIT)) {
					String duration = e.getChild(TAG_PARAMETERS).getChild(TAG_PARAMETER).getText();
					result.append("new " + CLASS_WAIT + "(" + guardExpression + ", " + duration);
				} else if (type.equals(VAL_PARALLEL)) {
					ParallelPolicy policy = parallelPolicyFromString(e.getChild(TAG_PARAMETERS)
							.getChild(TAG_PARAMETER).getText());
					result.append("new " + CLASS_PARALLEL + "(" + guardExpression + ","
							+ CLASS_PARALLEL_POLICY + "." + policy.toString());
				} else if (type.equals(VAL_SUBTREE_LOOKUP)) {
					String subtreeName = e.getChild(TAG_PARAMETERS).getChild(TAG_PARAMETER)
							.getText();
					result.append("new " + CLASS_SUBTREE_LOOKUP + "(" + guardExpression + ","
							+ "\"" + subtreeName + "\"");
				} else if (type.equals(VAL_DYNAMIC_PRIORITY_LIST)) {
					result.append("new " + CLASS_DYNAMIC_PRIORITY_LIST + "(" + guardExpression);
				} else if (type.equals(VAL_STATIC_PRIORITY_LIST)) {
					result.append("new " + CLASS_STATIC_PRIORITY_LIST + "(" + guardExpression);
				} else if (type.equals(VAL_HIERARCHICAL_CONTEXT_MANAGER)) {
					result.append("new " + CLASS_HIERARCHICAL_CONTEXT_MANAGER + "("
							+ guardExpression);
				} else if (type.equals(VAL_SAFE_CONTEXT_MANAGER)) {
					result.append("new " + CLASS_SAFE_CONTEXT_MANAGER + "(" + guardExpression);

				} else if (type.equals(VAL_SAFE_OUTPUT_CONTEXT_MANAGER)) {
					result.append("new " + CLASS_SAFE_OUTPUT_CONTEXT_MANAGER + "("
							+ guardExpression + ", " + getSafeOutputContextOutputListOfVariables(e));

				} else if (type.equals(VAL_SUCCESS)) {
					result.append("new " + CLASS_SUCCESS + "(" + guardExpression);
				} else if (type.equals(VAL_FAILURE)) {
					result.append("new " + CLASS_FAILURE + "(" + guardExpression);
				} else if (type.equals(VAL_SUCCEEDER)) {
					result.append("new " + CLASS_SUCCEEDER + "(" + guardExpression);
				} else if (type.equals(VAL_VARIABLE_RENAMER)) {
					List parameters = e.getChild(TAG_PARAMETERS).getChildren();
					Element param1 = (Element) parameters.get(0);
					Element param2 = (Element) parameters.get(1);

					String variableName = new String();
					String newVariableName = new String();

					if (param1.getAttributeValue(ATTR_NAME).equals(VAL_VARIABLE_NAME)) {
						variableName = param1.getText();
					} else {
						newVariableName = param1.getText();
					}

					if (param2.getAttributeValue(ATTR_NAME).equals(VAL_VARIABLE_NAME)) {
						variableName = param2.getText();
					} else {
						newVariableName = param2.getText();
					}

					result.append("new " + CLASS_VARIABLE_RENAMER + "(" + guardExpression + ", \""
							+ variableName + "\", \"" + newVariableName + "\"");
				} else if (type.equals(VAL_ACTION)) {
					String actionName = e.getAttributeValue(ATTR_NAME);
					ParsedAction mmpmAction = getAction(actionName);
					if (mmpmAction == null) {
						throw new RuntimeException("The tree read from " + this.treeFileName
								+ " makes use of an action -" + actionName
								+ "- that could not be found in the domain files");
					}
					String parameters = getActionStaticParameters(e, getAction(actionName));
					result.append("new " + actionsPackage + "." + actionName + "("
							+ guardExpression + (parameters == null ? "" : ", " + parameters));
				} else if (type.equals(VAL_CONDITION)) {
					String conditionName = e.getAttributeValue(ATTR_NAME);
					ParsedMethod mmpmCondition = getCondition(conditionName);
					if (mmpmCondition == null) {
						throw new RuntimeException("The tree read from " + this.treeFileName
								+ " makes use of a condition -" + conditionName
								+ "- that could not be found in the domain files");
					}
					String parameters = getConditionStaticParameters(e, mmpmCondition);
					result.append("new " + conditionsPackage + "." + e.getAttributeValue(ATTR_NAME)
							+ "(" + guardExpression + (parameters == null ? "" : ", " + parameters));
				}

				List<Element> children = e.getChildren();

				if (children.size() != 0) {
					Element childrenNode = null;

					for (int i = 0; i < children.size(); i++) {
						Element currentChild = children.get(i);

						if (currentChild.getName().equals(TAG_CHILDREN)) {
							childrenNode = currentChild;
							break;
						}
					}

					if (childrenNode != null) {
						StringBuffer partialResult = new StringBuffer();
						processElement(pathToCurrentTree, currentRoot, partialResult, childrenNode);
						result.append(partialResult);
					}
				}

				result.append(")");
			}
		}
	}

	/**
	 * Given the String representation of a parallel task policy in the XML file, this method
	 * returns the corresponding {@link ParallelPolicy} object.
	 * 
	 * @param value
	 *            the string representation of the parallel policy.
	 * @return the ParallelPolicy associated to <code>value</code>.
	 */
	private ParallelPolicy parallelPolicyFromString(String value) {
		if (value.equalsIgnoreCase(VAL_SEQUENCE_POLICY)) {
			return ParallelPolicy.SEQUENCE_POLICY;
		}
		if (value.equalsIgnoreCase(VAL_SELECTOR_POLICY)) {
			return ParallelPolicy.SELECTOR_POLICY;
		}
		throw new RuntimeException("Invalid string for parallel policy: " + value);
	}

	/**
	 * Given the String representation of a task status in the XML file, this method returns the
	 * corresponding {@link Status} object.
	 * 
	 * @param value
	 *            the string representation of the task status.
	 * @return the Status associated to <code>value</code>.
	 */
	private Status statusFromString(String value) {
		if (value.equalsIgnoreCase(VAL_SUCCESS_STATUS)) {
			return Status.SUCCESS;
		}
		if (value.equalsIgnoreCase(VAL_FAILURE_STATUS)) {
			return Status.FAILURE;
		}
		throw new RuntimeException("Invalid string for task status: " + value);
	}

	/**
	 * <code>e</code> is an Element representing a node ({@link #TAG_NODE}) of type action (
	 * {@link #VAL_ACTION}) in the BT XML file. <code>actionDefinition</code> represents the MMPM
	 * definition of such action (MMPM action). This method returns a String representing the list
	 * of values for the parameters that the corresponding ModelAction object receives in its
	 * constructor.
	 * <p>
	 * For each parameter in the XML file, two values are created:
	 * <ul>
	 * <li>The value itself: if an actual value is specified in the XML file (that is, the value of
	 * the parameter is not going to be read from the context), an object containing that value is
	 * created. Otherwise, it is null.
	 * <li>The context location: if an actual value is not specified (that is, the value is going to
	 * be read from the context), then a String containing the location in the context is created.
	 * Otherwise, it is null.
	 * </ul>
	 * 
	 * The parameters are separated by commas. If the element does not have any parameters, null is
	 * returned.
	 * 
	 * @param e
	 *            the XML node element representing the action.
	 * @param actionDefinition
	 *            the MMPM definition of the action.
	 * @return a String as described above.
	 */
	private String getActionStaticParameters(Element e, ParsedAction actionDefinition) {
		String result = null;

		Element parametersElement = e.getChild(TAG_PARAMETERS);
		if (parametersElement != null) {
			List<Element> parameters = parametersElement.getChildren();
			List<ParsedActionParameter> parametersDefinition = actionDefinition.getParameters();

			if (parameters.size() != parametersDefinition.size()) {
				throw new RuntimeException(
						"The number of parameters of the action "
								+ actionDefinition.getName()
								+ " defined in the MMPM domain file does not match that of the action in the XML file");
			}

			if (parameters.size() != 0) {
				Iterator<Element> parametersIterator = parameters.iterator();
				Iterator<ParsedActionParameter> parametersDefinitionIterator = parametersDefinition
						.iterator();

				result = new String();

				while (parametersIterator.hasNext()) {
					Element currentParam = parametersIterator.next();
					ParsedActionParameter currentParamDefinition = parametersDefinitionIterator
							.next();

					if (currentParam.getAttributeValue(ATTR_FROM_CONTEXT).equals("true")) {
						result += "null, " + "\"" + currentParam.getText() + "\"" + ", ";
					} else {
						/*
						 * When the parameter does not come from the context, an appropriate object
						 * must be constructed.
						 */
						result += getNewExpression(currentParamDefinition.getType(),
								currentParam.getText())
								+ ", null, ";
					}
				}

				result = result.substring(0, result.length() - 2);
			}
		}

		return result;
	}

	/**
	 * <code>e</code> is an Element representing a node ({@link #TAG_NODE}) of type condition (
	 * {@link #VAL_CONDITION}) in the BT XML file. <code>conditionDefinition</code> represents the
	 * MMPM definition of such condition (MMPM sensor). This method returns a String representing
	 * the list of values for the parameters that the corresponding ModelCondition object receives
	 * in its constructor.
	 * <p>
	 * For each parameter in the XML file, two values are created:
	 * <ul>
	 * <li>The value itself: if an actual value is specified in the XML file (that is, the value of
	 * the parameter is not going to be read from the context), an object containing that value is
	 * created. Otherwise, it is null.
	 * <li>The context location: if an actual value is not specified (that is, the value is going to
	 * be read from the context), then a String containing the location in the context is created.
	 * Otherwise, it is null.
	 * </ul>
	 * 
	 * The parameters are separated by commas. If the element does not have any parameters, null is
	 * returned.
	 * 
	 * @param e
	 *            the XML node element representing the condition.
	 * @param conditionDefinition
	 *            the MMPM definition of the condition.
	 * @return a String as described above.
	 */
	private String getConditionStaticParameters(Element e, ParsedMethod conditionDefinition) {
		String result = null;

		Element parametersElement = e.getChild(TAG_PARAMETERS);
		if (parametersElement != null) {
			List<Element> parameters = parametersElement.getChildren();
			List<ParsedActionParameter> parametersDefinition = conditionDefinition.getParameters();

			if (parameters.size() != parametersDefinition.size()) {
				throw new RuntimeException(
						"The number of parameters of the sensor "
								+ conditionDefinition.getName()
								+ " defined in the MMPM domain file does not match that of the condition in the XML file");
			}

			if (parameters.size() != 0) {
				Iterator<Element> parametersIterator = parameters.iterator();
				Iterator<ParsedActionParameter> parametersDefinitionIterator = parametersDefinition
						.iterator();

				result = new String();

				while (parametersIterator.hasNext()) {
					Element currentParam = parametersIterator.next();
					ParsedActionParameter currentParamDefinition = parametersDefinitionIterator
							.next();

					if (currentParam.getAttributeValue(ATTR_FROM_CONTEXT).equals("true")) {
						/* If the parameter comes from the context... */
						result += "null, " + "\"" + currentParam.getText() + "\"" + ", ";
					} else {
						/*
						 * When the parameter does not come from the context, an appropriate object
						 * must be constructed.
						 */
						result += getNewExpression(currentParamDefinition.getType(),
								currentParam.getText())
								+ ", null, ";
					}
				}

				result = result.substring(0, result.length() - 2);
			}
		}

		return result;
	}

	/**
	 * Given a MMPM parameter type and a String read from the BT XML file, this function returns a
	 * String representing a "new" statement that creates an object of a compatible type with the
	 * specified value. Keep in mind that MMPM types are converted according to
	 * {@link Util#fromMMPMParameterType(ActionParameterType)}. Note that
	 * {@link ActionParameterType#ENTITY_ID} is not allowed, since such MMPM type is treated as a
	 * Java Object that must neccesarily be retrieved from the context.
	 * 
	 * @param type
	 *            the MMPM type of the expression.
	 * @param value
	 *            the value of the expression.
	 * @return the "new" expression that constructs a variable of a type compatible with
	 *         <code>type</code> and value <code>value</code>.
	 */
	private String getNewExpression(ActionParameterType type, String value) {
		Class actualClass = Util.fromMMPMParameterType(type);

		if (actualClass.equals(Integer.class)) {
			return "(int)" + value;
		} else if (actualClass.equals(Float.class)) {
			return "(float)" + value;
		} else if (actualClass.equals(Boolean.class)) {
			return "(boolean)" + value;
		} else if (actualClass.equals(String.class)) {
			return "\"" + value + "\"";
		} else if (actualClass.equals(float[].class)) {
			String[] numbers = value.split("( )+");
			String result = "new " + float[].class.getCanonicalName() + "{";
			for (String currentNumber : numbers) {
				result += currentNumber + ", ";
			}
			result = result.substring(0, result.length() - 2);
			result += "}";
			return result;
		}

		throw new RuntimeException("Unexpected action parameter type: " + type.name());
	}

	/**
	 * Given a Node element ({@link #TAG_NODE}) <code>nodeElement</code> of the BT in the XML file,
	 * this method returns the position of a descendant Node element (also including
	 * <code>nodeElement</code>) whose identifier ( {@link #ATTR_ID}) is <code>nodeID</code>. The
	 * position is computed from <code>currentPosition</code>, which is the position of
	 * <code>nodeElement</code> in the tree.
	 * <p>
	 * If such a node is not found, null is returned.
	 * 
	 * @param nodeElement
	 *            the element from which the search starts.
	 * @param nodeID
	 *            the identifier of the node being searched for.
	 * @param currentPosition
	 *            the position of <code>nodeElement</code> in the tree.
	 * @return the node with identifier <code>nodeID</code>, or null in case it is not found.
	 */
	private Position findNode(Element nodeElement, String nodeID, Position currentPosition) {

		if (nodeElement.getAttributeValue(ATTR_ID).equals(nodeID)) {
			return currentPosition;
		}

		Element childrenElement = nodeElement.getChild(TAG_CHILDREN);

		if (childrenElement != null) {
			List<Element> children = childrenElement.getChildren();

			for (int i = 0; i < children.size(); i++) {
				Position currentChildPosition = new Position(currentPosition);
				currentChildPosition.addMove(i);
				Position found = findNode(children.get(i), nodeID, currentChildPosition);
				if (found != null) {
					return found;
				}
			}
		}

		return null;
	}

	/**
	 * An InterrupterMatch just represents a match between an interrupter and a perform interruption
	 * task in a BT. This class mainly stores two positions, the positions of the tasks that match.
	 * <p>
	 * When a match has to be performed, we must take into account that the match itself may be in
	 * the tasks of a node's guard. This match may even be between tasks of a guard of a node's
	 * guard, and so on.
	 * <p>
	 * We may therefore think that matches happen between tasks in trees, and that each guard is a
	 * tree independent from other guards as well as the original tree. Therefore, the
	 * InterrupterMatch also includes some information to know what is the tree (guard, or guard
	 * within guard, and so on) where the match has to be applied. This is just a list of Position
	 * objects. If the list if empty, the match has to be done in the original tree. If not, each
	 * Position object represents a path from the root of the last tree to the next guard. For
	 * example, if the list contains one Position object, the match is between two tasks in the tree
	 * whose root is the guard of the node pointed by such position. If the list contains two
	 * Position objects, then the same process is applied twice: the first Position object points to
	 * a guard object (that of the node pointed by the position). The second Position object is used
	 * to get the next guard, starting from that obtained from the first Position. The match is
	 * between two tasks of the tree whose root is this last guard.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class InterrupterMatch {
		/** The position of the interrupter. */
		private Position interrupterPosition;
		/** The position of the perform interruption. */
		private Position performInterruptionPosition;
		/** The path to the subtree where the match takes place. */
		private List<Position> pathToTree;

		/**
		 * Creates an InterrupterMatch with two positions.
		 * 
		 * @param interrupterPosition
		 *            the position of the interrupter.
		 * @param performInterruptionPosition
		 *            the position of the perform interruption.
		 * @param pathToTree
		 *            the path to the tree where the match takes place, as described in
		 *            {@link InterrupterMatch}.
		 */
		public InterrupterMatch(Position interrupterPosition, Position performInterruptionPosition,
				List<Position> pathToTree) {

			if (interrupterPosition == null) {
				throw new IllegalArgumentException();
			}

			if (performInterruptionPosition == null) {
				throw new IllegalArgumentException();
			}

			if (pathToTree == null) {
				throw new IllegalArgumentException();
			}

			this.interrupterPosition = interrupterPosition;
			this.performInterruptionPosition = performInterruptionPosition;
			this.pathToTree = pathToTree;
		}

		/**
		 * Returns the position of the interrupter.
		 */
		public Position getInterrupterPosition() {
			return this.interrupterPosition;
		}

		/**
		 * Returns the position of the perform interruption.
		 */
		public Position getPerformInterruptionPosition() {
			return this.performInterruptionPosition;
		}

		/**
		 * Returns the path to the tree where the match takes place. Returns an empty list if the
		 * match is done between two tasks of the original behaviour tree.
		 */
		public List<Position> getPathToTree() {
			return this.pathToTree;
		}
	}

	/**
	 * Class that stores the result of the method
	 * {@link ModelBTGenerator#parseBTFile(FileInputStream, String, String, List, List)} .
	 * <p>
	 * When a BT XML file is parsed, two things are generated:
	 * <ul>
	 * <li>An "new" expression for the tree, not even ended in ";". This expression, however, is
	 * incomplete, since interrupters are not associated with any perform interruption.
	 * <li>A match between interrupters and perform interruption tasks in the tree. This match is
	 * represented as a list containing {@link InterrupterMatch} object, one for each match.
	 * </ul>
	 * 
	 * After parsing the BT XML file, the result (BTFileParseResult) should be further processed in
	 * order to complete the declaration expression of the behaviour tree. One of the things that
	 * must be done is, for instance, do the actual match between interrupters and perform
	 * interruption tasks of the created tree.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	private static class BTFileParseResult {
		/** The expression of the BT, with no ";" at the end. */
		private String modelBTExpression;
		/**
		 * The list of match between interrupters and perform interruptions.
		 */
		private List<InterrupterMatch> interruptersMatches;

		/**
		 * Builds a {@link BTFileParseResult}.
		 * 
		 * @param modelBTExpression
		 *            the expression of the BT, with no ";" at the end.
		 * @param interruptersMatchings
		 *            the list of match between interrupters and perform interruptions that should
		 *            be done in the tree represented by <code>modelBTExpression</code>.
		 */
		public BTFileParseResult(String modelBTExpression,
				List<InterrupterMatch> interruptersMatchings) {
			if (modelBTExpression == null) {
				throw new IllegalArgumentException();
			}

			if (interruptersMatchings == null) {
				throw new IllegalArgumentException();
			}

			this.modelBTExpression = modelBTExpression;
			this.interruptersMatches = interruptersMatchings;
		}

		/**
		 * Returns the expression of the BT.
		 */
		public String getModelBTExpression() {
			return this.modelBTExpression;
		}

		/**
		 * Returns the matches that must be performed between interrupters and performs
		 * interruptions in the tree.
		 */
		public List<InterrupterMatch> getInterruptersMatches() {
			return this.interruptersMatches;
		}
	}

	/**
	 * Returns the ParsedAction in {@link #actionsDefinition} whose name is <code>name</code>, or
	 * null if not found.
	 */
	private ParsedAction getAction(String name) {
		for (ParsedAction a : this.actionsDefinition)
			if (a.getName().equals(name))
				return a;
		return null;
	}

	/**
	 * Returns the ParsedMethod in {@link #conditionsDefinition} whose name is <code>name</code>, or
	 * null if not found.
	 */
	private ParsedMethod getCondition(String name) {
		for (ParsedMethod m : this.conditionsDefinition)
			if (m.getName().equals(name))
				return m;
		return null;
	}

	/**
	 * Given a SafeOutputContextManager node of the XML file, this method returns a String
	 * representing the declaration of a List containing the names of its output variables.
	 */
	private String getSafeOutputContextOutputListOfVariables(Element e) {
		/* First, check if the list of variables is syntactically correct. */
		String listOfVariablesString = e.getChild(TAG_PARAMETERS).getChild(TAG_PARAMETER).getText();
		Matcher matcher = pattern.matcher(listOfVariablesString);

		if (!matcher.matches()) {
			throw new RuntimeException("List of variables syntactically incorrect: "
					+ listOfVariablesString);
		}

		/* Now construct a List with all output variables. */
		List<String> listOfVariables = new LinkedList<String>();

		int startQuotations = listOfVariablesString.indexOf('"');

		while (true) {
			int endQuotations = listOfVariablesString.indexOf('"', startQuotations + 1);

			listOfVariables
					.add(listOfVariablesString.substring(startQuotations + 1, endQuotations));

			startQuotations = listOfVariablesString.indexOf('"', endQuotations + 1);

			if (startQuotations == -1) {
				break;
			}
		}

		/*
		 * Now construct an expression that creates the list of the output variable of the
		 * SafeOuputContext.
		 */
		String result = Arrays.class.getCanonicalName() + ".asList(";

		String arrayExpression = "new " + String[].class.getCanonicalName() + "{";

		for (String variable : listOfVariables) {
			arrayExpression += "\"" + variable + "\", ";
		}
		arrayExpression = arrayExpression.substring(0, arrayExpression.length() - 2) + "}";

		result += arrayExpression + ")";

		return result;
	}
}
