interface Token
data class Name(val name: String) : Token
data class Num(val num: Double) : Token
data class Ch(val ch: Char) : Token
enum class Operator : Token {
    Plus, Minus,
    Times, DividedBy, Modulo,
    Power, Root,
    Gt, Equals, Lt,
    And, Or;

    companion object {
        val map = mapOf(
            '+' to Plus, '-' to Minus,
            '*' to Times, '/' to DividedBy, '%' to Modulo,
            '^' to Power, '√' to Root,
            '>' to Gt, '=' to Equals, '<' to Lt,
            '&' to And, '|' to Or)
    }

    fun execute(a: Double, b: Double): Double {
        return when (this) {
            Plus -> a + b
            Minus -> a - b
            Times -> a * b
            DividedBy -> a / b
            Modulo -> a % b
            Power -> Math.pow(a, b)
            Root -> {
                when (a) {
                    2.0 -> Math.sqrt(b)
                    3.0 -> Math.cbrt(b)
                    else -> {
                        val factor = 1.0 / a // 4^0.5 = 2 root 4, 8^0.3333... = 3 root 8
                        Math.pow(b, factor)
                    }
                }
            }
            Gt -> if (a > b) 1.0 else 0.0
            Lt -> if (a < b) 1.0 else 0.0
            Equals -> if (a == b) 1.0 else 0.0
            And -> if ((a > 0.0) && (b > 0.0)) 1.0 else 0.0
            Or -> if ((a > 0.0) || (b > 0.0)) 1.0 else 0.0
        }
    }
}

typealias Expr = List<Token>
typealias MutableExpr = MutableList<Token>

fun String.tokenize(name: Regex = "[a-zA-Z_]+".toRegex()): Expr {
    val tokens = mutableListOf<String>()

    val number = "[0-9]+\\.?[0-9]*".toRegex()


    val names = name.findAll(this).map { it.range }.toList()
    val noNames = this
    names.forEach { intRange -> noNames.replaceRange(intRange.first, intRange.last, "A".repeat(intRange.last - intRange.first + 1)) }
    val nums = number.findAll(this).map { it.range }.toList()
    val a = names + nums

    var i = 0
    while (i < length) {
        val ins = a.map { i in it }
        if (true !in ins) {
            tokens += get(i).toString()
            i++
        } else {
            val intRange = a[ins.indexOf(true)]
            tokens += substring(intRange)
            i += intRange.last - intRange.first + 1
        }
    }
    return tokens.mapNotNull { it.toToken() }
}

fun String.toToken(name: Regex = "[a-zA-Z_]+".toRegex()): Token? {
    val number = "[0-9]+\\.?[0-9]*".toRegex()
    return when {
        matches(name) -> Name(this)
        matches(number) -> Num(this.toDouble())
        get(0) in Operator.map.keys -> Operator.map[this[0]]
        else -> Ch(this[0])
    }
}
