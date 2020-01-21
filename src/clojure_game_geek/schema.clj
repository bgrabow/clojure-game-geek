(ns clojure-game-geek.schema
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component]))

(defn resolve-element-by-id
  [elements-map context args value]
  (get elements-map (:id args)))

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

(defn rating-summary
  [cgg-data]
  (fn [_ _ board-game]
    (let [ratings (for [rating (:ratings cgg-data)
                        :when (= (:id board-game)
                                 (:game_id rating))]
                    (:rating rating))
          n (count ratings)]
      {:count n
       :average (if (zero? n)
                  0
                  (/ (reduce + ratings) n))})))


(defn member-ratings
  [ratings]
  (fn [_ _ member]
    (filter (fn [rating] (= (:id member) (:member_id rating)))
            ratings)))


(defn game-rating->game
  [games-map]
  (fn [_ _ game-rating]
    (get games-map (:game_id game-rating))))

(defn resolver-map
  []
  (let [cgg-data (-> (io/resource "cgg-data.edn")
                     slurp
                     edn/read-string)
        games-map (entity-map cgg-data :games)
        members-map (entity-map cgg-data :members)
        designers-map (entity-map cgg-data :designers)]
    {:query/game-by-id (partial resolve-element-by-id games-map)
     :query/member-by-id (partial resolve-element-by-id members-map)
     :BoardGame/designers (partial resolve-board-game-designers designers-map)
     :BoardGame/rating-summary (rating-summary cgg-data)
     :GameRating/game (game-rating->game games-map)
     :Designer/games (partial resolve-designer-games games-map)
     :Member/ratings (member-ratings (:ratings cgg-data))}))

(defn load-schema
  []
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
      schema/compile))

(defrecord SchemaProvider [schema]
  component/Lifecycle
  (start [this]
    (assoc this :schema (load-schema)))
  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (map->SchemaProvider {})})
