package io.noobymatze.kapir.extensions

import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.tags.Tag

fun io.swagger.v3.oas.annotations.tags.Tag.toTag(): Tag = Tag().apply {
    this.name = this@toTag.name.nullOnBlank()
    this.description = this@toTag.description.nullOnBlank()
}

fun io.swagger.v3.oas.annotations.info.Info.toInfo(): Info = Info().apply {
    this.title = this@toInfo.title.nullOnBlank()
    this.description = this@toInfo.description.nullOnBlank()
    this.termsOfService = this@toInfo.termsOfService.nullOnBlank()
    this.version = this@toInfo.version.nullOnBlank()
    this@toInfo.extensions.forEach {
        this.addExtension(it.name, it.properties)
    }
    this.contact = this@toInfo.contact?.toContact()
    this.license = this@toInfo.license?.toLicense()
}

private fun io.swagger.v3.oas.annotations.info.License.toLicense(): License? {
    val name = this.name.nullOnBlank()
    val url = this.url.nullOnBlank()

    return if (listOfNotNull(name, url).isEmpty())
        null
    else
        License().apply {
            this.name = name
            this.url = url
        }
}

fun io.swagger.v3.oas.annotations.info.Contact.toContact(): Contact? {
    val email = this.email.nullOnBlank()
    val name = this.name.nullOnBlank()
    val url = this.url.nullOnBlank()

    return if (listOfNotNull(email, name, url).isEmpty())
        null
    else
        Contact().apply {
            this.email = email
            this.name = name
            this.url = url
        }
}

private fun String.nullOnBlank(): String? =
    ifBlank { null }