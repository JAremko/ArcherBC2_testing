(ns tvt.a7.profedit.calc
  (:require [clojure.spec.alpha :as s]
            [tvt.a7.profedit.widgets :as w]
            [tvt.a7.profedit.nullableinp :as ni]
            [seesaw.border :refer [empty-border]]
            [seesaw.forms :as sf]
            [seesaw.core :as sc]
            [tvt.a7.profedit.profile :as prof])
  (:import [numericutil CustomNumberFormatter]))


(def ^:private row-count 10)


(defn- mean [vals]
  (/ (reduce + vals) (count vals)))


(defn- linear-regression-coefficients [data]
  (let [x-mean (mean (map :temperature data))
        y-mean (mean (map :velocity data))
        numerator (reduce + (map (fn [{:keys [temperature velocity]}]
                                   (* (- temperature x-mean) (- velocity y-mean)))
                                 data))
        denominator (reduce + (map (fn [{:keys [temperature]}]
                                     (Math/pow (- temperature x-mean) 2))
                                   data))]
    {:m (/ numerator denominator)
     :b (- y-mean (* (/ numerator denominator) x-mean))}))


(defn- calculate-percent-change-linear-regression [data]
  (let [{:keys [m]} (linear-regression-coefficients data)
        avg-velocity (mean (map :velocity data))]
    (* 1500 (/ m avg-velocity))))


(defn- make-pwdr-sens-calc-state []
  {:profile {:pwdr-sens-table (->> {:temperature nil :velocity nil}
                                   constantly
                                   (repeatedly)
                                   (into [] (take row-count)))}})


(s/def ::temperature (s/nilable ::prof/c-zero-p-temperature))


(s/def ::velocity (s/nilable ::prof/c-muzzle-velocity))


(s/def ::pwdr-sens-table-row (s/keys :req-un [::temperature ::velocity]))


(s/def ::pwdr-sens-table (s/coll-of ::pwdr-sens-table-row
                                    :count row-count
                                    :kind vector))


(defn- mk-nulable-number-fmt
  [_ fraction-digits]
  (proxy [CustomNumberFormatter] []
    (stringToValue
      (^clojure.lang.Numbers [^java.lang.String s]
       (w/str->double s fraction-digits)))

    (valueToString
      (^java.lang.String [^clojure.lang.Numbers value]
       (if value
         (w/val->str (double value) fraction-digits)
         "")))))


(defn- nulable-input-num [& args]
  (apply ni/create-input mk-nulable-number-fmt args))


(defn- make-pwdr-sens-calc-row [*calc-state idx]
  [(nulable-input-num *calc-state
                      [:pwdr-sens-table idx :temperature]
                      ::prof/c-zero-p-temperature :columns 5)
   (nulable-input-num *calc-state
                      [:pwdr-sens-table idx :velocity]
                      ::prof/c-muzzle-velocity :columns 5)])


(defn- make-pwdr-sens-calc-children [*calc-state]
  (let [calc-state (deref *calc-state)
        rows (get-in calc-state [:profile :pwdr-sens-table])
        num-rows (count rows)]
    (into [(sf/span (sc/label :text ::ballistic-table :class :fat) 3) (sf/next-line)]
          (mapcat
           (partial make-pwdr-sens-calc-row *calc-state))
          (range 0 num-rows))))


(defn- make-func-coefs [*calc-state]
  (sc/border-panel :border (empty-border :thickness 20)
                   :center (sc/scrollable
                            (sf/forms-panel
                             "pref,4dlu,pref"
                             :items (make-pwdr-sens-calc-children *calc-state)))))


(def ^:private *c-s (atom (make-pwdr-sens-calc-state)))


(defn show-pwdr-sens-calc-frame [*state parent]
  (try
    (add-watch *c-s :refresh-*state (fn [key atom old-state new-state]
                                     #_ (calculate-percent-change-linear-regression)))
    (sc/show! (sc/pack! (sc/dialog :parent parent
                                   :title ::calc-title
                                   :modal? true
                                   :content (make-func-coefs *c-s))))
    (finally (remove-watch *c-s :refresh-*state))))
