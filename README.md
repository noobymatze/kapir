# Kapir

The most widely used libraries and frameworks for implementing HTTP
APIs on the JVM are Spring (Boot) and JAX-RS/Jakarta EE.  Libraries
like http4k, ktor and Javelin are other contenders especially in the
Kotlin world. However, none of them

Therefore, this library has the following goals.

1. Be as convenient in declaring HTTP APIs as Spring, but be explicit
   in using it.
2. Be able to automatically generate an HTTP client from the API,
   without the use of OpenAPI (akin to retrofit).
3. Do ensure, that the server implements the declared API.

The inspiration for this comes mainly from Servant and all mentioned
libraries. Servant is a library, which allows the declaration of an
HTTP API at the type-level. Tapir is a library in Scala, which allows
to do this as well. However, the type system of Kotlin and Java are
both not strong enough to handle this type of thing.

## Example

To better understand, take a look at the following example.

```kotlin
// First, we define our API
@OpenAPIDefinition(info = Info(title = "Hello World!"))
@Path("/api/v1/hello")
interface HelloWorldAPI {

    @GET
    fun hello(@QueryParam("name") name: String? = null): Handler<String>
    
}


// Then, we implement it.

val helloServer = object: HelloWorldAPI {

    override fun hello(name: String?): Handler<String> =
        Handler.succeed("Hello ${name ?: "World"}!")

}


// 1. Then we serve it
serve(helloServer)

// 2. Now we query it.
val client = API.client("http://localhost:8080", HelloWorldAPI::class)
val value: String = client.hello("Test").unsafeRun()
println("$value") // => "Hello Test!"
```


[tapir]: https://github.com/softwaremill/tapir
