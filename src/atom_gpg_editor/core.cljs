(ns atom-gpg-editor.core
  (:require
    [atom-gpg-editor.child_process :as child]
    [clojure.string :as string]
    [hipo.core :as hipo]
    [dommy.core :as dommy]))

(def state
  (atom
    {:panel nil
     :mini-editor nil
     :will-save-buffer-obs nil
     :created-text-editor-obs nil
     :gpg-password nil}))

(defn atom-confirm!
  [message]
  (.confirm js/atom (clj->js {:message message})))

(defn will-save-buffer
  [text-buffer]
  (->
    (child/spawn! "gpg" ["--help"])
    :stdout string/split-lines first atom-confirm!))

(defn save-password!
  []
  (swap! state
    (fn [state]
      (if (:mini-editor state)
        (assoc state :gpg-password (-> state :mini-editor .getText))
        state))))

(defn fill-password!
  []
  (swap! state
    (fn [state]
      (when (and (:mini-editor state) (:gpg-password state))
        (.setText (:mini-editor state) (:gpg-password state)))
      state)))

(defn dispose-panel!
  []
  (save-password!)
  (swap! state
    (fn [state]
      (if-not (:panel state)
        state
        (do
          (.destroy (:panel state))
          (.dispose (:will-save-buffer-obs state))
          (assoc state
            :panel nil
            :mini-editor nil
            :will-save-buffer-obs nil))))))

(defn add-panel!
  [editor]
  (let [mini-editor
          (-> {:mini true :placeholderText "GPG Password"}
              clj->js
              js/atom.workspace.buildTextEditor)
        panel-div (hipo/create [:div.atom-gpg-editor])]
    (dommy/append! panel-div (js/atom.views.getView mini-editor))
    ; FIXME hide password input
    (dispose-panel!)
    (swap! state assoc
      :mini-editor mini-editor
      :will-save-buffer-obs (.. editor getBuffer (onWillSave will-save-buffer))
      :panel (->> {:item panel-div} clj->js js/atom.workspace.addModalPanel))
    (fill-password!)
    (js/atom.commands.add panel-div "core:confirm" save-password!)
    (js/atom.commands.add panel-div "core:cancel" dispose-panel!)))

(defn created-text-editor
  [editor]
  (if-let [path (.getPath editor)]
    (when (string/ends-with? path ".gpg")
      (add-panel! editor))))

(defn activate
  []
  (swap! state assoc
    :created-text-editor-obs
    (js/atom.workspace.observeTextEditors created-text-editor)))

(defn deactivate
  []
  (.dispose (:created-text-editor-obs @state))
  (dispose-panel!))

(set! js/module.exports
  (clj->js
    {:activate activate
     :deactivate deactivate
     :serialize (constantly nil)}))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
