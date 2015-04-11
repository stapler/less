# On-the-fly LESS compiler

This library defines an object you can bound in the URL space (via Stapler) to compile LESS files
from classpath and serve them as CSS files. This enables modular web applications to compose
their LESS files at runtime to enable late binding.

To use it, expose `LessServer` from your URL space (for example by binding it to a field of your "App" class.)

```
class App {
    public final LessServer less_css = new LessServer(...);
}
```

Then request to `/less_css/foo/bar/zot.less.css` would compile and render `foo/bar/zot.less` in your classpath.
To prevent leaking the class files and other potentially sensitive information inside the program, `LessServer`
only serves `*.less` files.

LESS compilation is very time consuming, so this library implements caching on disk.

This library relies on [lesscss-java](https://github.com/marceloverdijk/lesscss-java), which in turn uses
Rhino to run the original version of the less compiler. lesscss-java is in many ways poorly implemented,
so if this concept proves to be successful, we need to significantly refactor lesscss-java.