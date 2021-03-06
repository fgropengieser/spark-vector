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
package com.actian.spark_vector

/**
 * This package contains a series of helpers to create and manage JDBC connections to `Vector`, to define the equivalence of `SparkSQL` and `Vector` data types,
 * to create tables when they do not exist (respecting input `DataFrame`'s schema), to obtain column metadata for `Vector` tables, and high level methods to initiate
 * loading to `Vector` tables.
 */
package object vector {}
