package org.example;

import java.awt.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ClipboardMonitor {
    private static String lastClipboard = "";
    private static final String GEMINI_API_KEY = "YOUR_API_KEY_HERE"; // Your key here
    private static final String MODEL_NAME = "gemini-1.5-flash";

    public static void main(String[] args) {
        System.out.println("Clipboard monitor started. Copy text to get AI responses.");
        new Timer().schedule(new ClipboardCheckTask(), 0, 1000);
    }

    private static class ClipboardCheckTask extends TimerTask {
        @Override
        public void run() {
            try {
                String content = getClipboardContent();
                if (!content.isEmpty() && !content.equals(lastClipboard)) {
                    lastClipboard = content;

                    System.out.println("\nCopied Text: " + content);
                    content = "give me Answer only \n"+content;
                    String response = getGeminiResponse(content);
                    showNotification(response, 10000);
                    System.out.println("AI Response: " + response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getClipboardContent() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);

            if (contents == null) return "";

            // Check for text first
            if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
            // Check for image
            else if (contents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                BufferedImage image = (BufferedImage) contents.getTransferData(DataFlavor.imageFlavor);
                return performOCR(image);  // Your OCR function
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String performOCR(BufferedImage image) {
        try {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("D:/OCR/tessdata"); // Set your tessdata path
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            e.printStackTrace();
            return "";
        }
    }


    private static String getGeminiResponse(String prompt) {
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");

            JSONObject requestBody = new JSONObject();

            // Content structure
            JSONArray contents = new JSONArray();
            JSONObject contentItem = new JSONObject();
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put("text", prompt));
            contentItem.put("parts", parts);
            contents.put(contentItem);
            requestBody.put("contents", contents);

            // Generation configuration
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("maxOutputTokens", 500);
            generationConfig.put("temperature", 0.9);
            generationConfig.put("topP", 0.95);
            requestBody.put("generationConfig", generationConfig);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1/models/" + MODEL_NAME + ":generateContent?key=" + GEMINI_API_KEY)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "API Error: " + response.code() + " - " + response.body().string();
                }

                JSONObject responseJson = new JSONObject(response.body().string());
                JSONArray candidates = responseJson.optJSONArray("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    JSONObject candidate = candidates.getJSONObject(0);
                    JSONObject content = candidate.getJSONObject("content");
                    JSONArray responseParts = content.getJSONArray("parts");

                    if (!responseParts.isEmpty()) {
                        return responseParts.getJSONObject(0).optString("text", "No text in response");
                    }
                }
                return "No valid response from AI";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private static void showNotification(String message, int durationMs) {
        SwingUtilities.invokeLater(() -> {
            if (!SystemTray.isSupported()) {
                JOptionPane.showMessageDialog(null, "System tray not supported!");
                return;
            }

            try {
                SystemTray tray = SystemTray.getSystemTray();

                // Create a 1x1 transparent placeholder image
                Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

                // Configure the invisible tray icon
                TrayIcon trayIcon = new TrayIcon(image, "");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);

                // Display the notification
                trayIcon.displayMessage("", message, TrayIcon.MessageType.NONE);


            } catch (AWTException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        });
    }
}