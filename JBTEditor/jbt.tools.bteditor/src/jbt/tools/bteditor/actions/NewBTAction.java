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

import jbt.tools.bteditor.Application;
import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.editor.BTEditorInput;
import jbt.tools.bteditor.util.IconsPaths;
import jbt.tools.bteditor.util.StandardDialogs;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Action that creates a new BT and opens an editor for it.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NewBTAction extends Action {
	/** Integer used for generating names for the trees that are created. */
	private static int currentTreeNumber = 1;

	/**
	 * Constructor.
	 */
	public NewBTAction() {
		this.setText("New BT");
		this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Application.PLUGIN_ID, IconsPaths.NEW_BT));
	}

	/**
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench()
				.getWorkbenchWindows()[0].getActivePage();

		BTEditorInput editorInput = new BTEditorInput("new "
				+ currentTreeNumber, false, false);

		currentTreeNumber++;

		try {
			activePage.openEditor(editorInput, BTEditor.ID);
		} catch (PartInitException e) {
			StandardDialogs.exceptionDialog("Error when creating new tree",
					"There was an unexpected error when creating the tree", e);
		}
	}
}
