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

import java.io.File;
import java.util.Vector;

import jbt.tools.bteditor.Application;
import jbt.tools.bteditor.util.Extensions;
import jbt.tools.bteditor.util.IconsPaths;

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
public class DialogOpenBTAction extends Action implements IWorkbenchAction {
	private IWorkbenchWindow window;

	/**
	 * Constructor.
	 * 
	 * @param window the main window.
	 */
	public DialogOpenBTAction(IWorkbenchWindow window) {
		this.window = window;
		this.setText("Open BT");
		this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Application.PLUGIN_ID, IconsPaths.OPEN_BT));
	}

	/**
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		/*
		 * Open dialog for asking the user to enter some file names.
		 */
		FileDialog dialog = new FileDialog(this.window.getShell(), SWT.MULTI);
		String[] individualFilters = Extensions
				.getFiltersFromExtensions(Extensions.getBTFileExtensions());
		String[] unifiedFilter = new String[] { Extensions
				.getUnifiedFilterFromExtensions(Extensions
						.getBTFileExtensions()) };
		String[] filtersToUse = Extensions.joinArrays(individualFilters,
				unifiedFilter);
		dialog.setFilterExtensions(filtersToUse);
		dialog.setText("Open BT");

		/*
		 * If the user has selected at least one file name, we must open it.
		 * Note that the user may select several files.
		 */
		if (dialog.open() != null) {
			/* Get the name of the files (NOT absolute path). */
			String[] singleNames = dialog.getFileNames();

			/*
			 * This vector will store the absolute path of every single selected
			 * file.
			 */
			Vector<String> absolutePath = new Vector<String>();

			for (int i = 0, n = singleNames.length; i < n; i++) {
				StringBuffer buffer = new StringBuffer(dialog.getFilterPath());
				if (buffer.charAt(buffer.length() - 1) != File.separatorChar)
					buffer.append(File.separatorChar);
				buffer.append(singleNames[i]);
				absolutePath.add(buffer.toString());
			}

			new OpenBTAction(absolutePath).run();
		}
	}

	public void dispose() {
	}
}
