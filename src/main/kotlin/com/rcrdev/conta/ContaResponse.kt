package com.rcrdev.conta

import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.cliente.ClienteResponse
import com.rcrdev.instituicao.InstituicaoResponse
import io.micronaut.core.annotation.Introspected
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ContaResponse(

    @field: NotNull
    val tipo: String,

    @field: NotNull
    val instituicao: InstituicaoResponse,

    @field: NotBlank
    val agencia: String,

    @field: NotBlank
    val numero: String,

    @field: NotNull
    val titular: ClienteResponse
) {

    fun toModel(validador: Validator): Conta {
        val conta = Conta(
            tipoConta = TipoConta.valueOf(this.tipo),
            instituicao = this.instituicao.toModel(validador),
            agencia = this.agencia,
            numero = this.numero,
            titular = this.titular.toModel(validador)
        )

        val erros = validador.validate(conta)
        if (erros.isNotEmpty()) {
            throw ConstraintViolationException(erros)
        }
        return conta
    }
}