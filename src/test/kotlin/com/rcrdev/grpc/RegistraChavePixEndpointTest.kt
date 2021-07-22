package com.rcrdev.grpc

import com.rcrdev.ChavePixRequest
import com.rcrdev.RegistraChavePixServiceGrpc
import com.rcrdev.RegistraChavePixServiceGrpc.RegistraChavePixServiceBlockingStub
import com.rcrdev.TipoChave
import com.rcrdev.TipoConta
import com.rcrdev.bcb.*
import com.rcrdev.bcb.enums.AccountType
import com.rcrdev.bcb.enums.AccountType.*
import com.rcrdev.bcb.enums.KeyType
import com.rcrdev.bcb.enums.KeyType.*
import com.rcrdev.bcb.enums.OwnerType
import com.rcrdev.bcb.enums.OwnerType.*
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.tipos.TipoChave.CPF
import com.rcrdev.chavepix.tipos.TipoConta.CONTA_CORRENTE
import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.cliente.ClienteResponse
import com.rcrdev.conta.Conta
import com.rcrdev.conta.ContaRepository
import com.rcrdev.conta.ContaResponse
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
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.validation.Validator

@MicronautTest(transactional = false)
internal class RegistraChavePixEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val instituicaoRepository: InstituicaoRepository,
    private val clienteRepository: ClienteRepository,
    private val contaRepository: ContaRepository,
    private val grpcClient: RegistraChavePixServiceBlockingStub,
    private val validador: Validator
) {

    private lateinit var instResponse: InstituicaoResponse
    private lateinit var cliResponse: ClienteResponse
    private lateinit var contaResponse: ContaResponse

    private lateinit var instituicao: Instituicao
    private lateinit var cliente: Cliente
    private lateinit var conta: Conta

    private lateinit var chavePixRequest: ChavePixRequest

    private lateinit var owner: Owner
    private lateinit var bankAccount: BankAccount
    private lateinit var createPKRequest: CreatePixKeyRequest
    private lateinit var createPKResponse: CreatePixKeyResponse

    @Inject lateinit var itauErpClient: ItauErpClient

    @Inject lateinit var bcbClient: BcbClient



    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll() // manter a ordem de delete por causa dos relacionamentos
        clienteRepository.deleteAll()
        instituicaoRepository.deleteAll()

        instResponse = InstituicaoResponse(12345, "NULLBANK")
        cliResponse = ClienteResponse("c56dfef4-8002-44fb-85e3-a2cefb159999", "Patolino De Souza", "95094701045")
        contaResponse = ContaResponse(CONTA_CORRENTE.name, instResponse, "0001", "350176", cliResponse)

        instituicao = instResponse.toModel(validador)
        cliente = cliResponse.toModel(validador)
        conta = contaResponse.toModel(validador)

        chavePixRequest = ChavePixRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoChave.CPF)
            .setChave("24311919077")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        owner = Owner(type = LEGAL_PERSON, name = "Ander Castro", taxIdNumber = "12345365377")
        bankAccount = BankAccount(branch = "1122", accountNumber = "332211", accountType = CACC)
        createPKRequest = CreatePixKeyRequest(keyType = EMAIL, key = "ander@email.com",
                                        bankAccount = bankAccount, owner = owner)

        createPKResponse = CreatePixKeyResponse(keyType = EMAIL, key = "ander@email.com",
            bankAccount = bankAccount, owner = owner, LocalDateTime.now())
    }

    @Test
    fun `deve cadastrar nova Chave Pix`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("c56dfef4-7901-44fb-84e2-a2cefb157890", CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(contaResponse))

        `when`(bcbClient.createChavePix(createPKRequest)).thenReturn(HttpResponse.ok(createPKResponse))

        // AÇÃO
        val response = grpcClient.registraChavePix(chavePixRequest)

        // VALIDAÇÃO
        with(response) {
            assertNotNull(idCliente)
            assertNotNull(pixId)
            assertTrue(chavePixRepository.existsByPixId(pixId))
        }

    }

    @Test
    fun `deve retornar ALREADY_EXISTS quando cliente ja possuir ChavePix cadastrada`() {
        // CENÁRIO
        chavePixRepository
            .save(ChavePix("c56dfef4-7901-44fb-84e2-a2cefb157890", CPF, "39913256089", CONTA_CORRENTE))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("39913256089")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // VALIDAÇÃO
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
    }

    @Test
    fun `deve cadastrar uma nova Instituicao`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("c56dfef4-7901-44fb-84e2-a2cefb157890", CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(contaResponse))

        val novaInstituicao = contaResponse.instituicao.toModel(validador)

        // AÇÃO
        grpcClient.registraChavePix(chavePixRequest)

        // VALIDAÇÃO
        with(novaInstituicao) {
            assertTrue(instituicaoRepository.existsById(ispb))
            assertEquals(12345, ispb)
            assertEquals("NULLBANK", nome)
        }
    }

    @Test
    fun `deve cadastrar um novo Cliente`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("c56dfef4-7901-44fb-84e2-a2cefb157890", CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(contaResponse))

        val novoCliente = contaResponse.titular.toModel(validador)

        // AÇÃO
        grpcClient.registraChavePix(chavePixRequest)

        // VALIDAÇÃO
        with(novoCliente) {
            assertTrue(clienteRepository.existsById(id))
            assertEquals("c56dfef4-8002-44fb-85e3-a2cefb159999", id)
            assertEquals("Patolino De Souza", nome)
            assertEquals("95094701045", cpf)
        }
    }

    @Test
    fun `deve cadastrar uma nova Conta`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("c56dfef4-7901-44fb-84e2-a2cefb157890", CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.ok(contaResponse))

        val novaInstituicao = contaResponse.instituicao.toModel(validador)
        val novoCliente = contaResponse.titular.toModel(validador)
        val novaConta = contaResponse.toModel(validador)

        // AÇÃO
        grpcClient.registraChavePix(chavePixRequest)

        // VALIDAÇÃO
        with(novaConta) {
            assertTrue(contaRepository.existsByAgenciaAndNumero(agencia, numero))
            assertEquals(CONTA_CORRENTE, tipoConta)
            assertEquals(conta.instituicao.ispb, instituicao.ispb)
            assertEquals("0001", agencia)
            assertEquals("350176", numero)
            assertEquals(conta.titular.id, titular.id)
        }
    }

    @Test
    fun `deve retornar ABORTED quando ERP Itau indisponivel`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("c56dfef4-7901-44fb-84e2-a2cefb157890", CONTA_CORRENTE.name))
            .thenThrow(HttpClientException("Erro de conexão com ERP ITAU"))

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("24311919077")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()) }

        // VALIDAÇÃO
        assertEquals(Status.ABORTED.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (CPF - NOTBLANK)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()) }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (CPF - REGEX)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("319") // REGEX: ^[0-9]{11}\$
                    .setTipoConta(TipoConta.CONTA_POUPANCA)
                    .build() )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (CPF - HibernateValidator)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("bc35591d-b547-4151-a325-4a9d2cd19614")
                    .setTipoChave(TipoChave.CPF)
                    .setChave("31982600000")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build())
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }


    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (TELEFONE - NOTBLANK)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.TELEFONE)
                    .setChave("")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build() )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (TELEFONE - REGEX)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.TELEFONE)
                    .setChave("-55319123") // REGEX: ^\+[1-9][0-9]\d{1,14}$
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build() )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (EMAIL NOTBLANK)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build() )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar INVALID_ARGUMENT quando ChavePix invalida (EMAIL HibernateValidator)`() {
        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave("emailinvalido.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build() )
        }

        // VALIDAÇÃO
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }

    @Test
    fun `deve retornar NOT_FOUND quando Conta x Cliente nao encontrados no ERP ITAU`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("c56dfef4-7901-44fb-84e2-a2cefb157890", CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.notFound())

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(chavePixRequest)
        }

        // VALIDAÇÃO
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Conta x Cliente não encontrados no ERP Itaú.", status.description)
        }
    }

    @Test
    fun `deve retornar UNKNOWN quando lancada excecao nao prevista`() {
        // CENÁRIO
        `when`(itauErpClient.consultaContaErp("", CONTA_CORRENTE.name))
            .thenReturn(HttpResponse.notFound())

        // AÇÃO
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraChavePix(chavePixRequest)
        }

        // VALIDAÇÃO
        with(error) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("Erro inesperado", status.description)
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
        return mock(ItauErpClient::class.java)
    }

    // Conexão com o BCB
    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return mock(BcbClient::class.java)
    }

}