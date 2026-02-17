package terminal

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
    val screen: Array<Array<Cell>> = Array(height){Array(width){Cell()}}

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

}

/*var text=""
        for (row in 0 until height){
            text+=getLine(row)+"\n"
        }
        return text*/
/*var text=""
for (line in scrollback){
    text+=line.joinToString("") {it.char.toString()}+ "\n"
}

text+=getScreenContent()

return text*/
