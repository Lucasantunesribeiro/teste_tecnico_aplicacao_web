package com.solutionti.usuarios.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    private CpfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "529.982.247-25",
        "52998224725",
        "111.444.777-35",
        "11144477735"
    })
    void deveRetornarTrueParaCpfsValidos(String cpf) {
        assertTrue(validator.isValid(cpf));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "",
        "000.000.000-00",
        "111.111.111-11",
        "222.222.222-22",
        "999.999.999-99",
        "123.456.789-00",
        "12345678900",
        "1234567",
        "abc.def.ghi-jk",
        "529.982.247-26"
    })
    void deveRetornarFalseParaCpfsInvalidos(String cpf) {
        assertFalse(validator.isValid(cpf));
    }
}
