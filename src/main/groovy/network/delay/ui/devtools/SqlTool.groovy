package network.delay.ui.devtools

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*

@CompileStatic
class SqlTool extends VBox {
    private final TextArea inputArea = new TextArea()
    private final TextArea outputArea = new TextArea()

    SqlTool() {
        setSpacing(10d)
        setPadding(new Insets(15d))

        def header = new Label("SQL Formatter").with {
            style = "-fx-font-size: 18px; -fx-font-weight: bold;"
            it
        }

        def toolbar = new HBox(10).with {
            alignment = Pos.CENTER_LEFT
            def formatBtn = new Button("Beautify SQL ↓")
            formatBtn.setOnAction { format() }
            
            def clearBtn = new Button("Clear")
            clearBtn.setOnAction { inputArea.clear(); outputArea.clear() }
            
            children.addAll(formatBtn, clearBtn)
            it
        }

        [inputArea, outputArea].each {
            it.style = "-fx-font-family: 'Monospaced'; -fx-font-size: 12px;"
            VBox.setVgrow(it, Priority.ALWAYS)
        }
        inputArea.promptText = "SELECT * FROM users WHERE id=1..."
        outputArea.editable = false
        outputArea.promptText = "Formatted SQL will appear here..."

        children.addAll(header, new Label("Raw SQL:"), inputArea, toolbar, new Label("Formatted:"), outputArea)
    }

    private void format() {
        String sql = inputArea.text.trim()
        if (!sql) return

        sql = sql.replaceAll(/\s+/, " ")

        String[] majorKeywords = [
            "SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "HAVING", "LIMIT",
            "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM",
            "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "CROSS JOIN", "JOIN", "UNION"
        ]

        String result = sql
        
        for (String kw : majorKeywords) {
            result = result.replaceAll(/(?i)\b${kw}\b/, "\n${kw.toUpperCase()}\n  ")
        }

        result = result.replaceAll(/,\s*/, ",\n  ")
        
        result = result.replaceAll(/\n\s*\n/, "\n").trim()

        outputArea.text = result
    }
}