package com.mycompany.basicpayrollprogram;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class SalaryView extends JPanel {
    private JTextField txtEmployeeNumber;
    private JTextArea txtSalaryInfo;

    public SalaryView() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter Employee Number:"));
        txtEmployeeNumber = new JTextField(10);
        topPanel.add(txtEmployeeNumber);
        JButton btnGenerate = new JButton("Generate");
        topPanel.add(btnGenerate);
        add(topPanel, BorderLayout.NORTH);

        txtSalaryInfo = new JTextArea();
        txtSalaryInfo.setEditable(false);
        add(new JScrollPane(txtSalaryInfo), BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> {
            String empNum = txtEmployeeNumber.getText().trim();
            Map<String, String[]> employees = EmployeeModel.getAllEmployees();
            Map<String, Double> attendance = PayrollService.getAttendanceForEmployee(empNum);
            String info = PayrollService.generatePayrollSummary(empNum, employees.get(empNum), attendance);
            txtSalaryInfo.setText(info);
        });
    }
}