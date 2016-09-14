(ns atom-gpg-editor.core
  (:require
    [atom-gpg-editor.child_process :as child]
    [clojure.string :as string]
    [debux.cs.core :refer-macros [clog]]))

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

(defn get-gpg-recipients!
  [path]
  (let [short-sha-regexp #", ID ([A-Z\d]{8}),"
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
      (if-not (:success? gpg-result)
        (atom-error! "GPG encrypt could not be run" (:stderr gpg-result)))
      (.destroy editor)
      (throw (js/Error "aborting plaintext save")))))

(defn decrypt-file!
  [editor]
  (let [gpg-result (child/spawn! "gpg" ["--decrypt" "--batch" "--yes" (.getPath editor)])]
    (if (:success? gpg-result)
      (.setText editor (:stdout gpg-result))
      (atom-error! "GPG decrypt could not be run" (:stderr gpg-result)))))

(defn created-text-editor
  [editor]
  (if-let [path (.getPath editor)]
    (when (string/ends-with? path ".gpg")
      (swap! state assoc
        :will-save-buffer-obs
        (.. editor getBuffer (onWillSave #(encrypt-file! editor))))
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
