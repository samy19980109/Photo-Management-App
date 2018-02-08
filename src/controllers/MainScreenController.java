package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.FilePathTreeItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MainScreenController {

    @FXML
    public TextField imageFIleName;
    @FXML
    public VBox treeContainer;
    @FXML
    public ImageView selectedImageView;
    @FXML
    public VBox allAvailableTagsContainer;
    @FXML
    public TextField tagsOfSelectedImage;
    @FXML
    public TextField newTagTextBox;

    private TreeView<String> treeView;
    private List<String> selectedImageTagList = new ArrayList<>();

    private String originalFilepath;
    private String originalFileExtension;


    @FXML
    public void initialize() {
        //setup the file browser root
        String hostName = "computer";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException x) {
        }

        TreeItem<String> rootNode = new TreeItem<>(hostName, new ImageView(new Image(String.valueOf(getClass().getResource("../resources/computer.jpg")))));

        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();

        treeView = new TreeView<>(rootNode);
        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem> observable, TreeItem oldValue, TreeItem newValue) {
                FilePathTreeItem selectedItem = null;
                try {
                    selectedItem = (FilePathTreeItem) newValue;
                    if (!selectedItem.isDirectory()) {
                        Image image = new Image(new FileInputStream(selectedItem.getFullPath()));
                        selectedImageView.setImage(image);

                        /* Get originl File path*/
                        originalFilepath = selectedItem.getFullPath();

                        /* Get original File Extension*/
                        int i = originalFilepath.lastIndexOf('.');
                        if (i > 0) {
                            originalFileExtension = originalFilepath.substring(i + 1);
                        }

                        /* Clear Tag List*/
                        selectedImageTagList.clear();

                        /* Populate tag list of this image*/
                        Path path = Paths.get(selectedItem.getFullPath());
                        String fileNameWithOutExt = path.getFileName().toString().replaceFirst("[.][^.]+$", "");
                        populateSelectedImagetagList(fileNameWithOutExt);

                        /* Set Image file name */
                        imageFIleName.setText(path.getFileName().toString());

                        /*Set tags of this image*/
                        String allTags = getAllTagsAsAString();
                        tagsOfSelectedImage.setText(allTags.trim());
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        for (Path name : rootDirectories) {
            FilePathTreeItem treeNode = new FilePathTreeItem(name);
            rootNode.getChildren().add(treeNode);
        }
        rootNode.setExpanded(true);

        treeContainer.getChildren().addAll(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        /* Load existing tags*/
        List<String> existingTags = Utility.loadExistingTags("tags.txt");

        for (String tag : existingTags) {
            CheckBox checkBox = new CheckBox(tag);
            checkBox.setOnAction(checkBoxHandler);
            allAvailableTagsContainer.getChildren().add(checkBox);
        }
    }

    EventHandler checkBoxHandler = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (event.getSource() instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) event.getSource();
                if (checkBox.isSelected()) {
                    selectedImageTagList.add(checkBox.getText());

                } else {
                    selectedImageTagList.remove(checkBox.getText());
                }
                String allTags = getAllTagsAsAString();
                tagsOfSelectedImage.setText(allTags.trim());
            }
        }
    };

    private void populateSelectedImagetagList(String fileNameWithOutExt) {
        String[] tags = fileNameWithOutExt.split(" ");

        if (tags.length == 0) {
            return;
        } else {
            for (int i = 0; i < tags.length; i++) {
                int index = tags[i].indexOf("@");
                if (index == -1) {
                    continue;
                } else {
                    selectedImageTagList.add(tags[i].replace("@", ""));
                }
            }
        }
    }

    private String getAllTagsAsAString() {
        StringBuilder sb = new StringBuilder("");
        if (!selectedImageTagList.isEmpty()) {
            for (String tag : selectedImageTagList) {
                sb.append(tag);
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    public void renameImage(ActionEvent actionEvent) {
        try {
            File originalFile = new File(originalFilepath);
            String filePath = originalFile.getCanonicalPath().replaceAll(imageFIleName.getText(), "");

            String newFileName = getNewFileNameToBeSaved();
            File newFile = new File(filePath + newFileName + "."+originalFileExtension);

            if (newFile.exists()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("File already exist!!!");
                alert.setContentText("A file already exist with the same file name!!!\n"+ newFileName + "."+originalFileExtension);
                alert.showAndWait();
            }else {
                boolean success = originalFile.renameTo(newFile);

                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Success!!!");
                    alert.setContentText("File Successfully renamed to "+ newFileName + "."+originalFileExtension);
                    alert.showAndWait();
                }/*else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Error while deleting file");
                    alert.setContentText("Error while deleting file");
                    alert.showAndWait();
                }*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNewFileNameToBeSaved() {
        StringBuilder name = new StringBuilder("");
        for (String s : selectedImageTagList) {
            name.append("@");
            name.append(s);
            name.append(" ");
        }
        return name.toString().trim();
    }


    public void addTag(ActionEvent actionEvent) {
        String newTagName = newTagTextBox.getText();
        if (newTagName != null && newTagName.length() > 0) {
            selectedImageTagList.add(newTagName);

            String allTags = getAllTagsAsAString();
            tagsOfSelectedImage.setText(allTags.trim());

            CheckBox checkBox = new CheckBox(newTagName);
            checkBox.setOnAction(checkBoxHandler);
            allAvailableTagsContainer.getChildren().add(checkBox);

            newTagTextBox.clear();
        }
    }
}
