import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;

public class AdminEditForm extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/campus_lostfound?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "r00t";

    private JTable table;
    private DefaultTableModel model;
    private int selectedId = -1;

    public AdminEditForm() {
        setTitle("Admin Panel - Manage Cases");
        setSize(950, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        String[] cols = {"ID", "Name", "Semester", "Department", "Item", "Location", "Status", "Contact"};
        for (String col : cols) model.addColumn(col);

        table = new JTable(model);
        loadItems();

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        selectedId = (int) table.getValueAt(row, 0);
                        openEditForm(
                                (String) table.getValueAt(row, 4), // item
                                (String) table.getValueAt(row, 5), // location
                                (String) table.getValueAt(row, 6), // status
                                (String) table.getValueAt(row, 7), // contact
                                (String) table.getValueAt(row, 1), // name
                                (String) table.getValueAt(row, 2), // semester
                                (String) table.getValueAt(row, 3)  // department
                        );
                    }
                }
            }
        });

        JPanel buttons = new JPanel(new FlowLayout());
        JButton refresh = new JButton("Refresh");
        CampusLostFoundApp.styleButton(refresh);
        refresh.addActionListener(e -> loadItems());
        buttons.add(refresh);

        setLayout(new BorderLayout());
        add(new JLabel("🔒 Admin Panel – Double-click any row to edit", JLabel.CENTER), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void loadItems() {
        model.setRowCount(0);
        String sql = "SELECT id, user_name, semester, department, item_name, location, status, contact_info FROM items ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("user_name"),
                        rs.getString("semester"),
                        rs.getString("department"),
                        rs.getString("item_name"),
                        rs.getString("location"),
                        rs.getString("status"),
                        rs.getString("contact_info")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
        }
    }

    private void openEditForm(String item, String loc, String status, String contact, String name, String sem, String dept) {
        JFrame editFrame = new JFrame("Edit Item Details");
        editFrame.setSize(500, 600);
        editFrame.setLayout(null);
        editFrame.setLocationRelativeTo(this);

        // Item
        addEditField(editFrame, "Item Name:", 50, item, "item");
        // Location
        addEditField(editFrame, "Location:", 100, loc, "loc");
        // Status
        String[] statuses = {"Lost", "Found", "Claimed", "Closed"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(status);
        statusCombo.setBounds(150, 150, 200, 25);
        editFrame.add(new JLabel("Status:")).setBounds(50, 150, 100, 25);
        editFrame.add(statusCombo);
        // Contact
        JTextField contactField = addEditField(editFrame, "Contact:", 200, contact, "contact");
        // Name
        JTextField nameField = addEditField(editFrame, "Name:", 250, name, "name");
        // Semester
        JTextField semField = addEditField(editFrame, "Semester:", 300, sem, "sem");
        // Department
        JTextField deptField = addEditField(editFrame, "Department:", 350, dept, "dept");

        JButton save = new JButton("Save Changes");
        CampusLostFoundApp.styleButton(save);
        save.setBounds(170, 420, 160, 35);
        save.addActionListener(e -> {
            if (saveChangesToDB(
                    item, loc, (String) statusCombo.getSelectedItem(),
                    contactField.getText(), nameField.getText(),
                    semField.getText(), deptField.getText()
            )) {
                JOptionPane.showMessageDialog(editFrame, "✅ Changes saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadItems(); // Refresh table
                editFrame.dispose();
            }
        });
        editFrame.add(save);
        editFrame.setVisible(true);
    }

    private JTextField addEditField(JFrame frame, String label, int y, String value, String key) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBounds(50, y, 120, 25);
        frame.add(lbl);

        JTextField field = new JTextField(value);
        field.setBounds(150, y, 200, 25);
        frame.add(field);
        return field;
    }

    private boolean saveChangesToDB(String item, String loc, String status, String contact, String name, String sem, String dept) {
        String sql = "UPDATE items SET item_name=?, location=?, status=?, contact_info=?, user_name=?, semester=?, department=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item);
            stmt.setString(2, loc);
            stmt.setString(3, status);
            stmt.setString(4, contact);
            stmt.setString(5, name);
            stmt.setString(6, sem);
            stmt.setString(7, dept);
            stmt.setInt(8, selectedId);

            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
            return false;
        }
    }

    public static void showAdminLogin() {
        // Create a custom dialog with JPasswordField
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Enter Admin Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Admin Login",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String enteredPassword = new String(passwordField.getPassword());
            if ("admin".equals(enteredPassword)) { // ✅ Your password is "admin"
                new AdminEditForm().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "❌ Incorrect password!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}