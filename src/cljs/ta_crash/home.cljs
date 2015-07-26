(ns ta-crash.home
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn search
  [search-term owner]
  (println "search: " search-term))

(defn search-bar
  [search-term owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "home-search-bar"}
        (dom/span #js {:className "home-search-text"} "I want to see crashes in ")
        (dom/input #js {:className "home-search-input" :ref "search-term"} )
        (dom/span #js {:className "home-search-text"} ".")
        (dom/button #js {:className "home-search-button" :onClick #(search search-term owner)} "Search" )))))

(defn home-view
  [{:keys [data], :as all-data} owner]
  (reify
    om/IInitState
    (init-state [this]
      ;(let [totals (:totals (first (agg/total-data agg/sum-all-data data)))]
      ;  (assoc all-data :totals totals))
      )
    om/IRenderState
    (render-state [this state]
      ;(println "home-view: " (:search-term state))
      (dom/div #js {:className "home"}
        (dom/h1 #js {:className "home-header"} "CrashPad")
        (om/build search-bar "")))))
