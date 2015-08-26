(ns ta-crash.crash
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ta-crash.header :as header]
            [ta-crash.requester :as requester]))

(def crash-map (atom nil))
(def outline-layer (atom nil))

(def map-id "map")
(def nyc-coords [40.78 -73.97])
(def default-zoom 11)
(def tile-url "http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png")
;(def tile-url "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png")
(def tile-opts { :attribution "&copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a> &copy; <a href=\"http://cartodb.com/attributions\">CartoDB</a>"
  :subdomains "abcd"
  :minZoom 11
  :maxZoom 17})
(def feature-style {:weight 2
                    :opacity 1
                    :fill true
                    :stroke true
                    :fillOpacity 0
                    :color "#000000"})

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
  (.geoJson js/L (clj->js geo-data) (clj->js options)))

(defn remove-layer []
  (let [crash-map (get-map)]
    (.removeLayer crash-map @outline-layer)))

(defn check-outline-layer [geo-layer]
  (if (nil? @outline-layer)
    (swap! outline-layer (fn [] geo-layer))
    (do
      (remove-layer)
      (swap! outline-layer (fn [] geo-layer)))))

(defn set-feature
  [feature layer]
  (println "set-feature" layer)
  (.bindPopup layer (.. js/feature -properties -identifier))
  (.on layer "click" (fn [e] (println "properties" (.. js/feature -properties -identifier)))))

(defn set-shape
  [geo-data]
  (let [crash-map (get-map)
        geo-layer (set-geo-layer geo-data {:style feature-style :onEachFeature set-feature})]
    (do
      (check-outline-layer geo-layer)
      (-> geo-layer
          (.addTo crash-map))
      (-> crash-map
          (.fitBounds (.getBounds geo-layer))))))

(defn crashes-view
  [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [nav-chan (om/get-state owner :nav-chan)]
        (go (loop []
          (let [nav-selection (<! nav-chan)
                geo-data (<! (requester/get-geo-data nav-selection))]
            (println nav-selection)
            (om/transact! data :geo-data (fn [] geo-data))
            (recur))))))
    om/IWillUpdate
    (will-update [_ next-props next-state]
      (set-shape (:geo-data next-props)))
    om/IInitState
    (init-state [_]
      (assoc data :nav-chan (chan)))
    om/IDidMount
    (did-mount [_]
      (set-map nyc-coords default-zoom tile-url tile-opts)
      (when (not (nil? (:geo-data data)))
        (set-shape (:geo-data data))))
    om/IRenderState
    (render-state
      [_ state]
      (dom/div #js {:className "container"}
        (om/build header/header-view
          {:areas (:areas data)
           :type-ident ["precinct" "83rd"];(get-type-identifier data)
           :page-type (:page-type data)
           :nav-chan (:nav-chan state)})
        (dom/div #js {:className "stats" } "Stats")
        (dom/div #js {:className "map" :id "map"})))))
