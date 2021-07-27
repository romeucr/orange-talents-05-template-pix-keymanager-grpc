package com.rcrdev.grpc

import com.rcrdev.ConsultaCPSistemaRequest
import com.rcrdev.ConsultaCPSistemasServiceGrpc
import com.rcrdev.ConsultaCPSistemasServiceGrpc.ConsultaCPSistemasServiceBlockingStub
import com.rcrdev.bcb.*
import com.rcrdev.bcb.enums.AccountType
import com.rcrdev.bcb.enums.KeyType
import com.rcrdev.bcb.enums.OwnerType
import com.rcrdev.chavepix.ChavePix
import com.rcrdev.chavepix.ChavePixRepository
import com.rcrdev.chavepix.tipos.TipoChave
import com.rcrdev.chavepix.tipos.TipoConta
import com.rcrdev.cliente.Cliente
import com.rcrdev.cliente.ClienteRepository
import com.rcrdev.conta.Conta
import com.rcrdev.conta.ContaRepository
import com.rcrdev.instituicao.Instituicao
import com.rcrdev.instituicao.InstituicaoRepository
import com.rcrdev.instituicao.exceptions.InstituicaoNotFoundException
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class ConsultaCPSistemasEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: ConsultaCPSistemasServiceBlockingStub,
    private val clienteRepository: ClienteRepository,
    private val instituicaoRepository: InstituicaoRepository,
    private val contaRepository: ContaRepository,
    private val bcbClient: BcbClient
) {
    private lateinit var consulta: ConsultaCPSistemaRequest
    private lateinit var clientId: String
    private lateinit var pixId: String
    private lateinit var chavePix: ChavePix
    private lateinit var chaveCPF: String
    private lateinit var instituicao: Instituicao
    private lateinit var cliente: Cliente
    private lateinit var conta: Conta
    private lateinit var pixKeyDetailsResponse: PixKeyDetailsResponse
    private lateinit var owner: Owner
    private lateinit var bankAccount: BankAccount

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll()
        clienteRepository.deleteAll()
        instituicaoRepository.deleteAll()

        clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b"
        pixId = "0b8ffed1-26f7-45e7-866a-3d24eae18096"
        chaveCPF = "31982606800"
        consulta = ConsultaCPSistemaRequest.newBuilder()
            .setChave(chaveCPF)
            .build()

        instituicao = Instituicao(60701190, "ITAU UNIBANCO")
        cliente = Cliente(clientId, "Patolino De Souza", chaveCPF)
        conta = Conta(TipoConta.CONTA_CORRENTE, instituicao, "0001", "350176", cliente)
        instituicaoRepository.save(instituicao)
        clienteRepository.save(cliente)
        contaRepository.save(conta)

        chavePix = ChavePix(clientId, TipoChave.CPF, chaveCPF, TipoConta.CONTA_CORRENTE)
        chavePix.pixId = pixId
        chavePix.criadoEm = LocalDateTime.now()
        chavePixRepository.save(chavePix)

        owner = Owner(type = OwnerType.NATURAL_PERSON, name = "Pernalonga do Amaral", taxIdNumber = "95094701045")
        bankAccount = BankAccount(branch = "1111", accountNumber = "1234567", accountType = AccountType.CACC)
        pixKeyDetailsResponse = PixKeyDetailsResponse(KeyType.CPF, chaveCPF, bankAccount, owner, LocalDateTime.now())
    }

    @Test
    fun `deve retornar os dados da ChavePix, consultando BCB, quando nao cadastrada no nosso sistema`() {
        // CENÁRIO
        val consultaKeyNoBcb = ConsultaCPSistemaRequest.newBuilder()
            .setChave("95094701045")
            .build()

        Mockito.`when`(bcbClient.getChavePix("95094701045")).thenReturn(HttpResponse.ok(pixKeyDetailsResponse))

        // AÇÃO
        val response = grpcClient.consultaChavePixSistemas(consultaKeyNoBcb)

        // VALIDAÇÃO
        with(response) {
            assertNotNull(chave)
            assertNotNull(conta)
            assertNotNull(criadoEm)
            assertNotNull(tipoChave)
            assertNotNull(titular)
            assertEquals("31982606800", chave)
        }
    }

    @Test
    fun `deve retornar os dados da ChavePix quando cadastrada no nosso sistema`() {
        // AÇÃO
        val response = grpcClient.consultaChavePixSistemas(consulta)

        // VALIDAÇÃO
        with(response) {
            assertNotNull(chave)
            assertNotNull(conta)
            assertNotNull(criadoEm)
            assertNotNull(tipoChave)
            assertNotNull(titular)
            assertEquals("31982606800", chave)
        }
    }

    /*** MOCKS ***/
    // client gRpc
    @Factory
    class Clients {
        @Bean
        fun blockingStubConsultaSistemas(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): ConsultaCPSistemasServiceBlockingStub {
            return ConsultaCPSistemasServiceGrpc.newBlockingStub(channel)
        }
    }

    // Conexão com o BCB
    @MockBean(BcbClient::class)
    fun bcbMock(): BcbClient {
        return mock(BcbClient::class.java)
    }
}