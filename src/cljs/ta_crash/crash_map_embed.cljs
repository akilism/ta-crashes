(ns ta-crash.crash-map-embed
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]))

(def crash-map (atom nil))
(def outline-layer (atom nil))
(defn transform-hover
  [[event feature]]
  (let [event-type (.-type js/event)
        dom-event (.-originalEvent js/event)
        identifier (.-identifier js/feature) ]
    {:type (keyword event-type) :identifier (.-identifier js/feature) :x (.-x dom-event) :y (.-y dom-event)}))

(def map-hover-chan (chan 1 (map transform-hover)))
(def map-click-chan (chan))
(def map-id "map")
(def nyc-coords [40.78 -73.97])
(def default-zoom 11)
(def tile-url "http://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png")
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

;; remove
(defn get-borough
  [id]
  (case id
    2 "bronx"
    3 "brooklyn"
    1 "manhattan"
    4 "queens"
    5 "staten island"))

;; remove
(defn get-name-display
  [type identifier]
  (case type
    :borough (get-borough identifier)
    :community-board (str "community board district " identifier)
    :city-council (str "city council district " identifier)
    :precinct (str "police precinct " identifier)
    identifier))

;; remove
(defn get-type-identifier
  [data]
  (let [type (first (:type data))
        identifier (first (:identifier data))]
    ;(println "data:" data)
    [type identifier]))

;; keep
(defn new-map []
  (.map js/L map-id))

;; keep
(defn get-map []
  (if (nil? @crash-map)
    (swap! crash-map new-map)
    @crash-map))

;; keep
(defn set-map
  [coords zoom tile-url tile-opts]
  (let [crash-map (get-map)]
    (-> crash-map
        (.setView (clj->js coords) zoom))
    (-> (.tileLayer js/L tile-url (clj->js tile-opts))
      (.addTo crash-map))))

;; keep
(defn set-geo-layer
  [geo-data options]
  (.geoJson js/L (clj->js geo-data) (clj->js options)))

;; keep
(defn remove-layer []
  (let [crash-map (get-map)]
    (.removeLayer crash-map @outline-layer)))

;; keep
(defn check-outline-layer [geo-layer]
  (if (nil? @outline-layer)
    (swap! outline-layer (fn [] geo-layer))
    (do
      (remove-layer)
      (swap! outline-layer (fn [] geo-layer)))))

;; keep
(defn set-feature
  [feature layer type]
  ;(println "set-feature" layer)
  ;(.bindPopup layer (.. js/feature -properties -identifier))
  (.on layer "click" (fn [e] (put! map-click-chan [type (.. js/feature -properties -identifier)])))
  (.on layer "mouseover" (fn [e] (put! map-hover-chan [e (.-properties js/feature)])))
  (.on layer "mouseout" (fn [e] (put! map-hover-chan [e (.-properties js/feature)])))
  (.on layer "mousemove" (fn [e] (put! map-hover-chan [e (.-properties js/feature)]))))

;; keep
(defn set-shape
  [geo-data]
  (let [crash-map (get-map)
        geo-layer (set-geo-layer geo-data {:style feature-style :onEachFeature #(set-feature %1 %2 (:active-type geo-data))})]
    (do
      (let [bounds (.getBounds geo-layer)]
        (println (.toBBoxString bounds)))
      (check-outline-layer geo-layer)
      (-> geo-layer
          (.addTo crash-map))
      (-> crash-map
          (.fitBounds (.getBounds geo-layer))))))

;;remove
(defn nav-to
  [nav-selection data]
  (go (let [geo-data (<! (requester/get-geo-data nav-selection))]
    (om/transact! data :geo-data (fn [] (assoc geo-data :active-type nav-selection)))
    (om/transact! data :set-shapes (fn [] true)))))

;; remove
(defn handle-hover
  [{:keys [identifier type x y]} data]
  (condp = type
    :mouseover (om/transact! data (fn [] {:show true :type :name :pos {:x x :y y} :identifier identifier}))
    :mousemove (om/transact! data (fn [] {:show true :type :name :pos {:x x :y y} :identifier identifier}))
    :mouseout (om/transact! data (fn [] {:show false}))))

;; remove
(defn get-indentifier
  [type identifier]
  (case type
    :borough (get-borough identifier)
    identifier))

;; remove
(defn filter-map
  [[type identifier]]
  (let [path (str "/" (name type) "/" (get-indentifier type identifier))
        history (.-history js/window)]
    (.pushState js/history #js {:type type :identifier identifier}, (str type "-" identifier) path)
    (secretary/dispatch! path)))

(defn map-view
  [data owner]
  (reify
;    om/IWillMount
;    (will-mount [_]
;      (let [nav-chan (om/get-state owner :nav-chan)]
;        (go (loop []
;          (let [[v ch] (alts! [nav-chan map-hover-chan map-click-chan])]
;            ;; run handler for each channel.
;            ;(println "go" v)
;            (condp = ch
;              nav-chan (nav-to v data)
;              map-hover-chan (handle-hover v (om/ref-cursor (:hover-data data)))
;              map-click-chan (filter-map v))
;            (recur))))))
    om/IWillUpdate
    (will-update [_ next-props next-state]
      (when (:set-shapes next-props)
        (set-shape (:geo-data next-props))
        (om/transact! data :set-shapes (fn [] false))))
    om/IInitState
    (init-state [_]
      (assoc data :nav-chan (chan)))
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
