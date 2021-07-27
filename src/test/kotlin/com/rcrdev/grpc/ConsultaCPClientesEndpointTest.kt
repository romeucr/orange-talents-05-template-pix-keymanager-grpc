package com.rcrdev.grpc

import com.rcrdev.ConsultaCPClienteRequest
import com.rcrdev.ConsultaCPClientesServiceGrpc
import com.rcrdev.ConsultaCPClientesServiceGrpc.ConsultaCPClientesServiceBlockingStub
import com.rcrdev.bcb.BcbClient
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
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime

@MicronautTest(transactional = false)
internal class ConsultaCPClientesEndpointTest(
    private val chavePixRepository: ChavePixRepository,
    private val grpcClient: ConsultaCPClientesServiceBlockingStub,
    private val clienteRepository: ClienteRepository,
    private val instituicaoRepository: InstituicaoRepository,
    private val contaRepository: ContaRepository,
    private val bcbClient: BcbClient
) {

    private lateinit var consulta: ConsultaCPClienteRequest
    private lateinit var clientId: String
    private lateinit var pixId: String
    private lateinit var chavePix: ChavePix
    private lateinit var chaveCPF: String
    private lateinit var instituicao: Instituicao
    private lateinit var cliente: Cliente
    private lateinit var conta: Conta

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()

        clientId = "5260263c-a3c1-4727-ae32-3bdb2538841b"
        pixId = "0b8ffed1-26f7-45e7-866a-3d24eae18096"
        chaveCPF = "31982606800"
        consulta = ConsultaCPClienteRequest.newBuilder()
            .setIdCliente(clientId)
            .setPixId(pixId)
            .build()

        instituicao= Instituicao(12345, "NULLBANK")
        cliente = Cliente(clientId, "Patolino De Souza", chaveCPF)
        conta = Conta(TipoConta.CONTA_CORRENTE, instituicao, "0001", "350176", cliente)
        instituicaoRepository.save(instituicao)
        clienteRepository.save(cliente)
        contaRepository.save(conta)

        chavePix = ChavePix(clientId, TipoChave.CPF, chaveCPF, TipoConta.CONTA_CORRENTE)
        chavePix.pixId = pixId
        chavePix.criadoEm = LocalDateTime.now()
        chavePixRepository.save(chavePix)
    }

    @Test
    fun `deve retornar os dados da ChavePix vindas do BCB`() {
        // AÇÃO
        val response = grpcClient.consultaChavePixClientes(consulta)

        // VALIDAÇÃO
        with(response) {
            assertNotNull(this.chave)
            assertNotNull(this.conta)
            assertNotNull(this.criadoEm)
            assertNotNull(this.idCliente)
            assertNotNull(this.pixId)
            assertNotNull(this.tipoChave)
            assertNotNull(this.titular)

        }
    }

    /*** MOCKS ***/
    // client gRpc
    @Factory
    class Clients {
        @Bean
        fun blockingStubConsultaClientes(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): ConsultaCPClientesServiceBlockingStub {
            return ConsultaCPClientesServiceGrpc.newBlockingStub(channel)
        }
    }
    
}