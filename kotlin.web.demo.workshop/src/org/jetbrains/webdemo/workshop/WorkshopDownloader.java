/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
 */

package org.jetbrains.webdemo.workshop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WorkshopDownloader {
    private static String remoteURL = "https://github.com/JetBrains/workshop-jb";
    private static String workshopPath = "dependencies" + File.separator + "workshop";

    public static void main(String[] args) throws GitAPIException, IOException {
        File workshopDirectory = new File(workshopPath);
//        Git.cloneRepository()
//                .setURI(remoteURL)
//                .setDirectory(workshopDirectory)
//                .call();
        File manifest = new File(workshopPath + File.separator + ".webdemo" + File.separator + "manifest.json");
        if (manifest.exists()) {
            WorkshopFolder rootFolder = new ObjectMapper().readValue(manifest, WorkshopFolder.class);
            FileUtils.delete(new File("examples" + File.separator + "Workshop"), FileUtils.RECURSIVE);
            processWorkshopFolder(rootFolder, "/");
        }
    }


    private static void processWorkshopFolder(WorkshopFolder workshopFolderManifest, String path) throws IOException {
        WebDemoFolder webDemoFolderManifest = new WebDemoFolder();
        webDemoFolderManifest.sequential = workshopFolderManifest.sequential;

        File folder = new File("examples" + path + File.separator + workshopFolderManifest.name);
        File manifestFile = new File(folder.getPath() + File.separator + "manifest.json");
        folder.mkdir();

        if (workshopFolderManifest.folders != null) {
            webDemoFolderManifest.folders = new ArrayList<>();
            for (WorkshopFolder childFolder : workshopFolderManifest.folders) {
                webDemoFolderManifest.folders.add(childFolder.name);
                processWorkshopFolder(childFolder, path + File.separator + workshopFolderManifest.name);
            }
        }

        webDemoFolderManifest.examples = processWorkshopProjects(folder, workshopFolderManifest.projects);

        webDemoFolderManifest.files = copyFiles(folder, workshopFolderManifest.commonFiles);

        new ObjectMapper().writeValue(manifestFile, webDemoFolderManifest);
    }

    private static List<String> processWorkshopProjects(File parentFolder, List<WorkshopProject> projects) throws IOException {
        if(projects == null) return null;
        List<String> projectNames = new ArrayList<>();
        for(WorkshopProject project : projects){
            File projectFolder = new File(parentFolder.getAbsolutePath() + File.separator + project.name);
            projectFolder.mkdir();
            File manifestFile = new File(projectFolder.getAbsolutePath() + File.separator + "manifest.json");
            WebDemoProject webDemoProjectManifest = new WebDemoProject();
            webDemoProjectManifest.name = project.name;
            webDemoProjectManifest.files = copyFiles(projectFolder, project.files);
            projectNames.add(project.name);
            new ObjectMapper().writeValue(manifestFile, webDemoProjectManifest);
        }
        return projectNames;
    }

    private static List<WebDemoFile> copyFiles(File destinationDirectory, List<WorkshopFile> files) throws IOException {
        if (files == null) return null;

        List<WebDemoFile> webDemoFiles = new ArrayList<>();
        for (WorkshopFile file : files) {
            Path destinationPath = Paths.get(destinationDirectory.getAbsolutePath() + File.separator + file.name);
            String folderName = file.type.equals("test") ? "test" : "src";
            Path sourcePath = Paths.get(workshopPath + File.separator + folderName + File.separator + file.path);
            Files.copy(sourcePath, destinationPath);

            if (file.type.equals("test")) {
                webDemoFiles.add(new WebDemoFile(false, file.hidden, file.name, "kotlin-test", false));
            } else if (file.type.equals("task")) {
                webDemoFiles.add(new WebDemoFile(true, file.hidden, file.name, null, false));
            } else if (file.type.equals("javaCode")) {
                webDemoFiles.add(new WebDemoFile(false, file.hidden, file.name, "java", true));
            } else if (file.type.equals("utils")) {
                webDemoFiles.add(new WebDemoFile(false, file.hidden, file.name, null, false));
            }
        }

        return webDemoFiles;
    }

}

class WorkshopFolder {
    public String name;
    public boolean sequential;
    public List<WorkshopFolder> folders;
    public List<WorkshopProject> projects;
    public List<WorkshopFile> commonFiles;

    WorkshopFolder() {

    }
}

class WorkshopProject {
    public String name;
    public List<WorkshopFile> files;
}

class WorkshopFile {
    public String name;
    public String path;
    public boolean hidden;
    public String type;
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class WebDemoFolder {
    public boolean sequential;
    public List<String> folders;
    public List<String> examples;
    public List<WebDemoFile> files;
}

class WebDemoProject {
    public String name;
    public String args = "";
    public String confType = "junit";
    public String help = "";
    public List<WebDemoFile> files = new ArrayList<>();
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class WebDemoFile {
    public boolean modifiable;
    public boolean hidden;
    public boolean skipInTestVersion;
    public String filename;
    public String type;

    public WebDemoFile(boolean modifiable, boolean hidden, String filename, String type, boolean skipInTestVersion) {
        this.modifiable = modifiable;
        this.hidden = hidden;
        this.filename = filename;
        this.type = type;
        this.skipInTestVersion = skipInTestVersion;
    }
}
