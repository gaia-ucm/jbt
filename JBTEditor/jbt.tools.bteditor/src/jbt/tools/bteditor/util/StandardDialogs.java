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

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;

/**
 * Class that contains a bunch of standard SWT dialogs to be used by calling
 * simple methods.
 * <p>
 * All these dialogs block when opened.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class StandardDialogs {
	/*
	 * Private constructor.
	 */
	private StandardDialogs() {}

	public static void informationDialog(String title, String informationMessage) {
		MessageDialog.openInformation(null, title, informationMessage);
	}

	public static void errorDialog(String title, String errorMessage) {
		MessageDialog.openError(null, title, errorMessage);
	}

	public static void exceptionDialog(String title, String errorMessage, Exception e) {
		DetailsDialog dialog = new DetailsDialog(title, errorMessage,
				Utilities.stackTraceToString(e), DetailsDialog.ERROR, null, SWT.APPLICATION_MODAL
						| SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.CLOSE);
		dialog.setBlockOnOpen(true);
		dialog.open();
	}

	public static void exceptionDialog(String title, String errorMessage, List<Exception> exceptions) {
		String exceptionMessage = new String();
		for (Exception currentException : exceptions) {
			exceptionMessage += "** Exception **\n\n"
					+ Utilities.stackTraceToString(currentException) + "\n\n";
		}
		DetailsDialog dialog=new DetailsDialog(title, errorMessage, exceptionMessage, DetailsDialog.ERROR, null,
				SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.CLOSE);
		
		dialog.setBlockOnOpen(true);
		dialog.open();
	}

	public static void warningDialog(String title, String warningMessage) {
		MessageDialog.openWarning(null, title, warningMessage);
	}

	public static boolean confirmationDialog(String title, String confirmationMessage) {
		return MessageDialog.openConfirm(null, title, confirmationMessage);
	}

	public static boolean questionDialog(String title, String question) {
		return MessageDialog.openQuestion(null, title, question);
	}
}
