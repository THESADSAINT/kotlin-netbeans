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
package org.black.kotlin.projectsextensions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.projectsextensions.j2se.classpath.J2SEExtendedClassPathProvider;
import org.black.kotlin.project.KotlinSources;
import org.black.kotlin.projectsextensions.maven.MavenHelper;
import org.black.kotlin.projectsextensions.maven.classpath.MavenExtendedClassPath;
import org.black.kotlin.projectsextensions.maven.classpath.MavenClassPathProviderImpl;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinProjectHelper {
    
    public static KotlinProjectHelper INSTANCE = new KotlinProjectHelper();
    
    private KotlinProjectHelper(){}
    
    private final Map<Project, KotlinSources> kotlinSources = new HashMap<Project, KotlinSources>();
    private final Map<Project, FileObject> lightClassesDirs = new HashMap<Project, FileObject>();
    private final Map<Project, ClassPathExtender> extendedClassPaths = new HashMap<Project, ClassPathExtender>();
    
    public boolean checkProject(Project project){
        if ((project instanceof J2SEProject) || (project instanceof NbMavenProjectImpl)){
             return true;
         }
        
        return false;
    }
    
    public KotlinSources getKotlinSources(Project project){
        Project p = project;
        
        if (!(checkProject(p))){
            return null;
        }
        
//        if (p instanceof NbMavenProjectImpl) {
//            try {
//                p = MavenHelper.getMainParent((NbMavenProjectImpl) p);
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
        
        if (!kotlinSources.containsKey(p)) {
            kotlinSources.put(p, new KotlinSources(p));
        }
        
        return kotlinSources.get(p);
    }
    
    public FileObject getLightClassesDirectory(Project project){
        Project p = project;
        
        if (!(checkProject(p))){
            return null;
        }
        
        if (p instanceof NbMavenProjectImpl) {
            try {
                p = MavenHelper.getMainParent((NbMavenProjectImpl) p);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        if (!(lightClassesDirs.containsKey(p))) {
            lightClassesDirs.put(p, setLightClassesDir(p));
        }
        
        return lightClassesDirs.get(p);
    }
    
    private FileObject setLightClassesDir(Project project){
        if (Places.getUserDirectory() == null){
            return project.getProjectDirectory();
        }
        FileObject userDirectory = FileUtil.toFileObject(Places.getUserDirectory());
        String projectName = project.getProjectDirectory().getName();
        if (userDirectory.getFileObject(projectName) == null){
            try {
                userDirectory.createFolder(projectName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return userDirectory.getFileObject(projectName);
    }
    
    public ClassPathExtender getExtendedClassPath(Project project) {
        Project p = project;
        
        if (!(checkProject(p))){
            return null;
        }
        
        if (!extendedClassPaths.containsKey(p)){
            if (p instanceof J2SEProject) {
                extendedClassPaths.put(p, new J2SEExtendedClassPathProvider(p));
            }
            if (p instanceof NbMavenProjectImpl) {
                extendedClassPaths.put(p, new MavenExtendedClassPath((NbMavenProjectImpl) p));

            }
        }
        
        return extendedClassPaths.get(p);
    }

    public void updateExtendedClassPath(Project project) {
        Project p = project;
        if (p instanceof J2SEProject) {
            extendedClassPaths.put(p, new J2SEExtendedClassPathProvider(p));
        }
        if (p instanceof NbMavenProjectImpl) {
            MavenClassPathProviderImpl impl = p.getLookup().lookup(MavenClassPathProviderImpl.class);
            if (impl != null) {
                impl.updateClassPath();
            }
            extendedClassPaths.put(p, new MavenExtendedClassPath((NbMavenProjectImpl) p));
        }
        NetBeansJavaProjectElementUtils.updateClasspathInfo(p);
        KotlinEnvironment.updateKotlinEnvironment(project);
    }
    
}
