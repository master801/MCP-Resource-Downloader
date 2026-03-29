package org.slave.mcprd.cmd.test.forge;

import org.junit.Before;
import org.junit.Test;
import org.slave.mcprd.cmd.test.Constants;
import org.slave.mcprd.cmd.test.Runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestForge {

    @Before
    public void testFileCompetency() throws FileNotFoundException {
        Constants.testFileCompetency(Constants.FILE_JAR_FERNFLOWER);

        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_1);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_2_3);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_2_5);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_4_5);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_4_7);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_5_2);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_6_2);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_6_4);

        Constants.testFileCompetency(Constants.FILE_MODLOADER_ZIP_1_1);
        Constants.testFileCompetency(Constants.FILE_MODLOADER_ZIP_1_2_3);

        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_1);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_2_3);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_2_5);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_4_5);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_4_7);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_5_2);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_6_2);
        Constants.testFileCompetency(Constants.FILE_FORGE_DEV_ZIP_1_6_4);

        System.out.printf("File competency seems to be okay%s%n", System.lineSeparator());
    }

    @Test
    public void forge1_1() throws IOException {
        final String versionMC = "1.1";
        File dirDest = new File("Test/Forge/" + versionMC);
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_1, Constants.FILE_FORGE_DEV_ZIP_1_1, dirDest, true);
        Runtime.runMCPDownloader(dirDest, versionMC, true);
        Runtime.setUpModLoaderForForge(dirDest, Constants.FILE_MODLOADER_ZIP_1_1);
    }

    @Test
    public void forge1_2_3() throws IOException {
        final String versionMC = "1.2.3";
        File dirDest = new File("Test/Forge/" + versionMC);
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_2_3, Constants.FILE_FORGE_DEV_ZIP_1_2_3, dirDest, true);
        Runtime.runMCPDownloader(dirDest, versionMC, true);
        Runtime.setUpModLoaderForForge(dirDest, Constants.FILE_MODLOADER_ZIP_1_2_3);
    }

    @Test
    public void forge1_2_5() throws IOException {
        final String versionMC = "1.2.5";
        File dirDest = new File("Test/Forge/" + versionMC);
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_2_5, Constants.FILE_FORGE_DEV_ZIP_1_2_5, dirDest, true);
        Runtime.runMCPDownloader(dirDest, versionMC, true);
    }

    @Test
    public void forge1_3_2() throws IOException {
        final String versionMC = "1.3.2";
        File dirDest = new File("Test/Forge/" + versionMC);
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_3_2, Constants.FILE_FORGE_DEV_ZIP_1_3_2, dirDest, true);
        Runtime.runMCPDownloader(dirDest, versionMC, true);
    }

    @Test
    public void forge1_4_5() throws IOException {
        final String versionMC = "1.4.5";
        File dirDest = new File("Test/Forge/" + versionMC);
        File dirForge = new File(dirDest, "forge");
        File dirMCP = new File(dirForge, "mcp");
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_4_5, Constants.FILE_FORGE_DEV_ZIP_1_4_5, dirDest, false);
        Runtime.runMCPDownloader(dirMCP, versionMC, true);
    }

    @Test
    public void forge1_4_7() throws IOException {
        final String versionMC = "1.4.7";
        File dirDest = new File("Test/Forge/" + versionMC);
        File dirForge = new File(dirDest, "forge");
        File dirMCP = new File(dirForge, "mcp");
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_4_7, Constants.FILE_FORGE_DEV_ZIP_1_4_7, dirDest, false);
        Runtime.runMCPDownloader(dirMCP, versionMC, true);
    }

    @Test
    public void forge1_5_2() throws IOException, InterruptedException {
        final String versionMC = "1.5.2";
        File dirDest = new File("Test/Forge/" + versionMC);
        File dirForge = new File(dirDest, "forge");
        File dirMCP = new File(dirForge, "mcp");
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_5_2, Constants.FILE_FORGE_DEV_ZIP_1_5_2, dirDest, false);
        Runtime.runMCPDownloader(dirMCP, versionMC, true);
//        Runtime.runForgeInstaller(dirForge, versionMC, false);
    }

    @Test
    public void forge1_6_2() throws IOException, InterruptedException {
        final String versionMC = "1.6.2";
        File dirDest = new File("Test/Forge/" + versionMC);
        File dirForge = new File(dirDest, "forge");
        File dirMCP = new File(dirForge, "mcp");
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_6_2, Constants.FILE_FORGE_DEV_ZIP_1_6_2, dirDest, false);
        Runtime.runMCPDownloader(dirMCP, versionMC, true);
//        Runtime.runForgeInstaller(dirForge, versionMC, false);
    }

    @Test
    public void forge1_6_4() throws IOException, InterruptedException {
        final String versionMC = "1.6.4";
        File dirDest = new File("Test/Forge/1.6.4");
        File dirForge = new File(dirDest, "forge");
        File dirMCP = new File(dirForge, "mcp");
        Runtime.setUpMCPFiles(Constants.FILE_MCP_ZIP_1_6_4, Constants.FILE_FORGE_DEV_ZIP_1_6_4, dirDest, false);
        Runtime.runMCPDownloader(dirMCP, versionMC, true);
//        Runtime.runForgeInstaller(dirForge, versionMC, false);//FIXME
    }

}
