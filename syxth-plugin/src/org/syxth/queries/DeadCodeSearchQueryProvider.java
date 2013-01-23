package org.syxth.queries;

import org.eclipse.core.resources.IResource;
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search2.internal.ui.text2.DefaultTextSearchQueryProvider;

@SuppressWarnings("restriction")
public class DeadCodeSearchQueryProvider extends DefaultTextSearchQueryProvider {

	public ISearchQuery createQuery(String searchForString, boolean isRegEx) {
		FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(getFileNamePatterns(), true);
		return new FileSearchQuery(searchForString, isRegEx, true, scope);
	}

	public ISearchQuery createQuery(String searchForString, boolean isRegEx, IResource resource) {
		FileTextSearchScope scope = FileTextSearchScope.newSearchScope(new IResource[] { resource }, getFileNamePatterns(), true);
		return new FileSearchQuery(searchForString, isRegEx, true, scope);
	}
	
	private String[] getFileNamePatterns() {
		return new String[] { "*.java" };
	}
}
