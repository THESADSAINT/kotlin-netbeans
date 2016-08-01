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
package org.black.kotlin.refactorings;

import org.black.kotlin.refactorings.rename.KotlinRenameRefactoring;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander.Baratynski
 */
@MimeRegistration(mimeType="text/x-kt",service=RefactoringPluginFactory.class)
public class KotlinRefactoringsFactory implements RefactoringPluginFactory {

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup lookup = refactoring.getRefactoringSource();
        
        return new KotlinRenameRefactoring(null);
    }
    
}
