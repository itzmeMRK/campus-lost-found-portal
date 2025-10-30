import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JDateChooser;

public class CampusLostFoundApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/campus_lostfound?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "r00t";

    // ===== MAIN METHOD =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Campus Lost & Found Portal");
        frame.setSize(420, 240);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel title = new JLabel("Campus Lost & Found Portal");
        title.setFont(new Font("Arial Black", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, 30, 420, 30);
        frame.add(title);

        JButton btnStudent = new JButton("I'm a Student");
        styleButton(btnStudent);
        btnStudent.setBounds(130, 90, 160, 35);
        btnStudent.addActionListener(e -> showStudentOptions());
        frame.add(btnStudent);

        JButton btnAdmin = new JButton("Admin Login");
        styleButton(btnAdmin);
        btnAdmin.setBounds(130, 140, 160, 35);
        btnAdmin.addActionListener(e -> AdminEditForm.showAdminLogin());
        frame.add(btnAdmin);

        frame.setVisible(true);
    }

    private static void showStudentOptions() {
        JFrame studentFrame = new JFrame("Student Options");
        studentFrame.setSize(340, 240);
        studentFrame.setLayout(null);
        studentFrame.setLocationRelativeTo(null);
        studentFrame.getContentPane().setBackground(Color.WHITE);

        JButton btnLost = new JButton("Report Lost Item");
        styleButton(btnLost);
        btnLost.setBounds(90, 40, 160, 35);
        btnLost.addActionListener(e -> new ReportForm("Lost").setVisible(true));
        studentFrame.add(btnLost);

        JButton btnFound = new JButton("Report Found Item");
        styleButton(btnFound);
        btnFound.setBounds(90, 90, 160, 35);
        btnFound.addActionListener(e -> new ReportForm("Found").setVisible(true));
        studentFrame.add(btnFound);

        JButton btnView = new JButton("View All Items");
        styleButton(btnView);
        btnView.setBounds(90, 140, 160, 35);
        btnView.addActionListener(e -> new ViewItemsFrame().setVisible(true));
        studentFrame.add(btnView);

        studentFrame.setVisible(true);
    }

    // Change this line:
    public static void styleButton(JButton btn) {  // ← was 'private'
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(30, 144, 255));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    // ===== REPORT FORM (LOST OR FOUND) =====
    static class ReportForm extends JFrame {
        private final String reportType; // "Lost" or "Found"
        private JComboBox<String> itemCombo, locationCombo;
        private JTextField otherItemField, otherLocationField, contactField, nameField, semField, deptField;
        private JTextArea descArea;
        private JDateChooser dateChooser;

        public ReportForm(String type) {
            this.reportType = type;
            setTitle("Report " + type + " Item");
            setSize(500, 780);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(null);
            getContentPane().setBackground(Color.WHITE);

            JLabel heading = new JLabel("Report a " + type + " Item");
            heading.setFont(new Font("Arial Black", Font.BOLD, 20));
            heading.setHorizontalAlignment(SwingConstants.CENTER);
            heading.setBounds(0, 20, 500, 40);
            add(heading);

            // Item
            addLabel("What did you " + (type.equals("Lost") ? "lose" : "find") + "?*", 80);
            String[] items = {"ID Card", "Mobile Phone", "Laptop", "Wallet", "Keys", "Books", "Water Bottle", "Earphones", "Clothing", "Other"};
            itemCombo = new JComboBox<>(items);
            itemCombo.setBounds(50, 110, 300, 30);
            itemCombo.addActionListener(e -> toggleOther(itemCombo, otherItemField));
            add(itemCombo);
            otherItemField = new JTextField();
            otherItemField.setBounds(50, 150, 300, 30);
            otherItemField.setVisible(false);
            add(otherItemField);

            // Description
            addLabel("Description (color, brand, etc.):", 200);
            descArea = new JTextArea();
            add(new JScrollPane(descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {{
                setBounds(50, 230, 300, 60);
            }});

            // Location
            addLabel("Where did you " + (type.equals("Lost") ? "lose" : "find") + " it?*", 310);
            String[] locs = {"Main Library", "Engineering A", "Student Union", "Cafeteria", "Gym", "Bus Stop", "CS Lab 305", "Admin Block", "Auditorium", "Other"};
            locationCombo = new JComboBox<>(locs);
            locationCombo.setBounds(50, 340, 300, 30);
            locationCombo.addActionListener(e -> toggleOther(locationCombo, otherLocationField));
            add(locationCombo);
            otherLocationField = new JTextField();
            otherLocationField.setBounds(50, 380, 300, 30);
            otherLocationField.setVisible(false);
            add(otherLocationField);

            // Date
            addLabel("Date " + type + "*:", 430);
            dateChooser = new JDateChooser();
            dateChooser.setBounds(160, 430, 190, 30);
            dateChooser.setDateFormatString("yyyy-MM-dd");
            add(dateChooser);

            // Contact
            addLabel("Contact (Email/Phone):", 470);
            contactField = new JTextField();
            contactField.setBounds(50, 500, 300, 30);
            add(contactField);

            // User Info
            addLabel("Your Full Name*:", 540);
            nameField = new JTextField();
            nameField.setBounds(50, 570, 300, 30);
            add(nameField);

            addLabel("Semester*:", 610);
            semField = new JTextField();
            semField.setBounds(50, 640, 140, 30);
            add(semField);

            addLabel("Department*:", 610);
            deptField = new JTextField();
            deptField.setBounds(210, 640, 140, 30);
            add(deptField);

            JButton submit = new JButton("Submit " + type + " Report");
            styleButton(submit);
            submit.setBounds(170, 690, 180, 35);
            submit.addActionListener(e -> submitReport());
            add(submit);
        }

        private void addLabel(String text, int y) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setBounds(50, y, 250, 25);
            add(lbl);
        }

        private void toggleOther(JComboBox<String> combo, JTextField field) {
            field.setVisible("Other".equals(combo.getSelectedItem()));
            revalidate(); repaint();
        }

        private void submitReport() {
            String item = (String) itemCombo.getSelectedItem();
            if ("Other".equals(item)) {
                item = otherItemField.getText().trim();
                if (item.isEmpty()) { showError("Specify the item."); return; }
            }

            String loc = (String) locationCombo.getSelectedItem();
            if ("Other".equals(loc)) {
                loc = otherLocationField.getText().trim();
                if (loc.isEmpty()) { showError("Specify the location."); return; }
            }

            String dateStr = "";
            if (dateChooser.getDate() != null) {
                dateStr = new SimpleDateFormat("yyyy-MM-dd").format(dateChooser.getDate());
            } else { showError("Select a date."); return; }

            String contact = contactField.getText().trim();
            String name = nameField.getText().trim();
            String sem = semField.getText().trim();
            String dept = deptField.getText().trim();

            if (name.isEmpty() || sem.isEmpty() || dept.isEmpty()) {
                showError("Fill all required fields.");
                return;
            }

            String status = reportType; // "Lost" or "Found"
            String sql = "INSERT INTO items (item_name, description, location, date_lost, status, contact_info, user_name, semester, department, reported_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, item);
                stmt.setString(2, descArea.getText().trim());
                stmt.setString(3, loc);
                stmt.setString(4, dateStr);
                stmt.setString(5, status);
                stmt.setString(6, contact);
                stmt.setString(7, name);
                stmt.setString(8, sem);
                stmt.setString(9, dept);
                stmt.setString(10, reportType.equals("Lost") ? "Student" : "Finder");

                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(this, "✅ " + reportType + " item reported!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (Exception ex) {
                showError("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        private void showError(String msg) {
            JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ===== VIEW ITEMS FRAME =====
    static class ViewItemsFrame extends JFrame {
        private JTable table;
        private DefaultTableModel model;

        public ViewItemsFrame() {
            setTitle("All Lost & Found Items");
            setSize(850, 500);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            model = new DefaultTableModel();
            String[] cols = {"ID", "Reported By", "Item", "Location", "Date", "Status", "Contact"};
            for (String col : cols) model.addColumn(col);

            table = new JTable(model);
            loadItems("");

            JPanel top = new JPanel(new FlowLayout());
            top.add(new JLabel("Search (Item/Location):"));
            JTextField search = new JTextField(20);
            top.add(search);
            JButton btnSearch = new JButton("Search");
            styleButton(btnSearch);
            btnSearch.addActionListener(e -> loadItems(search.getText().trim()));
            top.add(btnSearch);

            setLayout(new BorderLayout());
            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
        }

        private void loadItems(String keyword) {
            model.setRowCount(0);
            String sql = "SELECT id, user_name, item_name, location, date_lost, status, contact_info FROM items";
            if (!keyword.isEmpty()) sql += " WHERE item_name LIKE ? OR location LIKE ?";
            sql += " ORDER BY id DESC";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                if (!keyword.isEmpty()) {
                    String k = "%" + keyword + "%";
                    stmt.setString(1, k);
                    stmt.setString(2, k);
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("user_name"),
                            rs.getString("item_name"),
                            rs.getString("location"),
                            rs.getDate("date_lost"),
                            rs.getString("status"),
                            rs.getString("contact_info")
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Load error: " + ex.getMessage());
            }
        }
    }
}