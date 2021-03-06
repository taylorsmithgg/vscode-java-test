/*******************************************************************************
* Copyright (c) 2018 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.test.plugin.util;

import com.microsoft.java.test.plugin.model.TestItem;
import com.microsoft.java.test.plugin.model.TestKind;
import com.microsoft.java.test.plugin.model.TestLevel;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.lsp4j.Range;

import java.util.Collections;

@SuppressWarnings("restriction")
public class TestItemUtils {

    public static final String DEFAULT_PACKAGE_NAME = "<Default Package>";

    public static TestItem constructTestItem(IJavaElement element, TestLevel level) throws JavaModelException {
        return constructTestItem(element, level, null);
    }

    public static TestItem constructTestItem(IJavaElement element, TestLevel level, TestKind kind)
            throws JavaModelException {
        final String displayName;
        final String fullName;
        if (element instanceof IPackageFragment && ((IPackageFragment) element).isDefaultPackage()) {
            displayName = DEFAULT_PACKAGE_NAME;
            fullName = DEFAULT_PACKAGE_NAME;
        } else {
            displayName = element.getElementName();
            fullName = parseTestItemFullName(element, level);
        }
        final String uri = JDTUtils.getFileURI(element.getResource());
        final Range range = parseTestItemRange(element);
        final String projectName = element.getJavaProject().getProject().getName();

        return new TestItem(displayName, fullName, uri, projectName, Collections.emptyList(), range, level, kind);
    }

    public static Range parseTestItemRange(IJavaElement element) throws JavaModelException {
        if (element instanceof ISourceReference) {
            final ISourceRange range = ((ISourceReference) element).getNameRange();
            return JDTUtils.toRange(element.getOpenable(), range.getOffset(), range.getLength());
        }
        return new Range();
    }

    private static String parseTestItemFullName(IJavaElement element, TestLevel level) {
        switch (level) {
            case CLASS:
                final IType type = (IType) element;
                return type.getFullyQualifiedName();
            case METHOD:
                final IMethod method = (IMethod) element;
                return method.getDeclaringType().getFullyQualifiedName() + "#" + method.getElementName();
            default:
                return element.getElementName();
        }
    }
}
