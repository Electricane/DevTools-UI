package network.delay.ui.devtools

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*

@CompileStatic
class CronTool extends VBox {
    private final TextField cronInput = new TextField()
    private final TextArea explanationArea = new TextArea()
    private final Label statusLabel = new Label("Ready")

    CronTool() {
        setSpacing(15d)
        setPadding(new Insets(20d))

        def header = new Label("Cron Expression Parser").with {
            style = "-fx-font-size: 20px; -fx-font-weight: bold;"
            it
        }

        cronInput.promptText = "e.g. 0 0/30 8-17 * * MON-FRI"
        cronInput.style = "-fx-font-family: 'Monospaced'; -fx-font-size: 16px;"
        
        explanationArea.editable = false
        explanationArea.wrapText = true
        explanationArea.style = "-fx-font-size: 14px;"
        VBox.setVgrow(explanationArea, Priority.ALWAYS)

        def parseBtn = new Button("Parse Expression ↓")
        parseBtn.setOnAction { parse() }

        // Examples
        def examples = new HBox(10).with {
            alignment = Pos.CENTER_LEFT
            children.addAll(
                new Label("Quick Load:"),
                createExampleBtn("Every Minute", "* * * * *"),
                createExampleBtn("Hourly", "0 * * * *"),
                createExampleBtn("Midnight Daily", "0 0 * * *")
            )
            it
        }

        children.addAll(header, new Label("Cron String (Min Hour Day Month DayOfWeek):"), cronInput, examples, parseBtn, new Label("Human Readable:"), explanationArea, statusLabel)
    }

    private Button createExampleBtn(String name, String expr) {
        new Button(name).with {
            onAction = { cronInput.text = expr; parse() }
            style = "-fx-font-size: 10px;"
            it
        }
    }

    private void parse() {
        String input = cronInput.text.trim()
        if (!input) return

        try {
            String[] parts = input.split(/\s+/)
            if (parts.length < 5) throw new Exception("Need at least 5 parts (Minute Hour Day Month DayOfWeek)")

            def summary = new StringBuilder()
            summary.append("Summary:\n")
            summary.append("  - Minutes: ${describePart(parts[0], "minute")}\n")
            summary.append("  - Hours: ${describePart(parts[1], "hour")}\n")
            summary.append("  - Day of Month: ${describePart(parts[2], "day")}\n")
            summary.append("  - Month: ${describePart(parts[3], "month")}\n")
            summary.append("  - Day of Week: ${describePart(parts[4], "day of week")}\n")

            explanationArea.text = summary.toString()
            statusLabel.text = "Success"
            statusLabel.style = "-fx-text-fill: green;"
        } catch (Exception e) {
            explanationArea.text = "Error parsing: ${e.message}"
            statusLabel.text = "Invalid Format"
            statusLabel.style = "-fx-text-fill: red;"
        }
    }

    private String describePart(String part, String unit) {
        if (part == "*") return "every $unit"
        if (part.contains("/")) {
            def steps = part.split("/")
            return "every ${steps[1]} ${unit}s starting from ${steps[0] == "*" ? "0" : steps[0]}"
        }
        if (part.contains("-")) {
            return "between $part (inclusive)"
        }
        if (part.contains(",")) {
            return "at $unit markers [$part]"
        }
        return "at $unit $part"
    }
}