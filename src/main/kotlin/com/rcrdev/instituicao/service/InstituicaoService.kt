package com.rcrdev.conta.service

import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.compartilhado.utils.ofuscaUuid
import com.rcrdev.instituicao.Instituicao
import com.rcrdev.instituicao.InstituicaoRepository
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class InstituicaoService(private val repository: InstituicaoRepository) {
    private val logger = LoggerFactory.getLogger(InstituicaoService::class.java)

    @Transactional
    fun validaESalva(instituicao: Instituicao?) {
        if (instituicao != null && !repository.existsById(instituicao.ispb)) {
            repository.save(instituicao)
            logger.info("Instituicao: ${instituicao.nome} - ISPB: ${instituicao.ispb} armazenada com sucesso na base de dados.")
        }
    }
}