package com.data.service;

import java.util.Map;

public interface TransactionService {

	Map<String, Object> getMyTransactions(String email, String accountNumber, String type, String search,
			String from, String to, int page, int size);

	Map<String, Object> getAllTransactions(String account, String type, String from, String to, int page, int size);

	String getStatementCsv(String email, String accountNumber, String month);

	byte[] getStatementPdf(String email, String accountNumber, String month);
}