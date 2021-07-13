package com.rcrdev.compartilhado.handlers

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, FIELD, TYPE, FUNCTION)
@Around //obrigatória para criar a error advice (around advice)
annotation class ErrorAroundAdvice()
