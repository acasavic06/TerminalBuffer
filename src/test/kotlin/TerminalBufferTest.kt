package terminal

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TerminalBufferTest {

    // Testing Cursor movement up,down, left,right
    @Test
    fun testCursorMovement() {
        val buffer = TerminalBuffer(width = 5, height = 5, scrollbackMax = 5)

        buffer.setCursor(2, 2)
        buffer.moveCursorUp(1)
        assertEquals(1, buffer.cursorRow)

        buffer.moveCursorDown(10)
        assertEquals(4, buffer.cursorRow)

        buffer.moveCursorLeft(5)
        assertEquals(0, buffer.cursorCol)

        buffer.moveCursorRight(10)
        assertEquals(4, buffer.cursorCol)
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
    fun testWriteTextWrapsAndScrolls() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 3)

        buffer.writeText("ABCDEFGHI")

        assertEquals("ABCDE", buffer.getLine(0))
        assertEquals("FGHI ", buffer.getLine(1))
        assertEquals(1, buffer.cursorRow)
        assertEquals(4, buffer.cursorCol)
    }

    @Test
    fun testInsertTextLineFitsInLine() {
        val buffer = TerminalBuffer(width = 8, height = 3, scrollbackMax = 5)

        buffer.overWriteLine("ABCDEFGH")
        buffer.insertTextLine(0, 2, "123")

        assertEquals("AB123CDE", buffer.getLine(0))
        assertEquals("FGH     ", buffer.getLine(1))
    }

    // Scrollback functionality
    @Test
    fun testInsertTextLineOverflowToScrollback() {
        val buffer = TerminalBuffer(width = 5, height = 1, scrollbackMax = 5)

        buffer.overWriteLine("ABCD")
        buffer.insertTextLine(0, 2, "XYZXYZ")

        assertEquals("ABXYZ", buffer.getLine(0))
        assertEquals("XYZCD", buffer.scrollback[0].joinToString("") { it.char.toString() })
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
    fun testFillLine() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMax = 3)

        buffer.fillLine(0, 'X')

        assertEquals("XXXX", buffer.getLine(0))
    }

    //Deleting Screen
    @Test
    fun testClearContent() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 3)

        buffer.overWriteLine("ABC")
        buffer.overWriteLine("DEF")

        buffer.clearContent()

        assertEquals("   ", buffer.getLine(0))
        assertEquals("   ", buffer.getLine(1))
        assertEquals(0, buffer.cursorRow)
        assertEquals(0, buffer.cursorCol)
    }

    // Deleting Screen and scrollback
    @Test
    fun testClearFullContent() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMax = 3)

        buffer.overWriteLine("ABC")
        buffer.overWriteLine("DEF")
        buffer.insertEmptyLine()

        buffer.clearFullContent()

        assertEquals(0, buffer.scrollback.size)
        assertEquals("   ", buffer.getLine(0))
        assertEquals("   ", buffer.getLine(1))
    }


    //  !!!!!       IMPORTANT       !!!!!
    // insertTextLine operates on the entire line, including blank spaces.
    // For example, if the line is "Jet  " (width = 5) and we insert "Brains" after 'J',
    // the result will be split across rows as:
    // line0 = "JBrai"
    // line1 = "nset_"
    // line2 = "_^   " , where _ is blank space and ^ is current position of Cursor
    // Now, if we want to execute writeText("Hey"), it will be written like this
    // line2 = "_Hey^" , again ^ is position of Cursor

    // Important: getLine(row) returns the full line with spaces, not just the visible text.
    // If you want to ignore trailing spaces when inserting, use getLine(row).trimEnd().
    // This way "Jet" will be treated as length 3 instead of "Jet  " length 5.

    @Test
    fun test1() {
        val buffer = TerminalBuffer(width = 5, height = 4, scrollbackMax = 5)

        buffer.writeText("Jet")
        buffer.insertTextLine(0, 1, "Brains")
        buffer.writeText("Is")

        val line0 = buffer.getLine(0)
        val line1 = buffer.getLine(1)
        val line2 = buffer.getLine(2)

        assertEquals("JBrai", line0)
        assertEquals("nset ", line1)
        assertEquals(" Is  ", line2)

        buffer.insertTextLine(1, 2, "best")
        val line00 = buffer.getLine(0)
        val line10 = buffer.getLine(1)
        val line20 = buffer.getLine(2)
        val line30 = buffer.getLine(3)
        assertEquals("JBrai", line00)
        assertEquals("nsbes", line10)
        assertEquals("tet  ", line20)
        assertEquals("Is   ", line30)
    }


    // Testing resizing
    @Test
    fun testResizeLargerKeepsContent() {
        val buffer = TerminalBuffer(width = 3, height = 3, scrollbackMax = 5)

        buffer.writeText("ABCDEF") // popuni 2 reda

        buffer.resize(4, 3)

        assertEquals(4, buffer.width)
        assertEquals(3, buffer.height)

        assertEquals("ABCD", buffer.getLine(0))
        assertEquals("EF  ", buffer.getLine(1))
        assertEquals("    ", buffer.getLine(2))
    }

    @Test
    fun testResizeSmallerMovesOverflowToScrollback() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMax = 5)

        buffer.writeText("ABCDEFGHIJKLMN")
        buffer.resize(3, 2)

        assertEquals(3, buffer.width)
        assertEquals(2, buffer.height)

        assertEquals("ABC", buffer.getLine(0))
        assertEquals("DEF", buffer.getLine(1))

        val fullContent = buffer.getFullContent()

        assertTrue(fullContent.contains("GHI"))
        assertTrue(fullContent.contains("JKL"))
        assertTrue(fullContent.contains("MN"))
    }

    @Test
    fun testResizePreservesCursorPosition() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMax = 5)

        buffer.setCursor(1, 3)

        buffer.resize(10, 4)

        assertEquals(0, buffer.cursorRow)
        assertEquals(8, buffer.cursorCol)
    }

    /* Before resizing screen, _ -> blank space and ^ cursor postion
    _____
    ___^_

    After resizing screen,
    ________^_
    __________
    __________
    __________
    */


    //Testing changeMaxScrollBack
    @Test
    fun testChangeMaxScrollBackReducesSize() {
        val buffer = TerminalBuffer(width = 3, height = 1, scrollbackMax = 5)

        buffer.writeText("AAAA")
        buffer.insertEmptyLine()
        buffer.writeText("BBBB")
        buffer.insertEmptyLine()
        buffer.writeText("CCCC")
        buffer.insertEmptyLine()

        assertTrue(buffer.scrollback.size > 0)

        buffer.changeMaxScrollBack(1)

        assertEquals(1, buffer.scrollback.size)
    }

    @Test
    fun testChangeMaxScrollBackIncreaseDoesNotRemove() {
        val buffer = TerminalBuffer(width = 3, height = 1, scrollbackMax = 1)

        buffer.writeText("AAAA")
        buffer.insertEmptyLine()

        val oldSize = buffer.scrollback.size

        buffer.changeMaxScrollBack(10)

        assertEquals(oldSize, buffer.scrollback.size)
        assertEquals(10, buffer.scrollbackMax)
    }

    @Test
    fun testChangeMaxScrollBackRemovesOldestLines() {
        val buffer = TerminalBuffer(width = 3, height = 1, scrollbackMax = 5)

        buffer.writeText("111")
        buffer.insertEmptyLine()

        buffer.writeText("222")
        buffer.insertEmptyLine()

        buffer.writeText("333")
        buffer.insertEmptyLine()

        buffer.changeMaxScrollBack(2)

        assertEquals(2, buffer.scrollback.size)

        val content = buffer.scrollback.joinToString("") {
            it.joinToString("") { c -> c.char.toString() }
        }

        assertFalse(content.contains("111"))
    }

}
