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


(defn get-svg
  [selector]
  (let [svg (.select (.select js/d3 selector) "svg")]
    (if (nil? (first (first (js->clj svg)))) (.append (.select js/d3 selector) "svg") svg)))

(defn get-g
  [selector from-elem]
  (let [g (.select from-elem selector)]
    (if (nil? (first (first (js->clj g)))) (.append from-elem "g") g)))

(defn set-graph
  [graph-data]
  ;(println (count (clj->js (map #(js/Date. (:year %) (:month %)) graph-data))))
  (let [count-data (count graph-data)
        svg (-> (get-svg ".line-graph-modal")
                (.attr "width" (+ width (:right margin) (:left margin)))
                (.attr "height" (+ height (:top margin) (:bottom margin))))
        line-group (-> (get-g ".line-group" svg)
                       (.attr "class" "line-group")
                       (.attr "transform" (str "translate(" (:left margin) "," 0 ")")))
        domain-max (.max js/d3 (clj->js (map #(:val %) graph-data)))
        domain-min (.min js/d3 (clj->js (map #(:val %) graph-data)))
        total-scale (-> (.linear (.-scale js/d3))
                        (.domain #js [0, (+ domain-max 10)])
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
        x-axis-group (-> (get-g ".axis.x" line-group)
                         (.attr "transform" (str "translate(0 ," (- height 25) ")"))
                         (.attr "class" "x axis")
                         (.call x-axis)
                         (.selectAll "text")
                         (.attr "x" 0)
                         (.attr "y" 12)
                         (.attr "dx" "-1.5em")
                         (.style "text-anchor" "start"))
        y-axis-group (-> (get-g ".axis.y" line-group)
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
    ;(println "set-graph:" graph-data)
    (-> (.selectAll line-group "path")
        (.remove))
    (-> (.append line-group "path")
        (.attr "d" (line-segment (clj->js graph-data)))
        (.attr "class" "year-line")
        (.attr "transform" (str "translate(0, -25)")))
    (-> (.selectAll line-group "circle")
        (.remove))
    (-> (.append enter "svg:circle")
        (.attr "cx" #(x-pos %))
        (.attr "cy" #(y-pos %))
        (.attr "r" 3)
        (.attr "class" "year-marker circle")
        (.attr "transform" (str "translate(0, -25)")))
    (-> (.append enter "svg:circle")
        (.attr "cx" #(x-pos %))
        (.attr "cy" #(y-pos %))
        (.attr "r" 8)
        (.attr "class" "year-hover circle")
        (.attr "transform" (str "translate(0, -25)")))
    (.remove (.exit lines))))

(defn line-graph-view
  [{:keys [data title] :as state} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (set-graph data))
    om/IDidUpdate
    (did-update [_ prev-p prev-s]
      (set-graph data))
    om/IRenderState
    (render-state [_ {:keys [line-data]}]
      ;(println "line-graph-data: " data)
      (dom/div nil
        (dom/h2 #js {:className "graph-title"} title)
        (dom/div #js {:className "line-graph-modal"})))))
