package org.syxth.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

public class SyxthTree {

	public SyxthTree(Collection<IMethod> deadMethods) {
		this.element = null;
		for (IMethod method : deadMethods) 
			add(method);
	}

	public IJavaElement element() {
		return element;
	}

	public void add(IMethod element) {
		SyxthTree item = findOrCreateNodeToAdd(element);
		item.items.add(new SyxthTree(element));
	}public List<SyxthTree> allItems() {
		return children(this);
	}

	@Override
	public String toString() {
		return element.getElementName();
	}

	private SyxthTree(IJavaElement element) {
		this.element = element;
	}
	
	private SyxthTree findOrCreateNodeToAdd(IMethod element) {
		IType declaringType = element.getDeclaringType();
		SyxthTree classItem = getItemFor(this, declaringType);
		if(classItem == null) {
			classItem = new SyxthTree(declaringType);
			IPackageFragment packageFragment = declaringType.getPackageFragment();
			SyxthTree packageItem = getItemFor(this, packageFragment);
			if(packageItem == null) {
				packageItem = new SyxthTree(packageFragment);
				items.add(packageItem);
			}
			packageItem.items.add(classItem);
		}
		
		return classItem;
	}

	private SyxthTree getItemFor(SyxthTree tree, IJavaElement element) {
		if(element == null) return null;
		
		SyxthTree treeItem = null;
		for (SyxthTree item : tree.items) {
			if(treeItem != null)
				return treeItem;
			if(element.equals(item.element()))
				return item;
			treeItem = getItemFor(item, element);
		}
		return treeItem;
	}
	
	private List<SyxthTree> children(SyxthTree syxthTree) {
		return syxthTree.items;
	}

	private final List<SyxthTree> items = new ArrayList<SyxthTree>();
	private final IJavaElement element;
}
