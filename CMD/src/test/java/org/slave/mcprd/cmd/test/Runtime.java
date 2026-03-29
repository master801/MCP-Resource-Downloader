package org.slave.mcprd.cmd.test;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.slave.mcprd.cmd.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

@UtilityClass
public class Runtime {

    /**
     * @param fileMCPZip File of MCP zip - never null
     * @param fileForgeZip File of Forge zip - can be null (needs a check!)
     * @param isLegacy If it is 1.3.2 or older - this is a requirement because older versions use the "mcp/forge" file path, instead of newer versions which use "forge/mcp" fp
     */
    public static void setUpMCPFiles(@NonNull final File fileMCPZip, final File fileForgeZip, final File dirDest, final boolean isLegacy) throws IOException {
        if (!dirDest.exists() && !dirDest.mkdirs()) throw new IOException(String.format("Cannot create directory \"%s\" due to an IO error!", dirDest.getAbsolutePath()));
        try(ZipFile zipFileMCP = new ZipFile(fileMCPZip)) {
            if (fileForgeZip != null) {
                if (isLegacy) {//mcp/forge
                    Utilities.extractZip(zipFileMCP, dirDest);
                    try(ZipFile zipFileForge = new ZipFile(fileForgeZip)) {
                        Utilities.extractZip(zipFileForge, dirDest);
                    }
                } else {//forge/mcp
                    try(ZipFile zipFileForge = new ZipFile(fileForgeZip)) {
                        Utilities.extractZip(zipFileForge, dirDest);
                    }
                    Utilities.extractZip(zipFileMCP, new File(dirDest, "forge/mcp"));

                    //Copy MCP zip into forge/fml folder
                    StringBuilder sb = new StringBuilder(fileMCPZip.getName());
                    sb.insert(4, '.');
                    sb.insert(0, "forge/fml/");
                    File fileFMLMCPZip = new File(dirDest, sb.toString());
                    Files.copy(fileMCPZip.toPath(), fileFMLMCPZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        if (isLegacy) {//Copy fernflower.jar - this isn't packaged in older versions (before 1.2.5) of MCP due to licensing issues
            File fileJarFernFlower = new File(dirDest, "runtime/bin/fernflower.jar");
            if (!Constants.FILE_JAR_FERNFLOWER.exists()) throw new FileNotFoundException(String.format("Failed to copy \"fernflower.jar\" to \"%s\" due to it not existing!", fileJarFernFlower.getAbsolutePath()));
            if (!fileJarFernFlower.exists()) Files.copy(Constants.FILE_JAR_FERNFLOWER.toPath(), fileJarFernFlower.toPath());
        }
    }

    /**
     * For really old versions of Minecraft - 1.1 / 1.2.3
     */
    public static void setUpModLoaderForForge(final File dirMCP, final File fileModLoaderZip) throws IOException {
        File dirBin = new File(dirMCP, "jars/bin");

        File fileJar = new File(dirBin, "minecraft.jar");
        File fileJarBK = new File(dirBin, "minecraft_bk.jar");
        File fileJarPatched = new File(dirBin, "minecraft_patched.jar");

        Utilities.patchJar(fileJar, fileModLoaderZip, fileJarPatched);

        if(!fileJar.renameTo(fileJarBK)) {
            throw new IOException(
                    String.format("Failed to rename file \"%s\" to \"%s\" due to an IO error!", fileJar.getAbsolutePath(), fileJarBK.getAbsolutePath())
            );
        }
        if (!fileJarPatched.renameTo(fileJar)) {
            throw new IOException(
                    String.format("Failed to rename file \"%s\" to \"%s\" due to an IO error!", fileJarPatched.getAbsolutePath(), fileJar.getAbsolutePath())
            );
        }
    }

    /**
     * @param dirMCP Directory of where the "mcp" folder is
     * @param versionMC Version of Minecraft - "1.6.4"
     */
    public static void runMCPDownloader(@NonNull final File dirMCP, @NonNull final String versionMC, final boolean isForge) {
        List<String> arguments = new ArrayList<>();

        arguments.add("--mcVersion");
        arguments.add(versionMC);

        arguments.add("--mcp");
        arguments.add(dirMCP.getAbsolutePath());

        arguments.add("--jars");
        arguments.add("--client");
        arguments.add("--server");

        arguments.add("--libraries");

        arguments.add("--natives");
        arguments.add("--windows");
        arguments.add("--linux");

        arguments.add("--resources");

        arguments.add("--local-assets");

        if (isForge) arguments.add("--forge");
        Main.main(arguments.toArray(new String[0]));
    }

    public static void runForgeInstaller(@NonNull final File dirForge, @NonNull final String versionMC, final boolean isLegacy) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("./install.cmd", "--decompile");
        processBuilder.directory(dirForge);
        processBuilder.inheritIO();
//        List<String> arguments = new ArrayList<>();
//        arguments.add("install.cmd");
//        arguments.add("--decompile");
//        if (versionMC.indexOf(3) >= '6') arguments.add("--no-assets");//1.6 versions use the --no-assets argument
//        processBuilder.command("install.cmd");

        Process process = processBuilder.start();
        process.waitFor(3, TimeUnit.MINUTES);
        if (process.exitValue() != 0) throw new RuntimeException("Failed to run Forge installer!");
    }

}
