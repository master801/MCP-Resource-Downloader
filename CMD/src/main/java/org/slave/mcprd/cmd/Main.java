package org.slave.mcprd.cmd;

import lombok.NoArgsConstructor;
import org.slave.mcprd.MCPRD;

import java.io.IOException;

@NoArgsConstructor
public final class Main {

    public static void main(final String[] args) {
        String mcpDir = null, mcVersion = null;
        boolean ignoreMCP = false;
        boolean jars = false, client = false, server = false;
        boolean libraries = false, natives = false, resources = false;
        Boolean linux = null;
        Boolean windows = null, w32 = null, w64 = null;
        Boolean osx = null;
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
            if (arg.equals("--linux")) linux = true;
            if (arg.equals("--windows")) windows = true;
            if (arg.equals("--w32")) w32 = true;
            if (arg.equals("--w64")) w64 = true;
            if (arg.equals("--osx")) osx = true;

            if (arg.equals("--resources")) resources = true;

            if (arg.equals("--overwrite")) overwrite = true;
        }

        if (linux == null) linux = System.getProperty("os.name").startsWith("Linux");
        if (windows == null) windows = System.getProperty("os.name").startsWith("Windows");
        if (w32 == null) w32 = System.getProperty("os.arch").endsWith("86");
        if (w64 == null) w64 = System.getProperty("os.arch").endsWith("64");
        if (osx == null) {
            osx = System.getProperty("os.name").toLowerCase().contains("mac");//Mac OS users are completely fucked...
            if (osx) {
                System.out.println("May you find light in Windows or Linux...");
                System.out.println(System.getProperty("os.name"));
            }
        }

        MCPRD mcprd = new MCPRD();
        try {
            mcprd.download(
                    mcpDir,

                    mcVersion,

                    ignoreMCP,

                    jars,
                    client,
                    server,

                    libraries,

                    natives,
                    linux,
                    windows,
                    w32,
                    w64,
                    osx,

                    resources,

                    overwrite
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to download MCP resources !", e);
        }
    }

}
