
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollSystemGUI {

        // Paths to employee and attendance CSV files
    private String employeeCsvPath = "resources/MotorPH_Employee Data - Employee Details.csv";
    private String attendanceCsvPath = "resources/MotorPH_Employee Data - Attendance Record.csv"; // Palitan ng tamang path
    private String salaryApprovalDir = "resources/salaries"; // Folder para sa salary details

    private String role = "";
    private String loginEmpNumber = "";

    private List<Employee> employees = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EmployeePayrollSystemGUI().showLoginScreen();
        });
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(350, 250);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel roleLabel = new JLabel("Select Role:");
        String[] roles = {"HR" };
        JComboBox<String> roleCombo = new JComboBox<>(roles);

        JPanel userPanel = new JPanel(new FlowLayout());
        JLabel userLabel = new JLabel("Username :");
        JTextField userField = new JTextField(15);
        userPanel.add(userLabel);
        userPanel.add(userField);

        JPanel passPanel = new JPanel(new FlowLayout());
        JLabel passLabel = new JLabel("Password :");
        JPasswordField passField = new JPasswordField(15);
        passPanel.add(passLabel);
        passPanel.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> {
            role = (String) roleCombo.getSelectedItem();
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (role.equals("Employee")) {
                handleEmployeeLogin(username, password);
            } else {
                handleHRLogin(username, password);
            }
        });

        loginFrame.add(roleLabel);
        loginFrame.add(roleCombo);
        loginFrame.add(userPanel);
        loginFrame.add(passPanel);
        loginFrame.add(loginBtn);

        loginFrame.setVisible(true);
    }

    private void handleEmployeeLogin(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(employeeCsvPath))) {
            String line;
            boolean found = false;
            String[] empData = null;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4) {
                    String lastName = values[1].trim();
                    String empNumber = values[0].trim();
                    if (lastName.equalsIgnoreCase(username) && empNumber.equals(password)) {
                        empData = values;
                        found = true;
                        loginEmpNumber = empNumber;
                        break;
                    }
                }
            }
            if (found && empData != null) {
                showEmployeeView(empData);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Employee credentials");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading employee data");
        }
    }

    private void handleHRLogin(String username, String password) {
        if (username.equalsIgnoreCase("hr") && password.equals("hr123")) {
            showHRView();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid HR credentials");
        }
    }

    private void showEmployeeView(String[] employeeData) {
        JFrame empFrame = new JFrame("Employee Dashboard");
        empFrame.setSize(1000, 700);
        empFrame.setLocationRelativeTo(null);
        empFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        empFrame.setLayout(new BorderLayout());

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            empFrame.dispose();
            showLoginScreen();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnLogout);
        empFrame.add(topPanel, BorderLayout.NORTH);

        String empNumber = employeeData[0];
        String lastName = employeeData[1];
        String firstName = employeeData[2];

        // Buttons for Employee Details, Attendance, Salary
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton btnDetails = new JButton("Employee Details");
        JButton btnAttendance = new JButton("Attendance");
        JButton btnSalary = new JButton("Salary");

        btnPanel.add(btnDetails);
        btnPanel.add(btnAttendance);
        btnPanel.add(btnSalary);

        JPanel displayPanel = new JPanel(new BorderLayout());

        // Load salary details if approved
        String salaryDetails = getApprovedSalaryDetails(empNumber);
        boolean salaryApproved = salaryDetails != null;

        // Action for Employee Details
        btnDetails.addActionListener(e -> {
            displayPanel.removeAll();
            displayPanel.add(getEmployeeDetailsPanel(employeeData), BorderLayout.CENTER);
            displayPanel.revalidate();
            displayPanel.repaint();
        });

        // Action for Attendance
        btnAttendance.addActionListener(e -> {
            displayPanel.removeAll();
            List<String[]> attendanceRecords = getAttendanceRecords(empNumber);
            displayPanel.add(getAttendancePanel(attendanceRecords, firstName, lastName), BorderLayout.CENTER);
            displayPanel.revalidate();
            displayPanel.repaint();
        });

        // Action for Salary
        btnSalary.addActionListener(e -> {
            displayPanel.removeAll();
            String salaryDetailsLocal = getApprovedSalaryDetails(empNumber);
            if (salaryDetailsLocal != null) {
                displayPanel.add(getSalaryPanel(salaryDetailsLocal), BorderLayout.CENTER);
            } else {
                JLabel lbl = new JLabel("Your salary details are pending approval.", SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createTitledBorder("Salary Status"));
                displayPanel.add(lbl, BorderLayout.CENTER);
            }
            displayPanel.revalidate();
            displayPanel.repaint();
        });

        // Initialize with Employee Details
        displayPanel.add(getEmployeeDetailsPanel(employeeData), BorderLayout.CENTER);

        empFrame.add(btnPanel, BorderLayout.SOUTH);
        empFrame.add(displayPanel, BorderLayout.CENTER);

        empFrame.setVisible(true);
    }

    private JPanel getEmployeeDetailsPanel(String[] employeeData) {
        JPanel panel = new JPanel(new GridLayout(14, 2));
        panel.setBorder(BorderFactory.createTitledBorder("Employee Details"));

        panel.add(new JLabel("Employee #:"));
        panel.add(new JLabel(employeeData[0]));
        panel.add(new JLabel("Last Name:"));
        panel.add(new JLabel(employeeData[1]));
        panel.add(new JLabel("First Name:"));
        panel.add(new JLabel(employeeData[2]));
        panel.add(new JLabel("Birthday:"));
        panel.add(new JLabel(employeeData[3]));
        panel.add(new JLabel("TIN #:"));
        panel.add(new JLabel(employeeData[4]));

        return panel;
    }

    private JPanel getAttendancePanel(List<String[]> attendanceRecords, String firstName, String lastName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Attendance Records"));

        if (attendanceRecords.isEmpty()) {
            panel.add(new JLabel("No attendance records for " + firstName + " " + lastName, SwingConstants.CENTER), BorderLayout.CENTER);
            return panel;
        }

        String[] colNames = {"Employee #", "Last Name", "First Name", "Date"};
        Object[][] data = new Object[attendanceRecords.size()][4];
        for (int i = 0; i < attendanceRecords.size(); i++) {
            data[i] = attendanceRecords.get(i);
        }

        JTable table = new JTable(new DefaultTableModel(data, colNames));
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getSalaryPanel(String salaryDetails) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Salary Details"));
        JLabel lbl = new JLabel("<html>" + salaryDetails.replaceAll("\n", "<br>") + "</html>");
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private List<String[]> getAttendanceRecords(String empNumber) {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(attendanceCsvPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4 && values[0].trim().equalsIgnoreCase(empNumber)) {
                    records.add(values);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading attendance data");
        }
        return records;
    }

    // Method to get approved salary details for specific employee
    private String getApprovedSalaryDetails(String empNumber) {
        String filePath = salaryApprovalDir + "/" + empNumber + ".txt";
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                // error reading file
            }
        }
        return null;
    }
    // SINGLE saveApprovedSalary method

    private void showHRView() {
        JFrame hrFrame = new JFrame("HR Dashboard");
        hrFrame.setSize(1000, 600);
        hrFrame.setLocationRelativeTo(null);
        hrFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            hrFrame.dispose();
            showLoginScreen();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(btnLogout);

        loadEmployees();

        String[] columnNames = {"Employee #", "Last Name", "First Name", "Birthday", "TIN #", "SSS #", "Philhealth #", "Pag-Ibig #", "Salary", "Semi-Monthly salary", "Hourly Rate"};
        DefaultTableModel model = new DefaultTableModel(new Object[0][0], columnNames);
        JTable employeeTable = new JTable(model);

        refreshEmployeeTable(employeeTable);

        JScrollPane scrollPane = new JScrollPane(employeeTable);

        JButton btnAdd = new JButton("Add Employee");
        JButton btnEdit = new JButton("Update Employee");
        JButton btnAttendance = new JButton("Attendance");
        JButton btnDelete = new JButton("Delete Employee");
        JButton btnComputeSalary = new JButton("Compute & Approve Salary");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnAttendance);
        buttonPanel.add(btnComputeSalary);

        // Add Employee
        btnAdd.addActionListener(e -> {
            Employee newEmp = showEmployeeDialog(null);
            if (newEmp != null) {
                employees.add(newEmp);
                saveEmployees();
                refreshEmployeeTable(employeeTable);
            }
        });

        // Edit Employee
        btnEdit.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                Employee emp = employees.get(selectedRow);
                Employee updatedEmp = showEmployeeDialog(emp);
                if (updatedEmp != null) {
                    employees.set(selectedRow, updatedEmp);
                    saveEmployees();
                    refreshEmployeeTable(employeeTable);
                }
            } else {
                JOptionPane.showMessageDialog(hrFrame, "Select an employee to edit");
            }
        });

        // Show Attendance
        btnAttendance.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                Employee emp = employees.get(selectedRow);
                showAttendanceDialog(emp);
            } else {
                JOptionPane.showMessageDialog(hrFrame, "Select an employee to view attendance");
            }
        });

        // Delete Employee
        btnDelete.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                Employee emp = employees.get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(hrFrame, "Are you sure you want to delete " + emp.firstName + " " + emp.lastName + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    employees.remove(selectedRow);
                    saveEmployees();
                    refreshEmployeeTable(employeeTable);
                }
            } else {
                JOptionPane.showMessageDialog(hrFrame, "Select an employee to delete");
            }
        });

        // Compute Salary and Approve
        btnComputeSalary.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                Employee emp = employees.get(selectedRow);
                int totalDays = computeDaysForCurrentMonth();

                double dailyRate = Double.parseDouble(emp.dailyRate) * 8;
                double totalSalary = totalDays * dailyRate;

                double sssRate = 0.0363;
                double pagIbigRate = 0.02;
                double philHealthRate = 0.035;

                double deductionsSSS = totalSalary * sssRate;
                double deductionsPagIbig = totalSalary * pagIbigRate;
                double deductionsPhilHealth = totalSalary * philHealthRate;

                double totalDeductions = deductionsSSS + deductionsPagIbig + deductionsPhilHealth;
                double netSalary = totalSalary - totalDeductions;

                String salaryDetails = String.format(
                        "Employee #: %s\n"
                        + "Last Name: %s\n"
                        + "First Name: %s\n\n"
                        + "Payroll Month: %s\n"
                        + "Total Days Worked: %d\n"
                        + "Daily Rate: Php %.2f\n"
                        + "Total Salary: Php %.2f\n\n"
                        + "Deductions:\n"
                        + "SSS (%.2f%%): Php %.2f\n"
                        + "Pag-IBIG (%.2f%%): Php %.2f\n"
                        + "PhilHealth (%.2f%%): Php %.2f\n\n"
                        + "Total Deductions: Php %.2f\n"
                        + "Net Salary: Php %.2f",
                        emp.empNumber,
                        emp.lastName,
                        emp.firstName,
                        java.time.LocalDate.now().getMonth(),
                        totalDays,
                        dailyRate,
                        totalSalary,
                        sssRate * 100, deductionsSSS,
                        pagIbigRate * 100, deductionsPagIbig,
                        philHealthRate * 100, deductionsPhilHealth,
                        totalDeductions,
                        netSalary
                );

                int confirm = JOptionPane.showConfirmDialog(
                        hrFrame,
                        salaryDetails + "\n\nApprove this salary?",
                        "Confirm Salary",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    saveApprovedSalary(emp.empNumber, salaryDetails);
                    JOptionPane.showMessageDialog(hrFrame, "Salary approved and saved for current month.");
                }
            } else {
                JOptionPane.showMessageDialog(hrFrame, "Select an employee first.");
            }
        });

        hrFrame.add(topPanel, BorderLayout.NORTH);
        hrFrame.add(scrollPane, BorderLayout.CENTER);
        hrFrame.add(buttonPanel, BorderLayout.SOUTH);
        hrFrame.setVisible(true);
    }

    // SINGLE saveApprovedSalary method (dito lang)
    private void saveApprovedSalary(String empNumber, String salaryDetails) {
        String filePath = salaryApprovalDir + "/" + empNumber + ".txt";
        try {
            File dir = new File(salaryApprovalDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                bw.write(salaryDetails);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving salary approval");
        }
    }

    private void loadEmployees() {
        employees.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(employeeCsvPath))) {
            String line;
            Boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                if (headerSkipped == false) {
                    headerSkipped = true;
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 4) {
                    Employee emp = new Employee(values[0].trim(), values[1].trim(), values[2].trim(), values[3].trim(), values[4].trim(), values[5].trim(), values[6].trim(), values[7].trim(), values[8].trim(), values[9].trim(), values[10].trim());
                    employees.add(emp);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading employees");
        }
    }

    private void saveEmployees() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(employeeCsvPath))) {
            for (Employee emp : employees) {
                bw.write(emp.empNumber + "," + emp.lastName + "," + emp.firstName + "," + emp.birthday + "," + emp.tin + "," + emp.sss + "," + emp.philhealth + "," + emp.pagibig + "," + emp.salary + "," + emp.halfSalary+ "," + emp.dailyRate);
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving employees " + e);
        }
    }

    private void refreshEmployeeTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Employee emp : employees) {
            model.addRow(new Object[]{emp.empNumber, emp.lastName, emp.firstName, emp.birthday, emp.tin, emp.sss, emp.philhealth, emp.pagibig, emp.salary, emp.halfSalary, emp.dailyRate});
        }
    }

    private Employee showEmployeeDialog(Employee emp) {
        JTextField txtEmpNumber = new JTextField();
        txtEmpNumber.setEditable(false);
        JTextField txtLastName = new JTextField();
        JTextField txtFirstName = new JTextField();
        JTextField txtBirthday = new JTextField(10); // user types MM/dd/yyyy
        JTextField txtTin = new JTextField();
        JTextField txtSss = new JTextField();
        JTextField txtPhil = new JTextField();
        JTextField txtPagibig = new JTextField();
        JTextField txtSalary = new JTextField();
        JTextField txtHalfSalary = new JTextField();
        JTextField txtDailyRate = new JTextField();
        if (emp != null) {
            txtEmpNumber.setText(emp.empNumber);
            txtLastName.setText(emp.lastName);
            txtFirstName.setText(emp.firstName);
            txtBirthday.setText(emp.birthday);
            txtTin.setText(emp.tin);
            txtSss.setText(emp.sss);
            txtPhil.setText(emp.philhealth);
            txtPagibig.setText(emp.pagibig);
            txtSss.setText(emp.sss);
            txtPhil.setText(emp.philhealth);
            txtPagibig.setText(emp.pagibig);
            txtSalary.setText(emp.salary);
            txtHalfSalary.setText(emp.halfSalary);
            txtDailyRate.setText(emp.dailyRate);
        } else {
            txtEmpNumber.setText(generateEmployeeNumber());
        }

        JPanel panel = new JPanel(new GridLayout(14, 2));
        panel.add(new JLabel("Employee #:"));
        panel.add(txtEmpNumber);
        panel.add(new JLabel("Last Name:"));
        panel.add(txtLastName);
        panel.add(new JLabel("First Name:"));
        panel.add(txtFirstName);
        panel.add(new JLabel("Birthday:"));
        panel.add(txtBirthday);
        panel.add(new JLabel("Tin:"));
        panel.add(txtTin);
        panel.add(new JLabel("SSS:"));
        panel.add(txtSss);
        panel.add(new JLabel("Phil Health:"));
        panel.add(txtPhil);
        panel.add(new JLabel("Pag-ibig:"));
        panel.add(txtPagibig);
        panel.add(new JLabel("Salary:"));
        panel.add(txtSalary);
        panel.add(new JLabel("Semi-Monthly:"));
        panel.add(txtHalfSalary);
        panel.add(new JLabel("Daily Rate:"));
        panel.add(txtDailyRate);

        int result = JOptionPane.showConfirmDialog(null, panel,
                (emp == null ? "Add Employee" : "Edit Employee"),
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {

            var validFlag = true;
            var emptyMessage = "";
            var emptyFlag = true;

            if ("".equals(txtEmpNumber.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Employee Number can't be empty\n";
            }
            if ("".equals(txtLastName.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Last name can't be empty\n";
            }
            if ("".equals(txtFirstName.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "First name can't be empty\n";
            }
            if ("".equals(txtBirthday.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Birth day can't be empty\n";
            }
            if ("".equals(txtTin.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "TIN # can't be empty\n";
            }
            if ("".equals(txtSss.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "SSS # can't be empty\n";
            }
            if ("".equals(txtPhil.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Phil Health # can't be empty\n";
            }
            if ("".equals(txtPagibig.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Pag-ibig # can't be empty\n";
            }
            
            if ("".equals(txtSalary.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Salary can't be empty\n";
            }
            
            if ("".equals(txtHalfSalary.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Semi-Monthly salary can't be empty\n";
            }
            
            if ("".equals(txtDailyRate.getText().trim())) {
                validFlag = false;
                emptyFlag = false;
                emptyMessage += "Daily rate can't be empty\n";
            }

           if (emptyFlag == false) {
               JOptionPane.showMessageDialog(null, emptyMessage);
               return null;
           } else {
               try {
                   LocalDate date = LocalDate.parse(txtBirthday.getText(), DateTimeFormatter.ofPattern("M/d/yyyy"));
               } catch (DateTimeParseException e) {
                   validFlag = false;
                   JOptionPane.showMessageDialog(null, "Invalid date format. Use M/d/yyyy");
               }
           }
            
           try {
               Double.parseDouble(txtSalary.getText());
               Double.parseDouble(txtHalfSalary.getText());
               Double.parseDouble(txtDailyRate.getText());
           } catch (NumberFormatException e) {
               validFlag = false;
               JOptionPane.showMessageDialog(null, "Invalid salary format");
           }

            if (validFlag || true) {
                return new Employee(
                        txtEmpNumber.getText().trim(),
                        txtLastName.getText().trim(),
                        txtFirstName.getText().trim(),
                        txtBirthday.getText().trim(),
                        txtTin.getText().trim(),
                        txtSss.getText().trim(),
                        txtPhil.getText().trim(),
                        txtPagibig.getText().trim(),
                        txtSalary.getText().trim(),
                        txtHalfSalary.getText().trim(),
                        txtDailyRate.getText().trim()
                );
            } else {
                showEmployeeDialog(new Employee(txtEmpNumber.getText(),
                        txtLastName.getText(),
                        txtFirstName.getText(),
                        txtBirthday.getText(),
                        txtTin.getText(),
                        txtSss.getText(),
                        txtPhil.getText(),
                        txtPagibig.getText(),
                        txtSalary.getText().trim(),
                        txtHalfSalary.getText().trim(),
                        txtDailyRate.getText().trim())
                );
            }

        }
        return null;
    }

    private String generateEmployeeNumber() {
        String empNum = "";
        try (BufferedReader br = new BufferedReader(new FileReader(employeeCsvPath))) {
            String line;
            Boolean headerSkipped = false;
            Boolean lastRow = false;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4) {
                    empNum = values[0].trim();
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading employees");
        }
        return Integer.parseInt(empNum) + 1 + "";
    }

    private void showAttendanceDialog(Employee emp) {
        List<String[]> attendanceRecords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(attendanceCsvPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4 && values[0].trim().equalsIgnoreCase(emp.empNumber)) {
                    attendanceRecords.add(values);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading attendance data");
            return;
        }

        if (attendanceRecords.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No attendance records for " + emp.firstName + " " + emp.lastName);
            return;
        }

        String[] colNames = {"Employee #", "Last Name", "First Name", "Date"};
        Object[][] data = new Object[attendanceRecords.size()][4];
        for (int i = 0; i < attendanceRecords.size(); i++) {
            data[i] = attendanceRecords.get(i);
        }

        JTable attendanceTable = new JTable(new DefaultTableModel(data, colNames));
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        JOptionPane.showMessageDialog(null, scrollPane, "Attendance for " + emp.firstName + " " + emp.lastName, JOptionPane.PLAIN_MESSAGE);
    }

    // Method para makuha ang total days sa current month
    private int computeDaysForCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        return currentMonth.lengthOfMonth();
    }

    class Employee {

        String empNumber;
        String lastName;
        String firstName;
        String birthday;
        String tin;
        String sss;
        String philhealth;
        String pagibig;
        String salary;
        String halfSalary;
        String dailyRate;

        Employee(String empNumber, String lastName, String firstName, String birthday, String tin, String sss, String philhealth, String pagibig, String salary, String halfSalary, String dailyRate) {

            this.empNumber = empNumber;
            this.lastName = lastName;
            this.firstName = firstName;
            this.birthday = birthday;
            this.tin = tin;
            this.sss = sss;
            this.philhealth = philhealth;
            this.pagibig = pagibig;
            this.salary = salary;
            this.halfSalary = halfSalary;
            this.dailyRate = dailyRate;
        }
    }
}
