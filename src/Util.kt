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