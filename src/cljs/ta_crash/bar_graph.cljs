(ns ta-crash.bar-graph
  (:require [om.core :as om :include-macros true]
           [om.dom :as dom :include-macros true]))

; Title (ie "Pedestrian Injuries", "Crashes", "Bicyclist Deaths")

; Y-Axis
; Worst in City (name, value, type)
; Worst in _____ (name, value, type)
; Selected intersection (name, value, type)  rank? (20/100 in __) (20th in __) (Safest in __)

; X-Axis
; Value

; Types
; :city :selected :borough
; :zip-code :city-council :community-district
; :neighborhood :police-precinct :state-assembly
; :congressional

(def y-tick-count 10)
(def x-tick-count 12)
(def tick-padding 5)
(def bar-height 45)
(def width 640)

(defn type-formatter
  [type]
  (name type))

(defn number-formatter
  []
  (.format js/d3 ","))

(defn create-axis
  [axis-scale axis-orientation tick-size tick-padding tick-format tick-count]
  (-> (.axis (.-svg js/d3))
    (.scale axis-scale)
    (.orient axis-orientation)
    (.tickSize tick-size)
    (.tickPadding tick-padding)
    (.tickFormat tick-format)
    (.ticks tick-count)))

(defn set-graph [graph-data]
  (let [svg (.append (.select js/d3 ".bar-graph-modal") "svg")
        bar-group (.append svg "g")
        domain-max (.max js/d3 (map #(:value %) graph-data))
        domain-min (.min js/d3 (map #(:value %) graph-data))
        total-scale (-> (.linear (.-scale js/d3))
                        (.domain #js [0, domain-max])
                        (.nice))
        type-scale (-> (.linear (.-scale js/d3))
                        (.domain #js [0, 3])
                        (.nice))
        y-axis (create-axis type-scale :right width tick-padding type-formatter y-tick-count)
        x-axis (create-axis total-scale :right width tick-padding number-formatter x-tick-count)
        bars (-> (.selectAll bar-group "rect.bar")
                 (.data (clj->js graph-data)))
        enter (.enter bars)]
    (println "set-graph:" graph-data)
    (-> (.append enter "svg:rect")
        (.attr "x" 0)
        (.attr "y" #(* bar-height %2))
        (.attr "height" bar-height)
        (.attr "width" #(.-val %))
        (.attr "class" #(.-type %)))
    (println "append-bars")))

(defn bar-graph-view
  [{:keys [data title] :as state} owner]
  (reify
    om/IDidMount
    (did-mount [this]
      (set-graph data))
    om/IRenderState
    (render-state [this {:keys [bar-data]}]
      (println "bar-graph-data: " title)
      (dom/div nil
        (dom/h2 #js {:className "graph-title"} title)
        (dom/div #js {:className "bar-graph-modal"} )))))


