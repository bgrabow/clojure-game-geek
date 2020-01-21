(ns clojure-game-geek.system
  (:require [com.stuartsierra.component :as component]
            [clojure-game-geek.server :as server]
            [clojure-game-geek.schema :as schema]))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)
         (schema/new-schema-provider)))
