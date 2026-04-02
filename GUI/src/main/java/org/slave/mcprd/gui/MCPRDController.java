package org.slave.mcprd.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
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
import org.slave.mcprd.gui.tasks.TaskGetVersions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public final class MCPRDController {

    private static final String VERSION_DEFAULT = "None";

    @SuppressWarnings("FieldCanBeLocal")
    private MCPRD mcprd;

    private boolean isWindows = false, isLinux = false, isMac = false;
	private boolean is86 = false, is64 = false;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChoiceBox<String> choiceBoxVersion;
    public Button buttonGetVersions;
	public CheckBox checkBoxRelease, checkBoxSnapshot, checkBoxBeta, checkBoxAlpha, checkBoxInfdev, checkBoxClassic;

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
	public CheckBox checkBoxWindows, checkBoxLinux, checkBoxMac;
	public HBox hBoxNatives;
	public ChoiceBox<String> choiceBoxNativesPlatform, choiceBoxNativesNative, choiceBoxNativesNativeVersion;

	//Resources
	public HBox hBoxResources;
	public RadioButton radioButtonLocalAssets, radioButtonRemoteAssets;

    public void init(final Window parentWindow, final ResourceBundle resourceBundle) {
        mcprd = new MCPRD();
		try {
			mcprd.init();
	    } catch(IOException e) {
			throw new RuntimeException(e);
		}

		isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
		isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
		is86 = System.getProperty("os.arch").endsWith("86");
		is64 = System.getProperty("os.arch").endsWith("64");

		initComponents(parentWindow, resourceBundle);

		initJars(resourceBundle);
		initNatives(resourceBundle);
		initResources(resourceBundle);
    }

	private void initComponents(final Window parentWindow, final ResourceBundle resourceBundle) {
		choiceBoxVersion.setValue(MCPRDController.VERSION_DEFAULT);
		choiceBoxVersion.setDisable(true);
		choiceBoxVersion.setOnAction(event -> {
			try {
				mcprd.setVersion(choiceBoxVersion.getValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			if (mcprd.getVersionUnsafe() != null) {
				buttonDownload.disableProperty().set(false);//Enable the download button
			}
		});
		buttonGetVersions.setOnAction(event -> refreshVersions());
		checkBoxRelease.selectedProperty().set(true);//Enable only release by default
		checkBoxBeta.selectedProperty().set(false);
		checkBoxAlpha.selectedProperty().set(false);
		checkBoxInfdev.selectedProperty().set(false);

		buttonMCP.setOnAction(event -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
			directoryChooser.setTitle(resourceBundle.getString("key.button.mcp"));
			File f = directoryChooser.showDialog(parentWindow);
			if (f != null) textFieldMCP.setText(f.getAbsolutePath());
		});
		checkBoxHidden.setOnAction(event -> {
			if (checkBoxHidden.selectedProperty().get()) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText(resourceBundle.getString("key.alert.hidden.header"));
				alert.setContentText(resourceBundle.getString("key.alert.hidden.content"));
				alert.showAndWait();
			}
		});

		buttonDownload.disableProperty().set(true);
		buttonDownload.setOnAction(event -> {
			if (choiceBoxVersion.valueProperty().get().equalsIgnoreCase(MCPRDController.VERSION_DEFAULT)) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText(resourceBundle.getString("key.alert.version_missing.header"));
				alert.setContentText(resourceBundle.getString("key.alert.version_missing.content"));
				alert.showAndWait();
				return;
			}
			if (textFieldMCP.textProperty().isEmpty().get()) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText(resourceBundle.getString("key.alert.mcp_missing.header"));
				alert.setContentText(resourceBundle.getString("key.alert.mcp_missing.content"));
				alert.showAndWait();
				return;
			}
			if (!checkBoxJars.selectedProperty().get() && !checkBoxLibraries.selectedProperty().get() && !checkBoxNatives.selectedProperty().get() && !checkBoxResources.selectedProperty().get()) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
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
                        checkBoxMac.selectedProperty().get(),
                        checkBoxResources.selectedProperty().get(),
                        checkBoxForge.selectedProperty().get(),
                        radioButtonLocalAssets.selectedProperty().get() && !radioButtonRemoteAssets.selectedProperty().get(),
                        checkBoxOverwrite.selectedProperty().get()
                );

				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setHeaderText("Done!");
				alert.showAndWait();
			} catch(FileNotFoundException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
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
					alert = new Alert(Alert.AlertType.ERROR);
					alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_18.header"));
					alert.setContentText(resourceBundle.getString("key.alert.registry_java_version_18.content"));
					alert.showAndWait();
				} else if (returnValue == -1) {//not JDK 1.7
					alert = new Alert(Alert.AlertType.ERROR);
					alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_17.header"));
					alert.setContentText(resourceBundle.getString("key.alert.registry_java_version_17.content"));
					alert.showAndWait();
				} else if (returnValue == 1) {//Correct JDK version
					alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_correct.header"));
					alert.showAndWait();
				} else {
					alert = new Alert(Alert.AlertType.WARNING);
					alert.setHeaderText(resourceBundle.getString("key.alert.registry_java_version_invalid.header"));
					alert.showAndWait();
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void refreshVersions() {
		if (mcprd.getVersionManifestUnsafe() == null) {//Only fetch once so we're not hammering the server with requests
			TaskGetVersionManifest task = new TaskGetVersionManifest(mcprd);
			task.setOnRunning(value -> buttonGetVersions.disableProperty().set(true));
			task.setOnCancelled(value -> buttonDownload.disableProperty().set(false));
			task.setOnFailed(value -> buttonDownload.disableProperty().set(false));
			executorService.execute(task);
		}
		if (mcprd.getVersionManifestUnsafe() != null) {
			choiceBoxVersion.disableProperty().set(true);//Disable box

			//Heavy processing - do in a separate thread
			Task<ObservableList<String>> task = new TaskGetVersions(
					mcprd.getVersionManifestUnsafe(),
					checkBoxRelease.selectedProperty().get(),
					checkBoxSnapshot.selectedProperty().get(),
					checkBoxBeta.selectedProperty().get(),
					checkBoxAlpha.selectedProperty().get(),
					checkBoxInfdev.selectedProperty().get(),
					checkBoxClassic.selectedProperty().get()
			);
			task.setOnSucceeded(e -> {
				if (e.getSource().getValue() instanceof ObservableList<?>) {
					choiceBoxVersion.itemsProperty().set((ObservableList<String>)e.getSource().getValue());
				} else {
					//TODO Show error
				}
			});
			executorService.execute(task);

			choiceBoxVersion.disableProperty().set(false);//Reenable box
		} else {
			buttonGetVersions.disableProperty().set(false);
			//TODO Display a warning
		}
	}

	private void initJars(final ResourceBundle resourceBundle) {
		checkBoxJars.setOnAction(event -> {
			checkBoxClient.disableProperty().set(!checkBoxClient.disableProperty().get());
			checkBoxServer.disableProperty().set(!checkBoxServer.disableProperty().get());
		});
	}

	private void initNatives(final ResourceBundle resourceBundle) {
		vBoxNatives.managedProperty().set(false);//Actually hide this inactive window

		checkBoxWindows.selectedProperty().set(isWindows);
		checkBoxLinux.selectedProperty().set(isLinux);
		checkBoxMac.selectedProperty().set(isMac);
		checkBoxNatives.setOnAction(event -> {
			checkBoxWindows.disableProperty().set(!checkBoxWindows.disableProperty().get());
			checkBoxLinux.disableProperty().set(!checkBoxLinux.disableProperty().get());
			checkBoxMac.disableProperty().set(!checkBoxMac.disableProperty().get());

			choiceBoxNativesNative.disableProperty().set(!choiceBoxNativesNative.disableProperty().get());
			choiceBoxNativesNativeVersion.disableProperty().set(!choiceBoxNativesNativeVersion.disableProperty().get());
		});

		ObservableList<String> observableListPlatforms = FXCollections.observableArrayList();
		Arrays.stream(Constants.OS.values()).map(Constants.OS::getName).forEach(observableListPlatforms::add);
		choiceBoxNativesPlatform.itemsProperty().set(observableListPlatforms);
		if (isWindows) {
			choiceBoxNativesPlatform.getSelectionModel().select(Constants.OS.WINDOWS.getName());
		} else if (isLinux) {
			choiceBoxNativesPlatform.getSelectionModel().select(Constants.OS.LINUX.getName());
		} else if (isMac) {
			choiceBoxNativesPlatform.getSelectionModel().select(Constants.OS.MAC.getName());
		}
	}

	private void initResources(final ResourceBundle resourceBundle) {
		checkBoxResources.setOnAction(event -> {
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
