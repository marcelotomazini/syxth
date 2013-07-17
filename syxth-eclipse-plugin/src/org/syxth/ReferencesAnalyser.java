package org.syxth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.syxth.queries.DeadCodeJavaSearchQuery;
import org.syxth.queries.DeadCodeSearchQueryProvider;

@SuppressWarnings("restriction")
public class ReferencesAnalyser {

	public ReferencesAnalyser(IJavaElement javaElement) {
		this.subject = javaElement;
	}

	public void analyse(IProgressMonitor monitor) {
		try {
			searchMethodsToAnalyse();

			runQueries(monitor);
			selectMethodsByQuantityOfReferences(0);

			Map<ISearchQuery, IMethod> otherMap = new HashMap<ISearchQuery, IMethod>(queries);
			queries.clear();
			for (Map.Entry<ISearchQuery, IMethod> entry : otherMap.entrySet()) {
				String searchForRegex = "(\\W|^)" + entry.getValue().getElementName() + "(\\W|$)";
				queries.put(provider.createQuery(searchForRegex), entry.getValue());
			}

			runQueries(monitor);
			removeResultsInComments();
			selectMethodsByQuantityOfReferences(1);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void removeResultsInComments() {
		for (ISearchQuery query : new HashSet<ISearchQuery>(queries.keySet())) {
			Pattern slashStarComment = Pattern.compile(".*/\\*.*" + queries.get(query).getElementName() + ".*\\*/.*");
			Pattern doubleSlashComment = Pattern.compile(".*//.*" + queries.get(query).getElementName() + ".*");

			AbstractTextSearchResult searchResult = (AbstractTextSearchResult) query.getSearchResult();
			for (Object element : searchResult.getElements())
				for (Match match : searchResult.getMatches(element)) {
					String contents = ((FileMatch) match).getLineElement().getContents();
					if (slashStarComment.matcher(contents).matches() || doubleSlashComment.matcher(contents).matches() || contents.trim().startsWith("*"))
						searchResult.removeMatch(match);
				}
		}
	}

	public Collection<IMethod> deadMethods() {
		return queries.values();
	}

	public void cancel() {
		queries.clear();
	}

	@Override
	public String toString() {
		return queries.size() + " methods unused in " + subject.getElementName();
	}

	private void searchMethodsToAnalyse() throws CoreException {
		for (IMethod method : searchMethodsToAnalyse(subject))
			queries.put(new DeadCodeJavaSearchQuery(method), method);
	}

	private List<IMethod> searchMethodsToAnalyse(IJavaElement javaElement) throws CoreException {
		List<IMethod> matches = new ArrayList<IMethod>();
		try {
			if (javaElement instanceof IJavaProject || javaElement instanceof IPackageFragmentRoot)
				matches = searchMethodsToAnalyse((IParent) javaElement);
			if (javaElement instanceof IPackageFragment)
				matches = searchMethodsToAnalyse((IPackageFragment) javaElement);
			if (javaElement instanceof ICompilationUnit)
				matches = searchMethodsToAnalyse((ICompilationUnit) javaElement);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		return matches;
	}

	private List<IMethod> searchMethodsToAnalyse(ICompilationUnit compilationUnit) throws CoreException {
		List<IMethod> matches = new ArrayList<IMethod>();
		IType type = compilationUnit.getAllTypes()[0];
		for (IMethod method : type.getMethods()) {
			if (ExclusionPatterns.ignoreMethod(method))
				continue;

			matches.add(method);
		}
		return matches;
	}

	private List<IMethod> searchMethodsToAnalyse(IParent parent) throws CoreException {
		List<IMethod> matches = new ArrayList<IMethod>();
		for (IJavaElement javaElement : parent.getChildren())
			matches.addAll(searchMethodsToAnalyse(javaElement));
		return matches;
	}

	private List<IMethod> searchMethodsToAnalyse(IPackageFragment packageFragment) throws CoreException {
		List<IMethod> matches = new ArrayList<IMethod>();

		for (IPackageFragment subPackage : withSubpackages(packageFragment))
			for (ICompilationUnit compilationUnit : subPackage.getCompilationUnits())
				matches.addAll(searchMethodsToAnalyse(compilationUnit));

		return matches;
	}

	private List<IPackageFragment> withSubpackages(IPackageFragment packge) throws JavaModelException {
		IJavaElement[] allPackages = ((IPackageFragmentRoot) packge.getParent()).getChildren();
		List<IPackageFragment> ret = new ArrayList<IPackageFragment>();
		for (IJavaElement candidate : allPackages)
			if (candidate.getElementName().startsWith(packge.getElementName()))
				ret.add((IPackageFragment) candidate);

		return ret;
	}

	private void selectMethodsByQuantityOfReferences(int quantity) {
		for (ISearchQuery query : new HashSet<ISearchQuery>(queries.keySet()))
			if (((AbstractTextSearchResult) query.getSearchResult()).getMatchCount() > quantity)
				queries.remove(query);
	}

	private void runQueries(IProgressMonitor monitor) {
		for (ISearchQuery query : queries.keySet())
			query.run(monitor);
	}

	private final IJavaElement subject;
	private final DeadCodeSearchQueryProvider provider = new DeadCodeSearchQueryProvider();
	private final Map<ISearchQuery, IMethod> queries = new HashMap<ISearchQuery, IMethod>();
}
