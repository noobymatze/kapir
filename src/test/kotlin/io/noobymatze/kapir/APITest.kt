package io.noobymatze.kapir

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.noobymatze.kapir.annotation.GET
import io.noobymatze.kapir.annotation.Path
import io.noobymatze.kapir.extensions.toInfo
import io.noobymatze.kapir.extensions.toTag
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.junit.jupiter.api.Test
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.javaType

@ExperimentalStdlibApi
class APITest {

    fun <A: API> openApi(api: KClass<A>): OpenAPI {
        val def = api.annotations.findAnnotation(OpenAPIDefinition::class)
        val rootPath = api.annotations.findAnnotation(Path::class)?.value?.let {
            if (it.endsWith("/"))
                it
            else
                "$it/"
        }

        val paths = Paths()
        api.declaredMembers.forEach { member ->
            val (name, item) = analyzeMember(rootPath ?: "/", member)
            paths.addPathItem(name, item)
        }

        return OpenAPI().apply {
            this.info = def?.info?.toInfo()
            this.tags = def?.tags?.map { it.toTag() }
            this.paths = paths
        }
    }

    private fun analyzeMember(rootPath: String, member: KCallable<*>): Pair<String, PathItem> {
        val p = member.annotations.findAnnotation(Path::class)?.value
        val get = member.annotations.findAnnotation(GET::class)?.let {
            Operation().apply {
                this.operationId = member.name
                this.responses = ApiResponses().addApiResponse("200", ApiResponse().apply {
                    this.content = Content().addMediaType("application/json", MediaType().schema(Schema<Any?>().apply {
                        type = member.returnType.arguments[0].type?.javaType?.typeName?.let { "string" }
                    }))
                })
            }
        }

        val item = PathItem().apply {
            this.get = get
        }

        return "$rootPath${if(p != null) "$p" else ""}" to item
    }

    private fun <T: Annotation> List<Annotation>.findAnnotation(clazz: KClass<T>): T? =
        find { it.annotationClass == clazz }?.let { it as? T }

    @Test
    fun test() {
        val api = openApi(HelloWorld::class)
        val mapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val x = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(api)
        println(x)
    }

}