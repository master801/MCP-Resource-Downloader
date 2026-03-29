package org.slave.mcprd.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.NoArgsConstructor;
import org.slave.mcprd.Constants;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.gui.tasks.TaskGetVersionManifest;
import org.slave.mcprd.models.Version;
import org.slave.mcprd.models.VersionManifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public final class MCPRDController {

    private static final String VERSION_DEFAULT = "None";

    @SuppressWarnings("FieldCanBeLocal")
    private MCPRD mcprd;

    private boolean isWindows = false, isLinux = false, isOSX = false;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChoiceBox<String> choiceBoxVersion;
    public Button buttonGetVersions;

    public TextField textFieldMCP;
    public Button buttonMCP;
    public CheckBox checkBoxHidden;
    public CheckBox checkBoxForge;

    public Button buttonDownload;
    public CheckBox checkBoxOverwrite;

    public CheckBox checkBoxJars, checkBoxLibraries, checkBoxNatives, checkBoxResources;

	//Jars
	public HBox hBoxJars;
    public CheckBox checkBoxClient, checkBoxServer;

	//Natives
	public VBox vBoxNatives;
	public HBox hBoxNativesPlatforms;
	public CheckBox checkBoxWindows, checkBoxLinux, checkBoxOSX;
	public HBox hBoxNatives;
	public ChoiceBox<String> choiceBoxNativesPlatform, choiceBoxNativesNative, choiceBoxNativesNativeVersion;

	//Resources
	public HBox hBoxResources;
	public RadioButton radioButtonLocalAssets, radioButtonRemoteAssets;

    public void init(final Window parentWindow, final ResourceBundle resourceBundle) {
        mcprd = new MCPRD();

        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
		isOSX = System.getProperty("os.name").toLowerCase().startsWith("mac");

        choiceBoxVersion.setValue(MCPRDController.VERSION_DEFAULT);
		choiceBoxVersion.setDisable(true);
		choiceBoxVersion.setOnAction(event -> {
			if (mcprd.getVersionUnsafe() != null) {
				for(Version.Library lib : mcprd.getVersionUnsafe().getNatives()) {
					Version.Library.Natives s = lib.natives();
					System.out.println("");
				}
//				choiceBoxNativesNative.itemsProperty()
//						.set(Arrays.asList(mcprd.getVersionUnsafe().getNatives()));
			}
		});
        buttonGetVersions.setOnAction(event -> {
            if (choiceBoxVersion.getItems().isEmpty() || mcprd.getVersionManifestUnsafe() == null) {
                TaskGetVersionManifest task = new TaskGetVersionManifest(mcprd);
                task.setOnRunning(value -> buttonGetVersions.disableProperty().set(true));
                task.setOnSucceeded(value -> {
                    if (mcprd.getVersionManifestUnsafe() != null) {
                        List<String> versionIDs = new ArrayList<>();
                        for(int i = mcprd.getVersionManifestUnsafe().versions().length-1; i != 0; i--) {
                            VersionManifest.Version version = mcprd.getVersionManifestUnsafe().versions()[i];
                            if (version.type().equals(VersionManifest.Version.TYPE_SNAPSHOT)) continue;
                            if (version.id().startsWith("a") || version.id().startsWith("b") || version.id().startsWith("c") || version.id().startsWith("inf-") || version.id().startsWith("rd-")) continue;
                            versionIDs.add(version.id());
                        }
                        Collections.reverse(versionIDs);//Make it look pretty
						choiceBoxVersion.setDisable(false);
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
            if (checkBoxHidden.selectedProperty().get()) {
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

            try {
                mcprd.download(
                        textFieldMCP.textProperty().get(),
                        choiceBoxVersion.valueProperty().get(),
                        checkBoxHidden.selectedProperty().get(),
                        checkBoxJars.selectedProperty().get(),
                        checkBoxClient.selectedProperty().get(),
                        checkBoxServer.selectedProperty().get(),
                        checkBoxLibraries.selectedProperty().get(),
                        checkBoxNatives.selectedProperty().get(),
                        checkBoxLinux.selectedProperty().get(),
                        checkBoxWindows.selectedProperty().get(),
                        System.getProperty("os.arch").endsWith("86"),
                        System.getProperty("os.arch").endsWith("64"),
                        checkBoxOSX.selectedProperty().get(),
                        checkBoxResources.selectedProperty().get(),
                        checkBoxForge.selectedProperty().get(),
                        radioButtonLocalAssets.selectedProperty().get() && !radioButtonRemoteAssets.selectedProperty().get(),
                        checkBoxOverwrite.selectedProperty().get()
                );

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("Done!");
                alert.showAndWait();
            } catch(FileNotFoundException e) {
                Alert alert = new Alert(AlertType.ERROR);
                if (e.getMessage().startsWith("Selected")) {//invalid MCP directory//TODO detect different types of FNEs
                    alert.setHeaderText(resourceBundle.getString("key.alert.mcp_dir_invalid.header"));
                    alert.setContentText(
                            String.format(resourceBundle.getString("key.alert.mcp_dir_invalid.content"), textFieldMCP.textProperty().get())
                    );
                } else if (e.getMessage().startsWith("MCP") && e.getMessage().endsWith("exist!")) {
                    alert.setHeaderText(resourceBundle.getString("key.alert.mcp_dir_not_found.header"));
                    alert.setContentText(
                            String.format(resourceBundle.getString("key.alert.mcp_dir_not_found.content"), textFieldMCP.textProperty().get())
                    );
                }
                alert.showAndWait();
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (isWindows) {
//                mcprd.getWindowsRegistryValues();//FIXME
//                int returnValue = mcprd.checkJavaVersionFromWindowsRegistry();//FIXME
                int returnValue = 0;
                Alert alert;
                if (returnValue == -2) {//not JDK 1.8
                    alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_18.header"));
                    alert.setContentText(resourceBundle.getString("key.alert.registry_java_version_18.content"));
                    alert.showAndWait();
                } else if (returnValue == -1) {//not JDK 1.7
                    alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_17.header"));
                    alert.setContentText(resourceBundle.getString("key.alert.registry_java_version_17.content"));
                    alert.showAndWait();
                } else if (returnValue == 1) {//Correct JDK version
                    alert = new Alert(AlertType.INFORMATION);
                    alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_correct.header"));
                    alert.showAndWait();
                } else {
                    alert = new Alert(AlertType.WARNING);
                    alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_invalid.header"));
                    alert.showAndWait();
                }
            }
        });

		//Jars
        checkBoxJars.setOnAction(event -> {
//			hBoxJars.visibleProperty().set(!hBoxJars.visibleProperty().get());
            checkBoxClient.disableProperty().set(!checkBoxClient.disableProperty().get());
            checkBoxServer.disableProperty().set(!checkBoxServer.disableProperty().get());
        });

		//Natives
		checkBoxWindows.selectedProperty().set(isWindows);
		checkBoxLinux.selectedProperty().set(isLinux);
		checkBoxOSX.selectedProperty().set(isOSX);
        checkBoxNatives.setOnAction(event -> {
//			vBoxNatives.visibleProperty().set(!vBoxNatives.visibleProperty().get());
            checkBoxWindows.disableProperty().set(!checkBoxWindows.disableProperty().get());
            checkBoxLinux.disableProperty().set(!checkBoxLinux.disableProperty().get());
            checkBoxOSX.disableProperty().set(!checkBoxOSX.disableProperty().get());

			choiceBoxNativesNative.disableProperty().set(!choiceBoxNativesNative.disableProperty().get());
			choiceBoxNativesNativeVersion.disableProperty().set(!choiceBoxNativesNativeVersion.disableProperty().get());
        });
		ObservableList<String> e = FXCollections.observableArrayList();
		Arrays.stream(Constants.OS.values()).map(Constants.OS::getName).forEach(e::add);
		choiceBoxNativesPlatform.itemsProperty().set(e);

		//Resources
        checkBoxResources.setOnAction(event -> {
//			hBoxResources.visibleProperty().set(!hBoxResources.visibleProperty().get());
            radioButtonLocalAssets.disableProperty().set(!radioButtonLocalAssets.disableProperty().get());
            radioButtonRemoteAssets.disableProperty().set(!radioButtonRemoteAssets.disableProperty().get());
        });
        radioButtonLocalAssets.setOnAction(event -> {
            if (radioButtonRemoteAssets.selectedProperty().get()) radioButtonRemoteAssets.selectedProperty().set(false);
        });
        radioButtonRemoteAssets.setOnAction(event -> {
            if (radioButtonLocalAssets.selectedProperty().get()) radioButtonLocalAssets.selectedProperty().set(false);
        });
        radioButtonRemoteAssets.selectedProperty().set(true);
    }

}
