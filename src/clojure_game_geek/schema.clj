(ns clojure-game-geek.schema
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component]
            [clojure-game-geek.db :as db]))

(defn entity-map
  [data k]
  (zipmap (map :id (get data k)) (get data k)))

(defn rating-summary
  [db]
  (fn [_ _ board-game]
    (let [ratings (->> (db/list-ratings-for-game db (:id board-game))
                       (map :rating))
          n (count ratings)]
      {:count n
       :average (if (zero? n)
                  0
                  (/ (reduce + ratings) n))})))

(defn member-ratings
  [db]
  (fn [_ _ member]
    (db/list-ratings-for-member db (:id member))))

(defn game-rating->game
  [db]
  (fn [_ _ game-rating]
    (db/find-game-by-id db (:game_id game-rating))))

(defn game-by-id
  [db]
  (fn [_ args _]
    (db/find-game-by-id db (:id args))))

(defn member-by-id
  [db]
  (fn [_ args _]
    (db/find-member-by-id db (:id args))))

(defn board-game-designers
  [db]
  (fn [_ _ board-game]
    (db/list-designers-for-game db (:id board-game))))

(defn designer-games
  [db]
  (fn [_ _ designer]
    (db/list-games-for-designer db (:id designer))))

(defn resolver-map
  [component]
  (let [db (:db component)
        cgg-data (-> (io/resource "cgg-data.edn")
                     slurp
                     edn/read-string)
        games-map (entity-map cgg-data :games)
        members-map (entity-map cgg-data :members)
        designers-map (entity-map cgg-data :designers)]
    {:query/game-by-id (game-by-id db)
     :query/member-by-id (member-by-id db)
     :BoardGame/designers (board-game-designers db)
     :BoardGame/rating-summary (rating-summary db)
     :GameRating/game (game-rating->game db)
     :Designer/games (designer-games db)
     :Member/ratings (member-ratings db)}))

(defn load-schema
  [component]
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      schema/compile))

(defrecord SchemaProvider [schema]
  component/Lifecycle
  (start [this]
    (assoc this :schema (load-schema this)))
  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (-> (map->SchemaProvider {})
                        (component/using [:db]))})
