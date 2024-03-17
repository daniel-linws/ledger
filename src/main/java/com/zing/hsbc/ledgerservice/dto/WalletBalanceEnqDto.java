package com.zing.hsbc.ledgerservice.dto;

import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Data
public class WalletBalanceEnqDto {
    @NotNull
    private Long walletId;
    @NotNull
    private LocalDateTime timestamp;
}
