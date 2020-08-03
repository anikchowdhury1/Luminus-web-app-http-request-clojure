(ns bibliotheque.routes.parallel
  (:require [clojure.core.async :as async]
            [taoensso.timbre :as log]))


(defn spawn-task-executor
  [executor-id data-chan task-fn]
  (async/thread
    ;(log/debug (format "[Executor %s] Spawning ..." executor-id))
    (loop [data (async/<!! data-chan)]
      (when data
        ;(log/debug (format "[Executor %s] Received data: %s" executor-id data))
        (task-fn data)
        (recur (async/<!! data-chan))))))
;(log/debug (format "[Executor %s] Done ..." executor-id))))


(defn spawn-task-executors
  [task-fn n-threads]
  (let [data-chan (async/chan (async/buffer (* 2 n-threads)))]
    (doseq [executor-id (range n-threads)]
      (spawn-task-executor executor-id data-chan task-fn))
    data-chan))