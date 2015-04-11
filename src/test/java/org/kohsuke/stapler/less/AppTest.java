package org.kohsuke.stapler.less;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.test.JettyTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Unit test for simple App.
 */
public class AppTest extends JettyTestCase {
    public LessServer more;

    public void testExclude() throws Exception {
        File cache = File.createTempFile("cache", null);
        cache.delete();
        cache.mkdirs();

        more = new LessServer(cache,getClass().getClassLoader());

        URL url = new URL(this.url, "/more/pkg/foo.less.css");
        assertThatWeFindExpectedContents(url);
        assertEquals(1, more.compileCount);

        // 2nd time around, the cache should have hit
        assertThatWeFindExpectedContents(url);
        assertEquals(1, more.compileCount);

        // delete the cache and it should get recompiled
        FileUtils.deleteDirectory(cache);
        assertThatWeFindExpectedContents(url);
        assertEquals(2, more.compileCount);

        // mess with the timestamp and it should get recompiled again
        new File(cache,"pkg/foo.less.css").setLastModified(System.currentTimeMillis()+50000);
        assertThatWeFindExpectedContents(url);
        assertEquals(3, more.compileCount);
    }

    private void assertThatWeFindExpectedContents(URL url) throws IOException {
        assertTrue(load(url).contains("color: red"));
    }

    private String load(URL url) throws IOException {
        return IOUtils.toString(url.openStream());
    }

}
