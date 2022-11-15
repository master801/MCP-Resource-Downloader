package org.slave.mcprd.gui;

import javafx.application.Platform;
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
import org.slave.mcprd.models.Version;
import org.slave.mcprd.models.VersionManifest;

import java.io.File;
import java.io.FileNotFoundException;
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
                            if (version.id().equals("1.5.2")) break;//Stop parsing versions after 1.5.2
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

            download(
                    resourceBundle,
                    mcprd,
                    choiceBoxVersion.valueProperty().get(),
                    textFieldMCP.textProperty().get(),
                    !checkBoxHidden.isSelected(),
                    checkBoxJars.selectedProperty().get(),
                    checkBoxClient.selectedProperty().get(),
                    checkBoxServer.selectedProperty().get(),
                    checkBoxLibraries.selectedProperty().get(),
                    checkBoxNatives.selectedProperty().get(),
                    checkBoxResources.selectedProperty().get(),
                    checkBoxOverwrite.selectedProperty().get()
            );
        });

        checkBoxJars.setOnAction(event -> {
            checkBoxClient.disableProperty().setValue(!checkBoxClient.disableProperty().get());
            checkBoxServer.disableProperty().setValue(!checkBoxServer.disableProperty().get());
        });

    }

    private void download(final ResourceBundle resourceBundle, final MCPRD mcprd, final String mcVersion, final String mcp, final boolean ensureMCP, final boolean jars, final boolean client, final boolean server, final boolean libraries, final boolean natives, final boolean resources, final boolean overwrite) {
        File dirMCP = new File(mcp);
        if (!dirMCP.exists()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.alert.mcp_dir_not_found.header"));
                alert.setContentText(String.format(resourceBundle.getString("key.alert.mcp_dir_not_found.content"), dirMCP.getAbsolutePath()));
                alert.showAndWait();
            });
            throw new RuntimeException(new FileNotFoundException(String.format("MCP Directory \"%s\" does not exist!", dirMCP.getAbsolutePath())));
        }
        if (!dirMCP.isDirectory()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.alert.mcp_dir_not_dir.header"));
                alert.setContentText(String.format(resourceBundle.getString("key.alert.mcp_dir_not_dir.content"), dirMCP.getAbsolutePath()));
                alert.showAndWait();
            });
            throw new RuntimeException(new FileNotFoundException(String.format("Selected path \"%s\" is not a directory!", dirMCP.getAbsolutePath())));
        }
        if (ensureMCP) {
            if (!new File(dirMCP, "docs/README-MCP.TXT").isFile()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText(resourceBundle.getString("key.alert.mcp_dir_invalid.header"));
                    alert.setContentText(String.format(resourceBundle.getString("key.alert.mcp_dir_invalid.content"), dirMCP.getAbsolutePath()));
                    alert.showAndWait();
                });
                throw new RuntimeException(new FileNotFoundException(String.format("Selected path \"%s\" is not not a valid MCP directory!", dirMCP.getAbsolutePath())));
            }
        }

        VersionManifest.Version manifestVersion = null;
        for(VersionManifest.Version i : versionManifest.versions()) {
            if(i.id().equals(mcVersion)) {
                manifestVersion = i;
                break;
            }
        }

        if (manifestVersion == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.null_manifest_version.header"));
                alert.setContentText(resourceBundle.getString("key.null_manifest_version.content"));
                alert.showAndWait();
            });
            return;
        }

        Version version = mcprd.getVersion(manifestVersion);
        if (version == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(resourceBundle.getString("key.null_version.header"));
                alert.setContentText(resourceBundle.getString("key.null_version.content"));
                alert.showAndWait();
            });
            return;
        }

        File dirJars = new File(dirMCP, "jars");
        File dirJarsBin = new File(dirJars, "bin");
        File dirJarsResources = new File(dirJars, "resources");
        File dirNatives = new File(dirJarsBin, "natives");

        if (jars) {
            if (!dirJarsBin.exists()) {
                if (!dirJarsBin.mkdirs()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setHeaderText(resourceBundle.getString("key.dir_jars_mkdirs.header"));
                        alert.setContentText(String.format(resourceBundle.getString("key.dir_jars_mkdirs.content"), dirJarsBin.getAbsolutePath()));
                        alert.showAndWait();
                    });
                    throw new RuntimeException(new IOException(String.format("Could not create directory \"%s\"!", dirJarsBin.getPath())));
                }
            }
            mcprd.downloadMinecraftJars(dirJars, dirJarsBin, version, client, server, overwrite);
        }
        if (libraries) {
            mcprd.downloadLibraries(dirJarsBin, version.libraries(), overwrite);
        }
        if (natives) {
            if (!dirNatives.exists()) {
                if (!dirNatives.mkdirs()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setHeaderText(resourceBundle.getString("key.dir_natives_mkdirs.header"));
                        alert.setContentText(String.format(resourceBundle.getString("key.dir_natives_mkdirs.content"), dirNatives.getAbsolutePath()));
                        alert.showAndWait();
                    });
                    throw new RuntimeException(new IOException(String.format("Could not create directory \"%s\"!", dirNatives.getPath())));
                }
            }
            mcprd.downloadAndExtractNatives(dirNatives, version, overwrite);
        }
        if (resources) {
            if (!dirJarsResources.exists()) {
                if (!dirJarsResources.mkdirs()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setHeaderText(resourceBundle.getString("key.dir_resources_mkdirs.header"));
                        alert.setContentText(String.format(resourceBundle.getString("key.dir_resources_mkdirs.content"), dirJarsResources.getAbsolutePath()));
                        alert.showAndWait();
                    });
                    throw new RuntimeException(new IOException(String.format("Could not create directory \"%s\"!", dirJarsResources.getPath())));
                }
            }
            mcprd.downloadResources(dirJarsResources, version, overwrite);
        }

        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText(resourceBundle.getString("key.done.header"));
            alert.setContentText(String.format(resourceBundle.getString("key.done.content"), dirJarsResources.getAbsolutePath()));
            alert.showAndWait();
        });
    }

}
