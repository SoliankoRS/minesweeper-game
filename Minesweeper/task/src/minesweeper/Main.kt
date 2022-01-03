package minesweeper

import kotlin.random.Random

fun main() {
    println("How many mines do you want on the field?")
    val numOfMines = readLine()!!.toInt()
    val fieldSize = 9
    MinesweeperGame(fieldSize, numOfMines).play()
}

class MinesweeperGame(private val fieldSize: Int, private val numOfMines: Int) {

    private val fieldLength = fieldSize - 1
    private val allMineLocations = mutableListOf<Cell>()
    private val hiddenMineLocations = mutableListOf<Cell>()
    private val selectedMineLocations = mutableListOf<Cell>()
    private val internalField: MutableList<MutableList<Mark>> =
        MutableList(fieldSize) { MutableList(fieldSize) { Mark.EXPLORED_CELL } }
    private val playerField: MutableList<MutableList<Mark>> =
        MutableList(fieldSize) { MutableList(fieldSize) { Mark.UNEXPLORED_CELL } }

    private var isGameOver = false
    private var isPlayerDead = false

    inner class Cell(_x: Int, _y: Int) {
        val x = _x
        val y = _y

        var internalFieldMark: Mark
            get() = this@MinesweeperGame.internalField[this.x][this.y]
            set(value) {
                this@MinesweeperGame.internalField[this.x][this.y] = value
            }

        var playerFieldMark: Mark
            get() = this@MinesweeperGame.playerField[this.x][this.y]
            set(value) {
                this@MinesweeperGame.playerField[this.x][this.y] = value
            }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Cell

            if (x != other.x) return false
            if (y != other.y) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x
            result = 31 * result + y
            return result
        }
    }

    fun play() {
        stageStart()
        do {
            stageChose()
        } while (!isGameOver)
        stageFinish()
    }

    private fun stageStart() {
        do {
            showPlayerField()
            println("Set/unset mines marks or claim a cell as free:")
            val input = readLine()!!.split(" ")
            val startCell = Cell(input[1].toInt() - 1, input[0].toInt() - 1)
            when (val chose = input[2]) {
                "free" -> {
                    initFieldOfMines(startCell)
                    playerCheckCell(startCell)
                    break
                }
                "mine" -> {
                    playerMarkCell(startCell)
                }
                else -> println("ERROR: wrong selection command: $chose")
            }
        } while (true)
    }

    private fun stageChose() {
        showPlayerField()
        println("Set/unset mines marks or claim a cell as free:")
        val input = readLine()!!.split(" ")
        val choseCell = Cell(input[1].toInt() - 1, input[0].toInt() - 1)
        when (val chose = input[2]) {
            "free" -> {
                playerCheckCell(choseCell)
            }
            "mine" -> {
                playerMarkCell(choseCell)
            }
            else -> println("ERROR: wrong selection command: $chose")
        }
    }

    private fun stageFinish() {
        if (isGameOver && isPlayerDead) {
            println("You stepped on a mine and failed!")
        } else {
            println("Congratulations! You found all the mines!")
        }
    }

    private fun initFieldOfMines(startCell: Cell) {
        val random = Random
        repeat(numOfMines) {
            var x: Int
            var y: Int
            var cell: Cell
            do {
                x = random.nextInt(0, fieldSize)
                y = random.nextInt(0, fieldSize)
                cell = Cell(x, y)
            } while (startCell == cell || allMineLocations.contains(cell))
            cell.internalFieldMark = Mark.MINE_CELL
            markNearMineCell(cell)
            this.allMineLocations.add(Cell(x, y))
        }
        this.hiddenMineLocations.addAll(this.allMineLocations)
    }

    private fun getAroundCells(cell: Cell): MutableList<Cell> {
        val x = cell.x
        val y = cell.y
        val result = mutableListOf<Cell>()
        // N -> NE -> E -> ES -> S -> SW -> W -> WN
        if (x - 1 >= 0) result.add(Cell(x - 1, y))
        if (x - 1 >= 0 && y + 1 <= fieldLength) result.add(Cell(x - 1, y + 1))
        if (y + 1 <= fieldLength) result.add(Cell(x, y + 1))
        if (x + 1 <= fieldLength && y + 1 <= fieldLength) result.add(Cell(x + 1, y + 1))
        if (x + 1 <= fieldLength) result.add(Cell(x + 1, y))
        if (x + 1 <= fieldLength && y - 1 >= 0) result.add(Cell(x + 1, y - 1))
        if (y - 1 >= 0) result.add(Cell(x, y - 1))
        if (x - 1 >= 0 && y - 1 >= 0) result.add(Cell(x - 1, y - 1))
        return result
    }

    private fun markNearMineCell(cell: Cell) {
        val nearCells = getAroundCells(cell)
        nearCells.forEach {
            if (it.internalFieldMark != Mark.MINE_CELL) {
                when (it.internalFieldMark) {
                    Mark.EXPLORED_CELL -> it.internalFieldMark = Mark.NUMBER_ONE_CELL
                    else -> {
                        it.internalFieldMark = Mark.findByChar(it.internalFieldMark.getValue() + 1)
                    }
                }
            }
        }
    }

    private fun playerCheckCell(cell: Cell) {
        if (cell.playerFieldMark != Mark.UNEXPLORED_CELL) return
        if (allMineLocations.contains(cell)) {
            allMineLocations.forEach { mineCell -> mineCell.playerFieldMark = Mark.MINE_CELL }
            this.isGameOver = true
            this.isPlayerDead = true
        } else {
            cell.playerFieldMark = cell.internalFieldMark
            if (cell.internalFieldMark == Mark.EXPLORED_CELL) {
                cell.playerFieldMark = cell.internalFieldMark
                markNearExploredCell(cell)
            }
        }
    }

    private fun markNearExploredCell(cell: Cell) {
        val nearCells = getAroundCells(cell)
        nearCells.forEach {
            if (it.playerFieldMark == Mark.UNEXPLORED_CELL || it.playerFieldMark == Mark.PLAYER_MARKED_CELL) {
                if (it.internalFieldMark == Mark.EXPLORED_CELL) {
                    it.playerFieldMark = it.internalFieldMark
                    markNearExploredCell(it)
                } else {
                    it.playerFieldMark = it.internalFieldMark
                }
            }
        }
    }

    private fun playerMarkCell(cell: Cell) {
        when (cell.playerFieldMark) {
            Mark.UNEXPLORED_CELL -> {
                if (allMineLocations.contains(cell)) {
                    hiddenMineLocations.remove(cell)
                }
                selectedMineLocations.add(cell)
                cell.playerFieldMark = Mark.PLAYER_MARKED_CELL
                if (hiddenMineLocations.isEmpty() && numOfMines == selectedMineLocations.size) {
                    isGameOver = true
                }
            }
            Mark.PLAYER_MARKED_CELL -> {
                if (allMineLocations.contains(cell)) {
                    hiddenMineLocations.add(cell)
                }
                selectedMineLocations.remove(cell)
                cell.playerFieldMark = Mark.UNEXPLORED_CELL
            }
            else -> return
        }
    }

    fun showPlayerField() {
        printField(playerField)
    }

    private fun showInternalField() {
        printField(internalField)
    }

    fun printField(field: MutableList<MutableList<Mark>>) {
        println(" |${(1..fieldSize).joinToString("")}|")
        println("-|${"-".repeat(fieldSize)}|")
        field.forEachIndexed { x, y -> println("${x + 1}|${y.joinToString("")}|") }
        println("-|${"-".repeat(fieldSize)}|")
    }
}
