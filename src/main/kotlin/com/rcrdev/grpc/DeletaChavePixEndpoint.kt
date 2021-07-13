package com.rcrdev.grpc

import com.rcrdev.ChavePixDeleteRequest
import com.rcrdev.ChavePixDeleteResponse
import com.rcrdev.DeletaChavePixServiceGrpc
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.grpc.extensoes.toChavePixDelete
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@Validated
@ErrorAroundAdvice
class DeletaChavePixEndpoint(
    private val chavePixRepository: ChavePixRepository,
    private val validator: Validator
): DeletaChavePixServiceGrpc.DeletaChavePixServiceImplBase() {
    private val logger = LoggerFactory.getLogger(DeletaChavePixEndpoint::class.java)

     override fun deletaPixId(request: ChavePixDeleteRequest?, responseObserver: StreamObserver<ChavePixDeleteResponse>?)
    {
        val delRequest = request?.toChavePixDelete(validator)

        val chavePix = chavePixRepository.findByPixIdAndClientId(delRequest?.pixId, delRequest?.clientId)

        if (chavePix.isEmpty) {
            logger.warn("Chave Pix ${ofuscaUuid(delRequest?.pixId)} não encontrada ou não pertence ao cliente.")
            val error = Status.NOT_FOUND
                .withDescription("Chave Pix não encontrada ou não pertence ao cliente.")
                .asRuntimeException()

            responseObserver?.onError(error)
            return
        }

        chavePixRepository.delete(chavePix.get())
        logger.info("Chave Pix ${ofuscaUuid(request?.pixId)} excluída com sucesso.")

        val response = ChavePixDeleteResponse.newBuilder()
            .setPixId(delRequest?.pixId)
            .setIdCliente(delRequest?.clientId)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}