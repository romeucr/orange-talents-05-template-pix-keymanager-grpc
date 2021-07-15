package com.rcrdev.chavepix.validadores

import com.rcrdev.chavepix.ChavePixRepository
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD

@MustBeDocumented
@Target(FIELD, CONSTRUCTOR)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValorUnicoValidador::class])
annotation class ValorUnico(
    val message: String = "Já existe Chave Pix gerada para o valor informado.",
)

@Singleton
class ValorUnicoValidador(private val chavePixRepository: ChavePixRepository) :
    ConstraintValidator<ValorUnico, String> {

    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValorUnico>,
        context: ConstraintValidatorContext
    ): Boolean {

        val campos = annotationMetadata.values

        if (campos.containsValue("clientId"))
            return chavePixRepository.existsByClientId(value.toString())

        if (campos.containsValue("chave"))
            return chavePixRepository.existsByChave(value.toString())

        return false
    }

}

