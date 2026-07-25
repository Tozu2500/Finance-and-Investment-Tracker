package com.financetracker.dto.calculator;

public record SalaryBreakdownDto(
    
    double annual,
    double monthly,
    double weekly,
    double daily,
    double perHourWorked,
    double perMinuteWorked,
    double perSecondWorked,
    double perHourYear,
    double perMinuteYear,
    double perSecondYear
) {}
