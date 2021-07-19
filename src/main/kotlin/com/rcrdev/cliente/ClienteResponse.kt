package com.rcrdev.cliente

import io.micronaut.core.annotation.Introspected
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank

@Introspected
data class ClienteResponse(

    @field: NotBlank
    val id: String,

    @field: NotBlank
    val nome: String,

    @field: NotBlank
    val cpf: String
) {

    fun toModel(validador: Validator): Cliente {
        val cliente = Cliente(
            id = this.id,
            nome = this.nome,
            cpf = this.cpf
        )

        val erros = validador.validate(cliente)
        if (erros.isNotEmpty()) {
            throw ConstraintViolationException(erros)
        }

        return cliente
    }
}
