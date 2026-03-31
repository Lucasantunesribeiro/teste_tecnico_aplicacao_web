package com.solutionti.usuarios.validator;

import org.springframework.stereotype.Component;

@Component
public class CepValidator {

    public boolean isValid(String cep) {
        if (cep == null) return false;
        String digits = cep.replaceAll("\\D", "");
        return digits.length() == 8;
    }
}
