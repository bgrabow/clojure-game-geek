(ns clojure-game-geek.db
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defrecord ClojureGameGeekDb [data]
  component/Lifecycle
  (start [this]
    (assoc this :data (-> (io/resource "cgg-data.edn")
                          slurp
                          edn/read-string
                          atom)))
  (stop [this]
    (assoc this :data nil)))

(defn new-db
  []
  {:db (map->ClojureGameGeekDb {})})

(defn find-game-by-id
  [db game-id]
  (->> @(:data db)
       :games
       (filter #(= game-id (:id %)))
       first))

(defn find-member-by-id
  [db member-id]
  (->> @(:data db)
       :members
       (filter #(= member-id (:id %)))
       first))

(defn list-designers-for-game
  [db game-id]
  (let [designers (:designers (find-game-by-id db game-id))]
    (->> @(:data db)
         :designers
         (filter #(contains? designers (:id %))))))

(defn list-games-for-designer
  [db designer-id]
  (->> @(:data db)
       :games
       (filter #(contains? (:designers %) designer-id))))

(defn list-ratings-for-game
  [db game-id]
  (->> @(:data db)
       :ratings
       (filter #(= game-id (:game_id %)))))

(defn list-ratings-for-member
  [db member-id]
  (->> @(:data db)
       :ratings
       (filter #(= member-id (:member_id %)))))

(defn ^:private apply-game-rating
  [game-ratings game-id member-id rating]
  (->> game-ratings
       (remove #(and (= game-id (:game_id %))
                     (= member-id (:member_id %))))
       (cons {:game_id game-id
              :member_id member-id
              :rating rating})))

(defn upsert-game-rating
  "Adds a new game rating, or changes the value of an existing game rating."
  [db game-id member-id rating]
  (swap! (:data db)
         update :ratings apply-game-rating game-id member-id rating))
