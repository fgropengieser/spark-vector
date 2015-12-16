package com.actian.spark_vectorh.vector

import org.apache.hadoop.fs.Path
import org.apache.spark.{ Logging, SparkContext }
import org.apache.spark.sql.types._
import org.scalatest.{ FunSuite, Matchers }
import org.scalatest.prop.PropertyChecks

import com.actian.spark_vectorh.test.tags.IntegrationTest
import com.actian.spark_vectorh.util.StructTypeUtil
import com.actian.spark_vectorh.vector.ErrorCodes._
import com.actian.spark_vectorh.vector.Vector.Field2Column
import com.actian.spark_vectorh.vector.VectorFixture.withTable

class VectorTest extends FunSuite with Matchers with PropertyChecks with VectorFixture with Logging {

  test("getTableSchema for existing table", IntegrationTest) {
    def createTable(tableName: String): Unit = {
      VectorJDBC.withJDBC(connectionProps) { cxn =>
        cxn.dropTable(tableName)
        cxn.executeStatement(createTableStatement(tableName, allTypesColumnMD))
      }
    }

    withTable(createTable) { tableName =>
      val schema = Vector.getTableSchema(connectionProps, tableName).map(_.structField)
      schema should be (allTypesColumnMD.map(_.structField))
    }
  }

  test("getTableSchema for non-existing table", IntegrationTest) {
    withTable(func => Unit) { tableName =>
      intercept[VectorException] {
        Vector.getTableSchema(connectionProps, tableName)
      }
    }
  }

  test("getTableSchema with bad cxn props", IntegrationTest) {
    withTable(func => Unit) { tableName =>
      val ex = intercept[VectorException] {
        Vector.getTableSchema(VectorConnectionProperties("host", "instance", "db"), tableName)
      }

      ex.errorCode should be (sqlException)
    }
  }

  private def validatePath(sc: SparkContext, path: Path): Boolean = {
    val fs = path.getFileSystem(sc.hadoopConfiguration)
    if (fs.exists(path) && fs.isDirectory(path)) true else false
  }

  test("applyFieldMap") {

    val sourceSchema = StructTypeUtil.createSchema(("a", StringType), ("b", StringType))
    val targetSchema = StructTypeUtil.createSchema(("B", StringType), ("A", StringType))

    val result = Vector.applyFieldMap(Map("a"->"A", "b"->"B"), sourceSchema, targetSchema)

    result should be (sourceSchema.map(_.name).zip(targetSchema.reverseMap(_.name)).map {case(a: String, b: String) => Field2Column(a,b)})
  }

  test("applyFieldMap with an empty map") {

    val sourceSchema = StructTypeUtil.createSchema(("a", StringType), ("b", StringType))
    val targetSchema = StructTypeUtil.createSchema(("B", StringType), ("A", StringType))

    val result = Vector.applyFieldMap(Map(), sourceSchema, targetSchema)
    result should be (sourceSchema.map(_.name).zip(targetSchema.map(_.name)).map {case(a: String, b: String) => Field2Column(a,b)})
  }

  test("applyFieldMap with an empty map and unbalanced cardinality") {

    val sourceSchema = StructTypeUtil.createSchema(("a", StringType))
    val targetSchema = StructTypeUtil.createSchema(("B", StringType), ("A", StringType))

    val ex = intercept[VectorException] {
      Vector.applyFieldMap(Map(), sourceSchema, targetSchema)
    }

    ex.errorCode should be (invalidNumberOfInputs)
  }

  test("applyFieldMap with too many inputs") {

    val sourceSchema = StructTypeUtil.createSchema(("a", StringType), ("b", StringType), ("c", StringType))
    val targetSchema = StructTypeUtil.createSchema(("B", StringType), ("A", StringType))

    val ex = intercept[VectorException] {
      Vector.applyFieldMap(Map("a"->"A", "b"->"B", "c"->"C"), sourceSchema, targetSchema)
    }

    ex.errorCode should be (invalidNumberOfInputs)
  }

  test("applyFieldMap to non-existing column") {

    val sourceSchema = StructTypeUtil.createSchema(("a", StringType), ("b", StringType), ("c", StringType), ("d", StringType))
    val targetSchema = StructTypeUtil.createSchema(("B", StringType), ("A", StringType), ("C", StringType), ("E", StringType))

    val ex = intercept[VectorException] {
      Vector.applyFieldMap(Map("a"->"A", "b"->"B", "c"->"C", "d"->"D"), sourceSchema, targetSchema)
    }

    ex.errorCode should be (noSuchColumn)
  }

  test("applyFieldMap map has reference to non-existing field") {

    val sourceSchema = StructTypeUtil.createSchema(("b", StringType))
    val targetSchema = StructTypeUtil.createSchema(("A", StringType))

    val ex = intercept[VectorException] {
      Vector.applyFieldMap(Map("a"->"A"), sourceSchema, targetSchema)
    }

    ex.errorCode should be (noSuchSourceField)
  }

  private val testColumns = StructType(Seq(
    StructField("a", StringType, false),
    StructField("b", StringType, true)
  ))

  test("validateColumns with required column") {
    Vector.validateColumns(testColumns, Seq("a"))
  }

  test("validateColumns with all columns") {
    Vector.validateColumns(testColumns, Seq("a", "b"))
  }

  test("validateColumns excluding non-null column") {
    val ex = intercept[VectorException] {
      Vector.validateColumns(testColumns, Seq("b"))
    }

    ex.errorCode should be (missingNonNullColumn)
  }
}
