package com.rcrdev.instituicao

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class InstituicaoResponseTest {
    @Test
    fun `deve converter Instituicao em modelo`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(1234L, "BANCO")

        // AÇÃO
        val instModelo = instResponse.toModel()

        // VALIDAÇÃO
        Assertions.assertEquals(instResponse.ispb, instModelo.ispb)
        Assertions.assertEquals(instResponse.nome, instModelo.nome)
    }
}