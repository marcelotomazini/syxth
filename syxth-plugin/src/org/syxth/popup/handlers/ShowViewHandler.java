package org.syxth.popup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.syxth.views.SyxthView;



public class ShowViewHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		view().showSearchResult(currentSelection);
		
		return null;
	}

	private SyxthView view() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		SyxthView result = null;
		try {
			result = (SyxthView)activePage.showView(SyxthView.SEARCH_VIEW_ID);
		} catch (PartInitException e) {
			throw new IllegalStateException(e);
		}

		activePage.activate(result);
		return result;
	}
}
