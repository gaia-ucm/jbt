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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jbt.tools.bteditor.util.IconsPaths;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Class used to manage common icons in the application. These include icons
 * for standard nodes (those read form the
 * {@link NodesLoader#STANDARD_NODES_FILE} file), as well for "actions",
 * "conditions", "categories" and "guards" (whose path can be accessed via
 * {@link IconsPaths}).
 * <p>
 * They can be retrieved by their path in the plugin, and they are
 * automatically disposed by the application.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ApplicationIcons {
	/** List of the icons. Can be retrieved by their path in the plugin. */
	private static Map<String, Image> applicationIcons = new Hashtable<String, Image>();

	/**
	 * Returns an icon by its path in the plugin, or null if it cannot be
	 * found.
	 * <p>
	 * The returned image does not have to be disposed. It is automatically
	 * managed by the application.
	 * <p>
	 * This function should be called after loading the icons (
	 * {@link #loadIcons()}).
	 */
	public static Image getIcon(String iconLocation) {
		return applicationIcons.get(iconLocation);
	}

	/**
	 * Loads all the common standard icons.
	 * 
	 * @throws IOException
	 *             in case there is an error while loading the icons.
	 */
	public static void loadIcons() throws IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(
				NodesLoader.STANDARD_NODES_FILE), Collections.EMPTY_MAP);

		URL fileUrl = null;
		fileUrl = FileLocator.toFileURL(url);
		FileInputStream file = new FileInputStream(fileUrl.getPath());

		/* First get icons from the standard nodes file. */
		parseStandardNodesFile(file);

		/* Then non standard icons. */
		loadNonStandardIcons();
	}

	private static List<Exception> parseStandardNodesFile(FileInputStream file) {
		List<Exception> exceptions = new Vector<Exception>();

		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(file);

			Element root = doc.getRootElement();

			parseElement(root);
		} catch (Exception e) {
			exceptions.add(e);
		}

		return exceptions;
	}

	private static void parseElement(Element e) {
		String nodeType = e.getName();

		if (nodeType.equals("Category")) {
			List<Element> children = e.getChildren();

			for (Element child : children) {
				parseElement(child);
			}
		} else if (nodeType.equals("Node")) {
			String path = e.getChildText("Icon");
			loadIcon(path);
		}
	}

	private static void loadNonStandardIcons() {
		loadIcon(IconsPaths.ACTION);
		loadIcon(IconsPaths.CONDITION);
		loadIcon(IconsPaths.ROOT);
		loadIcon(IconsPaths.CATEGORY);
		loadIcon(IconsPaths.GUARD);
		loadIcon(IconsPaths.BT);
	}

	/**
	 * Loads an icon into {@link #applicationIcons} from an icon path.
	 */
	private static void loadIcon(String iconPath) {
		ImageDescriptor imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, iconPath);
		Image icon = imageDescriptor == null ? null : imageDescriptor.createImage();

		applicationIcons.put(iconPath, icon);
	}

	/**
	 * Disposes all the icons stored by the ApplicationIcons.
	 */
	public static void disposeIcons() {
		Collection<Image> icons = applicationIcons.values();
		for (Image icon : icons) {
			if (icon != null) {
				icon.dispose();
			}
		}
	}

	private ApplicationIcons() {
	};
}
