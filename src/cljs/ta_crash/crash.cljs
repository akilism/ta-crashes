(ns ta-crash.crash
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [ta-crash.aggregation :as agg]
            [ta-crash.header :as header]
            [ta-crash.tooltip :as tooltip]
            [ta-crash.crash-map-embed :as crash-map]
            [ta-crash.text-stats :as text-stats]
            [ta-crash.requester :as requester]
            [secretary.core :as secretary]))

(defn transform-hover
  [[event feature]]
  (let [event-type (.-type js/event)
        dom-event (.-originalEvent js/event)
        identifier (.-identifier js/feature) ]
    {:type (keyword event-type) :identifier (.-identifier js/feature) :x (.-x dom-event) :y (.-y dom-event)}))

(defn get-borough
  [id]
  (case id
    2 "bronx"
    3 "brooklyn"
    1 "manhattan"
    4 "queens"
    5 "staten island"))

(defn get-name-display
  [type identifier]
  (case type
    :borough (get-borough identifier)
    :community-board (str "community board district " identifier)
    :city-council (str "city council district " identifier)
    :precinct (str "police precinct " identifier)
    identifier))

(defn get-type-identifier
  [data]
  (let [type (first (:type data))
        identifier (first (:identifier data))]
    [type identifier]))

(defn nav-to
  [nav-selection data]
  (go (let [geo-data (<! (requester/get-geo-data nav-selection))]
    (om/transact! data :geo-data (fn [] (assoc geo-data :active-type nav-selection)))
    (om/transact! data :set-shapes (fn [] true)))))

(defn handle-hover
  [{:keys [identifier type x y]} data]
  (condp = type
    :mouseover (om/transact! data (fn [] {:show true :type :name :pos {:x x :y y} :identifier identifier}))
    :mousemove (om/transact! data (fn [] {:show true :type :name :pos {:x x :y y} :identifier identifier}))
    :mouseout (om/transact! data (fn [] {:show false}))))

(defn get-indentifier
  [type identifier]
  (case type
    :borough (get-borough identifier)
    identifier))

(defn filter-map
  [[type identifier]]
  (println type " - " identifier)
  (let [path (str "/" (name type) "/" (get-indentifier type identifier))
        history (.-history js/window)]
    (.pushState js/history #js {:type type :identifier identifier}, (str type "-" identifier) path)
    (secretary/dispatch! path)))

(defn get-totals
  [[t & ts] data acc]
  (cond
    (nil? t) acc
    :else (recur ts data (assoc acc t (first (filter #(= t (:type %)) data))))))


(defn crashes-view
  [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [nav-chan (om/get-state owner :nav-chan)
            map-hover-chan (om/get-state owner :map-hover-chan)
            map-click-chan (om/get-state owner :map-click-chan)]
        (go (loop []
          (let [[v ch] (alts! [nav-chan map-hover-chan map-click-chan])]
            ; (println "go" v)
            (condp = ch
              nav-chan (nav-to v data)
              map-hover-chan (handle-hover v (om/ref-cursor (:hover-data data)))
              map-click-chan (filter-map v))
            (recur))))))
    om/IInitState
    (init-state [_]
      (assoc data :nav-chan (chan)
                  :map-hover-chan (chan 1 (map transform-hover))
                  :map-click-chan (chan)
                  :stat-data (first (agg/total-data agg/sum-all-data (get-in data [:stats])))))
    om/IRenderState
    (render-state
      [_ state]
      (dom/div #js {:className "container"}
        (om/build header/header-view
          {:areas (:areas data)
           :type-ident ["precinct" "83rd"];(get-type-identifier data)
           :page-type (:page-type data)
           :nav-chan (:nav-chan state)})
        (om/build text-stats/text-stats-view
          (get-totals [:crashes :injured :killed] (get-in state [:stat-data :totals]) {})
          {:init-state {:set-line-chart-data (:set-line-chart-data state)}})
        (om/build crash-map/map-view data {:init-state {:map-hover-chan (om/get-state owner :map-hover-chan)
                                                   :map-click-chan (om/get-state owner :map-click-chan)}})
        (om/build tooltip/tool-tip (assoc (:hover-data data) :display-f get-name-display :type (-> data :geo-data :active-type)))))))
