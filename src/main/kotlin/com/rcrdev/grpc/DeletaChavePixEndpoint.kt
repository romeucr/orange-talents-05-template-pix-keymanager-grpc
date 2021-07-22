package com.rcrdev.grpc

import com.rcrdev.ChavePixDeleteRequest
import com.rcrdev.ChavePixDeleteResponse
import com.rcrdev.DeletaChavePixServiceGrpc
import com.rcrdev.bcb.service.BcbService
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.conta.service.ContaService
import com.rcrdev.grpc.extensoes.toChavePixDelete
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@Validated
@ErrorAroundAdvice
class DeletaChavePixEndpoint(
    private val validator: Validator,
    private val chavePixService: ChavePixService,
    private val contaService: ContaService,
    private val bcbService: BcbService
) : DeletaChavePixServiceGrpc.DeletaChavePixServiceImplBase() {
    private val logger = LoggerFactory.getLogger(DeletaChavePixEndpoint::class.java)

    override fun deletaPixId(
        request: ChavePixDeleteRequest,
        responseObserver: StreamObserver<ChavePixDeleteResponse>
    ) {
        val delRequest = request.toChavePixDelete(validator)

        val chavePix = chavePixService.buscaChavePix(delRequest.pixId, delRequest.clientId)

        val conta = contaService.buscaConta(chavePix.clientId, chavePix.tipoConta)

        bcbService.deletaChavePix(chavePix.chave, conta.instituicao.ispb.toString())

        chavePixService.validaEDeleta(delRequest)

        val response = ChavePixDeleteResponse.newBuilder()
            .setPixId(delRequest.pixId)
            .setIdCliente(delRequest.clientId)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}