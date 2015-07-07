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

(def margin {:top 0 :right 40 :bottom 10 :left 140})
(def y-tick-count 3)
(def x-tick-count 10)
(def tick-padding 5)
(def bar-height 55)
(def width (- (- 640 (:left margin)) (:right margin)))
(def bar-padding 5)


(defn type-formatter
  [type]
  (let [key-type (keyword type)]
    (cond
      (= :city key-type) "Worst in city."
      (= :selected key-type) "Selected intersection."
      :else (str "Worst in " type "."))))

(defn number-formatter
  [x]
  ((.format js/d3 ",") x))

(defn create-axis
  [axis-scale axis-orientation tick-size tick-padding tick-format tick-count]
  (-> (.axis (.-svg js/d3))
    (.scale axis-scale)
    (.orient (name axis-orientation))
    (.tickSize tick-size)
    (.tickPadding tick-padding)
    (.tickFormat tick-format)
    (.ticks tick-count)))

(defn set-graph [graph-data]
  (let [height (- (+ (* 3 (+ bar-padding bar-height)) 30) (:top margin) (:bottom margin))
        count-data (count graph-data)
        svg (-> (.append (.select js/d3 ".bar-graph-modal") "svg")
                (.attr "width" (+ width (:right margin) (:left margin)))
                (.attr "height" (+ height (:top margin) (:bottom margin))))
        bar-group (-> (.append svg "g")
                      (.attr "transform" (str "translate(" (:left margin) "," 0 ")")))
        domain-max (.max js/d3 (clj->js (map #(:val %) graph-data)))
        domain-min (.min js/d3 (clj->js (map #(:val %) graph-data)))
        total-scale (-> (.linear (.-scale js/d3))
                        (.domain #js [0, (+ domain-max 50)])
                        (.range #js [0, width])
                        (.nice))
        type-scale (-> (.ordinal (.-scale js/d3))
                       (.domain (clj->js (map #(:type %) graph-data)))
                       (.rangeBands #js [0, height] 1))
        y-axis (create-axis type-scale :right width tick-padding type-formatter count-data)
        x-axis (create-axis total-scale :top height tick-padding number-formatter x-tick-count)
        x-axis-group (-> (.append bar-group "g")
                         (.attr "transform" (str "translate(0 ," (- height 25) ")"))
                         (.attr "class" "x axis")
                         (.call x-axis)
                         (.selectAll "text")
                         (.attr "x" 0)
                         (.attr "y" 12)
                         (.attr "dx" "-0.25em")
                         (.style "text-anchor" "start"))
        y-axis-group (-> (.append bar-group "g")
                         (.attr "transform" (str "translate(0, -25)"))
                         (.attr "class" "y axis")
                         (.call y-axis)
                         (.selectAll "text")
                         (.attr "x" -5)
                         (.attr "y" 0)
                         (.attr "dy" ".85em")
                         (.style "text-anchor" "end"))
        bars (-> (.selectAll bar-group "rect.bar")
                 (.data (clj->js graph-data)))
        enter (.enter bars)]
    (println "set-graph:" graph-data)
    (-> (.append enter "svg:rect")
        (.attr "x" 0)
        (.attr "y" #(* bar-height %2))
        (.attr "height" (- bar-height bar-padding))
        (.attr "width" #(total-scale (.-val %)))
        (.attr "class" #(.-type %)))))

(defn bar-graph-view
  [{:keys [data title] :as state} owner]
  (reify
    om/IDidMount
    (did-mount [this]
      (set-graph data))
    om/IRenderState
    (render-state [this {:keys [bar-data]}]
      ;(println "bar-graph-data: " title)
      (dom/div nil
        (dom/h2 #js {:className "graph-title"} title)
        (dom/div #js {:className "bar-graph-modal"} )))))


