package com.rcrdev.conta.service

import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.compartilhado.utils.ofuscaUuid
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class ClienteService(private val repository: ClienteRepository) {
    private val logger = LoggerFactory.getLogger(InstituicaoService::class.java)

    @Transactional
    fun validaESalva(cliente: Cliente?) {
        if (cliente != null && !repository.existsById(cliente.id)) {
            repository.save(cliente)
            logger.info("Client ID ${ofuscaUuid(cliente.id)} armazenado com sucesso na base de dados.")
        }
    }
}