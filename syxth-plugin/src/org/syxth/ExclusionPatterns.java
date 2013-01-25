package org.syxth;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.syxth.exceptions.SyxthException;
import org.syxth.preferences.PreferenceConstants;

public class ExclusionPatterns {

	public static boolean ignoreMethod(IMethod method) {
		try {
			List<String> excludedAnnotations = getExcludedAnnotations();
			for (IAnnotation annotation : method.getAnnotations())
				if(excludedAnnotations.contains(annotation.getElementName()))
					return true;
			return false;
		} catch (JavaModelException e) {
			throw new SyxthException("Error trying to get annotations from method " + method.getElementName());
		}
	}
	
	static private List<String> getExcludedAnnotations() {
		return Arrays.asList(SyxthPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.P_ANNOTATION_EXCLUDES).split("\\s+"));
	}

}
