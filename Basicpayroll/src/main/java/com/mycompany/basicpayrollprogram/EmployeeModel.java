package com.mycompany.basicpayrollprogram;

import java.io.*;
import java.util.*;

public class EmployeeModel {
    private static final String EMPLOYEE_FILE = "resources/MotorPH_Employee Data - Employee Details.csv";

    public static Map<String, String[]> getAllEmployees() {
        Map<String, String[]> employees = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] fields = splitCsvLine(line);
                if (fields.length >= 19) {
                    employees.put(fields[0], fields);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public static String getEmployeeInfo(String employeeNumber) {
        Map<String, String[]> employees = getAllEmployees();
        if (employees.containsKey(employeeNumber)) {
            String[] data = employees.get(employeeNumber);
            String fullName = data[2] + " " + data[1];
            String birthday = data[3];
            String tin = data[4];
            String sss = data [5];
            String pagibig = data[6];
            String philhealth = data[7];
            return "Employee Number: " + employeeNumber + "\n" +
                   "Name: " + fullName + "\n" +
                   "Birthday: " + birthday +
                   "Tin #" + tin + "\n" +
                   "SSS #: " + sss + "\n" +
                   "Pag-Ibig #: " + pagibig + "\n" +
                   "PhilHealth #: " + philhealth;
        } else {
            return "Employee not found.";
        }
    }

    private static String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }
}