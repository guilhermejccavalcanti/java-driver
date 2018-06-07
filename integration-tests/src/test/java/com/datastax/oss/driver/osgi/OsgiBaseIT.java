/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.osgi;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.selectFrom;
import static com.datastax.oss.driver.osgi.BundleOptions.baseOptions;
import static com.datastax.oss.driver.osgi.BundleOptions.driverCoreBundle;
import static com.datastax.oss.driver.osgi.BundleOptions.driverQueryBuilderBundle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.options;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.testinfra.ccm.CustomCcmRule;
import com.datastax.oss.driver.api.testinfra.session.SessionUtils;
import com.datastax.oss.driver.categories.IsolatedTests;
import com.google.common.collect.ObjectArrays;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@Category(IsolatedTests.class)
public abstract class OsgiBaseIT {

  @ClassRule public static CustomCcmRule ccmRule = CustomCcmRule.builder().withNodes(1).build();

  /** @return Additional options that should be used in OSGi environment configuration. */
  public abstract Option[] additionalOptions();

  @Configuration
  public Option[] config() {
    return ObjectArrays.concat(
        options(driverCoreBundle(), driverQueryBuilderBundle(), baseOptions()),
        additionalOptions(),
        Option.class);
  }

  /** @return Additional options that should be used during session construction. */
  public abstract String[] sessionOptions();

  /**
   * A very simple test that ensures a session can be established and a query made when running in
   * an OSGi container.
   */
  @Test
  public void should_connect_and_query() {
    try (CqlSession session = SessionUtils.newSession(ccmRule, sessionOptions())) {
      ResultSet result = session.execute(selectFrom("system", "local").all().build());

      assertThat(result.getAvailableWithoutFetching()).isEqualTo(1);

      Row row = result.one();
      assertThat(row.getString("key")).isEqualTo("local");
    }
  }
}