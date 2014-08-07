(ns milelab.instances
  (:import [ cc.mallet.types
             Alphabet Instance InstanceList FeatureSequence]))

(defn- create-instance [ alphabet data-raw target name source ]
  (let [
         feature-sequence (FeatureSequence. alphabet)
         feature-sequence-add-f #(.add feature-sequence %)
         _ (dorun (map feature-sequence-add-f data-raw))
        ]
  (Instance. feature-sequence target name source)))

(defn create-instance-list
  ([ name-coll data-raw-coll ]
    (create-instance-list name-coll data-raw-coll (repeat nil) (repeat nil)))
  ([ name-coll data-raw-coll target-coll source-coll ]
    (let [
         alphabet (Alphabet.)
         instance-list (InstanceList. alphabet nil)
         instance-list-add-f
            #(.add instance-list
                   (create-instance alphabet %1 %2 %3 %4))
         _
           (dorun
             (map
               instance-list-add-f
               data-raw-coll target-coll name-coll source-coll))
        ]
      instance-list)))
