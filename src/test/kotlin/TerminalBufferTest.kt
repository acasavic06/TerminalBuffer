package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TerminalBufferTest {

    @Test
    fun testWriteTextAndGetLine() {
        val buffer = TerminalBuffer(width = 10, height = 5, scrollbackMax = 10)

        buffer.writeText("Hello")

        val line0 = buffer.getLine(0)

        assertTrue(line0.startsWith("Hello"))

        assertEquals(0, buffer.cursorRow)
        assertEquals(5, buffer.cursorCol)
    }

    @Test
    fun testCursorBounds() {
        val buffer = TerminalBuffer(width = 10, height = 5, scrollbackMax = 10)

        buffer.setCursor(2, 3)
        assertEquals(2, buffer.cursorRow)
        assertEquals(3, buffer.cursorCol)

        assertThrows(TerminalException::class.java) {
            buffer.setCursor(10, 0)
        }
    }

    @Test
    fun testFullContentIncludesScrollback() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 5)

        buffer.writeText("12345")
        buffer.writeText("67890")
        buffer.writeText("ABCDE")

        val fullContent = buffer.getFullContent()

        assertTrue(fullContent.contains("12345"))
        assertTrue(fullContent.contains("ABCDE"))
    }


    @Test
    fun testInsertTextFitsInLine() {
        val buffer = TerminalBuffer(width = 8, height = 3, scrollbackMax = 5)

        buffer.overWriteLine("ABCDEFGH")
        buffer.insertTextLine(0, 2, "123")

        val line0 = buffer.getLine(0)

        assertEquals("AB123CDE", line0)
    }

    @Test
    fun testInsertTextWrapsToNextLine() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 5)

        buffer.overWriteLine("ABCDE")
        buffer.insertTextLine(0, 2, "XYZXYZ")

        val line0 = buffer.getLine(0)
        val line1 = buffer.getLine(1)

        assertEquals("ABXYZ", line0)
        assertEquals("XYZCD", line1)
    }

    @Test
    fun test1() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 5)

        buffer.writeText("Hello")
        buffer.insertTextLine(0, 1, "XYZ")
        buffer.writeText("ABC")

        val line0 = buffer.getLine(0)
        val line1 = buffer.getLine(1)
        val line2 = buffer.getLine(2)

        assertEquals("HXYZe", line0)
        assertEquals("lloAB", line1)
        assertEquals("C    ", line2)

    }

}
