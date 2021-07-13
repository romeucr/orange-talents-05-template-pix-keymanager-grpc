package com.rcrdev.compartilhado.handlers

import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientException
import java.lang.IllegalArgumentException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundAdvice::class)
class ErrorAroundAdviceInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            context.proceed()
        } catch (ex: Exception) {
            //segundo elemento que recebe o método registraPixId da classe ChavePixGrpcEndpoint
            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when(ex) {
                is ConstraintViolationException ->
                    if (ex.message!!.contains("já possui um PixId")) {
                        Status.ALREADY_EXISTS
                            .withCause(ex)
                            .withDescription(ex.message)
                    } else
                        Status.INVALID_ARGUMENT
                            .withCause(ex)
                            .withDescription(ex.message)

                is IllegalArgumentException -> Status.INVALID_ARGUMENT
                    .withCause(ex)
                    .withDescription("Erro inesperado")

                is HttpClientException -> Status.ABORTED
                    .withCause(ex)
                    .withDescription("Não foi possível validar o cliente no ERP Itaú.")

                else -> Status.UNKNOWN
                    .withCause(ex)
                    .withDescription("Erro desconhecido")
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}