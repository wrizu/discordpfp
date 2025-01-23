import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import javax.imageio.ImageIO;

public class DiscordAppGUI {
    private JFrame frame;
    private JTextField accessTokenField;
    private JLabel imageLabel;
    private File selectedFile;

    public DiscordAppGUI() {
        // Initialize the frame
        frame = new JFrame("Discord Avatar Updater");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // Create and set up components
        JPanel inputPanel = new JPanel(new GridLayout(3, 1));
        JLabel tokenLabel = new JLabel("Access Token:");
        accessTokenField = new JTextField();
        JButton selectImageButton = new JButton("Select Image");
        imageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectImage();
            }
        });

        inputPanel.add(tokenLabel);
        inputPanel.add(accessTokenField);
        inputPanel.add(selectImageButton);

        JButton uploadButton = new JButton("Upload Avatar");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadAvatar();
            }
        });

        // Add components to the frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(imageLabel, BorderLayout.CENTER);
        frame.add(uploadButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(frame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            try {
                // Load the image file and display it
                BufferedImage image = ImageIO.read(selectedFile);
                Image scaledImage = image.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
                imageLabel.setText(""); // Clear the text
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error loading the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void uploadAvatar() {
        if (accessTokenField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Access token is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(frame, "Please select an image!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Resize the image to 128x128
            BufferedImage resizedImage = resizeImage(selectedFile, 128, 128);

            // Convert the resized image to a Base64 string
            String base64Image = "data:image/png;base64," + encodeImageToBase64(resizedImage);

            // Send the avatar update request
            String accessToken = accessTokenField.getText();
            boolean success = sendUpdateRequest(accessToken, base64Image);

            if (success) {
                JOptionPane.showMessageDialog(frame, "Avatar updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to update avatar. Check your access token and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error processing the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BufferedImage resizeImage(File file, int width, int height) throws IOException {
        BufferedImage originalImage = ImageIO.read(file);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    private String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private boolean sendUpdateRequest(String accessToken, String base64Image) {
        try {
            // Create the JSON payload
            String jsonPayload = "{\"avatar\":\"" + base64Image + "\"}";

            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://discord.com/api/v9/users/@me"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send the request
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check the response code
            if (response.statusCode() == 200) {
                return true;
            } else {
                System.out.println("Error: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        // Run the GUI application
        SwingUtilities.invokeLater(DiscordAppGUI::new);
    }
}
