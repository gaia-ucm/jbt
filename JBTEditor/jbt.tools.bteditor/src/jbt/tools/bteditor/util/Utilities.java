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
package jbt.tools.bteditor.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import jbt.tools.bteditor.editor.BTEditor;
import jbt.tools.bteditor.editor.BTEditorIDGenerator;
import jbt.tools.bteditor.editor.BTEditorInput;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

/**
 * General utilities used in the tool.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class Utilities {
	/**
	 * Returns a IViewPart by its class. If cannot be found, returns null.
	 */
	public static IViewPart getView(Class c) {
		IWorkbenchPage page = getMainWindowActivePage();

		if (page != null) {
			IViewReference[] views = page.getViewReferences();
			for (int i = 0; i < views.length; i++) {
				if (views[i].getView(true) != null) {
					if (c.isInstance(views[i].getView(false))) {
						return views[i].getView(false);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns a List containing all the BTEditor that are currently open.
	 */
	public static List<BTEditor> getBTEditors() {
		IWorkbenchPage activePage = getMainWindowActivePage();

		if (activePage != null) {
			IEditorReference[] editors = activePage.getEditorReferences();
			if (editors.length == 0)
				return new Vector<BTEditor>();
			List<BTEditor> returnedEditors = new Vector<BTEditor>();
			for (int i = 0; i < editors.length; i++) {
				if (editors[i].getEditor(false) instanceof BTEditor) {
					returnedEditors.add((BTEditor) editors[i].getEditor(false));
				}
			}
			return returnedEditors;
		}

		return new LinkedList<BTEditor>();
	}

	/**
	 * Given a BTEditor ID (see {@link BTEditorIDGenerator}), this method
	 * returns the corresponding BTEditor, or null if not found.
	 */
	public static BTEditor getBTEditor(long btEditorID) {
		List<BTEditor> btEditors = getBTEditors();

		for (BTEditor editor : btEditors) {
			if (((BTEditorInput) editor.getEditorInput()).getEditorID() == btEditorID) {
				return editor;
			}
		}

		return null;
	}

	/**
	 * Returns the currently active BTEditor, or null if no BTEditor is active.
	 */
	public static BTEditor getActiveBTEditor() {
		IWorkbenchPage page = getMainWindowActivePage();

		if (page != null) {
			IEditorPart editor = page.getActiveEditor();
			if (editor instanceof BTEditor)
				return (BTEditor) editor;
			else
				return null;
		}

		return null;
	}

	/**
	 * Returns the main application window.
	 */
	public static IWorkbenchWindow getMainWindow() {
		return PlatformUI.getWorkbench().getWorkbenchWindows()[0];
	}

	/**
	 * Returns the active page of the window returned by
	 * {@link #getMainWindow()}, or null if not found.
	 */
	public static IWorkbenchPage getMainWindowActivePage() {
		return getMainWindow().getActivePage();
	}

	/**
	 * Returns the application's display.
	 */
	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}

	/**
	 * Returns the application's shell.
	 */
	public static Shell getShell() {
		return PlatformUI.getWorkbench().getDisplay().getActiveShell();
	}

	/**
	 * Returns the stack trace of a Throwable as a String. This can be used for
	 * exceptions, for example. This method may return an empty ("") String in
	 * case an error occurs or in case <code>t</code> does not have a strack
	 * trace.
	 */
	public static String stackTraceToString(Throwable t) {
		String retValue = "";
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			retValue = sw.toString();
		} finally {
			try {
				if (pw != null)
					pw.close();
				if (sw != null)
					sw.close();
			} catch (IOException e) {
			}
		}
		return retValue;
	}

	/**
	 * Activates an editor.
	 * 
	 * @param editor
	 *            the editor to activate.
	 */
	public static void activateEditor(EditorPart editor) {
		IWorkbenchPage page = editor.getSite().getPage();
		page.activate(editor);
	}

	private Utilities() {
	}
}
