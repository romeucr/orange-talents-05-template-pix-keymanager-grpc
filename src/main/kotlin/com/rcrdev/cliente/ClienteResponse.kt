package com.rcrdev.cliente

import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.instituicao.InstituicaoResponse
import io.micronaut.core.annotation.Introspected
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ClienteResponse(
    @field: NotBlank val id: String,
    @field: NotBlank val nome: String,
    @field: NotBlank val cpf: String,
    @field: NotNull  val instituicao: InstituicaoResponse
) {
    fun toModel(validador: Validator): Cliente {
        val cliente = Cliente(id, nome, cpf, instituicao.toModel(validador))

        val erros = validador.validate(cliente)
        if (erros.isNotEmpty()) {
            throw ConstraintViolationException(erros)
        }

        return cliente
    }
}