package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

class MainFrame extends JFrame {
    private DatabaseManager dbManager;

    public MainFrame() throws SQLException {
        dbManager = new DatabaseManager();
        setTitle("Управление данными");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        add(createEducationPanel());
        add(createExperiencePanel());
        add(createCompanyPanel());
        add(createResultPanel());
        add(createApplicantPanel());

        setVisible(true);
    }

    private JPanel createEducationPanel() {
        return createDataPanel("Образование", new String[]{"Университет", "Степень"}, "Education");
    }

    private JPanel createExperiencePanel() {
        return createDataPanel("Опыт работы", new String[]{"Компания", "Должность"}, "Experience");
    }

    private JPanel createCompanyPanel() {
        return createDataPanel("Компания", new String[]{"Название компании"}, "Companies");
    }

    private JPanel createResultPanel() {
        return createDataPanel("Результат", new String[]{"Applicant ID", "Ключевые навыки"}, "Results");
    }

    private JPanel createApplicantPanel() {
        return createDataPanel("Соискатель", new String[]{"Имя", "Email"}, "Applicants");
    }

    private JPanel createDataPanel(String title, String[] columns, String tableName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title)); // Добавляем заголовок

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        loadTableData(table, tableName);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = createForm(columns, table, tableName);
        panel.add(form, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createForm(String[] labels, JTable table, String tableName) {
        JPanel form = new JPanel(new FlowLayout());
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            JPanel fieldPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(labels[i] + ":");
            fields[i] = new JTextField(10);
            fieldPanel.add(label, BorderLayout.NORTH);
            fieldPanel.add(fields[i], BorderLayout.CENTER);
            form.add(fieldPanel);
        }

        JButton addButton = new JButton("Добавить");
        JButton removeButton = new JButton("Удалить");

        addButton.addActionListener(e -> addRow(fields, table, tableName));
        removeButton.addActionListener(e -> removeRow(table, tableName));

        form.add(addButton);
        form.add(removeButton);
        return form;
    }

    private void addRow(JTextField[] fields, JTable table, String tableName) {
        Object[] rowData = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            rowData[i] = fields[i].getText();
            fields[i].setText("");
        }

        String sql = buildInsertSQL(tableName, fields.length);
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < rowData.length; i++) {
                pstmt.setObject(i + 1, rowData[i]);
            }
            pstmt.executeUpdate();
            ((DefaultTableModel) table.getModel()).addRow(rowData);
           loadTableData(table, tableName);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при добавлении записи: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);

        }
    }

    private String buildInsertSQL(String tableName, int columnCount) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        switch (tableName) {
            case "Applicants":
                sql.append("name, email");
                break;
            case "Education":
                sql.append("university, degree");
                break;
            case "Experience":
                sql.append("company, position");
                break;
            case "Companies":
                sql.append("company_name");
                break;
            case "Results":
                sql.append("applicant_id, key_skills");
                break;
            default:
                return null;
        }

        sql.append(") VALUES (");
        for (int i = 0; i < columnCount; i++) {
            sql.append("?");
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
        sql.append(")");
        return sql.toString();
    }


    private void removeRow(JTable table, String tableName) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Object idValue = model.getValueAt(selectedRow, 0);

            if (idValue == null) {
                JOptionPane.showMessageDialog(this, "ID записи не найдено.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Connection conn = dbManager.getConnection();
                String sql = "DELETE FROM " + tableName + " WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, idValue.toString());
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    model.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось удалить запись.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
                loadTableData(table, tableName);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Ошибка при удалении записи: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите строку для удаления.", "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }



    private void loadTableData(JTable table, String tableName) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        String query = "SELECT * FROM " + tableName;
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при загрузке данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    }