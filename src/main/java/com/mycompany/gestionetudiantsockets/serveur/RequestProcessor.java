package com.mycompany.gestionetudiantsockets.serveur;

public class RequestProcessor {
    private DatabaseManager dbManager;
    
    public RequestProcessor() {
        this.dbManager = new DatabaseManager();
    }
    
    public String processRequest(String requete) {
        Object[] result = dbManager.executeQuery(requete);
        String type = (String) result[0];
        Object data = result[1];
        
        StringBuilder response = new StringBuilder();
        response.append(type).append("|");
        
        if (type.equals("single")) {
            response.append(data);
        } else if (type.equals("table")) {
            Object[] tableData = (Object[]) data;
            String[] columnNames = (String[]) tableData[0];
            String[][] rows = (String[][]) tableData[1];
            
            response.append(columnNames.length).append("|");
            for (String col : columnNames) {
                response.append(col).append(";");
            }
            response.append("|");
            
            for (String[] row : rows) {
                for (String value : row) {
                    response.append(value).append(";");
                }
                response.append("|");
            }
        } else {
            response.append(data);
        }
        
        return response.toString();
    }
}