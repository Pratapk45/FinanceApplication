package com.example.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.finance.entity.Transaction;
import com.example.finance.entity.enums.TransactionDirection;
import com.example.finance.entity.enums.TransactionType;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {


    /*
     * ============================================================
     * DERIVED QUERY
     * ============================================================
     */
    Page<Transaction> findByAccountId(
            Long accountId,
            Pageable pageable
    );


    /*
     * Find transaction by unique reference.
     */
    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );


    /*
     * ============================================================
     * JPQL QUERY
     * ============================================================
     */
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.account.id = :accountId
              AND t.direction = :direction
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findByAccountIdAndDirection(
            @Param("accountId") Long accountId,
            @Param("direction")
            TransactionDirection direction
    );


    /*
     * ============================================================
     * NAMED QUERY
     * ============================================================
     *
     * The actual query is defined on Transaction.java.
     */
    @Query(
            name = "Transaction.findByAccountIdAndTransactionType"
    )
    List<Transaction> findByAccountIdAndTransactionTypeNamed(
            @Param("accountId") Long accountId,
            @Param("transactionType")
            TransactionType transactionType
    );


    /*
     * ============================================================
     * NATIVE SQL QUERY
     * ============================================================
     */
    @Query(
            value = """
                    SELECT *
                    FROM transactions
                    WHERE account_id = :accountId
                    ORDER BY created_at DESC
                    """,
            nativeQuery = true
    )
    List<Transaction> findTransactionHistoryNative(
            @Param("accountId") Long accountId
    );
}


