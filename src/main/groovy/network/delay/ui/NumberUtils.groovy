package network.delay.ui

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.scene.Node as JFXNode // Alias to avoid collision
import javafx.scene.control.*
import javafx.scene.layout.*
import java.math.BigInteger

@CompileStatic
class NumberUtils extends VBox {
    private final TextArea inputArea = new TextArea()
    private final Map<String, TextField> fields = [:]

    NumberUtils(String initialValue = "") {
        setSpacing(10d)
        setPadding(new Insets(15d))

        def header = new Label("Numbers").with {
            style = "-fx-font-size: 22px; -fx-font-weight: bold;"
            it
        }

        inputArea.with {
            promptText = "Enter any massive number..."
            prefHeight = 80
            wrapText = true
            style = "-fx-font-family: 'Monospaced'; -fx-font-size: 14px;"
            text = initialValue
        }

        def mainScroll = new ScrollPane().with {
            fitToWidth = true
            content = createDashboard()
            style = "-fx-background-color: transparent;"
            it
        }
        VBox.setVgrow(mainScroll, Priority.ALWAYS)

        children.addAll(header, new Label("Input (Decimal):"), inputArea, (JFXNode)mainScroll)

        inputArea.textProperty().addListener { obs, old, newVal -> process(newVal) }
        if (initialValue) process(initialValue)
    }

    private JFXNode createDashboard() {
        def container = new VBox(20)

        container.children.add((JFXNode)createSection("Programmer Bases", ["Hex (0x)", "Binary", "Octal", "Base32"]))
        container.children.add((JFXNode)createSection("Data Sizes (IEC)", ["Bytes", "KiB", "MiB", "GiB", "TiB", "PiB"]))
        container.children.add((JFXNode)createSection("Time Durations (as Millis)", ["Seconds", "Minutes", "Hours", "Days", "Years"]))
        container.children.add((JFXNode)createSection("Math Properties", ["Scientific", "Bit Count", "Is Prime? (Probable)", "Next Probable Prime"]))

        return container
    }

    private JFXNode createSection(String title, List<String> labelNames) {
        def section = new VBox(5)
        def head = new Label(title).with {
            style = "-fx-font-weight: bold; -fx-text-fill: -my-accent-color;"
            it
        }
        def grid = new GridPane().with { hgap = 10; vgap = 8; it }

        labelNames.eachWithIndex { String name, int i ->
            def field = new TextField().with {
                editable = false
                style = "-fx-font-family: 'Monospaced';"
                it
            }
            fields[name] = field
            grid.add(new Label(name), 0, i)
            grid.add(field, 1, i)
            GridPane.setHgrow(field, Priority.ALWAYS)
        }
        section.children.addAll(head, grid)
        return section
    }

    private void process(String text) {
        String clean = text.replaceAll(/\s+/, "")
        if (!clean || !clean.matches(/^-?\d*\.?\d*$/) || clean == "." || clean == "-.") {
            fields.values().each { it.text = "..." }
            return
        }

        try {
            BigDecimal dec = new BigDecimal(clean)
            BigInteger val = dec.toBigInteger()

            fields["Hex (0x)"].text = "0x" + val.toString(16).toUpperCase()
            fields["Binary"].text = val.toString(2)
            fields["Octal"].text = "0" + val.toString(8)
            fields["Base32"].text = val.toString(32).toUpperCase()

            fields["Bytes"].text = String.format("%,.2f", dec)
            fields["KiB"].text = dec.divide(new BigDecimal("1024"), 6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["MiB"].text = dec.divide(new BigDecimal("1048576"), 6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["GiB"].text = dec.divide(new BigDecimal("1073741824"), 6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["TiB"].text = dec.divide(new BigDecimal("1099511627776"), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["PiB"].text = dec.divide(new BigDecimal("1125899906842624"), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()

            fields["Seconds"].text = dec.divide(new BigDecimal("1000"), 3, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["Minutes"].text = dec.divide(new BigDecimal("60000"), 5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["Hours"].text = dec.divide(new BigDecimal("3600000"), 5, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["Days"].text = dec.divide(new BigDecimal("86400000"), 6, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()
            fields["Years"].text = dec.divide(new BigDecimal("31536000000"), 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()

            fields["Scientific"].text = String.format("%.10E", dec)
            fields["Bit Count"].text = val.bitLength().toString()
            
            if (dec.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                fields["Is Prime? (Probable)"].text = val.isProbablePrime(10) ? "YES" : "NO"
                fields["Next Probable Prime"].text = val.nextProbablePrime().toString()
            } else {
                fields["Is Prime? (Probable)"].text = "N/A (Decimal)"
                fields["Next Probable Prime"].text = "N/A"
            }

        } catch (Exception e) {
            fields.values().each { it.text = "ERROR" }
        }
    }
}