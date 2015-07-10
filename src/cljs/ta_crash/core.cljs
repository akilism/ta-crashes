(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<!]]
            [ta-crash.requester :as requester]
            [ta-crash.total-groups :as total-groups]
            [secretary.core :as secretary :refer-macros [defroute]]))

(enable-console-print!)

(defonce app-state (atom {:data []
                          :active-years [2015]
                          :line-graph-dimension :crashes
                          :bar-data {:title "Pedestrians Injured"
                                     :data [{:type :city :val 10000 :name "34th St & 6th Ave"}
                                            {:type :borough :val 8000 :name "Carroll St & 5th Ave"}
                                            {:type :selected :val 6500 :name "Fulton Ave & Franklin St"}]} }))

(defn render-root []
  (go
    (let [data (<! (requester/get-data))]
      (swap! app-state assoc :data data)
      (om/root
        total-groups/total-groups-view
        app-state
        {:target (. js/document (getElementById "app"))}))))

(defn main []
  (render-root))
