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

import utils.Listenable
import utils.VarListener
import utils.addKotlinExtension
import java.util.*


class UserProject(
        type: ProjectType,
        id: String,
        name: String,
        parent: Folder,
        onFileDeleted: (String) -> Unit,
        onContentLoaded: (ArrayList<File>) -> Unit,
        onContentNotFound: () -> Unit,
        val onFileAdded: (File) -> Unit
) : Project(type, id, name, parent, onFileDeleted, onContentLoaded, onContentNotFound) {
    val nameListener = VarListener<String>()
    override var name by Listenable(name, nameListener)

    fun addEmptyFile(name: String, publicId: String): File {
        var file = File(this, name, publicId)
        file.listenableIsModified.addModifyListener {onModified()}
        files.add(file)
        onFileAdded(file)
        return file
    }


    fun addFileWithMain(name: String, publicId: String): File {
        var file = File(
                this,
                addKotlinExtension(name),
                publicId,
                "fun main(args: Array<String>) {\n\n}"
        )
        file.listenableIsModified.addModifyListener {onModified()}
        files.add(file)
        onFileAdded(file)
        return file
    }
}
