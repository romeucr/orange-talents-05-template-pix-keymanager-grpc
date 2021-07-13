package com.rcrdev.instituicao

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class InstituicaoResponse(

    @field: NotBlank
    val ispb: Long,

    @field: NotBlank
    val nome: String

) {
    fun toModel(): Instituicao {
        return Instituicao(ispb, nome)
    }
}