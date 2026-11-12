package dev.bootcamp.creditservice.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Estado de deuda de un cliente tal como se guarda en la colección {@code customer_debt_status};
 * su id es el del cliente.
 */
@Getter
@AllArgsConstructor
@Document(collection = "customer_debt_status")
public class CustomerDebtStatusDocument {
    @Id private final String customerId;
    private final boolean hasOverdueDebt;

    @Field(targetType = FieldType.DECIMAL128)
    private final BigDecimal overdueAmount;

    private final Instant occurredAt;
}
