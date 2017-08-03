package com.ericsson.ei.jmespath;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;

public class DiffFunction extends BaseFunction {
    public DiffFunction() {
        super(ArgumentConstraints.listOf(2, Integer.MAX_VALUE, ArgumentConstraints.typeOf(JmesPathType.NUMBER)));
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
        double diff = 0;
        T value1 = arguments.get(0).value();
        T value2 = arguments.get(1).value();
        double num1 = runtime.toNumber(value1).doubleValue();
        double num2 = runtime.toNumber(value2).doubleValue();
        diff = num1 - num2;
        return runtime.createNumber(diff);
    }

}
