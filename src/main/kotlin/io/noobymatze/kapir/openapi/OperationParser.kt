package io.noobymatze.kapir.openapi

import com.fasterxml.jackson.annotation.JsonView
import io.noobymatze.kapir.annotation.Consumes
import io.noobymatze.kapir.annotation.Produces
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.links.Link
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import java.util.*


const val COMPONENTS_REF = Components.COMPONENTS_SCHEMAS_REF

// All of the functions in this file have been ported from the OptionParser.
// https://github.com/swagger-api/swagger-core/blob/master/modules/swagger-jaxrs2/src/main/java/io/swagger/v3/jaxrs2/OperationParser.java

fun getRequestBody(
    requestBody: io.swagger.v3.oas.annotations.parameters.RequestBody?,
    classConsumes: Consumes?,
    methodConsumes: Consumes?,
    components: Components?,
    jsonViewAnnotation: JsonView?
): Optional<RequestBody> {
    if (requestBody == null) {
        return Optional.empty<RequestBody>()
    }
    val requestBodyObject = RequestBody()
    var isEmpty = true
    if (requestBody.ref.isNotBlank()) {
        requestBodyObject.`$ref` = requestBody.ref
        return Optional.of<RequestBody>(requestBodyObject)
    }
    if (requestBody.description.isNotBlank()) {
        requestBodyObject.description = requestBody.description
        isEmpty = false
    }
    if (requestBody.required) {
        requestBodyObject.required = requestBody.required
        isEmpty = false
    }
    if (requestBody.extensions.isNotEmpty()) {
        val extensions = AnnotationsUtils.getExtensions(*requestBody.extensions)
        extensions?.forEach(requestBodyObject::addExtension)
        isEmpty = false
    }
    if (requestBody.content.isNotEmpty()) {
        isEmpty = false
    }
    if (isEmpty) {
        return Optional.empty<RequestBody>()
    }
    AnnotationsUtils.getContent(
        requestBody.content,
        classConsumes?.value ?: arrayOfNulls(0),
        methodConsumes?.value ?: arrayOfNulls(0),
        null,
        components,
        jsonViewAnnotation
    ).ifPresent(requestBodyObject::setContent)
    return Optional.of<RequestBody>(requestBodyObject)
}

fun getApiResponses(
    responses: Array<io.swagger.v3.oas.annotations.responses.ApiResponse>?,
    classProduces: Produces?,
    methodProduces: Produces?,
    components: Components?,
    jsonViewAnnotation: JsonView?
): Optional<ApiResponses> {
    if (responses == null) {
        return Optional.empty<ApiResponses>()
    }
    val apiResponsesObject = ApiResponses()
    for (response in responses) {
        val apiResponseObject = ApiResponse()
        if (response.ref.isNotBlank()) {
            apiResponseObject.`$ref` = response.ref
            if (response.responseCode.isNotBlank()) {
                apiResponsesObject.addApiResponse(response.responseCode, apiResponseObject)
            } else {
                apiResponsesObject._default(apiResponseObject)
            }
            continue
        }
        if (response.description.isNotBlank()) {
            apiResponseObject.setDescription(response.description)
        }
        if (response.extensions.isNotEmpty()) {
            val extensions = AnnotationsUtils.getExtensions(*response.extensions)
            extensions?.forEach(apiResponseObject::addExtension)
        }
        AnnotationsUtils.getContent(
            response.content,
            classProduces?.value ?: arrayOfNulls(0),
            methodProduces?.value ?: arrayOfNulls(0),
            null,
            components,
            jsonViewAnnotation
        ).ifPresent(apiResponseObject::content)
        AnnotationsUtils.getHeaders(response.headers, jsonViewAnnotation).ifPresent(apiResponseObject::headers)
        if (apiResponseObject.description.isNotBlank() || apiResponseObject.content != null || apiResponseObject.headers != null) {
            val links: Map<String, Link> = AnnotationsUtils.getLinks(response.links)
            if (links.isNotEmpty()) {
                apiResponseObject.setLinks(links)
            }
            if (response.responseCode.isNotBlank()) {
                apiResponsesObject.addApiResponse(response.responseCode, apiResponseObject)
            } else {
                apiResponsesObject._default(apiResponseObject)
            }
        }
    }
    return if (apiResponsesObject.isEmpty()) {
        Optional.empty<ApiResponses>()
    } else Optional.of<ApiResponses>(apiResponsesObject)
}