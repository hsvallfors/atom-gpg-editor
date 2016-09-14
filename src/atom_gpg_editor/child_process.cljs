(ns atom-gpg-editor.child_process
  (:require [cljs.nodejs :as nodejs]
            [debux.cs.core :refer-macros [clog]]))

(def ^:private spawnSync (.-spawnSync (nodejs/require "child_process")))

(defn- parse-process-object
  [process-object]
  {:stdout (-> process-object .-stdout .toString)
   :stderr (-> process-object .-stderr .toString)
   :success? (-> process-object .-status (= 0))})

(defn spawn!
  [command args]
  (parse-process-object (spawnSync command (clj->js args))))

(defn spawn-with-stdin!
  [command stdin args]
  (parse-process-object
    (spawnSync command (clj->js args) (clj->js {:input stdin}))))
