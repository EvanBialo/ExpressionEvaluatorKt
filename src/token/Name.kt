package token

data class Name(val name: String) : Token {
    companion object: Token {
        val regex = "[a-zA-Z_]+".toRegex()
    }
}