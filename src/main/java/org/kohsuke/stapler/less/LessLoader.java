package org.kohsuke.stapler.less;

import org.kohsuke.stapler.assets.Asset;
import org.kohsuke.stapler.assets.AssetLoader;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;

import java.io.File;
import java.io.IOException;

/**
 * {@link AssetLoader} that serves LESS as compiled CSS.
 *
 * <p>
 * Request {@code foo/bar.less.css} to serve compiled form of {@code foo/bar.less}
 *
 * @author Kohsuke Kawaguchi
 */
public class LessLoader extends AssetLoader {
    protected final ClassLoader cl;

    /**
     * Directory to store the generated CSS as cache.
     */
    protected final File cache;

    /**
     * Cache file we generate should have this timestamp, or else we need to regenerate the file.
     */
    private final long timestamp;

    /*package for test*/ int compileCount;

    public LessLoader(File cache, ClassLoader cl) {
        this.cl = cl;
        this.cache = cache;
        timestamp = (System.currentTimeMillis()/2000)*2000;
    }

    @Override
    public Asset load(String path) throws IOException {
        if (!path.endsWith(".less.css"))
            return null;

        File cache = getCache(path);

        // to prevent double compilation, lock by the request path
        synchronized (path.intern()) {
            File d = cache.getParentFile();
            d.mkdirs();
            File tmp = File.createTempFile("less","tmp",d);

            String src = path.substring(0, path.length() - 4);
            try {
                COMPILER.get().compile(new LessSource(new ClasspathSource(cl, src)), tmp);
            } catch (LessException e) {
                throw new IOException("Failed to compile LESS to CSS: "+path,e);
            }
            if (!tmp.renameTo(cache)) {
                cache.delete();
                tmp.renameTo(cache);
            }
            compileCount++;
        }

        return Asset.fromFile(cache);
    }

    private File getCache(String path) {
        if (path.startsWith("/"))   path=path.substring(1);
        return new File(cache,path);
    }

    private static final ThreadLocal<LessCompiler> COMPILER = new ThreadLocal<LessCompiler>() {
        @Override
        protected LessCompiler initialValue() {
            return new LessCompiler();
        }
    };
}
