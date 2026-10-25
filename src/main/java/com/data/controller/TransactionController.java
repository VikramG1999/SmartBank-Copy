package com.data.controller;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.data.service.TransactionService;

@RestController
@RequestMapping("/api")
public class TransactionController {

	@Autowired
	private TransactionService service;

	@GetMapping("/transactions")
	public ResponseEntity<Map<String, Object>> transactions(
			@RequestParam(defaultValue = "") String type,
			@RequestParam(defaultValue = "") String search,
			@RequestParam(defaultValue = "") String from,
			@RequestParam(defaultValue = "") String to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestHeader(value = "X-Account-Number", required = false) String acc,
			Authentication auth) {
		return ResponseEntity.ok(service.getMyTransactions(auth.getName(), acc, type, search, from, to,
				Math.max(page, 0), Math.min(Math.max(size, 1), 50)));
	}

	@GetMapping("/statement")
	public ResponseEntity<byte[]> statementCsv(@RequestParam(defaultValue = "") String month,
			@RequestHeader(value = "X-Account-Number", required = false) String acc, Authentication auth) {
		String csv = service.getStatementCsv(auth.getName(), acc, month);
		String label = month.isBlank() ? YearMonth.now().toString() : month;
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + label + ".csv")
				.contentType(MediaType.parseMediaType("text/csv"))
				.body(csv.getBytes(StandardCharsets.UTF_8));
	}

	@GetMapping("/statement/pdf")
	public ResponseEntity<byte[]> statementPdf(@RequestParam(defaultValue = "") String month,
			@RequestHeader(value = "X-Account-Number", required = false) String acc, Authentication auth) {
		byte[] pdf = service.getStatementPdf(auth.getName(), acc, month);
		String label = month.isBlank() ? YearMonth.now().toString() : month;
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement-" + label + ".pdf")
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdf);
	}
}