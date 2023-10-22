(ns tvt.a7.profedit.distances
  (:require [seesaw.core :as sc]
            [tvt.a7.profedit.widgets :as w]
            [tvt.a7.profedit.profile :as prof]
            [seesaw.border :refer [empty-border]]
            [tvt.a7.profedit.config :as conf]
            [dk.ative.docjure.spreadsheet :as sp]
            [j18n.core :as j18n]
            [clojure.spec.alpha :as s]
            [tvt.a7.profedit.fio :as fio])
  (:import [javax.swing JList]))


(defn- del-selected! [*state ^JList d-lb]
  (sc/invoke-later
   (let [idx (.getSelectedIndex d-lb)
         zeroing-dist-idx? (fn [state dist-idx]
                             (let [zd-idx (prof/get-in-prof
                                           state
                                           [:c-zero-distance-idx])]
                               (= zd-idx dist-idx)))]
     (if (> idx -1)
       (swap! *state
              (fn [state]
                (let [cur-val (prof/get-in-prof state [:distances])
                      new-val (into (subvec cur-val 0 idx)
                                    (subvec cur-val (inc idx)))
                      cur-zidx (prof/get-in-prof state [:c-zero-distance-idx])
                      new_zidx (if (> cur-zidx idx) (dec cur-zidx) cur-zidx)]
                  (if (zeroing-dist-idx? state idx)
                    (do (prof/status-err! ::del-sel-cant-delete)
                        state)
                    (do (prof/status-ok! ::del-sel-distance-deleted)
                        (-> state
                            (prof/assoc-in-prof [:c-zero-distance-idx] new_zidx)
                            (prof/assoc-in-prof [:distances] new-val)))))))
       (prof/status-err! ::del-sel-select-for-deletion)))))




#_(defn mk-firmware-update-dialogue
  [frame {:keys [device serial version] :as entry}]
  (sc/invoke-later
   (let [action (sc/input
                 frame
                 (format (j18n/resource ::firmware-update-text)
                         device
                         serial
                         version
                         (:version (:newest-firmware entry)))
                 :title (j18n/resource ::firmware-update-title)
                 :choices [::update-firmware-now
                           ::undate-firmware-later]
                 :value ::update-firmware-now
                 :type :question
                 :to-string j18n/resource)]
     (when (= action ::update-firmware-now)
       (try
         (fio/copy-newest-firmware entry)
         (sc/alert frame (j18n/resource ::firmware-uploaded) :type :info)
         (catch Exception e (sc/alert frame (.getMessage e) :type :error)))))))


(defn- dist-swapper [state distances]
  (let [zero-dist (nth (prof/get-in-prof state [:distances])
                       (prof/get-in-prof state [:c-zero-distance-idx]))
        {:keys [units min-v max-v]}
        (meta (s/get-spec ::prof/distance))
        new-dist (into [zero-dist] (map parse-double) distances)]
    (if (s/valid? ::prof/distances new-dist)
      (-> state
          (prof/assoc-in-prof [:distances] new-dist)
          (prof/assoc-in-prof [:c-zero-distance-idx] 0))
      (throw (Exception. (format "Distances should be in %s units and range from %d to %d" (j18n/resource units) min-v max-v))))))

(defn- import-from-excel [*state]
  (try
    (let [wb (w/load-excel-from-chooser)
          hv (w/workbook->header-vec wb)
          c-hv (count hv)]
      (cond
        (= 0 c-hv)
        (throw (Exception. (str "First spreadsheet should have at least one column")))

        (= 1 c-hv)
        (swap! *state dist-swapper (w/get-workbook-column wb 0))

        :else
        (sc/invoke-now (sc/alert "Multi column loading not implemented")))
    ;; TODO Add succ status
      )
    (catch Exception e (prof/status-err!
                        (let [em (.getMessage e)]
                          (if (seq em) em (format "Bad Excel table"))))
           nil)))


(defn- export-to-excel [*state]
  (try
    (let [distances (prof/get-in-prof* *state [:distances])
          {:keys [units]} (meta (s/get-spec ::prof/distance))
          wb (sp/create-workbook "Distances"
                                 (into [[(format "Distances (%s)"
                                                 (j18n/resource units))]]
                                       (map #(vector (str %)))
                                       distances))]
      (dorun (for [sheet (sp/sheet-seq wb)]
               (sp/auto-size-all-columns! sheet)))
      (w/save-excel-as-chooser *state wb))
    ;; TODO Add succ status
    (catch Exception e (prof/status-err! (.getMessage e)) nil)))


(defn make-dist-panel [*state]
  (let [d-lb (w/distances-listbox *state)

        btn-del (sc/button
                 :icon (conf/key->icon :distances-button-del-icon)
                 :text ::dist-pan-delete-selected
                 :listen [:action (fn [_] (del-selected! *state d-lb))])

        btn-e-imp (sc/button
                   :text "import"
                   :listen [:action (fn [_] (import-from-excel *state))])

        btn-e-exp (sc/button
                   :text "export"
                   :listen [:action (fn [_] (export-to-excel *state))])]

    (sc/border-panel
     :hgap 20
     :vgap 20
     :border (empty-border :thickness 5)
     :north (sc/label :text :tvt.a7.profedit.distances/distances-tab-header
                      :class :fat)
     :center (sc/border-panel
              :vgap 5
              :hgap 5
              :north (sc/label :text ::dist-pan-distances-reorder
                               :font conf/font-small)
              :center (sc/border-panel
                       :north (w/input-distance *state)
                       :center (sc/scrollable d-lb))
              :south (sc/horizontal-panel
                      :items [btn-del
                              (sc/separator :orientation :vertical)
                              (->> :file-excel
                                   conf/key->icon
                                   sc/icon
                                   (sc/label :icon))
                              (sc/vertical-panel :items [btn-e-imp
                                                         btn-e-exp])])))))
