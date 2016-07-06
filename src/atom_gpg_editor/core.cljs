(ns atom-gpg-editor.core
  (:require
    [atom-gpg-editor.child_process :as child]
    [clojure.string :as string]
    [hipo.core :as hipo]
    [dommy.core :as dommy]))

(def observers
  (atom []))

(def panel
  (atom nil))

(defn add-observer!
  [obs]
  (swap! observers conj obs))

(defn dispose-observers!
  []
  (doall (map #(.dispose %) @observers)))

(defn dispose-panel!
  []
  (when @panel
    (.destroy @panel)
    (reset! panel nil)))

(defn atom-confirm!
  [message]
  (.confirm js/atom (clj->js {:message message})))

(defn will-save-buffer
  [text-buffer]
  (->
    (child/spawn! "gpg" ["--help"])
    :stdout string/split-lines first atom-confirm!))

(def gpg-cancel dispose-panel!)

(defn gpg-confirm
  [mini-editor]
  (atom-confirm! (str "Password was " (.getText mini-editor))))

(defn created-text-editor
  [editor]
  (when (string/ends-with? (.getPath editor) ".gpg")
    (let [mini-editor (-> {:mini true :placeholderText "GPG Password"}
                          clj->js
                          js/atom.workspace.buildTextEditor)
          panel-div (hipo/create [:div.atom-gpg-editor])]
      (dommy/append! panel-div (js/atom.views.getView mini-editor))
      ; FIXME hide password input
      (->> {:item panel-div} clj->js js/atom.workspace.addModalPanel (reset! panel))
      (js/atom.commands.add panel-div "core:confirm" #(gpg-confirm mini-editor))
      (js/atom.commands.add panel-div "core:cancel" #(gpg-cancel))
      (add-observer! (.. editor getBuffer (onWillSave will-save-buffer))))))

(defn activate
  []
  (add-observer! (js/atom.workspace.observeTextEditors created-text-editor)))

(defn deactivate
  []
  (dispose-observers!)
  (dispose-panel!))

(set! js/module.exports
  (clj->js
    {:activate activate
     :deactivate deactivate
     :serialize (constantly nil)}))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
