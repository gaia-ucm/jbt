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
package jbt.tools.bteditor.viewers;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import jbt.tools.bteditor.model.BTNode;
import jbt.tools.bteditor.model.ConceptualBTNode.NodeInternalType;
import jbt.tools.bteditor.model.BTNode.Parameter;
import jbt.tools.bteditor.util.Utilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * This class is used to display some information about a {@link BTNode}. This
 * Composite implements the {@link Observer} interface so that it gets notified
 * whenever the BTNode changes. By doing so, it can properly keep the displayed
 * information updated.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class NodeInfoViewer extends Composite implements Observer {
	/**
	 * The BTNode whose information is being displayed.
	 */
	private BTNode node;
	/**
	 * Toolkit used to manage the form that displays the information (
	 * {@link #global}).
	 */
	private FormToolkit toolkit;
	/**
	 * Form that displays all the node's information.
	 */
	private ScrolledForm global;

	/**
	 * Constructor.
	 */
	public NodeInfoViewer(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		this.toolkit = new FormToolkit(Utilities.getDisplay());
		this.global = this.toolkit.createScrolledForm(this);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		this.global.getBody().setLayout(layout);

		/* For disposing the toolkit. */
		this.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});

		/* For the wheel event. */
		this.global.addListener(SWT.Activate, new Listener() {
			public void handleEvent(Event event) {
				global.setFocus();
			}
		});
	}

	/**
	 * Sets the node whose information will be displayed. If <code>node</code>
	 * is null, no information is displayed. This method registers the
	 * NodeInfoViewer as an observer of <code>node</code>, so whenever
	 * <code>node</code> changes, the NodeInfoViewer will be notified and will
	 * update the displayed information accordingly.
	 */
	public void setNode(BTNode node) {
		if (this.node != null) {
			this.node.deleteObserver(this);
		}

		this.node = node;
		if (node != null)
			this.node.addObserver(this);
		updateView();
	}

	/**
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		updateView();
	}

	/**
	 * Updates all the information that is displayed on the Composite, by
	 * obtaining it from the BTNode.
	 */
	private void updateView() {
		/* Clean previos information. */
		if (this.global.getBody().getChildren().length != 0) {
			for (Control c : this.global.getBody().getChildren()) {
				c.dispose();
			}
		}

		/* If child is null, do nothing. */
		if (this.node == null) {
			return;
		}

		Composite parent = this.global.getBody();
		this.toolkit.createLabel(parent, "Type").setLayoutData(
				new TableWrapData(TableWrapData.LEFT));
		Label valueLabel = this.toolkit.createLabel(parent, "", SWT.RIGHT);
		valueLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		if (!this.node.getConceptualNode().getType().equals(NodeInternalType.ACTION.toString())
				&& !this.node.getConceptualNode().getType()
						.equals(NodeInternalType.CONDITION.toString())) {
			valueLabel.setText(this.node.getConceptualNode().getReadableType());
		} else {
			valueLabel.setText(this.node.getConceptualNode().getType());
		}

		/* Shows node's ID. */
		this.toolkit.createLabel(parent, "ID");
		this.toolkit.createLabel(parent, this.node.getID().toString(), SWT.RIGHT).setLayoutData(
				new TableWrapData(TableWrapData.FILL_GRAB));

		/* Shows name of the node. */
		if (this.node.getConceptualNode().getHasName()) {
			this.toolkit.createLabel(parent, "Name");

			valueLabel = this.toolkit.createLabel(parent, "", SWT.RIGHT);
			valueLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			/* This is for the root. */
			if (this.node.getConceptualNode().getType().equals(NodeInternalType.ROOT.toString())) {
				if (this.node.getName() != null) {
					valueLabel.setText(this.node.getName());
				} else {
					valueLabel.setText("Not assigned");
				}
			} else {
				valueLabel.setText(this.node.getConceptualNode().getReadableType());
			}
		}

		/* Show parameters. */
		List<Parameter> parameters = this.node.getParameters();

		for (Parameter p : parameters) {
			Label nameLabel = this.toolkit.createLabel(parent, "");
			valueLabel = this.toolkit.createLabel(parent, p.getValue(), SWT.RIGHT);
			valueLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			nameLabel.setText(p.getName() + (p.getFromContext() ? " (from context)" : ""));
		}

		/* Show error message. */
		if (this.node.getErrorMessage() != null) {
			this.toolkit.createLabel(parent, "ERROR");
			this.toolkit.createLabel(parent, this.node.getErrorMessage(), SWT.RIGHT).setLayoutData(
					new TableWrapData(TableWrapData.FILL_GRAB));
		}

		/* Lay out the Composite so that it refreshes. */
		global.reflow(true);
	}
}
