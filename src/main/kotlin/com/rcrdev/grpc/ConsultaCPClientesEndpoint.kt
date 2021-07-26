package com.rcrdev.grpc

import com.rcrdev.ConsultaCPClienteRequest
import com.rcrdev.ConsultaCPClienteResponse
import com.rcrdev.ConsultaCPClientesServiceGrpc
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.conta.Conta
import com.rcrdev.conta.service.ContaService
import com.rcrdev.grpc.extensoes.criarResponseGrpcCliente
import com.rcrdev.grpc.extensoes.validar
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Validated
@ErrorAroundAdvice
@Singleton
class ConsultaCPClientesEndpoint(
    private val validador: Validator,
    private val chavePixService: ChavePixService,
    private val contaService: ContaService
): ConsultaCPClientesServiceGrpc.ConsultaCPClientesServiceImplBase(){
    private val logger = LoggerFactory.getLogger(ConsultaCPClientesEndpoint::class.java)

    override fun consultaChavePixClientes(
        request: ConsultaCPClienteRequest,
        responseObserver: StreamObserver<ConsultaCPClienteResponse>
    ) {

        val consultaChave = request.validar(validador)
        val chavePix: ChavePix
        with (consultaChave) {
            chavePix = chavePixService.buscaChavePix(pixId, clientId)
        }

        val conta: Conta
        with (chavePix) {
            conta = contaService.buscaConta(clientId, tipoConta)
        }

        logger.info("Retornando informações da ChavePix solicitada. [PixId: ${ofuscaUuid(chavePix.pixId)} - " +
                "Cliente: ${ofuscaUuid(conta.titular.id)}]")
        responseObserver.onNext(criarResponseGrpcCliente(chavePix, conta))
        responseObserver.onCompleted()
    }
}