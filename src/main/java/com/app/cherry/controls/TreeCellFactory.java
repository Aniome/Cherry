package com.app.cherry.controls;

import com.app.cherry.Markdown;
import com.app.cherry.controllers.MainController;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class TreeCellFactory {
    private final DataFormat JAVA_FORMAT = DataFormat.PLAIN_TEXT;
    TreeView<String> treeView;
    Background whiteBackground;
    TreeCell<String> treeCellTreeView;
    ContextMenu contextMenu;
    TreeItem<String> draggedItem;
    MainController mainController;

    public TreeCellFactory(TreeView<String> treeView, MainController mainController) {
        this.treeView = treeView;
        this.mainController = mainController;
        init();
    }

    private void init(){
        treeView.setCellFactory(tree -> {
            TreeCell<String> treeCell = new EditableTreeCell();

            whiteBackground = treeCell.getBackground();
            treeCellTreeView = treeCell;
            treeCell.setOnMouseEntered( mouseEvent -> {
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (treeItem == null)
                    return;
                if (showingContextMenu())
                    return;
                treeView.getSelectionModel().select(treeItem);
            });
            treeCell.setOnMouseExited(mouseEvent -> {
                if (showingContextMenu())
                    return;
                treeView.setContextMenu(null);
                treeView.getSelectionModel().clearSelection();
            });
            treeCell.setOnMouseClicked(event -> {
                TreeItem<String> selectedItem = treeCell.getTreeItem();
                if (selectedItem == null || !selectedItem.isLeaf())
                    return;
                MouseButton mouseButton = event.getButton();
                if (mouseButton.equals(MouseButton.PRIMARY)){
                    mainController.loadDataOnFormOnClick(selectedItem);
                }
                if (mouseButton.equals(MouseButton.SECONDARY)){
                    treeView.setContextMenu(contextMenu);
                }
            });
            treeCell.setOnDragDetected((MouseEvent event) -> dragDetected(event, treeCell));
            treeCell.setOnDragOver((DragEvent event) -> dragOver(event, treeCell));
            treeCell.setOnDragDropped((DragEvent event) -> drop(event, treeCell, treeView));
            treeCell.setOnDragDone((DragEvent event) -> clearDropLocation());
            treeCell.setOnDragEntered(dragEvent -> {
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (treeItem == null || treeItem.isLeaf()){
                    return;
                }
                BackgroundFill background_fill = new BackgroundFill(Color.BLUEVIOLET,
                        CornerRadii.EMPTY, Insets.EMPTY);
                treeCell.setBackground(new Background(background_fill));
                dragEvent.consume();
            });
            treeCell.setOnDragExited(dragEvent -> {
                treeCell.setBackground(whiteBackground);
                dragEvent.consume();
            });

            return treeCell;
        });
    }

    private boolean showingContextMenu(){
        ContextMenu contMenu = treeView.getContextMenu();
        return contMenu != null && contMenu.isShowing();
    }

    private void dragDetected(MouseEvent event, TreeCell<String> treeCell) {
        draggedItem = treeCell.getTreeItem();
        if (draggedItem == null)
            return;
        Dragboard db = treeCell.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(JAVA_FORMAT, draggedItem.getValue());
        db.setContent(content);
        db.setDragView(treeCell.snapshot(null, null));
        event.consume();
    }

    private void dragOver(DragEvent event, TreeCell<String> treeCell) {
        if (!event.getDragboard().hasContent(JAVA_FORMAT))
            return;
        TreeItem<String> thisItem = treeCell.getTreeItem();
        // can't drop on itself
        if (draggedItem == null || thisItem == null || thisItem == draggedItem)
            return;
        event.acceptTransferModes(TransferMode.MOVE);
    }

    private void drop(DragEvent event, TreeCell<String> treeCell, TreeView<String> treeView) {
        Dragboard db = event.getDragboard();
        if (!db.hasContent(JAVA_FORMAT))
            return;

        TreeItem<String> thisItem = treeCell.getTreeItem();
        TreeItem<String> droppedItemParent = draggedItem.getParent();

        // remove from previous location
        droppedItemParent.getChildren().remove(draggedItem);
        thisItem.getParent().getChildren().add(draggedItem);
        event.setDropCompleted(true);
        event.consume();
    }

    private void clearDropLocation() {
        treeView.getSelectionModel().clearSelection();
    }
}
