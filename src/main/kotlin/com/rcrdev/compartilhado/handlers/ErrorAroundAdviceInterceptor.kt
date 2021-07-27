package com.rcrdev.compartilhado.handlers

import com.rcrdev.bcb.exceptions.BcbEndpointException
import com.rcrdev.chavepix.exceptions.ChavePixNotFoundException
import com.rcrdev.instituicao.exceptions.InstituicaoNotFoundException
import com.rcrdev.itau.exceptions.ErpItauNotFoundException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundAdvice::class)
class ErrorAroundAdviceInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        try {
            context.proceed()
        } catch (ex: Exception) {
            // segundo elemento que recebe o método registraPixId da classe ChavePixGrpcEndpoint
            // para poder retornar o responseObserver(response gRPC)
            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (ex) {
                is ConstraintViolationException ->
                    if (ex.message!!.contains("Já existe Chave Pix gerada para o valor informado.")) {
                        Status.ALREADY_EXISTS
                            .withCause(ex)
                            .withDescription(ex.message)
                    } else
                        Status.INVALID_ARGUMENT
                            .withCause(ex)
                            .withDescription(ex.message)

                is HttpClientException,
                is InstituicaoNotFoundException,
                is BcbEndpointException -> Status.ABORTED
                    .withCause(ex)
                    .withDescription(ex.message)

                is ErpItauNotFoundException,
                is ChavePixNotFoundException -> Status.NOT_FOUND
                    .withCause(ex)
                    .withDescription(ex.message)

                else -> Status.UNKNOWN
                    .withCause(ex)
                    .withDescription("Erro inesperado")
            }

            responseObserver.onError(status.asRuntimeException())
        }
        return null
    }
}