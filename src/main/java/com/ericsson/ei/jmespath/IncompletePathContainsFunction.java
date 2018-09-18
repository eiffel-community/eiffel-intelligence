package com.ericsson.ei.jmespath;

import com.github.wnameless.json.flattener.JsonFlattener;

import java.util.List;
import java.util.Map;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class IncompletePathContainsFunction extends BaseFunction {

    public IncompletePathContainsFunction() {
        super(ArgumentConstraints.listOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT),
                ArgumentConstraints.typeOf(JmesPathType.STRING), ArgumentConstraints.typeOf(JmesPathType.STRING)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.burt.jmespath.function.BaseFunction#callFunction(io.burt.jmespath.
     * Adapter, java.util.List)
     * 
     * Takes a JSON object and a path with value. The path can only contain
     * parts of a path. For example paths a.b.c.d.e and b.d.c.d.e are valid for
     * incomplete path c.e since both elements of the incomplete path are
     * contained in the example paths.
     * 
     * But the value should also match.
     */
    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        T value1 = arguments.get(0).value();
        T value2 = arguments.get(1).value();
        T value3 = arguments.get(2).value();

        String object = runtime.toString(value1);
        String path = runtime.toString(value2);
        String pathValue = runtime.toString(value3);
        String[] pathPair = path.split(":");
        String pathKey = pathPair[0];
        String[] pathParts = pathKey.split("\\.");

        boolean result = objectContainsIncompletePathWithValue(object, pathParts, pathValue);

        return runtime.createBoolean(result);
    }

    private boolean objectContainsIncompletePathWithValue(String object, String[] pathParts, String pathValue) {
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(object);

        int lastPosition = -1;
        for (Map.Entry<String, Object> entry : flattenJson.entrySet()) {
            String entryKey = entry.getKey();
            Object entryValue = entry.getValue();
            String entryValueString = entry.getValue().toString();

            if (entryValue != null && entryValueString.equals(pathValue)) {
                int index = 0;
                for (String pathPart : pathParts) {
                    int position = entryKey.indexOf(pathPart, lastPosition);
                    if (index > 0) {
                        // a path part should be followed by a dot in
                        // the entry key
                        String subString = entryKey.substring(position - 1, position);
                        if (!subString.equals(".")) {
                            lastPosition = -1;
                            break;
                        }
                    }
                    if (position > lastPosition) {
                        lastPosition = position + pathPart.length();
                    } else {
                        // reset to start no complete sequence valid
                        lastPosition = -1;
                    }
                    index++;
                }
                // all path parts found and in right order
                if (lastPosition >= 0)
                    return true;
            }
        }

        return false;
    }
}
