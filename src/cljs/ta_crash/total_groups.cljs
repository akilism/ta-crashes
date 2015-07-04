(ns ta-crash.total-groups
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn has-year?
  "Does the supplied year exist in the dataset?"
  [data year]
  (some #(= year (:year %)) data))

(defn total-data
  "Reduce the supplied data with the supplied function accumulate into a vector."
  [reduce-f data]
  (reduce reduce-f [] data))

(defn data-summer
  "Takes a vector of totals returns a function that takes a total map. Adds the values from the matching type in the vector to the supplied total map."
  [curr-totals]
  (fn [agg-total]
    (let [agg-type (keyword (:type agg-total))
          curr-total (first (filter #(= (keyword (:type %)) (:type agg-total)) curr-totals))
          all-keys (set (concat (keys agg-total) (keys curr-total)))]
      (into (hash-map)
      (map (fn [k v]
          ;(println "summer: " k " - " (k agg-total) " - " (k curr-total))
          (cond
            (= k :type) [k agg-type]
            (= k :year) [k v]
            ;(or (= k :type) (= k :year)) [k (k agg-total)]
            (and (k curr-total) (k agg-total)) [k (+ (k curr-total) (k agg-total))]
            (k agg-total) [k (k agg-total)]
            (k curr-total) [k (k curr-total)])) all-keys)))))

(defn sum-totals
  "Maps over types and sums the values."
  [agg-totals curr-totals]
  ;(println "agg-totals: " (count agg-totals) "\ncurr-totals: " (count curr-totals))
  (let [summer (data-summer curr-totals)]
    (map summer agg-totals)))

(defn sum-distinct-years
  "aggregate data into distinct years"
  [acc {:keys [year totals]}]
  (cond
    (has-year? acc year)
      (map (fn [x]
          (cond
            (= year (:year x)) {:year year :totals (sum-totals (:totals x) totals)}
            :else x)) acc)
    :else (conj acc {:year year :totals totals})))

(defn sum-all-data
  "roll up all data."
  [acc {:keys [totals]}]
  (cond
     (= 0 (count acc)) (conj acc {:year "all" :totals totals})
     :else (map (fn [x] {:year "all" :totals (sum-totals (:totals x) totals)})
                acc)))

(defmulti get-total-items (fn [type _] type))

(defmethod get-total-items :crashes
  [type items]
  (dissoc items :total-crashes :type))

(defmethod get-total-items :injured
  [type items]
  (dissoc items :people-injured :type))

(defmethod get-total-items :killed
  [type items]
  (dissoc items :people-killed :type))

(defn is-group?
  [{:keys [type]}]
  (let [non-groups [:contributing-factors :vehicle-types]]
    (not-any? #(= % type) non-groups)))

(defn is-not-group?
  [{:keys [type]}]
  (let [non-groups [:contributing-factors :vehicle-types]]
    (not (not-any? #(= % type) non-groups))))

(defn get-title-display
  [type total]
  (cond
    (= type :crashes) (str "Total Crashes " (:total-crashes total))
    (= type :injured) (str "Total Injuries " (:people-injured total))
    (= type :killed) (str "Total Deaths " (:people-killed total))))

(defn get-type-display
  [type]
  (cond
    (= type :total-with-injured) (str "Injury")
    (= type :total-with-death) (str "Death")
    (or (= type :pedestrians-injured) (= type :pedestrians-killed)) (dom/span #js {:className "icon-pedestrians"} "Pedestrians")
    (or (= type :bicyclists-injured) (= type :bicyclists-killed)) (dom/span #js {:className "icon-bicyclists"} "Bicyclists")
    (or (= type :motorists-injured) (= type :motorists-killed)) (dom/span #js {:className "icon-motorists"} "Motorists")))

; ~~~~~~~~~~~~~~~~~~~~~~~~
; Total Display Components
; ~~~~~~~~~~~~~~~~~~~~~~~~

(defn total-item-display
  [[type val]]
  (reify
    om/IRender
    (render [this]
      (dom/div #js {:className "total-item"}
        (dom/p #js {:className "total-item-count"} val)
        (dom/p nil (get-type-display type))))))

(defn total-group-item
  [total owner]
  (reify
    om/IRender
    (render [this]
      ; (println "total-group-item: " total)
      (om/build total-item-display total))))

(defn total-group
  [total owner]
  (reify
    om/IRender
    (render [this]
      (let [type (:type total)
            items (get-total-items type total)]
        ; (println "total-group: " total)
        (dom/li #js {:className "total-group"}
          (dom/p #js {:className "total-title"}
            (get-title-display type total))
          (apply dom/ul #js {:className "total-group-items"}
          (om/build-all total-group-item items)))))))

(defn factor-item
  [[type val :as item] owner]
  (reify
    om/IRender
    (render [this]
      (dom/div nil
        (dom/p nil (name type))
        (dom/p nil val)))))

(defn total-factors
  [factors owner]
  (reify
    om/IRender
    (render [this]
      (let [type (:type factors)
            items (dissoc factors :type)]
        (apply dom/div #js {:className "factor"}
          (om/build-all factor-item items))))))

(defn total-groups-view
  [{:keys [data] :as state} owner]
  (reify
    om/IRender
    (render [this]
      ;(println "total-group-view: " state)
      (let [totals (:totals (first (total-data sum-all-data data)))]
        (apply dom/div #js {:className "totals"}
          (apply dom/ul #js {:className "total-groups"}
            (om/build-all total-group (filter is-group? totals)))
          (om/build-all total-factors (filter is-not-group? totals))

          )))))
