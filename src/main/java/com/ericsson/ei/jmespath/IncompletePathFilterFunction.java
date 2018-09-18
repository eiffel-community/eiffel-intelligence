package com.ericsson.ei.jmespath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

        Map<T, T> result = new HashMap<>();
        T resValues = runtime.createString(filterObjectWithIncompletePath(object, key).toString());
        result.put(value2, resValues);
        return runtime.createObject(result);

    }

    private ArrayList<String> filterObjectWithIncompletePath(String object, String key) {
        Map<String, Object> flattJson = JsonFlattener.flattenAsMap(object);
        ArrayList<String> resultArray = new ArrayList<String>();

        for (Entry<String, Object> e : flattJson.entrySet()) {
            if (e.getKey().endsWith(key)) {
                resultArray.add(e.getValue().toString());
            }
        }

        return resultArray;

    }

}
