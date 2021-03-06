(ns milelab.topics
  (:require [ milelab.instances :refer [create-instance-list]])
  (:import [milelab.jtopics FriendlyParallelTopicModel]))

(defn estimate-topics
  [ instance-list num-topics
    & { :keys [ alpha-sum beta random-seed num-threads num-iter ]
        :or { alpha-sum nil
              beta nil
              random-seed nil
              num-threads (.availableProcessors (Runtime/getRuntime))
              num-iter 100 }}]
  (let [
         parallel-topic-model
           (if (and alpha-sum  beta)
             (FriendlyParallelTopicModel. num-topics alpha-sum beta)
             (FriendlyParallelTopicModel. num-topics))
         _
            (when random-seed
                (. parallel-topic-model setRandomSeed random-seed))
        ]
    (doto parallel-topic-model
      (.addInstances instance-list)
      (.setNumIterations num-iter)
      (.setNumThreads num-threads)
      (.estimate))))

(defn get-topic [ parallel-topic-model topic-id ]
  (. parallel-topic-model getTopic topic-id))

(defn get-topic-only-top-words [ parallel-topic-model topic-id n]
  (. parallel-topic-model getTopicOnlyTopWords topic-id n))

(defn get-topic-only-exceeding-prob-words
  [ parallel-topic-model topic-id min-prob]
  (. parallel-topic-model getTopicOnlyExceedingProbWords topic-id min-prob))

(defn get-docs-topics [ parallel-topic-model n min-prob ]
  (. parallel-topic-model getDocsTopics n min-prob))
