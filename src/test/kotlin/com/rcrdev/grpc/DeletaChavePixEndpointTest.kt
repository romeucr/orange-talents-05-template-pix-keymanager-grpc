package com.rcrdev.grpc

import com.rcrdev.ChavePixDeleteRequest
import com.rcrdev.DeletaChavePixServiceGrpc
import com.rcrdev.DeletaChavePixServiceGrpc.DeletaChavePixServiceBlockingStub
import com.rcrdev.bcb.BcbClient
import com.rcrdev.bcb.DeletePixKeyRequest
import com.rcrdev.bcb.DeletePixKeyResponse
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.tipos.TipoChave
import com.rcrdev.chavepix.tipos.TipoConta.CONTA_CORRENTE
import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.conta.Conta
import com.rcrdev.conta.ContaRepository
import com.rcrdev.instituicao.Instituicao
import com.rcrdev.instituicao.InstituicaoRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DeletaChavePixEndpointTest(
    private val grpcClientDeleta: DeletaChavePixServiceBlockingStub,
    private val chavePixRepository: ChavePixRepository,
    private val contaRepository: ContaRepository,
    private val clienteRepository: ClienteRepository,
    private val instituicaoRepository: InstituicaoRepository
) {
    lateinit var CLIENTID_EXISTENTE: String
    lateinit var CHAVE_EXISTENTE: ChavePix
    lateinit var PIXID_INEXISTENTE: String
    lateinit var CLIENTID_INEXISTENTE: String

    lateinit var deletePixKeyRequest: DeletePixKeyRequest
    lateinit var deletePixKeyResponse: DeletePixKeyResponse

    @Inject
    private lateinit var bcbClient: BcbClient

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll()
        clienteRepository.deleteAll()
        instituicaoRepository.deleteAll()

        CLIENTID_EXISTENTE = "7fdb2d46-ba8d-4e8d-aaa9-668e9b0f1584"
        PIXID_INEXISTENTE = "6894853b-0fba-4c48-a2df-e887240e0ae5"
        CLIENTID_INEXISTENTE = "70feb5b4-41a4-4454-bc72-ad7642d6a52a"

        CHAVE_EXISTENTE = ChavePix(
            clientId = CLIENTID_EXISTENTE,
            tipoChave = TipoChave.CPF,
            chave = "86318132066",
            tipoConta = CONTA_CORRENTE
        )
        chavePixRepository.save(CHAVE_EXISTENTE)


        val instituicao = Instituicao(12345, "NULLBAK")
        val cliente = Cliente("7fdb2d46-ba8d-4e8d-aaa9-668e9b0f1584", "Patolino da Silva", "86318132066")
        val conta = Conta(CONTA_CORRENTE, instituicao, "0001", "350176", cliente)

        instituicaoRepository.save(instituicao)
        clienteRepository.save(cliente)
        contaRepository.save(conta)

        deletePixKeyRequest = DeletePixKeyRequest("86318132066", "12345")
        deletePixKeyResponse = DeletePixKeyResponse("86318132066", "12345")
    }

    @Test
    fun `deve retornar ABORTED quando BCB retornar diferente de HttpStatus OK`() {
        // CENÁRIO
        val request = ChavePixDeleteRequest.newBuilder()
            .setIdCliente(CHAVE_EXISTENTE.clientId)
            .setPixId(CHAVE_EXISTENTE.pixId)
            .build()

        Mockito.`when`(bcbClient.deleteChavePix(deletePixKeyRequest, "86318132066"))
            .thenReturn(HttpResponse.badRequest())

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
           grpcClientDeleta.deletaPixId(request) }

        // VALIDAÇÃO
        assertEquals(Status.ABORTED.code, error.status.code)
    }

    @Test
    fun `nao deve remover PIX ID quando chave inexistente`() {
        // CENARIO
        val request = ChavePixDeleteRequest.newBuilder()
            .setIdCliente(CLIENTID_EXISTENTE)
            .setPixId(PIXID_INEXISTENTE)
            .build()

        // ACAO
        val error = assertThrows<StatusRuntimeException> {
            grpcClientDeleta.deletaPixId(request)
        }

        // VALIDACAO
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("NOT_FOUND: ChavePix não encontrada ou não pertence ao cliente.", error.message)
    }

    @Test
    fun `nao deve remover PIX ID quando chave existente mas pertence a outro usuario`() {
        // CENARIO
        val outroClientId = "70feb5b4-41a4-4454-bc72-ad7642d6a52a"

        val request = ChavePixDeleteRequest.newBuilder()
            .setIdCliente(outroClientId)
            .setPixId(CHAVE_EXISTENTE.pixId)
            .build()

        // ACAO
        val error = assertThrows<StatusRuntimeException> {
            grpcClientDeleta.deletaPixId(request)
        }

        // VALIDACAO
        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("NOT_FOUND: ChavePix não encontrada ou não pertence ao cliente.", error.message)
    }

    @Test
    fun `deve remover PIX ID existente do usuario que solicitou`() {
        // CENARIO
        val request = ChavePixDeleteRequest.newBuilder()
            .setIdCliente(CHAVE_EXISTENTE.clientId)
            .setPixId(CHAVE_EXISTENTE.pixId)
            .build()

        Mockito.`when`(bcbClient.deleteChavePix(deletePixKeyRequest, "86318132066"))
            .thenReturn(HttpResponse.ok(deletePixKeyResponse))

        // ACAO
        val response = grpcClientDeleta.deletaPixId(request)

        // VALIDACAO
        assertEquals(CHAVE_EXISTENTE.clientId, response.idCliente)
        assertEquals(CHAVE_EXISTENTE.pixId, response.pixId)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando request conter pixId vazio`() {
        // CENARIO
        val request = ChavePixDeleteRequest.newBuilder()
            .setIdCliente("6894853b-0fba-4c48-a2df-e887240e0ae5")
            .setPixId("")
            .build()

        // AÇAO
        val error = assertThrows<StatusRuntimeException> {
            grpcClientDeleta.deletaPixId(request)
        }

        // VALIDACAO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("INVALID_ARGUMENT: pixId: must not be blank", error.message)

    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando request conter clientId vazio`() {
        // CENARIO
        val request = ChavePixDeleteRequest.newBuilder()
            .setIdCliente("")
            .setPixId("70feb5b4-41a4-4454-bc72-ad7642d6a52a")
            .build()

        // AÇAO
        val error = assertThrows<StatusRuntimeException> {
            grpcClientDeleta.deletaPixId(request)
        }

        // VALIDACAO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("INVALID_ARGUMENT: clientId: must not be blank", error.message)
    }


    /*** MOCKS ***/
    // client gRpc para o DeletaChavePixService
    @Factory
    class Clients {
        @Bean
        fun blockingStubDeleta(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): DeletaChavePixServiceBlockingStub {
            return DeletaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    // Conexão com o BCB
    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

}