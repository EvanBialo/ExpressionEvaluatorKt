@Suppress("unused")
class ExpressionEnvironment {

    private val vars: MutableMap<String, Double> = mutableMapOf()
    private val funcs: MutableMap<String, Function> = mutableMapOf()

    fun resolve(exprRaw: String): Double? {
        var varname: String? = null

        var expr = exprRaw.tokenize()

        if (Ch(':') in expr) {
            // determine if function or variable
            if (expr[0] is Name && expr[1] == Ch('(')) {
                val funcName = (expr[0] as Name).name
                val end = expr.indexOf(Ch(')'))
                val funcVar = expr.slice(2 until end)
                    .split(Ch(','))
                    .map { it[0] as Name }

                funcs[funcName] = Function(expr.slice((end + 2) until expr.size), funcVar)
                return null
            } else {
                // variable definition
                varname = (expr[0] as Name).name

                expr = expr.slice(2..(expr.size - 1))
            }
        }
        expr.forEachIndexed { i, it ->
            if (it in vars.keys.map { Name(it) }) expr = expr.replaceAt(i, listOf(Num(vars[(it as Name).name]!!)))
        }
        while (expr.size > 1) {
            var i = 0
            var curSubexpr: MutableExpr = mutableListOf()
            var funcName: Token? = null
            var expectingBracket = false
            for (it in expr.withIndex()) {
                if (it.value is Name) {
                    i = it.index
                    funcName = it.value
                    expectingBracket = true
                    curSubexpr = mutableListOf()
                } else if (it.value == Ch('(')) {
                    if (!expectingBracket) {
                        funcName = null
                        i = it.index
                        curSubexpr = mutableListOf()
                    } else expectingBracket = false
                } else if (it.value == Ch(')')) {
                    break
                } else {
                    curSubexpr.add(it.value)
                }
            }
            expr = when (funcName) {
                null -> expr.replaceRange(
                    i..(i + curSubexpr.size + 1),
                    listOf(Num(curSubexpr.resolveBEDMAS()))
                )
                else -> {
                    expr.replaceRange(
                        i..(i + 2 + curSubexpr.size),
                        funcs[(funcName as Name).name]!!.solve(curSubexpr.split(Ch(',')))
                    )
                }
            }
        }

        val v = expr.resolveBEDMAS()
        if (varname != null) {
            vars[varname] = v
        }

        return v
    }

}

fun Expr.solve(): Double? {
    var expr = this

    while (expr.size > 1) {
        var i = 0
        var curSubexpr: MutableExpr = mutableListOf()
        for (it in expr.withIndex()) {
            if (it.value == Ch('(')) {
                i = it.index
                curSubexpr = mutableListOf()
            } else if (it.value == Ch(')')) {
                break
            } else {
                curSubexpr.add(it.value)
            }
        }
        expr = expr.replaceRange(i..(i + curSubexpr.size), listOf(Num(curSubexpr.resolveBEDMAS())))
    }
    return expr.resolveBEDMAS()
}


fun <E> List<E>.replaceRange(intRange: IntRange, that: List<E>) =
    this.slice(0..(intRange.first - 1)) + that + this.slice((intRange.last + 1)..(this.size - 1))

fun <E> List<E>.replaceAt(i: Int, that: List<E>): List<E> =
    this.slice(0 until i) + that + this.slice((i + 1) until this.size)

fun <E> List<E>.split(by: E): List<List<E>> {
    val indices = listOf(-1) + (0..(size - 1)).filter { this[it] == by }
    return indices.mapIndexed { i, it ->
        if (i + 1 < indices.size) this.slice(it + 1 until indices[i + 1])
        else slice(it + 1 until size)
    }
}

fun <E> List<E>.replaceAll(e: E, that: List<E>): List<E> {
    if (e in that) throw java.lang.IllegalArgumentException("e in that")
    var l = this

    (0 until size)
        .filter { this[it] == e }
        .reversed()
        .forEach { l = l.replaceAt(it, that) }

    return l
}

fun Expr.resolveBEDMAS(): Double {
    var expr = this
    expr = expr.resolveOperators(Operator.Modulo)
    expr = expr.resolveOperators(Operator.Power)
    expr = expr.resolveOperators(Operator.Root)
    expr = expr.resolveOperators(Operator.Times, Operator.DividedBy)
    expr = expr.resolveOperators(Operator.Plus, Operator.Minus)
    expr = expr.resolveOperators(Operator.Lt, Operator.Equals, Operator.Gt)
    expr = expr.resolveOperators(Operator.And)
    expr = expr.resolveOperators(Operator.Or)
    return (expr[0] as Num).num
}

fun Expr.resolveOperators(vararg operators: Operator): Expr {
    var expr = this
    val ops = operators.toList()

    val opsLeft = ops.filter { it in expr }.toMutableList()
    while (opsLeft.isNotEmpty()) {
        expr = expr.operateAt(opsLeft.map { expr.indexOf(it) }.min()!!)
        opsLeft.removeIf { it !in expr }
    }
    return expr
}

private fun Expr.operateAt(i: Int): Expr {
    val op = this[i] as Operator
    val v = (op).execute((this[i - 1] as Num).num, (this[i + 1] as Num).num)
    return this.slice(0..(i - 2)) + listOf(Num(v)) + this.slice((i + 2)..(this.size - 1))
}

fun main() {
    val env = ExpressionEnvironment()

    println("Examples:")
    println("---------------------------------------------------------")
    println("not(bool): bool<0 | bool=0")
    println("max(x, y): x*(x>y|x=y) + y*(y>x)")
    println("if(bool, true, false): true*(bool>0) + false*(not(bool>0))")

    while (true) {
        try {
            println(env.resolve(readLine()!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
