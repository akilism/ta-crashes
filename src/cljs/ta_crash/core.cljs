(ns ta-crash.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<!]]
            [ta-crash.requester :as requester]
            [ta-crash.home :as home]
            [ta-crash.crash-map :as map]
            [ta-crash.crash-rank :as rank]
            [ta-crash.crash-trend :as trend]
            [ta-crash.total-groups :as total-groups]
            [secretary.core :as secretary :refer-macros [defroute]]))

(enable-console-print!)

(defonce app-state (atom {:data {}
                          :active-years [2015]
                          :line-chart-dimension [:crashes :total-crashes]
                          :bar-data {:title "Pedestrians Injured"
                                     :data [{:type :city :val 10000 :name "34th St & 6th Ave"}
                                            {:type :borough :val 8000 :name "Carroll St & 5th Ave"}
                                            {:type :selected :val 6500 :name "Fulton Ave & Franklin St"}]} }))

(defonce target {:target (. js/document (getElementById "app"))})

;;; Helpers for setting state data.

(defmulti set-state-data!
  (fn [type _]
    type))

(defmethod set-state-data! :crash-map
  [_ data])

(defmethod set-state-data! :crash-rank
  [_ data])

(defmethod set-state-data! :crash-trend
  [_ data]
  (let [type (keyword (:type (first data)))
        identifier (keyword (:identifier (first data)))]
    (swap! app-state assoc-in [:data type identifier] data)
    (swap! app-state assoc :identifier [identifier] :type [type])))


;;; Page Render Methods

(defmulti render-page
  (fn [type &_]
    type))

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
  [_ data]
  (om/root
    trend/crash-trend-view
    app-state
    target))


;;; Client Side Routes
(defn get-area-type []
  (if-let [area-type (.getItem (.-localStorage js/window) "area")]
    (keyword area-type)
    :city))

(defroute home "/" []
  (render-page :home))

(defroute page-path "/:page-type/:identifier" {:as params}
  (go
    (let [page-type (keyword (:page-type params))
          identifier (keyword (:identifier params))
          area-type (get-area-type)
          data (<! (requester/get-data page-type area-type identifier))]
      (set-state-data! page-type data)
      (render-page page-type data))))

(defn get-client-route []
  (let [location (.-location js/window)
        path (.-pathname location)
        search (.-search location)]
    (str path search)))

(defn main []
  (secretary/dispatch! (get-client-route)))
