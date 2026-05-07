package com.mycompany.gestionetudiantsockets.serveur;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class ServeurApp {
    private static final int PORT = 9999;
    
    public static void main(String[] args) {
        // Interface graphique pour le serveur
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Serveur CRUD Étudiant");
            frame.setSize(500, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            
            JTextArea logArea = new JTextArea();
            logArea.setEditable(false);
            logArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            JScrollPane scroll = new JScrollPane(logArea);
            frame.add(scroll);
            
            // Rediriger System.out vers la zone de texte
            PrintStream printStream = new PrintStream(new TextAreaOutputStream(logArea));
            System.setOut(printStream);
            System.setErr(printStream);
            
            frame.setVisible(true);
        });
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("╔════════════════════════════════════╗");
            System.out.println("║     SERVEUR CRUD ÉTUDIANT          ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║ Port: " + PORT + "                       ║");
            System.out.println("║ Base: MySQL/PostgreSQL            ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.println("\n✅ Serveur démarré avec succès!");
            System.out.println("⏳ En attente des clients...\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔌 Nouveau client connecté: " + 
                    clientSocket.getInetAddress().getHostAddress());
                
                ServeurThread thread = new ServeurThread(clientSocket);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Erreur du serveur: " + e.getMessage());
        }
    }
    
    // Classe utilitaire pour rediriger System.out vers JTextArea
    static class TextAreaOutputStream extends OutputStream {
        private JTextArea textArea;
        
        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
        
        @Override
        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char)b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}