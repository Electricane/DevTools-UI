package network.delay.ui.devtools

import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import java.security.SecureRandom
import javafx.scene.control.SelectionMode

@CompileStatic
class GeneratorTool extends VBox {
    private final TextArea outputArea = new TextArea()
    private final ListView<String> historyLog = new ListView<String>()
    private final SecureRandom random = new SecureRandom()

    // Data Sets
    private final List<String> firstNames = ["James", "Mary", "Robert", "Patricia", "John",
                                             "Jennifer", "Michael", "Linda"]
    private final List<String> lastNames = ["Smith", "Johnson", "Williams", "Brown",
                                            "Jones", "Garcia", "Miller"]
    private final List<String> domains = ["gmail.com", "outlook.com", "proton.me", "company.io", "service.dev"]

    GeneratorTool() {
        setSpacing(10d)
        setPadding(new Insets(15d))


        // Takeoff like a rocket 🚀
        // TBH theres a lot of different Generators but zou can open a ticket and request another one.
        def header = new Label("Generators").with {
            style = "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
            it
        }

        def accordion = new VBox(5)

        def panes = [
                createSection("🔑 Identity", [
                        "UUID v4": { out UUID.randomUUID().toString() },
                        "NanoID": { out genRandomString(21, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz") },
                        "MongoID": { out genRandomString(24, "0123456789abcdef") },
                        "API Key": { out "ak_" + genRandomString(32, "0123456789abcdef") }
                ]),
                createSection("🌐 Networking", [
                        "IPv4": { out "${r(255)}.${r(255)}.${r(255)}.${r(255)}".toString() },
                        "IPv6": { out genIPv6() },
                        "MAC": { out genMAC() },
                        "Port": { out String.valueOf(r(65535)) }
                ]),
                createSection("👤 Content", [
                        "Full Name": { out "${pick(firstNames)} ${pick(lastNames)}".toString() },
                        "Email": { out "${pick(firstNames).toLowerCase()}@${pick(domains)}".toString() },
                        "Timestamp": { out String.valueOf(System.currentTimeMillis()) },
                        "ISO Now": { out java.time.Instant.now().toString() }
                ]),
                createSection("🎲 Random", [
                        "Dice (d20)": { out String.valueOf(random.nextInt(20) + 1) },
                        "Hex Color": { out String.format("#%06X", random.nextInt(0xFFFFFF + 1)) },
                        "Boolean": { out String.valueOf(random.nextBoolean()) },
                        "Lat/Long": { out "${r(180)-90}.${r(9999)}, ${r(360)-180}.${r(9999)}".toString() }
                ])
        ]

        // Force all panes to be expanded and add to VBox
        panes.each {
            it.setExpanded(true)
            accordion.children.add(it)
        }

        def scrollGenerators = new ScrollPane(accordion).with {
            fitToWidth = true
            style = "-fx-background-color: transparent;"
            it
        }

        // --- Layout Assemble ---
        def split = new SplitPane()
        VBox.setVgrow(split, Priority.ALWAYS)

        // Center: Generators and current Output
        def centerBox = new VBox(10, new Label("Generators:"), scrollGenerators, new Label("Latest Output:"), outputArea)
        outputArea.with {
            prefHeight = 100
            style = "-fx-font-family: 'Monospaced'; -fx-font-size: 14px;"
        }
        VBox.setVgrow(scrollGenerators, Priority.ALWAYS)

        def logBox = new VBox(10, new Label("History Log:"), historyLog)
        historyLog.with {
            styleClass.add("match-log")
            // Enable selecting multiple lines
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            
            def contextMenu = new ContextMenu()
            
            // --- Option 1: Copy Selection ---
            def copySelectionItem = new MenuItem("Copy Selection")
            copySelectionItem.setOnAction {
                def selected = selectionModel.selectedItems
                if (!selected.isEmpty()) {
                    copyToClipboard(selected.join("\n"))
                }
            }
            
            // --- Option 2: Copy All ---
            def copyAllItem = new MenuItem("Copy All History")
            copyAllItem.setOnAction {
                def allItems = items
                if (!allItems.isEmpty()) {
                    copyToClipboard(allItems.join("\n"))
                }
            }
            
            contextMenu.items.addAll(copySelectionItem, new SeparatorMenuItem(), copyAllItem)
            setContextMenu(contextMenu)
        }
        VBox.setVgrow(historyLog, Priority.ALWAYS)

        split.items.addAll(centerBox, logBox)
        split.setDividerPositions(0.7d)

        children.addAll(header, split)
    }

    /**
     * Helper to handle system clipboard interaction
     */
    private void copyToClipboard(String text) {
        def content = new ClipboardContent()
        content.putString(text)
        Clipboard.systemClipboard.setContent(content)
    }

    private TitledPane createSection(String title, Map<String, Closure> items) {
        def tilePane = new TilePane().with {
            hgap = 10; vgap = 10; padding = new Insets(10)
            prefColumns = 3
            it
        }
        items.each { String name, Closure logic ->
            def btn = new Button(name).with {
                prefWidth = 110; prefHeight = 50; wrapText = true
                style = "-fx-font-size: 11px; -fx-cursor: hand;"
                setOnAction { logic.call() }
                it
            }
            tilePane.children.add(btn)
        }
        return new TitledPane(title, tilePane)
    }

    //private void setupHistoryMenu() { // Removed, since logic moved into `historyLog.with` block
    //    ContextMenu menu = new ContextMenu()
    //    MenuItem copyItem = new MenuItem("Copy to Clipboard")
    //    copyItem.setOnAction {
    //        String selected = historyLog.selectionModel.selectedItem
    //        if (selected) {
    //            ClipboardContent content = new ClipboardContent()
    //            content.putString(selected)
    //            Clipboard.systemClipboard.setContent(content)
    //        }
    //    }
    //    menu.items.add(copyItem)
    //    historyLog.setContextMenu(menu)
    //}

    private void out(String s) {
        outputArea.setText(s)
        historyLog.items.add(0, s) // Add to top of log
        if (historyLog.items.size() > 50) historyLog.items.remove(50)
    }

    private int r(int bound) { random.nextInt(bound) }
    private String pick(List<String> list) { list.get(random.nextInt(list.size())) }

    private String genRandomString(int length, String charset) {
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < length; i++) sb.append(charset.charAt(random.nextInt(charset.length())))
        return sb.toString()
    }

    private String genIPv6() {
        List<String> p = []
        for (int i = 0; i < 8; i++) p.add(Integer.toHexString(random.nextInt(0xFFFF)))
        return String.join(":", p)
    }

    private String genMAC() {
        List<String> p = []
        for (int i = 0; i < 6; i++) p.add(String.format("%02x", random.nextInt(256)))
        return String.join(":", p)
    }
}