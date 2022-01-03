package minesweeper

enum class Mark(private val mark: Char) {
    UNEXPLORED_CELL('.'),
    EXPLORED_CELL('/'),
    PLAYER_MARKED_CELL('*'),
    MINE_CELL('X'),
    NUMBER_ONE_CELL('1'),
    NUMBER_TWO_CELL('2'),
    NUMBER_THREE_CELL('3'),
    NUMBER_FOUR_CELL('4'),
    NUMBER_FIVE_CELL('5'),
    NUMBER_SIX_CELL('6'),
    NUMBER_SEVEN_CELL('7'),
    NUMBER_EIGHT_CELL('8');

    fun getValue() = mark

    companion object {
        fun findByChar(char: Char): Mark {
            for (enum in values()) {
                if (char == enum.getValue()) return enum
            }
            return UNEXPLORED_CELL
        }
    }

    override fun toString(): String {
        return "$mark"
    }
}