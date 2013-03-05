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
package jbt.tools.bteditor.actions;

import java.io.IOException;
import java.util.Vector;

import jbt.tools.bteditor.NodesLoader;
import jbt.tools.bteditor.model.ConceptualNodesTree;
import jbt.tools.bteditor.util.StandardDialogs;
import jbt.tools.bteditor.util.Utilities;
import jbt.tools.bteditor.views.NodesNavigator;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;

/**
 * Action that loads, into the {@link NodesLoader}, the actions and conditions
 * (sensors) present in a MMPM domain file.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class LoadMMPMDomainAction extends Action {
	/** Names of the files to open. */
	private Vector<String> fileNames;

	/**
	 * Constructor.
	 * 
	 * @param fileNames
	 *            the names of the files to load.
	 */
	public LoadMMPMDomainAction(Vector<String> fileNames) {
		this.fileNames = fileNames;
	}

	/**
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		Vector<Exception> exceptions = new Vector<Exception>();

		for (String currentFile : this.fileNames) {
			try {
				ConceptualNodesTree newTree = NodesLoader
						.loadNonStandardNodes(currentFile);

				IViewPart view = Utilities.getView(NodesNavigator.class);

				if (view != null) {
					NodesNavigator treeView = (NodesNavigator) view;
					treeView.addTree(newTree);
				}

			} catch (IOException e) {
				exceptions.add(e);
			}
		}

		if (exceptions.size() != 0) {
			StandardDialogs.exceptionDialog("Error loading MMPM domain file",
					"There were errors when opening MMPM domain files",
					exceptions);
		}
	}
}
