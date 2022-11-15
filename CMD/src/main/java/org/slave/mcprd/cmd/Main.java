package org.slave.mcprd;

import lombok.NoArgsConstructor;

import java.io.IOException;

@NoArgsConstructor
public final class Main {

    public static void main(final String[] args) {
        MCPRD mcprd = new MCPRD();

        String mcpDir = null, mcVersion = null;
        boolean ignoreMCP = false;
        boolean jars = false, client = false, server = false;
        boolean libraries = false, natives = false, resources = false;
        boolean overwrite = false;
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--mcp")) mcpDir = args[++i];
            if (arg.equals("--mcVersion")) mcVersion = args[++i];
            if (arg.equals("--ignoreMCP")) ignoreMCP = !ignoreMCP;
            if (arg.equals("--jars")) jars = true;
            if (arg.equals("--client")) client = true;
            if (arg.equals("--server")) server = true;
            if (arg.equals("--libraries")) libraries = true;
            if (arg.equals("--natives")) natives = true;
            if (arg.equals("--resources")) resources = true;
            if (arg.equals("--overwrite")) overwrite = true;
        }

        try {
            mcprd.download(mcpDir, mcVersion, ignoreMCP, jars, client, server, libraries, natives, resources, overwrite);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download resources !", e);
        }
    }

}
