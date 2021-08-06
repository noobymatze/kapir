package io.noobymatze.kapir

import io.noobymatze.kapir.openapi.openApi
import io.swagger.v3.core.util.Json
import org.junit.jupiter.api.Test

@ExperimentalStdlibApi
class APITest {

    @Test
    fun test() {
        val api = openApi(HelloWorld::class)
        val result = Json.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(api)
        println(result)
    }

}