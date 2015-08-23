(ns ta-crash.crash
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ta-crash.header :as header]))

(defonce crash-map (atom nil))

(def map-id "map")
(def nyc-coords [40.78 -73.97])
(def default-zoom 11)

(defn get-type-identifier
  [data]
  (let [type (first (:type data))
        identifier (first (:identifier data))]
    (println "data:" data)
    [type identifier]))

(defn new-map []
  (.map js/L map-id))

(defn get-map []
  (if (nil? @crash-map)
    (swap! crash-map new-map)
    @crash-map))

(defn set-map
  [coords zoom tile-url tile-opts]
  (let [crash-map (get-map)]
    (-> crash-map
        (.setView (clj->js coords) zoom))
    (-> (.tileLayer js/L tile-url (clj->js tile-opts))
      (.addTo crash-map))))

(defn set-geo-layer
  [geo-data options]
  (.geoJson js/L (clj->js geo-data) options))

(defn set-shape
  [geo-data]
  (let [crash-map (get-map)
        geo-layer (set-geo-layer geo-data {})]
    (do
      (-> geo-layer
          (.addTo crash-map))
      (-> crash-map
          (.fitBounds (.getBounds geo-layer))))))

(defn crashes-view
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
            :subdomains "abcd" :minZoom 11 :maxZoom 17}
            crash-map (set-map nyc-coords default-zoom tile-url tile-opts)]
        (set-shape (:geo-data data))))
    om/IRenderState
    (render-state
      [_ state]
      (dom/div #js {:className "container"}
        (om/build header/header-view
          {:areas (:areas data)
           :type-ident ["precinct" "83rd"];(get-type-identifier data)
           :page-type (:page-type data)})
        (dom/div #js {:className "stats" } "Stats")
        (dom/div #js {:className "map" :id "map"})))))
