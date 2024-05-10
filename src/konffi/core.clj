(ns core
  "Malli + Aero = Konffi
  Define your configurations as Malli schemas.
  Use Aero reader tags to collect the values from the environment."
  (:require [aero.core :as aero]
            [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]))

(def transformer
  (mt/default-value-transformer
   {:key :value
    :defaults {:map    (constantly {})
               :string (constantly "")
               :vector (constantly [])}}))

(defn read-config
  ([filename]
   (read-config filename {:transformer transformer}))
  ([filename {:keys [transformer]}]
   (let [schema (try (m/schema (aero/read-config filename))
                     (catch Exception e
                       (throw (ex-info "Invalid configuration schema"
                                       {:malli/error (ex-data e)}))))
         config (m/decode schema nil transformer)]
     (when-not (m/validate schema config)
       (throw (ex-info "Invalid configuration"
                       {:value config
                        :error  (me/humanize (m/explain schema config))})))
     config)))

(comment
  (require '[clojure.java.io :as io])
  (read-config (io/resource "config.edn")))
