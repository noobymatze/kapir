# Kapir


## Example

Let the code speak for itself.

```kotlin
@OpenAPIDefinition(
    info = Info(
        title = "Hello World!",
    )
)
@Path("/api/v1/hello")
interface HelloWorldAPI {

    @GET
    fun hello(@QueryParam("name") name: String?): Handler<String>
    
}

class HelloServer: HelloWorldAPI {

    override fun hello(name: String?): Handler<String> =
        Handler.succeed("Hello ${name ?: "World"}!")

}
```
