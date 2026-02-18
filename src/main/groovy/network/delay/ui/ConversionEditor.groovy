package network.delay.ui

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@CompileStatic
class ConversionEditor extends VBox {

    enum ToolMode { BASE64, AES, HASH, JSON }

    private final TextArea topArea = new TextArea()
    private final TextArea bottomArea = new TextArea()
    private final TextField keyField = new TextField()
    private final Label statusLabel = new Label("Ready")
    private final ToolMode currentMode

    ConversionEditor(ToolMode mode, String title) {
        this.currentMode = mode
        setSpacing(10d)
        setPadding(new Insets(15d))

        def header = new Label(title).with {
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            it
        }

        VBox.setVgrow(topArea, Priority.ALWAYS)
        VBox.setVgrow(bottomArea, Priority.ALWAYS)
        topArea.promptText = "Input 1..."
        bottomArea.promptText = "Input 2 / Output..."

        def toolbar = new HBox(15)
        toolbar.alignment = Pos.CENTER

        def downBtn = new Button("Process ↓ (Encode/Encrypt)")
        def upBtn = new Button("Process ↑ (Decode/Decrypt)")
        def clearBtn = new Button("Clear All")

        if (currentMode == ToolMode.HASH) {
            upBtn.disable = true
            downBtn.text = "Generate Hash ↓"
        }

        toolbar.children.addAll(downBtn, upBtn, clearBtn)

        children.addAll(header, new Label("Top Editor:"), topArea)

        if (currentMode == ToolMode.AES) {
            keyField.promptText = "Enter 16-character AES key..."
            children.addAll(new Label("Security Key:"), keyField)
        }

        children.addAll(toolbar, new Label("Bottom Editor:"), bottomArea, statusLabel)

        downBtn.setOnAction { runConversion(true) }
        upBtn.setOnAction { runConversion(false) }
        clearBtn.setOnAction {
            topArea.clear(); bottomArea.clear(); keyField.clear()
            statusLabel.text = "Ready"
        }
    }

    private void runConversion(boolean topToBottom) {
        String input = topToBottom ? topArea.text : bottomArea.text
        if (!input) return

        try {
            String result = ""
            switch (currentMode) {
                case ToolMode.BASE64:
                    result = topToBottom ? encodeB64(input) : decodeB64(input)
                    break
                case ToolMode.AES:
                    result = topToBottom ? encryptAES(input) : decryptAES(input)
                    break
                case ToolMode.HASH:
                    result = handleHash(input)
                    break
                case ToolMode.JSON:
                    result = formatJson(input)
                    break
            }

            if (topToBottom) bottomArea.text = result else topArea.text = result
            statusLabel.text = "Success"
        } catch (Exception e) {
            statusLabel.text = "Error: ${e.message}"
        }
    }

    private String encodeB64(String s) { Base64.encoder.encodeToString(s.getBytes(StandardCharsets.UTF_8)) }
    private String decodeB64(String s) { new String(Base64.decoder.decode(s), StandardCharsets.UTF_8) }

    private String encryptAES(String s) {
        processAES(s, Cipher.ENCRYPT_MODE)
    }
    private String decryptAES(String s) {
        byte[] decoded = Base64.getDecoder().decode(s)
        processAESBytes(decoded, Cipher.DECRYPT_MODE)
    }

    private String processAES(String s, int mode) {
        processAESBytes(s.getBytes(StandardCharsets.UTF_8), mode)
    }

    private String processAESBytes(byte[] data, int mode) {
        String key = keyField.text
        if (key.length() != 16) throw new Exception("Key must be 16 characters")

        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES")
        Cipher cipher = Cipher.getInstance("AES")
        cipher.init(mode, secretKey)
        byte[] result = cipher.doFinal(data)

        return mode == Cipher.ENCRYPT_MODE ? Base64.encoder.encodeToString(result) : new String(result, StandardCharsets.UTF_8)
    }

    private String formatJson(String input) {
        input.replace("{", "{\n  ").replace(",", ",\n  ").replace("}", "\n}")
    }

    private String handleHash(String input) {
        def digest = java.security.MessageDigest.getInstance("SHA-256")
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8))
        StringBuilder hexString = new StringBuilder()
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b)
            if (hex.length() == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }
}