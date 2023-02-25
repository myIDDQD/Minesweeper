package minesweeper

import kotlin.system.exitProcess

enum class Cells(val symbol: Char) {
    UNEXPLORED('.'),
    EXPLORED('/'),
    MINE('X'),
    MARKED('*')
}

enum class Mark {
    MINE,
    FREE
}

class Field {

    // Parameters field
    private var qtyMines: Int
    private var width: Int
    private var height: Int

    // Coordinates
    private var x: Int = 0
    private var y: Int = 0
    private lateinit var mark: Mark

    // Inner fields
    private val fieldInternal: List<MutableList<Char>>
    private val fieldExternal: List<MutableList<Char>>

    init {
        print("How many mines do you want on the field? ")
        // TODO fix if (NULL)
        // TODO fix if qty > cells on field
        qtyMines = readln().toInt()

        // Set field size
        width = 9
        height = 9

        // Initialize field with open mines
        fieldInternal = initField(false)

        // Initialize field with hidden mines
        fieldExternal = initField(true)
    }

    private fun initField(isHidden: Boolean): List<MutableList<Char>> {
        fun Char.repeat(count: Int): String = this.toString().repeat(count)

        var firstString = " │"
        var lastString = "—│"

        val resultField : MutableList<MutableList<Char>> = when (isHidden) {
            true -> {
                val field = Cells.UNEXPLORED.symbol.repeat(width * height) // Create string with needed qty unexplored cells
                    .chunked(width) // split string to lists
                    .map { it.toMutableList() }.toMutableList()

                for ((index, row) in field.withIndex()) {
                    row.add(0, '1' + index)
                    row.add(1, '│')
                    row.add('│')
                }

                field
            }

            false -> {
                val field =
                    (Cells.EXPLORED.symbol.repeat(width * height - qtyMines) + Cells.MINE.symbol.repeat(qtyMines)) // Create string with needed qty chars, with mines and explored marked cells
                        .toList().shuffled() // shuffled chars
                        .chunked(width) // split string to lists
                        .map { it.toMutableList() }.toMutableList()

                // Calculate the number of mines around each empty cell
                for (row in field.indices) {
                    for (col in field[row].indices) {

                        if (field[row][col] == Cells.EXPLORED.symbol) {

                            var minesCount = 0

                            for (i in -1..1) {
                                for (j in -1..1) {

                                    val r = row + i
                                    val c = col + j

                                    if (r in 0 until height && c in 0 until width && field[r][c] == Cells.MINE.symbol) {
                                        minesCount++
                                    }
                                }
                            }
                            if (minesCount > 0) field[row][col] = '0' + minesCount
                        }
                    }
                }

                for ((index, row) in field.withIndex()) {
                    row.add(0, '1' + index)
                    row.add(1, '│')
                    row.add('│')
                }

                field
            }
        }

        (1..width).forEach { firstString += it.toString() }

        (1..width).forEach { lastString += "—" }

        resultField.add(0, ("$firstString│").toMutableList())
        resultField.add(1, ("$lastString│").toMutableList())
        resultField.add(("$lastString│").toMutableList())
        return resultField
    }

    fun makeMove() {
        print("Set/unset mine marks or claim a cell as free: ")
        val splitInput = readln().split(" ")

        x = splitInput[1].toInt() + 1
        y = splitInput[0].toInt() + 1

        if (splitInput[2] == "free") {
            mark = Mark.FREE
        } else if (splitInput[2] == "mine") {
            mark = Mark.MINE
        } else {
            makeMove()
        }

        when (mark) {
            // When command is free
            Mark.FREE -> {
                when {
                    // If stepped on a mine
                    fieldInternal[x][y] == Cells.MINE.symbol -> {

                        fieldInternal.forEachIndexed { indexRow, row ->
                            row.forEachIndexed { indexCell, cell ->
                                if (cell == Cells.MINE.symbol) {
                                    fieldExternal[indexRow][indexCell] = Cells.MINE.symbol
                                }
                            }
                        }

                        printField()

                        println("You stepped on a mine and failed!")

                        exitProcess(0)
                    }

                    // If stepped on a marked cell
                    fieldInternal[x][y] == Cells.MARKED.symbol -> {
                        makeMove()
                    }

                    // If stepped on a free cell
                    fieldInternal[x][y] == Cells.EXPLORED.symbol -> {
                        if (fieldExternal[x][y] == Cells.EXPLORED.symbol) {
                            // If free cell already explored
                            makeMove()
                        } else {
                            // If free cell is unexplored
                            // TODO open all around cells
//                            fieldExternal[x][y] = Cells.EXPLORED.symbol

                            // Calculate the number of mines around each empty cell

//                            fun openAround() {
                                for (i in x - 1..x + 1) {
                                    for (j in y - 1..y + 1) {
                                        if (fieldInternal[i][j].isDigit()) fieldExternal[i][j] = fieldInternal[i][j]

                                        if (fieldInternal[i][j] == Cells.EXPLORED.symbol) {
                                            fieldExternal[i][j] = fieldInternal[i][j]
//                                            x = i
//                                            y = j
//                                            openAround()
                                        }
                                    }
                                }
//                            }

//                            openAround()

                        }
                    }

                    // If cell is a digit
                    fieldInternal[x][y].isDigit() -> {
                        if (fieldExternal[x][y].isDigit()) {
                            // If digit already open
                            makeMove()
                        } else {
                            // If digit isn't open
                            fieldExternal[x][y] = fieldInternal[x][y]
                        }
                    }
                }
            }

            // When command set or unset mines marks
            Mark.MINE -> {
                if (fieldExternal[x][y] == Cells.UNEXPLORED.symbol) {
                    fieldExternal[x][y] = Cells.MARKED.symbol

                    if (fieldInternal[x][y] == Cells.MINE.symbol) {
                        fieldInternal[x][y] = Cells.MARKED.symbol
                    }
                } else if (fieldExternal[x][y] == Cells.MARKED.symbol) {
                    fieldExternal[x][y] = Cells.UNEXPLORED.symbol

                    if (fieldInternal[x][y] == Cells.MARKED.symbol) {
                        fieldInternal[x][y] = Cells.MINE.symbol
                    }
                }
            }
        }
    }

    fun printField() {
        println("")
        fieldExternal.forEachIndexed { index, row -> println(row.joinToString("")) }
    }

    // TODO DELETE FOR TESTS
    fun printInternalField() {
        println("")
        fieldInternal.forEachIndexed { index, row -> println(row.joinToString("")) }
    }

    fun continueGame(): Boolean {

        val openFieldToString = fieldInternal.joinToString("") { it.joinToString("") }
        val hiddenFieldToString = fieldExternal.joinToString("") { it.joinToString("") }

        return if (openFieldToString.contains(Cells.MINE.symbol) ||
            openFieldToString.filter { it == Cells.UNEXPLORED.symbol } !=
            hiddenFieldToString.filter { it == Cells.MINE.symbol }
        ) {
            true
        } else {
            println("Congratulations! You found all the mines!")
            false
        }
    }
}

fun main() {
    // Initialize field
    val field = Field()

    // Start game where player enters two numbers as coordinates and command on the field
    do {
        field.printField()
        field.printInternalField()
        field.makeMove()
    } while (field.continueGame())

}
