(ns clojure-game-geek.pathom.cgg-resolvers
  (:require [com.wsscode.pathom.connect :as pc]
            [clojure-game-geek.db :as db]))

(pc/defresolver board-game-resolver
  [{:keys [database]} {:keys [board-game/id]}]
  {::pc/input #{:board-game/id}
   ::pc/output [:board-game/name
                ;:board-game/rating_summary
                :board-game/summary
                :board-game/description
                {:board-game/designers [:designer/id]}
                :board-game/min_players
                :board-game/max_players
                :board-game/play_time]}
  (let [board-game (db/find-game-by-id database id)]
    {:board-game/name (:name board-game)
     :board-game/summary (:summary board-game)
     :board-game/description (:description board-game)
     :board-game/min_players (:min_players board-game)
     :board-game/max_players (:max_players board-game)
     :board-game/play_time (:play_time board-game)
     :board-game/designers (for [designer-id (:designers board-game)]
                             {:designer/id designer-id})}))

(pc/defresolver board-game-rating-summary-resolver
  [{:keys [database]} {:keys [board-game/id]}]
  {::pc/input #{:board-game/id}
   ::pc/output [:game-rating-summary/count
                :game-rating-summary/average]}
  (let [ratings (->> (db/list-ratings-for-game database id)
                     (map :rating))
        n (count ratings)]
    {:game-rating-summary/count n
     :game-rating-summary/average (if (zero? n)
                                    0
                                    (/ (reduce + ratings) n))}))

#_(pc/defresolver designers-resolver
    [{} {:keys [board-game/designers]}]
    {::pc/input #{:board-game/designers}
     ::pc/output [{:board-game/designers [:designer/id]}]}
    {:board-game-designers designers})

(pc/defresolver designer-resolver
  [{:keys [database]} {:keys [designer/id]}]
  {::pc/input  #{:designer/id}
   ::pc/output [:designer/url
                :designer/name]}
  (let [designer (db/find-designer-by-id database id)]
    {:designer/url (:url designer)
     :designer/name (:name designer)}))
