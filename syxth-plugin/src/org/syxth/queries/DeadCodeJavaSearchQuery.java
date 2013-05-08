package org.syxth.queries;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.search.JavaSearchQuery;
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;

@SuppressWarnings("restriction")
public class DeadCodeJavaSearchQuery extends JavaSearchQuery {

	public DeadCodeJavaSearchQuery(IJavaElement javaElement) {
		super(new ElementQuerySpecification(javaElement, IJavaSearchConstants.REFERENCES, JavaSearchScopeFactory.getInstance().createWorkspaceScope(false), JavaSearchScopeFactory.getInstance().getWorkspaceScopeDescription(false)));
	}

	@Override
	public String getResultLabel(int nMatches) {
		return "Dead code search. Found " + nMatches + " methods without reference.";
	}

}
