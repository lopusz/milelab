package milelab.jtopics;

import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.IDSorter;


public class FriendlyParallelTopicModel extends ParallelTopicModel {

    public FriendlyParallelTopicModel (int numberOfTopics) {
        super(numberOfTopics);
    }

    public FriendlyParallelTopicModel(int numberOfTopics,
                                         double alphaSum, double beta) {
        super(numberOfTopics,alphaSum,beta);
    }

    public FriendlyParallelTopicModel(LabelAlphabet topicAlphabet,
                                         double alphaSum, double beta) {
        super(topicAlphabet, alphaSum, beta);
    }

    protected Map<String,Integer> calcTopicWordCount(int topicId) {
        HashMap<String,Integer> wordCount = new HashMap<String,Integer>();

        for (int doc = 0; doc < data.size(); doc++) {
            FeatureSequence tokenSequence = (FeatureSequence) data.get(doc).instance.getData();
	    LabelSequence topicSequence =  (LabelSequence) data.get(doc).topicSequence;

	    for (int pi = 0; pi < topicSequence.getLength(); pi++) {
                int curTopic = topicSequence.getIndexAtPosition(pi);

                if (curTopic!=topicId) continue;
                int type = tokenSequence.getIndexAtPosition(pi);

		String word=(String)alphabet.lookupObject(type);
                int count=1;
                if (wordCount.containsKey(word)) {
                    count=wordCount.get(word)+1;
                }
                wordCount.put(word,count);
	    }
	}
        return wordCount;
    }

    protected int calcTopicTotalCount(Map<String,Integer> wordCount) {
        int total=0;
        for (int c : wordCount.values()) {
            total+=c;
        }
        return total;
    }

    protected List<Map.Entry<String, Integer>> calcTopicSortedWordCountEntryList(Map<String,Integer> wordCount) {
       ArrayList<Map.Entry<String, Integer>> entryList =
            new ArrayList<Map.Entry<String,Integer>>( wordCount.entrySet() );
       Collections.sort( entryList, new Comparator<Map.Entry<String,Integer>>()
           {
               public int compare( Map.Entry<String,Integer> o1, Map.Entry<String,Integer> o2 )
               {
                  return (o2.getValue()).compareTo( o1.getValue() );
               }
            } );
       return entryList;
    }

    public Map<String,Double> getTopic(int topicId) {
        Map<String,Integer> wordCount=calcTopicWordCount(topicId);

        int total=calcTopicTotalCount(wordCount);

        LinkedHashMap<String,Double> wordProb = new LinkedHashMap<String,Double>();

        for(Map.Entry<String, Integer> entry : calcTopicSortedWordCountEntryList(wordCount)) {
            String word = entry.getKey();
            int count = entry.getValue();
            wordProb.put(word,(1.0*count)/total);
        }
        return wordProb;
    }

    public Map<String,Double> getTopicOnlyTopWords(int topicId, int n) {
        Map<String,Integer> wordCount=calcTopicWordCount(topicId);

        int total=calcTopicTotalCount(wordCount);

        LinkedHashMap<String,Double> wordProb = new LinkedHashMap<String,Double>();

        int i=1;

        for(Map.Entry<String, Integer> entry : calcTopicSortedWordCountEntryList(wordCount)) {
            i++;
            String word = entry.getKey();
            int count = entry.getValue();
            wordProb.put(word,(1.0*count)/total);
            if (i>n) break;
        }
        return wordProb;
    }

    public Map<String,Double> getTopicOnlyExceedingProbWords(int topicId, double minProb) {
        Map<String,Integer> wordCount=calcTopicWordCount(topicId);

        int total=calcTopicTotalCount(wordCount);

        LinkedHashMap<String,Double> wordProb = new LinkedHashMap<String,Double>();

        int i=1;

        for(Map.Entry<String, Integer> entry : calcTopicSortedWordCountEntryList(wordCount)) {
            i++;
            String word = entry.getKey();
            int count = entry.getValue();
            double prob=(1.0*count)/total;
            if (prob>minProb)
                wordProb.put(word,prob);
            else
                break;
        }
        return wordProb;
    }

    public Map<Object,Map<Long,Double>> getDocsTopics (int n, double minProb) {

  	int docLen;
	int[] topicCounts = new int[ numTopics ];

	IDSorter[] sortedTopics = new IDSorter[ numTopics ];
	for (int topic = 0; topic < numTopics; topic++) {
	   // Initialize the sorters with dummy values
	   sortedTopics[topic] = new IDSorter(topic, topic);
	}

	if (n < 0 || n > numTopics) {
            n = numTopics;
	}

        Map<Object,Map<Long,Double>>
            res = new LinkedHashMap<Object,Map<Long,Double>>();

	for (int doc = 0; doc < data.size(); doc++) {

	    LabelSequence topicSequence = (LabelSequence) data.get(doc).topicSequence;
	    int[] currentDocTopics = topicSequence.getFeatures();
            Object docName;
	    if (data.get(doc).instance.getName() != null) {
                docName=data.get(doc).instance.getName();
	    }
	    else {
		    docName="no-name";
	    }

	    docLen = currentDocTopics.length;

	    // Count up the tokens
	    for (int token=0; token < docLen; token++) {
                topicCounts[ currentDocTopics[token] ]++;
	    }

	    // And normalize
	    for (int topic = 0; topic < numTopics; topic++) {
		 sortedTopics[topic].set(topic, (alpha[topic] + topicCounts[topic]) / (docLen + alphaSum) );
	    }

	    Arrays.sort(sortedTopics);

            Map<Long,Double> docRes=new LinkedHashMap<Long,Double>();

	    for (int i = 0; i < n; i++) {
                if (sortedTopics[i].getWeight() < minProb) { break; }
                docRes.put(new Long(sortedTopics[i].getID()),sortedTopics[i].getWeight());
	    }
            res.put(docName,docRes);

	    Arrays.fill(topicCounts, 0);
        }
        return res;
    }
}
