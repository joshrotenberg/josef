# josef

Boilerplate reduction for processing Kafka streams with Clojure Transducers

## Usage

[![Clojars Project](http://clojars.org/josef/latest-version.svg)](http://clojars.org/josef)

```clojure
(ns my.app
    (:require [josef.consumer :as j]))

(defn msg->String
  [m]
  (String. (.message m)))

(defn more-than-four
  [s]
  (< 4 (count s)))

(defn -main
  [& args]
  (let [cnsmr (j/consumer "localhost:2181" "whatever") ;; get a Kafka consumer
        topic-pattern "test.*"                         ;; specify all of our test topics
        xf (comp (map msg->String)                     ;; compose our processing transducer xform
                 (filter more-than-four)
                 (map clojure.string/upper-case)
		 (partition-all 5))                     ;; and process 5 at a time
        rf println]                                     ;; and println out the results
    (j/process-stream! cnsmr topic-pattern xf rf)))
```

## Overview

`josef` is a simple library for processing [Kafka](http://kafka.apache.org/) streams with Clojure
[transducers](http://clojure.org/transducers).

I had a bunch of similar but ultimately distinct filters that needed
to process the same Kafka topic. Clojure
transducers are a great way to
compose various pieces and build transformations and filters, but I
found myself writing the same boilerplate otherwise. After two, I
broke it out into a seprate, more general function. After a few more,
I figured it was time for a little library.

## License

Copyright © 2015 Josh Rotenberg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
