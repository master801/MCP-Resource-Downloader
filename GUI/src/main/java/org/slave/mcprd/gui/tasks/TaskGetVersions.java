package org.slave.mcprd.gui.tasks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.slave.mcprd.models.VersionManifest;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Master on 4/1/2026 at 1:24 PM
 *
 * @author Master
 */
@RequiredArgsConstructor
public final class TaskGetVersions extends Task<ObservableList<String>> {

	private final VersionManifest versionManifest;

	private final boolean isRelease, isSnapshot;
	private final boolean isBeta, isAlpha;
	private final boolean isInfdev, isClassic;

	@Override
	protected ObservableList<String> call() throws Exception {
		return Arrays.stream(versionManifest.versions())
				.filter(version ->
						(version.type().equals(VersionManifest.Version.TYPE_RELEASE) && isRelease) ||
						(version.type().equals(VersionManifest.Version.TYPE_SNAPSHOT) && isSnapshot) ||
						(version.id().startsWith("b") && isBeta) ||//beta
						(version.id().startsWith("a") && isAlpha) ||//alpha
						(version.id().startsWith("inf-") && isInfdev) ||//infdev
						(version.id().startsWith("c") && isClassic) ||//classic
						(version.id().startsWith("rd-") && isClassic))//pre-classic
				.map(VersionManifest.Version::id)
				.collect(Collectors.toCollection(FXCollections::observableArrayList));
	}

}
