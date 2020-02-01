(ns clojure-game-geek.pathom.parser
  (:require [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.connect :as pc]
            [clojure-game-geek.pathom.cgg-resolvers :as cgg-resolvers]
            [clojure.core.async :as a]
            [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]))

(def resolver-registry [cgg-resolvers/board-game-resolver
                        cgg-resolvers/designer-resolver
                        cgg-resolvers/board-game-rating-summary-resolver])

(def parser
  (p/parallel-parser
    {::p/env {::p/reader [p/map-reader
                          pc/parallel-reader
                          pc/open-ident-reader
                          p/env-placeholder-reader]
              ::p/placeholder-prefixes #{">"}}
     ::p/mutate pc/mutate-async
     ::p/plugins [(pc/connect-plugin
                    {::pc/register resolver-registry})
                  p/error-handler-plugin
                  p/trace-plugin]}))

(a/<!! (parser {:database {:data (-> (io/resource "cgg-data.edn")
                                     slurp
                                     edn/read-string
                                     atom)}}
               [{[:board-game/id "1234"]
                 [{:board-game/designers [:designer/name]}]}]))

(a/<!! (parser {:database {:data (-> (io/resource "cgg-data.edn")
                                     slurp
                                     edn/read-string
                                     atom)}}
               [{[:board-game/id "1234"]
                 [:game-rating-summary/count
                  :board-game/summary
                  :board-game/name
                  {:board-game/designers [:designer/name]}]}]))
