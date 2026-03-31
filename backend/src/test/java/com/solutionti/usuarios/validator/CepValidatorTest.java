package com.solutionti.usuarios.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CepValidatorTest {

    private CepValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CepValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "01310-100",
        "01310100",
        "04538-133",
        "04538133",
        "20040-020",
        "20040020"
    })
    void deveRetornarTrueParaCepsValidos(String cep) {
        assertTrue(validator.isValid(cep));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",
        "1234567",
        "123456789",
        "abcdefgh",
        "0153-000",
        "0000000"
    })
    void deveRetornarFalseParaCepsInvalidos(String cep) {
        assertFalse(validator.isValid(cep));
    }
}
