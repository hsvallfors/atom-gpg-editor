(ns atom-gpg-editor.core
  (:require [hipo.core :as hipo]))

(def observers
  (atom ()))

(defn atom-confirm!
  [message]
  (.confirm js/atom (js-obj "message" message)))

(defn text-editor-observer
  [editor]
  (atom-confirm! "You opened a file!"))

(defn activate
  []
  (atom-confirm! "Hello World!")
  (swap! observers conj (js/atom.workspace.observeTextEditors text-editor-observer)))

(defn deactivate
  []
  (doall (map #(.dispose %) @observers)))

(set! js/module.exports
  (js-obj "activate" activate
          "deactivate" deactivate
          "serialize" (constantly nil)))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
