(ns josef.consumer
  (:require [clj-kafka.consumer.zk :as zk]))

(defn get-consumer
  "docs"
  [zookeepers group-id & {:keys []
                          :as args
                          :or []}]
  (let [config {"zookeeper.connect" zookeepers
                "group.id" group-id
                "auto.offset.reset" "largest"
                "auto.commit.enable" "false"}]
    (zk/consumer config)))

(defn process-streams
  "docs"
  ([consumer topic num-streams xform rf]
   (let [streams (process-streams consumer topic num-streams xform)]
     (doseq [s streams]
       (run! rf s))))
  ([consumer topic num-streams xform]
   (let [streams (zk/create-message-streams consumer {topic num-streams})]
     (mapv #(eduction xform %) (get streams topic)))))

(defn process-stream
  "docs"
  [consumer topic xform & rf]
  (if (count rf)
    (process-streams consumer topic 1 xform (first rf))
        (process-streams consumer topic 1 xform)))
