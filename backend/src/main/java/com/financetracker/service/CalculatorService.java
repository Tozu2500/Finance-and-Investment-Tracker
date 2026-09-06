package com.financetracker.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.financetracker.dto.calculator.InvestmentRequest;
import com.financetracker.dto.calculator.InvestmentResultDto;
import com.financetracker.dto.calculator.SalaryBreakdownDto;
import com.financetracker.dto.calculator.SalaryRequest;

@Service
public class CalculatorService {

    public SalaryBreakdownDto salary(SalaryRequest req) {
        double annual = switch (req.timeframe().toLowerCase()) {
            case "hourly" -> req.amount() * req.hoursPerWeek() * 52;
            case "daily" -> req.amount() * 5 * 52;
            case "weekly" -> req.amount() * 52;
            case "monthly" -> req.amount() * 12;
            default -> req.amount();
        };

        double workedHoursPerYear = req.hoursPerWeek() * 52;
        double calendarHoursPerYear = 8760.0;

        return new SalaryBreakdownDto(
            annual,
            annual / 12,
            annual / 52,
            annual / 260,
            annual / workedHoursPerYear,
            annual / workedHoursPerYear / 60,
            annual / workedHoursPerYear / 3600,
            annual / calendarHoursPerYear,
            annual / calendarHoursPerYear / 60,
            annual / calendarHoursPerYear / 3600
        );
    }

    public InvestmentResultDto investment(InvestmentRequest req) {
        double effectiveRate = (req.annualReturn() - req.annualFees()) / 100.0;
        if (effectiveRate < -1.0) throw new BadRequestException("Annual fees cannot exceed annual return by more than 100%");
        double monthlyRate = Math.pow(1 + effectiveRate, 1.0 / 12) - 1;

        List<String> yearLabels = new ArrayList<>();
        List<Double> balanceByYear = new ArrayList<>();
        List<Double> contributionsByYear = new ArrayList<>();
        List<Double> realByYear = new ArrayList<>();

        List<InvestmentResultDto.YearRow> schedule = new ArrayList<>();

        double balance = req.principal();
        double totalContributed = req.principal();
        double monthlyContrib = req.monthlyContribution();

        for (int year = 1; year <= req.years(); year++) {
            double interest = balance * monthlyRate;

            balance += interest + monthlyContrib;
            yearContribs += monthlyContrib;
            yearInterest += interest;
            totalContributed += monthlyContrib;
        }

        
    }
}
