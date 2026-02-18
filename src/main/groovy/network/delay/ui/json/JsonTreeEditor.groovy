package network.delay.ui.json;

import com.google.gson.*
import groovy.transform.CompileStatic
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import network.delay.ui.HelloApplication
import network.delay.ui.NumberUtils

@CompileStatic
class JsonTreeEditor extends VBox {
    private TreeView<JsonNodeItem> treeView = new TreeView<>()
    private TextArea rawJsonArea = new TextArea()
    private Gson gson = new GsonBuilder().setPrettyPrinting().create()

    JsonTreeEditor() {
        setSpacing(10d)
        setPadding(new Insets(15d))
        setVgrow(treeView, Priority.ALWAYS)
        setVgrow(rawJsonArea, Priority.ALWAYS)

        getStyleClass().add("json-tree-editor")

        def rootItem = new TreeItem<JsonNodeItem>(new JsonNodeItem("root", "", true))
        rootItem.expanded = true
        treeView.root = rootItem
        treeView.editable = true
        
        setupCellFactory()

        // Toolbar
        def toolbar = new HBox(10).with {
            alignment = Pos.CENTER_LEFT
            it.children.addAll(
                new Button("+ Subnode").with { onAction = { addNode(false) }; it },
                new Button("+ Object").with { onAction = { addNode(true) }; it },
                new Button("- Delete").with { onAction = { deleteNode() }; it },
                new Separator(),
                new Button("Build JSON ↓").with { onAction = { syncTreeToJson() }; it },
                new Button("Parse JSON ↑").with { onAction = { syncJsonToTree() }; it }
            )
            it
        }

        setupContextMenu()
        children.addAll(new Label("Visual Node Editor:"), treeView, toolbar, new Label("Raw JSON:"), rawJsonArea)
    }

    private void setupCellFactory() {
        treeView.cellFactory = { TreeView<JsonNodeItem> tv ->
            new TreeCell<JsonNodeItem>() {
                private HBox editBox
                private final TextField keyField = new TextField()
                private final TextField valueField = new TextField()
                private boolean initialized = false

                private void ensureInitialized() {
                    if (initialized) return
                    
                    keyField.setPromptText("Key")
                    valueField.setPromptText("Value")
                    
                    keyField.setOnKeyPressed { if (it.code == KeyCode.ENTER) commitEditAction() }
                    valueField.setOnKeyPressed { if (it.code == KeyCode.ENTER) commitEditAction() }
                    
                    initialized = true
                }

                private void commitEditAction() {
                    JsonNodeItem item = getItem()
                    if (item != null) {
                        item.key = keyField.getText()
                        if (!item.isObject) {
                            item.value = valueField.getText()
                        }
                        
                        commitEdit(item)
                    }
                }

                @Override
                protected void updateItem(JsonNodeItem item, boolean empty) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        setText(null)
                        setGraphic(null)
                    } else {
                        if (isEditing()) {
                            ensureInitialized()
                            setupEditBox(item)
                            setText(null)
                            setGraphic(editBox)
                        } else {
                            setText(item.toString())
                            setGraphic(null)
                        }
                    }
                }

                @Override
                void startEdit() {
                    if (getTreeItem() == getTreeView().getRoot()) {
                        return
                    }
                    
                    super.startEdit()
                    JsonNodeItem item = getItem()
                    if (item != null) {
                        ensureInitialized()
                        setupEditBox(item)
                        setText(null)
                        setGraphic(editBox)
                        keyField.requestFocus()
                    }
                }

                @Override
                void cancelEdit() {
                    super.cancelEdit()
                    JsonNodeItem item = getItem()
                    setText(item != null ? item.toString() : null)
                    setGraphic(null)
                }

                private void setupEditBox(JsonNodeItem item) {
                    if (editBox == null) {
                        editBox = new HBox(5)
                        editBox.setAlignment(Pos.CENTER_LEFT)
                    }
                    editBox.getChildren().clear()
                    keyField.setText(item.key)
                    editBox.getChildren().add(keyField)
                    
                    if (!item.isObject) {
                        valueField.setText(item.value)
                        editBox.getChildren().addAll(new Label(":"), valueField)
                    }
                }
            }
        }
    }

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu()
        MenuItem openInNumbers = new MenuItem("Open in NumberUtils")
        
        openInNumbers.setOnAction {
            def selected = treeView.selectionModel.selectedItem?.value
            if (selected && !selected.isObject) {
                HelloApplication.requestTab("Numbers", new NumberUtils(selected.value))
            }
        }
        
        menu.items.add(openInNumbers)

        treeView.setContextMenu(menu)
        menu.setOnShowing {
            def val = treeView.selectionModel.selectedItem?.value?.value
            openInNumbers.visible = val?.isLong() || val?.isBigDecimal()
        }
    }

    private void addNode(boolean isObject) {
        def selected = treeView.selectionModel.selectedItem ?: treeView.root
        def newNode = new TreeItem<>(new JsonNodeItem("key", isObject ? "" : "value", isObject))
        selected.children.add(newNode)
        selected.expanded = true
    }

    private void deleteNode() {
        def selected = treeView.selectionModel.selectedItem
        if (selected != null && selected != treeView.root) {
            selected.parent.children.remove(selected)
        }
    }

    private void syncTreeToJson() {
        Map<String, Object> map = [:]
        buildMapFromTree(treeView.root, map)
        rawJsonArea.text = gson.toJson(map.get("root"))
    }

    private void buildMapFromTree(TreeItem<JsonNodeItem> item, Map<String, Object> parentMap) {
        def data = item.value
        if (data.isObject) {
            Map<String, Object> childMap = [:]
            item.children.each { buildMapFromTree(it, childMap) }
            parentMap.put(data.key, childMap)
        } else {
            parentMap.put(data.key, data.value == "null" ? null : data.value)
        }
    }

    private void syncJsonToTree() {
        try {
            JsonElement rootElement = JsonParser.parseString(rawJsonArea.text)
            if (!rootElement.isJsonObject()) throw new Exception("Root must be a JSON Object")
            
            JsonObject jsonObject = rootElement.asJsonObject
            def newRoot = new TreeItem<>(new JsonNodeItem("root", "", true))
            parseJsonToTree(jsonObject, newRoot)
            treeView.root = newRoot
            newRoot.expanded = true
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Invalid JSON: ${e.message}").show()
        }
    }

    private void parseJsonToTree(JsonObject obj, TreeItem<JsonNodeItem> parent) {
        obj.entrySet().each { entry ->
            JsonElement val = entry.value
            
            if (val.isJsonObject()) {
                def child = new TreeItem<>(new JsonNodeItem(entry.key, "", true))
                parent.children.add(child)
                parseJsonToTree(val.asJsonObject, child)
            } else if (val.isJsonNull()) {
                parent.children.add(new TreeItem<>(new JsonNodeItem(entry.key, "null", false)))
            } else if (val.isJsonArray()) {
                parent.children.add(new TreeItem<>(new JsonNodeItem(entry.key, val.toString(), false)))
            } else {
                parent.children.add(new TreeItem<>(new JsonNodeItem(entry.key, val.asString, false)))
            }
        }
    }
}