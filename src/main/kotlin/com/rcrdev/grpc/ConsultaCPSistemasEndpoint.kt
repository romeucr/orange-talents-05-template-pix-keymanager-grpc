package com.rcrdev.grpc

import com.rcrdev.ConsultaCPSistemaRequest
import com.rcrdev.ConsultaCPSistemaResponse
import com.rcrdev.ConsultaCPSistemasServiceGrpc
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.grpc.extensoes.validar
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Validated
@ErrorAroundAdvice
@Singleton
class ConsultaCPSistemasEndpoint(
    private val validador: Validator,
    private val chavePixService: ChavePixService
) : ConsultaCPSistemasServiceGrpc.ConsultaCPSistemasServiceImplBase() {

    private val logger = LoggerFactory.getLogger(ConsultaCPSistemasEndpoint::class.java)

    override fun consultaChavePixSistemas(
        request: ConsultaCPSistemaRequest,
        responseObserver: StreamObserver<ConsultaCPSistemaResponse>
    ) {

        val consultaChave = request.validar(validador)
        val grpcResponse = chavePixService.buscaChavePixSistema(consultaChave.chave)

        logger.info("Retornando informações da ChavePix solicitada. [Chave: ${consultaChave.chave}]")
        responseObserver.onNext(grpcResponse)
        responseObserver.onCompleted()
    }
}