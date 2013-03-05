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
package jbt.tools.bteditor;

import jbt.tools.bteditor.views.NodeInfo;
import jbt.tools.bteditor.views.NodesNavigator;
import jbt.tools.bteditor.views.NodesSearcher;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	private static final String RIGHT_FOLDER_ID = "RightFolder";
	private static final String BOTTOM_RIGHT_FOLDER_ID = "BottomRightFolder";

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		layout.addView(NodesNavigator.ID, IPageLayout.LEFT, 0.3f, layout.getEditorArea());

		IFolderLayout rightFolderLayout = layout.createFolder(RIGHT_FOLDER_ID, IPageLayout.RIGHT,
				0.6f, layout.getEditorArea());
		
		IFolderLayout bottomRightFolderLayout=layout.createFolder(BOTTOM_RIGHT_FOLDER_ID, IPageLayout.BOTTOM, 0.5f, RIGHT_FOLDER_ID);
		
		rightFolderLayout.addView(NodeInfo.ID);
		bottomRightFolderLayout.addView(NodesSearcher.ID);
	}
}
