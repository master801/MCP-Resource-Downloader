package org.slave.mcprd.gui.tasks;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.models.Version;

import java.io.File;

@RequiredArgsConstructor
public final class TaskDownloadNatives extends Task<Void> {

    private final MCPRD mcprd;
    private final File dirNatives;
    private final Version version;
    private final boolean overwrite;

    @Override
    protected Void call() throws Exception {
        mcprd.downloadAndExtractNatives(dirNatives, version, overwrite);
        return null;
    }

}
