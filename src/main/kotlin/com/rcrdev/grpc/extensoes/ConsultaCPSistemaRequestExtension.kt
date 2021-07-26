package com.rcrdev.grpc.extensoes

import com.rcrdev.ConsultaCPSistemaRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

fun ConsultaCPSistemaRequest.validar(validador: Validator): ConsultaChavePixSistemas {
    val consultaChavePix = ConsultaChavePixSistemas(this.chave)

    val errors = validador.validate(consultaChavePix)

    if (errors.isNotEmpty()) throw ConstraintViolationException(errors)

    return consultaChavePix
}


data class ConsultaChavePixSistemas(
    @field: NotBlank
    @field: Size(max = 77)
    val chave: String,
)