package org.kohsuke.stapler.less;

import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * Stapler-bound object that serves LESS files in CSS on the fly.
 *
 * @author Kohsuke Kawaguchi
 */
public class LessServer {
    private final ClassLoader cl;
    private final File cache;
    private final long timestamp = System.currentTimeMillis();

    public LessServer(File cache, ClassLoader cl) {
        this.cl = cl;
        this.cache = cache;
    }

    public void doDynamic(StaplerRequest req,StaplerResponse rsp) throws IOException, LessException, ServletException {
        String path = req.getRestOfPath();
        if (path.startsWith("/"))   path=path.substring(1);

        // to prevent arbitrary class file leak to the browser, don't serve files unless they are LESS files.
        if (!path.endsWith(".less.css"))
            throw HttpResponses.notFound();

        File cache = getCache(path);

        if (!cache.exists() || cache.lastModified()!=timestamp) {
            cache.getParentFile().mkdirs();
            String src = path.substring(0, path.length() - 4);
            COMPILER.get().compile(new LessSource(new ClasspathSource(cl, src)), cache);
        }

        long expires = MetaClass.NO_CACHE ? 0 : 24L * 60 * 60 * 1000; /*1 day*/

        if(getServletPath(req).startsWith("/static/"))
            expires*=365;   // static resources are unique, so we can set a long expiration date

        rsp.serveFile(req, cache.toURL(), expires);
    }

    private File getCache(String path) {
        if (path.startsWith("/"))   path=path.substring(1);
        return new File(cache,path);
    }

    private String getServletPath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }

    private static final ThreadLocal<LessCompiler> COMPILER = new ThreadLocal<LessCompiler>() {
        @Override
        protected LessCompiler initialValue() {
            return new LessCompiler();
        }
    };
}
