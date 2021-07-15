package com.rcrdev.cliente

import com.rcrdev.compartilhado.utils.ofuscaUuid
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
@Validated
class ClienteService(private val repository: ClienteRepository) {
    private val logger = LoggerFactory.getLogger(ClienteService::class.java)

    fun validaESalva(cliente: Cliente?) {
        if (cliente != null) {
            if (!repository.existsById(cliente.id)) {
                repository.save(cliente)
                logger.info("Nova Cliente inserido na base de dados: ${ofuscaUuid(cliente.id)}.")
            }
        }
    }

}