package org.slave.mcprd.gui.tasks;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.models.Version;
import org.slave.mcprd.models.VersionManifest;

@RequiredArgsConstructor
public final class TaskGetVersion extends Task<Version> {

    private final MCPRD mcprd;
    private final VersionManifest.Version manifestVersion;

    @Override
    protected Version call() throws Exception {
        return mcprd.getVersion(manifestVersion);
    }

}
