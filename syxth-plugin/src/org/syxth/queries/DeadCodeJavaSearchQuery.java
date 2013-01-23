package org.syxth.queries;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.search.JavaSearchQuery;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;

@SuppressWarnings("restriction")
public class DeadCodeJavaSearchQuery extends JavaSearchQuery {

	public DeadCodeJavaSearchQuery(IJavaElement javaElement) {
		super(new ElementQuerySpecification(javaElement, 0, null, null));
	}

	@Override
	public String getResultLabel(int nMatches) {
		return "Dead code search. Found " + nMatches + " methods without reference.";
	}

}
