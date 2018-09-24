package com.ericsson.ei.jmespath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.controller.QueryControllerImpl;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class IncompletePathFilterFunction extends BaseFunction {

    public IncompletePathFilterFunction() {
        super(ArgumentConstraints.listOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT), ArgumentConstraints.typeOf(JmesPathType.STRING)));
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryControllerImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see io.burt.jmespath.function.BaseFunction#callFunction(io.burt.jmespath.Adapter, java.util.List)
     * 
     * This takes JSON object and a key. The key can contain the whole path, parts of path, or only a simple key.
     * It search through the whole object after the values that have same key and returns a Map that contains
     * a key and a list of all found values.
     */
    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T value1 = arguments.get(0).value();
        T value2 = arguments.get(1).value();

        String object = runtime.toString(value1);
        String key = runtime.toString(value2);

        T result = null;
        ArrayList<String> arrayResult = filterObjectWithIncompletePath(object, key);
        if (arrayResult == null || arrayResult.isEmpty()) {
            result = runtime.createString(null);
        } else if (arrayResult.size() == 1) {
            result = runtime.createString(arrayResult.get(0));
        } else {
            result = runtime.createString(arrayResult.toString());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * Flatten an object and creates a list with the parts of search key. Returns array that contains filtered values.
     */
    private ArrayList<String> filterObjectWithIncompletePath(String object, String key) {
        Map<String, Object> flattJson = flatten(object);
        ArrayList<String> resultArray = new ArrayList<String>();
        List<String> keyParts = Arrays.asList(key.split("\\."));
        loopThroughAllPaths(resultArray, flattJson, keyParts);

        return resultArray;
    }

    private void loopThroughAllPaths(ArrayList<String> resultArray, Map<String, Object> flattJson, List<String> keyParts) {
        for (Entry<String, Object> elementOfSet : flattJson.entrySet()) {
            filterPathsThatContainSearchKey(resultArray, elementOfSet, keyParts);
        }
    }

    /*
     * (non-Javadoc)
     *
     * To minimize the amount of tested paths, an if statement checks if the current path ends with required key. Loop loops through the list with
     * parts of search key. Returns array that contains filtered values.
     */
    private void filterPathsThatContainSearchKey(ArrayList<String> resultArray, Entry<String, Object> elementOfSet, List<String> keyParts) {
        String elementKey = elementOfSet.getKey();
        List<String> elementKeyParts = Arrays.asList(elementKey.split("\\."));
        int index = 0;
        int lastPartIndex = keyParts.size() - 1;
        String ending = keyParts.get(lastPartIndex);
        if (elementKey.endsWith(ending)) {
            for (int i = 0; i < keyParts.size(); i++) {
                String keyPart = keyParts.get(i);
                int tempIndex = -1;

                tempIndex = checkIfArray(elementKeyParts, keyPart, tempIndex);
                index = checkIfCorrectOrder(index, tempIndex, elementKeyParts, ending, elementOfSet, resultArray);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * It needs to be checked if the part of search key is a key for the array or not. And if user wants to search in all array's elements or only one
     * specific.
     */
    private int checkIfArray(List<String> elementKeyParts, String keyPart, int tempIndex) {
        for (int j = 0; j < elementKeyParts.size(); j++) {

            if (keyPart.contains("[")) {
                tempIndex = getIndexIfEqual(elementKeyParts.get(j), keyPart, j, tempIndex);
            } else {
                String elementPartWithoutBracket = Arrays.asList(elementKeyParts.get(j).split("\\[")).get(0);
                tempIndex = getIndexIfEqual(elementPartWithoutBracket, keyPart, j, tempIndex);
            }
        }
        return tempIndex;
    }

    private int getIndexIfEqual(String elementPart, String keyPart, int currentIndex, int tempIndex) {
        if (elementPart.equals(keyPart)) {
            return currentIndex;
        }
        return tempIndex;
    }

    /*
     * (non-Javadoc)
     *
     * Checks if index is higher then -1 and if tempIndex for the current part of search key is higher then the index of the previous part. If the
     * order is incorrect, index is set to -1.
     */
    private int checkIfCorrectOrder(int index, int tempIndex, List<String> elementKeyParts, String ending, Entry<String, Object> elementOfSet,
                                    ArrayList<String> resultArray) {
        if (index != -1 && tempIndex >= index) {
            index = tempIndex;
            checkIfEndOfPath(index, elementKeyParts, ending, elementOfSet, resultArray);
        } else {
            index = -1;
        }
        return index;
    }

    private void checkIfEndOfPath(int index, List<String> elementKeyParts, String ending, Entry<String, Object> elementOfSet,
                                  ArrayList<String> resultArray) {
        if (index == elementKeyParts.indexOf(ending)) {
            updateResultArray(elementOfSet, resultArray);
        }
    }

    /*
     * (non-Javadoc)
     *
     * If value is null, it creates a string with null as text and adds it to resultArray, in other case it adds value.
     */
    private void updateResultArray(Entry<String, Object> elementOfSet, ArrayList<String> resultArray) {
        if (elementOfSet.getValue() == null) {
            resultArray.add("null");
        } else {
            resultArray.add(elementOfSet.getValue().toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * Flatten object and creates all possible key combinations (In correct order) with values.
     * Ex. a = {b.c.d}; a.b = {c.d}, etc.
     */
    private Map<String, Object> flatten(String object) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {

            addKeys("", result, object);

        } catch (Exception e) {
            LOGGER.error("Failed to flatten an object\n: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * Iterate through an object and adds, its keys combinations together with values, to a map.
     */
    private void addKeys(String prevKey, Map<String, Object> result, String object) {
        try {
            JSONObject json = (JSONObject) JSONParser.parseJSON(object);
            Iterator<?> iter = json.keys();
            while (iter.hasNext()) {
                String key = iter.next().toString();
                result.put(prevKey + key, json.get(key));
                if (json.get(key) instanceof JSONObject) {
                    addKeys(prevKey + key + ".", result, json.get(key).toString());
                } else if (json.get(key) instanceof JSONArray) {
                    for (int i = 0; i < ((JSONArray) json.get(key)).length(); i++) {
                        addKeys(prevKey + key + "[" + i + "].", result, ((JSONArray) json.get(key)).get(i).toString());
                    }
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Failed to add key\n: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
