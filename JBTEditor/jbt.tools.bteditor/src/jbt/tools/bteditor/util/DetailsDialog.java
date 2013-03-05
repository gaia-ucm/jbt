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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class represents a dialog that shows an icon, a message, and a text area
 * where plain text can be shown.
 * <p>
 * This class represents the classic "details" dialog that shows a "details"
 * button to see a detailed report of the message.
 * <p>
 * This dialog has two buttons, a "close" button and a "details" button. By
 * pressing the details button, the details area can be hidden or made visible.
 * The icon that is showed by the dialog can also be set.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class DetailsDialog extends IconAndMessageDialog {
	/**
	 * Type of icon to use. Must be {@link #ERROR}, {@link #WARNING},
	 * {@link #INFORMATION} or {@link #QUESTION}.
	 */
	protected int iconType;
	/**
	 * Text shown in the details area.
	 */
	protected String details;
	/**
	 * Text area where the details are shown.
	 */
	protected Text detailsText;
	/**
	 * Boolean that tells if the details text ({@link #detailsText}) is
	 * currently created or not. The visualization and hiding of the details
	 * area is done by creating and destroying {@link #detailsText}. This flag
	 * tells if {@link #detailsText} is actyallu created or not.
	 */
	protected boolean detailsTextCreated = false;
	/**
	 * Constant for an error icon.
	 */
	public static final int ERROR = 0;
	/**
	 * Constant for a warning icon.
	 */
	public static final int WARNING = 1;
	/**
	 * Constant for an information icon.
	 */
	public static final int INFORMATION = 2;
	/**
	 * Constant for a question icon.
	 */
	public static final int QUESTION = 3;
	/**
	 * Button used to hide and visualize the details area.
	 */
	protected Button detailsButton;
	/**
	 * Title of the dialog.
	 */
	protected String title;

	/**
	 * Constructs a DetailsDialog. The style of the dialog (the shell) is
	 * <code>SWT.CLOSE | SWT.RESIZE | SWT.MAX | SWT.MODELESS</code>. .
	 * 
	 * @param title
	 *            title of the dialog.
	 * @param message
	 *            main message shown by the dialog.
	 * @param details
	 *            text that is shown in the details area.
	 * @param iconType
	 *            type of the icon that is shown in the dialog. Must be one of
	 *            the following: <code>{@link #ERROR}</code>,
	 *            <code>{@link #WARNING}</code>,
	 *            <code>{@link #INFORMATION}</code> ,
	 *            <code>{@link #QUESTION}</code>.
	 * @param parentShell
	 *            this dialog's parent shell. If null, it is a top level shell.
	 */
	public DetailsDialog(String title, String message, String details, int iconType,
			Shell parentShell) {
		this(title, message, details, iconType, parentShell, SWT.CLOSE | SWT.RESIZE | SWT.MAX
				| SWT.MODELESS);
	}

	/**
	 * Constructs a DetailsDialog. The dialog style can be specified.
	 * 
	 * @param title
	 *            title of the dialog.
	 * @param message
	 *            main message shown by the dialog.
	 * @param details
	 *            text that is shown in the details area.
	 * @param iconType
	 *            type of the icon that is shown in the dialog. Must be one of
	 *            the following: <code>{@link #ERROR}</code>,
	 *            <code>{@link #WARNING}</code>,
	 *            <code>{@link #INFORMATION}</code> ,
	 *            <code>{@link #QUESTION}</code>.
	 * @param parentShell
	 *            this dialog's parent shell. If null, it is a top level shell.
	 * @param shellStyle
	 *            style of the dialog's shell.
	 */
	public DetailsDialog(String title, String message, String details, int iconType,
			Shell parentShell, int shellStyle) {
		super(parentShell);
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null");
		}
		this.message = message;
		if (iconType < 0 || iconType > 3) {
			throw new IllegalArgumentException("Invalid icon type (" + iconType + ")");
		}
		if (details == null) {
			throw new IllegalArgumentException("Details cannot be null");
		}
		if (title == null) {
			throw new IllegalArgumentException("Title cannot be null");
		}
		this.details = details;
		this.iconType = iconType;
		this.title = title;
		this.setBlockOnOpen(false);
		this.setShellStyle(shellStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		return createMessageArea(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
		this.detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
				IDialogConstants.SHOW_DETAILS_LABEL, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.DETAILS_ID) {
			/*
			 * If the details button has been pressed, then the details area
			 * must be shown or hidden, depending on whether it as hidden or
			 * not. After doing so, the dialog must be resized.
			 */
			Point dialogOldDimensions = getShell().getSize();
			if (this.detailsTextCreated) {
				/*
				 * If the details area is being showed, we delete it. Also, the
				 * label on the details button must be changed.
				 */
				this.detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
				this.detailsText.dispose();
			}
			else {
				/*
				 * If the text area is not being showed, it must be created and
				 * showed. In order to do so, we initialize "this.detailsText".
				 * Also, the label on the details button must be changed.
				 */
				this.detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
				this.detailsText = new Text((Composite) getContents(), SWT.BORDER | SWT.READ_ONLY
						| SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
				this.detailsText.setText(this.details);
				GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
				this.detailsText
						.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				data.horizontalSpan = 2;
				this.detailsText.setLayoutData(data);
			}
			getContents().getShell().layout();
			this.detailsTextCreated = !this.detailsTextCreated;

			/*
			 * The dialog is finalli resized.
			 */
			Point dialogNewDimensions = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int screenHeight = Display.getCurrent().getClientArea().height;
			getShell()
					.setSize(
							new Point(dialogOldDimensions.x, Math.min(dialogNewDimensions.y,
									screenHeight)));
		}
		else {
			/*
			 * Close the dialog...
			 */
			close();
		}
		setReturnCode(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	protected Image getImage() {
		switch (this.iconType) {
			case ERROR:
				return getErrorImage();
			case WARNING:
				return getWarningImage();
			case INFORMATION:
				return getInfoImage();
			case QUESTION:
				return getQuestionImage();
			default:
				throw new RuntimeException("Invalid type of image: " + this.iconType);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(this.title);
	}
}
