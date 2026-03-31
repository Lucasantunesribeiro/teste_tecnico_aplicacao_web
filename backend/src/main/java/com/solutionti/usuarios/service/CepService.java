package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.response.CepResponse;
import com.solutionti.usuarios.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CepService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    private final RestTemplate restTemplate;

    @Cacheable(value = "ceps", key = "#cep")
    public CepResponse consultarCep(String cep) {
        String cepDigits = cep.replaceAll("\\D", "");
        log.info("Consultando CEP: {}", cepDigits);

        if (cepDigits.length() != 8) {
            throw new BusinessException("CEP inválido: " + cep);
        }

        try {
            CepResponse response = restTemplate.getForObject(VIA_CEP_URL, CepResponse.class, cepDigits);

            if (response == null) {
                throw new BusinessException("Nenhuma resposta do serviço de CEP para: " + cepDigits);
            }

            if (Boolean.TRUE.equals(response.erro())) {
                throw new BusinessException("CEP não encontrado: " + cepDigits);
            }

            log.info("CEP {} consultado com sucesso: {}", cepDigits, response.localidade());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao consultar CEP {}: {}", cepDigits, e.getMessage());
            throw new BusinessException("Erro ao consultar CEP: " + cepDigits);
        }
    }
}
