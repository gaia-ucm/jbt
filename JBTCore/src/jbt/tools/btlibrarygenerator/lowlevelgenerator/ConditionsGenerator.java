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
import jbt.execution.task.leaf.condition.ExecutionCondition;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.condition.ModelCondition;
import jbt.tools.btlibrarygenerator.util.Util;
import jbt.util.Pair;
import gatech.mmpm.tools.parseddomain.ParsedActionParameter;
import gatech.mmpm.tools.parseddomain.ParsedMethod;

/**
 * Class used for generating Java-BT expressions for MMPM sensors (represented
 * as {@link ParsedMethod} objects).
 * <p>
 * Given a ParsedMethod, this class can be used for generating a
 * {@link ModelCondition} or an {@link ExecutionCondition} for such a sensor. In
 * reality, this class does not produce instances of ModelCondition and
 * ExecutionCondition, but String representations of the Java implementation of
 * those classes.
 * 
 * @see ActionsGenerator
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ConditionsGenerator {
	/**
	 * This method is used for creating a String representation of the
	 * definition of a Java class for <code>condition</code>.
	 * <p>
	 * The output class extends {@link ModelCondition}, and it is the conceptual
	 * representation of <code>condition</code>.
	 * <p>
	 * The generated class contains two private fields for each parameter of
	 * <code>condition</code>. Given a parameter with name <code>pName</code>,
	 * two class variables are created in the output class:
	 * <ul>
	 * <li> <code>pName</code>. The type of this variable will be a Java type
	 * compatible with that of the MMPM definition of <code>pName</code> in
	 * <code>condition</code> (according to
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
	 * {@link ExecutionCondition}. This method assumes that the corresponding
	 * ExecutionCondition's name is <code>condition</code>'s name too, and that
	 * the class is located in the Java package
	 * <code>executionConditionPackageName</code>. The corresponding
	 * ExecutionCondition receives in its constructor all the private class
	 * variables of the output class, in the same order as they are declared in
	 * the output class. In a similar way,
	 * {@link #getExecutionConditionClass(ParsedMethod)} can be used to
	 * construct a String expression for <code>condition</code>.
	 * <p>
	 * The first argument of the constructor of the output class is the guard.
	 * The rest of them are values for each private class variable of the output
	 * class, in the same order as they are declared.
	 * 
	 * @param condition
	 *            the condition whose representation as a ModelCondition is
	 *            going to be created.
	 * @param executionConditionPackageName
	 *            the name of the package that contains the ExecutionCondition
	 *            associated to <code>condition</code>. It must not be empty,
	 *            and it has to be of the form <i>level1.level2. ...
	 *            .levelN</i>.
	 * @return a String representation of <code>condition</code> as a
	 *         ModelCondition class.
	 */
	public String getModelConditionClass(ParsedMethod condition,
			String executionConditionPackageName) {
		String result = new String();

		/* First element is the type, and the second element is the value. */
		List<Pair<Class, String>> params = new LinkedList<Pair<Class, String>>();

		for (ParsedActionParameter parameter : condition.getParameters()) {
			params.add(new Pair<Class, String>(Util.fromMMPMParameterType(parameter.getType()),
					parameter.getName()));
		}

		result += "/** ModelCondition class created from MMPM condition " + condition.getName()
				+ ". */";

		/* Class header. */
		result += getModelConditionClassHeader(condition) + "\n";

		/* Class variables. */
		result += CommonCodeGenerationUtilities.getClassVariables(params) + "\n\n";

		/* Constructor. */
		result += CommonCodeGenerationUtilities.getModelConstructor(condition.getName(), params)
				+ "\n\n";

		/* "createExecutor()" method. */
		result += CommonCodeGenerationUtilities.getCreateExecutorMethod(condition.getName(),
				executionConditionPackageName, params) + "\n";

		result += "}";

		return result;
	}

	/**
	 * This method is used for creating a String representation of the
	 * definition of a Java class for <code>condition</code>.
	 * <p>
	 * The output class extends {@link ExecutionCondition}, and it defines how
	 * <code>condition</code> actually works.
	 * <p>
	 * The generated class contains two private fields for each parameter of
	 * <code>condition</code>. Given a parameter with name <code>pName</code>,
	 * two class variables are created in the output class:
	 * <ul>
	 * <li> <code>pName</code>. The type of this variable will be a Java type
	 * compatible with that of the MMPM definition of <code>pName</code> in
	 * <code>condition</code> (according to
	 * {@link Util#fromMMPMParameterType(gatech.mmpm.ActionParameterType)}).
	 * This variable represents the value of the parameter in case a value has
	 * been specified when constructing an instance of the output class.
	 * <li> <code>pNameLoc</code>. This is a String representing where the
	 * variable <code>pName</code> can be located in the context. In case a
	 * value is not specified for the variable, the task's context must be
	 * searched for a variable whose name is <code>pNameLoc</code>.
	 * </ul>
	 * <p>
	 * For all of <code>condition</code>'s parameters, a <i>getter</i> method is
	 * constructed. Given a parameter with name <code>pName</code>, a getter
	 * named <code>getPName()</code> is constructed. If at construction time a
	 * value was specified for <code>pName</code>, then <code>getPName()</code>
	 * returns such a value. Otherwise, <code>getPName()</code> will search for
	 * a variable of name <code>pNameLoc</code> in the context, and will return
	 * its value.
	 * <p>
	 * This method also constructs an empty skeleton for all abstract methods of
	 * ExecutionCondition. The getter methods can be used in the implementation
	 * of those abstract methods in order to retrieve the condition's
	 * parameters.
	 * <p>
	 * The first argument of the constructor of the output class is its
	 * corresponding ModelContion. The second one is its corresponding
	 * BTExecutor. The rest of them are values for each private class variable
	 * of the output class, in the same order as they are declared.
	 * 
	 * @param condition
	 *            the condition whose representation as an ExecutionCondition is
	 *            going to be created.
	 * @return a String representation of <code>action</code> as an
	 *         ExecutionCondition class.
	 */
	public String getExecutionConditionClass(ParsedMethod condition,
			String modelConditionPackageName) {
		String result = new String();

		/* First element is the type, and the second element is the value. */
		List<Pair<Class, String>> params = new LinkedList<Pair<Class, String>>();

		for (ParsedActionParameter parameter : condition.getParameters()) {
			params.add(new Pair<Class, String>(Util.fromMMPMParameterType(parameter.getType()),
					parameter.getName()));
		}

		result += "/** ExecutionCondition class created from MMPM condition " + condition.getName()
				+ ". */";

		/* Class header. */
		result += getExecutionConditionClassHeader(condition) + "\n";

		/* Class variables. */
		result += CommonCodeGenerationUtilities.getClassVariables(params) + "\n\n";

		/* Constructor. */
		result += CommonCodeGenerationUtilities.getExecutionConstructor(condition.getName(),
				modelConditionPackageName, params) + "\n\n";

		/* Getters. */
		result += CommonCodeGenerationUtilities.getGetters(params) + "\n\n";

		/* Abstract methods. */
		result += CommonCodeGenerationUtilities.getAbstractMethods();

		result += "}";

		return result;
	}

	private String getModelConditionClassHeader(ParsedMethod condition) {
		return "public class " + condition.getName() + " extends "
				+ ModelCondition.class.getCanonicalName() + "{";
	}

	private String getExecutionConditionClassHeader(ParsedMethod condition) {
		return "public class " + condition.getName() + " extends "
				+ ExecutionCondition.class.getCanonicalName() + "{";
	}
}
