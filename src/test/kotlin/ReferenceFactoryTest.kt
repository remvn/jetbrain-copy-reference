package net.remvn.copyref

import com.intellij.openapi.editor.impl.DocumentImpl
import junit.framework.TestCase

class ReferenceFactoryTest : TestCase() {
    fun testCreatesReferenceForCaretLine() {
        assertEquals("@src/main/kotlin/Main.kt:2", ReferenceFactory.format("src/main/kotlin/Main.kt", 2..2))
    }

    fun testCreatesReferenceForSingleSelectedLine() {
        assertEquals("@Main.kt:2", ReferenceFactory.format("Main.kt", 2..2))
    }

    fun testCreatesReferenceForSelectedLineRange() {
        assertEquals("@Main.kt:1-3", ReferenceFactory.format("Main.kt", 1..3))
    }

    fun testSelectionEndingAtNextLineStartExcludesNextLine() {
        val document = DocumentImpl("first\nsecond\nthird")

        assertEquals(
            1..2,
            ReferenceFactory.lineRange(document, 0, 0, document.getLineStartOffset(2)),
        )
    }

    fun testReverseSelectionOffsetsProduceAscendingRange() {
        val document = DocumentImpl("first\nsecond\nthird")

        assertEquals(
            1..3,
            ReferenceFactory.lineRange(document, 0, document.textLength, 0),
        )
    }

    fun testCaretWithoutSelectionUsesCaretLine() {
        val document = DocumentImpl("first\nsecond\nthird")

        assertEquals(
            2..2,
            ReferenceFactory.lineRange(document, document.getLineStartOffset(1)),
        )
    }

    fun testCreatesProjectViewReferenceWithoutLines() {
        assertEquals("@src/Main.kt", ReferenceFactory.format("src/Main.kt", null))
    }

    fun testCreatesProjectViewDirectoryReference() {
        assertEquals("@src/main/kotlin", ReferenceFactory.format("src/main/kotlin", null))
    }

    fun testCreatesProjectRelativePathWithForwardSlashes() {
        assertEquals(
            "src/Main.kt",
            ReferenceFactory.projectRelativePath("C:/project", "C:/project/src/Main.kt"),
        )
    }

    fun testRejectsFileOutsideProject() {
        assertNull(ReferenceFactory.projectRelativePath("C:/project", "C:/other/Main.kt"))
    }
}
