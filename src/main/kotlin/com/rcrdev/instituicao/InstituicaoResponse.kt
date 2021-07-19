package com.rcrdev.instituicao

import io.micronaut.core.annotation.Introspected
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank

@Introspected
data class InstituicaoResponse(

    @field: NotBlank
    val ispb: Int,

    @field: NotBlank
    val nome: String
) {

    fun toModel(validador: Validator): Instituicao {
        val instituicao = Instituicao(
            ispb = this.ispb,
            nome = this.nome
        )

        val erros = validador.validate(instituicao)
        if (erros.isNotEmpty()) {
            throw ConstraintViolationException(erros)
        }

        return instituicao
    }
}