package com.rcrdev.itau.service

import com.rcrdev.cliente.Cliente
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.itau.ItauErpClient
import com.rcrdev.itau.exceptions.ErpItauClientNotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Validated
@Singleton
//@ErrorAroundAdvice
class ItauService(
    private val itauErpClient: ItauErpClient,
    private val validador: Validator
) {
    private val logger = LoggerFactory.getLogger(ItauService::class.java)

    fun consultaCliente(clientId: String): Cliente? {
        logger.info("Consultando ERP Itaú. ClientId: ${ofuscaUuid(clientId)}.")
        // verifica se cliente existe no ERP Itaú. Testar quando Itau estiver offline (usar try/catch ???)
        val erpResponse = itauErpClient.consultaCliente(clientId)

        if (erpResponse.status == HttpStatus.NOT_FOUND) {
            logger.warn("ClientId ${ofuscaUuid(clientId)} não encontrado no ERP Itaú.")
            throw ErpItauClientNotFoundException("Cliente não encontrado no ERP Itaú.")
        }
        logger.info("ClientId ${ofuscaUuid(clientId)} encontrado no ERP Itaú.")

        return erpResponse.body()?.toModel(validador)
    }

    fun consultaContaCliente() {

    }
}