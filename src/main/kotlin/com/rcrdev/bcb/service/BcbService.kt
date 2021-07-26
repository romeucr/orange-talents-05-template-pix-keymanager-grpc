package com.rcrdev.bcb.service

import com.rcrdev.bcb.*
import com.rcrdev.bcb.enums.AccountType
import com.rcrdev.bcb.enums.KeyType
import com.rcrdev.bcb.enums.OwnerType.NATURAL_PERSON
import com.rcrdev.bcb.exceptions.BcbEndpointException
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.conta.Conta
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class BcbService(
    private val bcbClient: BcbClient
) {
    private val logger = LoggerFactory.getLogger(BcbService::class.java)

//    @Transactional ==>> TESTAR
    fun createPixKey(novaChavePix: ChavePix, contaCliente: Conta) {
        val owner = with(contaCliente) { Owner(NATURAL_PERSON, titular.nome, titular.cpf) }
        val bankAccount = with(contaCliente) {
            BankAccount(
                branch = agencia,
                accountNumber = numero,
                accountType = if (tipoConta == TipoConta.CONTA_CORRENTE) AccountType.CACC else AccountType.SVGS
            )
        }

        val createPixKeyRequest = with(novaChavePix) {
            CreatePixKeyRequest(tipoChave.defineBcbKeyType(), chave, bankAccount, owner)
        }

        with(createPixKeyRequest) {
            logger.info("Enviando ChavePix para registro no BCB - [Chave: $key - ISPB: ${bankAccount.participant}]")
        }

        val bcbClientHttpResponse: HttpResponse<*>
        try {
          bcbClientHttpResponse = bcbClient.createChavePix(createPixKeyRequest)
         } catch (ex: HttpClientException) {
          throw BcbEndpointException("Erro ao tentar registrar a chave no BCB")
        }

        if (bcbClientHttpResponse.status != HttpStatus.CREATED) {
            logger.warn("Falha ao registrar no BCB - [Chave: ${createPixKeyRequest.key} - " +
                    "ISPB: ${bankAccount.participant}]")

            throw BcbEndpointException("Erro ao tentar registrar a chave no BCB")
        }

        val bcbResponse = bcbClientHttpResponse.body()

        with(bcbResponse) {
            if (this?.keyType == KeyType.RANDOM) novaChavePix.chave = key
            novaChavePix.criadoEm = this?.createdAt
            logger.info("Nova ChavePix registrada com sucesso no BCB - [Chave: ${this?.key} - Cliente: ${owner.name}]")
        }
    }

    @Transactional
    fun deletaChavePix(key: String, participant: String) {
        logger.info("Enviando ChavePix para deleção no BCB. [Chave: $key - Instituição: $participant]")
        val bcbClientResponse = bcbClient
            .deleteChavePix(DeletePixKeyRequest(key, participant), key)

        if (bcbClientResponse.status != HttpStatus.OK) {
            logger.warn("Falha ao deletar ChavePix no BCB. [Chave: $key - Instituição: $participant]")
            throw BcbEndpointException("Erro ao tentar excluir a chave no BCB")
        }

        logger.info("ChavePix deletada com sucesso no BCB. [Chave: $key - Instituição: $participant]")
    }

    fun getChavePix(key: String): PixKeyDetailsResponse {
        logger.info("Consultando ChavePix no BCB. [Chave: $key]")
        val bcbClientResponse = bcbClient.getChavePix(key)

        if (bcbClientResponse.status != HttpStatus.OK) {
            logger.warn("ChavePix não cadastrada no BCB. [Chave: $key]")
            throw BcbEndpointException("ChavePix não cadastrada no BCB.")
        }

        logger.info("ChavePix encontrada. Retornando informações da ChavePix solicitada. [Chave: $key]")
        return bcbClientResponse.body()!!
    }
}