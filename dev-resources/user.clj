(ns user
  (:require [clojure-game-geek.schema :as s]
            [com.walmartlabs.lacinia :as lacinia]
            [clojure.walk :as walk])
  (:import (clojure.lang IPersistentMap)))

(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk
    (fn [node]
      (cond (instance? IPersistentMap node) (into {} node)

            (seq? node) (vec node)

            :else node))
    m))

(defn q
  [query-string]
  (simplify (lacinia/execute (s/load-schema) query-string nil nil)))
