package org.syxth.queries;

import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search2.internal.ui.text2.DefaultTextSearchQueryProvider;

@SuppressWarnings("restriction")
public class DeadCodeSearchQueryProvider extends DefaultTextSearchQueryProvider {

	@Override
	public ISearchQuery createQuery(String searchForString) {
		FileTextSearchScope scope = FileTextSearchScope.newWorkspaceScope(getFileNamePatterns(), true);
		return new FileSearchQuery(searchForString, true, true, scope);
	}

	private String[] getFileNamePatterns() {
		return new String[] { "*.java" };
	}
}
