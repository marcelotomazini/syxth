package org.syxth.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class SyxthContentProvider implements ITreeContentProvider {
	
	protected TreeViewer viewer;
	
	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		return ((SyxthTree) parentElement).allItems().toArray();
	}
	
	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

}
