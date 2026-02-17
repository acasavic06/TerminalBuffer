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
}
