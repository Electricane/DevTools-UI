package network.delay.ui

import groovy.transform.CompileStatic
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import javafx.util.Duration
import network.delay.ui.devtools.CronTool
import network.delay.ui.devtools.DiffTool
import network.delay.ui.devtools.GeneratorTool
import network.delay.ui.devtools.JwtTool
import network.delay.ui.devtools.RegexTool
import network.delay.ui.devtools.SqlTool
import network.delay.ui.json.JsonTreeEditor

import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import com.sun.management.OperatingSystemMXBean as SunOSBean

@CompileStatic
class HelloApplication extends Application {
    private static HelloApplication instance
    private TabPane tabPane = new TabPane()
    private Label statusCpu = new Label("CPU: 0%")
    private Label statusRam = new Label("RAM: 0MB")
    private final String version = "v1.2.1-STABLE"
    private boolean isDarkMode = false
    private final File configFile = new File("config.properties")

    @Override
    void start(Stage stage) {
        instance = this
        loadSettings()
        
        def rootPane = new BorderPane()
        rootPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm())
        
        if (isDarkMode) {
            rootPane.getStyleClass().add("dark-theme")
        }
        
        def sidebar = setupSidebar()
        tabPane.tabClosingPolicy = TabPane.TabClosingPolicy.ALL_TABS

        def statusBar = setupStatusBar()
        
        def splitPane = new SplitPane(sidebar, tabPane)
        splitPane.setDividerPositions(0.2d)
        
        SplitPane.setResizableWithParent(sidebar, false)
    
        rootPane.center = splitPane
        rootPane.bottom = statusBar

        setupMetricsTimer()

        def scene = new Scene(rootPane, 1200, 800)

        if (isDarkMode) {
            rootPane.getStyleClass().add("dark-theme")
        }

