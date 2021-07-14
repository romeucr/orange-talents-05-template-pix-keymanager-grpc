package com.rcrdev.grpc.extensoes

import com.rcrdev.ChavePixDeleteRequest
import com.rcrdev.chavepix.ChavePixDelete
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ChavePixDeleteRequest.toChavePixDelete(validador: Validator): ChavePixDelete {
    val delRequest = ChavePixDelete(
        pixId = this.pixId,
        clientId = this.idCliente
    )

    val erros = validador.validate(delRequest)

    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return delRequest
}