(ns tvt.a7.profedit.util
  (:require [seesaw.core :as sc])
  (:import [javax.swing JFrame]))


(defn maximized?
  [^javax.swing.JFrame frame]
  (= (.getExtendedState frame) JFrame/MAXIMIZED_BOTH))


(defn maximize!
  [^javax.swing.JFrame frame]
  (sc/show! frame)
  (sc/invoke-later (.setExtendedState frame JFrame/MAXIMIZED_BOTH)))


(defn reload-frame! [frame frame-cons]
  (sc/invoke-later
   (let [was-maximized? (maximized? frame)]
     (sc/config! frame :on-close :nothing)
     (sc/dispose! frame)
     (if was-maximized?
       (maximize! (frame-cons))
       (sc/show! (frame-cons))))))


(defn dispose-frame! [frame]
  (sc/invoke-later
   (sc/config! frame :on-close :nothing)
   (sc/dispose! frame)))


(defn mean [vals]
  (/ (reduce + vals) (count vals)))


(defn linear-regression-coefficients [data]
  (let [x-mean (mean (map :temperature data))
        y-mean (mean (map :speed data))
        numerator (reduce + (map (fn [{:keys [temperature speed]}]
                                   (* (- temperature x-mean) (- speed y-mean)))
                               data))
        denominator (reduce + (map (fn [{:keys [temperature]}]
                                     (Math/pow (- temperature x-mean) 2))
                               data))]
    {:m (/ numerator denominator)
     :b (- y-mean (* (/ numerator denominator) x-mean))}))


(defn calculate-percent-change-linear-regression [data]
  (let [{:keys [m]} (linear-regression-coefficients data)
        avg-speed (mean (map :speed data))]
    (* 1500 (/ m avg-speed))))
