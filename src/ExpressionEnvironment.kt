import function.CustomFunction
import token.*

@Suppress("unused")
class ExpressionEnvironment {
    private val vars: MutableMap<String, Double> = mutableMapOf()
    private val funcs: MutableMap<String, CustomFunction> = mutableMapOf()

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

                funcs[funcName] = CustomFunction(expr.slice((end + 2) until expr.size), funcVar)
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