        stage.setTitle("DevTools")
        stage.setScene(scene)
        stage.show()
    }

    private void loadSettings() {
        try {
            if (configFile.exists()) {
                def props = new Properties()
                configFile.withInputStream { props.load(it) }
                isDarkMode = props.getProperty("darkMode", "false").toBoolean()
                println "Settings loaded: darkMode=$isDarkMode"
            }
        } catch (Exception e) {
            println "Could not load settings: ${e.message}"
        }
    }

    private void saveSettings() {
        def props = new Properties()
        props.setProperty("darkMode", isDarkMode.toString())
        configFile.withOutputStream { props.store(it, "App Settings") }
    }

    private Node setupStatusBar() {
        def bar = new HBox(20).with {
            padding = new Insets(8, 20, 8, 20)
            alignment = Pos.CENTER_RIGHT
            styleClass.add("status-bar")
            it
        }

        def themeToggle = new Button(isDarkMode ? "☀" : "🌙").with {
            style = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;"
            onAction = {
                isDarkMode = !isDarkMode
                text = isDarkMode ? "☀" : "🌙"

                def rootNode = getScene().getRoot()
                if (isDarkMode) {
                    if (!rootNode.styleClass.contains("dark-theme")) rootNode.styleClass.add("dark-theme")
                } else {
                    rootNode.styleClass.removeAll("dark-theme")
                }
                saveSettings()
            }
            it
        }

        def versionLabel = new Label("Version: $version").with {
            style = "-fx-font-family: 'Segoe UI', system-ui; -fx-text-fill: #888;"
            it
        }

        def spacer = new Pane()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        bar.children.addAll(themeToggle, versionLabel, spacer, statusCpu, statusRam)
        
        statusCpu.style = "-fx-font-family: 'Monospaced'; -fx-text-fill: #666;"
        statusRam.style = "-fx-font-family: 'Monospaced'; -fx-text-fill: #666;"
        
        return bar
    }

    private void setupMetricsTimer() {
        def timeline = new Timeline(new KeyFrame(Duration.seconds(1), { e ->
            updateMetrics()
        }))
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }

    private void updateMetrics() {
        Runtime rt = Runtime.getRuntime()
        long total = rt.totalMemory()
        long free = rt.freeMemory()
        long max = rt.maxMemory()
        
        long usedMB = (total - free).intdiv(1024).intdiv(1024)
        long maxMB = max.intdiv(1024).intdiv(1024)
    
        def osBean = ManagementFactory.getOperatingSystemMXBean()
        double cpuLoad = 0
        if (osBean instanceof SunOSBean) {
            cpuLoad = ((SunOSBean) osBean).getCpuLoad() * 100
        }

        Platform.runLater {
            statusCpu.setText(String.format("CPU: %.1f%%", cpuLoad))
            statusRam.setText("RAM: ${usedMB}MB / ${maxMB}MB".toString())
        
            if (usedMB > (maxMB * 0.8)) {
                statusRam.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Monospaced';")
                statusCpu.setStyle("-fx-text-fill: -my-text-main; -fx-font-family: 'Monospaced';")
            } else {
                statusRam.setStyle("-fx-text-fill: -my-text-main; -fx-font-family: 'Monospaced';")
                statusCpu.setStyle("-fx-text-fill: -my-text-main; -fx-font-family: 'Monospaced';")
            }
        }
    }

    private Node setupSidebar() {
        def sidebarList = new VBox(5)
        sidebarList.fillWidth = true

        def group1Box = new TilePane().with {
            padding = new Insets(10d)
            hgap = 10d; vgap = 10d
            prefColumns = 2
            prefTileWidth = 90d
            prefTileHeight = 90d
            it
        }
        
        group1Box.children.addAll(
            createIconButton("B64", "Base64") { openTab("Base64", new ConversionEditor(ConversionEditor.ToolMode.BASE64, "Base64 Converter")) },
            createIconButton("AES", "AES") { openTab("AES", new ConversionEditor(ConversionEditor.ToolMode.AES, "AES Encryption")) },
            createIconButton("#", "Hash") { openTab("Hash", new ConversionEditor(ConversionEditor.ToolMode.HASH, "SHA-256 Hasher")) },
            createIconButton("{}", "JSON") { openTab("JSON", new JsonTreeEditor()) },
            createIconButton(".*", "Regex") { 
                openTab("Regex Tester", new RegexTool())
            },
            createIconButton("JWT", "JWT") { 
                openTab("JWT Decoder", new JwtTool())
            },
            createIconButton("GEN", "Generators") { 
                openTab("Generators", new GeneratorTool())
            },
//            createIconButton("!=", "Diff") {
//                openTab("Diff Tool", new DiffTool())
//            },
            createIconButton("SQL", "SQL Formatter") {
                openTab("SQL Formatter", new SqlTool())
            },
            createIconButton("CRON", "Cron Parser") { 
                openTab("Cron Parser", new CronTool())
            }
        )

        def textToolsPane = new TitledPane("Text & Data", group1Box)

        def group2Box = new TilePane().with {
            padding = new Insets(10d); hgap = 10d; vgap = 10d
            prefColumns = 2
            prefTileWidth = 90d
            prefTileHeight = 90d
            it
        }
        group2Box.children.add(
            createIconButton("#123", "Numbers") { openTab("Numbers", new NumberUtils()) }
        )
        def numberToolsPane = new TitledPane("Number Tools", group2Box)

        sidebarList.children.addAll(textToolsPane, numberToolsPane)
    
        def scrollSidebar = new ScrollPane(sidebarList).with {
            fitToWidth = true
            minWidth = 230d
            maxWidth = 230d
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            styleClass.add("sidebar-scroll")
            it
        }

        return scrollSidebar
    }

    static void requestTab(String title, Node content) {
        instance.openTab(title, content)
    }

    private void openTab(String title, Node content) {
        def existingTab = tabPane.tabs.find { it.text == title }
        
//        if (existingTab) {
//            tabPane.selectionModel.select(existingTab)
//        } else {
            def newTab = new Tab(title, content)
            tabPane.tabs.add(newTab)
            tabPane.selectionModel.select(newTab)
//        }
    }

    private Button createIconButton(String iconChar, String title, Closure action) {
        def button = new Button(title)
        def icon = new Label(iconChar).with {
            style = "-fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 16px; -fx-text-fill: #0078d4;"
            it
        }
        button.with {
            graphic = icon
            contentDisplay = ContentDisplay.TOP
            alignment = Pos.CENTER
            setPrefSize(95d, 85d)
            onAction = { action.call() }
        }
        return button
    }
}