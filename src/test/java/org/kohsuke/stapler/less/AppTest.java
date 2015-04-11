package org.kohsuke.stapler.less;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.assets.AssetsManager;
import org.kohsuke.stapler.test.JettyTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Unit test for simple App.
 */
public class AppTest extends JettyTestCase {
    public AssetsManager assets;
    private LessLoader more;
    private File cache;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = File.createTempFile("cache", null);
        cache.delete();
        cache.mkdirs();
        init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(cache);
    }

    public void testCompilation() throws Exception {
        URL url = new URL(this.url, "/assets/pkg/foo.less.css");
        assertThatWeFindExpectedContents(url);
        assertEquals(1, more.compileCount);

        // 2nd time around, the cache should have hit
        assertThatWeFindExpectedContents(url);
        assertEquals(1, more.compileCount);

        // delete the cache and it should get recompiled
        FileUtils.deleteDirectory(cache);
        assertThatWeFindExpectedContents(url);
        assertEquals(2, more.compileCount);

        // simulate the restart of the webapp that picks up the same cache dir that
        // contains old cached files. It should get recompiled again
        new File(cache,"pkg/foo.less.css").setLastModified(System.currentTimeMillis() - 50000);
        assertThatWeFindExpectedContents(url);
        assertEquals(3, more.compileCount);
    }

    private void init() {
        more = new LessLoader(cache,getClass().getClassLoader());
        assets = new AssetsManager("/assets",0, more);
    }

    private void assertThatWeFindExpectedContents(URL url) throws IOException {
        assertTrue(load(url).contains("color: red"));
    }

    private String load(URL url) throws IOException {
        return IOUtils.toString(url.openStream());
    }

}
