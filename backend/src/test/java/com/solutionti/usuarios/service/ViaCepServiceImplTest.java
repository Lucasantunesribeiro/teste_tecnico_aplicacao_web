package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.response.CepResponse;
import com.solutionti.usuarios.exception.BusinessException;
import com.solutionti.usuarios.service.impl.ViaCepServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaCepServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private ViaCepServiceImpl viaCepService;

    private static final String CEP_DIGITS = "01001000";
    private static final String CEP_FORMATTED = "01001-000";

    @BeforeEach
    void setUp() {
        viaCepService = new ViaCepServiceImpl(restTemplate);
    }

    @Test
    void deveRetornarCepResponseParaCepValido() {
        // Given
        CepResponse cepResponse = new CepResponse(
            CEP_DIGITS, "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP", "3550308", null
        );
        when(restTemplate.getForObject(anyString(), eq(CepResponse.class), eq(CEP_DIGITS)))
            .thenReturn(cepResponse);

        // When
        CepResponse result = viaCepService.consultarCep(CEP_DIGITS);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.localidade()).isEqualTo("São Paulo");
        assertThat(result.uf()).isEqualTo("SP");
    }

    @Test
    void deveNormalizarCepComMascara() {
        // Given — CEP passed with hyphen mask should be stripped before lookup
        CepResponse cepResponse = new CepResponse(
            CEP_DIGITS, "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP", "3550308", null
        );
        when(restTemplate.getForObject(anyString(), eq(CepResponse.class), eq(CEP_DIGITS)))
            .thenReturn(cepResponse);

        // When — pass formatted CEP
        CepResponse result = viaCepService.consultarCep(CEP_FORMATTED);

        // Then — digits-only key used for the HTTP call
        assertThat(result.localidade()).isEqualTo("São Paulo");
        verify(restTemplate).getForObject(anyString(), eq(CepResponse.class), eq(CEP_DIGITS));
    }

    @Test
    void deveLancarExcecaoQuandoCepRetornaErroTrue() {
        // Given — ViaCEP returns {"erro":true} for unknown CEPs
        CepResponse erroResponse = new CepResponse(
            null, null, null, null, null, null, null, Boolean.TRUE
        );
        when(restTemplate.getForObject(anyString(), eq(CepResponse.class), eq(CEP_DIGITS)))
            .thenReturn(erroResponse);

        // When/Then
        assertThatThrownBy(() -> viaCepService.consultarCep(CEP_DIGITS))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CEP não encontrado");
    }

    @Test
    void deveLancarExcecaoQuandoViaCepRetornaNull() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(CepResponse.class), eq(CEP_DIGITS)))
            .thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> viaCepService.consultarCep(CEP_DIGITS))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Nenhuma resposta");
    }

    @Test
    void deveLancarExcecaoQuandoViaCepEstaIndisponivel() {
        // Given — network timeout / connection refused
        when(restTemplate.getForObject(anyString(), eq(CepResponse.class), eq(CEP_DIGITS)))
            .thenThrow(new ResourceAccessException("Connection refused"));

        // When/Then
        assertThatThrownBy(() -> viaCepService.consultarCep(CEP_DIGITS))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Erro ao consultar o serviço de CEP");
    }

    @Test
    void deveLancarExcecaoParaCepComMenosDe8Digitos() {
        assertThatThrownBy(() -> viaCepService.consultarCep("1234567"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CEP inválido");
    }
}
