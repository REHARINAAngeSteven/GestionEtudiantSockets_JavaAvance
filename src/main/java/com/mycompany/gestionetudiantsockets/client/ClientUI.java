package com.mycompany.gestionetudiantsockets.client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.border.*;

public class ClientUI extends JFrame {
    private JTextArea textAreaRequete;
    private JButton btnEnvoyer;
    private JTable tableResultats;
    private JTextArea textAreaResultat;
    private ClientSocketHandler socketHandler;
    private JLabel statusLabel;
    private JPanel resultPanel;
    private CardLayout cardLayout;
    
    public ClientUI() {
        initUI();
        initConnection();
    }
    
    private void initUI() {
        setTitle("Application CRUD Étudiant - Client");
        setSize(950, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panneau requête
        JPanel queryPanel = createQueryPanel();
        
        // Panneau résultats avec CardLayout
        resultPanel = new JPanel();
        cardLayout = new CardLayout();
        resultPanel.setLayout(cardLayout);
        resultPanel.setBorder(BorderFactory.createTitledBorder("Résultats"));
        
        // Table pour les résultats
        tableResultats = new JTable();
        tableResultats.setFont(new Font("Arial", Font.PLAIN, 12));
        tableResultats.setRowHeight(25);
        JScrollPane scrollTable = new JScrollPane(tableResultats);
        
        // Zone texte pour résultats simples
        textAreaResultat = new JTextArea(10, 60);
        textAreaResultat.setEditable(false);
        textAreaResultat.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollText = new JScrollPane(textAreaResultat);
        
        resultPanel.add(scrollTable, "table");
        resultPanel.add(scrollText, "text");
        
        // Barre de statut
        statusLabel = new JLabel(" Statut: Non connecté");
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        mainPanel.add(queryPanel, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 120, 215)), 
            "Requête SQL",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12)
        ));
        
        textAreaRequete = new JTextArea(6, 60);
        textAreaRequete.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textAreaRequete.setText("SELECT * FROM ETUDIANT");
        JScrollPane scrollQuery = new JScrollPane(textAreaRequete);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEnvoyer = new JButton("🚀 Envoyer la requête");
        btnEnvoyer.setFont(new Font("Arial", Font.BOLD, 14));
        btnEnvoyer.setBackground(new Color(0, 120, 215));
        btnEnvoyer.setForeground(Color.WHITE);
        btnEnvoyer.setFocusPainted(false);
        btnEnvoyer.addActionListener(e -> envoyerRequete());
        
        buttonPanel.add(btnEnvoyer);
        
        panel.add(scrollQuery, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void initConnection() {
        socketHandler = new ClientSocketHandler();
        if (socketHandler.connect("localhost", 9999)) {
            statusLabel.setText("✅Statut: Connecté au serveur");
            statusLabel.setForeground(new Color(0, 150, 0));
        } else {
            statusLabel.setText("Statut: Échec de connexion au serveur");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Impossible de se connecter au serveur.\n\n" +
                "Vérifiez que:\n" +
                "1. Le serveur est démarré (ServeurApp.java)\n" +
                "2. Le serveur écoute sur le port 9999\n" +
                "3. Aucun pare-feu ne bloque la connexion", 
                "Erreur de connexion", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void envoyerRequete() {
        String requete = textAreaRequete.getText().trim();
        
        if (requete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une requête SQL");
            return;
        }
        
        // Effacer les résultats précédents
        DefaultTableModel model = (DefaultTableModel) tableResultats.getModel();
        model.setRowCount(0);
        textAreaResultat.setText("");
        
        // Désactiver le bouton
        btnEnvoyer.setEnabled(false);
        btnEnvoyer.setText("Envoi en cours...");
        
        // Envoyer la requête
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return socketHandler.sendRequest(requete);
            }
            
            @Override
            protected void done() {
                try {
                    String reponse = get();
                    if (reponse == null) {
                        textAreaResultat.setText("Échec de connexion");
                        statusLabel.setText("Statut: Échec de connexion");
                        statusLabel.setForeground(Color.RED);
                    } else {
                        traiterReponse(reponse);
                    }
                } catch (Exception e) {
                    textAreaResultat.setText("Erreur: " + e.getMessage());
                } finally {
                    btnEnvoyer.setEnabled(true);
                    btnEnvoyer.setText("🚀 Envoyer la requête");
                }
            }
        };
        worker.execute();
    }
    
    private void traiterReponse(String reponse) {
        String[] parts = reponse.split("\\|", 2);
        String type = parts[0];
        String data = parts.length > 1 ? parts[1] : "";
        
        switch (type) {
            case "table":
                afficherTableau(data);
                cardLayout.show(resultPanel, "table");
                break;
            case "single":
                textAreaResultat.setText("Résultat de la requête:\n\n" + data);
                cardLayout.show(resultPanel, "text");
                break;
            case "success":
                textAreaResultat.setText("✅ " + data);
                cardLayout.show(resultPanel, "text");
                statusLabel.setText("✅ " + data);
                break;
            case "empty":
                textAreaResultat.setText("Aucun résultat trouvé");
                cardLayout.show(resultPanel, "text");
                break;
            case "error":
                textAreaResultat.setText("❌ " + data);
                cardLayout.show(resultPanel, "text");
                break;
            default:
                textAreaResultat.setText(reponse);
                cardLayout.show(resultPanel, "text");
                break;
        }
    }
    
    private void afficherTableau(String data) {
        String[] parts = data.split("\\|");
        
        if (parts.length < 2) {
            textAreaResultat.setText("Format de données invalide");
            return;
        }
        
        try {
            int columnCount = Integer.parseInt(parts[0]);
            String[] columnNames = parts[1].split(";");
            
            if (columnNames.length > columnCount) {
                String[] temp = new String[columnCount];
                System.arraycopy(columnNames, 0, temp, 0, columnCount);
                columnNames = temp;
            }
            
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            for (int i = 2; i < parts.length - 1; i++) {
                String[] rowData = parts[i].split(";");
                if (rowData.length == columnCount) {
                    model.addRow(rowData);
                }
            }
            
            tableResultats.setModel(model);
            textAreaResultat.setText("✅ " + model.getRowCount() + " ligne(s) chargée(s)");
            statusLabel.setText("✅ Affichage de " + model.getRowCount() + " enregistrements");
            
            // Ajuster les colonnes
            for (int i = 0; i < tableResultats.getColumnCount(); i++) {
                tableResultats.getColumnModel().getColumn(i).setPreferredWidth(150);
            }
            
        } catch (NumberFormatException e) {
            textAreaResultat.setText("Erreur de format des données");
        }
    }
}