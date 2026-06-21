package com.mycompany.basicpayrollprogram;

import javax.swing.*;
import java.awt.*;

public class EmployeeView extends JPanel {
    private JTextField txtEmployeeNumber;
    private JTextArea txtEmployeeInfo;

    public EmployeeView() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Enter Employee Number:"));
        txtEmployeeNumber = new JTextField(10);
        topPanel.add(txtEmployeeNumber);
        JButton btnView = new JButton("View");
        topPanel.add(btnView);
        add(topPanel, BorderLayout.NORTH);

        txtEmployeeInfo = new JTextArea();
        txtEmployeeInfo.setEditable(false);
        add(new JScrollPane(txtEmployeeInfo), BorderLayout.CENTER);

        btnView.addActionListener(e -> {
            String empNum = txtEmployeeNumber.getText().trim();
            String info = EmployeeModel.getEmployeeInfo(empNum);
            txtEmployeeInfo.setText(info);
        });
    }
}