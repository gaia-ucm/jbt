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
package jbt.tools.btlibrarygenerator;

import gatech.mmpm.ActionParameterType;
import gatech.mmpm.tools.parseddomain.ParseException;
import gatech.mmpm.tools.parseddomain.ParsedAction;
import gatech.mmpm.tools.parseddomain.ParsedDomain;
import gatech.mmpm.tools.parseddomain.ParsedMethod;

import jargs.gnu.CmdLineParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jbt.execution.task.leaf.action.ExecutionAction;
import jbt.model.task.leaf.action.ModelAction;
import jbt.model.task.leaf.condition.ModelCondition;
import jbt.tools.btlibrarygenerator.lowlevelgenerator.ActionsGenerator;
import jbt.tools.btlibrarygenerator.lowlevelgenerator.ConditionsGenerator;
import jbt.tools.btlibrarygenerator.util.Util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * ActionsAndConditionsGenerator is an application that generates the skeleton
 * for low level actions and conditions specified in one or several MMPM domain
 * files.
 * <p>
 * For every MMPM action, two classes are created: one extending
 * {@link ModelAction}, which conceptually represents the action, and another
 * one extending {@link ExecutionAction}, which represents how the action
 * actually works -whose abstract methods must be completed in order for the
 * action to perform any task at all-.
 * <p>
 * Also, for every MMPM boolean sensor, two classes are created: one extending
 * {@link ModelCondition}, which conceptually represents the condition (sensor),
 * and another one extending {@link ExecutionCondition}, which represents how
 * the condition actually works -whose abstract methods must be completed in
 * order for the condition to perform any task at all-.
 * <p>
 * Created execution classes define getter methods for each MMPM parameter.
 * <p>
 * The syntax of this program is as follows:
 * 
 * <pre>
 * ActionsAndConditionsGenerator -c configurationFile [-r relativePath] [-o]
 * </pre>
 * 
 * Where <i>configurationFile</i> is an XML file that contains all the
 * information required to run this application. The syntax of such a file is:
 * 
 * <pre>
 * &lt;Configuration&gt;
 * 	
 *  &lt;DomainFile&gt;MMPMDomainFile1&lt;/DomainFile&gt;
 *  &lt;DomainFile&gt;MMPMDomainFile2&lt;/DomainFile&gt;
 *  ...
 *  &lt;DomainFile&gt;MMPMDomainFileN&lt;/DomainFile&gt;
 *  
 *  &lt;ModelActionsPackage&gt;Name of the package for generated model action classes&lt;/ModelActionsPackage&gt;
 *  
 *  &lt;ModelConditionsPackage&gt;Name of the package for generated model condition classes&lt;/ModelConditionsPackage&gt;
 *  
 *  &lt;ModelActionsOutputDirectory&gt;Name of the directory where model actions are created&lt;/ModelActionsOutputDirectory&gt;
 *  
 *  &lt;ModelConditionsOutputDirectory&gt;Name of the directory where model conditions are created&lt;/ModelConditionsOutputDirectory&gt;
 *  
 *  &lt;ExecutionActionsPackage&gt;Name of the package for generated execution action classes&lt;/ExecutionActionsPackage&gt;
 *  
 *  &lt;ExecutionConditionsPackage&gt;Name of the package for generated execution condition classes&lt;/ExecutionConditionsPackage&gt;
 *  
 *  &lt;ExecutionActionsOutputDirectory&gt;Name of the directory where execution actions are created&lt;/ExecutionActionsOutputDirectory&gt;
 *  
 *  &lt;ExecutionConditionsOutputDirectory&gt;Name of the directory where execution conditions are created&lt;/ExecutionConditionsOutputDirectory&gt;
 *  
 * &lt;/Configuration&gt;
 * </pre>
 * 
 * The order in which the elements are specified is not relevant. If the input
 * files do contain only actions, parameters related to conditions may not be
 * specified, and vice versa.
 * <p>
 * The -r option is used to add a path to the beginning of the files listed in
 * the configuration file; as a result, each file is considered to be placed at
 * the path specified in the -r option. The -r option may not be specified, in
 * which case the files are considered to be at the current execution directory.
 * <p>
 * The -o option (standing for <i>overwrite</i>) is either is specified or not.
 * If it is not specified, generated output files will not overwrite any
 * existing file in the file system, and as a result, the corresponding class
 * file will not be produced in case there is a file with the same name in the
 * file system. If the option -o is specified, then generated output files will
 * overwrite any file in the file system whose name matches.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ActionsAndConditionsGenerator {
	/** Tag of the XML configuration file. */
	private static final String CONFIGURATION_TAG = "Configuration";
	/** Tag of the XML configuration file. */
	private static final String DOMAIN_FILE_TAG = "DomainFile";
	/** Tag of the XML configuration file. */
	private static final String MODEL_ACTIONS_PACKAGE_TAG = "ModelActionsPackage";
	/** Tag of the XML configuration file. */
	private static final String MODEL_CONDITIONS_PACKAGE_TAG = "ModelConditionsPackage";
	/** Tag of the XML configuration file. */
	private static final String MODEL_ACTIONS_OUTPUT_DIRECTORY_TAG = "ModelActionsOutputDirectory";
	/** Tag of the XML configuration file. */
	private static final String MODEL_CONDITIONS_OUTPUT_DIRECTORY_TAG = "ModelConditionsOutputDirectory";
	/** Tag of the XML configuration file. */
	private static final String EXECUTION_ACTIONS_PACKAGE_TAG = "ExecutionActionsPackage";
	/** Tag of the XML configuration file. */
	private static final String EXECUTION_CONDITIONS_PACKAGE_TAG = "ExecutionConditionsPackage";
	/** Tag of the XML configuration file. */
	private static final String EXECUTION_ACTIONS_OUTPUT_DIRECTORY_TAG = "ExecutionActionsOutputDirectory";
	/** Tag of the XML configuration file. */
	private static final String EXECUTION_CONDITIONS_OUTPUT_DIRECTORY_TAG = "ExecutionConditionsOutputDirectory";

	public static void main(String[] args) {
		/* Parse command line arguments. */
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option configurationFileOption = parser.addStringOption('c', "config");
		CmdLineParser.Option relativePathOption = parser.addStringOption('r', "relativepath");
		CmdLineParser.Option overwriteOption = parser.addBooleanOption('o', "overwrite");

		try {
			parser.parse(args);
		}
		catch (Exception e) {
			printUsage();
			System.exit(1);
		}

		try {
			/* Relative path read from the command line. */
			String relativePath = (String) parser.getOptionValue(relativePathOption);

			if (relativePath == null) {
				relativePath = "";
			}
			else {
				relativePath = Util.addDirectorySeparator(relativePath);
			}

			/* Overwrite option. */
			Boolean overwrite = (Boolean) parser.getOptionValue(overwriteOption, Boolean.FALSE);

			/* Open the configuration file and read file names. */
			String configurationFileName;

			configurationFileName = (String) parser.getOptionValue(configurationFileOption);

			if (configurationFileName == null) {
				printUsage();
				System.exit(1);
			}

			FileInputStream file = new FileInputStream(configurationFileName);

			SAXBuilder confFileBuilder = new SAXBuilder();
			Document confFileDoc = confFileBuilder.build(file);
			file.close();

			List<String> fileNames = new LinkedList<String>();
			Element root = confFileDoc.getRootElement();
			List<Element> childrenElements = root.getChildren(DOMAIN_FILE_TAG);

			for (Element e : childrenElements) {
				fileNames.add(e.getText());
			}

			/*
			 * First, we process all MMPM domain files and extract a global list
			 * of actions and conditions.
			 */
			List<ParsedAction> actions = new LinkedList<ParsedAction>();
			List<ParsedMethod> conditions = new LinkedList<ParsedMethod>();

			for (String currentFileName : fileNames) {
				try {
					currentFileName = relativePath + currentFileName;

					System.out.println("Reading and parsing " + currentFileName + "...");
					FileInputStream currentFile = new FileInputStream(currentFileName);

					SAXBuilder mmpmFileBuilder = new SAXBuilder();
					Document mmpmDoc = mmpmFileBuilder.build(currentFile);
					currentFile.close();
					ParsedDomain domain = new ParsedDomain();

					domain.init(mmpmDoc.getRootElement(), null);

					actions.addAll(domain.getActionSet().getAction());
					for (ParsedMethod method : domain.getSensorSet().getMethods()) {
						if (method.getReturnedType() == ActionParameterType.BOOLEAN) {
							conditions.add(method);
						}
					}
				}
				catch (IOException e) {
					System.out.println("Could not open file: " + currentFileName + ": "
							+ e.getMessage());
				}
				catch (ParseException e) {
					System.out.println("There were errors while parsing the MMPM domain file "
							+ currentFileName + ": " + e.getMessage());
				}
			}

			/* Now create all the action classes. */
			if (actions.size() != 0) {
				try {
					/*
					 * Get output directories and packages from the XML
					 * configuration file.
					 */
					String modelActionsPackage = root.getChildText(MODEL_ACTIONS_PACKAGE_TAG);
					String modelActionsOutputDirectory = relativePath
							+ root.getChildText(MODEL_ACTIONS_OUTPUT_DIRECTORY_TAG);

					/* If directory ends in separator, remove it. */
					modelActionsOutputDirectory = Util
							.removeDirectorySeparator(modelActionsOutputDirectory);

					String executionActionsPackage = root
							.getChildText(EXECUTION_ACTIONS_PACKAGE_TAG);
					String executionActionsOutputDirectory = relativePath
							+ root.getChildText(EXECUTION_ACTIONS_OUTPUT_DIRECTORY_TAG);

					/* If directory ends in separator, remove it. */
					executionActionsOutputDirectory = Util
							.removeDirectorySeparator(executionActionsOutputDirectory);

					File modelActionsDirectory = new File(modelActionsOutputDirectory);

					/* Create output directories. */
					if (!modelActionsDirectory.isDirectory()) {
						System.out.println("Creating output directory: "
								+ modelActionsOutputDirectory + "...");
						modelActionsDirectory.mkdirs();
					}

					File executionActionsDirectory = new File(executionActionsOutputDirectory);

					if (!executionActionsDirectory.isDirectory()) {
						System.out.println("Creating output directory: "
								+ executionActionsOutputDirectory + "...");
						executionActionsDirectory.mkdirs();
					}

					/* Create action classes. */
					ActionsGenerator actionGenerator = new ActionsGenerator();

					for (ParsedAction currentAction : actions) {
						try {
							/* Create model action. */
							String modelOutputFileName = modelActionsOutputDirectory
									+ File.separator + currentAction.getName() + ".java";

							boolean skipModelAction = false;

							if (Util.fileExists(modelOutputFileName)) {
								System.out.print(modelOutputFileName + " already exists. ");

								if (overwrite) {
									System.out.println("It will be overwritten.");
								}
								else {
									System.out.println(modelOutputFileName
											+ " will not be generated.");
									skipModelAction = true;
								}
							}

							if (!skipModelAction) {
								System.out.println("Creating model action class: "
										+ modelOutputFileName + "...");
								String modelActionFileContent = new String();
								modelActionFileContent += getModelClassesFileHeader();
								modelActionFileContent += "package " + modelActionsPackage
										+ ";\n\n";

								modelActionFileContent += actionGenerator.getModelActionClass(
										currentAction, executionActionsPackage);

								BufferedWriter modelOutputFile = new BufferedWriter(new FileWriter(
										modelOutputFileName));

								modelOutputFile.write(Util.format(modelActionFileContent));
								modelOutputFile.close();
							}

							/* Create execution action. */
							String executionOutputFileName = executionActionsOutputDirectory
									+ File.separator + currentAction.getName() + ".java";

							boolean skipExecutionAction = false;

							if (Util.fileExists(executionOutputFileName)) {
								System.out.print(executionOutputFileName + " already exists. ");

								if (overwrite) {
									System.out.println("It will be overwritten.");
								}
								else {
									System.out.println(executionOutputFileName
											+ " will not be generated.");
									skipExecutionAction = true;
								}
							}

							if (!skipExecutionAction) {
								System.out.println("Creating execution action class: "
										+ executionOutputFileName + "...");
								BufferedWriter executionOutputFile = new BufferedWriter(
										new FileWriter(executionOutputFileName));
								String executionActionFileContent = new String();

								executionActionFileContent += getExecutionClassesFileHeader();

								executionActionFileContent += "package " + executionActionsPackage
										+ ";\n\n";

								executionActionFileContent += actionGenerator
										.getExecutionActionClass(currentAction, modelActionsPackage);

								executionOutputFile.write(Util.format(executionActionFileContent));
								executionOutputFile.close();
							}
						}
						catch (IOException e) {
							System.out
									.println("There were errors while creating classes for the MMPM action "
											+ currentAction.getName() + ": " + e.getMessage());
						}
					}
				}
				catch (Exception e) {
					System.out
							.println("There was an unexpected error while creating actions. Actions generation aborted");
					e.printStackTrace();
				}
			}

			/* Now create all the condition classes. */
			if (conditions.size() != 0) {
				try {
					/*
					 * Get output directories and packages from the XML
					 * configuration file.
					 */
					String modelConditionsPackage = root.getChildText(MODEL_CONDITIONS_PACKAGE_TAG);
					String modelConditionsOutputDirectory = relativePath
							+ root.getChildText(MODEL_CONDITIONS_OUTPUT_DIRECTORY_TAG);

					/* If directory ends in separator, remove it. */
					modelConditionsOutputDirectory = Util
							.removeDirectorySeparator(modelConditionsOutputDirectory);

					String executionConditionsPackage = root
							.getChildText(EXECUTION_CONDITIONS_PACKAGE_TAG);
					String executionConditionsOutputDirectory = relativePath
							+ root.getChildText(EXECUTION_CONDITIONS_OUTPUT_DIRECTORY_TAG);

					/* Create ouput directories. */
					File modelConditionsDirectory = new File(modelConditionsOutputDirectory);

					if (!modelConditionsDirectory.isDirectory()) {
						System.out.println("Creating output directory: "
								+ modelConditionsOutputDirectory + "...");
						modelConditionsDirectory.mkdirs();
					}

					File executionConditionsDirectory = new File(executionConditionsOutputDirectory);

					if (!executionConditionsDirectory.isDirectory()) {
						System.out.println("Creating output directory: "
								+ executionConditionsOutputDirectory + "...");
						executionConditionsDirectory.mkdirs();
					}

					/* Create condition classes. */
					ConditionsGenerator conditionGenerator = new ConditionsGenerator();

					for (ParsedMethod currentCondition : conditions) {
						try {
							/* Create model condition. */
							String modelOutputFileName = modelConditionsOutputDirectory
									+ File.separator + currentCondition.getName() + ".java";

							boolean skipModelCondition = false;

							if (Util.fileExists(modelOutputFileName)) {
								System.out.print(modelOutputFileName + " already exists. ");

								if (overwrite) {
									System.out.println("It will be overwritten.");
								}
								else {
									System.out.println(modelOutputFileName
											+ " will not be generated.");
									skipModelCondition = true;
								}
							}

							if (!skipModelCondition) {
								System.out.println("Creating model condition class: "
										+ modelOutputFileName + "...");
								BufferedWriter modelOutputFile = new BufferedWriter(new FileWriter(
										modelOutputFileName));

								String modelConditionFileContent = new String();

								modelConditionFileContent += getModelClassesFileHeader();

								modelConditionFileContent += "package " + modelConditionsPackage
										+ ";\n\n";

								modelConditionFileContent += conditionGenerator
										.getModelConditionClass(currentCondition,
												executionConditionsPackage);

								modelOutputFile.write(Util.format(modelConditionFileContent));
								modelOutputFile.close();
							}

							/* Create execution condition. */

							boolean skipExecutionCondition = false;

							String executionOutputFileName = executionConditionsOutputDirectory
									+ File.separator + currentCondition.getName() + ".java";

							if (Util.fileExists(executionOutputFileName)) {
								System.out.print(executionOutputFileName + " already exists. ");

								if (overwrite) {
									System.out.println("It will be overwritten.");
								}
								else {
									System.out.println(executionOutputFileName
											+ " will not be generated.");
									skipExecutionCondition = true;
								}
							}

							if (!skipExecutionCondition) {
								System.out.println("Creating execution condition class: "
										+ executionOutputFileName + "...");
								String executionConditionFileContent = new String();

								executionConditionFileContent += getExecutionClassesFileHeader();

								executionConditionFileContent += "package "
										+ executionConditionsPackage + ";\n\n";

								executionConditionFileContent += conditionGenerator
										.getExecutionConditionClass(currentCondition,
												modelConditionsPackage);

								BufferedWriter executionOutputFile = new BufferedWriter(
										new FileWriter(executionOutputFileName));

								executionOutputFile.write(Util
										.format(executionConditionFileContent));
								executionOutputFile.close();
							}
						}
						catch (IOException e) {
							System.out
									.println("There were errors while creating classes for the MMPM sensor "
											+ currentCondition.getName() + ": " + e.getMessage());
						}
					}
				}
				catch (Exception e) {
					System.out
							.println("There was an unexpected error while creating conditions. Conditions generation aborted");
					e.printStackTrace();
				}
			}

			System.out.println("Finished successfully");
		}
		catch (Exception e) {
			System.out.println("An error occurred while creating actions and conditions classes");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Prints the usage syntax of the application.
	 */
	private static void printUsage() {
		System.out.println("Syntax error. Usage: \n");
		System.out
				.println("ActionsAndConditionsGenerator -c configurationFile [-r relativePath] [-o]\n");
		System.out.println("-\"configurationFile\" is the configuration file that includes all");
		System.out.println("the information required to run the actions and conditions generator.");
		System.out
				.println("-\"r\" is the root path (directory) of all the files specified within the ");
		System.out.println("configuration file. This is an optional option. If not specified, ");
		System.out.println("if will be considered to be the current execution directory.");
		System.out
				.println("-\"o\" is an optional option. If not specified, generated output files will not");
		System.out
				.println("overwrite files in the file system. As a result, if there are files in the file");
		System.out
				.println("system with the same name as those generated by the application, they will not");
		System.out.println("be overwritten. Otherwise, it will overwrite any existing file.");
	}

	/**
	 * Returns the file header for model actions and conditions.
	 */
	private static String getModelClassesFileHeader() {
		String result = new String();

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String currentDate = dateFormat.format(date);

		result += "// ******************************************************* \n";
		result += "//                   MACHINE GENERATED CODE                \n";
		result += "//                       DO NOT MODIFY                     \n";
		result += "//                                                         \n";
		result += "// Generated on " + currentDate + "\n";
		result += "// ******************************************************* \n";

		return result;
	}

	/**
	 * Returns the file header for execution actions and conditions.
	 */
	private static String getExecutionClassesFileHeader() {
		String result = new String();

		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String currentDate = dateFormat.format(date);

		result += "// ******************************************************* \n";
		result += "//                   MACHINE GENERATED CODE                \n";
		result += "//                MUST BE CAREFULLY COMPLETED              \n";
		result += "//                                                         \n";
		result += "//           ABSTRACT METHODS MUST BE IMPLEMENTED          \n";
		result += "//                                                         \n";
		result += "// Generated on " + currentDate + "\n";
		result += "// ******************************************************* \n";

		return result;
	}
}
