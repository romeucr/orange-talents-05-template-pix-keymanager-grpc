package com.rcrdev.conta

import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteResponse
import com.rcrdev.instituicao.InstituicaoResponse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@MicronautTest
internal class ContaResponseTest(private val validador: Validator){

    @Test
    fun `deve retornar um objeto do modelo`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(12345, "NULLBANK")
        val cliResponse =
            ClienteResponse("7fdb2d46-ba8d-4e8d-aaa9-668e9b0f1584","Tio Patinhas", "70840079036")
        val contaResponse =
            ContaResponse(TipoConta.CONTA_CORRENTE.name, instResponse, "0001", "123123", cliResponse)

        // AÇÃO
        val conta = contaResponse.toModel(validador)

        // VALIDAÇÃO
        with(conta) {
            assertTrue(this is Conta)
            assertEquals(contaResponse.tipo, tipoConta.name)
            assertEquals(contaResponse.instituicao.ispb, instituicao.ispb)
            assertEquals(contaResponse.agencia, agencia)
            assertEquals(contaResponse.numero, numero)
            assertEquals(contaResponse.titular.id, titular.id)
        }
    }

    @Test
    fun `deve lancar ConstraintViolationException quando Agencia for vazio`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(12345, "NULLBANK")
        val cliResponse =
            ClienteResponse("7fdb2d46-ba8d-4e8d-aaa9-668e9b0f1584","Tio Patinhas", "70840079036")
        val contaResponse =
            ContaResponse(TipoConta.CONTA_CORRENTE.name, instResponse, "", "123123", cliResponse)

        // AÇÃO  // VALIDAÇÃO
        assertThrows<ConstraintViolationException> {
            val conta = contaResponse.toModel(validador)
        }
    }
}