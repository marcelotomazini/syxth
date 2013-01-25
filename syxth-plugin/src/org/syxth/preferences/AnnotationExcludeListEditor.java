package org.syxth.preferences;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

public class AnnotationExcludeListEditor extends ListEditor {
	
	protected AnnotationExcludeListEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			result.append(items[i]).append(" \n");
		}
		return result.toString();
	}

	@Override
	protected String getNewInputObject() {
		InputDialog dialog = new InputDialog(getShell(), "Input Annotation", "Enter an annotation to be excluded:", "", new IInputValidator() {
			@Override public String isValid(String newText) {
				if(newText.startsWith("@"))
					return "Insert the annotation without @";

				return null;
			}
		});
		if (dialog.open() == Window.OK) {
			return dialog.getValue();
		}
		return null;
	}

	@Override
	protected String[] parseString(String stringList) {
		return stringList.split("\\s+");
	}
}
