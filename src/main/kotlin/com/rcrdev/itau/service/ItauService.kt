package com.rcrdev.itau.service

import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.conta.Conta
import com.rcrdev.itau.ItauErpClient
import com.rcrdev.itau.exceptions.ErpItauNotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.Validator

@Validated
@Singleton
class ItauService(
    private val itauErpClient: ItauErpClient,
    private val validador: Validator
) {
    private val logger = LoggerFactory.getLogger(ItauService::class.java)

    fun consultaContaCliente(clientId: String, tipo: String): Conta? {
        logger.info("Consultando ERP-Contas Itaú. [Conta: $tipo - ClientId: ${ofuscaUuid(clientId)}]")
        val erpContaResponse = itauErpClient.consultaContaErp(clientId, tipo)

        if (erpContaResponse.status == HttpStatus.NOT_FOUND) {
            logger.warn("Conta x Cliente não encontrados no ERP-Contas Itaú. [Conta: $tipo - ClientId ${ofuscaUuid(clientId)}]")
            throw ErpItauNotFoundException("Conta x Cliente não encontrados no ERP Itaú.")
        }
        logger.info("Conta x Cliente encontrados no ERP Itaú. [Conta: $tipo - ClientId ${ofuscaUuid(clientId)}]")

        return erpContaResponse.body()?.toModel(validador)
    }
}