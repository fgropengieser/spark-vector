/*
 * Copyright 2016 Actian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.actian.spark_vector.colbuffer.decimal

import com.actian.spark_vector.colbuffer._

import java.nio.ByteBuffer
import java.math.BigDecimal

private class DecimalShortColumnBuffer(valueCount: Int, name: String, index: Int, precision: Int, scale: Int, nullable: Boolean) extends
              DecimalColumnBuffer(valueCount, DecimalShortColumnBuffer.DecimalShortSize, name, index, precision, scale, nullable) {

  override protected def putScaled(scaledSource: BigDecimal, buffer: ByteBuffer): Unit = {
    buffer.putShort(scaledSource.shortValue())
  }
}

/** `ColumnBuffer` object for `decimal(<short>)` types. */
object DecimalShortColumnBuffer extends DecimalColumnBufferInstance {
  private final val DecimalShortSize = 2

  final override protected def minPrecision = 3
  final override protected def maxPrecision = 4

  private[colbuffer] override def getNewInstance(name: String, index: Int, precision: Int, scale: Int,
                                                 nullable: Boolean, maxRowCount: Int): ColumnBuffer[Number] = {
    new DecimalShortColumnBuffer(maxRowCount, name, index, precision, scale, nullable)
  }
}
