package com.rcrdev.conta.service

import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.conta.Conta
import com.rcrdev.conta.ContaRepository
import com.rcrdev.conta.exceptions.ContaNotFoundException
import com.rcrdev.instituicao.InstituicaoRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class ContaService(
    private val instituicaoRepository: InstituicaoRepository,
    private val clienteRepository: ClienteRepository,
    private val contaRepository: ContaRepository
) {
    private val logger = LoggerFactory.getLogger(ContaService::class.java)

    @Transactional
    fun validaESalva(conta: Conta?) {
        if (conta != null) {
            if (!instituicaoRepository.existsById(conta.instituicao.ispb)) { //existsById não permite Int?
                with(conta) {
                    instituicaoRepository.save(instituicao)
                    logger.info("Instituição armazenada com sucesso na base de dados. " +
                            "[Instituição: ${instituicao.nome} - ISPB: ${instituicao.ispb}]")
                }
            }

            if (!clienteRepository.existsById(conta.titular.id)) {
                with (conta) {
                    clienteRepository.save(titular)
                    logger.info("Cliente armazenado com sucesso na base de dados. [ID: ${ofuscaUuid(titular.id)}]")
                }
            }

            if(!contaRepository.existsByAgenciaNumeroTipo(conta.agencia, conta.numero, conta.tipoConta.name)) {
                with (conta) {
                    contaRepository.save(conta)
                    logger.info("Conta armazenada com sucesso na base de dados. " +
                            "[Tipo: $tipoConta - Ag: $agencia - Número: $numero]")
                }
            }
        }
    }

    fun buscaConta(clientId: String, tipoConta: TipoConta): Conta {
        logger.info("Buscando Conta. [ClientId: ${ofuscaUuid(clientId)} - $tipoConta]")
        val conta = contaRepository.findByClientIdAndTipoConta(clientId, tipoConta.name)

        if (conta.isEmpty) {
            logger.warn("Não foi encontrada Conta do tipo para o cliente informado. " +
                    "[Tipo: $tipoConta - Cliente: ${ofuscaUuid(clientId)}]")
            throw ContaNotFoundException("Não foi encontrada Conta do tipo para o Cliente informado.")
        }

        logger.info("Conta encontrada. [ClientId: ${ofuscaUuid(clientId)} - $tipoConta]")
        return conta.get()
    }
}