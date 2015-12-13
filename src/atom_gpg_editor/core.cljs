(ns atom-gpg-editor.core
    (:require [dommy.core :as dommy :refer-macros [sel1]]
              [hipo.core :as hipo]))

(defn activate
  []
  (dommy/append!
    (sel1 :body)
    (hipo/create [:div#hello-world "Hello World!"])))

(defn deactivate
  []
  (dommy/remove! (sel1 :#hello-world)))

(set! js/module.exports
  (js-obj "activate" activate
          "deactivate" deactivate
          "serialize" (constantly nil)))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly nil))
