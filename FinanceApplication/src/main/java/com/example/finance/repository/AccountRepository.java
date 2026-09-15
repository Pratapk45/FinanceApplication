package com.example.finance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.finance.entity.Account;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /*
     * Used when generating/checking an account number.
     */
    boolean existsByAccountNumber(String accountNumber);

    /*
     * Used for operations such as:
     *
     * GET /api/accounts/account-number/{accountNumber}
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /*
     * JPQL query:
     *
     * Account.customer.id
     * means we are navigating the entity relationship,
     * not directly referring to the database column.
     *
     * Pageable gives us:
     * - Pagination
     * - Sorting
     */
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.customer.id = :customerId
            """)
    Page<Account> findByCustomerId(
            @Param("customerId") Long customerId,
            Pageable pageable
    );

    /*
     * JPQL query to find active accounts of a customer.
     *
     * We will use this only if the service actually
     * needs this business operation.
     */
    
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.customer.id = :customerId
              AND a.status = 'ACTIVE'
            """)
    Page<Account> findActiveAccountsByCustomerId(
            @Param("customerId") Long customerId,
            Pageable pageable
    );
    
    /*  ============================================================ 
     * TRANSACTION-SAFE LOOKUP
     * ============================================================ 
     *
     *  Locks the account row until the current 
     *  database transaction completes.
      * */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    		SELECT a FROM Account a WHERE a.id = :accountId """)
    Optional<Account> findByIdForUpdate( @Param("accountId") Long accountId );
    
}