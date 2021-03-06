/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.hints.intentions

import javax.swing.text.Document
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtDeclarationWithInitializer
import com.intellij.psi.PsiElement

class RemoveExplicitTypeIntention(doc: Document,
                                  analysisResult: AnalysisResult?,
                                  psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {

    override fun isApplicable(caretOffset: Int): Boolean {
        val element = psi.getNonStrictParentOfType(KtCallableDeclaration::class.java) ?: return false
        
        if (element.containingFile is KtCodeFragment) return false
        if (element.typeReference == null) return false

        val initializer = (element as? KtDeclarationWithInitializer)?.initializer
        if (initializer != null && initializer.textRange.containsOffset(caretOffset)) return false

        return when (element) {
            is KtProperty -> initializer != null
            is KtNamedFunction -> !element.hasBlockBody() && initializer != null
            is KtParameter -> element.isLoopParameter
            else -> false
        }
    }

    override fun getDescription() = "Remove explicit type specification"

    override fun implement() {
        val element = psi.getNonStrictParentOfType(KtCallableDeclaration::class.java) ?: return
        val anchor = getAnchor(element) ?: return

        val endOffset = anchor.textRange.endOffset
        val endOfType = element.typeReference!!.textRange.endOffset

        doc.remove(endOffset, endOfType - endOffset)
    }

}

fun getAnchor(element: KtCallableDeclaration): PsiElement? {
    return when (element) {
        is KtProperty, is KtParameter -> element.nameIdentifier
        is KtNamedFunction -> element.valueParameterList
        else -> null
    }
}
