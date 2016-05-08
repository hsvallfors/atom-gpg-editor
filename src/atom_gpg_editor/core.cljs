(ns atom-gpg-editor.core)

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
  (atom-confirm! "You will save a text buffer!"))

(defn created-text-editor
  [editor]
  (atom-confirm! "You opened a file!")
  (add-observer! (.. editor getBuffer (onWillSave will-save-buffer))))

(defn activate
  []
  (atom-confirm! "Hello World!")
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
