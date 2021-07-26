package com.rcrdev.instituicao

import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteResponse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@MicronautTest
internal class InstituicaoResponseTest(private val validador: Validator) {

    @Test
    fun `deve retornar um objeto do modelo`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(12345, "NULLBANK")

        // AÇÃO
        val instituicao = instResponse.toModel(validador)

        // VALIDAÇÃO
        with(instituicao) {
            assertTrue(this is Instituicao)
            assertEquals(instResponse.ispb, ispb)
            assertEquals(instResponse.nome, nome)
        }
    }

    @Test
    fun `deve lancar ConstraintViolationException quando Nome for vazio`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(12345, "")

        // AÇÃO  // VALIDAÇÃO
        assertThrows<ConstraintViolationException> {
            instResponse.toModel(validador)
        }
    }
}