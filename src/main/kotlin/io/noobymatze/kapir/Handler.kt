package io.noobymatze.kapir

import java.net.http.HttpHeaders
import java.net.http.HttpResponse

sealed class Handler<out A> {

    data class HandlerError(
        val status: Int,
        val headers: HttpHeaders,
    )

    data class Success<out A>(
        val value: A
    ) : Handler<A>()

    data class Failure(
        val failure: HandlerError
    ) : Handler<Nothing>()

    companion object {

        operator fun <T> invoke(f: () -> T): Handler<T> =
            Success(f())

    }

}