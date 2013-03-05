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

import java.util.List;

import jbt.execution.core.BTExecutor;
import jbt.execution.core.ExecutionTask;
import jbt.execution.core.BTExecutor.BTExecutorList;
import jbt.execution.core.ExecutionTask.Status;
import jbt.execution.core.ITaskState;
import jbt.execution.task.leaf.action.ExecutionAction;
import jbt.execution.task.leaf.condition.ExecutionCondition;
import jbt.model.core.ModelTask;
import jbt.model.task.leaf.action.ModelAction;
import jbt.model.task.leaf.condition.ModelCondition;
import jbt.util.Pair;

/**
 * Some helper functions used for easily generating String representations of {@link ModelAction}s,
 * {@link ModelCondition}s, {@link ExecutionAction}s and {@link ExecutionCondition}s from MMPM
 * actions and sensors.
 * <p>
 * {@link ActionsGenerator} and {@link ConditionsGenerator} share most of their functionality, so
 * this class just gathers all of the common parts when they are generating classes.
 * 
 * @see ActionsGenerator
 * @see ConditionsGenerator
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class CommonCodeGenerationUtilities {
	/** Message for a "to do". */
	static final String TODO_MESSAGE = "/* TODO: this method's implementation must be completed. */";
	/**
	 * Suffix that is placed at the end of the class's variables to represent the variable that will
	 * store the location in the context of the original variable.
	 */
	static final String PARAM_LOCATION_SUFFIX = "Loc";

	/**
	 * Given a list of class "parameters", this method returns a String representation of the
	 * declaration of those parameters as private members of a class.
	 * <p>
	 * <code>params</code> contains all the parameters. Each Pair represents a parameter, being the
	 * first element its class and the second element its name.
	 * <p>
	 * The returned representation is tabulated one unit.
	 * 
	 * @param params
	 *            the set of parameters from which a String representation is created.
	 * @return a String representation of the declaration of the parameters in <code>params</code>.
	 */
	static String getClassVariables(List<Pair<Class, String>> params) {
		String result = new String();

		for (Pair<Class, String> currentParam : params) {
			result += "/**Value of the parameter \"" + currentParam.getSecond()
					+ "\" in case its value is specified at construction time. null otherwise.*/";
			result += "private " + currentParam.getFirst().getCanonicalName() + " "
					+ currentParam.getSecond() + ";" + "\n";
			result += "/**Location, in the context, of the parameter \""
					+ currentParam.getSecond()
					+ "\" in case its value is not specified at construction time. null otherwise.*/";
			result += "private " + String.class.getCanonicalName() + " " + currentParam.getSecond()
					+ PARAM_LOCATION_SUFFIX + ";" + "\n";
		}

		if (params.size() != 0) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	/**
	 * Creates a String expression for the constructor of a ModelAction or ModelCondition. The name
	 * of the class is <code>modelClassName</code>, and <code>params</code> is a list of parameters
	 * (the first element of each pair is the class of the parameter and the second element is its
	 * name).
	 * <p>
	 * For each parameter of name <code>pName</code> in <code>params</code>, the constructor will
	 * receive two parameters, one of name <code>pName</code>, whose type is that specified in the
	 * list of parameters, and another one of name <code>pNameLoc</code>, whose type is String, and
	 * which represents the place in the context where <code>pName</code> must be looked for in case
	 * a value is not specified for it.
	 * <p>
	 * The constructor assigns the value of every input argument to its corresponding class
	 * variable.
	 * 
	 * @param modelClassName
	 *            the name of the ModelAction or ModelCondition class whose constructor is going to
	 *            be created.
	 * @param params
	 *            the list of parameters to be used when building the constructor.
	 * @return a String expression for the constructor, as described above.
	 */
	static String getModelConstructor(String modelClassName, List<Pair<Class, String>> params) {
		String result = new String();

		result += "/** Constructor. Constructs an instance of " + modelClassName + ".\n";

		for (Pair<Class, String> currentParam : params) {
			result += "@param " + currentParam.getSecond() + " value of the parameter \""
					+ currentParam.getSecond()
					+ "\", or null in case it should be read from the context. If null, <code>"
					+ currentParam.getSecond() + PARAM_LOCATION_SUFFIX
					+ "</code> cannot be null.\n";

			result += "@param "
					+ currentParam.getSecond()
					+ PARAM_LOCATION_SUFFIX
					+ " in case <code>"
					+ currentParam.getSecond()
					+ "</code> is null, this variable represents the place in the context where the parameter's value will be retrieved from.\n";
		}

		result += "*/";

		result += "public " + modelClassName + "(";

		String stringParams = new String();

		stringParams += ModelTask.class.getCanonicalName() + " guard, ";

		if (params.size() != 0) {
			for (Pair<Class, String> currentParam : params) {
				stringParams += currentParam.getFirst().getCanonicalName() + " "
						+ currentParam.getSecond() + ", ";
				stringParams += String.class.getCanonicalName() + " " + currentParam.getSecond()
						+ PARAM_LOCATION_SUFFIX + ", ";
			}
		}

		stringParams = stringParams.substring(0, stringParams.length() - 2);

		result += stringParams + "){\n";

		result += "super(guard);\n";

		for (Pair<Class, String> currentParam : params) {
			result += "this." + currentParam.getSecond() + " = " + currentParam.getSecond() + ";\n";
			result += "this." + currentParam.getSecond() + PARAM_LOCATION_SUFFIX + " = "
					+ currentParam.getSecond() + PARAM_LOCATION_SUFFIX + ";\n";
		}

		result += "}";

		return result;
	}

	/**
	 * Creates a String expression for the constructor of a ExecutionAction or ExecutionCondition.
	 * The name of the class is <code>executionClassName</code>, and <code>params</code> is a list
	 * of parameters (the first element of each pair is the class of the parameter and the second
	 * element is its name).
	 * <p>
	 * For each parameter of name <code>pName</code> in <code>params</code>, the constructor will
	 * receive two parameters, one of name <code>pName</code>, whose type is that specified in the
	 * list of parameters, and another one of name <code>pNameLoc</code>, whose type is String, and
	 * which represents the place in the context where <code>pName</code> must be looked for in case
	 * a value is not specified for it.
	 * <p>
	 * The constructor assigns the value of every input argument to its corresponding class
	 * variable.
	 * <p>
	 * <code>modelClassPackageName</code> is the name of the package that contains the corresponding
	 * model class (the name of the corresponding model class is assumed to be
	 * <code>executionClassName</code>.
	 * 
	 * @param executionClassName
	 *            the name of the ModelAction or ModelCondition class whose constructor is going to
	 *            be created.
	 * @param params
	 *            the list of parameters to be used when building the constructor.
	 * @param modelClassPackageName
	 *            the name of the package that contains the corresponding model class.
	 * @return a String expression for the constructor, as described above.
	 */
	static String getExecutionConstructor(String executionClassName, String modelClassPackageName,
			List<Pair<Class, String>> params) {
		String result = new String();

		result += "/** Constructor. Constructs an instance of " + executionClassName
				+ " that is able to run a " + modelClassPackageName + "." + executionClassName
				+ ".\n";

		for (Pair<Class, String> currentParam : params) {
			result += "@param " + currentParam.getSecond() + " value of the parameter \""
					+ currentParam.getSecond()
					+ "\", or null in case it should be read from the context. If null, <code>"
					+ currentParam.getSecond() + PARAM_LOCATION_SUFFIX + "<code> cannot be null.\n";

			result += "@param "
					+ currentParam.getSecond()
					+ PARAM_LOCATION_SUFFIX
					+ " in case <code>"
					+ currentParam.getSecond()
					+ "</code> is null, this variable represents the place in the context where the parameter's value will be retrieved from.\n";
		}

		result += "*/";

		result += "public " + executionClassName + "(";

		String stringParams = new String();

		stringParams += modelClassPackageName + "." + executionClassName + " modelTask, "
				+ BTExecutor.class.getCanonicalName() + " executor, "
				+ ExecutionTask.class.getCanonicalName() + " parent, ";

		if (params.size() != 0) {
			for (Pair<Class, String> currentParam : params) {
				stringParams += currentParam.getFirst().getCanonicalName() + " "
						+ currentParam.getSecond() + ", ";
				stringParams += String.class.getCanonicalName() + " " + currentParam.getSecond()
						+ PARAM_LOCATION_SUFFIX + ", ";
			}
		}

		stringParams = stringParams.substring(0, stringParams.length() - 2);

		result += stringParams + "){\n";

		result += "super(modelTask, executor, parent);\n\n";

		if (params.size() != 0) {
			result += "\n\n";
			for (Pair<Class, String> currentParam : params) {
				result += "this." + currentParam.getSecond() + " = " + currentParam.getSecond()
						+ ";\n";
				result += "this." + currentParam.getSecond() + PARAM_LOCATION_SUFFIX + " = "
						+ currentParam.getSecond() + PARAM_LOCATION_SUFFIX + ";\n";
			}
		}

		result += "}";

		return result;
	}

	/**
	 * This method creates a String representing the definition of the
	 * {@link ModelTask#createExecutor(BTExecutor, ExecutionTask)} method of a class.
	 * <p>
	 * The method will return an instance of an ExecutionTask whose name is
	 * <code>executionClassName</code> and placed in the package
	 * <code>executionActionPackageName</code>.
	 * <p>
	 * <code>params</code> represents the list of parameters that the ExecutionTask will receive in
	 * its constructor. The first argument that is passed to the ExecutionTask's constructor is
	 * <code>this</code>; the second argument is <code>executor</code> (<code>executor</code>
	 * represents the {@link BTExecutor} that is passed to the <code>createExecutor()</code> method.
	 * The rest of the parameters that are passed to the constructor are those in
	 * <code>params</code>. Each Pair in <code>params</code> represents a parameter, being the first
	 * element its class and the second element its name. For each parameter in <code>params</code>,
	 * the constructor receives two parameters, the parameter itself (for instance
	 * <code>pName</code>) , and the location of the parameter in the context (<code>pNameLoc</code>
	 * ).
	 * 
	 * @param executionClassName
	 *            the name of the ExecutionTask that the method will return.
	 * @param params
	 *            the list of parameters that the ExecutionTask <code>executionClassName</code> will
	 *            receive in its constructor, apart from <code>this</code> and <code>executor</code>
	 *            .
	 * @param executionClassPackageName
	 *            the name of the Java package that contains the class
	 *            <code>executionClassName</code>.
	 * @return a String representation for the <code>createExecutor()</code> method as described
	 *         above.
	 */
	static String getCreateExecutorMethod(String executionClassName,
			String executionClassPackageName, List<Pair<Class, String>> params) {
		/*
		 * TODO: this method could be improved, since only the names of the parameters are required.
		 */
		String result = new String();

		result += "/** Returns a " + executionClassPackageName + "." + executionClassName
				+ " task that is able to run this task. */";

		result += "public " + ExecutionTask.class.getCanonicalName() + " createExecutor("
				+ BTExecutor.class.getCanonicalName() + " executor, "
				+ ExecutionTask.class.getCanonicalName() + " parent" + "){\n";

		String returnStatement = "return new " + executionClassPackageName + "."
				+ executionClassName + "(";

		returnStatement += "this, executor, parent, ";

		if (params.size() != 0) {
			for (Pair<Class, String> currentParam : params) {
				returnStatement += "this." + currentParam.getSecond() + ", ";
				returnStatement += "this." + currentParam.getSecond() + PARAM_LOCATION_SUFFIX
						+ ", ";
			}
		}

		returnStatement = returnStatement.substring(0, returnStatement.length() - 2);

		returnStatement += ");\n";

		result += returnStatement;

		result += "}";

		return result;
	}

	/**
	 * Given a list of parameters, this method creates an expression declaring a public getter
	 * method for each parameter. These getters must be used by a class extending
	 * {@link ExecutionTask}, since the implementation of such getters make use of the context of
	 * the task.
	 * <p>
	 * <code>params</code> contains all the parameters. Each Pair represents a parameter, being the
	 * first element its class and the second element its name.
	 * <p>
	 * Given a parameter of name <code>pName</code>, the getter function has as a name
	 * <code>getPName()</code>. If the variable to get is not null, the getter method returns the
	 * variable. Otherwise, it searches for the variable in the context by using the class private
	 * variable <code>getPNameLoc</code>.
	 * 
	 * @param params
	 *            the set of parameters from which getters are going to be obtained.
	 * @return a String representation of all the getters for the parameters in <code>params</code.
	 */
	static String getGetters(List<Pair<Class, String>> params) {
		String result = new String();

		for (Pair<Class, String> currentParam : params) {
			String currentGetter = getGetter(currentParam);
			result += currentGetter + "\n\n";
		}

		if (params.size() != 0) {
			result = result.substring(0, result.length() - 2);
		}

		return result;
	}

	/**
	 * Given a parameter, this method returns a getter method for it. This getter must be used by a
	 * class extending {@link ExecutionTask}, since the implementation of such getter make use of
	 * the context of the task.
	 * <p>
	 * <code>param</code> represents the parameter, being the first element its class and the second
	 * element its name.
	 * <p>
	 * Given a parameter of name <code>pName</code>, the getter function has as a name
	 * <code>getPName()</code>. If the variable to get is not null, the getter method returns the
	 * variable. Otherwise, it searches for the variable in the context by using the class private
	 * variable <code>getPNameLoc</code>.
	 * 
	 * @param param
	 *            the parameter from which the getter method is obtained.
	 * @return a String representation for the parameter <code>param</code>.
	 */
	static String getGetter(Pair<Class, String> param) {
		String result = new String();

		result += "/** Returns the value of the parameter \""
				+ param.getSecond()
				+ "\", or null in case it has not been specified or it cannot be found in the context. */";

		result += "public " + param.getFirst().getCanonicalName() + " get"
				+ Character.toUpperCase(param.getSecond().charAt(0))
				+ param.getSecond().substring(1) + "(){\n";
		result += "if(this." + param.getSecond() + " != null){\n";
		result += "return this." + param.getSecond() + ";\n";
		result += "}\n";
		result += "else{\n";
		result += "return (" + param.getFirst().getCanonicalName() + ")"
				+ "this.getContext().getVariable(" + "this." + param.getSecond()
				+ PARAM_LOCATION_SUFFIX + ");\n";
		result += "}\n";
		result += "}";

		return result;
	}

	/**
	 * Returns a String representation of the declaration of the abstract methods of the
	 * {@link ExecutionTask} class. The String is tabulated one unit.
	 * 
	 * @return a String representation of the declaration of the abstract methods of the
	 *         {@link ExecutionTask} class.
	 */
	static String getAbstractMethods() {
		String result = new String();

		result += "protected void internalSpawn(){\n";
		result += "/* Do not remove this first line unless you know what it does and you need not do it. */\n";
		result += "this.getExecutor().requestInsertionIntoList("
				+ BTExecutorList.class.getCanonicalName() + "."
				+ BTExecutorList.TICKABLE.toString() + ",this);\n";
		result += TODO_MESSAGE + "\n";
		result += "System.out.println(this.getClass().getCanonicalName() +" + "\" spawned\");";
		result += "}\n\n";

		result += "protected " + Status.class.getCanonicalName() + " internalTick(){\n";
		result += "/* TODO: this method's implementation must be completed. This function should only return Status.SUCCESS, Status.FAILURE or Status.RUNNING. No other values are allowed. */"
				+ "\n";
		result += "return " + Status.class.getCanonicalName() + "." + Status.SUCCESS.toString()
				+ ";\n";
		result += "}\n\n";

		result += "protected void internalTerminate(){\n";
		result += TODO_MESSAGE + "\n";
		result += "}\n\n";

		result += "protected void restoreState(" + ITaskState.class.getCanonicalName()
				+ " state){\n";
		result += TODO_MESSAGE + "\n";
		result += "}\n\n";

		result += "protected " + ITaskState.class.getCanonicalName() + " storeState(){\n";
		result += TODO_MESSAGE + "\n";
		result += "return null;";
		result += "}\n";

		result += "protected " + ITaskState.class.getCanonicalName()
				+ " storeTerminationState(){\n";
		result += TODO_MESSAGE + "\n";
		result += "return null;";
		result += "}\n";

		return result;
	}
}
