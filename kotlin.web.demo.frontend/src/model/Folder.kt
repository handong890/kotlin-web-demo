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

package model

import views.tree.ProjectHeader
import kotlin.browser.localStorage

open class Folder(
        val name: String,
        val id: String,
        val projects: List<ProjectHeader>,
        val childFolders: List<Folder>
){
    var parent: Folder? = null
        private  set

    init{
        childFolders.forEach { it.parent = this }
    }
}

class TasksFolder(
        name: String,
        id: String,
        val isSequential: Boolean,
        projects: List<ProjectHeader>,
        childFolders: List<Folder>
) : Folder(name, id, projects, childFolders)
