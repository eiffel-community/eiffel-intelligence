/*
   Copyright 2017 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
