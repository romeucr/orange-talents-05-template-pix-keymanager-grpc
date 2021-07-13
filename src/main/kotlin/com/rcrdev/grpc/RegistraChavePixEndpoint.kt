package com.rcrdev.grpc

import com.rcrdev.ChavePixRequest
import com.rcrdev.ChavePixResponse
import com.rcrdev.PixServiceGrpc
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.compartilhado.handlers.ErrorAroundAdvice
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.grpc.extensoes.toChavePix
import com.rcrdev.instituicao.InstituicaoRepository
import com.rcrdev.itau.ItauErpClient
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@Validated
@ErrorAroundAdvice
class RegistraChavePixEndpoint(
    private val chavePixRepository: ChavePixRepository,
    private val clienteRepository: ClienteRepository,
    private val instituicaoRepository: InstituicaoRepository,
    private val itauErpClient: ItauErpClient,
    private val validador: Validator,
): PixServiceGrpc.PixServiceImplBase() {
    private val logger = LoggerFactory.getLogger(RegistraChavePixEndpoint::class.java)

    override fun registraChavePix(request: ChavePixRequest?, responseObserver: StreamObserver<ChavePixResponse>?) {

        val novaChavePix = request?.toChavePix(validador)

        // verifica se cliente existe no ERP Itaú
        logger.info("Consultando ClientId ${ofuscaUuid(novaChavePix?.clientId)} no ERP Itaú.")
        val itauErpResponse = novaChavePix?.let { itauErpClient.consultaCliente(it.clientId) }

        if (itauErpResponse != null) {
            if (itauErpResponse.status == HttpStatus.NOT_FOUND) {
                logger.warn("ClientId ${ofuscaUuid(novaChavePix.clientId)} não encontrado no ERP Itaú.")
                val error = Status.NOT_FOUND
                    .withDescription("Cliente não encontrado no ERP Itaú.")
                    .asRuntimeException()

                responseObserver?.onError(error)
                return
            }
        }
        logger.info("ClientId ${novaChavePix?.let { ofuscaUuid(it.clientId) }} encontrado no ERP Itaú.")

        //salva instituicao no banco de dados
        val cliente = itauErpResponse?.body()?.toModel() ?: throw IllegalArgumentException()
        if (!instituicaoRepository.existsById(cliente.instituicao.ispb)) {
            instituicaoRepository.save(cliente.instituicao)
            logger.info("Nova Instituicao (${cliente.instituicao.nome}) inserida na base de dados.")
        }

        //salva cliente no banco de dados
        if (!clienteRepository.existsById(cliente.id)) {
            clienteRepository.save(cliente)
            logger.info("ClientId ${ofuscaUuid(novaChavePix.clientId)} inserido na base de dados.")
        }

        //salva Chave Pix
        chavePixRepository.save(novaChavePix)
        logger.info("PixId gerado com sucesso para o ClientId ${ofuscaUuid(novaChavePix.clientId)}!")

        val response = ChavePixResponse.newBuilder()
            .setPixId(novaChavePix.pixId)
            .setIdCliente(novaChavePix.clientId)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()

    }

}