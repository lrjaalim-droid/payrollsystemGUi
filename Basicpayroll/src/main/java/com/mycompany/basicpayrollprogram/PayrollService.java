package com.mycompany.basicpayrollprogram;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PayrollService {
    private static final String ATTENDANCE_FILE = "resources/MotorPH_Employee Data - Attendance Record.csv";

    public static Map<String, Double> getAttendanceForEmployee(String employeeNumber) {
        Map<String, Double> attendanceMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            String line = br.readLine(); // skip header
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 6) continue;
                String empNum = fields[0].replace("\"", "").trim();
                String dateStr = fields[3].replace("\"", "").trim();
                String loginStr = fields[4].replace("\"", "").trim();
                String logoutStr = fields[5].replace("\"", "").trim();

                if (!empNum.equals(employeeNumber)) continue;

                LocalDate date = LocalDate.parse(dateStr, dateFormatter);
                if (date.getYear() != 2024 || date.getMonthValue() < 6 || date.getMonthValue() > 12) continue;

                LocalTime loginTime = LocalTime.parse(loginStr, timeFormatter);
                LocalTime logoutTime = LocalTime.parse(logoutStr, timeFormatter);
                double hoursWorked = computeHoursWorked(loginTime, logoutTime);
                String cutoffKey = buildCutoffKey(date);

                attendanceMap.put(cutoffKey, attendanceMap.getOrDefault(cutoffKey, 0.0) + hoursWorked);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attendanceMap;
    }

    public static String generatePayrollSummary(String employeeNumber, String[] employeeData, Map<String, Double> attendance) {
        if (employeeData == null) return "Employee data not found.";
        StringBuilder sb = new StringBuilder();
        String fullName = employeeData[2] + " " + employeeData[1];
        double hourlyRate = parseDouble(employeeData[18]);
        sb.append("Payroll Summary for ").append(fullName).append("\n");
        sb.append("Employee Number: ").append(employeeNumber).append("\n");
        for (int month = 6; month <= 12; month++) {
            String monthName = getMonthName(month);
            String firstCutoff = "2024-" + padMonth(month) + "-1";
            String secondCutoff = "2024-" + padMonth(month) + "-2";

            double hoursFirst = attendance.getOrDefault(firstCutoff, 0.0);
            double hoursSecond = attendance.getOrDefault(secondCutoff, 0.0);
            double grossFirst = hoursFirst * hourlyRate;
            double grossSecond = hoursSecond * hourlyRate;

            // Deductions
            double totalGross = grossFirst + grossSecond;
            double sss = computeSss(totalGross);
            double philHealth = computePhilHealth(totalGross);
            double pagIbig = computePagIbig(totalGross);
            double taxableIncome = totalGross - sss - philHealth - pagIbig;
            double withholdingTax = computeTax(taxableIncome);
            double totalDeductions = sss + philHealth + pagIbig + withholdingTax;
            double netSecond = grossSecond - totalDeductions;

            sb.append("\n").append(monthName).append(":\n");
            sb.append("  Hours (1-15): ").append(hoursFirst).append("\n");
            sb.append("  Gross (1-15): ").append(grossFirst).append("\n");
            sb.append("  Hours (16-end): ").append(hoursSecond).append("\n");
            sb.append("  Gross (16-end): ").append(grossSecond).append("\n");
            sb.append("  Deductions:\n");
            sb.append("    SSS: ").append(sss).append("\n");
            sb.append("    PhilHealth: ").append(philHealth).append("\n");
            sb.append("    Pag-IBIG: ").append(pagIbig).append("\n");
            sb.append("    Withholding Tax: ").append(withholdingTax).append("\n");
            sb.append("  Net Salary: ").append(netSecond).append("\n");
        }
        return sb.toString();
    }

    // Helper methods
    static double computeHoursWorked(LocalTime login, LocalTime logout) {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);
        LocalTime grace = LocalTime.of(8, 10);
        LocalTime adjustedLogin = login.isBefore(grace) ? start : login;
        LocalTime adjustedLogout = logout.isAfter(end) ? end : logout;
        if (adjustedLogout.isBefore(adjustedLogin)) return 0.0;
        double hours = Duration.between(adjustedLogin, adjustedLogout).toMinutes() / 60.0;
        hours -= 1.0; // lunch
        if (hours < 0) return 0.0;
        return Math.min(hours, 8);
    }

    static String buildCutoffKey(LocalDate date) {
        String yearMonth = date.getYear() + "-" + padMonth(date.getMonthValue());
        return yearMonth + (date.getDayOfMonth() <= 15 ? "-1" : "-2");
    }

    static String getMonthName(int month) {
        String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
        return months[month - 1];
    }

    static String padMonth(int month) {
        return month < 10 ? "0" + month : String.valueOf(month);
    }

    static double parseDouble(String val) {
        try {
            return Double.parseDouble(val.trim().replace(",", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    static double computeSss(double gross) {
        // Implement your SSS table logic here (for brevity, simplified)
        if (gross < 3250) return 135;
        if (gross < 3750) return 157.5;
        if (gross < 4250) return 180;
        if (gross < 4750) return 202.5;
        if (gross < 5250) return 225;
        // ... add other brackets as needed
        return 1125; // maximum
    }

    static double computePhilHealth(double gross) {
        double base = Math.max(10000, Math.min(gross, 60000));
        return (base * 0.03) / 2;
    }

    static double computePagIbig(double gross) {
        double contribution = gross > 1500 ? gross * 0.02 : gross * 0.01;
        return Math.min(contribution, 100);
    }

    static double computeTax(double taxable) {
        if (taxable <= 20833) return 0;
        if (taxable < 33333) return (taxable - 20833) * 0.20;
        if (taxable < 66667) return 2500 + (taxable - 33333) * 0.25;
        if (taxable < 166667) return 10833 + (taxable - 66667) * 0.30;
        if (taxable < 666667) return 40833.33 + (taxable - 166667) * 0.32;
        return 200833.33 + (taxable - 666667) * 0.35;
    }
}