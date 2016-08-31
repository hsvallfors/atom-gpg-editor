(ns atom-gpg-editor.core
  (:require
    [atom-gpg-editor.child_process :as child]
    [clojure.string :as string]))

(def state
  (atom
     {:created-text-editor-obs nil
      :will-save-buffer-obs nil}))

(defn atom-confirm!
  [message]
  (.confirm js/atom (clj->js {:message message})))

(defn atom-error!
  [message detail]
  (.addError js/atom.notifications message (clj->js {:dismissable true :detail detail})))

(defn encrypt-file!
  [editor])

(defn decrypt-file!
  [editor]
  (let [gpg-result (child/spawn! "gpg" ["--decrypt" (.getPath editor)])]
    (if (:success? gpg-result)
      (.setText editor (:stdout gpg-result))
      (atom-error! "GPG decrypt could not be run" (:stderr gpg-result)))))

(defn created-text-editor
  [editor]
  (if-let [path (.getPath editor)]
    (when (string/ends-with? path ".gpg")
      (swap! state assoc
        :will-save-buffer-obs
        (.. editor getBuffer (onWillSave #(decrypt-file! editor))))
      (decrypt-file! editor))))

(defn activate
  []
  (swap! state assoc
    :created-text-editor-obs
    (js/atom.workspace.observeTextEditors created-text-editor)))

(defn deactivate
  []
  (.dispose (:created-text-editor-obs @state))
  (.dispose (:will-save-buffer-obs @state)))

(set! js/module.exports
  (clj->js
    {:activate activate
     :deactivate deactivate
     :serialize (constantly nil)}))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
