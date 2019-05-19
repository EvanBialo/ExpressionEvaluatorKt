package function

import token.*

abstract class Formula(open val inputArgs: List<Name>) {
    abstract fun solve(args: List<Expr>): Expr
}