package com.rcrdev.grpc.extensoes

import com.google.protobuf.Timestamp
import com.rcrdev.ConsultaCPClienteResponse
import com.rcrdev.TipoChave
import com.rcrdev.TitularChave
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.conta.Conta
import java.time.ZoneOffset

fun criarResponseGrpcCliente(chavePix: ChavePix, conta: Conta): ConsultaCPClienteResponse {

    return ConsultaCPClienteResponse.newBuilder()
        .setPixId(chavePix.pixId)
        .setIdCliente(chavePix.clientId)
        .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
        .setChave(chavePix.chave)
        .setTitular(
            TitularChave.newBuilder()
                .setNome(conta.titular.nome)
                .setCpf(conta.titular.cpf)
        )
        .setConta(
            com.rcrdev.Conta.newBuilder()
                .setAgencia(conta.agencia)
                .setNumero(conta.numero)
                .setNomeInstituicao(conta.instituicao.nome)
                .setTipoValue(conta.tipoConta.ordinal)
        )
        .setCriadoEm(chavePix.criadoEm?.let {
            Timestamp.newBuilder()
                .setSeconds(it.toEpochSecond(ZoneOffset.UTC))
                .build()
        })
        .build()
}