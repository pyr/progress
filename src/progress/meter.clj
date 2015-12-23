(ns progress.meter
  "Nice console progress meters for sequences")

(def bar-width 10)

(defn epoch
  []
  (quot (System/currentTimeMillis) 1000))

(defn hms
  [t]
  (let [s (rem t 60)
        m (quot t 60)
        h (quot m 60)]
    (if (pos? h)
      [h (rem m 60) s]
      [nil m s])))

(defn format-interval
  [t]
  (let [[h m s] (hms t)]
    (if h
      (format "%d:%02d:%02d" (long h) (long m) (long s))
      (format "%02d:%02d" (long m) (long s)))))

(defn draw-bar
  [bar-length bar-width]
  (reduce str (concat (repeat bar-length "#")
                      (repeat (- bar-width bar-length) "-"))))
(defn format-meter
  ([n total elapsed]
   (if (> n total)
     (format-meter n elapsed)
     (let [frac       (double (/ n total))
           bar-length (long (* frac bar-width))
           bar        (draw-bar bar-length bar-width)
           percentage (format "%3d%%" (long (* 100 frac)))]
       (format "|%s| %d/%d %s [elapsed: %s]"
               bar n total percentage
               (format-interval elapsed)))))
  ([n elapsed]
   (format "%d [elapsed: %s]" n (format-interval elapsed))))

(defn update-meter
  [{:keys [n start-ts last? total last-len] :as params}]
  (let [processed (inc n)
        ts        (epoch)
        elapsed   (- ts start-ts)
        text      (format-meter (inc n) total elapsed)
        len       (count text)
        pad       (reduce str (repeat (- last-len len) " "))]
    (print (format "\r%s%s" text pad))
    (when last?
      (println ""))
    (flush)
    (assoc params :last-len len :n processed)))

(defn progress-seq
  ([input]
   (let [s (seq input)]
     (progress-seq s {:n 0 :start-ts (epoch) :total (count s) :last-len 0})))
  ([[head & tail] params]
   (let [params (update-meter (assoc params :last? (nil? tail)))]
     (cons head (when tail (lazy-seq (progress-seq tail params)))))))
