(defproject atom-gpg-editor "0.1.0"

  :description "An Atom plugin for reading and editing GPG files."

  :license {:name "MIT Licence"
            :url "https://github.com/keiter/atom-gpg-editor/blob/master/LICENSE.md"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [lein-cljsbuild "1.1.1"]
                 [philoskim/debux "0.2.1"]]

  :plugins [[lein-cljsbuild "1.1.1"]]

  :source-paths ["src"]
  :clean-targets [:output-dir]

  :cljsbuild
   {:builds
     {:main
       {:source-paths ["src"]
        :compiler
         {:output-dir "lib/"
          :output-to "lib/atom-gpg-editor.js"
          :source-map "lib/atom-gpg-editor.js.map"
          :optimizations :simple
          :target :nodejs
          :pretty-print true}}}})
