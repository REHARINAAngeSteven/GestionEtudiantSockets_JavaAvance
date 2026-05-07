package com.mycompany.gestionetudiantsockets.client;

import java.io.*;
import java.net.*;

public class ClientSocketHandler {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected = false;
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("Connecté au serveur");
            return true;
        } catch (IOException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    public String sendRequest(String request) {
        if (!connected) {
            return "error|Échec de connexion";
        }
        
        try {
            writer.println(request);
            String response = reader.readLine();
            return response != null ? response : "error|Échec de connexion";
        } catch (IOException e) {
            System.err.println("Erreur d'envoi: " + e.getMessage());
            connected = false;
            return "error|Échec de connexion";
        }
    }
    
    public void disconnect() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
            connected = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}