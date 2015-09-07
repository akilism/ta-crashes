(ns ta-crash.crash-map-embed
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(declare map-hover-chan)
(declare map-click-chan)

(def crash-map (atom nil))
(def outline-layer (atom nil))

;; These could all be moved to props.
(def map-id "map")
(def nyc-coords [40.78 -73.97])
(def default-zoom 11)
(def tile-url "http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png")
(def tile-opts { :attribution "&copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a> &copy; <a href=\"http://cartodb.com/attributions\">CartoDB</a>"
  :subdomains "abcd"
  :minZoom 11
  :maxZoom 17})
(def feature-style {:weight 1.5
                    :opacity 1
                    :fill true
                    :stroke true
                    :fillOpacity 0
                    :color "#000000"})

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
  [feature layer type]
  ;(.bindPopup layer (.. js/feature -properties -identifier))
  (.on layer "click" (fn [e] (put! map-click-chan [type (.. js/feature -properties -identifier)])))
  (.on layer "mouseover" (fn [e] (put! map-hover-chan [e (.-properties js/feature)])))
  (.on layer "mouseout" (fn [e] (put! map-hover-chan [e (.-properties js/feature)])))
  (.on layer "mousemove" (fn [e] (put! map-hover-chan [e (.-properties js/feature)]))))

(defn set-shape
  [geo-data]
  (let [crash-map (get-map)
        geo-layer (set-geo-layer geo-data {:style feature-style :onEachFeature #(set-feature %1 %2 (:active-type geo-data))})]
    (do
      (let [bounds (.getBounds geo-layer)]
        (check-outline-layer geo-layer)
        (-> geo-layer
            (.addTo crash-map))
        (-> crash-map
            (.fitBounds (.getBounds geo-layer)))))))

(defn map-view
  [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (def map-hover-chan (om/get-state owner :map-hover-chan))
      (def map-click-chan (om/get-state owner :map-click-chan)))
    om/IWillUpdate
    (will-update [_ next-props next-state]
      (when (:set-shapes next-props)
        (set-shape (:geo-data next-props))
        (om/transact! data :set-shapes (fn [] false))))
    om/IDidMount
    (did-mount [_]
      (when (not (nil? @crash-map))
        (reset! crash-map nil))
      (set-map nyc-coords default-zoom tile-url tile-opts)
      (when (not (nil? (:geo-data data)))
        (set-shape (:geo-data data))))
    om/IRenderState
    (render-state
      [_ state]
      (dom/div #js {:className "map" :id "map"}))))
