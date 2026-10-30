package com.ledgersense.dto;

import com.ledgersense.service.impl.IBankTransactionService.RecategoriseResult;

public record RecategoriseResponse(BankTransactionResponse transaction, int merchantTransactionsUpdated) {

    public static RecategoriseResponse from(RecategoriseResult result) {
        return new RecategoriseResponse(
                BankTransactionResponse.from(result.transaction()), result.merchantTransactionsUpdated());
    }
}