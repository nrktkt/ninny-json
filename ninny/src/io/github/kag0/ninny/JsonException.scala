package io.github.kag0.ninny

class JsonException(
    message: String,
    cause: Throwable = null
) extends Exception(message, cause)

class JsonFieldException(
    val message: String,
    val field: String,
    cause: Throwable = null
) extends JsonException(s"Error converting $field from JSON: $message", cause)
