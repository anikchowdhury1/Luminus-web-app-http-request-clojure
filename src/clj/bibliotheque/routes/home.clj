(ns bibliotheque.routes.home
  (:require [bibliotheque.layout :as layout]
            [bibliotheque.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [ring.util.response :refer [redirect]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clj-http.util :as utility]
            [clojure.data.csv :as csv]
            [clojure.pprint :refer [pprint]]
            [bibliotheque.config :refer [env]]
            [taoensso.timbre :as log]
            [clojure.core.async :as async]
            [clojure.java.jdbc :as j]
            [clojure.edn :as edn]
            [bibliotheque.routes.parallel :refer [spawn-task-executors]]))



(def test-atom (atom ""))
(def processing-status "processing")
(def status-complete "done")
(def upload-file-status (atom "done"))


(def n-tasks 100)
(def n-threads 12)

(defn uuid
  []
  (str (java.util.UUID/randomUUID)))


(defrecord csv-data-details [track-id date input-file output-file])



(defn home-page [{:keys [flash]}]
  (let [csv-lists (db/get-N-csv-list {:n (env :row-limit)})]
    #_(prn "csv-lists" (merge {:csv-lists csv-lists}
                         (select-keys flash [:errors])))
    (layout/render
      "home.html"
      (merge {:csv-lists csv-lists}
        (select-keys flash [:errors])))))

(defn about-page []
  (layout/render "about.html"))


(defn new-page [{:keys [flash]}]
  (prn "checking: " (merge {:upload-processing @upload-file-status}
                      (select-keys flash [:errors])))
  (layout/render "new.html" (merge {:upload-processing @upload-file-status}
                              (select-keys flash [:errors]))))

(defn validate-list [params]
  (first
    (b/validate
      params
      :uuid [v/required v/string]
      :date_time [v/required v/string]
      :input_file_path [v/required v/string]
      :output_file_path [v/required v/string]
      :input_file_name [v/required v/string]
      :output_file_name [v/required v/string]
      :status [v/required v/string])))



(defn save-data->db
  [uuid date_time input_file_path output_file_path input_file_name output_file_name status]
  (prn "atom data: date->" date_time "input_file_path: " input_file_path
    "output_file_path: " output_file_path "input_file_name: " input_file_name
    "output_file_name: " output_file_name "status: " status)
  (let [data-list-map {:uuid             uuid
                       :date_time        date_time
                       :input_file_path  input_file_path
                       :output_file_path output_file_path
                       :input_file_name  input_file_name
                       :output_file_name output_file_name
                       :status           status}]

    (if-let [errors (validate-list data-list-map)]
      (-> (response/found "/home")
        (assoc :flash (assoc data-list-map :errors errors)))
      (do
        (db/create-csv-list! data-list-map)))))



(defn parse-int [s]
  (try
    (Integer/parseInt s)
    (catch Throwable t
      nil)))

(defn add-vendor-campaign-column [coll merge-data]
  (let [values (vec coll)]
    (doall
      (map (fn [a b]
             (conj a b))
        values
        merge-data))))

(defn append-csv
  [filename merge-data]
  (with-open [in-file (io/reader filename)]
    (doall
      (let [out-data      (add-vendor-campaign-column (csv/read-csv in-file) merge-data)
            dump-csv-file (io/file (env :output-file-path) (str (env :download-tag) @test-atom))]
        (with-open [out-file (io/writer dump-csv-file)]
          (csv/write-csv out-file out-data))))))


(defn test-lead
  [test_lead]
  (try
    (let [base_url             (env :base_url)
          get_url_path         (env :get_url_path)
          url                  (str base_url get_url_path test_lead)
          result               (:body (http/get url {:query-params     {:API_user (env :username)
                                                                        :API_pass (env :password)}
                                                     :debug?           false
                                                     :timeout          5
                                                     :content-type     :json
                                                     :throw-exceptions false
                                                     :coerce           :always
                                                     :as               :json-strict}))
          Auto_vendor_campaign (get-in result [:source :campaign])]


      Auto_vendor_campaign)
    (catch Exception e)))



(defn test-http
  [test_num]
  (try
    (let [base_url             (env :base_url)
          search_url_path      (env :search_url_path)
          url                  (str base_url search_url_path test_num)
          result               (:body (http/get url {:query-params     {:API_user (env :username)
                                                                        :API_pass (env :password)}
                                                     :debug?           false
                                                     :timeout          5
                                                     :content-type     :json
                                                     :throw-exceptions false
                                                     :coerce           :always
                                                     :as               :json-strict}))
          first_key            (:itemId (into {} (vals result)))
          Auto_vendor_campaign (test-lead first_key)]

      (if (nil? Auto_vendor_campaign)
        "NA"
        Auto_vendor_campaign))
    (catch Exception e)))



