package network.delay.ui.devtools

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import java.nio.charset.StandardCharsets

@CompileStatic
class JwtTool extends VBox {
    private final TextArea tokenInput = new TextArea()
    private final TextArea headerOutput = new TextArea()
    private final TextArea payloadOutput = new TextArea()
    private final Label statusLabel = new Label("Ready")

    JwtTool() {
        setSpacing(10d)
        setPadding(new Insets(15d))

        def header = new Label("Universal JWT Decoder").with {
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            it
        }

        tokenInput.promptText = "Paste JWT (header.payload.sig)..."
        tokenInput.prefHeight = 100

        headerOutput.editable = false; headerOutput.prefHeight = 150
        payloadOutput.editable = false
        VBox.setVgrow(payloadOutput, Priority.ALWAYS)

        // Styling
        [headerOutput, payloadOutput].each { it.style = "-fx-font-family: 'Monospaced'; -fx-control-inner-background: #fafafa;" }

        def decodeBtn = new Button("Decode JWT Components")
        decodeBtn.setOnAction { decode() }

        children.addAll(
                header,
                new Label("Full Token:"), tokenInput,
                decodeBtn,
                new Label("Header (Algorithm & Type):"), headerOutput,
                new Label("Payload (Data):"), payloadOutput,
                statusLabel
        )
    }

    private void decode() {
        try {
            String raw = tokenInput.text.trim()
            String[] parts = raw.split("\\.")

            if (parts.length < 2) throw new Exception("JWT must have at least Header and Payload")

            headerOutput.text = decodePart(parts[0])
            payloadOutput.text = decodePart(parts[1])

            statusLabel.text = "Success: Parsed ${parts.length} parts"
            statusLabel.setStyle("-fx-text-fill: green;")
        } catch (Exception e) {
            statusLabel.text = "Error: ${e.message}"
            statusLabel.setStyle("-fx-text-fill: red;")
        }
    }

    private String decodePart(String part) {
        // Use URL Decoder specifically for JWTs
        byte[] bytes = Base64.urlDecoder.decode(part)
        return new String(bytes, StandardCharsets.UTF_8)
    }
}