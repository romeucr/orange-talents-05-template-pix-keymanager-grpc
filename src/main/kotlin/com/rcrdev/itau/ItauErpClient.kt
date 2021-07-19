package com.rcrdev.itau

import com.rcrdev.conta.ContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("\${itau.erp.url}")
interface ItauErpClient {

    @Get("/{clienteId}/contas{?tipo}")
    fun consultaConta(clienteId: String, tipo: String): HttpResponse<ContaResponse>

}