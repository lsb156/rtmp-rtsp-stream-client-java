package com.pedro.encoder.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import androidx.annotation.NonNull;

/**
 * Created by pedro on 18/09/19.
 */
public interface EncoderCallback {
  void inputAvailable(@NonNull MediaCodec mediaCodec, int inBufferIndex);

  void outputAvailable(@NonNull MediaCodec mediaCodec, int outBufferIndex,
      @NonNull MediaCodec.BufferInfo bufferInfo);

  void formatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat);
}
