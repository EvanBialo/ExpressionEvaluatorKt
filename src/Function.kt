import java.lang.IllegalArgumentException

data class Function(val expr: Expr, val inputArgs: List<Name>) {
    fun solve(args: List<Expr>): Expr {
        if (inputArgs.size != args.size) throw IllegalArgumentException("Incorrect number of arguments")

        val map: MutableMap<Name, Expr> = inputArgs.zip(args).toMap().toMutableMap()
        var solution = expr
        map.forEach { solution = solution.replaceAll(it.key, listOf(Num(it.value.solve()!!))) }

        return listOf(Ch('(')) + solution + listOf(Ch(')'))
    }
}

fun solveFun(func: Expr, names: List<Pair<Name, Expr>>): Expr {
    var expr = listOf(Ch('(')) + func + listOf(Ch(')'))

    names.forEach { expr = expr.replaceAll(it.first, listOf(Num(it.second.solve()!!))) }

    return expr
}