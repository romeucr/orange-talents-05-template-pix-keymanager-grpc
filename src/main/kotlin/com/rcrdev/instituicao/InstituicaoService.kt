package com.rcrdev.instituicao

import com.rcrdev.grpc.RegistraChavePixEndpoint
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Validated
class InstituicaoService(private val repository: InstituicaoRepository) {
    private val logger = LoggerFactory.getLogger(InstituicaoService::class.java)

    @Transactional
    fun validaESalva(instituicao: Instituicao?) {
        if (instituicao != null) {
            if (!repository.existsById(instituicao.ispb)) {
                repository.save(instituicao)
                logger.info("Nova Instituição inserida na base de dados: ${instituicao.nome}.")
            }
        }
    }
}