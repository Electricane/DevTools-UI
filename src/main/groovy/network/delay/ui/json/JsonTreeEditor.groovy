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
    private Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create()

    JsonTreeEditor() {
        setSpacing(10d)
        setPadding(new Insets(15d))
        setVgrow(treeView, Priority.ALWAYS)
        setVgrow(rawJsonArea, Priority.ALWAYS)

        getStyleClass().add("json-tree-editor")

        def rootItem = new TreeItem<JsonNodeItem>(new JsonNodeItem("root", "", true, false))
        rootItem.expanded = true
        treeView.root = rootItem
        treeView.editable = true
        
        setupCellFactory()

        def toolbar = new HBox(10).with {
            alignment = Pos.CENTER_LEFT
            it.children.addAll(
                new Button("+ Subnode").with { onAction = { addNode(false, false) }; it },
                new Button("+ Object").with { onAction = { addNode(true, false) }; it },
                new Button("+ Array").with { onAction = { addNode(false, true) }; it },
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
                        if (!item.isObject && !item.isArray) {
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
                    
                    if (!item.isObject && !item.isArray) {
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
            if (selected && !selected.isObject && !selected.isArray) {
                HelloApplication.requestTab("Numbers", new NumberUtils(selected.value))
            }
        }
        
        menu.items.add(openInNumbers)

        treeView.setContextMenu(menu)
        menu.setOnShowing {
            def item = treeView.selectionModel.selectedItem?.value
            openInNumbers.visible = item != null && !item.isObject && !item.isArray && 
                                       (item.value?.isLong() || item.value?.isBigDecimal())
        }
    }

    private void addNode(boolean isObject, boolean isArray) {
        def selected = treeView.selectionModel.selectedItem ?: treeView.root
        def newNode = new TreeItem<>(new JsonNodeItem("key", isObject || isArray ? "" : "value", isObject, isArray))
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
        Object result = buildDataFromTree(treeView.root)
        rawJsonArea.text = gson.toJson(result)
    }

    private Object buildDataFromTree(TreeItem<JsonNodeItem> item) {
        def data = item.value
        if (data.isObject) {
            Map<String, Object> map = [:]
            item.children.each { map.put(it.value.key, buildDataFromTree(it)) }
            return map
        } else if (data.isArray) {
            List<Object> list = []
            item.children.each { list.add(buildDataFromTree(it)) }
            return list
        } else {
            return data.value == "null" ? null : data.value
        }
    }

    private void syncJsonToTree() {
        try {
            JsonElement rootElement = JsonParser.parseString(rawJsonArea.text)
            TreeItem<JsonNodeItem> newRoot
            
            if (rootElement.isJsonObject()) {
                newRoot = new TreeItem<>(new JsonNodeItem("root", "", true, false))
                parseJsonObjectToTree(rootElement.asJsonObject, newRoot)
            } else if (rootElement.isJsonArray()) {
                newRoot = new TreeItem<>(new JsonNodeItem("root", "", false, true))
                parseJsonArrayToTree(rootElement.asJsonArray, newRoot)
            } else {
                throw new Exception("Root must be a JSON Object or Array")
            }
            
            treeView.root = newRoot
            newRoot.expanded = true
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Invalid JSON: ${e.message}").show()
        }
    }

    private void parseJsonObjectToTree(JsonObject obj, TreeItem<JsonNodeItem> parent) {
        obj.entrySet().each { entry ->
            processJsonElement(entry.key, entry.value, parent)
        }
    }

    private void parseJsonArrayToTree(JsonArray array, TreeItem<JsonNodeItem> parent) {
        for (int i = 0; i < array.size(); i++) {
            processJsonElement("[$i]", array.get(i), parent)
        }
    }

    private void processJsonElement(String key, JsonElement val, TreeItem<JsonNodeItem> parent) {
        if (val.isJsonObject()) {
            def child = new TreeItem<>(new JsonNodeItem(key, "", true, false))
            parent.children.add(child)
            parseJsonObjectToTree(val.asJsonObject, child)
        } else if (val.isJsonArray()) {
            def child = new TreeItem<>(new JsonNodeItem(key, "", false, true))
            parent.children.add(child)
            parseJsonArrayToTree(val.asJsonArray, child)
        } else if (val.isJsonNull()) {
            parent.children.add(new TreeItem<>(new JsonNodeItem(key, "null", false, false)))
        } else {
            parent.children.add(new TreeItem<>(new JsonNodeItem(key, val.asString, false, false)))
        }
    }
}