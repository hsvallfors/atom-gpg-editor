(ns atom-gpg-editor.core
    (:require [hipo.core :as hipo]))

(def panel (atom nil))

(defn remove-panel!
  []
  (do
    (.destroy @panel)
    (reset! panel nil)))

(defn add-panel!
  []
  (->>  (hipo/create [:div#hello-world "Hello World!"])
        (js-obj "item")
        (js/atom.workspace.addModalPanel)
        (reset! panel)))

(defn activate
  []
  (add-panel!))

(defn deactivate
  []
  (remove-panel!))

(set! js/module.exports
  (js-obj "activate" activate
          "deactivate" deactivate
          "serialize" (constantly nil)))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
