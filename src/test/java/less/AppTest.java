package less;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.test.JettyTestCase;

import java.io.File;
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

        String contents = IOUtils.toString(new URL(url, "/more/pkg/foo.less.css").openStream());
        System.out.println(contents);

        FileUtils.deleteDirectory(cache);
    }

}
