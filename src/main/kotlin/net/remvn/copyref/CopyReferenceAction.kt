package net.remvn.copyref

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection
import kotlin.math.max
import kotlin.math.min

class CopyReferenceAction : AnAction() {
    /** Runs update checks in the background because they only inspect project and file data. */
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    /** Shows the action only when the current context contains a file inside the project. */
    override fun update(event: AnActionEvent) {
        val project = event.project
        val file = contextFile(event)
        event.presentation.isEnabledAndVisible =
            project != null && file != null && ReferenceFactory.projectRelativePath(project, file) != null
    }

    /** Creates a reference for the current file and editor position, then places it on the clipboard. */
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = contextFile(event) ?: return
        val editor = event.getData(CommonDataKeys.EDITOR)
        val reference = ReferenceFactory.create(project, file, editor) ?: return

        CopyPasteManager.getInstance().setContents(StringSelection(reference))
    }

    /** Prefers the editor's file and otherwise uses the only selected virtual file. */
    private fun contextFile(event: AnActionEvent): VirtualFile? {
        val editor = event.getData(CommonDataKeys.EDITOR)
        if (editor != null) {
            return FileDocumentManager.getInstance().getFile(editor.document)?.takeUnless { it.isDirectory }
        }

        return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
    }
}

internal object ReferenceFactory {
    fun create(project: Project, file: VirtualFile, editor: Editor?): String? {
        val path = projectRelativePath(project, file) ?: return null
        return create(path, editor)
    }

    fun create(path: String, editor: Editor?): String {
        val lines = editor?.caretModel?.primaryCaret?.let { caret ->
            lineRange(
                document = editor.document,
                caretOffset = caret.offset,
                selectionStart = caret.selectionStart.takeIf { caret.hasSelection() },
                selectionEnd = caret.selectionEnd.takeIf { caret.hasSelection() },
            )
        }

        return format(path, lines)
    }

    fun format(path: String, lines: IntRange?): String {
        val lineSuffix = when {
            lines == null -> ""
            lines.first == lines.last -> ":${lines.first}"
            else -> ":${lines.first}-${lines.last}"
        }
        return "`$path$lineSuffix`"
    }

    fun projectRelativePath(project: Project, file: VirtualFile): String? {
        val projectPath = project.basePath ?: return null
        return projectRelativePath(projectPath, file.path)
    }

    fun projectRelativePath(projectPath: String, filePath: String): String? =
        FileUtil.getRelativePath(projectPath, filePath, '/')
            ?.takeIf { it.isNotEmpty() && it != ".." && !it.startsWith("../") }

    fun lineRange(
        document: Document,
        caretOffset: Int,
        selectionStart: Int? = null,
        selectionEnd: Int? = null,
    ): IntRange {
        if (selectionStart == null || selectionEnd == null || selectionStart == selectionEnd) {
            return lineNumber(document, caretOffset)..lineNumber(document, caretOffset)
        }

        val startOffset = min(selectionStart, selectionEnd)
        val endOffsetExclusive = max(selectionStart, selectionEnd)
        val startLine = lineNumber(document, startOffset)
        val endLine = lineNumber(document, endOffsetExclusive - 1)
        return startLine..endLine
    }

    private fun lineNumber(document: Document, offset: Int): Int =
        document.getLineNumber(offset.coerceIn(0, document.textLength)) + 1
}
