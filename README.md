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

## Streams

Kafka streams are Iterable, which allows Clojure transducers to slice
and dice them the same way you might use any other Clojure
collection. `josef` wraps up some of the details and allows you to
call a single function to create a long running Kafka processor with
minimal effort. I've also included a simplistic Kafka Producer
implementation as consuming and then producing some result is a fairly
common paradigm. If you need more control over the producer, however,
please see [Paul Ingle](https://github.com/pingles)'s much more
complete [clj-kafka](https://github.com/pingles/clj-kafka) (without which `josef` would not have been possible). See the example application for a usage example of the included producer.

As a bonus, if for some reason you'd like access to the Kafka message
streams themselves for other reasons, `josef` also exposes the
functions used by `process-stream!` to access the raw Kafka message
stream instances.

`josef` is named after Josef K. in Franz Kafka's "The Trial", or, it's original title in German, "Der Process" ... witty, no? Yeah, a bit of a stretch.

## Status

`josef` has been an idea for a few months now but has only been real code for a couple of weeks. There are almost certainly bugs, inconsistencies in the API, and spots where performance can be improved (type hints, etc). Use with caution. Suggestions and pull requests are welcome.

## TODO

- Tests

## License

Copyright © 2015 Josh Rotenberg

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
