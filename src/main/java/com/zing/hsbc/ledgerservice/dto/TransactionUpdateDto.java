package com.zing.hsbc.ledgerservice.dto;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
@Data
public class TransactionUpdateDto {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Long sourceWalletId;
    @NotNull
    private Long targetWalletId;
}
