package org.slave.mcprd.cmd.test;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileNotFoundException;

@UtilityClass
public class Constants {

    //<editor-fold desc="MCP Zips">
    public static final File FILE_MCP_ZIP_1_1 = new File("D:\\Minecraft\\Dev\\mcp56.zip");
    public static final File FILE_MCP_ZIP_1_2_3 = new File("D:\\Minecraft\\Dev\\mcp60.zip");
    public static final File FILE_MCP_ZIP_1_2_5 = new File("D:\\Minecraft\\Dev\\mcp62.zip");
    public static final File FILE_MCP_ZIP_1_3_2 = new File("D:\\Minecraft\\Dev\\mcp72.zip");
    public static final File FILE_MCP_ZIP_1_4_5 = new File("D:\\Minecraft\\Dev\\mcp723.zip");
    public static final File FILE_MCP_ZIP_1_4_7 = new File("D:\\Minecraft\\Dev\\mcp726a.zip");
    public static final File FILE_MCP_ZIP_1_5_2 = new File("D:\\Minecraft\\Dev\\mcp751.zip");
    public static final File FILE_MCP_ZIP_1_6_2 = new File("D:\\Minecraft\\Dev\\mcp805.zip");
    public static final File FILE_MCP_ZIP_1_6_4 = new File("D:\\Minecraft\\Dev\\mcp811.zip");
    public static final File FILE_MCP_ZIP_1_7_10 = new File("D:\\Minecraft\\Dev\\mcp908.zip");
    public static final File FILE_MCP_ZIP_1_8 = new File("D:\\Minecraft\\Dev\\mcp910-pre1.zip");
    public static final File FILE_MCP_ZIP_1_12 = new File("D:\\Minecraft\\Dev\\mcp940.zip");
    //</editor-fold>

    //For 1.2.3 and older
    //<editor-fold desc="ModLoader Zips">
    public static final File FILE_MODLOADER_ZIP_1_1 = new File("D:\\Minecraft\\Dev\\Forge\\ModLoader 1.1.zip");
    public static final File FILE_MODLOADER_ZIP_1_2_3 = new File("D:\\Minecraft\\Dev\\Forge\\ModLoader 1.2.3.zip");
    //</editor-fold>

    //<editor-fold desc="Forge Dev Zips">
    public static final File FILE_FORGE_DEV_ZIP_1_1 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.1-1.3.4.29-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_2_3 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.2.3-1.4.1.64-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_2_5 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.2.5-3.4.9.171-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_3_2 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.3.2-4.3.5.318-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_4_5 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.4.5-6.4.2.448-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_4_7 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.4.7-6.6.2.534-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_5_2 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.5.2-7.8.1.738-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_6_2 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.6.2-9.10.1.871-src.zip");
    public static final File FILE_FORGE_DEV_ZIP_1_6_4 = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.6.4-9.11.1.1345-src.zip");
    //</editor-fold>

    //<editor-fold desc="Misc">
    public static final File FILE_JAR_FERNFLOWER = new File("D:\\Minecraft\\Dev\\Forge\\forge-1.2.5-3.4.9.171-src\\runtime\\bin\\fernflower.jar");
    //</editor-fold>

    public static void testFileCompetency(@NonNull final File file) throws FileNotFoundException {
        if (!file.exists()) throw new FileNotFoundException(String.format("Failed because file \"%s\" does not exist!", file.getName()));
    }

}
