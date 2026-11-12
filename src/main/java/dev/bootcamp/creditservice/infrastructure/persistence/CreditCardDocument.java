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
 * Tarjeta de crédito tal como se guarda en la colección {@code credit_cards}.
 */
@Getter
@AllArgsConstructor
@Document(collection = "credit_cards")
public class CreditCardDocument {
    @Id private final String id;
    @Indexed private final String customerId;

    /**
     * Tipo del cliente; se guarda para reconstruir la tarjeta sin consultar customer-service.
     */
    private final String customerType;

    /**
     * Perfil del cliente (STANDARD, VIP o PYME).
     */
    private final String customerProfile;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal creditLimit;

    /**
     * Día del mes (1 a 28) hasta el que se pagan los consumos del mes anterior.
     */
    private final int paymentDueDay;

    private final LocalDate grantedAt;
}
