package com.mycompany.gestionetudiantsockets.serveur;

import java.io.*;
import java.net.*;

public class ServeurThread extends Thread {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private RequestProcessor processor;
    
    public ServeurThread(Socket socket) {
        this.clientSocket = socket;
        this.processor = new RequestProcessor();
    }
    
    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            
            String requete;
            while ((requete = reader.readLine()) != null) {
                System.out.println("  → Requête reçue: " + requete);
                String reponse = processor.processRequest(requete);
                writer.println(reponse);
                System.out.println("  ← Réponse envoyée");
            }
        } catch (IOException e) {
            System.err.println("  ✗ Erreur client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("  Client déconnecté\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}