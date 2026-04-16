package com.fiap.mecanica.shared.rule;


import com.fiap.mecanica.shared.rule.dto.InputBaseDto;
import com.fiap.mecanica.shared.rule.dto.OutputBaseDto;

public abstract class BaseRule {
    public abstract OutputBaseDto execute(InputBaseDto inputBaseDto);
}
