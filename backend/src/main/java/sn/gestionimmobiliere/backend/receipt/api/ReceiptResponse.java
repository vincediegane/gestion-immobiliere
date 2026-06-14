package sn.gestionimmobiliere.backend.receipt.api;import java.time.Instant;import java.util.UUID;public record ReceiptResponse(UUID id,UUID rentChargeId,String receiptNumber,Instant issuedAt){}
