package network.delay.ui.devtools

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import java.util.regex.*

@CompileStatic
class RegexTool extends VBox {
    private final TextField patternField = new TextField()
    private final TextArea testArea = new TextArea()
    private final TextArea lineNumbers = new TextArea()
    private final ListView<MatchEntry> logView = new ListView<>()
    private final ComboBox<RegexTemplate> templatePicker = new ComboBox<>()

    static class MatchEntry {
        public String message
        public int start
        public int end
        public boolean isMatch

        @Override
        String toString() { message }
    }

    static class RegexTemplate {
        public String name, pattern, description
        RegexTemplate(String n, String p, String d) { name = n; pattern = p; description = d }
        @Override String toString() { name }
    }

    RegexTool() {
        setSpacing(10d)
        setPadding(new Insets(15d))

        getStyleClass().add("tool-pane")

        setupTemplates()
        def toolbar = new HBox(10).with {
            alignment = Pos.CENTER_LEFT
            children.addAll(new Label("Templates:"), templatePicker)
            it
        }

        lineNumbers.with {
            prefWidth = 45
            editable = false
            focusTraversable = false
            style = "-fx-background-color: #f0f0f0; -fx-text-fill: #999; -fx-font-family: 'Monospaced'; -fx-font-size: 13px;"
            mouseTransparent = true
        }

        testArea.with {
            promptText = "Enter text to test here..."
            style = "-fx-font-family: 'Monospaced'; -fx-font-size: 13px;"
            wrapText = false
            VBox.setVgrow(it, Priority.ALWAYS)
        }

        testArea.scrollTopProperty().addListener { obs, oldVal, newVal ->
            if (newVal instanceof Number) {
                lineNumbers.setScrollTop(((Number)newVal).doubleValue())
            }
        }

        def editorContainer = new HBox(lineNumbers, testArea).with {
            HBox.setHgrow(testArea, Priority.ALWAYS)
            it
        }
        VBox.setVgrow(editorContainer, Priority.ALWAYS)

        def split = new SplitPane()
        VBox.setVgrow(split, Priority.ALWAYS)

        def leftPane = new VBox(10).with {
            children.addAll(new Label("Pattern:"), patternField, new Label("Test String:"), editorContainer)
            VBox.setVgrow(it, Priority.ALWAYS)
            it
        }

        def rightPane = new VBox(10).with {
            children.addAll(new Label("Match Log:"), logView)
            VBox.setVgrow(logView, Priority.ALWAYS)
            it
        }

        setupLogCellFactory()
        split.items.addAll(leftPane, rightPane)
        split.setDividerPositions(0.65d)

        children.addAll(
                new Label("Regex Lab").with { style = "-fx-font-size: 18px; -fx-font-weight: bold;"; it },
                toolbar,
                split
        )

        patternField.textProperty().addListener { o, old, newVal -> runTest() }
        testArea.textProperty().addListener { o, old, newVal ->
            updateLineNumbers()
            runTest()
        }

        templatePicker.setOnAction {
            RegexTemplate sel = templatePicker.getSelectionModel().getSelectedItem()
            if (sel != null && sel.pattern != null) patternField.setText(sel.pattern)
        }

        // Fixed: Accessing fields of typed MatchEntry
        logView.getSelectionModel().selectedItemProperty().addListener { obs, old, val ->
            MatchEntry entry = (MatchEntry) val
            if (entry != null && entry.isMatch) {
                testArea.requestFocus()
                testArea.selectRange(entry.start, entry.end)
            }
        }

        updateLineNumbers()
    }

    private void updateLineNumbers() {
        int lines = testArea.getText().split("\n", -1).length
        StringBuilder sb = new StringBuilder()
        for (int i = 1; i <= lines; i++) {
            sb.append(i).append("\n")
        }
        lineNumbers.setText(sb.toString())
    }

