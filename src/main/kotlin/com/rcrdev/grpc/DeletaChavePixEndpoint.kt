package com.rcrdev.grpc

import com.rcrdev.ChavePixDeleteRequest
import com.rcrdev.ChavePixDeleteResponse
import com.rcrdev.DeletaChavePixServiceGrpc
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.grpc.extensoes.toChavePixDelete
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Validator

@Singleton
@Validated
@ErrorAroundAdvice
class DeletaChavePixEndpoint(
    private val validator: Validator,
    private val chavePixService: ChavePixService
): DeletaChavePixServiceGrpc.DeletaChavePixServiceImplBase() {
    private val logger = LoggerFactory.getLogger(DeletaChavePixEndpoint::class.java)

     override fun deletaPixId(request: ChavePixDeleteRequest,
                              responseObserver: StreamObserver<ChavePixDeleteResponse>) {

        val delRequest = request.toChavePixDelete(validator)
        chavePixService.validaEDeleta(delRequest)

        val response = ChavePixDeleteResponse.newBuilder()
            .setPixId(delRequest.pixId)
            .setIdCliente(delRequest.clientId)
            .build()

         responseObserver.onNext(response)
         responseObserver.onCompleted()
    }
}