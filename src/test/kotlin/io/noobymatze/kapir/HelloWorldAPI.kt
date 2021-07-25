package io.noobymatze.kapir

import io.noobymatze.kapir.annotation.GET
import io.noobymatze.kapir.annotation.Path
import io.noobymatze.kapir.annotation.QueryParam
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag


@OpenAPIDefinition(
    info = Info(
        title = "Hello World",
        description = "This is an example API.",
        version = "1.0",
    ),
    tags = [Tag(name = "hello"), Tag(name = "world")],
)
@Path("/hello")
interface HelloWorld : API {

    @GET
    fun hello(
        @QueryParam("name")
        name: String
    ): Handler<String>

}
