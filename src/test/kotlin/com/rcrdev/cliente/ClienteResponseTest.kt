package com.rcrdev.cliente

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@MicronautTest
internal class ClienteResponseTest(private val validador: Validator) {

    @Test
    fun `deve retornar um objeto do modelo`() {
        // CENÁRIO
        val cliResponse =
            ClienteResponse("7fdb2d46-ba8d-4e8d-aaa9-668e9b0f1584","Tio Patinhas", "70840079036")

        // AÇÃO
        val cliente = cliResponse.toModel(validador)

        // VALIDAÇÃO
        with(cliente) {
            assertTrue(this is Cliente)
            assertEquals(cliResponse.id, id)
            assertEquals(cliResponse.nome, nome)
            assertEquals(cliResponse.cpf, cpf)
        }
    }

    @Test
    fun `deve lancar ConstraintViolationException quando CPF for vazio`() {
        // CENÁRIO
        val cliResponse =
            ClienteResponse("7fdb2d46-ba8d-4e8d-aaa9-668e9b0f1584","Tio Patinhas", "")

        // AÇÃO  // VALIDAÇÃO
        assertThrows<ConstraintViolationException> { val cliente = cliResponse.toModel(validador) }
    }
}