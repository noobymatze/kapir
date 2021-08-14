package io.noobymatze.kapir

import io.noobymatze.kapir.annotation.*
import io.noobymatze.kapir.util.getAnnotation
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

sealed class Endpoint {

    data class Resource<A: Any>(
        val clazz: KClass<A>,
        val path: String,
        val annotations: List<Annotation>,
        val operations: List<Endpoint>,
    ): Endpoint()

    data class Op(
        val name: String,
        val method: String,
        val consumes: Consumes?,
        val produces: Produces?,
        val path: String,
        val annotations: List<Annotation>,
        val returnType: KClass<*>,
        val parameters: List<KParameter>,
        val underlying: KCallable<*>,
    ): Endpoint()

    data class Parameter(
        val name: String,
        val annotations: List<Annotation>,
        val type: KClass<*>,
    )

    companion object {

        fun <T: API> parse(api: KClass<T>): Endpoint {
            val annotations = api.annotations
            val members = api.declaredMembers
            val path = annotations.getAnnotation(Path::class)?.value ?: "/"

            return Resource(
                clazz = api,
                path = path,
                annotations = annotations,
                operations = members.map { parse(path, it) }
            )
        }

        private fun parse(path: String, member: KCallable<*>): Endpoint {
            val annotations = member.annotations
            val preliminaryReturnType = member.returnType.jvmErasure
            val thisPath = annotations.getAnnotation(Path::class)?.value ?: path
            val returnType = if (preliminaryReturnType == Handler::class) {
                member.returnType.arguments[0].type?.jvmErasure as KClass<*>
            }
            else {
                preliminaryReturnType
            }

            return Op(
                name = member.name,
                annotations = annotations,
                returnType = returnType,
                parameters = member.parameters,
                path = thisPath,
                underlying = member,
                consumes = annotations.getAnnotation(Consumes::class),
                produces = annotations.getAnnotation(Produces::class),
                method = annotations.getAnnotation(GET::class)?.let { "GET" }
                    ?: annotations.getAnnotation(POST::class)?.let { "POST" }
                    ?: "GET"
            )
        }

    }

}