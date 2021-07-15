package com.rcrdev.grpc

import com.rcrdev.ChavePixRequest
import com.rcrdev.RegistraChavePixServiceGrpc
import com.rcrdev.RegistraChavePixServiceGrpc.RegistraChavePixServiceBlockingStub
import com.rcrdev.TipoChave
import com.rcrdev.TipoConta
import com.rcrdev.TipoConta.CONTA_POUPANCA
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.tipos.TipoChave.CPF
import com.rcrdev.chavepix.tipos.TipoConta.CONTA_CORRENTE
import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.cliente.ClienteResponse
import com.rcrdev.instituicao.Instituicao
import com.rcrdev.instituicao.InstituicaoRepository
import com.rcrdev.instituicao.InstituicaoResponse
import com.rcrdev.itau.ItauErpClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.validation.Validator

@MicronautTest(transactional = false)
internal class RegistraChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val clienteRepository: ClienteRepository,
    private val instituicaoRepository: InstituicaoRepository,
    private val itauErpClient: ItauErpClient,
    private val grpcClient: RegistraChavePixServiceBlockingStub,
    private val validador: Validator
) {

    private lateinit var instituicaoResponse: InstituicaoResponse
    private lateinit var clienteResponse: ClienteResponse
    private lateinit var instituicao: Instituicao
    private lateinit var cliente: Cliente
    private lateinit var chavePixRequestValida: ChavePixRequest

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
        clienteRepository.deleteAll()
        instituicaoRepository.deleteAll()

        instituicaoResponse = InstituicaoResponse(123L, "ITAU")
        clienteResponse = ClienteResponse(
            id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            "Fat Mike", "39913256089", instituicaoResponse
        )

        instituicao = instituicaoResponse.toModel(validador)
        cliente = clienteResponse.toModel(validador)

        instituicaoRepository.save(instituicao)
        clienteRepository.save(cliente)

        chavePixRequestValida = ChavePixRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.EMAIL)
            .setChave("email@email.com")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()
    }

    @Test
    fun `deve retornar ALREADY_EXISTS quando clienteId ja possuir ChavePix cadastrada`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(clienteResponse))

        chavePixRepository.save(ChavePix("c56dfef4-7901-44fb-84e2-a2cefb157890",
            CPF,"39913256089", CONTA_CORRENTE))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("31982606800")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
    }

    @Test
    fun `deve retornar ABORTED quando ERP Itau indisponivel`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenThrow(HttpClientException("Erro de conexão com ERP ITAU"))

        // AÇÃO
        // deixado assim porque não consegui identificar porque ele diz que foi a Illegal e não a RunTime
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("31982606800")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.ABORTED.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando TipoChave invalida BLANK`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(1L, "BRADESCO")
        val cliResponse = ClienteResponse(id = "c78dfef5-8012-55fb-95e1-a3cefb268901",
            "Cliente ChavePix-CPF inválido", "31982806877", instResponse)

        Mockito.`when`(itauErpClient.consultaCliente("bc35591d-b547-4151-a325-4a9d2cd19614"))
            .thenReturn(HttpResponse.ok(cliResponse))

        // AÇÃO         // VALIDAÇÃO
        // deixado assim porque não consegui identificar porque ele diz que foi a Illegal e não a RunTime
        assertThrows<IllegalArgumentException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
                    .setTipoChave(TipoChave.valueOf(""))
                    .setChave("31982806877")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix-CPF invalida NULLBLANK`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(1L, "BRADESCO")
        val cliResponse = ClienteResponse(id = "c78dfef5-8012-55fb-95e1-a3cefb268901",
            "Cliente ChavePix-CPF inválido", "31982806877", instResponse)

        Mockito.`when`(itauErpClient.consultaCliente("bc35591d-b547-4151-a325-4a9d2cd19614"))
            .thenReturn(HttpResponse.ok(cliResponse))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("31982806877")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix-CPF invalida REGEX`() {
        // CENÁRIO
        val instResponse = InstituicaoResponse(1L, "BRADESCO")
        val cliResponse = ClienteResponse(id = "c78dfef5-8012-55fb-95e1-a3cefb268901",
            "Cliente ChavePix-CPF inválido", "319", instResponse)

        Mockito.`when`(itauErpClient.consultaCliente("bc35591d-b547-4151-a325-4a9d2cd19614"))
            .thenReturn(HttpResponse.ok(cliResponse))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("319")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix-TELEFONE invalida REGEX`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(clienteResponse))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.TELEFONE)
                    .setChave("319123")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix-TELEFONE invalida NULLBLANK`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(clienteResponse))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.TELEFONE)
                    .setChave("")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix-EMAIL invalida NULLBLANK`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(clienteResponse))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("")
                    .setTipoConta(CONTA_POUPANCA)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar NOT_FOUND quando clienteId nao encontrado no ERP ITAU`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")).thenReturn(HttpResponse.notFound())

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(chavePixRequestValida)
        }

        // VALIDAÇÃO
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Cliente não encontrado no ERP Itaú.", status.description)
        }
    }

    @Test
    fun `deve consultar clienteId no ERP Itau retornar detalhes e HTTP Status OK`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente(cliente.id)).thenReturn(HttpResponse.ok(clienteResponse))

        // AÇÃO
        val response = itauErpClient.consultaCliente(cliente.id)

        // VALIDAÇÃO
        assertEquals(HttpStatus.OK, response.status)
        assertNotNull(response.body())
        assertEquals(clienteResponse.id, response.body()!!.id)
        assertEquals(clienteResponse.cpf, response.body()!!.cpf)
        assertEquals(clienteResponse.instituicao, response.body()!!.instituicao)
    }

    @Test
    fun `deve consultar clienteId no ERP Itau nao retornar detalhes e HTTP Status NOT_FOUND`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente(cliente.id)).thenReturn(HttpResponse.notFound())

        // AÇÃO
        val response = itauErpClient.consultaCliente(cliente.id)

        // VALIDAÇÃO
        assertEquals(HttpStatus.NOT_FOUND, response.status)
        assertNull(response.body())
    }

    @Test
    fun `deve cadastrar uma nova Instituicao`() {
        // CENÁRIO
        val novaInstituicaoResponse = InstituicaoResponse(99L, "BANCO DO BRASIL")
        val novoClienteResponse = ClienteResponse(
            id = "lkjsdu12-7901-44fb-84e2-aahudy72635dss",
            "Outro Cliente", "999999999", novaInstituicaoResponse
        )

        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(novoClienteResponse))

        // AÇÃO
        grpcClient.registraChavePix(chavePixRequestValida)
        val instGravada = instituicaoRepository.findById(novaInstituicaoResponse.ispb)

        // VALIDAÇÃO
        assertTrue(instituicaoRepository.existsById(novaInstituicaoResponse.ispb))
        assertEquals("BANCO DO BRASIL", instGravada.get().nome)
    }

    @Test
    fun `deve cadastrar um novo Cliente`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(clienteResponse))

        // AÇÃO
        val response = grpcClient.registraChavePix(chavePixRequestValida)

        // VALIDAÇÃO
        assertNotNull(response.idCliente)
        assertNotNull(response.pixId)
        assertTrue(clienteRepository.existsById(clienteResponse.id))
    }

    @Test
    fun `deve cadastrar nova Chave Pix`() {
        // CENÁRIO
        Mockito.`when`(itauErpClient.consultaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890"))
            .thenReturn(HttpResponse.ok(clienteResponse))

        // AÇÃO
        val response = grpcClient.registraChavePix(chavePixRequestValida)

        // VALIDAÇÃO
        with(response) {
            assertNotNull(idCliente)
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsByPixId(pixId))
        }

    }

    /*** MOCKS ***/
    // client gRpc para o RegistraChavePixService
    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): RegistraChavePixServiceBlockingStub {
            return RegistraChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    // Conexão com o ERP Itaú
    @MockBean(ItauErpClient::class)
    fun erpItauMock(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }
}