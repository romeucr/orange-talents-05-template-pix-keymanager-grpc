package com.rcrdev.chavepix

import com.rcrdev.compartilhado.utils.ofuscaUuid
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Validated
@Singleton
class ChavePixService(private val repository: ChavePixRepository) {
    private val logger = LoggerFactory.getLogger(ChavePixService::class.java)

    fun validaESalva(novaChavePix: ChavePix) {
        repository.save(novaChavePix)
        logger.info("PixId gerado com sucesso para o ClientId ${ofuscaUuid(novaChavePix.clientId)}.")
    }
}