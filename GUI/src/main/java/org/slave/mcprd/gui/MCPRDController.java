package org.slave.mcprd.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.NoArgsConstructor;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.gui.tasks.TaskGetVersionManifest;
import org.slave.mcprd.models.VersionManifest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public final class MCPRDController {

    private static final String VERSION_DEFAULT = "None";

    private static final List<String> BAD_SERVER = List.of(
            "1.2.5"
    );

    @SuppressWarnings("FieldCanBeLocal")
    private MCPRD mcprd;
    private VersionManifest versionManifest = null;

    private boolean linux = false, windows = false, osx = false;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChoiceBox<String> choiceBoxVersion;
    public Button buttonGetVersions;

    public TextField textFieldMCP;
    public Button buttonMCP;
    public CheckBox checkBoxHidden;

    public Button buttonDownload;
    public CheckBox checkBoxOverwrite;

    public CheckBox checkBoxJars, checkBoxLibraries, checkBoxNatives, checkBoxResources;

    public CheckBox checkBoxClient, checkBoxServer;

    public void init(final Window parentWindow, final ResourceBundle resourceBundle) {
        mcprd = new MCPRD();

        linux = System.getProperty("os.name").startsWith("Linux");
        windows = System.getProperty("os.name").startsWith("Windows");
        osx = System.getProperty("os.name").toLowerCase().contains("mac");//Mac OS users are completely fucked...
        if (osx) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("May you find light in Windows or Linux...");
            alert.showAndWait();
        }

        choiceBoxVersion.setValue(MCPRDController.VERSION_DEFAULT);

        buttonGetVersions.setOnAction(event -> {
            if (choiceBoxVersion.getItems().isEmpty() || versionManifest == null) {
                TaskGetVersionManifest task = new TaskGetVersionManifest(mcprd);
                task.setOnRunning(value -> buttonGetVersions.disableProperty().set(true));
                task.setOnSucceeded(value -> {
                    versionManifest = task.getValue();
                    if (versionManifest != null) {
                        List<String> versionIDs = new ArrayList<>();
                        for(int i = versionManifest.versions().length-1; i != 0; i--) {
                            VersionManifest.Version version = versionManifest.versions()[i];

                            if (version.type().equals(VersionManifest.Version.TYPE_SNAPSHOT)) continue;
                            if (version.id().startsWith("a") || version.id().startsWith("b") || version.id().startsWith("c") || version.id().startsWith("inf-") || version.id().startsWith("rd-")) continue;
                            versionIDs.add(version.id());
                            if (version.id().equals("1.6.4")) break;//Stop parsing versions after 1.6.4
                        }
                        Collections.reverse(versionIDs);//Make it look pretty
                        choiceBoxVersion.getItems().addAll(versionIDs);
                    } else {
                        buttonGetVersions.disableProperty().set(false);
                    }
                });
                task.setOnCancelled(value -> buttonGetVersions.disableProperty().set(false));
                task.setOnFailed(value -> buttonGetVersions.disableProperty().set(false));
                executorService.execute(task);
                executorService.shutdown();
            }
        });

        buttonMCP.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            directoryChooser.setTitle(resourceBundle.getString("key.button.mcp"));
            File f = directoryChooser.showDialog(parentWindow);
            if (f != null) textFieldMCP.setText(f.getAbsolutePath());
        });
        checkBoxHidden.setOnAction(event -> {
            if (!checkBoxHidden.selectedProperty().get()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText(resourceBundle.getString("key.alert.hidden.header"));
                alert.setContentText(resourceBundle.getString("key.alert.hidden.content"));
                alert.showAndWait();
            }
        });

        buttonDownload.setOnAction(event -> {
            if (choiceBoxVersion.valueProperty().get().equalsIgnoreCase(MCPRDController.VERSION_DEFAULT)) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.alert.version_missing.header"));
                alert.setContentText(resourceBundle.getString("key.alert.version_missing.content"));
                alert.showAndWait();
                return;
            }
            if (textFieldMCP.textProperty().isEmpty().get()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.alert.mcp_missing.header"));
                alert.setContentText(resourceBundle.getString("key.alert.mcp_missing.content"));
                alert.showAndWait();
                return;
            }
            if (!checkBoxJars.selectedProperty().get() && !checkBoxLibraries.selectedProperty().get() && !checkBoxNatives.selectedProperty().get() && !checkBoxResources.selectedProperty().get()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.alert.none.header"));
                alert.setContentText(resourceBundle.getString("key.alert.none.content"));
                alert.showAndWait();
            }

            if (checkBoxServer.selectedProperty().get() && MCPRDController.BAD_SERVER.contains(choiceBoxVersion.valueProperty().get())) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setHeaderText(resourceBundle.getString("key.alert.bad_server.header"));
                alert.setContentText(resourceBundle.getString("key.alert.bad_server.content"));
                alert.showAndWait();
            }

            try {
                mcprd.download(
                        textFieldMCP.textProperty().get(),
                        choiceBoxVersion.valueProperty().get(),
                        !checkBoxHidden.selectedProperty().get(),
                        checkBoxJars.selectedProperty().get(),
                        checkBoxClient.selectedProperty().get(),
                        checkBoxServer.selectedProperty().get(),
                        checkBoxLibraries.selectedProperty().get(),
                        checkBoxNatives.selectedProperty().get(),
                        linux,
                        windows,
                        osx,
                        checkBoxResources.selectedProperty().get(),
                        false,
                        checkBoxOverwrite.selectedProperty().get()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        checkBoxJars.setOnAction(event -> {
            checkBoxClient.disableProperty().setValue(!checkBoxClient.disableProperty().get());
            checkBoxServer.disableProperty().setValue(!checkBoxServer.disableProperty().get());
        });

    }

}
