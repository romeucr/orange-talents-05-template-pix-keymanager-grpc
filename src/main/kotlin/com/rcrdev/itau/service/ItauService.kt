package com.rcrdev.itau.service

import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.conta.Conta
import com.rcrdev.itau.ItauErpClient
import com.rcrdev.itau.exceptions.ErpItauClientNotFoundException
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
        logger.info("Consultando ERP Itaú. Conta $tipo - ClientId: ${ofuscaUuid(clientId)}. Conta: $tipo")
        val erpContaResponse = itauErpClient.consultaConta(clientId, tipo)

        if (erpContaResponse.status == HttpStatus.NOT_FOUND) {
            logger.warn("Conta $tipo - ClientId ${ofuscaUuid(clientId)} não encontrados no ERP Itaú.")
            throw ErpItauClientNotFoundException("Cliente não encontrado no ERP Itaú.")
        }
        logger.info("Conta $tipo - ClientId ${ofuscaUuid(clientId)} encontrados no ERP Itaú.")

        println(erpContaResponse.body())

        val conta = erpContaResponse.body()?.toModel(validador)
        return conta
    }
}