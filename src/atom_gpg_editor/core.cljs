(ns atom-gpg-editor.core
  (:require
    [atom-gpg-editor.child_process :as child]
    [clojure.string :as string]))

(def state
  (atom
     {:created-text-editor-obs nil}))

(defn atom-info!
  [message description]
  (.addInfo js/atom.notifications message (clj->js {:dismissable true :description description})))

(defn atom-error!
  [message detail]
  (.addError js/atom.notifications message (clj->js {:dismissable true :detail detail})))

(defn get-gpg-recipients!
  [path]
  (let [short-sha-regexp #", ID ([A-Z\d]+),"
        gpg-result
          (child/spawn! "gpg"
            ["--list-only" "--no-default-keyring" "--batch" "--yes" "--secret-keyring" "/dev/null" path])]
    (if (and (:success? gpg-result) (not (string/blank? (:stderr gpg-result))))
      (->> gpg-result :stderr (re-seq short-sha-regexp) (map second) (into []))
      (do
        (atom-error!
          "GPG file recipients could not be fetched"
          (:stderr gpg-result))
        nil))))

(defn encrypt-file!
  [editor]
  (if-let [recipients (get-gpg-recipients! (.getPath editor))]
    (let
        [gpg-result
          (->>
            recipients
            (interpose "--recipient")
            (cons "--recipient")
            (concat ["--encrypt" "--output" (.getPath editor) "--trust-model" "always" "--batch" "--yes"])
            (into [])
            (child/spawn-with-stdin! "gpg" (.getText editor)))]
      (if (:success? gpg-result)
        (atom-info! "File encrypted!" "Your file was encrypted and saved succcessfully.")
        (atom-error! "GPG encrypt could not be run" (:stderr gpg-result)))
      (.destroy editor))))

(defn decrypt-file!
  [editor]
  (let [gpg-result (child/spawn! "gpg" ["--decrypt" "--batch" "--yes" (.getPath editor)])]
    (if (:success? gpg-result)
      (do
        (.setText editor (:stdout gpg-result))
        ; Prevent Atom from asking if you want to save tab contents
        (set! (.-isModified (.getBuffer editor)) (constantly false))
        (atom-info! "File decrypted!" "You are editing the plain text contents of your file. It will be encrypted before being saved."))
      (atom-error! "GPG decrypt could not be run" (:stderr gpg-result)))))

(defn is-gpg-file?
  [editor]
  (if-let [path (.getPath editor)]
    (some #(string/ends-with? path %) ["gpg" "pgp"])))

(defn created-text-editor
  [editor]
  (when (is-gpg-file? editor)
    (decrypt-file! editor)))

(defn save-hook
  [event]
  (if-let [editor (js/atom.workspace.getActivePaneItem)]
    ; Listen to file save events and hijack the flow if it is a GPG file.
    (when (is-gpg-file? editor)
      (.preventDefault event)
      (.stopPropagation event)
      (encrypt-file! editor))))

(defn activate
  []
  (swap! state assoc
    :created-text-editor-obs
    (js/atom.workspace.observeTextEditors created-text-editor))
  (js/atom.commands.add "atom-workspace" "core:save" save-hook))

(defn deactivate
  []
  (.dispose (:created-text-editor-obs @state)))

(set! js/module.exports
  (clj->js
    {:activate activate
     :deactivate deactivate
     :serialize (constantly nil)}))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))

;; debug printing
(enable-console-print!)
