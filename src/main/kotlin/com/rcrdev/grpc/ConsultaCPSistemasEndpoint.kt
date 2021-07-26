package com.rcrdev.grpc

import com.google.protobuf.Timestamp
import com.rcrdev.*
import com.rcrdev.ConsultaCPClientesServiceGrpc.*
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.conta.Conta
import com.rcrdev.conta.service.ContaService
import com.rcrdev.grpc.extensoes.criarResponseGrpc
import com.rcrdev.grpc.extensoes.validar
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import javax.inject.Singleton
import javax.validation.Validator
import com.rcrdev.Conta as ContaProto

@Validated
@ErrorAroundAdvice
@Singleton
class ConsultaCPSistemasEndpoint(
    private val validador: Validator,
    private val chavePixService: ChavePixService,
    private val contaService: ContaService
) : ConsultaCPSistemasServiceGrpc.ConsultaCPSistemasServiceImplBase() {

    private val logger = LoggerFactory.getLogger(ConsultaCPSistemasEndpoint::class.java)

    override fun consultaChavePixSistemas(
        request: ConsultaCPSistemaRequest,
        responseObserver: StreamObserver<ConsultaCPSistemaResponse>
    ) {

//        val consultaChave = request.validar(validador)
//
//        val chavePix: ChavePix
//        with (consultaChave) {
//            chavePix = chavePixService.buscaChavePix(pixId, clientId)
//        }
//
//        val conta: Conta
//        with (chavePix) {
//            conta = contaService.buscaConta(clientId, tipoConta)
//        }
//
//
//        logger.info("Retornando informações da ChavePix solicitada. [PixId: ${chavePix.pixId}]")
//        responseObserver.onNext(criarResponseGrpc(chavePix, conta))
//        responseObserver.onCompleted()
    }
}