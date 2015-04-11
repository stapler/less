# On-the-fly LESS compiler

This library defines an object you can bound in the URL space (via Stapler) to compile LESS files
from classpath and serve them as CSS files.

In this way, modular web applications can compose their LESS files at runtime to enable late binding.

This library relies on [lesscss-java](https://github.com/marceloverdijk/lesscss-java), which in turn uses
Rhino to run the original version of the less compiler. lesscss-java is in many ways poorly implemented,
so if this concept proves to be successful, we need to significantly refactor lesscss-java.