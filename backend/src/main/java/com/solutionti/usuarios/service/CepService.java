package com.solutionti.usuarios.service;

import com.solutionti.usuarios.dto.response.CepResponse;

public interface CepService {

    CepResponse consultarCep(String cep);
}
