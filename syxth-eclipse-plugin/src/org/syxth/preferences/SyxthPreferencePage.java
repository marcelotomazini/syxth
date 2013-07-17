package org.syxth.preferences;



import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.syxth.SyxthPlugin;

public class SyxthPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public SyxthPreferencePage() {
		super(GRID);
		setPreferenceStore(SyxthPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void createFieldEditors() {
		addField(new AnnotationExcludeListEditor(PreferenceConstants.P_ANNOTATION_EXCLUDES, "&Exclude from search the methods annotated with:", getFieldEditorParent()));		
		addField(new MethodExcludeListEditor(PreferenceConstants.P_METHOD_NAME_EXCLUDES, "&Exclude from search the methods named with:", getFieldEditorParent()));		
	}

	@Override
	public void init(IWorkbench workbench) {}
}