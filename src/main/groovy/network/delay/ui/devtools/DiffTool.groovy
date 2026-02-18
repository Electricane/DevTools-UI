package network.delay.ui.devtools

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.*
import javafx.scene.Node as JFXNode

@CompileStatic
class DiffTool extends VBox {
    private final TextArea leftInput = new TextArea()
    private final TextArea rightInput = new TextArea()

    private final VBox leftResult = new VBox()
    private final VBox rightResult = new VBox()

    private final ScrollPane leftScroll = new ScrollPane(leftResult)
    private final ScrollPane rightScroll = new ScrollPane(rightResult)

    private final StackPane leftStack = new StackPane()
    private final StackPane rightStack = new StackPane()

    DiffTool() {
        setSpacing(10d)
        setPadding(new Insets(15d))

        def toolbar = new HBox(10).with {
            alignment = Pos.CENTER_LEFT
            def compareBtn = new Button("Visual Diff ↓")
            compareBtn.setOnAction { runVisualDiff() }

            def editBtn = new Button("Edit Mode")
            editBtn.setOnAction { showEditMode() }

            def clearBtn = new Button("Clear")
            clearBtn.setOnAction { clear() }

            children.addAll(compareBtn, editBtn, clearBtn)
            it
        }

        [leftInput, rightInput].each { it.style = "-fx-font-family: 'Monospaced';"; VBox.setVgrow(it, Priority.ALWAYS) }

        [leftResult, rightResult].each { it.fillWidth = true; it.style = "-fx-background-color: white;" }
        [leftScroll, rightScroll].each { it.fitToWidth = true; VBox.setVgrow(it, Priority.ALWAYS) }

        leftStack.children.addAll(leftInput, leftScroll)
        rightStack.children.addAll(rightInput, rightScroll)

        leftScroll.visible = false
        rightScroll.visible = false

        leftScroll.vvalueProperty().addListener { obs, old, newVal -> rightScroll.setVvalue(((Number)newVal).doubleValue()) }
        rightScroll.vvalueProperty().addListener { obs, old, newVal -> leftScroll.setVvalue(((Number)newVal).doubleValue()) }

        def split = new SplitPane(leftStack, rightStack)
        split.setDividerPositions(0.5d)
        VBox.setVgrow(split, Priority.ALWAYS)

        children.addAll(new Label("Visual Diff Tool (Experimental)").with { style="-fx-font-weight:bold; -fx-font-size:16;"; it }, toolbar, split)
    }

    private void runVisualDiff() {
        leftResult.children.clear()
        rightResult.children.clear()

        def leftLines = leftInput.text.split("\n", -1)
        def rightLines = rightInput.text.split("\n", -1)
        int maxLines = Math.max(leftLines.length, rightLines.length)

        for (int i = 0; i < maxLines; i++) {
            String l = i < leftLines.length ? leftLines[i] : null
            String r = i < rightLines.length ? rightLines[i] : null

            boolean isDiff = l != r

            leftResult.children.add(createDiffLine(l ?: "", isDiff, "#ffeef0", "#fdb8c0")) // Light red
            rightResult.children.add(createDiffLine(r ?: "", isDiff, "#e6ffed", "#acf2bd")) // Light green
        }

        leftScroll.visible = true
        rightScroll.visible = true
        leftInput.visible = false
        rightInput.visible = false
    }

    private JFXNode createDiffLine(String content, boolean highlight, String bgColor, String borderColor) {
        return new Label(content ?: " ").with {
            maxWidth = Double.MAX_VALUE
            padding = new Insets(2, 5, 2, 5)
            style = "-fx-font-family: 'Monospaced'; -fx-font-size: 12px;"
            if (highlight) {
                style += "-fx-background-color: ${bgColor}; -fx-border-color: ${borderColor}; -fx-border-width: 0 0 1 0;"
            }
            it
        }
    }

    private void showEditMode() {
        leftScroll.visible = false
        rightScroll.visible = false
        leftInput.visible = true
        rightInput.visible = true
    }

    private void clear() {
        leftInput.clear(); rightInput.clear()
        leftResult.children.clear(); rightResult.children.clear()
        showEditMode()
    }
}