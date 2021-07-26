package com.rcrdev.grpc.extensoes

import com.rcrdev.ConsultaCPClienteRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

fun ConsultaCPClienteRequest.validar(validador: Validator): ConsultaChavePixClientes {
    val consultaChavePix = ConsultaChavePixClientes(this.pixId, this.idCliente)

    val errors = validador.validate(consultaChavePix)

    if (errors.isNotEmpty()) throw ConstraintViolationException(errors)

    return consultaChavePix
}

data class ConsultaChavePixClientes(

    @field: NotBlank
    @field: Size(max = 77)
    val pixId: String,

    @field: NotBlank
    val clientId: String
)
