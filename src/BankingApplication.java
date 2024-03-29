package Bank.src;
import java.sql.*;
import java.util.Scanner;

public class BankingApplication {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/banking";

    static final String USER = "root";
    static final String PASS = "Admin@123";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            // Creating tables if not exists
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "userid INT AUTO_INCREMENT PRIMARY KEY," +
                    "username VARCHAR(50) NOT NULL," +
                    "accountno VARCHAR(20) NOT NULL," +
                    "balance DECIMAL(10,2) NOT NULL)";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "transactionid INT AUTO_INCREMENT PRIMARY KEY," +
                    "userid INT," +
                    "transactiondate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "transactiontype VARCHAR(20) NOT NULL," +
                    "transactionamount DECIMAL(10,2) NOT NULL," +
                    "FOREIGN KEY (userid) REFERENCES users(userid))";
            stmt.executeUpdate(sql);

            Scanner scanner = new Scanner(System.in);
            int choice;

            System.out.println("Welcome to the bank.");
            System.out.println("Please select any one of the below options:");
            System.out.println("1. Create account");
            System.out.println("2. Checking the balance.");
            System.out.println("3. Deposit money.");
            System.out.println("4. Withdraw money.");

            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    createAccount(conn, scanner);
                    break;
                case 2:
                    checkBalance(conn, scanner);
                    break;
                case 3:
                    depositMoney(conn, scanner);
                    break;
                case 4:
                    withdrawMoney(conn, scanner);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

            scanner.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    private static void createAccount(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter username:");
        String username = scanner.next();
        System.out.println("Enter account number:");
        String accountNo = scanner.next();
        System.out.println("Enter initial balance:");
        double balance = scanner.nextDouble();

        String sql = "INSERT INTO users (username, accountno, balance) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, accountNo);
        preparedStatement.setDouble(3, balance);
        int rowsAffected = preparedStatement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Account created successfully.");
        } else {
            System.out.println("Failed to create account.");
        }
    }

    private static void checkBalance(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter your account number:");
        String accountNo = scanner.next();

        String sql = "SELECT balance FROM users WHERE accountno = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, accountNo);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            double balance = resultSet.getDouble("balance");
            System.out.println("Your account balance: " + balance);
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void depositMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter your account number:");
        String accountNo = scanner.next();
        System.out.println("Enter the amount to deposit:");
        double amount = scanner.nextDouble();

        String updateSql = "UPDATE users SET balance = balance + ? WHERE accountno = ?";
        PreparedStatement updateStatement = conn.prepareStatement(updateSql);
        updateStatement.setDouble(1, amount);
        updateStatement.setString(2, accountNo);
        int rowsUpdated = updateStatement.executeUpdate();
        if (rowsUpdated > 0) {
            String insertTransactionSql = "INSERT INTO transactions (userid, transactiontype, transactionamount) " +
                    "SELECT userid, 'DEPOSIT', ? FROM users WHERE accountno = ?";
            PreparedStatement insertTransactionStatement = conn.prepareStatement(insertTransactionSql);
            insertTransactionStatement.setDouble(1, amount);
            insertTransactionStatement.setString(2, accountNo);
            insertTransactionStatement.executeUpdate();
            System.out.println("Deposit successful.");
        } else {
            System.out.println("Failed to deposit money.");
        }
    }

    private static void withdrawMoney(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Enter your account number:");
        String accountNo = scanner.next();
        System.out.println("Enter the amount to withdraw:");
        double amount = scanner.nextDouble();

        String updateSql = "UPDATE users SET balance = balance - ? WHERE accountno = ?";
        PreparedStatement updateStatement = conn.prepareStatement(updateSql);
        updateStatement.setDouble(1, amount);
        updateStatement.setString(2, accountNo);
        int rowsUpdated = updateStatement.executeUpdate();
        if (rowsUpdated > 0) {
            String insertTransactionSql = "INSERT INTO transactions (userid, transactiontype, transactionamount) " +
                    "SELECT userid, 'WITHDRAW', ? FROM users WHERE accountno = ?";
            PreparedStatement insertTransactionStatement = conn.prepareStatement(insertTransactionSql);
            insertTransactionStatement.setDouble(1, amount);
            insertTransactionStatement.setString(2, accountNo);
            insertTransactionStatement.executeUpdate();
            System.out.println("Withdrawal successful.");
        } else {
            System.out.println("Failed to withdraw money.");
        }
    }
}
