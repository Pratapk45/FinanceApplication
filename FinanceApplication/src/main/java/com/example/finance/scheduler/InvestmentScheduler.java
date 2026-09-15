package com.example.finance.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.entity.Investment;
import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.repository.InvestmentRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InvestmentScheduler {

	private InvestmentRepository investmentRepository;

	/*
	 * ============================================================ 
	 * DAILY INVESTMENT MAINTENANCE
	 *  ============================================================
	 *
	 * Runs every day at 1:00 AM.
	 *
	 * Cron:
	 *
	 * 0 0 1 * * *
	 *
	 * Meaning:
	 *
	 * second = 0 minute = 0 hour = 1 every day every month every week
	 */
	@Scheduled(cron = "${finance.scheduler.investment-maintenance-cron:0 0 1 * * *}")
	@Transactional
	public void processActiveInvestments() {

		long startTime = System.currentTimeMillis();

		log.info("Investment scheduler started");

		try {

			List<Investment> activeInvestments = investmentRepository.findByStatus(InvestmentStatus.ACTIVE);

			log.info("Found {} active investments for daily maintenance", activeInvestments.size());

			int processedCount = 0;

			for (Investment investment : activeInvestments) {

				processInvestment(investment);

				processedCount++;
			}

			log.info("Investment scheduler completed successfully. " + "Processed {} investments in {} ms",
					processedCount, System.currentTimeMillis() - startTime);

		} catch (Exception exception) {

			log.error("Investment scheduler failed after {} ms", System.currentTimeMillis() - startTime, exception);
		}
	}

	/*
	 * ============================================================ 
	 * PROCESS SINGLE INVESTMENT
	 *  ============================================================
	 */
	private void processInvestment(Investment investment) {

		log.debug("Processing investment. id={}, reference={}, type={}", investment.getId(),
				investment.getInvestmentReference(), investment.getInvestmentType());

		/*
		 * Current model does not contain an external market-price source or maturity
		 * information.
		 *
		 * Therefore, the scheduler performs maintenance/validation rather than
		 * inventing an investment return calculation.
		 */

		if (investment.getInvestedAmount() == null) {

			log.warn("Investment {} has null invested amount", investment.getId());

			return;
		}

		if (investment.getCurrentValue() == null) {

			log.warn("Investment {} has null current value", investment.getId());

			return;
		}

		log.debug("Investment maintenance completed. " + "id={}, currentValue={}", investment.getId(),
				investment.getCurrentValue());
	}
}