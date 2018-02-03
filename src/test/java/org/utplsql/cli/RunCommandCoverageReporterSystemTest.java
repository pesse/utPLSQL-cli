package org.utplsql.cli;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.junit.Assert;
import org.junit.Test;
import org.utplsql.api.compatibility.OptionalFeatures;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * System tests for Code Coverage Reporter
 *
 * @author pesse
 */
public class RunCommandCoverageReporterSystemTest {

    private static final Pattern REGEX_COVERAGE_TITLE = Pattern.compile("<a href=\"[a-zA-Z0-9#]+\" class=\"src_link\" title=\"[a-zA-Z\\._]+\">([a-zA-Z0-9\\._]+)<\\/a>");

    private String getTempCoverageFileName(int counter) {

        return "tmpCoverage_" + String.valueOf(System.currentTimeMillis()) + "_" + String.valueOf(counter) + ".html";
    }

    /**
     * Returns a random filename which does not yet exist on the local path
     *
     * @return
     */
    private Path getTempCoverageFilePath() {
        int i = 1;
        Path p = Paths.get(getTempCoverageFileName(i));

        while (Files.exists(p) && i < 100)
            p = Paths.get(getTempCoverageFileName(i++));

        if (i >= 100)
            throw new IllegalStateException("Could not get temporary file for coverage output");

        return p;
    }

    /** Checks Coverage HTML Output if a given packageName is listed
     *
     * @param content
     * @param packageName
     * @return
     */
    private boolean hasCoverageListed( String content, String packageName) {
        Matcher m = REGEX_COVERAGE_TITLE.matcher(content);

        while ( m.find() ) {
            if ( packageName.equals(m.group(1)) )
                return true;
        }

        return false;
    }

    @Test
    public void run_CodeCoverageWithIncludeAndExclude() {

        try {
            Path coveragePath = getTempCoverageFilePath();

            RunCommand runCmd = RunCommandTestHelper.createRunCommand(RunCommandTestHelper.getConnectionString(),
                    "-f=ut_coverage_html_reporter", "-o=" + coveragePath, "-s", "-exclude=app.award_bonus,app.betwnstr");

            try {
                int result = runCmd.run();

                String content = new Scanner(coveragePath).useDelimiter("\\Z").next();

                Assert.assertEquals(true, hasCoverageListed(content, "app.remove_rooms_by_name"));
                Assert.assertEquals(false, hasCoverageListed(content, "app.award_bonus"));
                Assert.assertEquals(false, hasCoverageListed(content, "app.betwnstr"));

            } finally {
                Files.delete(coveragePath);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }
}