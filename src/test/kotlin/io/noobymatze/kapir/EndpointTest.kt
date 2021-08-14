package io.noobymatze.kapir

import io.noobymatze.kapir.annotation.Path
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.undertow.Undertow
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EndpointTest {

    interface NoOpAPI: API

    @Test
    fun testParsesSimple() {
        val endpoint = Endpoint.parse(NoOpAPI::class)
        assertTrue(endpoint is Endpoint.Resource<*>)
    }

    @Test
    fun testParsesSimpleStuff() {
        val endpoint = Endpoint.parse(HelloWorld::class)
        println(endpoint)
    }

}

fun main() {
    serve(HelloWorldHandler)
}

object HelloWorldHandler: HelloWorld {
    override fun hello(name: String?): Handler<String> =
        Handler.succeed("Hello ${name ?: "World"}!")
}

private class ApiHandler<A: API>(private val api: A): HttpHandler {

    private val underlying = toHandler()

    override fun handleRequest(exchange: HttpServerExchange?) {
        underlying.handleRequest(exchange)
    }

    private fun toHandler(): RoutingHandler {
        val endpoint = Endpoint.parse(api::class)
        var routingHandler = RoutingHandler()
        route(api, endpoint, "").forEach {
            println(it)
            routingHandler = routingHandler.add(it.method, it.path, it.handler)
        }

        return routingHandler
    }

    private data class Route(
        val method: String,
        val path: String,
        val handler: HttpHandler,
    )

    private fun <A: API> route(api: A, endpoint: Endpoint, path: String, routes: List<Route> = emptyList()): List<Route> =
        when (endpoint) {
            is Endpoint.Op -> listOf(Route(endpoint.method, path / endpoint.path) { exchange ->
                println(endpoint.path)
                val result: Handler<Any?> = endpoint.underlying.call(api, null) as Handler<Any?>
                when (result) {
                    is Handler.Failure ->
                        exchange.statusCode = result.failure.status

                    is Handler.Success -> {
                        exchange.statusCode = 200
                        exchange.responseSender.send(result.value.toString())
                    }
                }
            })

            is Endpoint.Resource<*> ->
                endpoint.operations.flatMap {
                    route(api, it, path / endpoint.path, routes)
                }
        }

    private operator fun String.div(other: String): String =
        when {
            this.endsWith("/") && other.startsWith("/") ->
                this.dropLastWhile { it == '/' } + other

            other.startsWith("/") ->
                "$this$other"

            else ->
                "$this/$other"
        }
}

private fun <A: API> serve(api: A, port: Int = 8080, host: String = "localhost") {
    val undertow = Undertow.builder()
        .addHttpListener(port, host, ApiHandler(api))
        .build()

    undertow.start()
}

