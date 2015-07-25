(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<!]]
            [ta-crash.requester :as requester]
            [ta-crash.home :as home]
            [ta-crash.total-groups :as total-groups]
            [ta-crash.routes :as routes]
            [secretary.core :as secretary]))

(enable-console-print!)

(defonce app-state (atom {:data []
                          :active-years [2015]
                          :line-graph-dimension :crashes
                          :bar-data {:title "Pedestrians Injured"
                                     :data [{:type :city :val 10000 :name "34th St & 6th Ave"}
                                            {:type :borough :val 8000 :name "Carroll St & 5th Ave"}
                                            {:type :selected :val 6500 :name "Fulton Ave & Franklin St"}]} }))

(defonce target {:target (. js/document (getElementById "app"))})


;;; Page Render Methods

(defmulti render-page
  (fn [type &_]
    (keyword type)))

(defmethod render-page :home
  [_]
  (om/root
    home/home-view
    app-state
    target))

(defmethod render-page :crash-map
  [_ identifier date-start date-end date-display]
  (om/root
    home/home-view
    app-state
    target))

(defmethod render-page :crash-rank
  [_ identifier date-start date-end date-display]
  (om/root
    home/home-view
    app-state
    target))

(defmethod render-page :crash-trend
  [_ identifier date-start date-end date-display]
  (om/root
    home/home-view
    app-state
    target))

;;(defn render-root []
;; (go
;;    (let [data (<! (requester/get-data))]
;;      (swap! app-state assoc :data data)
;;      (om/root
;;        total-groups/total-groups-view
;;        app-state
;;        {:target (. js/document (getElementById "app"))}))))


;;; Client side routes

(defn get-client-route []
  (let [location (.-location js/window)
        path (.-pathname location)
        search (.-search location)]
    (str path search)))

(defn main []
  (println (get-client-route))
  (secretary/dispatch! (get-client-route)))
