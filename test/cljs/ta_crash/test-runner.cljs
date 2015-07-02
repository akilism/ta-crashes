(ns ta_crash.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [ta_crash.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'ta_crash.core-test))
    0
    1))
