package com.rcrdev.chavepix.service

import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.ChavePixDelete
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.exceptions.ChavePixNotFoundException
import com.rcrdev.compartilhado.utils.ofuscaUuid
import io.grpc.Status
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class ChavePixService(private val repository: ChavePixRepository) {
    private val logger = LoggerFactory.getLogger(ChavePixService::class.java)

    @Transactional
    fun validaESalva(novaChavePix: ChavePix) {
        repository.save(novaChavePix)
        logger.info("PixId gerado com sucesso para o ClientId ${ofuscaUuid(novaChavePix.clientId)}.")
    }

    @Transactional
    fun validaEDeleta(delRequest: ChavePixDelete) {
        val chavePix = repository.findByPixIdAndClientId(delRequest.pixId, delRequest.clientId)

        if (chavePix.isEmpty) {
            logger.warn("Chave Pix ${ofuscaUuid(delRequest.pixId)} não encontrada ou não pertence ao cliente.")
            throw ChavePixNotFoundException("Chave Pix não encontrada ou não pertence ao cliente.")
        }

        repository.delete(chavePix.get())
        logger.info("Chave Pix ${ofuscaUuid(chavePix.get().pixId)} excluída com sucesso.")
    }
}