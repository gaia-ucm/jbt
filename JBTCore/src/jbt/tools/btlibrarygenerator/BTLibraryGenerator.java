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
import gatech.mmpm.tools.parseddomain.ParsedAction;
import gatech.mmpm.tools.parseddomain.ParsedDomain;
import gatech.mmpm.tools.parseddomain.ParsedMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import jargs.gnu.CmdLineParser;
import jbt.execution.core.IBTLibrary;
import jbt.tools.btlibrarygenerator.util.Util;

/**
 * BTLibraryGenerator is an application that generates a behaviour tree
 * libraries (classes implementing {@link IBTLibrary}) from:
 * <ul>
 * <li>A set of behaviour trees specified in XML files.
 * <li>The MMPM definition of the low level actions and conditions that are used
 * in the trees.
 * </ul>
 * <p>
 * The syntax of this program is as follows:
 * 
 * <pre>
 * BTLibraryGenerator -c configurationFile [-r relativePath] [-o]
 * </pre>
 * 
 * Where <i>configurationFile</i> is an XML file that contains all the
 * information required to run this application. The syntax of such a file is:
 * 
 * <pre>
 * &lt;Configuration&gt;
 * 
 *  &lt;BTLibrary&gt;
 * 
 *    &lt;BTFile&gt;BTFile1&lt;/BTFile&gt;
 *    &lt;BTFile&gt;BTFile2&lt;/BTFile&gt;
 *    ...
 *    &lt;BTFile&gt;BTFileN&lt;/BTFile&gt;	
 * 
 *    &lt;DomainFile&gt;MMPMDomainFile1&lt;/DomainFile&gt;
 *    &lt;DomainFile&gt;MMPMDomainFile2&lt;/DomainFile&gt;
 *    ...
 *    &lt;DomainFile&gt;MMPMDomainFileN&lt;/DomainFile&gt;
 *  
 *    &lt;ModelActionsPackage&gt;Name of the package where model action classes are placed&lt;/ModelActionsPackage&gt;
 *  
 *    &lt;ModelConditionsPackage&gt;Name of the package where model condition classes are placed&lt;/ModelConditionsPackage&gt;
 *  
 *    &lt;LibraryClassName&gt;Name of the class that is going to be created&lt;/LibraryClassName&gt;
 *  
 *    &lt;LibraryPackage&gt;Name of the package for the generated BT library&lt;/LibraryPackage&gt;
 * 	
 *    &lt;LibraryOutputDirectory&gt;Name of the directory where the generated library is going to be stored&lt;/LibraryOutputDirectory&gt;
 *  
 *    &lt;/BTLibrary&gt;
 *  
 *  &lt;BTLibrary&gt;
 *  ...
 *  &lt;/BTLibrary&gt;
 *  
 *  ...
 * &lt;/Configuration&gt;
 * </pre>
 * 
 * <p>
 * The order in which the elements are specified is not relevant.
 * <p>
 * In the file the user can define several BT libraries, each one within the
 * <i>BTLibrary</i> element. For each BT library defined in a <i>BTLibrary</i>
 * element, the program will produce an output file (class implementing the
 * <code>IBTLibrary</code> interface) for the library.
 * <p>
 * The -r option is used to add a path to the beginning of the files listed in
 * the configuration file; as a result, each file is considered to be placed at
 * the path specified in the -r option. The -r option may not be specified, in
 * which case the files are considered to be at the current execution directory.
 * <p>
 * The -o option (standing for <i>overwrite</i>) is either is specified or not.
 * If it is not specified, the generated output files will not overwrite any
 * existing file in the file system, and as a result, a behaviour tree library
 * may not be produced in case there is a file with the same name in the file
 * system. If the option -o is specified, then generated output files will
 * overwrite any file in the file system whose name matches.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTLibraryGenerator {
	/** Tag of the XML configuration file. */
	private static final String CONFIGURATION_TAG = "Configuration";
	/** Tag of the XML configuration file. */
	private static final String BT_LIBRARY = "BTLibrary";
	/** Tag of the XML configuration file. */
	private static final String BT_FILE_TAG = "BTFile";
	/** Tag of the XML configuration file. */
	private static final String DOMAIN_FILE_TAG = "DomainFile";
	/** Tag of the XML configuration file. */
	private static final String LIBRARY_CLASS_NAME_TAG = "LibraryClassName";
	/** Tag of the XML configuration file. */
	private static final String LIBRARY_OUTPUT_DIRECTORY_TAG = "LibraryOutputDirectory";
	/** Tag of the XML configuration file. */
	private static final String LIBRARY_PACKAGE_TAG = "LibraryPackage";
	/** Tag of the XML configuration file. */
	private static final String MODEL_ACTIONS_PACKAGE_TAG = "ModelActionsPackage";
	/** Tag of the XML configuration file. */
	private static final String MODEL_CONDITIONS_PACKAGE_TAG = "ModelConditionsPackage";

	public static void main(String[] args) {
		/* Parse command line arguments. */
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option configurationFileOption = parser.addStringOption('c', "config");
		CmdLineParser.Option relativePathOption = parser.addStringOption('r', "relativepath");
		CmdLineParser.Option overwriteOption = parser.addBooleanOption('o', "overwrite");

		try {
			parser.parse(args);
		} catch (Exception e) {
			printUsage();
			System.exit(1);
		}

		try {
			/* Read relative path from command line. */
			String relativePath = (String) parser.getOptionValue(relativePathOption);

			if (relativePath == null) {
				relativePath = "";
			} else {
				relativePath = Util.addDirectorySeparator(relativePath);
			}

			/* Overwrite option. */
			Boolean overwrite = (Boolean) parser.getOptionValue(overwriteOption, Boolean.FALSE);

			/*
			 * Open the configuration file and read behaviour trees file names.
			 */
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

			/*
			 * Parse and create all the libraries specified in the configuration
			 * file.
			 */
			Element root = confFileDoc.getRootElement();
			List<Element> btLibrariesDescription = root.getChildren(BT_LIBRARY);

			for (Element currentBTLibrary : btLibrariesDescription) {
				try {
					/*
					 * Read library class name, packages and output directory
					 * from the configuration file.
					 */
					System.out.print("Creating next BT library ");

					String libraryClassName = currentBTLibrary.getChildText(LIBRARY_CLASS_NAME_TAG);

					if (libraryClassName == null) {
						System.out
								.println("There were errors while parsing the configuration file: a name for the BT library class must be specified");
						continue;
					}

					System.out.println("\"" + libraryClassName + "\"...");

					String libraryDirectory = relativePath
							+ currentBTLibrary.getChildText(LIBRARY_OUTPUT_DIRECTORY_TAG);

					libraryDirectory = Util.removeDirectorySeparator(libraryDirectory);

					String libraryPackage = currentBTLibrary.getChildText(LIBRARY_PACKAGE_TAG);

					if (libraryPackage == null) {
						System.out
								.println("There were errors while parsing the configuration file: a name for the BT library Java package must be specified");
						continue;
					}

					String modelActionsPackage = currentBTLibrary
							.getChildText(MODEL_ACTIONS_PACKAGE_TAG);

					if (modelActionsPackage == null) {
						System.out
								.println("There were errors while parsing the configuration file: the name of the model low level actions Java package must be specified");
						continue;
					}

					String modelConditionsPackage = currentBTLibrary
							.getChildText(MODEL_CONDITIONS_PACKAGE_TAG);

					if (modelConditionsPackage == null) {
						System.out
								.println("There were errors while parsing the configuration file: the name of the model low level conditions Java package must be specified");
						continue;
					}

					/* Read the names of the files that contain the BTs. */
					List<String> btFileNames = new LinkedList<String>();

					List<Element> childrenElements = currentBTLibrary.getChildren(BT_FILE_TAG);

					for (Element e : childrenElements) {
						btFileNames.add(relativePath + e.getText());
					}

					/*
					 * Now read all the domain actions and conditions (boolean
					 * conditions) stored in domain files.
					 */
					List<ParsedAction> actions = new LinkedList<ParsedAction>();
					List<ParsedMethod> conditions = new LinkedList<ParsedMethod>();
					List<String> domainFileNames = new LinkedList<String>();

					childrenElements = currentBTLibrary.getChildren(DOMAIN_FILE_TAG);

					for (Element e : childrenElements) {
						domainFileNames.add(e.getText());
					}

					for (String domainFile : domainFileNames) {
						/* Get the parsed domain of the current file. */
						domainFile = relativePath + domainFile;
						System.out.println("Reading and parsing " + domainFile + "...");
						FileInputStream currentFile = new FileInputStream(domainFile);

						SAXBuilder mmpmFileBuilder = new SAXBuilder();
						Document mmpmDoc = mmpmFileBuilder.build(currentFile);
						currentFile.close();
						ParsedDomain domain = new ParsedDomain();
						domain.init(mmpmDoc.getRootElement(), null);
						actions.addAll(domain.getActionSet().getAction());
						List<ParsedMethod> booleanSensors = new LinkedList<ParsedMethod>();
						for (ParsedMethod sensor : domain.getSensorSet().getMethods()) {
							if (sensor.getReturnedType() == ActionParameterType.BOOLEAN) {
								booleanSensors.add(sensor);
							}
						}
						conditions.addAll(booleanSensors);
					}

					/* Now generate the library. */
					String outputFileName = libraryDirectory + File.separator + libraryClassName
							+ ".java";

					if (Util.fileExists(outputFileName)) {
						System.out.print(outputFileName + " already exists. ");

						if (overwrite) {
							System.out.println("It will be overwritten.");
						} else {
							System.out.println("The behaviour tree library will not be generated.");
							System.out.println("Finished");
							System.exit(0);
						}
					}

					System.out.println("Creating library " + outputFileName + "...");
					String outputFileContent = new String();

					outputFileContent += getClassFileHeader();

					outputFileContent += "package " + libraryPackage + ";\n\n";
					jbt.tools.btlibrarygenerator.librarygenerator.BTLibraryGenerator generator = new jbt.tools.btlibrarygenerator.librarygenerator.BTLibraryGenerator();
					outputFileContent += generator.getBTLibraryDeclaration(libraryClassName,
							modelActionsPackage, modelConditionsPackage, actions, conditions,
							btFileNames);

					/* And create the output file for it. */
					File libraryOutputDirectory = new File(libraryDirectory);

					if (!libraryOutputDirectory.isDirectory()) {
						libraryOutputDirectory.mkdirs();
					}

					BufferedWriter outputFile = new BufferedWriter(new FileWriter(outputFileName));

					outputFile.write(Util.format(outputFileContent));

					outputFile.close();

					System.out
							.println("\"" + libraryClassName + "\"" + " was successfully created");
				} catch (Exception e) {
					System.out
							.println("There was an unexpected error while creating the current BT library. Skipped to next");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.out
					.println("There was an unrecoverable error while creating the BT libraries. Program aborted");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Prints the usage syntax of the application.
	 */
	private static void printUsage() {
		System.out.println("Syntax error. Usage: \n");
		System.out.println("BTLibraryGenerator -c configurationFile [-r relativePath] [-o]");
		System.out.println("-\"configurationFile\" is the configuration file that includes all");
		System.out.println("the information required to run the behaviour tree library generator.");
		System.out
				.println("-\"r\" is the root path (directory) of all the files specified within the ");
		System.out.println("configuration file. This is an optional option. If not specified, ");
		System.out.println("if will be considered to be the current execution directory.");
		System.out
				.println("-\"o\" is an optional option. If not specified, the generated output files will not");
		System.out
				.println("overwrite files in the file system, and as a result, a behaviour tree library");
		System.out
				.println("may not be produced in case there is a file with the same name in the file system");
		System.out.println("system. Otherwise, it will overwrite any existing file.");
	}

	/**
	 * Returns the file header for the BT library.
	 */
	private static String getClassFileHeader() {
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
}
