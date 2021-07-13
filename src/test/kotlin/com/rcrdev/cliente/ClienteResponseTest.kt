package com.rcrdev.cliente

import com.rcrdev.instituicao.InstituicaoResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ClienteResponseTest {

    @Test
    fun `deve converter ClienteResponse em modelo`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(1234L, "BANCO")
        val cliResponse = ClienteResponse("abcd-1234", "Romeu", "11122233344", instResponse)

        // AÇÃO
        val clienteModelo = cliResponse.toModel()

        // VALIDAÇÃO
        Assertions.assertEquals(cliResponse.id, clienteModelo.id)
        Assertions.assertEquals(cliResponse.nome, clienteModelo.nome)
        Assertions.assertEquals(cliResponse.cpf, clienteModelo.cpf)
        Assertions.assertEquals(cliResponse.instituicao.ispb, clienteModelo.instituicao.ispb)
        Assertions.assertEquals(cliResponse.instituicao.nome, clienteModelo.instituicao.nome)
    }
}