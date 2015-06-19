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

package org.jetbrains.webdemo.examples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;

import java.util.ArrayList;
import java.util.List;

public class Example extends Project{
    private List<ProjectFile> hiddenFiles = new ArrayList<>();
    @Nullable
    private Example previousExample;
    @JsonIgnore
    public ExamplesFolder parent;

    public Example(
            String id,
            String name,
            String args,
            String confType,
            String originUrl,
            String expectedOutput,
            List<ProjectFile> files,
            List<ProjectFile> hiddenFiles,
            List<String> readOnlyFileNames,
            @Nullable Example previousExample
    ){
        super(id, name, args, confType, originUrl, expectedOutput, files, readOnlyFileNames);
        this.hiddenFiles = hiddenFiles;
        this.previousExample = previousExample;
    }

    @JsonIgnore
    public List<ProjectFile> getHiddenFiles(){
        return hiddenFiles;
    }

    @Nullable
    @JsonIgnore
    public Example getPreviousExample() {
        return previousExample;
    }

}
