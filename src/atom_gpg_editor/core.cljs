(ns atom-gpg-editor.core
  (:require [clojure.string :as string]
            [cljs.nodejs :as nodejs]))

(defn parse-process-object
  [process-object]
  {:stdout (-> process-object .-stdout .toString)
   :stderr (-> process-object .-stderr .toString)
   :status (.-status process-object)
   :success? (-> process-object .-status (= 0))})

(def spawn! (.-spawnSync (nodejs/require "child_process")))

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
  (-> (spawn! "date") parse-process-object pr-str atom-confirm!))

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
