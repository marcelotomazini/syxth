package org.syxth.views;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class SyxthLabelProvider extends LabelProvider {	
	
	@Override
	public Image getImage(Object element) {
		IJavaElement javaElement = ((SyxthTree) element).element();
		
		if (javaElement instanceof IPackageFragment)
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_PACKAGE);

		if (javaElement instanceof IType)
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CLASS);
		
		if (javaElement instanceof IMethod) {
			try {
				int flags = ((IMethod)javaElement).getFlags();
				if(Flags.isPrivate(flags))
					return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);
				if(Flags.isProtected(flags))
					return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);
				if(Flags.isPublic(flags))
					return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException("Unknown type of element in tree of type " + javaElement.getElementName());
	}

	@Override
	public String getText(Object element) {
		return element.toString();
	}

}
