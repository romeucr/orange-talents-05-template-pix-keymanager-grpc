package com.rcrdev.grpc.extensoes

import com.rcrdev.ChavePixRequest
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.tipos.TipoChave
import com.rcrdev.chavepix.tipos.TipoConta
import java.util.*

fun ChavePixRequest.toChavePix() : ChavePix {
    return ChavePix(
        clientId = this.idCliente,
        tipoChave = TipoChave.valueOf(this.tipoChave.name),
        chave = if (TipoChave.valueOf(this.tipoChave.name) == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
        tipoConta = TipoConta.valueOf(this.tipoConta.name)
    )
}