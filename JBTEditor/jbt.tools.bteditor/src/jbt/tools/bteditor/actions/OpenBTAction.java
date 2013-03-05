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

import java.util.Vector;

import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.editor.BTEditorInput;
import jbt.tools.bteditor.util.StandardDialogs;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Action for loading behaviour trees into the application, and opens an editor
 * for every tree that is properly opened. The trees are read from XML files.
 * This action can open several trees simultaneously.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class OpenBTAction extends Action {
	/** Names of the files that contain the trees to open. */
	private Vector<String> fileNames;

	/**
	 * Constructor.
	 * 
	 * @param fileNames
	 *            names of the files that contain the trees to open.
	 */
	public OpenBTAction(Vector<String> fileNames) {
		this.fileNames = fileNames;
	}

	public void run() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getWorkbenchWindows()[0].getActivePage();
		
		Vector<Exception> exceptions=new Vector<Exception>();

		for (String fileName : this.fileNames) {
			BTEditorInput editorInput = new BTEditorInput(fileName, true, false);

			try {
				activePage.openEditor(editorInput, BTEditor.ID);
			} catch (PartInitException e) {
				exceptions.add(e);
			}
		}
		
		if(exceptions.size()!=0){
			StandardDialogs.exceptionDialog("Error opening tree",
					"There was an error when opening the tree", exceptions);
		}
	}
}
