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
package jbt.tools.bteditor.model;

import java.util.List;
import java.util.Vector;

/**
 * A tree-like structure used to organize {@link ConceptualBTNode}s in
 * categories.
 * <p>
 * This tree is composed of two types of nodes:
 * <ul>
 * <li> {@link CategoryItem}: represents categories. It can contain both
 * CategoryItem (in order to create a hierarchy of categories) and NodeItem
 * objects.
 * <li> {@link ConceptualBTNodeItem}: contains a ConceptualBTNode.
 * </ul>
 * 
 * This class defines a method, {@link #insertNode(String, ConceptualBTNodeItem)}, that is
 * used to insert nodes (CategoryItem and NodeItem) into the tree.
 * <p>
 * This tree may have several roots, since there may be many nodes at the top
 * level (that is, within no category). All these nodes are roots of the tree.
 * 
 * @author Ricardo Juan Palma Durán
 * 
 */
public class ConceptualNodesTree {
	/** Categories separator. */
	public static final String CATEGORY_SEPARATOR = new String("/");

	/** Roots of the tree. */
	protected List<NodesTreeItem> roots;

	/**
	 * Constructs an empty ConceptualNodesTree.
	 */
	public ConceptualNodesTree() {
		this.roots = new Vector<NodesTreeItem>();
	}

	/**
	 * Returns the roots of the tree.
	 */
	public List<NodesTreeItem> getRoots() {
		return roots;
	}

	/**
	 * Sets the roots of the tree.
	 */
	public void setRoots(List<NodesTreeItem> roots) {
		this.roots = roots;
	}

	/**
	 * Inserts a NodesTreeItem into the tree, at a particular position. The position
	 * is specified by the <code>category</code> parameter. This parameter is a
	 * String representing the category where the NodeItem must be inserted.
	 * <p>
	 * <code>category</code> can be null, in which case the node is inserted as
	 * a root of the tree. Otherwise, the category must follow this format:
	 * <p>
	 * <code>subCat1 + {@link #CATEGORY_SEPARATOR} + subCat2 + {@link #CATEGORY_SEPARATOR} + ... +
	 * {@link #CATEGORY_SEPARATOR} + subCatN</code>
	 * <p>
	 * If the specified category does not exist in the tree, it is created on
	 * the fly.
	 */
	public void insertNode(String category, NodesTreeItem node) {
		/* If the category is null, the node is inserted at the top level. */
		if (category == null) {
			node.setParent(null);
			this.roots.add(node);
			return;
		}
		/*
		 * Otherwise, follow the standard algorithm. First we extract each of
		 * the categories that the path where this node is going to be inserted
		 * is composed of.
		 */
		String[] subCategories = category.split(CATEGORY_SEPARATOR);
		/*
		 * Main loop. Here we find the CategoryItem where the NodeItem must be
		 * inserted. "currentItem" represents the CategoryItem we are looking
		 * for.
		 */
		CategoryItem currentItem = null;
		for (int i = 0; i < subCategories.length; i++) {
			/*
			 * At every level of the tree, we get the children of the current
			 * category, and check if there is one whose name matches
			 * "subCategories[i]". If there is such a category, we move on to
			 * it. Otherwise, we create a new category whose name is
			 * "subCategories[i]".
			 */
			List<NodesTreeItem> children = i == 0 ? this.roots : currentItem
					.getChildren();

			/* Check if there is a category matching the current one. */
			boolean subCategoryExists = false;
			for (int j = 0; j < children.size(); j++) {
				if (children.get(j) instanceof CategoryItem
						&& children.get(j).getName().equals(subCategories[i])) {
					/*
					 * If the category does exist, we move on to the next level
					 * of the tree.
					 */
					currentItem = (CategoryItem) children.get(j);
					subCategoryExists = true;
					break;
				}
			}
			/*
			 * If the category does not exist, it must be created. Then, move on
			 * to the next level of the tree.
			 */
			if (!subCategoryExists) {
				CategoryItem newCategory = new CategoryItem();
				newCategory.setName(subCategories[i]);
				if (i == 0) {
					newCategory.setParent(null);
					this.roots.add(newCategory);
				} else {
					newCategory.setParent(currentItem);
					currentItem.addChild(newCategory);
				}
				currentItem = newCategory;
			}
		}
		/*
		 * When we have reached the category where the node must be inserted, it
		 * is inserted into it.
		 */
		currentItem.addChild(node);
		node.setParent(currentItem);
	}

	/**
	 * A node of the {@link ConceptualNodesTree}.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static abstract class NodesTreeItem {
		protected NodesTreeItem parent;

		public NodesTreeItem getParent() {
			return parent;
		}

		public void setParent(NodesTreeItem parent) {
			this.parent = parent;
		}

		public abstract String getName();
	}

	/**
	 * A category of the {@link ConceptualNodesTree}.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class CategoryItem extends NodesTreeItem {
		private String name;
		private List<NodesTreeItem> children;

		public CategoryItem() {
			this.children = new Vector<NodesTreeItem>();
		}

		public void addChild(NodesTreeItem child) {
			this.children.add(child);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<NodesTreeItem> getChildren() {
			return this.children;
		}

		public int getNumChildren() {
			return this.children.size();
		}
	}

	/**
	 * A {@link ConceptualBTNode} of the {@link ConceptualNodesTree}.
	 * 
	 * @author Ricardo Juan Palma Durán
	 * 
	 */
	public static class ConceptualBTNodeItem extends NodesTreeItem {
		private ConceptualBTNode nodeModel;

		public ConceptualBTNodeItem(ConceptualBTNode nodeModel) {
			this.nodeModel = nodeModel;
		}

		public String getName() {
			return this.nodeModel.getReadableType();
		}

		public ConceptualBTNode getNodeModel() {
			return this.nodeModel;
		}
	}
}
