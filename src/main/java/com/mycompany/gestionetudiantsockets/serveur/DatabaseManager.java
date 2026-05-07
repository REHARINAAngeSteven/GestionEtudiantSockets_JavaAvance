package com.mycompany.gestionetudiantsockets.serveur;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    // ===== CONFIGURATION - MODIFIEZ ICI =====
    private static final String DB_TYPE = "mysql"; // "mysql" ou "postgresql"
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = DB_TYPE.equals("mysql") ? "3306" : "5432";
    private static final String DB_NAME = "etudiant_db";
    private static final String DB_USER = "root";  // Votre utilisateur
    private static final String DB_PASSWORD = "";   // Votre mot de passe
    // =======================================
    
    private Connection connection;
    
    public DatabaseManager() {
        connect();
    }
    
    private void connect() {
        try {
            String url;
            if (DB_TYPE.equals("mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + 
                      "?useSSL=false&serverTimezone=UTC";
                connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
                System.out.println("✓ Connecté à MySQL");
            } else {
                Class.forName("org.postgresql.Driver");
                url = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
                connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
                System.out.println("✓ Connecté à PostgreSQL");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC non trouvé!");
            System.err.println("   Ajoutez le fichier JAR dans Libraries");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base: " + e.getMessage());
            System.err.println("   Vérifiez que MySQL/PostgreSQL est démarré");
        }
    }
    
    public Object[] executeQuery(String sql) {
        String sqlLower = sql.trim().toLowerCase();
        Object[] result = new Object[2];
        
        try {
            if (sqlLower.startsWith("select")) {
                return executeSelect(sql);
            } else if (sqlLower.startsWith("insert") || 
                       sqlLower.startsWith("update") || 
                       sqlLower.startsWith("delete")) {
                return executeUpdate(sql);
            } else {
                result[0] = "error";
                result[1] = "Type de requête non supportée";
            }
        } catch (SQLException e) {
            result[0] = "error";
            result[1] = "Erreur SQL: " + e.getMessage();
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return result;
    }
    
    private Object[] executeSelect(String sql) throws SQLException {
        Object[] result = new Object[2];
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        List<String[]> dataList = new ArrayList<>();
        int rowCount = 0;
        
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getString(i + 1);
                if (row[i] == null) row[i] = "NULL";
            }
            dataList.add(row);
            rowCount++;
        }
        
        if (rowCount == 0) {
            result[0] = "empty";
            result[1] = "Aucun résultat trouvé";
        } else if (rowCount == 1 && columnCount == 1) {
            result[0] = "single";
            result[1] = dataList.get(0)[0];
        } else {
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i + 1);
            }
            result[0] = "table";
            result[1] = new Object[]{columnNames, dataList.toArray(new String[0][])};
        }
        
        rs.close();
        stmt.close();
        return result;
    }
    
    private Object[] executeUpdate(String sql) throws SQLException {
        Object[] result = new Object[2];
        Statement stmt = connection.createStatement();
        int affectedRows = stmt.executeUpdate(sql);
        
        if (affectedRows > 0) {
            if (sql.trim().toLowerCase().startsWith("insert")) {
                result[0] = "success";
                result[1] = "Données reçues - Enregistrement ajouté";
            } else if (sql.trim().toLowerCase().startsWith("update")) {
                result[0] = "success";
                result[1] = "Données reçues - Enregistrement modifié";
            } else {
                result[0] = "success";
                result[1] = "Données reçues - Enregistrement supprimé";
            }
        } else {
            result[0] = "error";
            result[1] = "Aucune ligne affectée";
        }
        
        stmt.close();
        return result;
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Déconnexion de la base");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}