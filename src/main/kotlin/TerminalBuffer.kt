package terminal

//enum class Color{DEFAULT,BLACK,WHITE,RED,GREEN,BLUE}
//var bold : Boolean = false,
//var italic : Boolean = false,
//var underline : Boolean = false
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
    val width: Int,
    val height: Int,
    val scrollbackMax: Int
){
    val screen: Array<Array<Cell>> = Array(height){Array(width){Cell()}}

    val scrollback: MutableList<Array<Cell>> = mutableListOf()

    var cursorRow: Int = 0
    var cursorCol: Int = 0

    var currentAttr: Cell = Cell().copyAttributes()

    fun setCursor(row: Int, col: Int): Unit{
        if(row in 0 until height && col in 0 until width){
            cursorRow=row
            cursorCol=col
        }else{
            throw TerminalException("Cursor out of bounds: ($row,$col)")
        }
    }

    fun moveCursorUp(n: Int) : Unit{
        cursorRow = (cursorRow-n).coerceAtLeast(0)
    }
    fun moveCursorDown(n: Int) : Unit{
        cursorRow = (cursorRow+n).coerceAtMost(height-1)
    }
    fun moveCursorLeft(n: Int) : Unit{
        cursorCol = (cursorCol-n).coerceAtLeast(0)
    }
    fun moveCursorRight(n: Int) : Unit{
        cursorCol = (cursorCol+n).coerceAtMost(width-1)
    }

}
