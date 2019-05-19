package function

import token.*

abstract class Function(open val inputArgs: List<Name>) {
    abstract fun solve(args: List<Expr>): Expr
}