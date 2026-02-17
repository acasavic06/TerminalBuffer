package terminal

//import jdk.internal.agent.Agent.getText

//var bold : Boolean = false,
//var italic : Boolean = false,
//var underline : Boolean = false
enum class Color{DEFAULT,BLACK,WHITE,RED,GREEN,BLUE}
enum class Style { BOLD, ITALIC, UNDERLINE }

data class Cell(
    var char : Char = ' ',
    var foreground : Color = Color.DEFAULT,
    var background : Color = Color.DEFAULT,
    var styles: Set<Style> = emptySet()
){
    fun copyAttributes(): Cell {
        return Cell(
            char = ' ',
            foreground = this.foreground,
            background = this.background,
            styles = this.styles.toSet()
            /*bold=this.bold,
            italic=this.italic,
            underline=this.underline,*/
        )
    }
}


class TerminalBuffer(
    var width: Int,
    var height: Int,
    var scrollbackMax: Int
){
    var screen: Array<Array<Cell>> = Array(height){Array(width){Cell()}}

    val scrollback: MutableList<Array<Cell>> = mutableListOf()

    var cursorRow: Int = 0
    var cursorCol: Int = 0

    var currentAttr: Cell = Cell().copyAttributes()

    fun isInBounds(row: Int,col: Int) : Boolean{
        return row in 0 until height && col in 0 until width
    }

    fun setCursor(row: Int, col: Int): Unit{
        if(isInBounds(row,col)){
            cursorRow=row
            cursorCol=col
        }else{
            throw TerminalException("Cursor out of bounds: ($row,$col)")
        }
    }

    fun getLine(row: Int): String{
        if (row in 0 until height){
            return screen[row].joinToString("") {it.char.toString()}
        }
        throw TerminalException("Row out of bounds: $row")
    }

    fun getChar(row: Int, col: Int): Char{
        if (isInBounds(row,col)){
            return screen[row][col].char
        }
        throw TerminalException("Cursor out of bounds: ($row,$col)")
    }

    fun getAttributes(row: Int, col: Int): Cell{
        if (isInBounds(row,col)){
            return screen[row][col].copyAttributes()
        }
        throw TerminalException("Cursor out of bounds: ($row,$col)")
    }

    fun getScreenContent(): String{
        val text = StringBuilder()
        for (row in 0 until height) {
            text.append(getLine(row)).append("\n")
        }
        return text.toString()

    }

    fun getFullContent(): String{
        val text = StringBuilder()
        for (line in scrollback){
            text.append(line.joinToString("") {it.char.toString()})
            text.append("\n")
        }
        text.append(getScreenContent())
        return text.toString()
    }

    fun moveCursorUp(n: Int) {
        cursorRow = (cursorRow-n).coerceAtLeast(0)
    }
    fun moveCursorDown(n: Int) {
        cursorRow = (cursorRow+n).coerceAtMost(height-1)
    }
    fun moveCursorLeft(n: Int) {
        cursorCol = (cursorCol-n).coerceAtLeast(0)
    }
    fun moveCursorRight(n: Int) {
        cursorCol = (cursorCol+n).coerceAtMost(width-1)
    }

    fun writeText(text: String){
        for (c in text){
            screen[cursorRow][cursorCol++]=currentAttr.copyAttributes().apply {char=c}

            if (cursorCol>=width){
                cursorCol=0
                cursorRow++

                if (cursorRow>=height){
                    scrollback.add(screen[0])
                    if (scrollback.size>scrollbackMax){
                        scrollback.removeAt(0)
                    }

                    for (r in 0 until height-1){
                        screen[r]=screen[r+1]
                    }
                    screen[height-1]=Array(width){Cell()}
                    cursorRow=height-1
                }
            }
        }
    }

    fun clearContent() {
        for (row in 0 until height){
            screen[row]=Array(width){Cell()}
        }
        cursorRow=0
        cursorCol=0
    }

    fun clearFullContent() {
        scrollback.clear()
        clearContent()
    }

    /*  Here i did:
        Scrolls the screen up by one line.
        Moves the top line to scrollback (respecting max size),
        shifts all lines up, adds a new empty line at the bottom,
        and resets the cursor to the start of that line.
    */
    fun insertEmptyLine(){
        scrollback.add(screen[0])
        if (scrollback.size>scrollbackMax){
            scrollback.removeAt(0)
        }

        for (row in 0 until height-1){
            screen[row]=screen[row+1]
        }

        screen[height-1]=Array(width){Cell()}

        cursorRow = height - 1
        cursorCol = 0
    }

    fun overWriteLine(text: String) {
        cursorCol = 0

        writeText(text)

        if (text.length < width) {
            for (col in text.length until width) {
                screen[cursorRow][col] = currentAttr.copyAttributes().apply { char = ' ' }
            }
        }
    }

    fun insertTextLine(row: Int, col: Int, text: String){
        if (!isInBounds(row, col)) {
            throw TerminalException("Out of bounds: ($row,$col)")
        }

        val currentLine = getLine(row)
        val newLine = currentLine.substring(0, col) + text + currentLine.substring(col)

        for (i in 0 until width){
            val ch = if (i < newLine.length) newLine[i] else ' '
            screen[row][i] = currentAttr.copyAttributes().apply { char = ch }
        }

        /*if (newLine.length > width && row+1<height){
            insertTextLine(row+1,0,newLine.substring(width))
        }*/

        if (newLine.length > width){
            val nextLine = newLine.substring(width)

            if(row+1<height){
                insertTextLine(row+1,0,newLine.substring(width))
            }else{ //add to scrollback
                val overflow = Array(width) { Cell() }
                for (i in nextLine.indices.take(width)) {
                    overflow[i] = currentAttr.copyAttributes().apply { char = nextLine[i] }
                }
                scrollback.add(overflow)
                if (scrollback.size > scrollbackMax) {
                    scrollback.removeAt(0)
                }
            }
        }
        cursorRow = row + newLine.length / width
        cursorCol = newLine.length % width
    }

    fun fillLine(row: Int, ch: Char = ' ') {
        if (!isInBounds(row,0)){
            throw TerminalException("Row out of bounds: $row")
        }
        for (col in 0 until width) {
            screen[row][col] = currentAttr.copyAttributes().apply { char = ch }
        }
    }

    fun resize(newWidth: Int, newHeight: Int) {
        val text = getScreenContent().replace("\n","")
        val newScreen = Array(newHeight) { Array(newWidth) { Cell() } }
        var index=0

        for (row in 0 until newHeight){
            for (col in 0 until newWidth){
                if (index < width*height){
                    newScreen[row][col]=screen[index/width][index%width].copyAttributes().apply {char = text[index]}
                }else{
                    newScreen[row][col].char= ' '
                }
                index++
            }
        }

        if (index < text.length) {
            val remaining = text.substring(index)
            val rows = remaining.chunked(newWidth)
            for (line in rows){
                val cells = Array(newWidth) { Cell() }

                for (i in line.indices) {
                    cells[i].char = line[i]
                }
                scrollback.add(cells)
                if (scrollback.size > scrollbackMax) {
                    scrollback.removeAt(0)
                }
            }
        }


        val cursorPosition = cursorRow*width+cursorCol
        cursorRow=minOf(cursorPosition/newWidth, newHeight-1)
        cursorCol=minOf(cursorPosition%newWidth, newWidth-1)
        /*if (cursorPosition>newWidth*newHeight){
            cursorRow=0
            cursorCol = 0
        }else{
            cursorRow = cursorPosition / newWidth
            cursorCol = cursorPosition % newWidth
        }*/

        screen = newScreen
        width = newWidth
        height = newHeight
    }

    fun changeMaxScrollBack(size: Int) {
        if (size<scrollbackMax){
            val toRemove = (scrollback.size - size).coerceAtLeast(0)
            repeat (toRemove){
                scrollback.removeAt(0)
            }
        }
        scrollbackMax=size
    }
}

/*
val newScreen = Array(newWidth) { Array(newHeight) { Cell() } }
val text=getScreenContent()
var screenText=text
var scrollbackText = ""

if (text.length > newWidth*newHeight){
    screenText=screenText.substring(0, newWidth*newHeight)
    scrollbackText = text.substring(newWidth*newHeight)
}

clearContent()
width=newWidth
height=newHeight
writeText(screenText)
*/

