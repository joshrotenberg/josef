(ns josef.consumer-test
  (:require [clojure.test :refer :all]
            [josef.consumer :refer :all])
  (:import [kafka.message Message MessageAndMetadata]
           kafka.serializer.DefaultDecoder))

(deftest consumer-test
  (testing "consuming a stream"
    (let [d (DefaultDecoder. nil)
          m (Message. (.getBytes "foo"))
          md (MessageAndMetadata. "bar" 0 m 1 d d)]
;      MessageAndMetadata (topic = num.toString, partition = num, offset = num.toLong, rawMessage = rawMessage, keyDecoder = decoder, valueDecoder = decoder)
      
      (println md)
      (is (= 1 1)))))