(defn process-request-func
  [{:as   data
    :keys [input-val result-chan]}]
  ;(log/debug (format "Starting task: %s and id: %s ..." input-val data))


  ;; Simulate long/complex task by sleeping for a random period.
  (Thread/sleep (+ 500 (rand-int 500)))

  ;; Push the output value to output channel.
  (let [id (first input-val)
        output-val (test-http (first (first (rest input-val))))]
    (async/>!! result-chan
      {:input-val  input-val
       :id  id
       :output-val output-val})))


(defn csv_read_func [filename]
  (with-open [f (io/reader filename)]
    (let [rows (rest (into [] (csv/read-csv f)))
          count-rows (count rows)]

      (let [result-chan (async/chan (async/buffer n-tasks))

            map-data-id (loop [rest-rows rows
                               phone-map {}
                               id 65]
                          (let [split-row-data rest-rows
                                phn-map (into {} {(keyword (str (char id))) (first split-row-data)})]
                            (if-not (seq rest-rows)
                              phone-map
                              (recur (rest rest-rows)
                                (conj phone-map phn-map)
                                (inc id)))))

            phone-map (vec (sort-by first map-data-id))

            ;; Prepare tasks.
            tasks (for [[k v] phone-map]
                    (let [id-data [k v]]
                      {:input-val   id-data
                       :result-chan result-chan}))

            ;; Spawn task executors.
            data-chan (spawn-task-executors process-request-func n-threads)]


        ;; Push all the tasks to data channel.
        (doseq [task tasks]
          (async/>!! data-chan task))

        ;; Wait for results to complete.
        (let [results (loop [results    []
                             map-data {}
                             new-result (async/<!! result-chan)]
                        (if new-result
                          (do
                            ;(prn "new result: " new-result)
                            (let [results (conj results new-result)
                                  data-id (:id new-result)
                                  vendor-data (:output-val new-result)
                                  new-map-val (into {} {data-id vendor-data})
                                  map-data (conj map-data new-map-val)]
                              (if (= (count results) count-rows)
                                ;; We have received all the results.
                                map-data

                                ;; We have not yet received all the results.
                                ;; Need to wait for more.
                                (recur results
                                  map-data
                                  (async/<!! result-chan)))))

                          ;; The result channel was closed somehow. Time to show partial results.
                          results))
              fetched-data (vec (vals (into (sorted-map) results)))
              final-fetched-result ["Auto_Vendor_Campaign"]
              final-result (vec (concat final-fetched-result fetched-data))]


          (append-csv filename final-result)
          ;(prn "Processing done...")

          ;; Close data channel. Otherwise, the spawned threads wont close.
          (async/close! data-chan)

          ;; Close result channel.
          (async/close! result-chan))))))




(defn upload-handler [{:keys [filename content-type tempfile size filepath]}]
  (let [temp-csv-file (str (uuid) ".csv")
        uuid          (uuid)]
    (prn "hello" temp-csv-file)
    (reset! test-atom temp-csv-file)
    (reset! upload-file-status processing-status)
    (save-data->db
      uuid
      (str (java.util.Date.))
      (str (env :dest-path) temp-csv-file)
      (str (env :dest-path) (env :download-tag) temp-csv-file)
      filename
      (str (env :download-tag) filename)
      processing-status)

    (io/copy (io/file tempfile) (io/file (str (env :dest-path) temp-csv-file)))
    ;(prn "file details: " (.getAbsolutePath tempfile))
    (io/delete-file (.getAbsolutePath tempfile))
    ;(prn "details filename: " (io/as-relative-path (.getAbsolutePath filename)))
    (csv_read_func (str (env :dest-path) temp-csv-file))
    (prn "latest-id" uuid)
    (db/update-status {:uuid   uuid
                       :status status-complete})
    (reset! upload-file-status status-complete)
    (response/ok {:status :ok})))


(defn String->Number [str]
  (let [n (read-string str)]
    (if (number? n)
      n
      nil)))

(defn download-input-file [id]
  (let [input-file-path (db/get-input-file-path-by-id {:id (String->Number id)})]
    (prn "input file path: " input-file-path)
    (response/ok (-> (:input_file_path input-file-path)
                   slurp))))

(defn download-output-file [id]
  (let [output-file-path (db/get-output-file-path-by-id {:id (String->Number id)})]
    (prn "output file path: " output-file-path)
    (response/ok (-> (:output_file_path output-file-path)
                   slurp))))


(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/new" request (new-page request))
  (POST "/upload" [file] (upload-handler file))
  (GET "/download" [] (response/ok (-> (str (env :dest-path) (env :download-tag) @test-atom)
                                     slurp)))
  (GET "/i-file" [id] (download-input-file id))
  (GET "/o-file" [id] (download-output-file id)))


