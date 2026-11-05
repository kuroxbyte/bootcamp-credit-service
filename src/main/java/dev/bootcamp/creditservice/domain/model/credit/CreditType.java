package dev.bootcamp.creditservice.domain.model.credit;

public enum CreditType {
    PERSONAL,     // Solo uno por persona
    BUSINESS,     // Más de uno por empresa
    CREDIT_CARD   // Personal o empresarial según el tipo de cliente
}
