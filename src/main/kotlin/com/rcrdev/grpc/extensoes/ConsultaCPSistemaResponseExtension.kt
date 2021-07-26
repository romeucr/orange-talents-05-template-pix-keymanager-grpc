package com.rcrdev.grpc.extensoes

import com.google.protobuf.Timestamp
import com.rcrdev.ConsultaCPSistemaResponse
import com.rcrdev.Conta.newBuilder
import com.rcrdev.TipoChave
import com.rcrdev.TitularChave
import com.rcrdev.bcb.PixKeyDetailsResponse
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.conta.Conta
import java.time.ZoneOffset

fun criarResponseGrpcSistema(chavePix: ChavePix, conta: Conta): ConsultaCPSistemaResponse {

    return ConsultaCPSistemaResponse.newBuilder()
        .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
        .setChave(chavePix.chave)
        .setTitular(
            TitularChave.newBuilder()
                .setNome(conta.titular.nome)
                .setCpf(conta.titular.cpf)
        )
        .setConta(
            newBuilder()
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

fun criarResponseGrpcSistema(pixKeyDetailsResponse: PixKeyDetailsResponse, nomeInst: String): ConsultaCPSistemaResponse {
    with (pixKeyDetailsResponse) {
        return ConsultaCPSistemaResponse.newBuilder()
            .setTipoChave(TipoChave.valueOf(keyType.name))
            .setChave(key)
            .setTitular(
                TitularChave.newBuilder()
                    .setNome(owner.name)
                    .setCpf(owner.taxIdNumber)
            )
            .setConta(
                newBuilder()
                    .setAgencia(bankAccount.branch)
                    .setNumero(bankAccount.accountNumber)
                    .setNomeInstituicao(nomeInst)
                    .setTipoValue(bankAccount.accountType.ordinal)
            )
            .setCriadoEm(Timestamp.newBuilder()
                .setSeconds(createdAt.toEpochSecond(ZoneOffset.UTC)))
            .build()
    }
}