import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.*;

public class DiscordAppGUI {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JTextField discordTokenField, twitterTokenField, instagramTokenField;
    private JLabel discordImageLabel, twitterImageLabel, instagramImageLabel;
    private File discordSelectedFile, twitterSelectedFile, instagramSelectedFile;

    public DiscordAppGUI() {
        frame = new JFrame("Profile Updater");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Discord", createDiscordTab());
        tabbedPane.addTab("Twitter", createTwitterTab());
        tabbedPane.addTab("Instagram", createInstagramTab());

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    // Create the Discord Tab
    private JPanel createDiscordTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(3, 1));

        JLabel tokenLabel = new JLabel("Discord Access Token:");
        discordTokenField = new JTextField();
        JButton selectImageButton = new JButton("Select Image");
        discordImageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        discordImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        selectImageButton.addActionListener(e -> selectImage("Discord"));

        inputPanel.add(tokenLabel);
        inputPanel.add(discordTokenField);
        inputPanel.add(selectImageButton);

        JButton uploadButton = new JButton("Update Discord Avatar");
        uploadButton.addActionListener(e -> uploadDiscordAvatar());

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(discordImageLabel, BorderLayout.CENTER);
        panel.add(uploadButton, BorderLayout.SOUTH);

        return panel;
    }

    // Create the Twitter Tab (Placeholder for now)
    private JPanel createTwitterTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(3, 1));

        JLabel tokenLabel = new JLabel("Twitter Access Token:");
        twitterTokenField = new JTextField();
        JButton selectImageButton = new JButton("Select Image");
        twitterImageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        twitterImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        selectImageButton.addActionListener(e -> selectImage("Twitter"));

        inputPanel.add(tokenLabel);
        inputPanel.add(twitterTokenField);
        inputPanel.add(selectImageButton);

        JButton uploadButton = new JButton("Update Twitter Profile");
        uploadButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Feature coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(twitterImageLabel, BorderLayout.CENTER);
        panel.add(uploadButton, BorderLayout.SOUTH);

        return panel;
    }

    // Create the Instagram Tab (Placeholder for now)
    private JPanel createInstagramTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(3, 1));

        JLabel tokenLabel = new JLabel("Instagram Access Token:");
        instagramTokenField = new JTextField();
        JButton selectImageButton = new JButton("Select Image");
        instagramImageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        instagramImageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        selectImageButton.addActionListener(e -> selectImage("Instagram"));

        inputPanel.add(tokenLabel);
        inputPanel.add(instagramTokenField);
        inputPanel.add(selectImageButton);

        JButton uploadButton = new JButton("Update Instagram Profile");
        uploadButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Feature coming soon!", "Info", JOptionPane.INFORMATION_MESSAGE));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(instagramImageLabel, BorderLayout.CENTER);
        panel.add(uploadButton, BorderLayout.SOUTH);

        return panel;
    }

    // Select Image based on the tab
    private void selectImage(String platform) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnValue = fileChooser.showOpenDialog(frame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);

                switch (platform) {
                    case "Discord":
                        discordSelectedFile = selectedFile;
                        discordImageLabel.setIcon(new ImageIcon(scaledImage));
                        discordImageLabel.setText("");
                        break;
                    case "Twitter":
                        twitterSelectedFile = selectedFile;
                        twitterImageLabel.setIcon(new ImageIcon(scaledImage));
                        twitterImageLabel.setText("");
                        break;
                    case "Instagram":
                        instagramSelectedFile = selectedFile;
                        instagramImageLabel.setIcon(new ImageIcon(scaledImage));
                        instagramImageLabel.setText("");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error loading the image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Upload Discord Avatar
    private void uploadDiscordAvatar() {
        if (discordTokenField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Discord access token is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (discordSelectedFile == null) {
            JOptionPane.showMessageDialog(frame, "Please select an image for Discord!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BufferedImage resizedImage = resizeImage(discordSelectedFile, 128, 128);
            String base64Image = "data:image/png;base64," + encodeImageToBase64(resizedImage);

            String accessToken = discordTokenField.getText();
            boolean success = sendUpdateRequest(accessToken, base64Image);

            if (success) {
                JOptionPane.showMessageDialog(frame, "Discord avatar updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to update Discord avatar. Check your access token.", "Error", JOptionPane.ERROR_MESSAGE);
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
            String jsonPayload = "{\"avatar\":\"" + base64Image + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://discord.com/api/v9/users/@me"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
        SwingUtilities.invokeLater(DiscordAppGUI::new);
    }
}
