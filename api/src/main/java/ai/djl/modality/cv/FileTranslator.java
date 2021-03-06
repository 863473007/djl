/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.modality.cv;

import ai.djl.modality.cv.util.BufferedImageUtils;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Built-in {@code Translator} that provides image pre-processing from file path.
 *
 * @param <T> the output object type
 */
public class FileTranslator<T> implements Translator<Path, T> {

    private Translator<BufferedImage, T> translator;

    /**
     * Creates a {@code FileTranslator} instance.
     *
     * @param translator a {@code Translator} that can process image
     */
    public FileTranslator(Translator<BufferedImage, T> translator) {
        this.translator = translator;
    }

    /** {@inheritDoc} */
    @Override
    public NDList processInput(TranslatorContext ctx, Path input) throws Exception {
        BufferedImage image = BufferedImageUtils.fromFile(input);
        return translator.processInput(ctx, image);
    }

    /** {@inheritDoc} */
    @Override
    public T processOutput(TranslatorContext ctx, NDList list) throws Exception {
        return translator.processOutput(ctx, list);
    }
}
