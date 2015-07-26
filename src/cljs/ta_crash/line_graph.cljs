(ns ta-crash.line-graph
  (:require [om.core :as om :include-macros true]
           [om.dom :as dom :include-macros true]
           [ta-crash.graph :as graph]))

(def margin {:top 0 :right 90 :bottom 10 :left 100})
(def y-tick-count 10)
(def x-tick-count 12)
(def tick-padding 5)
(def width (- (- 960 (:left margin)) (:right margin)))
(def height (- (- 640 (:top margin)) (:bottom margin)))

;(defmulti get-data-type (fn [type _] type))

;(defmethod get-data-type :crashes
;  [type totals]
;  (let [type-total (first (filter #(= :crashes (keyword (:type %))) totals))]
;    ; (println "type-total: " (:total-crashes type-total))
;    (:total-crashes type-total)))

;(defmulti get-data-type (fn [parent & _] parent))

(defn get-data-type
  [parent type totals]
  (let [type-total (first (filter #(= parent (keyword (:type %))) totals))]
    ;(println "type-total: " (type type-total))
    (type type-total)))

(defn get-graph-title
  [type]
  (case type
    :total-crashes "Total Crashes"
    :total-with-death "Total crashes resulting in a death"
    :total-with-injured "Total crashes resulting in an injury"
    :people-killed "Total Persons Killed"
    :pedestrians-killed "Pedestrians Killed"
    :bicyclists-killed "Bicyclists Killed"
    :motorists-killed "Motorists Killed"
    :people-injured "Total Persons Injured"
    :pedestrians-injured "Pedestrians Injured"
    :bicyclists-injured "Bicyclists Injured"
    :motorists-injured "Motorists Injured"
    :bicycle "Bicycle Involved"
    :lost-consciousness "Lost Consciousness Contributing Factor"))

(defn build-data
  [dimension data]
  (let [[parent type] dimension]
    ; (println parent " & " type)
    {:title (get-graph-title type)
     :data (map (fn [x]
            {:month (:month x)
             :year (:year x)
             :type (:type x)
             :identifier (:identifier x)
             :val (get-data-type parent type (:totals x))}) data)}))

;(defn get-line-graph-data
;  [type data]
;  ;(println "get-line-graph-data: " (count data))
;  {:title (get-graph-title type)
;   :data (map (fn [x]
;          {:month (:month x)
;            :year (:year x)
;            :type (:type x)
;            :identifier (:identifier x)
;            :val (get-data-type type (:totals x))}) data)})

(defn get-svg
  [selector]
  (if-let [svg (.select (.select js/d3 selector) "svg")]
    svg
    (.append (.select js/d3 selector) "svg")))

(defn get-g
  [selector from-elem]
  (if-let [g (.select from-elem selector)]
    g
    (.append from-elem "g")))

(defn set-graph
  [graph-data]
  ;(println (count (clj->js (map #(js/Date. (:year %) (:month %)) graph-data))))
  (let [count-data (count graph-data)
        svg (-> (get-svg ".line-graph-modal")
                (.attr "width" (+ width (:right margin) (:left margin)))
                (.attr "height" (+ height (:top margin) (:bottom margin))))
        line-group (-> (.append svg "g")
                       (.attr "transform" (str "translate(" (:left margin) "," 0 ")")))
        domain-max (.max js/d3 (clj->js (map #(:val %) graph-data)))
        domain-min (.min js/d3 (clj->js (map #(:val %) graph-data)))
        total-scale (-> (.linear (.-scale js/d3))
                        (.domain #js [0, (+ domain-max 50)])
                        (.range #js [height, 0])
                        (.nice))
        time-scale (-> (.scale (.-time js/d3))
                       (.domain #js [(js/Date. (:year (first graph-data)) 0), (js/Date. (:year (first graph-data)) 11)])
                       (.range #js [0, width]))
        x-pos (fn [d] (time-scale (js/Date. (.-year d) (.-month d))))
        y-pos (fn [d] (total-scale (.-val d)))
        line-segment (-> (.line (.-svg js/d3))
                         (.x #(x-pos %))
                         (.y #(y-pos %))
                         (.interpolate "linear"))
        y-axis (graph/create-axis total-scale :right width tick-padding graph/number-formatter y-tick-count)
        x-axis (graph/create-axis time-scale :top height tick-padding graph/month-formatter x-tick-count)
        x-axis-group (-> (.append line-group "g")
                         (.attr "transform" (str "translate(0 ," (- height 25) ")"))
                         (.attr "class" "x axis")
                         (.call x-axis)
                         (.selectAll "text")
                         (.attr "x" 0)
                         (.attr "y" 12)
                         (.attr "dx" "-1.5em")
                         (.style "text-anchor" "start"))
        y-axis-group (-> (.append line-group "g")
                         (.attr "transform" (str "translate(0, -25)"))
                         (.attr "class" "y axis")
                         (.call y-axis)
                         (.selectAll "text")
                         (.attr "x" -5)
                         (.attr "y" 0)
                         (.attr "dy" ".25em")
                         (.style "text-anchor" "end"))
        lines (-> (.selectAll line-group "path.year-line")
                  (.data (clj->js graph-data)))
        enter (.enter lines)]
    (println "set-graph:" graph-data)
    (-> (.append line-group "path")
        (.attr "d" (line-segment (clj->js graph-data)))
        (.attr "class" "year-line"))
    (-> (.selectAll line-group "circle")
        (.remove))
    (-> (.append enter "svg:circle")
        (.attr "cx" #(x-pos %))
        (.attr "cy" #(y-pos %))
        (.attr "r" 3)
        (.attr "class" "year-marker circle"))
    (-> (.append enter "svg:circle")
        (.attr "cx" #(x-pos %))
        (.attr "cy" #(y-pos %))
        (.attr "r" 8)
        (.attr "class" "year-hover circle"))
    (.remove (.exit lines))))

(defn line-graph-view
  [{:keys [data title] :as state} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (set-graph data))
    om/IDidUpdate
    (did-update [_ prev-p prev-s]
      (println "did-update: " data)
      (set-graph data))
    om/IRenderState
    (render-state [_ {:keys [line-data]}]
      ;(println "line-graph-data: " data)
      (dom/div nil
        (dom/h2 #js {:className "graph-title"} title)
        (dom/div #js {:className "line-graph-modal"})))))
