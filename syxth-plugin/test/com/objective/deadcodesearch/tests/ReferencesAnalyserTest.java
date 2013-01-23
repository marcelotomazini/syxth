package com.objective.deadcodesearch.tests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.ui.search.JavaSearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.syxth.ReferencesAnalyser;

import com.objective.deadcodesearch.workspaceutils.JavaProject;



@SuppressWarnings("restriction")
public class ReferencesAnalyserTest {

	private JavaProject project;
	private JavaSearchResult subject;
	
	public static final String newLine = System.getProperty("line.separator");
	
	@Before public void beforeReferencesAnalysisTest() throws Exception { project = new JavaProject();}
	@After public void afterReferencesAnalysisTest() throws Exception { project.dispose(); }
	
	@Test
	public void methodWithReference() throws Exception {
		subject = searchMethodsReferencesFor(
				"class A { { new B().foo(); } }",
				"class B { void foo() {} }"
			);
		
		assertEmptySubject(subject.getElements());;
	}

	@Test
	public void methodWithoutReference() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { }");

		StringBuilder classB = new StringBuilder();
		classB.append("class B {" + newLine);
		classB.append("void foo() { }" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}
	
	@Test
	public void regExMethodSignature() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { }");

		StringBuilder classB = new StringBuilder();
		classB.append("class B { " + newLine);
		classB.append("void a() {}" + newLine);
		classB.append("public void b (){} " + newLine);
		classB.append("public void c (int $x1$_2x, int i2h$) { int x32 = $x1$_2x;}" + newLine);
		classB.append("public <T extends A> void d (String $x1$_2x, int i2h$, A[] array)  { }" + newLine);
		classB.append("public void e (String $x1$_2x, int i2h$, String[] array) { }" + newLine);
		classB.append("public void f (String $x1$_2x, int i2h$, boolean[] array) {}" + newLine);
		classB.append("}");

		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());

		assertSubjectContains(subject.getElements(), Arrays.asList("a", "b", "c", "d", "e", "f"));
	}
	
	@Test
	public void methodCommentedWithSlashAsterisk() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A {" + newLine);
		classA.append("/* foo() */" + newLine);
		classA.append("/* foo */" + newLine);
		classA.append(" int i = 0; /* foo() */" + newLine);
		classA.append("  /* dont used: foo(). Please, remove in future*/" + newLine);
		classA.append("  /* " + newLine);
		classA.append("  * foo() " + newLine);
		classA.append("  */" + newLine);
		classA.append("}");

		StringBuilder classB = new StringBuilder();
		classB.append("class B {" + newLine);
		classB.append("void foo() {}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}
	
	@Test
	public void methodCommentedWithSlashAsteriskAndLineWithoutAsterisk() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A {" + newLine);
		classA.append("  /* " + newLine);
		classA.append("  foo() " + newLine);
		classA.append("  */" + newLine);
		classA.append("}");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B {" + newLine);
		classB.append("void foo() {}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}
	
	@Test
	public void methodCommentedWithDoubleSlash() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A {" + newLine);
		classA.append("// foo() */" + newLine);
		classA.append("// foo " + newLine);
		classA.append(" int i = 0; // foo() " + newLine);
		classA.append("  // dont used: foo(). Please, remove in future" + newLine);
		classA.append("}");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B {" + newLine);
		classB.append("void foo() {}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}
	
	@Test
	public void methodInherited() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { protected void foo() {} }");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B extends A { @Override protected void foo() {} }");

		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo", "foo"));
	}

	@Test
	public void methodInheritedWithOneMethodWithoutReference() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { protected void foo() {} }");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B extends A { " + newLine);
		classB.append("       @Override protected void foo() { " + newLine);
		classB.append(" 		    super.foo();" + newLine);
		classB.append(" 	  } }");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}

	@Test
	public void methodWithDuplicateNames() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { void foo() {} }");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B { " + newLine);
		classB.append("		void foo() {" + newLine);
		classB.append("			A a = new A(); " + newLine);
		classB.append("			a.foo(); " + newLine);
		classB.append("		}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}

	@Test
	public void methodNameEqualsVariableName() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { void foo() {} }");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B { " + newLine);
		classB.append("		void foo() {" + newLine);
		classB.append("			A foo = new A(); " + newLine);
		classB.append("			foo.foo(); " + newLine);
		classB.append("		}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}

	@Test
	public void methodInvokedByPerformCall() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { " + newLine);
		classA.append("		void perform(String methodName) {}" + newLine);
		classA.append("		void foo() {}" + newLine);
		classA.append("}");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B { " + newLine);
		classB.append("		{" + newLine);
		classB.append("			A a = new A(); " + newLine);
		classB.append("			a.perform(\"foo\"); " + newLine);
		classB.append("		}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertEmptySubject(subject.getElements());
	}

	@Test
	public void methodInvokedByInvokeCall() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { " + newLine);
		classA.append("		void invoke(String methodName, Object[] args, Object[] argTypes) {}" + newLine);
		classA.append("		void foo() {}" + newLine);
		classA.append("}");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B { " + newLine);
		classB.append("		void foo() {" + newLine);
		classB.append("			A a = new A(); " + newLine);
		classB.append("			a.invoke(\"foo\", null, null); " + newLine);
		classB.append("		}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertEmptySubject(subject.getElements());
	}
	
	@Test
	public void methodInvokedByOnForActionCall() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { " + newLine);
		classA.append("		void on_for_action(String methodName, Object item, Object args) {}" + newLine);
		classA.append("		void buttonDoubleClicked(Object value) {}" + newLine);
		classA.append("		void buttonClicked() {}" + newLine);
		classA.append("}");
		
		StringBuilder classB = new StringBuilder();
		classB.append("class B { " + newLine);
		classB.append("		String y(String value) { return null; }" + newLine);
		classB.append("		Object receiver_send(Object receiver, String send) { return null; }" + newLine);
		classB.append("		void foo() {" + newLine);
		classB.append("			foo();" + newLine);
		classB.append("			A a = new A(); " + newLine);
		classB.append("			a.on_for_action(y(\"doubleClicked:\"), new Object(), receiver_send(this, y(\"buttonDoubleClicked:\")));" + newLine);
		classB.append("         a.on_for_action(y(\"clicked\"), new Object(), receiver_send(this, y(\"buttonClicked\")));" + newLine);
		classB.append("		}" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertEmptySubject(subject.getElements());
	}
	
	@Test
	public void methodProtectedWithoutReference() throws Exception {
		StringBuilder classA = new StringBuilder();
		classA.append("class A { " + newLine);
		classA.append("		/* " + newLine);
		classA.append("		   \"foo\" " + newLine);
		classA.append("		*/ " + newLine);
		classA.append("} ");

		StringBuilder classB = new StringBuilder();
		classB.append("class B {" + newLine);
		classB.append("protected void foo() { }" + newLine);
		classB.append("}");
		
		subject = searchMethodsReferencesFor(classA.toString(), classB.toString());
		assertSubjectContains(subject.getElements(), Arrays.asList("foo"));
	}

	private void assertEmptySubject(Object[] actual) {
		assertSubjectContains(actual, new ArrayList<String>());
	}
	
	private void assertSubjectContains(Object[] actual, List<String> expected) {
		List<String> actualMethods = new ArrayList<String>();
		for (Object method : actual)
			actualMethods.add(((IMethod)method).getElementName());
		
		Collections.sort(actualMethods);
		if (actualMethods.size() != expected.size() || !actualMethods.containsAll(expected))
			fail("Expected methods: " + expected + " but found: " + actualMethods);
	}
	
	private ICompilationUnit createCompilationUnit(String className, String code) throws CoreException {
		return project.createCompilationUnit("foopackage", className + ".java", "package foopackage; " + code);
	}
	
	private JavaSearchResult searchMethodsReferencesFor(String dependent, String provider) throws Exception {
		ICompilationUnit unit = createCompilationUnit("A", dependent);
		createCompilationUnit("B", provider);
		
		return searchMethodsReferences(unit.getParent());
	}
	
	private JavaSearchResult searchMethodsReferences(IJavaElement toAnalyse) throws CoreException {
		project.joinAutoBuild();
		assertBuildOK();
		
		ReferencesAnalyser r = new ReferencesAnalyser(toAnalyse);
		return r.performSearch();
	}
	
	private void assertBuildOK() throws CoreException {
		IMarker[] problems = project.getProject().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		for (IMarker problem : problems)
			if (problem.getAttribute(IMarker.SEVERITY).equals(IMarker.SEVERITY_ERROR))
				fail("" + problem.getAttribute(IMarker.MESSAGE));
	}
}
