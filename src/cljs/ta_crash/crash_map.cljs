(ns ta-crash.crash-map
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ta-crash.header :as header]))

(defn get-type-identifier
  [data]
  (let [type (first (:type data))
        identifier (first (:identifier data))]
    (println "data:" data)
    [type identifier]))

(defn get-map
  [coords zoom]
  (-> (.map js/L "map")
      (.setView (clj->js coords) zoom)))

(defn set-map
  [coords zoom tile-url tile-opts]
  (let [crash-map (get-map coords zoom)]
    (-> (.tileLayer js/L tile-url (clj->js tile-opts))
      (.addTo crash-map))))

(defn crash-map-view
  [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      )
    om/IInitState
    (init-state [_]
      data)
    om/IDidMount
    (did-mount [_]
      (let [tile-url "http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
            tile-opts {
            :attribution "&copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a> &copy; <a href=\"http://cartodb.com/attributions\">CartoDB</a>",
            :subdomains "abcd" :minZoom 11 :maxZoom 17}]
        (set-map [40.78 -73.97] 11 tile-url tile-opts)))
    om/IRenderState
    (render-state
      [_ state]
      (dom/div #js {:className "container"}
        (om/build header/header-view
          {:areas (:areas data)
           :type-ident ["precinct" "83rd"];(get-type-identifier data)
           :page-type (:page-type data)})
        (dom/div #js {:className "map" :id "map"}
          )))))
