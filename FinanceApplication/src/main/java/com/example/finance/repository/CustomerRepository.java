package com.example.finance.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.finance.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	/*
	 * --------------------------------------------- 
	 * 1. Pagination + Sorting
	 * ---------------------------------------------
	 *
	 * JpaRepository already provides:
	 *
	 * findAll(Pageable pageable)
	 *
	 * Example:
	 *
	 * Page<Customer> page = customerRepository.findAll( PageRequest.of( 0, 10,
	 * Sort.by("firstName").ascending() ) );
	 */

	/*
	 * ---------------------------------------------
	 *  2. Derived Query
	 * ---------------------------------------------
	 */
	Optional<Customer> findByEmail(String email);

	/*
	 * --------------------------------------------- 
	 * 3. JPQL Query
	 * ---------------------------------------------
	 *
	 * Search customers by first name.
	 */
	@Query("""
			SELECT c
			FROM Customer c
			WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
			""")
	Page<Customer> searchByFirstName(@Param("name") String name, Pageable pageable);

	/*
	 * --------------------------------------------- 
	 * 4. JPQL Query
	 * ---------------------------------------------
	 *
	 * Search by first name OR last name.
	 */
	@Query("""
			SELECT c
			FROM Customer c
			WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			   OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
			""")
	Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

	/*
	 * ---------------------------------------------
	 *  5. Native SQL Query
	 * ---------------------------------------------
	 *
	 * Native SQL works directly with the database table and column names.
	 */
	@Query(value = """
			SELECT *
			FROM customers
			WHERE email = :email
			""", nativeQuery = true)
	Optional<Customer> findByEmailNative(@Param("email") String email);

	/*
	 * --------------------------------------------- 
	 * 6. Native SQL + Pagination
	 * ---------------------------------------------
	 */
	@Query(value = """
			SELECT *
			FROM customers
			WHERE LOWER(first_name) LIKE
			      LOWER(CONCAT('%', :keyword, '%'))
			""",
			countQuery = """
			SELECT COUNT(*)
			FROM customers
			WHERE LOWER(first_name) LIKE
			      LOWER(CONCAT('%', :keyword, '%'))
			""", nativeQuery = true)
	Page<Customer> searchByFirstNameNative(@Param("keyword") String keyword, Pageable pageable);
	
	/*
	 * ---------------------------------------------
	 * Named Query
	 * ---------------------------------------------
	 */

	List<Customer> findActiveCustomersNamed();
}
