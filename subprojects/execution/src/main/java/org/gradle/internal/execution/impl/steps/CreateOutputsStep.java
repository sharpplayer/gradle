/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.execution.impl.steps;

import org.gradle.internal.execution.ExecutionResult;
import org.gradle.internal.execution.OutputFileProperty;
import org.gradle.internal.execution.OutputFileProperty.OutputType;
import org.gradle.internal.execution.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;

import static org.gradle.util.GFileUtils.mkdirs;

public class CreateOutputsStep implements DirectExecutionStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateOutputsStep.class);

    private final DirectExecutionStep delegate;

    public CreateOutputsStep(DirectExecutionStep delegate) {
        this.delegate = delegate;
    }

    @Override
    public ExecutionResult execute(UnitOfWork work) {
        work.visitOutputs(new UnitOfWork.OutputVisitor() {
            @Override
            public void visitOutput(OutputFileProperty output) {
                OutputType type = output.getOutputType();
                for (File outputRoot : output.getFiles()) {
                    ensureOutput(output, outputRoot, type);
                }
            }
        });
        return delegate.execute(work);
    }

    private static void ensureOutput(OutputFileProperty output, @Nullable File outputRoot, OutputType type) {
        if (outputRoot == null) {
            LOGGER.debug("Not ensuring directory exists for property {}, because value is null", output.getName());
            return;
        }
        switch (type) {
            case DIRECTORY:
                LOGGER.debug("Ensuring directory exists for property {} at {}", output.getName(), outputRoot);
                mkdirs(outputRoot);
                break;
            case FILE:
                LOGGER.debug("Ensuring parent directory exists for property {} at {}", output.getName(), outputRoot);
                mkdirs(outputRoot.getParentFile());
                break;
            default:
                throw new AssertionError();
        }
    }
}
