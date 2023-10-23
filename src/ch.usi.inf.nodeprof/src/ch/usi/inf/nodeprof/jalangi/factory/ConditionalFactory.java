/* *****************************************************************************
 * Copyright 2018 Dynamic Analysis Group, Università della Svizzera Italiana (USI)
 * Copyright (c) 2018, 2023, Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************/
package ch.usi.inf.nodeprof.jalangi.factory;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.js.aux.ModifiedResultStack;
import com.oracle.truffle.js.runtime.objects.JSDynamicObject;

import ch.usi.inf.nodeprof.handlers.BaseEventHandlerNode;
import ch.usi.inf.nodeprof.handlers.BinaryEventHandler;
import ch.usi.inf.nodeprof.handlers.ConditionalEventHandler;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.Undefined;

public class ConditionalFactory extends AbstractFactory {
    private final boolean isBinary;

    public ConditionalFactory(Object jalangiAnalysis, JSDynamicObject post,
                    boolean isBinary) {
        super("conditional", jalangiAnalysis, null, post);
        this.isBinary = isBinary;
    }

    @Override
    public BaseEventHandlerNode create(EventContext context) {
        if (!isBinary) {
            return new ConditionalEventHandler(context) {
                @Child CallbackNode cbNode = new CallbackNode();

                @Override
                public void executePost(VirtualFrame frame, Object result,
                                Object[] inputs) throws InteropException {
                    if (post != null && isConditional()) {
                        Object ret  = cbNode.postCall(this, jalangiAnalysis, post, getSourceIID(), convertResult(result));
                        if (ret != null && ret != Undefined.instance && JSObject.isJSObject(ret)) {
                            Object hookedResult = cbNode.interopLibrary.readMember(ret, "result");
                            if (hookedResult instanceof Boolean) {
                                ModifiedResultStack.results.put(getSourceSectionForIID(), hookedResult);
                            }
                        }
                    }
                }
            };
        } else {
            return new BinaryEventHandler(context) {
                @Child CallbackNode cbNode = new CallbackNode();

                @Override
                public void executePost(VirtualFrame frame, Object result,
                                Object[] inputs) throws InteropException {
                    if (post != null && this.isLogic()) {
                        cbNode.postCall(this, jalangiAnalysis, post, getSourceIID(), convertResult(result));
                    }
                }
            };
        }
    }

}
