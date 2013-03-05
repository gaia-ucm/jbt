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
package jbt.tools.btlibrarygenerator.lowlevelgenerator;

import java.util.LinkedList;
import java.util.List;

import jbt.execution.core.ExecutionTask;
import jbt.execution.task.leaf.action.ExecutionAction;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.action.ModelAction;
import jbt.tools.btlibrarygenerator.util.Util;
import jbt.util.Pair;
import gatech.mmpm.tools.parseddomain.ParsedAction;
import gatech.mmpm.tools.parseddomain.ParsedActionParameter;

/**
 * Class used for generating Java-BT expressions for MMPM actions (represented
 * as {@link ParsedAction} objects).
 * <p>
 * Given a ParsedAction, this class can be used for generating a
 * {@link ModelAction} or an {@link ExecutionAction} for such an action. In
 * reality, this class does not produce instances of ModelAction and
 * ExecutionAction, but String representations of the Java implementation of
 * those classes.
 * 
 * @see ConditionsGenerator
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ActionsGenerator {
	/**
	 * This method is used for creating a String representation of the
	 * definition of a Java class for <code>action</code>.
	 * <p>
	 * The output class extends {@link ModelAction}, and it is the conceptual
	 * representation of <code>action</code>.
	 * <p>
	 * The generated class contains two private fields for each parameter of
	 * <code>action</code>. Given a parameter with name <code>pName</code>, two
	 * class variables are created in the output class:
	 * <ul>
	 * <li> <code>pName</code>. The type of this variable will be a Java type
	 * compatible with that of the MMPM definition of <code>pName</code> in
	 * <code>action</code> (according to
	 * {@link Util#fromMMPMParameterType(gatech.mmpm.ActionParameterType)}).
	 * This variable represents the value of the parameter in case a value has
	 * been specified when constructing an instance of the output class.
	 * <li> <code>pNameLoc</code>. This is a String representing where the
	 * variable <code>pName</code> can be located in the context. In case a
	 * value is not specified for the variable, the task's context must be
	 * searched for a variable whose name is <code>pNameLoc</code>.
	 * </ul>
	 * 
	 * The
	 * {@link ModelTask#createExecutor(jbt.execution.core.BTExecutor, ExecutionTask)}
	 * method of the output class returns an instance of the corresponding
	 * {@link ExecutionAction}. This method assumes that the corresponding
	 * ExecutionTask's name is <code>action</code>'s name too, and that the
	 * class is located in the Java package
	 * <code>executionActionPackageName</code>. The corresponding
	 * ExecutionAction receives in its constructor all the private class
	 * variables of the output class, in the same order as they are declared in
	 * the output class. In a similar way,
	 * {@link #getExecutionActionClass(ParsedAction)} can be used to construct a
	 * String expression for <code>action</code>.
	 * <p>
	 * The first argument of the constructor of the output class is the guard.
	 * The rest of them are values for each private class variable of the output
	 * class, in the same order as they are declared.
	 * 
	 * @param action
	 *            the action whose representation as a ModelAction is going to
	 *            be created.
	 * @param executionActionPackageName
	 *            the name of the package that contains the ExecutionAction
	 *            associated to <code>action</code>. It must not be empty, and
	 *            it has to be of the form <i>level1.level2. ... .levelN</i>.
	 * @return a String representation of <code>action</code> as a ModelAction
	 *         class.
	 */
	public String getModelActionClass(ParsedAction action, String executionActionPackageName) {
		String result = new String();

		/* First element is the type, and the second element is the value. */
		List<Pair<Class, String>> params = new LinkedList<Pair<Class, String>>();

		/* Extract all the parameters. */
		for (ParsedActionParameter parameter : action.getParameters()) {
			params.add(new Pair<Class, String>(Util.fromMMPMParameterType(parameter.getType()),
					parameter.getName()));
		}

		result += "/** ModelAction class created from MMPM action " + action.getName() + ". */";

		/* Action class header. */
		result += getModelActionClassHeader(action) + "\n";

		/* Class variables. */
		result += CommonCodeGenerationUtilities.getClassVariables(params) + "\n\n";

		/* Constructor. */
		result += CommonCodeGenerationUtilities.getModelConstructor(action.getName(), params)
				+ "\n\n";

		/* "createExecutor()" method. */
		result += CommonCodeGenerationUtilities.getCreateExecutorMethod(action.getName(),
				executionActionPackageName, params) + "\n";

		result += "}";

		return result;
	}

	/**
	 * This method is used for creating a String representation of the
	 * definition of a Java class for <code>action</code>.
	 * <p>
	 * The output class extends {@link ExecutionAction}, and it defines how
	 * <code>action</code> actually works.
	 * <p>
	 * The generated class contains two private fields for each parameter of
	 * <code>action</code>. Given a parameter with name <code>pName</code>, two
	 * class variables are created in the output class:
	 * <ul>
	 * <li> <code>pName</code>. The type of this variable will be a Java type
	 * compatible with that of the MMPM definition of <code>pName</code> in
	 * <code>action</code> (according to
	 * {@link Util#fromMMPMParameterType(gatech.mmpm.ActionParameterType)}).
	 * This variable represents the value of the parameter in case a value has
	 * been specified when constructing an instance of the output class.
	 * <li> <code>pNameLoc</code>. This is a String representing where the
	 * variable <code>pName</code> can be located in the context. In case a
	 * value is not specified for the variable, the task's context must be
	 * searched for a variable whose name is <code>pNameLoc</code>.
	 * </ul>
	 * <p>
	 * For all of <code>action</code>'s parameters, a <i>getter</i> method is
	 * constructed. Given a parameter with name <code>pName</code>, a getter
	 * named <code>getPName()</code> is constructed. If at construction time a
	 * value was specified for <code>pName</code>, then <code>getPName()</code>
	 * returns such a value. Otherwise, <code>getPName()</code> will search for
	 * a variable of name <code>pNameLoc</code> in the context, and will return
	 * its value.
	 * <p>
	 * This method also constructs an empty skeleton for all abstract methods of
	 * ExecutionAction. The getter methods can be used in the implementation of
	 * those abstract methods in order to retrieve the action's parameters.
	 * <p>
	 * The first argument of the constructor of the output class is its
	 * corresponding ModelAction. The second one is its corresponding
	 * BTExecutor. The rest of them are values for each private class variable
	 * of the output class, in the same order as they are declared.
	 * 
	 * @param action
	 *            the action whose representation as an ExecutionAction is going
	 *            to be created.
	 * @return a String representation of <code>action</code> as an
	 *         ExecutionAction class.
	 */
	public String getExecutionActionClass(ParsedAction action, String modelActionPackageName) {
		String result = new String();

		/* First element is the type, and the second element is the value. */
		List<Pair<Class, String>> params = new LinkedList<Pair<Class, String>>();

		for (ParsedActionParameter parameter : action.getParameters()) {
			params.add(new Pair<Class, String>(Util.fromMMPMParameterType(parameter.getType()),
					parameter.getName()));
		}

		result += "/** ExecutionAction class created from MMPM action " + action.getName() + ". */";

		/* Action class header. */
		result += getExecutionActionClassHeader(action) + "\n";

		/* Class variables. */
		result += CommonCodeGenerationUtilities.getClassVariables(params) + "\n\n";

		/* Constructor. */
		result += CommonCodeGenerationUtilities.getExecutionConstructor(action.getName(),
				modelActionPackageName, params) + "\n\n";

		/* Getters. */
		result += CommonCodeGenerationUtilities.getGetters(params) + "\n\n";

		/* Abstract methods. */
		result += CommonCodeGenerationUtilities.getAbstractMethods();

		result += "}";

		return result;
	}

	private String getModelActionClassHeader(ParsedAction action) {
		return "public class " + action.getName() + " extends "
				+ ModelAction.class.getCanonicalName() + "{";
	}

	private String getExecutionActionClassHeader(ParsedAction action) {
		return "public class " + action.getName() + " extends "
				+ ExecutionAction.class.getCanonicalName() + "{";
	}
}
