package com.rcrdev.instituicao

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.validation.Validator

internal class InstituicaoResponseTest(private val validador: Validator) {
    @Test
    fun `deve converter Instituicao em modelo`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(1234L, "BANCO")

        // AÇÃO
        val instModelo = instResponse.toModel(validador)

        // VALIDAÇÃO
        Assertions.assertEquals(instResponse.ispb, instModelo.ispb)
        Assertions.assertEquals(instResponse.nome, instModelo.nome)
    }
}