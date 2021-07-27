package com.rcrdev.conta.service

import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.conta.exceptions.ContaNotFoundException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class ContaServiceTest(private val contaService: ContaService) {

    @Test
    fun `deve lancar ContaNotFoundException quando busca por conta nao encontrar registro`() {
        // AÇÃO         // VALIDAÇÃO
        assertThrows<ContaNotFoundException> {
            contaService.buscaConta("c56dfef4-7901-44fb-84e2-a2cefb157890", TipoConta.CONTA_CORRENTE)
        }
    }
}