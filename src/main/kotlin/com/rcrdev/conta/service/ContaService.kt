package com.rcrdev.conta.service

import com.rcrdev.conta.Conta
import com.rcrdev.conta.ContaRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class ContaService(
    private val repository: ContaRepository,
    private val clienteService: ClienteService,
    private val instituicaoService: InstituicaoService
) {
    private val logger = LoggerFactory.getLogger(InstituicaoService::class.java)

    @Transactional
    fun validaESalva(conta: Conta?) {
        if (conta != null && !repository.existsByAgenciaAndNumero(conta.agencia, conta.numero)) {
            instituicaoService.validaESalva(conta.instituicao)
            clienteService.validaESalva(conta.titular)
            repository.save(conta)
            logger.info("Conta ${conta.tipoConta} - Ag: ${conta.agencia} - Número: ${conta.numero} " +
                    "armazenados com sucesso na base de dados.")
        }
    }
}