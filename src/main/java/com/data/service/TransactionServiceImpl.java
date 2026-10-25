package com.data.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.data.exception.BadRequestException;
import com.data.model.Account;
import com.data.model.Transaction;
import com.data.repository.TransactionRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class TransactionServiceImpl implements TransactionService {

	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private AccountService accountService;

	@Override
	public Map<String, Object> getMyTransactions(String email, String accountNumber, String type, String search,
			String from, String to, int page, int size) {
		String number = accountService.resolveAccount(email, accountNumber).getAccountNumber();
		Page<Transaction> result = transactionRepository.search(number, type.trim().toUpperCase(), search.trim(),
				startOf(from), endOf(to), PageRequest.of(page, size, Sort.by("createdAt").descending()));
		return toPageResponse(result);
	}

	@Override
	public Map<String, Object> getAllTransactions(String account, String type, String from, String to,
			int page, int size) {
		Page<Transaction> result = transactionRepository.adminSearch(account.trim().toUpperCase(),
				type.trim().toUpperCase(), startOf(from), endOf(to),
				PageRequest.of(page, size, Sort.by("createdAt").descending()));
		return toPageResponse(result);
	}

	@Override
	public String getStatementCsv(String email, String accountNumber, String month) {
		String number = accountService.resolveAccount(email, accountNumber).getAccountNumber();
		List<Transaction> list = statement(number, parseMonth(month));

		StringBuilder csv = new StringBuilder("Date,Reference,Type,Note,Debit,Credit,Balance\n");
		for (Transaction t : list) {
			boolean debit = number.equals(t.getFromAccount());
			BigDecimal balance = debit ? t.getFromBalanceAfter() : t.getToBalanceAfter();
			csv.append(t.getCreatedAt().format(DATE_TIME)).append(',')
					.append(t.getReferenceNo()).append(',')
					.append(t.getType()).append(',')
					.append(csvText(t.getCategory())).append(',')
					.append(debit ? t.getAmount() : "").append(',')
					.append(debit ? "" : t.getAmount()).append(',')
					.append(balance == null ? "" : balance).append('\n');
		}
		return csv.toString();
	}

	@Override
	public byte[] getStatementPdf(String email, String accountNumber, String month) {
		Account account = accountService.resolveAccount(email, accountNumber);
		String number = account.getAccountNumber();
		YearMonth ym = parseMonth(month);
		List<Transaction> list = statement(number, ym);

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
			PdfWriter.getInstance(doc, out);
			doc.open();
			doc.add(new Paragraph("SmartBank - Account Statement",
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
			doc.add(new Paragraph("Account: " + number + " (" + account.getAccountType() + ")    Holder: "
					+ account.getUser().getName() + "    Period: " + ym));
			doc.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(new float[] { 2.3f, 2f, 1.8f, 1.5f, 1.3f, 1.3f, 1.5f });
			table.setWidthPercentage(100);
			Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL, Color.WHITE);
			for (String h : new String[] { "Date", "Reference", "Type", "Note", "Debit", "Credit", "Balance" }) {
				PdfPCell c = new PdfPCell(new Phrase(h, headFont));
				c.setBackgroundColor(new Color(15, 42, 74));
				c.setPadding(6);
				table.addCell(c);
			}
			Font body = FontFactory.getFont(FontFactory.HELVETICA, 9);
			BigDecimal totalDebit = BigDecimal.ZERO;
			BigDecimal totalCredit = BigDecimal.ZERO;
			for (Transaction t : list) {
				boolean debit = number.equals(t.getFromAccount());
				BigDecimal balance = debit ? t.getFromBalanceAfter() : t.getToBalanceAfter();
				if (debit) {
					totalDebit = totalDebit.add(t.getAmount());
				} else {
					totalCredit = totalCredit.add(t.getAmount());
				}
				cell(table, t.getCreatedAt().format(DATE_TIME), body);
				cell(table, t.getReferenceNo(), body);
				cell(table, t.getType(), body);
				cell(table, t.getCategory() == null ? "-" : t.getCategory(), body);
				cell(table, debit ? t.getAmount().toString() : "", body);
				cell(table, debit ? "" : t.getAmount().toString(), body);
				cell(table, balance == null ? "-" : balance.toString(), body);
			}
			doc.add(table);
			doc.add(new Paragraph(" "));
			if (list.isEmpty()) {
				doc.add(new Paragraph("No transactions in this period."));
			}
			doc.add(new Paragraph("Total debit: Rs " + totalDebit + "     Total credit: Rs " + totalCredit
					+ "     Current balance: Rs " + account.getBalance(),
					FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
			doc.add(new Paragraph("Deposits and withdrawals in this application are simulated transactions.",
					FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)));
			doc.close();
			return out.toByteArray();
		} catch (Exception e) {
			throw new BadRequestException("Could not create the PDF statement");
		}
	}

	// ---------- helper methods ----------

	private List<Transaction> statement(String number, YearMonth ym) {
		return transactionRepository.findStatement(number, ym.atDay(1).atStartOfDay(),
				ym.plusMonths(1).atDay(1).atStartOfDay());
	}

	private Map<String, Object> toPageResponse(Page<Transaction> result) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("content", result.getContent());
		response.put("page", result.getNumber());
		response.put("size", result.getSize());
		response.put("totalPages", result.getTotalPages());
		response.put("totalElements", result.getTotalElements());
		return response;
	}

	private LocalDateTime startOf(String from) {
		return from.isBlank() ? LocalDateTime.of(2000, 1, 1, 0, 0) : parseDate(from).atStartOfDay();
	}

	private LocalDateTime endOf(String to) {
		return to.isBlank() ? LocalDateTime.now().plusDays(1) : parseDate(to).plusDays(1).atStartOfDay();
	}

	private YearMonth parseMonth(String month) {
		if (month == null || month.isBlank()) {
			return YearMonth.now();
		}
		try {
			return YearMonth.parse(month.trim());
		} catch (DateTimeParseException e) {
			throw new BadRequestException("Month must be like 2026-10");
		}
	}

	private LocalDate parseDate(String text) {
		try {
			return LocalDate.parse(text.trim());
		} catch (DateTimeParseException e) {
			throw new BadRequestException("Date must be like 2026-10-31");
		}
	}

	private String csvText(String text) {
		if (text == null) {
			return "";
		}
		String safe = text.replace("\"", "\"\"");
		if (safe.startsWith("=") || safe.startsWith("+") || safe.startsWith("-") || safe.startsWith("@")) {
			safe = "'" + safe;
		}
		return "\"" + safe + "\"";
	}

	private void cell(PdfPTable table, String text, Font font) {
		PdfPCell c = new PdfPCell(new Phrase(text, font));
		c.setPadding(5);
		table.addCell(c);
	}
}