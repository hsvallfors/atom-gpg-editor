(ns atom-gpg-editor.child_process
  (:require [cljs.nodejs :as nodejs]))

(def ^:private spawnSync (.-spawnSync (nodejs/require "child_process")))

(defn- parse-process-object
  [process-object]
  {:stdout (-> process-object .-stdout .toString)
   :stderr (-> process-object .-stderr .toString)
   :status (.-status process-object)
   :success? (-> process-object .-status (= 0))})

(defn spawn!
  [command args input]
  (parse-process-object
    (spawnSync command (clj->js args) (clj->js {:input input}))))
