package com.mycompany.basicpayrollprogram;

public class Main {
    public static void main(String[] args) {
        // Tawagin muna ang method para ipakita ang CSV
        String csvPath = "C:\\Users\\Jazper\\Documents\\NetBeansProjects\\Basicpayroll\\resources\\MotorPH_Employee Data - Employee Details.csv"; // Palitan mo ito ng exact path ng CSV file mo

        CSVUtils.showCSVContent(csvPath);

        // Pagkatapos, magpakita ng iyong PayrollApp GUI
        new PayrollApp().setVisible(true);
    }
}