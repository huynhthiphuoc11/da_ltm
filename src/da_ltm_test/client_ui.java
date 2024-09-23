package da_ltm_test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

public class client_ui extends JFrame {
    private JTextField ipField;
    private JTextField portField;
    private JLabel statusLabel;
    private String username;
    private Socket socket;
    private PrintWriter out;
    private JTree fileTree;
    private JTable fileTable;
    private String selectedFilePath;
    private JTextArea uploadStatusArea; // Area for displaying upload status
    private JProgressBar uploadProgressBar; // Progress bar for file uploads

    public client_ui(String username) {
        this.username = username;
        setTitle("File Transfer Protocol - Client");

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel - File tree
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("/");
        fileTree = new JTree(root);
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        leftPanel.add(treeScrollPane);

        // Right panel - File table and buttons
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Top panel with IP address, port, and connect button
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel usernameLabel = new JLabel("Username: " + username);
        JLabel ipLabel = new JLabel("Host IP:");
        ipField = new JTextField("192.168.100.190", 10);
        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField("3333", 5);
        JButton connectButton = new JButton("Connect");
        JButton chatButton = new JButton("Chat"); // Chat button

        topPanel.add(usernameLabel);
        topPanel.add(ipLabel);
        topPanel.add(ipField);
        topPanel.add(portLabel);
        topPanel.add(portField);
        topPanel.add(connectButton);
        topPanel.add(chatButton); // Add chat button to the panel

        rightPanel.add(topPanel, BorderLayout.NORTH);

        // File/folder table
        String[] columnNames = {"Name", "Edit", "Delete"};
        Object[][] data = {};
        fileTable = new JTable(new DefaultTableModel(data, columnNames));
        JScrollPane tableScrollPane = new JScrollPane(fileTable);
        rightPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Buttons for New Folder, New File, Upload
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JButton newFolderButton = new JButton("New Folder");
        JButton newFileButton = new JButton("New File");
        JButton uploadButton = new JButton("Upload");

        buttonPanel.add(newFolderButton);
        buttonPanel.add(newFileButton);
        buttonPanel.add(uploadButton);
        rightPanel.add(buttonPanel, BorderLayout.EAST);

        // Upload status area and progress bar
        JPanel uploadPanel = new JPanel();
        uploadPanel.setLayout(new BoxLayout(uploadPanel, BoxLayout.Y_AXIS));
        uploadStatusArea = new JTextArea(5, 20);
        uploadStatusArea.setEditable(false);
        uploadProgressBar = new JProgressBar(0, 100);
        uploadProgressBar.setStringPainted(true);
        uploadPanel.add(new JScrollPane(uploadStatusArea));
        uploadPanel.add(uploadProgressBar);
        rightPanel.add(uploadPanel, BorderLayout.SOUTH);

        // Bottom panel - Status
        JPanel bottomPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Status: Disconnected");
        bottomPanel.add(statusLabel);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Action listeners for buttons
        connectButton.addActionListener(e -> connectToServer());
        newFolderButton.addActionListener(e -> createNewFolder());
        newFileButton.addActionListener(e -> createNewFile());
        uploadButton.addActionListener(e -> uploadFile());
//        chatButton.addActionListener(e -> openChatWindow()); // Chat button action listener

        // Mouse listeners for the file tree and file table
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTree tree = (JTree) e.getSource();
                if (e.getClickCount() == 1) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        updateFileDetails(selectedNode);
                    }
                }
            }
        });

        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fileTable.rowAtPoint(e.getPoint());
                int column = fileTable.columnAtPoint(e.getPoint());

                if (row >= 0 && selectedFilePath != null) {
                    if (column == 1) {
                        editSelectedFile(selectedFilePath);
                    } else if (column == 2) {
                        deleteSelectedFile(selectedFilePath);
                    }
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void connectToServer() {
        String ipAddress = ipField.getText();
        String portNumber = portField.getText();

        try {
            int port = Integer.parseInt(portNumber);
            socket = new Socket(ipAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            statusLabel.setText("Status: Connected to " + ipAddress + ":" + port);
            sendMessage("Connected: " + username);
            loadDriveContents("D:\\");
        } catch (IOException e) {
            statusLabel.setText("Status: Failed to Connect");
            e.printStackTrace();
        }
    }

    private void loadDriveContents(String path) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(path);
        File directory = new File(path);

        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file.getName());
                rootNode.add(fileNode);
            }
        }

        fileTree.setModel(new javax.swing.tree.DefaultTreeModel(rootNode));
    }

    private void updateFileDetails(DefaultMutableTreeNode selectedNode) {
        selectedFilePath = "D:\\" + selectedNode.toString();
        Object[][] data = {
            {selectedNode.toString(), "Edit", "Delete"}
        };
        fileTable.setModel(new DefaultTableModel(data, new String[]{"Name", "Edit", "Delete"}));
    }

    private void createNewFolder() {
        String folderName = JOptionPane.showInputDialog("Enter folder name:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            File folder = new File("D:\\" + folderName);
            if (folder.mkdir()) {
                sendMessage("Created folder: " + folderName);
                loadDriveContents("D:\\");
            } else {
                statusLabel.setText("Status: Failed to create folder.");
            }
        }
    }

    private void createNewFile() {
        String fileName = JOptionPane.showInputDialog("Enter file name:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            File file = new File("D:\\" + fileName);
            try {
                if (file.createNewFile()) {
                    sendMessage("Created file: " + fileName);
                    loadDriveContents("D:\\");
                }
            } catch (IOException e) {
                statusLabel.setText("Status: Failed to create file.");
                e.printStackTrace();
            }
        }
    }

    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            sendFile(selectedFile);
        }
    }

    private void sendFile(File file) {
        try (Socket fileSocket = new Socket(ipField.getText(), Integer.parseInt(portField.getText()));
             FileInputStream fis = new FileInputStream(file);
             PrintWriter out = new PrintWriter(fileSocket.getOutputStream(), true);
             DataOutputStream dataOutputStream = new DataOutputStream(fileSocket.getOutputStream())) {

            // Send file name to the server
            out.println("UPLOAD " + file.getName()); // Gửi thông điệp upload với tên file

            // Send file data
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            dataOutputStream.flush();

            statusLabel.setText("Status: Uploaded file: " + file.getName());
        } catch (IOException e) {
            statusLabel.setText("Status: Failed to upload file.");
            e.printStackTrace();
        }
    }



    private void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void editSelectedFile(String filePath) {
        // Implement file editing functionality
        JOptionPane.showMessageDialog(this, "Editing: " + filePath);
    }

    private void deleteSelectedFile(String filePath) {
        int response = JOptionPane.showConfirmDialog(this, "Delete " + filePath + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            File file = new File(filePath);
            if (file.delete()) {
                sendMessage("Deleted: " + filePath);
                loadDriveContents("D:\\");
            } else {
                statusLabel.setText("Status: Failed to delete file.");
            }
        }
    }

 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new client_ui("User"));
    }
}
