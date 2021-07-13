package com.rcrdev.cliente

import com.rcrdev.instituicao.InstituicaoResponse
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ClienteResponse(

    @field: NotBlank
    val id: String,

    @field: NotBlank
    val nome: String,

    @field: NotBlank
    val cpf: String,

    @field: NotNull
    val instituicao: InstituicaoResponse
) {
    fun toModel(): Cliente {
        return Cliente(id, nome, cpf, instituicao.toModel())
    }
}