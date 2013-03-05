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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jbt.tools.bteditor.model.BT;
import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.model.BTNode.VarParameter;
import jbt.tools.bteditor.model.ConceptualBTNode;
import jbt.tools.bteditor.model.BTNode.Identifier;
import jbt.tools.bteditor.model.BTNode.Parameter;
import jbt.tools.bteditor.model.ConceptualBTNode.NodeInternalType;
import jbt.tools.bteditor.model.ConceptualBTNode.ParameterType;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Class used to export and load behaviour trees into/from XML files.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTXMLManager {
	/**
	 * Exports a {@link BT} into an XML file.
	 */
	public static void export(BT tree, String fileName) throws IOException {
		FileOutputStream file = new FileOutputStream(fileName);
		export(tree, file);
		file.close();
	}

	/**
	 * Exports a {@link BT} into an XML file.
	 */
	public static void export(BT tree, FileOutputStream file) throws IOException {
		Document doc = createDocument(tree);
		XMLOutputter outputter = new XMLOutputter();
		Format format = Format.getPrettyFormat();
		outputter.setFormat(format);
		outputter.output(doc, file);
		file.close();
	}

	/**
	 * Loads a {@link BT} from an XML file with the output format of
	 * {@link #export(BT, FileOutputStream)} or {@link #export(BT, String)}.
	 */
	public static BT load(String fileName) throws IOException {
		FileInputStream file = new FileInputStream(fileName);
		BT loadedTree = load(file);
		loadedTree.reassignUnderlyingBT(loadedTree.getRoot());
		file.close();
		return loadedTree;
	}

	/**
	 * Loads a {@link BT} from an XML file with the output format of
	 * {@link #export(BT, FileOutputStream)} or {@link #export(BT, String)}.
	 */
	public static BT load(FileInputStream file) throws IOException {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);
			return loadTree(doc);
		} catch (Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * Loads a {@link BT} from a {@link Document} containing the BT in the
	 * format of {@link #export(BT, FileOutputStream)} or
	 * {@link #export(BT, FileOutputStream)}.
	 */
	private static BT loadTree(Document doc) throws IOException {
		return new BT(loadNode(doc.getRootElement().getChild("Node")));
	}

	/**
	 * Loads a BTNode from a {@link Element} containing the node in the format
	 * of {@link #export(BT, FileOutputStream)} or {@link #export(BT, String)} .
	 */
	private static BTNode loadNode(Element e) throws IOException {
		BTNode result = new BTNode(null);

		/*
		 * First of all, create the conceptual node associated to this BTNode.
		 * If it cannot be found in the BT library, the load fails.
		 */
		ConceptualBTNode xmlModel = NodesLoader.getNode(e.getAttributeValue("type"),
				e.getAttributeValue("name"));
		if (xmlModel == null) {
			throw new IOException(
					"Could not find type "
							+ e.getAttributeValue("type")
							+ (e.getAttributeValue("name") == null ? "" : "-"
									+ e.getAttributeValue("name")) + ". Tree not loaded.");
		}
		result.setConceptualNode(xmlModel);

		/* Set ID. */
		result.setID(new Identifier(e.getAttributeValue("id")));

		/* Set name (just for the root node) */
		if (result.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
			String name = e.getAttributeValue("name");
			if (name != null) {
				result.setName(name);
			}
		}

		/* Set parameters. */
		Element parameters = e.getChild("Parameters");

		if (parameters != null) {
			int i = 0;
			for (Object parameter : parameters.getChildren("Parameter")) {
				Element p = (Element) parameter;

				Parameter actualParameter = new Parameter();
				actualParameter.setName(p.getAttributeValue("name"));
				String fromContextString = p.getAttributeValue("fromcontext");

				boolean fromContext;
				if (fromContextString.equals("true")) {
					fromContext = true;
				} else if (fromContextString.equals("false")) {
					fromContext = false;
				} else {
					throw new IOException("Unexpected \"fromcontext\" value: " + fromContextString);
				}

				actualParameter.setFromContext(fromContext);
				
				ConceptualBTNode.Parameter par = xmlModel.getParameters().get(i);
				
				if (ParameterType.isVariable(par.getType()))
				{
					VarParameter varParameterResult = new VarParameter (actualParameter);
					boolean isConstant = p.getAttributeValue("isConstant").equals("true") ? true : false;
					varParameterResult.setIsConstant(isConstant);

					if (!isConstant)
						varParameterResult.setVariableName(p.getAttributeValue("variableName"));
					else
						varParameterResult.setVariableName("");

					varParameterResult.setValue(p.getText());
					result.addParameter(varParameterResult);
				}
				else
				{
					actualParameter.setValue(p.getText());
					result.addParameter(actualParameter);
				}
				++i;
			}
		}

		/* Recursively set the children. */
		Element children = e.getChild("Children");

		if (children != null) {
			for (Object child : children.getChildren("Node")) {
				BTNode btChild = loadNode((Element) child);
				btChild.setParent(result);
				result.addChild(btChild);
			}
		}

		/* Set the guard. */
		Element guard = e.getChild("Guard");

		if (guard != null) {
			result.setGuard(loadNode((Element) guard.getChildren().get(0)));
		}

		return result;
	}

	/**
	 * Creates a {@link Document} from a {@link BT}. This is used for exporting
	 * the BT into XML.
	 */
	private static Document createDocument(BT tree) {
		Document document = new Document();

		Element rootElement = new Element("Tree");

		rootElement.addContent(processNode(tree.getRoot()));

		document.setRootElement(rootElement);

		return document;
	}

	/**
	 * Creates an {@link Element} from a {@link BTNode}.
	 */
	private static Element processNode(BTNode node) {
		Element result = new Element("Node");
		result.setAttribute("id", node.getID().toString());

		if (node.getConceptualNode().getHasName()) {
			if (node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
				result.setAttribute("name", node.getName());
			} else {
				result.setAttribute("name", node.getConceptualNode().getName());
			}
		}

		result.setAttribute("type", node.getConceptualNode().getType());

		/* Process guard. */
		if (node.getGuard() != null) {
			Element guardElement = new Element("Guard");
			guardElement.setContent(processNode(node.getGuard()));
			result.addContent(guardElement);
		}

		/* Process parameters. */
		if (node.getConceptualNode().getParameters().size() != 0) {
			result.addContent(processParameters(node));
		}

		/* Process children. */
		if (node.getNumChildren() != 0) {
			Element children = new Element("Children");
			for (BTNode child : node.getChildren()) {
				children.addContent(processNode(child));
			}
			result.addContent(children);
		}

		return result;

	}

	/**
	 * Creates an {@link Element} containing all the parameters of a
	 * {@link BTNode}.
	 */
	private static Element processParameters(BTNode node) {
		Element result = new Element("Parameters");
		int i = 0;

		for (Parameter p : node.getParameters()) {
			
			Element currentParam = new Element("Parameter");
			currentParam.setAttribute("name", p.getName());
			currentParam.setText(p.getValue());
			currentParam.setAttribute("fromcontext", p.getFromContext() ? "true" : "false");
			
			ConceptualBTNode.Parameter aux = node.getConceptualNode().getParameters().get(i);
			if (ParameterType.isVariable(aux.getType()))
			{
				VarParameter varParameter = (VarParameter)p;
				
				boolean isConstant = varParameter.getIsConstant();
				currentParam.setAttribute("isConstant", isConstant ? "true" : "false");
				
				if (!isConstant)
				{
					currentParam.setAttribute("variableName", varParameter.getVariableName());
				} 
			}
			currentParam.setText(p.getValue());

			result.addContent(currentParam);
			
			++i;
		}

		return result;
	}
}
