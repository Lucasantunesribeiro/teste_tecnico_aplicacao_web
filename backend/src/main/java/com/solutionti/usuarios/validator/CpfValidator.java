package com.solutionti.usuarios.validator;

import org.springframework.stereotype.Component;

@Component
public class CpfValidator {

    public boolean isValid(String cpf) {
        if (cpf == null) return false;
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) return false;
        if (digits.chars().distinct().count() == 1) return false;

        int sum = 0;
        for (int i = 0; i < 9; i++) sum += (digits.charAt(i) - '0') * (10 - i);
        int first = 11 - (sum % 11);
        if (first >= 10) first = 0;
        if (first != (digits.charAt(9) - '0')) return false;

        sum = 0;
        for (int i = 0; i < 10; i++) sum += (digits.charAt(i) - '0') * (11 - i);
        int second = 11 - (sum % 11);
        if (second >= 10) second = 0;
        return second == (digits.charAt(10) - '0');
    }
}
