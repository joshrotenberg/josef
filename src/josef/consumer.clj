(ns josef.consumer
  (:import [java.util Properties])
  (:import [kafka.consumer ConsumerConfig Consumer KafkaStream Whitelist]
           [kafka.javaapi.consumer ConsumerConnector]
           [kafka.serializer DefaultDecoder])
  (:require [josef.util :refer [map->Properties]]))

(defn consumer
  "Returns a Kafka consumer pointed at zookeeper with the given consumer group id."
  [zookeeper group-id & {:keys [auto-offset-reset
                                auto-commit-interval-ms
                                auto-commit-enable]
                         :as args}]
  (->> (assoc args "zookeeper.connect" zookeeper "group.id" group-id) ;; map of all the args
       map->Properties ;; create a Properties instance
       (ConsumerConfig.) ;; create a config
       (Consumer/createJavaConsumerConnector))) ;; and create a consumer

(defn shutdown-consumer
  "Convenience for .shutdown on the Kafka consumer."
  [con]
  (.shutdown con))

(defn message-streams
  "Creates the specified number of streams on the topic pattern and returns a collection of streams."
  [consumer topic num-streams & {:keys [key-decoder
                                        value-decoder]
                                 :or {key-decoder (DefaultDecoder. nil)
                                      value-decoder (DefaultDecoder. nil)}}]
  (.createMessageStreamsByFilter consumer (Whitelist. topic) num-streams key-decoder value-decoder))

(defn message-stream
  "Like message-streams but only creates a single stream for the topic pattern."
  [consumer topic & {:keys [key-decoder
                            value-decoder]
                     :or {key-decoder (DefaultDecoder. nil)
                          value-decoder (DefaultDecoder. nil)}
                     :as decoder-args}]
  (first (apply message-streams consumer topic 1 decoder-args)))


(defn process-streams!
  "Process mulitple streams on the given topic pattern with the given xform, passing the final result to the rf
  function (presumably for side effects)."
  [consumer topic num-streams xf rf & {:keys [key-decoder
                                              value-decoder]
                                       :or {key-decoder (DefaultDecoder. nil)
                                            value-decoder (DefaultDecoder. nil)}
                                       :as decoder-args}]
  (let [agents (->> (apply message-streams consumer topic num-streams decoder-args)
                    (map #(agent %)))]
    (try
      (doseq [a agents] (send-off a #(run! rf (eduction xf %))))
      (apply await agents)
      (finally
        (shutdown-consumer consumer)))))

(defn process-stream!
  "Like process-streams! but only creates a single stream."
  [consumer topic xf rf & {:keys [key-decoder
                                  value-decoder]
                           :or {key-decoder (DefaultDecoder. nil)
                                value-decoder (DefaultDecoder. nil)}
                           :as decoder-args}]
  (apply process-streams! consumer topic 1 xf rf decoder-args))

