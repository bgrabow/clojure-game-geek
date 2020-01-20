(ns clojure-game-geek.schema
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.edn :as edn]))

(defn resolve-game-by-id
  [games-map context args value]
  (get games-map (:id args)))

(defn resolve-board-game-designers
  [designers-map context args board-game]
  (map designers-map (:designers board-game)))

(defn resolve-designer-games
  [games-map context args designer]
  (filter (fn [game] (contains? (:designers game) (:id designer)))
          (vals games-map)))

(defn entity-map
  [data k]
  (zipmap (map :id (get data k)) (get data k)))

(defn resolver-map
  []
  (let [cgg-data (-> (io/resource "cgg-data.edn")
                     slurp
                     edn/read-string)
        games-map (entity-map cgg-data :games)
        designers-map (entity-map cgg-data :designers)]
    {:query/game-by-id (partial resolve-game-by-id games-map)
     :BoardGame/designers (partial resolve-board-game-designers designers-map)
     :Designer/games (partial resolve-designer-games games-map)}))

(defn load-schema
  []
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
      schema/compile))
