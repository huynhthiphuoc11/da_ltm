package da_ltm_test;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientIP;
    private server_ui ui;
    

    public ClientHandler(Socket clientSocket, String clientIP, server_ui ui) {
        this.clientSocket = clientSocket;
        this.clientIP = clientIP;
        this.ui = ui;
    }

    // Other methods remain the same...

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
             DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream())) {

            // Receive username from client
            String username = reader.readLine();
            ui.appendMessage("Client connected: " + clientIP + " with username: " + username);
            ui.appendMessage("Login successful for user: " + username);

            // Handle messages from client
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("UPLOAD")) {
                    String fileName = message.split(" ")[1]; // Lấy tên file từ thông điệp
                    System.out.println("running reveive");
                    receiveFile(fileName, dataInputStream); // Nhận file từ client
                } else {
                    ui.appendMessage("Message from " + username + ": " + message);
                    writer.println("Server received: " + message);  // Respond back to client
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            ui.appendMessage("Error handling client " + clientIP + ": " + ex.getMessage());
        } finally {
            try {
                clientSocket.close();
                ui.appendMessage("Client " + clientIP + " disconnected.");
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void receiveFile(String fileName, DataInputStream dataInputStream) {
        try {
            File file = new File("D:\\" + fileName); // Đường dẫn lưu file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = dataInputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                ui.appendMessage("File " + fileName + " received and saved to D:\\");
                System.out.println("File created: " + fileName);
            }
        } catch (IOException e) {
            ui.appendMessage("Error receiving file: " + e.getMessage());
        }
    }}