    private void runTest() {
        logView.getItems().clear()
        String patternStr = patternField.getText()
        String textStr = testArea.getText()

        if (!patternStr || !textStr) return

        try {
            Pattern p = Pattern.compile(patternStr)
            Matcher m = p.matcher(textStr)

            int count = 0
            int searchOffset = 0

            while (count < 2000 && m.find(searchOffset)) {
                count++

                MatchEntry entry = new MatchEntry()
                entry.message = "MATCH [${count}]: Found '${m.group()}' at ${m.start()}".toString()
                entry.start = m.start()
                entry.end = m.end()
                entry.isMatch = true
                logView.getItems().add(entry)

                if (m.end() == searchOffset) {
                    searchOffset++
                } else {
                    searchOffset = m.end()
                }

                if (searchOffset > textStr.length()) break
            }

            if (count >= 2000) {
                MatchEntry warn = new MatchEntry()
                warn.message = "WARNING: Too many matches (Capped at 2000)".toString()
                warn.isMatch = false
                logView.getItems().add(warn)
            } else if (count == 0) {
                MatchEntry fail = new MatchEntry()
                fail.message = "FAIL: No matches found."
                fail.isMatch = false
                logView.getItems().add(fail)
            }
        } catch (PatternSyntaxException e) {
            MatchEntry err = new MatchEntry()
            err.message = "ERROR: Invalid Syntax".toString()
            err.isMatch = false
            logView.getItems().add(err)
        } catch (Exception e) {
            e.printStackTrace()
            MatchEntry critical = new MatchEntry()
            critical.message = "CRITICAL: Regex Engine Error (Too complex?)".toString()
            critical.isMatch = false
            logView.getItems().add(critical)
        }
    }

    private void setupLogCellFactory() {
        logView.setCellFactory { l ->
            new ListCell<MatchEntry>() {
                @Override
                protected void updateItem(MatchEntry item, boolean empty) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        setText(null); setStyle("")
                    } else {
                        setText(item.message)
                        if (item.isMatch) {
                            setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-cursor: hand;")
                        } else if (item.message.startsWith("FAIL") || item.message.startsWith("ERROR")) {
                            setStyle("-fx-text-fill: red; -fx-font-weight: bold;")
                        } else {
                            setStyle("-fx-text-fill: gray;")
                        }
                    }
                }
            }
        }
    }

    private void setupTemplates() {
        templatePicker.getItems().addAll(
                new RegexTemplate("Presets...", "", ""),

                new RegexTemplate("[WEB] Email (General)", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "Basic email validation"),
                new RegexTemplate("[WEB] URL (Simple)", "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)", "Matches most standard URLs"),
                new RegexTemplate("[WEB] Hex Color", "#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3})", "Matches #FFF or #FFFFFF"),
                new RegexTemplate("[WEB] HTML Tag", "<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "Captures full HTML tags"),
                new RegexTemplate("[WEB] Slug", "^[a-z0-9]+(?:-[a-z0-9]+)*\$", "URL-friendly slug (e.g. my-post-123)"),

                new RegexTemplate("[NET] IPv4 Address", "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b", "Validates 0.0.0.0 to 255.255.255.255"),
                new RegexTemplate("[NET] MAC Address", "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\$", "Standard hardware address"),
                new RegexTemplate("[NET] Port Number", "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])\$", "Range 0 to 65535"),

                new RegexTemplate("[SEC] UUID / GUID", "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "Standard 36-char UUID"),
                new RegexTemplate("[SEC] JWT Token", "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*\$", "Basic structure of a JWT"),
                new RegexTemplate("[SEC] Password (Strong)", "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$", "Min 8 chars, 1 Upper, 1 Lower, 1 Number, 1 Special"),
                new RegexTemplate("[SEC] MongoDB ObjectId", "^[0-9a-fA-F]{24}\$", "Standard 24-char hex ID"),

                new RegexTemplate("[DEV] ISO 8601 Date", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?", "Standard timestamp format"),
                new RegexTemplate("[DEV] camelCase", "^[a-z]+(?:[A-Z][a-z]+)*\$", "Matches camelCase strings"),
                new RegexTemplate("[DEV] snake_case", "^[a-z]+(?:_[a-z]+)*\$", "Matches snake_case strings"),
                new RegexTemplate("[DEV] kebab-case", "^[a-z]+(?:-[a-z]+)*\$", "Matches kebab-case strings"),
                new RegexTemplate("[DEV] Version (SemVer)", "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$", "Semantic Versioning"),

                new RegexTemplate("[NUM] Credit Card", "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})\$", "Matches most major CC numbers"),
                new RegexTemplate("[NUM] Integer Only", "^-?\\d+\$", "Positive or negative whole numbers"),
                new RegexTemplate("[NUM] Decimal Only", "^-?\\d*\\.\\d+\$", "Floating point numbers only")
        )
        templatePicker.getSelectionModel().selectFirst()
    }
}