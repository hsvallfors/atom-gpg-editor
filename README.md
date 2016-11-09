# atom-gpg-editor

An Atom plugin for reading and editing GPG files.

FIXME: examples of usage

## Installation

Install Atom GPG Editor from the settings menu or `apm install atom-gpg-editor`.

To use the addon, you need `gpg` on your path.
You also need a GPG pin entry program. I presume that when my addon calls
`gpg --decrypt` or `gpg --encrypt`, you get prompted for your password graphically.
If this is not the case, the addon most likely won't be allowed to decrypt
GPG files and won't work.

## How to build / contribute to Atom GPG Editor

I accept PRs as long as you can explain your code and it's well structured.

Atom GPG editor is written in Clojurescript.
The only build time dependency (asides from the runtime dependencies)
is [leiningen](http://leiningen.org/).
Once you have that installed, run `lein cljsbuild auto` to auto-build the addon
on change.

## License

[MIT License](https://github.com/keiter/atom-gpg-editor/blob/master/LICENSE.md)
