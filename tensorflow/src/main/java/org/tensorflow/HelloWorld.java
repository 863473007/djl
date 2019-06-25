/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.tensorflow;

import com.amazon.ai.TranslateException;
import com.amazon.ai.Translator;
import com.amazon.ai.TranslatorContext;
import com.amazon.ai.ndarray.NDArray;
import com.amazon.ai.ndarray.NDFactory;
import com.amazon.ai.ndarray.NDList;
import com.amazon.ai.ndarray.types.Shape;
import com.amazon.ai.util.Pair;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.tensorflow.engine.TfModel;
import org.tensorflow.engine.TfNDArray;
import org.tensorflow.engine.TfNDFactory;
import org.tensorflow.engine.TfPredictor;

public final class HelloWorld {

    private HelloWorld() {}

    @SuppressWarnings("PMD.SystemPrintln")
    public static void main(String[] args) throws Exception {
        try (NDFactory factory = TfNDFactory.SYSTEM_FACTORY.newSubFactory()) {
            NDArray a = factory.create(new float[] {1.0f, 2.0f});
            System.out.println(a.getShape());
            NDArray s = a.softmax();
            System.out.println(s.getShape());
            System.out.println(Arrays.toString(a.softmax().toFloatArray()));

            TfModel model =
                    TfModel.loadModel(
                            "/Users/kvasist/workplace/Models/models/ssd_inception_v2_coco_2017_11_17/saved_model");
            System.out.println(model.describeInput()[0].getShape());
            System.out.println(model.describeInput()[0].getName());

            String filename = "/Users/kvasist/Downloads/Image.jpeg";
            BufferedImage img = ImageIO.read(new File(filename));
            GenericTranslator translator = new GenericTranslator();
            TfPredictor<BufferedImage, NDList> predictor = new TfPredictor<>(model, translator);
            NDList list = predictor.predict(img);

            for (Pair<String, NDArray> pair : list) {
                System.out.println(pair.getKey() + " " + pair.getValue().getShape().toString());
            }
        }
    }

    private static void bgr2rgb(byte[] data) {
        for (int i = 0; i < data.length; i += 3) {
            byte tmp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = tmp;
        }
    }

    private static final class GenericTranslator implements Translator<BufferedImage, NDList> {

        @Override
        public NDList processInput(TranslatorContext ctx, BufferedImage img)
                throws TranslateException {
            if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                throw new TranslateException("");
            }
            byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
            // ImageIO.read seems to produce BGR-encoded images, but the model expects RGB.
            bgr2rgb(data);
            final int BATCH_SIZE = 1;
            final int CHANNELS = 3;
            int[] shape = new int[] {BATCH_SIZE, img.getHeight(), img.getWidth(), CHANNELS};
            TfNDArray tfNDArray =
                    ((TfNDFactory) ctx.getNDFactory())
                            .create(new Shape(shape), ByteBuffer.wrap(data));
            NDList ndList = new NDList();
            ndList.add("image_tensor", tfNDArray);
            return ndList;
        }

        @Override
        public NDList processOutput(TranslatorContext ctx, NDList list) throws TranslateException {
            return list;
        }
    }
}