package com.objective.deadcodesearch.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.search.JavaSearchResult;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

import com.objective.deadcodesearch.ReferencesAnalyser;


@SuppressWarnings("restriction")
public class DeadCodeSearchView extends SearchView {
	
	public static final String SEARCH_VIEW_ID = "com.objective.deadcodesearch.views.DeadCodeSearchView";
	
	private SearchJob searchJob;

	public void showSearchResult(ISelection selection) {
		showSearchResult(asJavaElement(selection));
	}
	
	private void showSearchResult(IJavaElement javaElement) {
		if (javaElement == null) return;
		
		showSearchResult(new ReferencesAnalyser(javaElement));
	}
	
	private void showSearchResult(final ReferencesAnalyser r) {
		final JavaSearchResult javaSearchResult = new JavaSearchResult(r.getNewQuery());
		(new Job("Analysing '" + r.getSubject().getElementName() + "'") { @Override protected IStatus run(IProgressMonitor monitor) {
			try {
				List<Match> matches = r.searchMatches();
				javaSearchResult.addMatches((Match[]) matches.toArray(new Match[matches.size()]));
				
				searchJob.schedule();
			} catch (Exception x) {
				return UIJob.errorStatus(x);
			}
			return Status.OK_STATUS;
		}}).schedule();
		showSearchResult(javaSearchResult);
	}

	private IJavaElement asJavaElement(ISelection candidate) {
		if (!(candidate instanceof IStructuredSelection)) return null;

		Object firstElement = ((IStructuredSelection)candidate).getFirstElement();
		if (!(firstElement instanceof IJavaElement)) return null;

		return (IJavaElement)firstElement;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		searchJob = new SearchJob(parent);
	}
		
	private final class SearchJob extends UIJob {
		
		private SearchJob(Composite parent) {
			super("Dead search code job");
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled()) return Status.OK_STATUS;
			this.schedule(1);

			return Status.OK_STATUS;
		}
	}
}
