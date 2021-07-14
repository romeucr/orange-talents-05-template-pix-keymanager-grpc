package com.rcrdev.chavepix

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class ChavePixDelete(
    @field: NotBlank val pixId: String,
    @field: NotBlank val clientId: String) {
}