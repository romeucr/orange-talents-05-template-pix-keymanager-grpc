package com.rcrdev.grpc

import com.rcrdev.ChavePixRequest
import com.rcrdev.ChavePixResponse
import com.rcrdev.RegistraChavePixServiceGrpc
import com.rcrdev.chavepix.service.ChavePixService
import com.rcrdev.cliente.service.ClienteService
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.grpc.extensoes.toChavePix
import com.rcrdev.instituicao.service.InstituicaoService
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
    private val instituicaoService: InstituicaoService,
    private val clienteService: ClienteService,
    private val chavePixService: ChavePixService
): RegistraChavePixServiceGrpc.RegistraChavePixServiceImplBase() {
    private val logger = LoggerFactory.getLogger(RegistraChavePixEndpoint::class.java)

    override fun registraChavePix(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {
        // faz algumas validações, incluso se cliente já possui chave cadastrada
        val novaChavePix = request.toChavePix(validador)

        // retorna os dados do cliente e instituicao
        val cliente = itauService.consultaCliente(novaChavePix.clientId)

        // verifica se já existe a instituição e o cliente e salva se não existe
        instituicaoService.validaESalva(cliente?.instituicao)
        clienteService.validaESalva(cliente)

        // grava a Chave Pix
        chavePixService.validaESalva(novaChavePix)

        val response = ChavePixResponse.newBuilder()
            .setPixId(novaChavePix.pixId)
            .setIdCliente(novaChavePix.clientId)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()

    }

}