package com.ericsson.ei.jmespath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.wnameless.json.flattener.JsonFlattener;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class IncompletePathFilterFunction extends BaseFunction {

    public IncompletePathFilterFunction() {
        super(ArgumentConstraints.listOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT), ArgumentConstraints.typeOf(JmesPathType.STRING)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.burt.jmespath.function.BaseFunction#callFunction(io.burt.jmespath.Adapter, java.util.List)
     * 
     * This takes JSON object and a key. The key can contain the whole path or only a part of it.
     * It search through the whole object after the values that have same key and returns a Map that
     * contains a key and a list of all found values.
     */
    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T value1 = arguments.get(0).value();
        T value2 = arguments.get(1).value();

        String object = runtime.toString(value1);
        String key = runtime.toString(value2);

        T result = null;
        ArrayList<String> arrayResult = filterObjectWithIncompletePath(object, key);
        if(arrayResult.isEmpty() || arrayResult == null) {
            result = runtime.createString(null);
        } else if(arrayResult.size() == 1) {
            result = runtime.createString(arrayResult.get(0));
        } else {
            result = runtime.createString(arrayResult.toString());
        }

        return result;
    }

    private ArrayList<String> filterObjectWithIncompletePath(String object, String key) {
        Map<String, Object> flattJson = JsonFlattener.flattenAsMap(object);
        ArrayList<String> resultArray = new ArrayList<String>();
        List<String> keyParts = Arrays.asList(key.split("\\."));
        for (Entry<String, Object> setElement : flattJson.entrySet()) {
            //System.out.println("set element : " + setElement);
            String elementKey = setElement.getKey();
            List<String> elementKeyParts = Arrays.asList(elementKey.split("\\."));
            int index = 0;
            int lastPartIndex = keyParts.size() - 1;
            String ending = keyParts.get(lastPartIndex);
            if (elementKey.endsWith(ending)) {
                for (int i = 0; i < keyParts.size(); i++) {
                    String keyPart = keyParts.get(i);

                    int tempIndex = -1;
                    for(int j = 0; j < elementKeyParts.size(); j++) {
                        if(elementKeyParts.get(j).contains(keyPart)) {
                            tempIndex = j;
                        }
                    }

                    if (index != -1 && tempIndex >= index) {
                        index = tempIndex;
                        if (index == elementKeyParts.indexOf(ending)) {
                            if (setElement.getValue() == null) {
                                resultArray.add("null");
                            } else {
                                resultArray.add(setElement.getValue().toString());
                            }
                        }
                    } else {
                        index = -1;
                    }
                }
            }
        }

        return resultArray;

    }

}
