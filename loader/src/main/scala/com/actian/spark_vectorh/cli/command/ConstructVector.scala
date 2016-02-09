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
package com.actian.spark_vector.loader.command

import com.actian.spark_vector.loader.options._
import com.actian.spark_vector.vector.{ LoadVector, VectorConnectionProperties }
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.types._
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.sql.SQLContext
import com.actian.spark_vector.loader.parsers.VectorHParser
import com.actian.spark_vector.loader.parsers.VectorHArgs

object ConstructVector {

  def execute(config: UserOptions, schema: Option[StructType]): Unit = {
    val conf = new SparkConf()
      .setAppName(s"Spark-VectorH ${config.mode} load into ${config.vector.targetTable}")
      .set("spark.task.maxFailures", "1")
    val sparkContext = new SparkContext(conf)
    val sqlContext = new SQLContext(sparkContext)

    val select = config.mode match {
      case m if (m == VectorHArgs.csvLoad.longName) => CSVRead.registerTempTable(config, sqlContext)
      case m if (m == VectorHArgs.parquetLoad.longName) => ParquetRead.registerTempTable(config, sqlContext)
      case m => throw new IllegalArgumentException("Invalid configuration mode: $m")
    }

    val targetTempTable = VectorTempTable.register(config, sqlContext)

    // Load the line item data into VectorH
    sqlContext.sql(s"insert into table ${targetTempTable} ${select}")
  }
}
