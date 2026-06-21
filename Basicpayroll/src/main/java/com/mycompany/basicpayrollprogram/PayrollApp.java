package com.mycompany.basicpayrollprogram;

import javax.swing.*;
import java.awt.*;

public class PayrollApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public PayrollApp() {
        setTitle("MotorPH Payroll System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Navigation panel
        JPanel navPanel = new JPanel();
        JButton btnEmployee = new JButton("Employee");
        JButton btnSalary = new JButton("Salary");
        JButton btnExit = new JButton("Exit");
        navPanel.add(btnEmployee);
        navPanel.add(btnSalary);
        navPanel.add(btnExit);

        // Main panel with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(new EmployeeView(), "Employee");
        mainPanel.add(new SalaryView(), "Salary");

        // Button actions
        btnEmployee.addActionListener(e -> cardLayout.show(mainPanel, "Employee"));
        btnSalary.addActionListener(e -> cardLayout.show(mainPanel, "Salary"));
        btnExit.addActionListener(e -> System.exit(0));

        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
}