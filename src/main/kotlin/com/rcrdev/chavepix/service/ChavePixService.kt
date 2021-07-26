package com.rcrdev.chavepix.service

import com.rcrdev.ConsultaCPSistemaResponse
import com.rcrdev.bcb.service.BcbService
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.ChavePixDelete
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.exceptions.ChavePixNotFoundException
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.conta.service.ContaService
import com.rcrdev.grpc.extensoes.criarResponseGrpcSistema
import com.rcrdev.instituicao.InstituicaoRepository
import com.rcrdev.instituicao.exceptions.InstituicaoNotFoundException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class ChavePixService(
    private val repository: ChavePixRepository,
    private val bcbService: BcbService,
    private val contaService: ContaService,
    private val instituicaoRepository: InstituicaoRepository
) {
    private val logger = LoggerFactory.getLogger(ChavePixService::class.java)

    @Transactional
    fun validaESalva(novaChavePix: ChavePix) {
        with(novaChavePix) {
            repository.save(novaChavePix)
            logger.info("PixId gerado com sucesso! [ClientId: ${ofuscaUuid(clientId)}]")
        }
    }

    @Transactional
    fun validaEDeleta(delRequest: ChavePixDelete) {
        val chavePix = repository.findByPixIdAndClientId(delRequest.pixId, delRequest.clientId).get()
        with(chavePix) {
            // ChavePix sempre será existente porque isso já foi validado no buscaChavePix()
            repository.delete(this)
            logger.info("ChavePix excluída com sucesso. [PixId: ${ofuscaUuid(this.pixId)}]")
        }
    }

    fun buscaChavePix(pixId: String, clientId: String): ChavePix {
        logger.info("Buscando ChavePix. [PixId: ${ofuscaUuid(pixId)}]")
        val chavePix = repository.findByPixIdAndClientId(pixId, clientId)

        with (chavePix) {
            if (isEmpty) {
                logger.warn("ChavePix não encontrada ou não pertence ao cliente. " +
                        "[PixId: ${ofuscaUuid(pixId)}]")
                throw ChavePixNotFoundException("ChavePix não encontrada ou não pertence ao cliente.")
            }
        }

        return chavePix.get()
    }

    fun buscaChavePixSistema(key: String): ConsultaCPSistemaResponse {
        val optChavePix = repository.findByChave(key)

        if (optChavePix.isPresent) {
            val chavePix = optChavePix.get()
            val conta = contaService.buscaConta(chavePix.clientId, chavePix.tipoConta)

            return criarResponseGrpcSistema(chavePix, conta)
        }
        val bcbResponse = bcbService.getChavePix(key)
        val optInst = instituicaoRepository.findById(bcbResponse.bankAccount.participant.toInt())
        if (optInst.isPresent) {
            val instituicao = optInst.get()
            return criarResponseGrpcSistema(bcbResponse, instituicao.nome)
        } else {
            throw InstituicaoNotFoundException("Instituição não cadastrada no sistema")
        }

    }

}