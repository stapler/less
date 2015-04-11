package org.kohsuke.stapler.less;

import org.kohsuke.stapler.assets.AssetLoader;
import org.kohsuke.stapler.framework.io.IOException2;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
    public URL load(String path) throws IOException {
        if (!path.endsWith(".less.css"))
            return null;

        File cache = getCache(path);

        // if the cache is stale, regenerate it.
        // to prevent double compilation, lock by the request path
        synchronized (path.intern()) {
            if (!cache.exists() || cache.lastModified() != timestamp) {
                cache.getParentFile().mkdirs();
                String src = path.substring(0, path.length() - 4);
                try {
                    COMPILER.get().compile(new LessSource(new ClasspathSource(cl, src)), cache);
                } catch (LessException e) {
                    throw new IOException("Failed to compile LESS to CSS: "+path,e);
                }
                cache.setLastModified(timestamp);
                compileCount++;
            }
        }

        return cache.toURL();
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
