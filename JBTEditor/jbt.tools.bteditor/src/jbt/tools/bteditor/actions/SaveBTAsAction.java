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

import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.editor.BTEditorInput;
import jbt.tools.bteditor.model.BT;
import jbt.tools.bteditor.util.Extensions;
import jbt.tools.bteditor.util.Utilities;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

/**
 * Action that saves a behaviour tree in a XML file. It first asks the user to
 * enter a file name in a dialog, and then saves the tree. If there is any
 * problem in the saving process, an expcetion is thrown. If there is an open
 * tree coming from a file with the same name that the user specifies, the
 * saving process also fails, and an exception is thrown.
 */
public class SaveBTAsAction extends Action {
	/** The tree to save. */
	private BT tree;
	/** The file where the tree will be stored. */
	private String selectedFile;
	/** The file name that is initially displayed in the file dialog. */
	private String initialFileName;

	/**
	 * Constructor.
	 * 
	 * @param tree
	 *            tree to save.
	 * @param initialFileNAme
	 *            the file name that is initially displayed in the file dialog.
	 */
	public SaveBTAsAction(BT tree, String initialFileName) {
		this.tree = tree;
		this.initialFileName = initialFileName;
	}

	/**
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		FileDialog dialog = new FileDialog(
				PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell(), SWT.SAVE);
		
		dialog.setOverwrite(true);
		dialog.setFilterExtensions(Extensions.getFiltersFromExtensions(Extensions
				.getBTFileExtensions()));
		dialog.setText("Save BT as");
		dialog.setFileName(this.initialFileName);
		
		String fileName = dialog.open();

		if (fileName != null) {
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
					Extensions.getBTFileExtensions()[dialog.getFilterIndex()]);

			new SaveBTAction(this.tree, targetFileName).run();

			this.selectedFile = targetFileName;
		}
	}

	/**
	 * Returns the name of the file where the tree has been stored. Returns null
	 * if the tree could not be saved or if {@link #run()} has not been called
	 * yet.
	 */
	public String getSelectedFile() {
		return this.selectedFile;
	}
}
