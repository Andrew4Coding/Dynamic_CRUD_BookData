package MySQLConnection.BookData;

import MySQLConnection.register;
import net.proteanit.sql.DbUtils;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Form extends JFrame implements ActionListener{
    private JPanel mainPanel;
    private JTextField bookNameField;
    private JTextField categoryField;
    private JButton saveButton;
    private JButton clearButton;
    private JTextField bookIDSearchField;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable showTable;
    private JTextField priceField;
    private JButton exitButton;
    private JButton searchButton;
    private JLabel codeLabel;
    private JButton deleteAllButton;

    JButton[] buttonList = {saveButton, searchButton, clearButton, exitButton, deleteButton, updateButton, deleteAllButton};

    Connection con;
    PreparedStatement pst;
    ResultSet rs;

    static int bookCode = -1;

    static final String DB_URL = "jdbc:mysql://localhost:3307/bookstore";
    static final String USER = "root";
    static final String PASS = "12345";
    static final String TABLENAME = "booktable";

    void createScreen(){
        setTitle("Book Store");
        setSize(700, 500);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);
    }
    Form() {
        createScreen();
        setContentPane(mainPanel);

        for (JButton b : buttonList){
            b.addActionListener(this);
        }

        connect();

        showTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = showTable.getSelectedRow();
                System.out.println(i + 1);

                TableModel tblmodel1 = showTable.getModel();

                codeLabel.setText("Registration: Code #" + (tblmodel1.getValueAt(i, 0).toString()));
                bookCode = Integer.parseInt(tblmodel1.getValueAt(i, 0).toString());
                bookNameField.setText(tblmodel1.getValueAt(i, 1).toString());
                categoryField.setText(tblmodel1.getValueAt(i, 2).toString());
                priceField.setText(tblmodel1.getValueAt(i, 3).toString());
            }
        });
    }

    void connect(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL,USER,PASS);
            loadSQLTable();
        }
        catch (ClassNotFoundException | SQLException e){
            Logger.getLogger(register.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == saveButton){
            createData();
        }
        else if (src == searchButton){
            readData();
        }
        else if (src == clearButton){
            clear();
        }
        else if (src == exitButton){
            dispose();
        }
        else if (src == deleteButton) {
            deleteData();
        }
        else if (src == updateButton){
            updateData();
        }
        else if (src == deleteAllButton){
            deleteAllData();
        }

    }

    void createData(){
        String getBookName = bookNameField.getText();
        String getCategory = categoryField.getText();
        String getPrice = priceField.getText();

        if (getBookName.isEmpty() || getCategory.isEmpty() || getPrice.isEmpty()){
            JOptionPane.showMessageDialog(this, "Field Can't be Empty", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else{
            try{
                String statement = "INSERT INTO " + TABLENAME + "(bookname, category, price) VALUES (?, ?, ?)";
                pst = con.prepareStatement(statement);

                pst.setString(1, getBookName);
                pst.setString(2, getCategory);
                pst.setString(3, getPrice);

                int k = pst.executeUpdate();
                if (k == 1){
                    JOptionPane.showMessageDialog(this, "Data Entered Successfully");
                    bookNameField.setText("");
                    categoryField.setText("");
                    priceField.setText("");
                    codeLabel.setText("Registration: ");

                    loadSQLTable();
                }
                else {
                    JOptionPane.showMessageDialog(this, "Data Error");
                }
            }
            catch (SQLException ex){
                throw new RuntimeException(ex);
            }
        }
    }
    void readData(){
        String getContent = bookIDSearchField.getText();

        if (getContent.isEmpty()){
            JOptionPane.showMessageDialog(this, "FIELD CAN'T BE EMPTY", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else {
            try{
                String statement = "SELECT * FROM " + TABLENAME + " WHERE ID = ? || INSTR(bookname, ?) || INSTR(category, ?) || INSTR(price, ?)";
                pst = con.prepareStatement(statement);
                for (int i = 1; i <= 4; i++) {
                    pst.setString(i, getContent);
                }
                rs = pst.executeQuery();
                if (rs.next()){
                    int phpID = rs.getInt("id");
                    String phpBookName = rs.getString("bookname");
                    String phpCategory = rs.getString("category");
                    String phpPrice = rs.getString("price");

                    bookNameField.setText(phpBookName);
                    categoryField.setText(phpCategory);
                    priceField.setText(phpPrice);
                    codeLabel.setText("Code #" + phpID);
                    loadSpecific(getContent);
                    bookCode = phpID;
                }

                else {
                    JOptionPane.showMessageDialog(this, "DATA NOT FOUND");
                }
            }
            catch (SQLException ex){
                throw new RuntimeException(ex);
            }
        }

    }
    void clear(){
        bookNameField.setText("");
        categoryField.setText("");
        priceField.setText("");
        bookIDSearchField.setText("");
        codeLabel.setText("Registration: ");

        loadSQLTable();
    }
    void deleteData(){
        int getID = -1;
        try{
            if (bookIDSearchField.getText().isEmpty()){
                if (bookCode == -1){
                    JOptionPane.showMessageDialog(this, "NOT DELETED", "Alert", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    getID = bookCode;
                }
            }
            else{
                getID = Integer.parseInt(bookIDSearchField.getText());
            }

            String statement = "DELETE FROM " + TABLENAME + " WHERE ID = " + getID;
            pst = con.prepareStatement(statement);

            int k = pst.executeUpdate();
            if (k == 1){
                JOptionPane.showMessageDialog(this, "DATA DELETED", "Alert", JOptionPane.INFORMATION_MESSAGE);
                String statement2 = "ALTER TABLE " + TABLENAME + " AUTO_INCREMENT = 1";
                pst = con.prepareStatement(statement2);

                pst.executeUpdate();
            }
            else {
                JOptionPane.showMessageDialog(this, "NOT DELETED", "Alert", JOptionPane.WARNING_MESSAGE);
            }

            loadSQLTable();
        }
        catch (SQLException ex){
            throw new RuntimeException(ex);
        }
    }
    void deleteAllData(){
        try {
            String statement = "DELETE FROM " + TABLENAME;
            pst = con.prepareStatement(statement);

            int k = pst.executeUpdate();
            if (k == 1){
                JOptionPane.showMessageDialog(this, "ALL ENTRY DELETED");
                statement = "ALTER TABLE " + TABLENAME + " AUTO_INCREMENT = 1";
                pst = con.prepareStatement(statement);

                pst.executeUpdate();

                loadSQLTable();
            }
            else {
                JOptionPane.showMessageDialog(this, "NOT DELETED", "Alert", JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (SQLException ex){
            throw new RuntimeException(ex);
        }
    }
    void updateData(){
        String getBookName = bookNameField.getText();
        String getCategory = categoryField.getText();
        String getPrice = priceField.getText();
        String getID = Integer.toString(bookCode);

        if (getBookName.isEmpty() || getCategory.isEmpty() || getPrice.isEmpty() || getID.isEmpty()){
            JOptionPane.showMessageDialog(this, "FIELD CAN'T BE EMPTY", "Alert", JOptionPane.WARNING_MESSAGE);
        }
        else {
            try{
                String statement = "UPDATE " + TABLENAME + " SET bookname = ?, category = ?, price = ? WHERE ID = ?";
                pst = con.prepareStatement(statement);
                pst.setString(1, getBookName);
                pst.setString(2, getCategory);
                pst.setString(3, getPrice);
                pst.setString(4, getID);

                int k = pst.executeUpdate();
                if (k == 1){
                    JOptionPane.showMessageDialog(this, "UPDATE SUCCEEDED");
                    loadSQLTable();
                }
                else {
                    JOptionPane.showMessageDialog(this, "UPDATE FAILED", "Alert", JOptionPane.WARNING_MESSAGE);
                }
            }
            catch (SQLException ex){
                throw new RuntimeException(ex);
            }
        }

    }

    void loadSQLTable(){
        try{
            String statement = "SELECT * FROM " + TABLENAME;
            pst = con.prepareStatement(statement);
            rs = pst.executeQuery();
            showTable.setModel(DbUtils.resultSetToTableModel(rs));
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }
    void loadSpecific(String thing){
        try{
            String statement = "SELECT * FROM " + TABLENAME + " WHERE ID = ? || INSTR(bookname, ?) || INSTR(category, ?) || INSTR(price, ?) ";
            pst = con.prepareStatement(statement);
            pst.setString(1, thing);
            pst.setString(2, thing);
            pst.setString(3, thing);
            pst.setString(4, thing);

            rs = pst.executeQuery();
            showTable.setModel(DbUtils.resultSetToTableModel(rs));
        }
        catch (SQLException ex){
            ex.printStackTrace();
        }
    }
}
