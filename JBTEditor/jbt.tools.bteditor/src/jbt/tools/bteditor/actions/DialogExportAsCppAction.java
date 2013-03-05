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

import java.util.List;

import jbt.tools.bteditor.Application;
import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.editor.BTEditorInput;
import jbt.tools.bteditor.model.BT;
import jbt.tools.bteditor.util.Extensions;
import jbt.tools.bteditor.util.IconsPaths;
import jbt.tools.bteditor.util.StandardDialogs;
import jbt.tools.bteditor.util.Utilities;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Action that loads several behaviour trees into the application. It first
 * opens a dialog where the user selects the files that contain the trees. Then,
 * these trees are opened in separate editors.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class DialogExportAsCppAction extends Action implements IWorkbenchAction {
	private IWorkbenchWindow window;

	/**
	 * Constructor.
	 * 
	 * @param window the main window.
	 */
	public DialogExportAsCppAction(IWorkbenchWindow window) {
		this.window = window;
		this.setText("Export BT to an inline file");
		this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, IconsPaths.INL));
	}

	/**
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		
		BTEditor myEditor = Utilities.getActiveBTEditor();	
		if (myEditor==null || !myEditor.checkTree())
		{
			StandardDialogs.errorDialog("Tree not saved",
					"Errors were detected while validating the tree");
		}
		else
		{
			/*
			 * Open dialog for asking the user to enter some file names.
			 */
			FileDialog dialog = new FileDialog(this.window.getShell(), SWT.SAVE);
			String[] individualFilters = Extensions.getFiltersFromExtensions(Extensions.getInlFileExtensions());
			String[] unifiedFilter = new String[] { Extensions.getUnifiedFilterFromExtensions(Extensions.getInlFileExtensions()) };
			String[] filtersToUse = Extensions.joinArrays(individualFilters, unifiedFilter);
			dialog.setFilterExtensions(filtersToUse);
			dialog.setText("Export BT to an inline file");
			
			String fileName = dialog.open();
			
			if (fileName != null)
			{
				List<BTEditor> editors = Utilities.getBTEditors();
	
				for (BTEditor editor : editors) {
					BTEditorInput editorInput = (BTEditorInput) editor.getEditorInput();
					if (editorInput.isFromFile() && editorInput.getTreeName().equals(fileName)) {
						throw new RuntimeException(
								"There is a behaviour tree already open with the same name ("
										+ fileName + "). Close it first.");
					}
				}
					
				String targetFileName = Extensions.joinFileNameAndExtension(fileName,
						Extensions.getInlFileExtensions()[dialog.getFilterIndex()]);
				
				BT tree = Utilities.getActiveBTEditor().getBT();
				
				new ExportToCppAction(tree, targetFileName).run();
			}
		}
	}

	public void dispose() {
	}
}
