package com.rcrdev.compartilhado.handlers

import io.micronaut.aop.Around

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.TYPE)
@Around //obrigatória para criar a error advice (around advice)
annotation class ErrorAroundAdvice()
