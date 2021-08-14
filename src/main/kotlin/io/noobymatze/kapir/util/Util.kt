package io.noobymatze.kapir.util

import kotlin.reflect.KClass


fun <A: Annotation> List<Annotation>.getAnnotation(annotation: KClass<A>): A? =
    find { it::class == annotation }.let { it as? A }
