 package com.Servlets;
 
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
public class UserRegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 90598906L;
    private static final Logger logger = Logger.getLogger(UserRegistrationServlet.class.getName());
 
    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/nani";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "nani123@@";
 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
 
        String role = request.getParameter("user");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String department = request.getParameter("department");
        String designation = request.getParameter("designation");
 
        Connection connection = null;
        PrintWriter out = response.getWriter();
        
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false); // Start transaction
 
            String lastIdQuery = "SELECT MAX(user_id) AS max_id FROM user_register WHERE role = ?";
            try (PreparedStatement stmt = connection.prepareStatement(lastIdQuery)) {
                stmt.setString(1, role);
                ResultSet rs = stmt.executeQuery();
 
                int newUserId = 1;
                if (rs.next()) {
                    newUserId = rs.getInt("max_id") + 1;
                }
 
                String insertQuery1 = "INSERT INTO user_register (user_id, user_name, email, password, role) VALUES (?, ?, ?, ?, ?)";
                String insertQuery2 = "INSERT INTO doctor (doctor_id, user_name, email) VALUES (?, ?, ?)";
                String insertQuery3 = "INSERT INTO staff (staff_id, department, designation) VALUES (?, ?, ?)";
 
                boolean isSuccess = false;
 
                if ("admin".equals(role)) {
                    try (PreparedStatement ps1 = connection.prepareStatement(insertQuery1)) {
                        ps1.setInt(1, newUserId);
                        ps1.setString(2, username);
                        ps1.setString(3, email);
                        ps1.setString(4, password);
                        ps1.setString(5, role);
                        isSuccess = ps1.executeUpdate() > 0;
                    }
                } else if ("doctor".equals(role)) {
                    try (PreparedStatement ps1 = connection.prepareStatement(insertQuery1);
                         PreparedStatement ps2 = connection.prepareStatement(insertQuery2);
                         PreparedStatement ps3 = connection.prepareStatement(insertQuery3)) {
 
                        ps1.setInt(1, newUserId);
                        ps1.setString(2, username);
                        ps1.setString(3, email);
                        ps1.setString(4, password);
                        ps1.setString(5, role);
                        isSuccess = ps1.executeUpdate() > 0;
 
                        ps2.setInt(1, newUserId);
                        ps2.setString(2, username);
                        ps2.setString(3, email);
                        isSuccess &= ps2.executeUpdate() > 0;
 
                        ps3.setInt(1, newUserId);
                        ps3.setString(2, department);
                        ps3.setString(3, designation);
                        isSuccess &= ps3.executeUpdate() > 0;
                    }
                } else if ("staff".equals(role)) {
                    try (PreparedStatement ps1 = connection.prepareStatement(insertQuery1);
                         PreparedStatement ps3 = connection.prepareStatement(insertQuery3)) {
 
                        ps1.setInt(1, newUserId);
                        ps1.setString(2, username);
                        ps1.setString(3, email);
                        ps1.setString(4, password);
                        ps1.setString(5, role);
                        isSuccess = ps1.executeUpdate() > 0;
 
                        ps3.setInt(1, newUserId);
                        ps3.setString(2, department);
                        ps3.setString(3, designation);
                        isSuccess &= ps3.executeUpdate() > 0;
                    }
                }
 
                if (isSuccess) {
                    connection.commit();  // Commit transaction
                    logger.info("Registration successful for role: " + role);
                    sendResponse(out, "Registration Successful...", true);
                } else {
                    connection.rollback();  // Rollback transaction
                    logger.warning("Registration failed for role: " + role);
                    sendResponse(out, "Registration Failed..", false);
                }
            }
        } catch (Exception e) {
            logger.severe("Error during registration: " + e.getMessage());
            e.printStackTrace();
            sendResponse(out, "Error during registration. Please try again later.", false);
        } finally {
            // Close connection in the finally block to ensure it's closed even if an exception occurs
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) {
                    logger.severe("Error closing connection: " + ex.getMessage());
                }
            }
            if (out != null) {
                out.close();
            }
        }
    }
 
    private void sendResponse(PrintWriter out, String message, boolean success) {
        out.println("<html><head>");
        out.println("<script type='text/javascript'>");
        out.println("alert('" + message + "');");
 
        // Conditional redirect based on success or failure
        if (success) {
            out.println("window.location.href='login.jsp';"); // Redirect to login.jsp on success
        } else {
            out.println("window.location.href='userregistration.jsp';"); // Redirect to userregistration.jsp on failure
        }
 
        out.println("</script>");
        out.println("</head><body>");
        out.println("</body></html>");
    }
}
