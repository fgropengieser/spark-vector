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
package com.actian.spark_vector.colbuffer.singles

import com.actian.spark_vector.colbuffer._
import com.actian.spark_vector.colbuffer.util.TimeConversion

import java.nio.ByteBuffer
import java.sql.Date

private class DateColumnBuffer(valueCount: Int, name: String, index: Int, nullable: Boolean) extends
              ColumnBuffer[Date](valueCount, DateColumnBuffer.DateSize, DateColumnBuffer.DateSize, name, index, nullable) {

  override protected def put(source: Date, buffer: ByteBuffer): Unit = {
    TimeConversion.convertLocalDateToUTC(source)
    buffer.putInt((source.getTime() / TimeConversion.MillisecondsInDay + DateColumnBuffer.DaysBeforeEpoch).toInt)
  }
}

/** `ColumnBuffer` object for `ansidate` types. */
object DateColumnBuffer extends ColumnBufferInstance[Date] {
  private final val DateSize = 4
  private final val DateTypeId = "ansidate"
  private final val DaysBeforeEpoch = 719528

  private[colbuffer] override def getNewInstance(name: String, index: Int, precision: Int, scale: Int,
                                                 nullable: Boolean, maxRowCount: Int): ColumnBuffer[Date] = {
    new DateColumnBuffer(maxRowCount, name, index, nullable)
  }

  private[colbuffer] override def supportsColumnType(tpe: String, precision: Int, scale:Int, nullable: Boolean): Boolean = {
    tpe.equalsIgnoreCase(DateTypeId)
  }
}
