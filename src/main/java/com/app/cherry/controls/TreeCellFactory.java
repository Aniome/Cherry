package com.app.cherry.controls;

import com.app.cherry.controllers.MainController;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.util.Set;

public class TreeCellFactory {
    private final DataFormat JAVA_FORMAT = DataFormat.PLAIN_TEXT;
    TreeView<String> treeView;
    TreeItem<String> draggedItem;
    MainController mainController;
    Background whiteBackground;
    Background selectBackground;
    Border whiteBorder;
    Border selectBorder;
    TreeItem<String> parent;

    public TreeCellFactory(TreeView<String> treeView, MainController mainController) {
        this.treeView = treeView;
        this.mainController = mainController;
        init();
    }

    private void init(){
        setBackground(Color.WHITE);
        setBackground(Color.AQUA);

        whiteBorder = new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
        selectBorder = new Border(new BorderStroke(Color.AQUA, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));

        treeView.setCellFactory(tree -> {
            TreeCell<String> treeCell = new EditableTreeCell();

            treeCell.setOnMouseEntered( mouseEvent -> {
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (treeItem == null)
                    return;
                if (showingContextMenu())
                    return;
                if (!mouseEvent.isDragDetect())
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
                if (selectedItem == null)
                    return;
                MouseButton mouseButton = event.getButton();
                if (mouseButton.equals(MouseButton.PRIMARY) && selectedItem.isLeaf()){
                    mainController.loadDataOnFormOnClick(selectedItem);
                }
                if (mouseButton.equals(MouseButton.SECONDARY)){
                    treeView.setContextMenu(mainController.contextMenu);
                }
            });
            treeCell.setOnDragDetected((MouseEvent event) -> dragDetected(event, treeCell));
            treeCell.setOnDragOver((DragEvent event) -> dragOver(event, treeCell));
            treeCell.setOnDragDropped((DragEvent event) -> dragDrop(event, treeCell));
            treeCell.setOnDragDone((DragEvent event) -> clearDropLocation());
            treeCell.setOnDragEntered(dragEvent -> {
                System.out.println("drag entered");
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (treeItem == null || draggedItem == treeItem){
                    return;
                }

                MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
                selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

                if (treeItem.isLeaf()){
                    ObservableList<TreeItem<String>> parentChildrens = treeItem.getParent().getChildren();
                    if (parent == null){
                        parent = treeItem.getParent();
                        for (TreeItem<String> child : parentChildrens){
                            selectionModel.select(child);
                        }
                        selectionModel.select(parent);
                    } else if (treeItem.getParent() != parent){
                        selectionModel.clearSelection();
                        parent = treeItem.getParent();
                        for (TreeItem<String> child : parentChildrens){
                            selectionModel.select(child);
                        }
                        selectionModel.select(parent);
                    } else {
                        return;
                    }
                } else {
                    ObservableList<TreeItem<String>> childrens = treeItem.getChildren();
                    for (TreeItem<String> child : childrens){
                        selectionModel.select(child);
                    }
                }

                dragEvent.consume();
            });
            treeCell.setOnDragExited(dragEvent -> {
                System.out.println("dragExited");
                TreeItem<String> treeItem = treeCell.getTreeItem();
                if (treeItem == null || draggedItem == treeItem){
                    return;
                }
                boolean contains = parent.getChildren().contains(treeItem);
                if (parent != null && !contains){
                    ObservableList<TreeItem<String>> childrens = parent.getChildren();
                    MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
                    for (int i = 0; i < childrens.size(); i++) {
                        selectionModel.clearSelection(i);
                    }
                }

                dragEvent.consume();
            });

            return treeCell;
        });
    }

    private void setBackground(Color color){
        BackgroundFill backgroundFill = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
        if (Color.WHITE == color){
            whiteBackground = new Background(backgroundFill);
        } else {
            selectBackground = new Background(backgroundFill);
        }
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

    private void dragDrop(DragEvent event, TreeCell<String> treeCell) {
        Dragboard db = event.getDragboard();
        if (!db.hasContent(JAVA_FORMAT))
            return;

        TreeItem<String> thisItem = treeCell.getTreeItem();
        TreeItem<String> droppedItemParent = draggedItem.getParent();

        // remove from previous location
        droppedItemParent.getChildren().remove(draggedItem);
        if (thisItem.isLeaf()){
            thisItem.getParent().getChildren().add(draggedItem);
        } else {
            thisItem.getChildren().add(draggedItem);
        }
        event.setDropCompleted(true);
        event.consume();
    }

    private void clearDropLocation() {
        MultipleSelectionModel<TreeItem<String>> selectionModel = treeView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.clearSelection();
    }
}
