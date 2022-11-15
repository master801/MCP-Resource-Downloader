package org.slave.mcprd.gui.tasks;

import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.slave.mcprd.MCPRD;
import org.slave.mcprd.models.Version;

import java.io.File;

@RequiredArgsConstructor
public final class TaskDownloadMinecraftJars extends Task<Void> {

    private final MCPRD mcprd;

    private final File dirJars, dirJarsBin;
    private final Version version;
    private final boolean client, server, overwrite;

    @Override
    protected Void call() throws Exception {
        mcprd.downloadMinecraftJars(dirJars, dirJarsBin, version, client, server, overwrite);
        return null;
    }

}
