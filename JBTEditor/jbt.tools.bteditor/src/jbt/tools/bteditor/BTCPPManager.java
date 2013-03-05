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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;

import jbt.tools.bteditor.NodesLoader.NodeCategories;
import jbt.tools.bteditor.model.BT;
import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.model.BTNode.Parameter;
import jbt.tools.bteditor.model.BTNode.VarParameter;
import jbt.tools.bteditor.model.ConceptualBTNode;
import jbt.tools.bteditor.model.ConceptualBTNode.NodeInternalType;
import jbt.tools.bteditor.model.ConceptualBTNode.ParameterType;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Class used to export and load behaviour trees into/from XML files.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class BTCPPManager {
	
	/**
	 * Exports a {@link BT} into an CPP file. Also creates a header file
	 */
	public static void export(BT tree, String fileName) throws IOException {
		
		//Uncomment blocks to export different files
		
		//String headerName = fileName.replace(".cpp", ".h");
		//FileOutputStream headerFile = new FileOutputStream(headerName);
		//exportHeader(tree, headerFile);
		//		
		//FileOutputStream file = new FileOutputStream(fileName);
		//export(tree, file);
		//file.close();
		
		String inlineName = fileName.replace(".cpp", ".inl");
		FileOutputStream inlineFile = new FileOutputStream(inlineName);
		exportInline(tree, inlineFile);
		inlineFile.close();
	}
	
	/**
	 * Exports a {@link BT} into a CPP file
	 */
	public static void exportInline(BT tree, FileOutputStream file) throws IOException {

		// Connect print stream to the output stream
		PrintStream p = new PrintStream( file );
		createInlineFile(tree,p);
		p.close();
	
		file.close();
	}

	/**
	 * Exports a {@link BT} into a CPP file
	 */
	public static void export(BT tree, FileOutputStream file) throws IOException {

		// Connect print stream to the output stream
		PrintStream p = new PrintStream( file );
		createCPPFile(tree, p);
		p.close();
	
		file.close();
	}
	
	/**
	 * Exports a {@link BT} into a .H file
	 */
	public static void exportHeader(BT tree, FileOutputStream file) throws IOException {

		PrintStream p = new PrintStream( file );
		createHeaderFile(tree, p);
		p.close();
	
		file.close();
	}

	/**
	 * Creates and .CPP file from a {@link BT}.
	 */
	private static void createCPPFile(BT tree, PrintStream p) {
		
		String BTName = tree.getRoot().getName();
		
		p.println("/**");
		p.println(" @file "+BTName+".cpp");
		p.println(" Contiene la implementaci�n de un componente de BT");
		p.println();
		p.println(" @author Fichero generado autom�ticamente con JBT Editor");
		p.println("*/");
		p.println();
		p.println("#include \""+BTName+".h\"");
		p.println("#include \"LatentAction.h\"");
		p.println();
		p.println("#include <alive/TreeBuilder.h>");
		p.println();
		p.println();
		p.println("namespace OIM {");
		p.println("");
		p.println("\tIMP_FACTORIA(C"+BTName+");");
		p.println("\tIMPL_SUBCLASS_TYPE(C"+BTName+", CBTComponent);");
		p.println("\talive::Node *C"+BTName+"::createBehaviourTree() {");
		p.println("\t\treturn");
		p.println("\t\t\talive::TreeBuilder()");
		
		//TODO write file info
		Element rootElement = new Element("Tree");
		processNode(tree.getRoot(), p, "\t\t\t");
		
		p.println();
		p.println("\t}");
		p.println();
		p.println("} //namespace OIM");
	}
	
	/**
	 * Creates and .CPP file from a {@link BT}.
	 */
	private static void createInlineFile(BT tree, PrintStream p) {
		
		String BTName = tree.getRoot().getName();
		
		p.println("\t\treturn");
		p.println("\t\t\talive::TreeBuilder()");
		
		//TODO write file info
		Element rootElement = new Element("Tree");
		processNode(tree.getRoot(), p, "\t\t\t");
	}
	
	/**
	 * Creates a .H from a {@link BT}. 
	 */
	private static void createHeaderFile(BT tree, PrintStream p) {
		
		Calendar cal = Calendar.getInstance();
		int currentYear = cal.get(Calendar.YEAR);
		String BTName = tree.getRoot().getName();
		
		p.println("/**");
		p.println("@file "+BTName+".h");
		p.println(" Contiene la declaraci�n de un componente de BT");
		p.println();
		p.println(" @author Fichero generado autom�ticamente con JBT Editor");
		
		p.println("*/");
		
		p.println("#ifndef OIM_"+BTName+"_H");
		p.println("#define OIM_"+BTName+"_H");
		p.println();
		p.println("#include \"BTComponent.h\"");
		p.println();
		p.println("#include <vector>");
		p.println();
		p.println("namespace OIM {");
		
		p.println("/**");
		p.println("\tComponente de IA de una entidad que funciona utilizando");
		p.println("\tun �rbol de comportamiento concreto de prueba.");
		p.println("\t<p>");
		p.println("\tEl componente simplemente hereda de CBTComponent y");
		p.println("\tredefine el m�todo factor�a para la creaci�n del");
		p.println("\t�rbol de comportamiento a utilizar.");
		p.println("\t@author Marco Antonio G�mez Mart�n");
		p.println("\t@date "+currentYear);
		p.println("*/");
		
		p.println("\tclass C"+BTName+" : public CBTComponent {");
		p.println("\t\tDEC_FACTORIA(C"+BTName+");");
		p.println("\t\tDECL_TYPE();");
		p.println("\tprotected:");
		p.println("\t\t/**");
		p.println("\t\t M�todo factor�a que crea el �rbol de comportamiento");
		p.println("\t\t utilizado por la entidad que contiene el componente.");
		p.println("\t\t <p>");
		p.println("\t\t En este caso, crea un BT para probar las acciones.");
		p.println("\t\t @return �rbol de comportamiento a ejecutar por la entidad");
		p.println("\t\t  a la que pertenece el componente.");
		p.println("\t\t */");
		p.println("\t\tvirtual alive::Node *createBehaviourTree();");
		p.println("\t};");
		p.println();
		p.println("} // namespace OIM");
		p.println();
		p.println("#endif // OIM_BTComponent_H");
	}

	/**
	 * Creates the method calls of a {@link BTNode}.
	 */
	private static void processNode(BTNode node, PrintStream p, String margin) {
		
		String nodeType = node.getConceptualNode().getType();
		NodeCategories category = NodesLoader.getCategoryOf(nodeType);
		String endToken = margin+".end()\n";
		
		switch(category)
		{
			case COMPOSITE:
				
				p.println(margin+".composite<alive::"+nodeType+">()");
				break;
				
			case DECORATOR:
				
				p.println(margin+".decorator<"+nodeType+">()");
				break;
				
			case LEAF:
				
				p.println(margin+".execute<"+nodeType+">()");
				break;
		}
		
		/* Process guard. */
		//if (node.getGuard() != null) {
		//	Element guardElement = new Element("Guard");
		//	guardElement.setContent(processNode(node.getGuard(), p));
		//	result.addContent(guardElement);
		//}

		/* Process parameters. */
		if (node.getConceptualNode().getParameters().size() != 0) {
			processParameters(node, p, margin+"\t");
		}
		
		/* Process children. */
		if (node.getNumChildren() != 0) {
			Element children = new Element("Children");
			for (BTNode child : node.getChildren()) {
				processNode(child, p, margin+"\t");
			}
		}
		if (!(node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString()))) {
			p.print(endToken);
		}
		else
		{
			p.print(margin+";");
		}
	}

	/**
	 * Creates the method calls associated to all the parameters of a {@link BTNode}.
	 */
	private static void processParameters(BTNode node, PrintStream p, String margin) {
		
		for (int i=0; i<node.getParameters().size();++i)
		{
			Parameter par = node.getParameters().get(i);
			ParameterType type = node.getConceptualNode().getParameters().get(i).getType();
			String textToWrite = "";
			
			VarParameter varParameter = null;
			
			if (ParameterType.isVariable(type))
			{
				varParameter = (VarParameter)par;
			}
			
			switch (type)
			{
			    case VARIABLE_INT:
			    	
			    	if (!varParameter.getIsConstant())
			    		textToWrite = "IntParam"+"(\""+varParameter.getVariableName()+"\", "+varParameter.getValue()+")";
			    	else
			    		textToWrite = ""+varParameter.getValue()+"";
			    	break;
			    	
			    case VARIABLE_FLOAT:
			    	
			    	if (!varParameter.getIsConstant())
			    		textToWrite = "FloatParam"+"(\""+varParameter.getVariableName()+"\", "+Float.parseFloat(varParameter.getValue())+"f)";
			    	else
			    		textToWrite = ""+Float.parseFloat(varParameter.getValue())+"f";
			    		
			    	break;
			    		
			    case VARIABLE_STRING:
			    	
			    	if (!varParameter.getIsConstant())
			    		textToWrite = "StringParam"+"(\""+varParameter.getVariableName()+"\", \""+varParameter.getValue()+"\")";
			    	else
			    		textToWrite = "\""+varParameter.getValue()+"\"";
			
			    	break;
			    	
				case BOOLEAN:
				case INTEGER:
					textToWrite = par.getValue();
					break;
				case REAL:
					textToWrite = Float.parseFloat(par.getValue())+"f";
					break;
				case STRING:
					textToWrite = "\""+par.getValue()+"\"";
					break;
			}
			p.println(margin+"\t."+par.getName()+"("+textToWrite+")");
		}
	}
}
