package io.noobymatze.kapir.openapi

import io.noobymatze.kapir.API
import io.noobymatze.kapir.annotation.GET
import io.noobymatze.kapir.annotation.Path
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.core.util.ReflectionUtils
import io.swagger.v3.oas.annotations.Hidden
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
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.jvmErasure

/**
 *
 * @param api
 */
fun <A: API> openApi(api: KClass<A>): OpenAPI? {
    // This whole implementation has been ported from the Reader in the jaxrs2 module.
    // https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-jaxrs2/src/main/java/io/swagger/v3/jaxrs2/Reader.java
    val def = api.getAnnotation(OpenAPIDefinition::class) ?: return null
    val hidden = api.getAnnotation(Hidden::class)

    if (hidden != null) {
        return null
    }

    val rootPath = api.getAnnotation(Path::class)?.value?.let {
        if (it.endsWith("/"))
            it
        else
            "$it/"
    }

    val openAPI = OpenAPI()

    AnnotationsUtils.getInfo(def.info)
        .ifPresent { openAPI.info = it }

    AnnotationsUtils.getTags(def.tags, false)
        .ifPresent { openAPI.tags = it.toList() }

    AnnotationsUtils.getExternalDocumentation(def.externalDocs)
        .ifPresent { openAPI.externalDocs = it }

    AnnotationsUtils.getServers(def.servers)
        .ifPresent { openAPI.servers = it }

    if (def.extensions.isNotEmpty()) {
        openAPI.extensions = AnnotationsUtils.getExtensions(*def.extensions)
    }

    val paths = Paths()
    api.declaredMembers.forEach { member ->
        val (name, item) = analyzeMember(rootPath ?: "/", member)
        paths.addPathItem(name, item)
    }

    openAPI.paths = paths

    return openAPI
}


private fun analyzeMember(rootPath: String, member: KCallable<*>): Pair<String, PathItem> {
    val p = member.getAnnotation(Path::class)?.value
    val get = member.getAnnotation(GET::class)?.let {
        Operation().apply {
            this.operationId = member.name
            this.responses = ApiResponses().addApiResponse("200", ApiResponse().apply {
                this.content = Content().addMediaType("application/json", MediaType().schema(Schema<Any?>().apply {
                    type = member.returnType.arguments[0].type?.jvmErasure?.let { "string" }
                }))
            })
        }
    }

    val item = PathItem().apply {
        this.get = get
    }

    return "$rootPath${if(p != null) "$p" else ""}" to item
}

private fun <T: Annotation, A: Any> KClass<A>.getAnnotation(clazz: KClass<T>): T? =
    ReflectionUtils.getAnnotation(this.java, clazz.java)

private fun <T: Annotation> KCallable<*>.getAnnotation(clazz: KClass<T>): T? =
    annotations.find { it.annotationClass == clazz }?.let { it as? T }
