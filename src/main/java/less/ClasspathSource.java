package less;

import org.lesscss.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Kohsuke Kawaguchi
 */
public class ClasspathSource implements Resource {
    private final ClassLoader cl;
    private final String path;

    private final URL res;

    public ClasspathSource(ClassLoader cl, String path) {
        this.cl = cl;
        this.path = path;
        res = cl.getResource(path);
    }

    public boolean exists() {
        return res!=null;
    }

    public long lastModified() {
        try {
            return res.openConnection().getLastModified();
        } catch (IOException e) {
            return 0;
        }
    }

    public InputStream getInputStream() throws IOException {
        return res.openStream();
    }

    public Resource createRelative(String rel) throws IOException {
        return new ClasspathSource(cl,combine(path, rel));
    }

    private String combine(String base, String rel) {
        if (rel.startsWith("/"))        return rel;
        else                            return normalize(base+"/../"+rel);
    }

    public String getName() {
        return path;
    }

    /**
     * {@link File#getParent()} etc cannot handle ".." and "." in the path component very well,
     * so remove them.
     */
    private static String normalize(String path) {
        StringBuilder buf = new StringBuilder();
        // Split remaining path into tokens, trimming any duplicate or trailing separators
        List<String> tokens = new ArrayList<String>();
        int s = 0, end = path.length();
        for (int i = 0; i < end; i++) {
            char c = path.charAt(i);
            if (c == '/' || c == '\\') {
                tokens.add(path.substring(s, i));
                s = i;
                // Skip any extra separator chars
                while (++i < end && ((c = path.charAt(i)) == '/' || c == '\\')) { }
                // Add token for separator unless we reached the end
                if (i < end) tokens.add(path.substring(s, s+1));
                s = i;
            }
        }
        if (s < end) tokens.add(path.substring(s));
        // Look through tokens for "." or ".."
        for (int i = 0; i < tokens.size();) {
            String token = tokens.get(i);
            if (token.equals(".")) {
                tokens.remove(i);
                if (tokens.size() > 0)
                    tokens.remove(i > 0 ? i - 1 : i);
            } else if (token.equals("..")) {
                if (i == 0) {
                    // If absolute path, just remove: /../something
                    // If relative path, not collapsible so leave as-is
                    tokens.remove(0);
                    if (tokens.size() > 0) token += tokens.remove(0);
                    buf.append(token);
                } else {
                    // Normalize: remove something/.. plus separator before/after
                    i -= 2;
                    for (int j = 0; j < 3; j++) tokens.remove(i);
                    if (i > 0) tokens.remove(i-1);
                    else if (tokens.size() > 0) tokens.remove(0);
                }
            } else
                i += 2;
        }
        // Recombine tokens
        for (String token : tokens) buf.append(token);
        if (buf.length() == 0) buf.append('.');
        return buf.toString();
    }

    @Override
    public String toString() {
        return getClass().getName()+"["+path+"]";
    }
}
