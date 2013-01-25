package org.syxth;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchPattern;
import org.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.CuCollectingSearchRequestor;
import org.eclipse.jdt.internal.ui.search.JavaSearchResult;
import org.eclipse.jdt.internal.ui.search.JavaSearchScopeFactory;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.Match;
import org.syxth.queries.DeadCodeJavaSearchQuery;
import org.syxth.queries.DeadCodeSearchQueryProvider;


@SuppressWarnings("restriction")
public class ReferencesAnalyser {

	private IJavaElement subject;
	
	public ReferencesAnalyser(IJavaElement javaElement) {
		this.subject = javaElement;
	}
	
	public IJavaElement getSubject() {
		return subject;
	}

	private boolean hasReference(IMethod method) throws CoreException {
		SearchPattern pattern = JavaSearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
		IJavaSearchScope scope = JavaSearchScopeFactory.getInstance().createWorkspaceScope(true);
		CollectingSearchRequestor collector = new CuCollectingSearchRequestor();
		SearchParticipant[] participants = new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() };
		
		SearchEngine engine = new SearchEngine();
		engine.search(pattern, participants, scope, collector, null);
		
		return collector.getResults().size() > 0;
	}
	
	public JavaSearchResult performSearch() throws CoreException {
		DeadCodeJavaSearchQuery query = new DeadCodeJavaSearchQuery(subject);
		JavaSearchResult javaSearchResult = new JavaSearchResult(query);
		
		List<Match> matches = searchMatches();

		javaSearchResult.addMatches((Match[]) matches.toArray(new Match[matches.size()]));
		return javaSearchResult;
	}
	
	public List<Match> searchMatches() throws CoreException {
		return searchMatches(subject);
	}
	
	public List<Match> searchMatches(IJavaElement javaElement) throws CoreException {
		List<Match> matches = new ArrayList<Match>();
		try {
			if (javaElement instanceof IJavaProject || javaElement instanceof IPackageFragmentRoot)
				matches = performSearch((IParent)javaElement);
			if (javaElement instanceof IPackageFragment)
				matches = performSearch((IPackageFragment)javaElement);
			if (javaElement instanceof ICompilationUnit)
				matches = performSearch((ICompilationUnit)javaElement);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return matches;
	}
	
	public DeadCodeJavaSearchQuery getNewQuery() {
		return new DeadCodeJavaSearchQuery(subject);
	}
	
	private List<Match> performSearch(ICompilationUnit compilationUnit) throws CoreException {
		List<Match> matches = new ArrayList<Match>();
		IType type = compilationUnit.getAllTypes()[0];
		for (IMethod method : type.getMethods()) {
			if (ExclusionPatterns.ignoreMethod(method))
				continue;
			
			if (!hasReference(method) && !hasStringReference(method)) {
				ISourceRange nameRange = method.getNameRange();
				matches.add(new Match(method, nameRange.getOffset(), nameRange.getLength()));
			}
		}
		return matches;
	}
	
	private List<Match> performSearch(IParent parent) throws CoreException {
		List<Match> matches = new ArrayList<Match>();
		for (IJavaElement javaElement : parent.getChildren())
			matches.addAll(searchMatches(javaElement));
		return matches;
	}

	private List<Match> performSearch(IPackageFragment packageFragment) throws CoreException {
		List<Match> matches = new ArrayList<Match>();

		for (IPackageFragment subPackage : withSubpackages(packageFragment))
			for (ICompilationUnit compilationUnit : subPackage.getCompilationUnits())
				matches.addAll(performSearch(compilationUnit));
		
		return matches;
	}

	private boolean isProtected(IMethod method) throws JavaModelException {
		return method.getSource().startsWith("protected");
	}

	private boolean hasStringReference(IMethod method) throws JavaModelException {
		DeadCodeSearchQueryProvider provider = new DeadCodeSearchQueryProvider();
		
		String searchForRegex = "(\\W|^)\"" + method.getElementName() + "(\\W|$)";
		ISearchQuery query = isProtected(method) ? provider.createQuery(searchForRegex, true, method.getResource()) : 
								provider.createQuery(searchForRegex, true);
		query.run(null);
		FileSearchResult searchResult = (FileSearchResult) query.getSearchResult();
		
		if(searchResult.getElements().length > 0)
			return true;
		
		return false;
	}
	
	private List<IPackageFragment> withSubpackages(IPackageFragment packge) throws JavaModelException {
		IJavaElement[] allPackages = ((IPackageFragmentRoot)packge.getParent()).getChildren();
		List<IPackageFragment> ret = new ArrayList<IPackageFragment>();
		for (IJavaElement candidate : allPackages)
			if (candidate.getElementName().startsWith(packge.getElementName()))
				ret.add((IPackageFragment)candidate);
		
		return ret;
	}
}
