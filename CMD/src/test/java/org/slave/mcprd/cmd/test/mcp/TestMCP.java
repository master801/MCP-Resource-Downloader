package org.slave.mcprd.cmd.test.mcp;

import org.junit.Before;
import org.slave.mcprd.cmd.test.Constants;

import java.io.FileNotFoundException;

public class TestMCP {

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
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_7_10);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_8);
        Constants.testFileCompetency(Constants.FILE_MCP_ZIP_1_12);

        System.out.printf("File competency seems to be okay%s%n", System.lineSeparator());
    }

}
