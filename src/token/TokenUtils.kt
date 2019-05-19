package token

typealias Expr = List<Token>
typealias MutableExpr = MutableList<Token>

fun String.toToken(name: Regex = "[a-zA-Z_]+".toRegex()): Token? {
    val number = "[0-9]+\\.?[0-9]*".toRegex()
    return when {
        matches(name) -> Name(this)
        matches(number) -> Num(this.toDouble())
        this[0] in Operator.map.keys -> Operator.map[this[0]]
        this.trim() == "" -> null
        else -> Ch(this[0])
    }
}

fun String.tokenize(name: Regex = "[a-zA-Z_]+".toRegex()): Expr {
    val tokens = mutableListOf<String>()

    val number = "[0-9]+\\.?[0-9]*".toRegex()


    val names = name.findAll(this).map { it.range }.toList()
    val noNames = this
    names.forEach { intRange ->
        noNames.replaceRange(
            intRange.first,
            intRange.last,
            "A".repeat(intRange.last - intRange.first + 1)
        )
    }
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

