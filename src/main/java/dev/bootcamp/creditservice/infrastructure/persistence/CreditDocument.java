package dev.bootcamp.creditservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** Crédito personal o empresarial tal como se guarda en la colección {@code credits}. */
@Getter
@AllArgsConstructor
@Document(collection = "credits")
public class CreditDocument {
    @Id private final String id;
    @Indexed private final String customerId;

    /** Tipo del cliente; se guarda para reconstruir el crédito sin consultar customer-service. */
    private final String customerType;

    /** PERSONAL o BUSINESS. */
    private final String type;
}
