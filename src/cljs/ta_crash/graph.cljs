(ns ta-crash.graph)

(defn number-formatter
  [x]
  ((.format js/d3 ",") x))

(defn type-formatter
  [type]
  (let [key-type (keyword type)]
    (cond
      (= :city key-type) "Worst in city."
      (= :selected key-type) "Selected intersection."
      :else (str "Worst in " type "."))))

(defn month-formatter
  [date]
  ((.format (.-time js/d3) "%B") date))

(defn create-axis
  [axis-scale axis-orientation tick-size tick-padding tick-format tick-count]
  (-> (.axis (.-svg js/d3))
    (.scale axis-scale)
    (.orient (name axis-orientation))
    (.tickSize tick-size)
    (.tickPadding tick-padding)
    (.tickFormat tick-format)
    (.ticks tick-count)))