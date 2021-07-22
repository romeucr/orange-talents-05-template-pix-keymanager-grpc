package com.rcrdev.grpc

import com.rcrdev.ChavePixRequest
import com.rcrdev.ChavePixResponse
import com.rcrdev.RegistraChavePixServiceGrpc
import com.rcrdev.bcb.BankAccount
import com.rcrdev.bcb.BcbClient
import com.rcrdev.bcb.CreatePixKeyRequest
import com.rcrdev.bcb.Owner
import com.rcrdev.bcb.enums.AccountType
import com.rcrdev.bcb.enums.KeyType
import com.rcrdev.bcb.enums.OwnerType
import com.rcrdev.bcb.service.BcbService
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.conta.service.ContaService
import com.rcrdev.grpc.extensoes.toChavePix
import com.rcrdev.itau.service.ItauService
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@Validated
@ErrorAroundAdvice
class RegistraChavePixEndpoint(
    private val validador: Validator,
    private val itauService: ItauService,
    private val contaService: ContaService,
    private val chavePixService: ChavePixService,
    private val bcbService: BcbService
) : RegistraChavePixServiceGrpc.RegistraChavePixServiceImplBase() {
    private val logger = LoggerFactory.getLogger(RegistraChavePixEndpoint::class.java)

    override fun registraChavePix(request: ChavePixRequest, responseObserver: StreamObserver<ChavePixResponse>) {
        val novaChavePix = request.toChavePix(validador)

        val contaCliente = itauService.consultaContaCliente(novaChavePix.clientId, novaChavePix.tipoConta.name)

        if (contaCliente != null) {
            contaService.validaESalva(contaCliente)
            bcbService.createPixKey(novaChavePix, contaCliente)
        }

        chavePixService.validaESalva(novaChavePix)

        val response = ChavePixResponse.newBuilder()
            .setPixId(novaChavePix.pixId)
            .setIdCliente(novaChavePix.clientId)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

}