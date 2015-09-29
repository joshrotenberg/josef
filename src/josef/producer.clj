(ns josef.producer
  (:require [josef.util :refer [map->Properties]])
  (:import [org.apache.kafka.clients.producer KafkaProducer ProducerRecord Callback]
           [org.apache.kafka.common.serialization Serializer ByteArraySerializer StringSerializer]))

(defn producer
  "Instantiate a Kafka producer. An initial bootstrap server is required. Uses the ByteArraySerializers by default."
  [servers & {:keys [key-serializer
                     value-serializer]
              :or {key-serializer (ByteArraySerializer.)
                   value-serializer (ByteArraySerializer.)}
              :as args}]
  (-> (assoc args "bootstrap.servers" servers)
      map->Properties
      (KafkaProducer. key-serializer value-serializer)))

(defn send!
  "Send a record to Kafka. Requires a producer, topic and value, with optional partition, key and callback options."
  [producer topic value & {:keys [partition
                                  key
                                  on-success
                                  on-error]
                           :or {on-success (constantly nil)
                                on-error (constantly nil)}}]
  (.send producer (ProducerRecord. topic partition key value)
         (reify Callback
           (onCompletion [this m e]
             (if (nil? e)
               (on-success (.topic m) (.offset m) (.partition m))
               (on-error e))))))
