package org.syxth.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.syxth.ReferencesAnalyser;



public class SyxthView extends ViewPart {

	public void showSearchResult(ISelection selection) {
		showSearchResult(asJavaElement(selection));
	}
	
	private void showSearchResult(IJavaElement javaElement) {
		if (javaElement == null) return;
		
		referencesAnalyser = new ReferencesAnalyser(javaElement);
	
		Job job= new SyxthJob();
		job.setPriority(Job.BUILD);
		job.setUser(true);
		job.addJobChangeListener(new IJobChangeListener() {
			@Override public void aboutToRun(IJobChangeEvent event) {}
			@Override public void awake(IJobChangeEvent event) {}
			@Override public void running(IJobChangeEvent event) {}
			@Override public void scheduled(IJobChangeEvent event) {}
			@Override public void sleeping(IJobChangeEvent event) {}
			@Override public void done(IJobChangeEvent event) {
				if(event.getResult().equals(Status.CANCEL_STATUS))
					referencesAnalyser.cancel();

				Display.getDefault().syncExec(new Runnable() { 
					@Override public void run() { 
						treeViewer.setInput(new SyxthTree(referencesAnalyser.deadMethods()));
						SyxthView.this.setContentDescription(referencesAnalyser.toString());
					}
				});
			}
		});
		job.schedule();
	}
	
	private IJavaElement asJavaElement(ISelection candidate) {
		if (!(candidate instanceof IStructuredSelection)) return null;

		Object firstElement = ((IStructuredSelection)candidate).getFirstElement();
		if (!(firstElement instanceof IJavaElement)) return null;

		return (IJavaElement)firstElement;
	}
	
	
	private class SyxthJob extends Job {

		public SyxthJob() {
			super("Searching for dead code");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if(monitor.isCanceled()) return Status.CANCEL_STATUS;
			referencesAnalyser.analyse(monitor);
			return Status.OK_STATUS;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		treeViewer = new TreeViewer(parent, 0);
		treeViewer.setContentProvider(new SyxthContentProvider());
		treeViewer.setLabelProvider(new SyxthLabelProvider());
		treeViewer.setUseHashlookup(true);
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IJavaElement element = ((SyxthTree) ((TreeSelection)event.getSelection()).getFirstElement()).element();
				try {
					JavaUI.openInEditor(element);
				} catch (PartInitException e) {
					throw new RuntimeException(e);
				} catch (JavaModelException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@Override
	public void setFocus() {}
		
	private TreeViewer treeViewer;
	private ReferencesAnalyser referencesAnalyser;
	public static final String SEARCH_VIEW_ID = "org.syxth.views.SyxthView";
}
