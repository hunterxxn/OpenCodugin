package com.github.hunterxxn.opencodugin.commands

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class OpenCodeCliProviderTest : BasePlatformTestCase() {

    fun testDisplayName() {
        assertEquals("OpenCode", OpenCodeCliProvider.displayName)
    }

    fun testDefaultCommand() {
        assertEquals("opencode", OpenCodeCliProvider.defaultCommand)
    }

    fun testBuildFileReferenceWithoutEditor() {
        val file = myFixture.configureByText("Test.kt", "content").virtualFile
        val ref = OpenCodeCliProvider.buildFileReference(file, project, null)
        assertTrue("Should contain @ prefix", ref.contains("@"))
        assertTrue("Should contain filename", ref.contains("Test.kt"))
        assertTrue("Should have leading space", ref.startsWith(" "))
        assertTrue("Should have trailing space", ref.endsWith(" "))
        assertFalse("Should not have line ref without editor", ref.contains("#L"))
    }

    fun testBuildFileReferenceWithSubdirectory() {
        val file = myFixture.addFileToProject("src/main/Foo.kt", "content").virtualFile
        val ref = OpenCodeCliProvider.buildFileReference(file, project, null)
        assertTrue("Should contain @ prefix", ref.contains("@"))
        assertTrue("Should contain filename", ref.contains("Foo.kt"))
    }

    fun testBuildFileReferenceWithSingleLineSelection() {
        val file = myFixture.configureByText("Test.kt", "line1\nline2\nline3").virtualFile
        val editor = myFixture.editor
        val doc = editor.document
        WriteCommandAction.runWriteCommandAction(project) {
            val start = doc.getLineStartOffset(0)
            val end = doc.getLineEndOffset(0)
            editor.selectionModel.setSelection(start, end)
        }
        val ref = OpenCodeCliProvider.buildFileReference(file, project, editor)
        assertTrue("Should contain single line ref", ref.contains("#L1"))
        assertFalse("Should not be a range", ref.contains("#L1-"))
    }

    fun testBuildFileReferenceWithMultiLineSelection() {
        val file = myFixture.configureByText("Test.kt", "line1\nline2\nline3").virtualFile
        val editor = myFixture.editor
        val doc = editor.document
        WriteCommandAction.runWriteCommandAction(project) {
            val start = doc.getLineStartOffset(0)
            val end = doc.getLineEndOffset(1)
            editor.selectionModel.setSelection(start, end)
        }
        val ref = OpenCodeCliProvider.buildFileReference(file, project, editor)
        assertTrue("Should contain line range", ref.contains("#L1-2"))
    }
}
