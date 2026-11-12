package dev.bootcamp.creditservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Crédito personal o empresarial tal como se guarda en la colección {@code credits}.
 */
@Getter
@AllArgsConstructor
@Document(collection = "credits")
public class CreditDocument {
    @Id private final String id;
    @Indexed private final String customerId;

    /**
     * Tipo del cliente; se guarda para reconstruir el crédito sin consultar customer-service.
     */
    private final String customerType;

    /**
     * Perfil del cliente (STANDARD, VIP o PYME).
     */
    private final String customerProfile;

    /**
     * PERSONAL o BUSINESS.
     */
    private final String type;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal amount;

    private final LocalDate dueDate;

    private final LocalDate grantedAt;
}
