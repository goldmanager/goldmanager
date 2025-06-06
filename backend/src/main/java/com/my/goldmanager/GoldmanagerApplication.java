/** Copyright 2025 fg12111

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
 * 
 */
package com.my.goldmanager;

import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class GoldmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldmanagerApplication.class, args);
	}

	@Bean
	public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
		DatabaseStartupValidator dsv = new DatabaseStartupValidator();
		dsv.setDataSource(dataSource);
		dsv.setInterval(5);
		dsv.setTimeout(1200);
		return dsv;
	}

	@Bean
	public static BeanFactoryPostProcessor dependsOnPostProcessor() {
	    return bf -> {
	        // Let beans that need the database depend on the DatabaseStartupValidator
	        // like the JPA EntityManagerFactory or Flyway
	        String[] liquibase = bf.getBeanNamesForType(SpringLiquibase.class);
	        Stream.of(liquibase)
	                .map(bf::getBeanDefinition)
	                .forEach(it -> it.setDependsOn("databaseStartupValidator"));

	        String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
	        Stream.of(jpa)
	                .map(bf::getBeanDefinition)
	                .forEach(it -> it.setDependsOn("databaseStartupValidator"));
	    };
	}


}
