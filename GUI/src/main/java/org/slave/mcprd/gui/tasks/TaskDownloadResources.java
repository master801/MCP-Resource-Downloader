package org.slave.mcprd.gui.tasks;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.models.Version;

import java.io.File;

@RequiredArgsConstructor
public final class TaskDownloadResources extends Task<Void> {

    private final MCPRD mcprd;
    private final File dirJarsResources;
    private final Version version;
    private final boolean overwrite;

    @Override
    protected Void call() throws Exception {
        mcprd.downloadResources(dirJarsResources, version, overwrite);
        return null;
    }

}
