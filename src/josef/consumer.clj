(ns josef.consumer
  (:require [clj-kafka.consumer.zk :as zk]))

(defn get-consumer
  "Takes a comma separated list of zookeeper host:ports and a consumer group id and returns a consumer."
  [zookeepers group-id & {:keys []
                          :as args
                          :or []}]
  (let [config {"zookeeper.connect" zookeepers
                "group.id" group-id
                "auto.offset.reset" "largest"
                "auto.commit.enable" "false"}]
    (zk/consumer config)))

(defn shutdown-consumer
  "Convenience for .shutdown on the Kafka consumer."
  [con]
  (.shutdown con))

(defn process-streams
  "Process multiple Kafka streams. Takes a consumer, a topic, the
  number of concurrent streams, an xform, and, optionally, a final
  function with side effects. The 4-arity version returns a vector of
  eductions suitable for processing however you want. The 5-arity
  version will call run! on each stream, useful for processing the
  resulting items when side effects are needed (i.e. update a
  database, write to another Kafka topic, etc)"
  ([consumer topic num-streams xform rf]
   (let [streams (process-streams consumer topic num-streams xform)]
     (doseq [s streams]
       (run! rf s))))
  ([consumer topic num-streams xform]
   (let [streams (zk/create-message-streams consumer {topic num-streams})]
     (mapv #(eduction xform %) (get streams topic)))))

(defn process-stream
  "Process a single stream. Convenience for process-streams with a single stream."
  [consumer topic xform & rf]
  (if (< 0 (count rf))
    (process-streams consumer topic 1 xform (first rf))
    (first (process-streams consumer topic 1 xform))))
