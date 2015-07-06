(ns ta-crash.aggregation)

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
