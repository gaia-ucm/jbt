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
package jbt.tools.btlibrarygenerator.librarygenerator;

import gatech.mmpm.tools.parseddomain.ParsedAction;
import gatech.mmpm.tools.parseddomain.ParsedMethod;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import jbt.execution.core.IBTLibrary;
import jbt.model.core.ModelTask;
import jbt.tools.btlibrarygenerator.modelbtgenerator.ModelBTGenerator;
import jbt.util.Pair;

/**
 * BTLibraryGenerator is used for generating String expressions representing the
 * declaration of a Java class that implements the {@link IBTLibrary} interface,
 * which also includes a list of behaviour trees that can be accessed.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTLibraryGenerator {
	/**
	 * The name of the class of the elements through which the iterator of
	 * IBTLibrary iterates.
	 */
	private static final String ITERABLE_ELEMENTS_CLASS_NAME = Pair.class.getCanonicalName() + "<"
			+ String.class.getCanonicalName() + ", " + ModelTask.class.getCanonicalName() + ">";

	/**
	 * The name of the class of the iterator of the IBTLibrary.
	 */
	private static final String ITERATOR_CLASS_NAME = Iterator.class.getCanonicalName() + "<"
			+ ITERABLE_ELEMENTS_CLASS_NAME + ">";

	/** Attribute "name" of the BT XML file. */
	private static final String NAME_ATTR = "name";

	/**
	 * This method creates an String representing an expression that declares a
	 * class that implements the {@link IBTLibrary} interface and contains all
	 * the behaviour trees in the files specified in <code>treeFileNames</code>.
	 * The created library will have <code>libraryClassName</code> as name.
	 * <p>
	 * In order to generate the expression for the library, some additional
	 * information must be provided:
	 * <ul>
	 * <li><code>actionsPackageName</code> is the name of the Java package that
	 * contains all the low level actions that are used in the tree.
	 * <li><code>conditionsPackageName</code> is the name of the Java package
	 * that contains all the low level conditions that are used in the tree.
	 * <li><code>actionsDefinition</code> is a list containing the MMPM
	 * definition of all the low level actions that are used in the tree.
	 * <li><code>conditionsDefinition</code> is a list containing the MMPM
	 * definition of all the low level conditions that are used in the tree.
	 * </ul>
	 * 
	 * @param libraryClassName
	 *            name of the class that will be generated.
	 * @param actionsPackageName
	 *            name of the Java package that contains all the low level
	 *            actions that are used in the tree.
	 * @param conditionsPackageName
	 *            name of the Java package that contains all the low level
	 *            conditions that are used in the tree.
	 * @param actionsDefinition
	 *            list containing the MMPM definition of all the low level
	 *            actions that are used in the tree.
	 * @param conditionsDefinition
	 *            list containing the MMPM definition of all the low level
	 *            conditions that are used in the tree.
	 * @param treeFileNames
	 *            names of the files that contain all the behaviour trees that
	 *            the generated library will contain.
	 * @return a String as described above.
	 * @throws BTLibraryGenerationException
	 *             if there is an error creating the library.
	 */
	public String getBTLibraryDeclaration(String libraryClassName, String actionsPackageName,
			String conditionsPackageName, List<ParsedAction> actionsDefinition,
			List<ParsedMethod> conditionsDefinition, List<String> treeFileNames)
			throws BTLibraryGenerationException {

		try {
			String result = new String();

			result += "/** BT library that includes the trees read from the following files:\n<ul>";

			for (String file : treeFileNames) {
				result += "<li>" + file + "</li>\n";
			}

			result += "</ul>*/";

			/* Class header. */
			result += "public class " + libraryClassName + " implements "
					+ IBTLibrary.class.getCanonicalName() + "{\n";

			/*
			 * Node declare all the behaviour trees as static variables. First,
			 * we declare them. Then we will generate appropriate expressions
			 * for them.
			 */
			List<String> treeNames = new LinkedList<String>();

			for (String fileName : treeFileNames) {
				String treeName = getTreeName(fileName);
				treeNames.add(treeName);
				result += "/**Tree generated from file " + fileName + ".*/";
				result += "private static " + ModelTask.class.getCanonicalName() + " " + treeName
						+ ";\n";
			}

			result += "\n";

			result += "/*Static initialization of all the trees.*/\n";

			result += "static{\n";

			/* Now we generate expressions for each behaviour tree. */
			ModelBTGenerator generator = new ModelBTGenerator();

			Iterator<String> fileNamesIt = treeFileNames.iterator();
			Iterator<String> treeNamesIt = treeNames.iterator();

			while (fileNamesIt.hasNext()) {
				String treeVariableName = treeNamesIt.next();
				String fileName = fileNamesIt.next();

				String modelBTDeclaration = generator.getModelBTDeclaration(treeVariableName,
						fileName, actionsPackageName, conditionsPackageName, actionsDefinition,
						conditionsDefinition, false);

				result += modelBTDeclaration + "\n\n";
			}

			result += "}\n\n";

			/* Adding the getBT() method of the IBTLibrary interface. */
			result += getGetBTMethod(treeNames) + "\n\n";

			/*
			 * Adding the iterator() method if the Iterable interface (which is
			 * implemented by IBTLibrary).
			 */
			result += getIteratorMethod(treeNames) + "\n\n";

			/*
			 * Creates an internal iterator class that knows how to iterate
			 * through the trees of the created library.
			 */
			result += getLibraryIterator(treeNames) + "\n";

			result += "}\n";

			return result;
		}
		catch (Exception e) {
			throw new BTLibraryGenerationException("Could not generate the BT library: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * Given the name of a file storing a behaviour tree in XML format (the
	 * format of <i>BT Editor</i>), this method returns the name of the tree
	 * (which is extracted from the <i>Root</i> node of the XML file).
	 * 
	 */
	private String getTreeName(String treeFileName) throws Exception {
		FileInputStream file = new FileInputStream(treeFileName);

		SAXBuilder builder = new SAXBuilder();

		Document doc = builder.build(file);
		Element documentRoot = doc.getRootElement();

		Element treeRoot = (Element) documentRoot.getChildren().get(0);

		String result = treeRoot.getAttributeValue(NAME_ATTR);

		file.close();

		return result;
	}

	/**
	 * Creates the {@link IBTLibrary#getBT(String)} method for the BT library.
	 * <code>treeNames</code> is the list of names of the trees in the library.
	 */
	private String getGetBTMethod(List<String> treeNames) {
		String result = new String();

		result += "/**Returns a  behaviour tree by its name, or null in case it cannot be found. "
				+ "It must be noted that the trees that are retrieved belong to the class, not to the instance (that is, the trees are static members of the class), "
				+ "so they are shared among all the instances of this class.*/";

		result += "public " + ModelTask.class.getCanonicalName() + " getBT("
				+ String.class.getCanonicalName() + " name){\n";

		for (String treeName : treeNames) {
			result += "if(name.equals(" + "\"" + treeName + "\")){\n";
			result += "return " + treeName + ";\n";
			result += "}\n";
		}

		result += "return null;\n";

		result += "}";

		return result;
	}

	/**
	 * Creates an expression for the {@link Iterable#iterator()} method of the
	 * IBTLibrary that is created. <code>treeNames</code> is the names of the
	 * trees that the library contains. The function will just return an
	 * instance of the private class BTLibraryIterator (see
	 * {@link #getLibraryIterator(List)}). Note that the <code>remove()</code>
	 * operation is not supported by this iterator.
	 */
	private String getIteratorMethod(List<String> treeNames) {
		String result = new String();

		result += "/**Returns an Iterator that is able to iterate through all the elements in the library.\n "
				+ "It must be noted that the iterator does not support the \"remove()\" operation.\n";

		result += "It must be noted that the trees that are retrieved belong to the class, "
				+ "not to the instance (that is, the trees are static members of the class), so "
				+ "they are shared among all the instances of this class.*/";

		result += "public " + ITERATOR_CLASS_NAME + " iterator(){\n";

		result += "return new BTLibraryIterator();\n";

		result += "}";

		return result;
	}

	/**
	 * Declares a class called BTLibraryIterator, which implements the
	 * {@link Iterator} interface, and that is able to iterate through the trees
	 * of the library. <code>treeNames</code> contains the names of the trees in
	 * the library. Note that the <code>remove()</code> operation is not
	 * supported by this iterator.
	 */
	private String getLibraryIterator(List<String> treeNames) {
		String result = new String();

		result += "private class BTLibraryIterator implements " + ITERATOR_CLASS_NAME + "{\n";

		result += "static final long numTrees = " + treeNames.size() + ";\n";

		result += "long currentTree = 0;\n\n";

		result += "public boolean hasNext(){\n";

		result += "return this.currentTree < numTrees;\n";

		result += "}\n\n";

		result += "public " + ITERABLE_ELEMENTS_CLASS_NAME + " next(){\n";

		result += "this.currentTree++;\n\n";

		for (int i = 0; i < treeNames.size(); i++) {
			String treeName = treeNames.get(i);

			result += "if((this.currentTree - 1) == " + i + "){\n";

			result += "return new " + ITERABLE_ELEMENTS_CLASS_NAME + "(\"" + treeName + "\", "
					+ treeName + ")" + ";\n";

			result += "}\n\n";
		}

		result += "throw new " + NoSuchElementException.class.getCanonicalName() + "();\n";

		result += "}\n\n";

		result += "public void remove(){\n";

		result += "throw new " + UnsupportedOperationException.class.getCanonicalName() + "();\n";

		result += "}\n";

		result += "}";

		return result;
	}
}
