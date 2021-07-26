package com.rcrdev.grpc.extensoes

import com.rcrdev.ChavePixRequest
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.tipos.TipoChave
import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ChavePixRequest.toChavePix(validador: Validator) : ChavePix {
    val novaChavePix = ChavePix(
        clientId = this.idCliente,
        tipoChave = TipoChave.valueOf(this.tipoChave.name),
        chave = if (TipoChave.valueOf(this.tipoChave.name) == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
        tipoConta = TipoConta.valueOf(this.tipoConta.name)
    )

    // validando se a request gRPC recebido contém erros. Verifica as anotações de ChavePix
    val erros = validador.validate(novaChavePix)

    if (erros.isNotEmpty()) {
        throw ConstraintViolationException(erros)
    }

    return novaChavePix
}