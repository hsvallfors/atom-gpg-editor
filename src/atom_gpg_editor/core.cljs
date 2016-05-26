(ns atom-gpg-editor.core
  (:require
    [atom-gpg-editor.child_process :as child]
    [clojure.string :as string]))

(def observers
  (atom []))

(defn add-observer!
  [obs]
  (swap! observers conj obs))

(defn dispose-observers!
  []
  (doall (map #(.dispose %) @observers)))

(defn atom-confirm!
  [message]
  (.confirm js/atom (js-obj "message" message)))

(defn will-save-buffer
  [text-buffer]
  (->
    (child/spawn! "gpg" ["--help"])
    :stdout string/split-lines first atom-confirm!))

(defn created-text-editor
  [editor]
  (when (string/ends-with? (.getPath editor) ".gpg")
    (atom-confirm! "You opened a GPG file!")
    (add-observer! (.. editor getBuffer (onWillSave will-save-buffer)))))

(defn activate
  []
  (add-observer! (js/atom.workspace.observeTextEditors created-text-editor)))

(defn deactivate
  []
  (dispose-observers!))

(set! js/module.exports
  (js-obj "activate" activate
          "deactivate" deactivate
          "serialize" (constantly nil)))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
