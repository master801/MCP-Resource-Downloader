package org.slave.mcprd.gui.tasks;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.models.VersionManifest;

@RequiredArgsConstructor
public final class TaskGetVersionManifest extends Task<VersionManifest> {

    private final MCPRD mcprd;

    @Override
    protected VersionManifest call() throws Exception {
        updateMessage("Getting version manifest...");
        VersionManifest versionManifest = mcprd.getVersionManifest();
        updateMessage("Done getting version manifest");
        return versionManifest;
    }

}
